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
     * Reparación puntual, manual y de una sola ejecución para el cambio de
     * temporada de septiembre de 2026. Toda la implementación vive en este
     * JSP para poder retirarla del servidor después de ejecutarla.
     *
     * Referencia de edad actual:
     *   1210 si el equipo ya se actualizó esta semana.
     *   1209 si todavía no se ha actualizado.
     *
     * Único cambio permitido:
     *   snapshots anteriores de 1197..1209 -> edad actual - 1.
     */
    private static final int MIN_CLUB_TID_REPAIR = 801;
    private static final int NEW_TRAINING_FORMAT_WEEK_REPAIR = 993;
    private static final Pattern TEAM_FILE_REPAIR = Pattern.compile("[0-9]+\\.properties");
    private static final Pattern PLAYER_LINE_REPAIR = Pattern.compile("(?m)^([0-9]+)([=:])(.*?)(\\r?)$");

    private Integer expectedHistoricalAge(int latestWeek, int latestAge, int snapshotWeek) {
        if (latestWeek != 1209 && latestWeek != 1210) {
            return null;
        }

        if (snapshotWeek >= latestWeek) {
            return null;
        }

        if (snapshotWeek < 1197 || snapshotWeek > 1209) {
            return null;
        }

        return Integer.valueOf(latestAge - 1);
    }

    private RepairSummary repairDirectory(File directory) throws IOException {
        if (directory == null || !directory.isDirectory()) {
            throw new IOException("No existe el directorio de datos: "
                    + (directory == null ? "null" : directory.getAbsolutePath()));
        }

        File[] files = directory.listFiles(new java.io.FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile()
                        && TEAM_FILE_REPAIR.matcher(file.getName()).matches()
                        && clubTid(file.getName()) >= MIN_CLUB_TID_REPAIR;
            }
        });
        if (files == null) {
            throw new IOException("No se puede listar el directorio de datos: " + directory.getAbsolutePath());
        }

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return a.getName().compareTo(b.getName());
            }
        });

        RepairSummary summary = new RepairSummary();
        for (File file : files) {
            summary.scannedFiles++;
            FileRepairResult result = repairFile(file.toPath());
            summary.modifiedFiles += result.modified ? 1 : 0;
            summary.modifiedPlayers += result.modifiedPlayers;
            summary.modifiedSnapshots += result.modifiedSnapshots;
            summary.concurrentSkips += result.concurrentSkip ? 1 : 0;
        }
        return summary;
    }

    private FileRepairResult repairFile(Path path) throws IOException {
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

        if (!Arrays.equals(originalBytes, Files.readAllBytes(path))) {
            return new FileRepairResult(false, true, 0, 0);
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
            if (parsed == null || parsed.snapshots.size() < 2) {
                continue;
            }

            List<AgeReplacement> replacements = findAgeReplacements(parsed);
            if (!replacements.isEmpty()) {
                repairs.add(new PlayerRepair(
                        matcher.start(3),
                        matcher.end(3),
                        rawValue,
                        replacements));
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

            for (AgeReplacement replacement : repair.replacements) {
                if (replacement.ageIndex < 0 || replacement.ageIndex >= rawTokens.length) {
                    throw new IOException("Formato de jugador inconsistente durante la reparación");
                }
                rawTokens[replacement.ageIndex] = Integer.toString(replacement.age);
                modifiedSnapshots++;
            }

            modifiedPlayers++;
            fixed.replace(repair.valueStart, repair.valueEnd, join(rawTokens));
        }

        return new RepairResult(fixed.toString(), modifiedPlayers, modifiedSnapshots);
    }

    private List<AgeReplacement> findAgeReplacements(ParsedPlayer parsed) {
        List<AgeReplacement> result = new ArrayList<AgeReplacement>();
        Snapshot latest = parsed.snapshots.get(0);
        int previousWeek = Integer.MAX_VALUE;

        for (Snapshot snapshot : parsed.snapshots) {
            if (snapshot.week >= previousWeek) {
                return new ArrayList<AgeReplacement>();
            }
            previousWeek = snapshot.week;

            Integer expectedAge = expectedHistoricalAge(latest.week, latest.age, snapshot.week);
            if (expectedAge != null && snapshot.age != expectedAge.intValue()) {
                result.add(new AgeReplacement(snapshot.ageIndex, expectedAge.intValue()));
            }
        }

        return result;
    }

    private ParsedPlayer parsePlayer(String value) {
        try {
            String[] tokens = value.split(",", -1);
            int index = 11;
            if (index >= tokens.length) {
                return null;
            }

            if (startsWithMinus(tokens, index)) {
                index += 4;
                if (index >= tokens.length) {
                    return null;
                }

                if (startsWithMinus(tokens, index)) {
                    index += 2;
                    if (index >= tokens.length) {
                        return null;
                    }
                }

                if (tokens[index].startsWith("#")) {
                    index++;
                    if (startsWithMinus(tokens, index)) {
                        index++;
                    }
                    if (startsWithMinus(tokens, index)) {
                        index++;
                    }
                }
            }

            List<Snapshot> snapshots = new ArrayList<Snapshot>();
            while (index < tokens.length && !"*".equals(tokens[index])) {
                if (index + 1 >= tokens.length) {
                    return null;
                }

                int week = Integer.parseInt(tokens[index]);
                int age = Integer.parseInt(tokens[index + 1]);
                snapshots.add(new Snapshot(week, age, index + 1));

                int next = afterSnapshot(tokens, index, week);
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

    private int afterSnapshot(String[] tokens, int weekIndex, int week) {
        int index = weekIndex + 2;

        if (index + 9 > tokens.length) {
            return -1;
        }
        index += 9;

        if (startsWithMinus(tokens, index)) {
            index++;
        }

        if (index + 3 > tokens.length) {
            return -1;
        }
        index += 3;

        if (startsWithMinus(tokens, index)) {
            if (index + 3 > tokens.length) {
                return -1;
            }
            index += 3;
        }

        if (week >= NEW_TRAINING_FORMAT_WEEK_REPAIR) {
            if (index >= tokens.length) {
                return -1;
            }
            index++;
        }

        return index;
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

    private int clubTid(String fileName) {
        try {
            return Integer.parseInt(fileName.substring(0, fileName.length() - ".properties".length()));
        } catch (RuntimeException e) {
            return -1;
        }
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

    private String snapshot(int week, int age) {
        return week + "," + age + ",100000,5,5,5,5,5,5,5,5,-0,10,DEF,100.0,-1,1,1,true";
    }

    private String player(int pid, String snapshots) {
        return pid + "=Jugador " + pid
                + ",1000,DEF,1,20/09/2026 10\\:00,true,0,0,0,,,-1000,180,750,2400,-,false,\\#,-,-false,"
                + snapshots + ",*\n";
    }

    private void selfTestRepair() throws Exception {
        String damaged = player(77,
                snapshot(1210, 25) + ","
                + snapshot(1209, 23) + ","
                + snapshot(1208, 25) + ","
                + snapshot(1197, 24) + ","
                + snapshot(1196, 23));
        String notUpdated = player(88,
                snapshot(1209, 25) + ","
                + snapshot(1208, 23) + ","
                + snapshot(1197, 25));
        String unsupportedReference = player(99,
                snapshot(1211, 30) + ","
                + snapshot(1209, 28));

        String content = "future_key=keep\\:exactly\n"
                + damaged
                + notUpdated
                + unsupportedReference;

        RepairResult fixed = repairContent(content);
        requireRepair(fixed.content.contains(snapshot(1210, 25)),
                "La edad actual de 1210 debe conservarse");
        requireRepair(fixed.content.contains(snapshot(1209, 24)),
                "La jornada 13 debe quedar en edad actual - 1 cuando 1210 es la referencia");
        requireRepair(fixed.content.contains(snapshot(1208, 24)),
                "Las jornadas anteriores deben quedar en edad actual - 1");
        requireRepair(fixed.content.contains(snapshot(1197, 24)),
                "La jornada 1 de la temporada anterior debe quedar en edad actual - 1");
        requireRepair(fixed.content.contains(snapshot(1196, 23)),
                "No se debe tocar una temporada más antigua");
        requireRepair(fixed.content.contains(snapshot(1209, 25)),
                "Si el equipo aún no se actualizó, 1209 es referencia y se conserva");
        requireRepair(fixed.content.contains(unsupportedReference),
                "No se debe reparar un jugador cuya referencia no sea 1209 o 1210");
        requireRepair(fixed.content.contains("future_key=keep\\:exactly"),
                "Las claves desconocidas deben conservarse exactamente");

        RepairResult second = repairContent(fixed.content);
        requireRepair(second.modifiedSnapshots == 0,
                "Una segunda pasada interna no debe encontrar cambios");
        requireRepair(second.content.equals(fixed.content),
                "Una segunda pasada interna debe ser idéntica");
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

    private static final class RepairSummary {
        int scannedFiles;
        int modifiedFiles;
        int modifiedPlayers;
        int modifiedSnapshots;
        int concurrentSkips;
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

    private static final class PlayerRepair {
        final int valueStart;
        final int valueEnd;
        final String rawValue;
        final List<AgeReplacement> replacements;

        PlayerRepair(int valueStart, int valueEnd, String rawValue, List<AgeReplacement> replacements) {
            this.valueStart = valueStart;
            this.valueEnd = valueEnd;
            this.rawValue = rawValue;
            this.replacements = replacements;
        }
    }

    private static final class AgeReplacement {
        final int ageIndex;
        final int age;

        AgeReplacement(int ageIndex, int age) {
            this.ageIndex = ageIndex;
            this.age = age;
        }
    }

    private static final class ParsedPlayer {
        final List<Snapshot> snapshots;

        ParsedPlayer(List<Snapshot> snapshots) {
            this.snapshots = snapshots;
        }
    }

    private static final class Snapshot {
        final int week;
        final int age;
        final int ageIndex;

        Snapshot(int week, int age, int ageIndex) {
            this.week = week;
            this.age = age;
            this.ageIndex = ageIndex;
        }
    }
%>

<%
    if (session.getAttribute("usuario") == null) {
        response.sendRedirect(request.getContextPath() + "/asistente");
        return;
    }
    if (session.getAttribute("admin") == null) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }

    final String executionMarker = "repairTrainingAges20260924Executed";
    boolean alreadyExecuted = Boolean.TRUE.equals(application.getAttribute(executionMarker));
    File dataDirectory = new File(SystemUtil.getVar(SystemUtil.PATH));
    String result = null;
    String error = null;
    String csrf = (String) session.getAttribute("repairTrainingAgesCsrf20260924");

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
        session.removeAttribute("repairTrainingAgesCsrf20260924");

        if (alreadyExecuted) {
            result = "Esta reparación ya se ejecutó en este arranque del servidor. No se ha modificado nada.";
        } else {
            try {
                RepairSummary summary = repairDirectory(dataDirectory);
                if (summary.concurrentSkips == 0) {
                    application.setAttribute("repairTrainingAges20260924Executed", Boolean.TRUE);
                    alreadyExecuted = true;
                    result = "Reparación terminada. Ficheros revisados: " + summary.scannedFiles
                            + ", ficheros modificados: " + summary.modifiedFiles
                            + ", jugadores corregidos: " + summary.modifiedPlayers
                            + ", edades corregidas: " + summary.modifiedSnapshots + ".";
                } else {
                    error = "La reparación no se marca como ejecutada porque " + summary.concurrentSkips
                            + " fichero(s) cambiaron mientras se procesaban. Los ficheros concurrentes se dejaron intactos."
                            + " Repite la ejecución cuando no haya actualizaciones en curso.";
                }
            } catch (Throwable e) {
                error = e.getClass().getName() + ": " + e.getMessage();
            }
        }
    }

    if (error == null && !alreadyExecuted && !"POST".equalsIgnoreCase(request.getMethod())) {
        csrf = UUID.randomUUID().toString();
        session.setAttribute("repairTrainingAgesCsrf20260924", csrf);
    } else if (error != null && !alreadyExecuted) {
        csrf = UUID.randomUUID().toString();
        session.setAttribute("repairTrainingAgesCsrf20260924", csrf);
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
<% } %>

<% if (alreadyExecuted) { %>
    <p>La reparación ya ha sido ejecutada. Elimina este JSP del despliegue.</p>
<% } else { %>
    <h2>Reparar edades de la temporada anterior</h2>
    <p>Herramienta puntual de administración. Debe ejecutarse manualmente una sola vez y retirarse después.</p>
    <p>Toma como edad actual la del último snapshot: jornada 1210 si el equipo ya se actualizó, o jornada 1209 si todavía no lo ha hecho.</p>
    <p>Solo corrige las jornadas anteriores de la temporada 1197-1209 para que tengan exactamente edad actual - 1. No toca otras temporadas ni otros campos.</p>
    <p>No utiliza backups como referencia ni crea copias auxiliares.</p>
    <form method="post">
        <input type="hidden" name="csrf" value="<%= escapeHtmlRepair(csrf) %>">
        <button type="submit">Ejecutar reparación una vez</button>
    </form>
<% } %>
</body>
</html>
