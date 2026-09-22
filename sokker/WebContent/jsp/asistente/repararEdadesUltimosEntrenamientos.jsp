<%@ page import="java.io.File" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.StringReader" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.nio.file.AtomicMoveNotSupportedException" %>
<%@ page import="java.nio.file.Files" %>
<%@ page import="java.nio.file.Path" %>
<%@ page import="java.nio.file.StandardCopyOption" %>
<%@ page import="java.nio.file.StandardOpenOption" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.UUID" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="com.formulamanager.sokker.auxiliares.SystemUtil" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%!
    /*
     * Reparación puntual para los datos creados alrededor del cambio de temporada de septiembre de 2026.
     * No forma parte de la lógica normal de actualización y debe eliminarse después de ejecutarla.
     *
     * Temporada anterior: 1197..1209
     * Temporada actual:   1210..1222
     */
    private static final int PREVIOUS_SEASON_START_REPAIR = 1197;
    private static final int PREVIOUS_SEASON_END_REPAIR = 1209;
    private static final int CURRENT_SEASON_START_REPAIR = 1210;
    private static final int CURRENT_SEASON_END_REPAIR = 1222;
    private static final int MIN_CLUB_TID_REPAIR = 801;
    private static final int JORNADA_NUEVO_ENTRENO_REPAIR = 993;

    private static final String MARKER_FILE_REPAIR = ".repair_training_ages_20260922.done";
    private static final String BACKUP_DIR_REPAIR = "repair_training_ages_20260922_backup";

    private static final Pattern TEAM_FILE_REPAIR = Pattern.compile("[0-9]+\\.properties");
    private static final Pattern PLAYER_LINE_REPAIR = Pattern.compile("(?m)^([0-9]+)([=:])(.*?)(\\r?)$");

    private RepairSummary repairDirectory(File directory) throws IOException {
        if (!directory.isDirectory()) {
            throw new IOException("No existe el directorio de datos: " + directory.getAbsolutePath());
        }

        File[] files = directory.listFiles(file -> file.isFile()
                && TEAM_FILE_REPAIR.matcher(file.getName()).matches()
                && clubTid(file.getName()) >= MIN_CLUB_TID_REPAIR);
        if (files == null) {
            throw new IOException("No se puede listar el directorio de datos: " + directory.getAbsolutePath());
        }

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return a.getName().compareTo(b.getName());
            }
        });

        Path backupDirectory = directory.toPath().resolve(BACKUP_DIR_REPAIR);
        RepairSummary summary = new RepairSummary();
        for (File file : files) {
            FileRepairResult result = repairFile(file.toPath(), backupDirectory);
            summary.modifiedFiles += result.modified ? 1 : 0;
            summary.modifiedPlayers += result.modifiedPlayers;
            summary.modifiedSnapshots += result.modifiedSnapshots;
            summary.concurrentSkips += result.concurrentSkip ? 1 : 0;
        }
        return summary;
    }

    private FileRepairResult repairFile(Path path, Path backupDirectory) throws IOException {
        byte[] originalBytes = Files.readAllBytes(path);
        String original = new String(originalBytes, StandardCharsets.ISO_8859_1);
        RepairResult repaired = repairContent(original);
        if (repaired.modifiedSnapshots == 0) {
            return new FileRepairResult(false, false, 0, 0);
        }

        RepairResult secondPass = repairContent(repaired.content);
        if (secondPass.modifiedSnapshots != 0 || !secondPass.content.equals(repaired.content)) {
            throw new IOException("La reparación no es idempotente para " + path.getFileName());
        }
        validateProperties(repaired.content, path);

        // No pisamos una actualización del usuario que haya llegado mientras analizábamos el fichero.
        if (!Arrays.equals(originalBytes, Files.readAllBytes(path))) {
            return new FileRepairResult(false, true, 0, 0);
        }

        Files.createDirectories(backupDirectory);
        Path backup = backupDirectory.resolve(path.getFileName().toString());
        if (!Files.exists(backup)) {
            Files.copy(path, backup, StandardCopyOption.COPY_ATTRIBUTES);
        }

        Path parent = path.toAbsolutePath().getParent();
        Path temp = Files.createTempFile(parent, path.getFileName().toString(), ".training-age.tmp");
        try {
            Files.write(temp,
                    repaired.content.getBytes(StandardCharsets.ISO_8859_1),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            String written = new String(Files.readAllBytes(temp), StandardCharsets.ISO_8859_1);
            if (!written.equals(repaired.content)) {
                throw new IOException("Error verificando el fichero temporal de " + path.getFileName());
            }

            // Segunda comprobación justo antes del replace atómico.
            if (!Arrays.equals(originalBytes, Files.readAllBytes(path))) {
                return new FileRepairResult(false, true, 0, 0);
            }

            try {
                Files.move(temp, path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            }

            return new FileRepairResult(true, false, repaired.modifiedPlayers, repaired.modifiedSnapshots);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private RepairResult repairContent(String content) throws IOException {
        List<PlayerRepair> repairs = new ArrayList<PlayerRepair>();
        Matcher matcher = PLAYER_LINE_REPAIR.matcher(content);

        while (matcher.find()) {
            String rawValue = matcher.group(3);
            if (hasContinuation(rawValue)) {
                continue;
            }

            ParsedPlayer parsed = parsePlayer(unescapePropertyValue(rawValue));
            if (parsed == null) {
                continue;
            }

            List<Integer> ageIndices = findAgeIndicesToRepair(parsed);
            if (!ageIndices.isEmpty()) {
                repairs.add(new PlayerRepair(
                        matcher.start(3),
                        matcher.end(3),
                        rawValue,
                        ageIndices,
                        parsed.snapshots.get(0).edad - 1));
            }
        }

        if (repairs.isEmpty()) {
            return new RepairResult(content, 0, 0);
        }

        StringBuilder fixed = new StringBuilder(content);
        int modifiedPlayers = 0;
        int modifiedSnapshots = 0;

        for (int i = repairs.size() - 1; i >= 0; i--) {
            PlayerRepair repair = repairs.get(i);
            String[] rawTokens = repair.rawValue.split(",", -1);

            for (Integer ageIndex : repair.ageIndices) {
                if (ageIndex < 0 || ageIndex >= rawTokens.length) {
                    throw new IOException("Formato de jugador inconsistente durante la reparación");
                }
                rawTokens[ageIndex] = Integer.toString(repair.targetAge);
                modifiedSnapshots++;
            }

            modifiedPlayers++;
            fixed.replace(repair.valueStart, repair.valueEnd, join(rawTokens));
        }

        return new RepairResult(fixed.toString(), modifiedPlayers, modifiedSnapshots);
    }

    private List<Integer> findAgeIndicesToRepair(ParsedPlayer parsed) {
        List<Integer> result = new ArrayList<Integer>();
        if (parsed.snapshots.size() < 2) {
            return result;
        }

        Snapshot latest = parsed.snapshots.get(0);

        // Este JSP es deliberadamente temporal: solo repara el cambio 1209 -> 1210.
        // Si se deja por error en el servidor durante otra temporada, no tocará nada.
        if (latest.jornada < CURRENT_SEASON_START_REPAIR || latest.jornada > CURRENT_SEASON_END_REPAIR) {
            return result;
        }

        int currentAge = latest.edad;
        int expectedPreviousAge = currentAge - 1;
        int previousJornada = Integer.MAX_VALUE;

        for (Snapshot snapshot : parsed.snapshots) {
            if (snapshot.jornada >= previousJornada) {
                // Cadena dañada o en un formato inesperado: no tocamos este jugador.
                return new ArrayList<Integer>();
            }
            previousJornada = snapshot.jornada;

            if (snapshot == latest || snapshot.jornada >= CURRENT_SEASON_START_REPAIR) {
                continue;
            }

            if (snapshot.jornada < PREVIOUS_SEASON_START_REPAIR) {
                break;
            }

            if (snapshot.jornada <= PREVIOUS_SEASON_END_REPAIR) {
                if (snapshot.edad == currentAge) {
                    // Contaminación de la versión anterior: heredó la edad actual.
                    result.add(snapshot.edadIndex);
                } else if (snapshot.edad != expectedPreviousAge) {
                    // Un valor distinto de edad actual / edad anterior es ambiguo. No arriesgamos datos.
                    return new ArrayList<Integer>();
                }
            }
        }

        return result;
    }

    private ParsedPlayer parsePlayer(String value) {
        try {
            String[] tokens = value.split(",", -1);
            int index = 11; // nombre, tid, demarcación, país, fecha, actualizado, tarjetas, nt, lesión, venta, notas
            if (index >= tokens.length) {
                return null;
            }

            if (startsWithMinus(tokens, index)) {
                index += 4; // salario, altura, peso, IMC
                if (index >= tokens.length) {
                    return null;
                }

                if (startsWithMinus(tokens, index)) {
                    index += 2; // talento, destacar
                    if (index >= tokens.length) {
                        return null;
                    }
                }

                if (tokens[index].startsWith("#")) {
                    index++; // color
                    if (startsWithMinus(tokens, index)) {
                        index++; // usuario2
                    }
                    if (startsWithMinus(tokens, index)) {
                        index++; // bot
                    }
                }
            }

            List<Snapshot> snapshots = new ArrayList<Snapshot>();
            while (index < tokens.length && !"*".equals(tokens[index])) {
                if (index + 1 >= tokens.length) {
                    return null;
                }

                int jornada = Integer.parseInt(tokens[index]);
                int edad = Integer.parseInt(tokens[index + 1]);
                snapshots.add(new Snapshot(jornada, edad, index + 1));

                int next = afterSnapshot(tokens, index, jornada);
                if (next <= index) {
                    return null;
                }
                index = next;
            }

            return snapshots.isEmpty() ? null : new ParsedPlayer(snapshots);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private int afterSnapshot(String[] tokens, int jornadaIndex, int jornada) {
        int index = jornadaIndex + 2;

        // valor + 8 habilidades
        if (index + 9 > tokens.length) {
            return -1;
        }
        index += 9;

        if (startsWithMinus(tokens, index)) {
            index++; // lesión
        }

        // forma, demarcación de entrenamiento, minutos
        if (index + 3 > tokens.length) {
            return -1;
        }
        index += 3;

        if (startsWithMinus(tokens, index)) {
            if (index + 3 > tokens.length) {
                return -1;
            }
            index += 3; // experiencia, disciplina táctica, trabajo en equipo
        }

        if (jornada >= JORNADA_NUEVO_ENTRENO_REPAIR) {
            if (index >= tokens.length) {
                return -1;
            }
            index++; // entrenamiento avanzado
        }

        return index;
    }

    private int clubTid(String fileName) {
        try {
            return Integer.parseInt(fileName.substring(0, fileName.length() - ".properties".length()));
        } catch (RuntimeException e) {
            return -1;
        }
    }

    private boolean startsWithMinus(String[] tokens, int index) {
        return index < tokens.length && tokens[index] != null && tokens[index].startsWith("-");
    }

    private boolean hasContinuation(String rawValue) {
        int backslashes = 0;
        for (int i = rawValue.length() - 1; i >= 0 && rawValue.charAt(i) == '\\'; i--) {
            backslashes++;
        }
        return (backslashes & 1) != 0;
    }

    private String unescapePropertyValue(String rawValue) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader("value=" + rawValue + "\n"));
        return properties.getProperty("value");
    }

    private void validateProperties(String content, Path path) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        if (properties.isEmpty() && content.length() > 0) {
            throw new IOException("El fichero reparado no contiene propiedades: " + path.getFileName());
        }
    }

    private void markDone(Path directory, RepairSummary summary) throws IOException {
        Path marker = directory.resolve(MARKER_FILE_REPAIR);
        String text = "executed=true\n"
                + "modifiedFiles=" + summary.modifiedFiles + "\n"
                + "modifiedPlayers=" + summary.modifiedPlayers + "\n"
                + "modifiedSnapshots=" + summary.modifiedSnapshots + "\n";
        Files.write(marker, text.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    private String join(String[] tokens) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                result.append(',');
            }
            result.append(tokens[i]);
        }
        return result.toString();
    }

    private String snapshot(int jornada, int edad) {
        return jornada + "," + edad + ",100000,5,5,5,5,5,5,5,5,-0,10,DEF,100.0,-1,1,1,true";
    }

    private String player(int pid, String snapshots) {
        return pid + "=Jugador " + pid + ",1000,DEF,1,20/09/2026 10\\:00,true,0,0,0,,,-1000,180,750,2400,-,false,\\#,-,-false," + snapshots + ",*\n";
    }

    private void selfTestRepair() throws Exception {
        String elio = player(1,
                snapshot(1210, 19) + "," + snapshot(1209, 19) + "," + snapshot(1208, 19));
        String alreadyCorrect = player(2,
                snapshot(1210, 20) + "," + snapshot(1209, 19) + "," + snapshot(1208, 19));
        String mixed = player(3,
                snapshot(1211, 24) + "," + snapshot(1210, 24) + "," + snapshot(1209, 24) + "," + snapshot(1208, 23));
        String outsideRepairSeason = player(4,
                snapshot(1223, 21) + "," + snapshot(1222, 21));

        String content = "# cabecera\n"
                + "future_key=keep\\:exactly\n"
                + elio
                + alreadyCorrect
                + mixed
                + outsideRepairSeason;

        RepairResult fixed = repairContent(content);
        requireRepair(fixed.modifiedPlayers == 2, "Debe reparar exactamente los dos jugadores contaminados");
        requireRepair(fixed.modifiedSnapshots == 3, "Debe reparar las tres edades contaminadas");
        requireRepair(fixed.content.contains(snapshot(1210, 19) + "," + snapshot(1209, 18) + "," + snapshot(1208, 18)),
                "Debe reparar los dos primeros y únicos entrenamientos de Elio");
        requireRepair(fixed.content.contains(snapshot(1211, 24) + "," + snapshot(1210, 24) + "," + snapshot(1209, 23) + "," + snapshot(1208, 23)),
                "Debe reparar solo el snapshot contaminado dentro de la temporada anterior");
        requireRepair(fixed.content.contains("future_key=keep\\:exactly"), "Debe conservar claves desconocidas");
        requireRepair(fixed.content.contains(outsideRepairSeason), "No debe tocar temporadas posteriores");

        RepairResult second = repairContent(fixed.content);
        requireRepair(second.modifiedSnapshots == 0, "La reparación debe ser idempotente");
        requireRepair(second.content.equals(fixed.content), "Una segunda pasada no debe modificar datos");
    }

    private void requireRepair(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private String escapeHtmlRepair(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static final class RepairResult {
        final String content;
        final int modifiedPlayers;
        final int modifiedSnapshots;

        RepairResult(String content, int modifiedPlayers, int modifiedSnapshots) {
            this.content = content;
            this.modifiedPlayers = modifiedPlayers;
            this.modifiedSnapshots = modifiedSnapshots;
        }
    }

    private static final class FileRepairResult {
        final boolean modified;
        final boolean concurrentSkip;
        final int modifiedPlayers;
        final int modifiedSnapshots;

        FileRepairResult(boolean modified, boolean concurrentSkip, int modifiedPlayers, int modifiedSnapshots) {
            this.modified = modified;
            this.concurrentSkip = concurrentSkip;
            this.modifiedPlayers = modifiedPlayers;
            this.modifiedSnapshots = modifiedSnapshots;
        }
    }

    private static final class RepairSummary {
        int modifiedFiles;
        int modifiedPlayers;
        int modifiedSnapshots;
        int concurrentSkips;
    }

    private static final class PlayerRepair {
        final int valueStart;
        final int valueEnd;
        final String rawValue;
        final List<Integer> ageIndices;
        final int targetAge;

        PlayerRepair(int valueStart, int valueEnd, String rawValue, List<Integer> ageIndices, int targetAge) {
            this.valueStart = valueStart;
            this.valueEnd = valueEnd;
            this.rawValue = rawValue;
            this.ageIndices = ageIndices;
            this.targetAge = targetAge;
        }
    }

    private static final class ParsedPlayer {
        final List<Snapshot> snapshots;

        ParsedPlayer(List<Snapshot> snapshots) {
            this.snapshots = snapshots;
        }
    }

    private static final class Snapshot {
        final int jornada;
        final int edad;
        final int edadIndex;

        Snapshot(int jornada, int edad, int edadIndex) {
            this.jornada = jornada;
            this.edad = edad;
            this.edadIndex = edadIndex;
        }
    }
%>

<%
    if (session.getAttribute("usuario") == null) {
        response.sendRedirect(request.getContextPath() + "/asistente");
        return;
    }

    File dataDirectory = new File(SystemUtil.getVar(SystemUtil.PATH));
    Path marker = dataDirectory.toPath().resolve(MARKER_FILE_REPAIR);
    boolean alreadyExecuted = Files.exists(marker);
    String result = null;
    String error = null;
    String csrf = (String) session.getAttribute("repairTrainingAgesCsrf");

    try {
        selfTestRepair();
    } catch (Throwable e) {
        error = "El autotest de la reparación ha fallado: " + e.getClass().getName() + ": " + e.getMessage();
    }

    if (error == null && "POST".equalsIgnoreCase(request.getMethod())) {
        String supplied = request.getParameter("csrf");
        if (csrf == null || supplied == null || !csrf.equals(supplied)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        session.removeAttribute("repairTrainingAgesCsrf");

        try {
            if (Files.exists(marker)) {
                result = "La reparación ya se ejecutó anteriormente. No se ha modificado nada.";
            } else {
                RepairSummary summary = repairDirectory(dataDirectory);
                if (summary.concurrentSkips == 0) {
                    markDone(dataDirectory.toPath(), summary);
                    result = "Reparación terminada. Ficheros modificados: " + summary.modifiedFiles
                            + ", jugadores corregidos: " + summary.modifiedPlayers
                            + ", edades de entrenamientos corregidas: " + summary.modifiedSnapshots + ".";
                } else {
                    result = "Se corrigieron " + summary.modifiedSnapshots + " edades, pero "
                            + summary.concurrentSkips + " fichero(s) cambiaron durante la reparación. "
                            + "No se ha creado la marca de finalización; vuelve a ejecutar el JSP.";
                }
            }
        } catch (Throwable e) {
            error = e.getClass().getName() + ": " + e.getMessage();
        }
    } else if (error == null && !alreadyExecuted) {
        csrf = UUID.randomUUID().toString();
        session.setAttribute("repairTrainingAgesCsrf", csrf);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Reparar edades de entrenamientos</title>
</head>
<body>
<% if (error != null) { %>
    <h2>Error</h2>
    <pre><%= escapeHtmlRepair(error) %></pre>
<% } else if (result != null) { %>
    <h2><%= escapeHtmlRepair(result) %></h2>
    <% if (Files.exists(marker)) { %>
        <p>La reparación queda bloqueada por la marca <code><%= escapeHtmlRepair(MARKER_FILE_REPAIR) %></code>.</p>
        <p>Ya puedes eliminar este JSP del despliegue.</p>
    <% } %>
<% } else if (alreadyExecuted) { %>
    <h2>La reparación ya fue ejecutada.</h2>
    <p>Este JSP no volverá a modificar ningún fichero.</p>
    <p>Ya puedes eliminarlo del despliegue.</p>
<% } else { %>
    <h2>Reparar edades de los últimos entrenamientos</h2>
    <p>Corrige únicamente jugadores de clubes cuyo snapshot actual pertenece a la temporada 1210-1222.</p>
    <p>En la temporada anterior (1197-1209), cualquier entrenamiento que heredó por error la edad actual se cambia a edad actual - 1.</p>
    <p>No toca selecciones, históricos archivados, otras temporadas ni campos distintos de la edad.</p>
    <p>Antes de modificar cada fichero se guarda una copia en <code><%= escapeHtmlRepair(BACKUP_DIR_REPAIR) %></code>.</p>
    <form method="post">
        <input type="hidden" name="csrf" value="<%= escapeHtmlRepair(csrf) %>">
        <button type="submit">Ejecutar reparación una sola vez</button>
    </form>
<% } %>
</body>
</html>
