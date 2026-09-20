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
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.UUID" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="com.formulamanager.sokker.auxiliares.SystemUtil" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%!
    private static final int JORNADAS_TEMPORADA_REPAIR = 13;
    private static final int JORNADA_NUEVO_SISTEMA_LIGAS_REPAIR = 976;
    private static final int JORNADA_NUEVO_ENTRENO_REPAIR = 993;

    private static final Pattern TEAM_FILE_REPAIR = Pattern.compile("[0-9]+\\.properties");
    private static final Pattern PLAYER_LINE_REPAIR = Pattern.compile("(?m)^([0-9]+)([=:])(.*?)(\\r?)$");

    private int repairDirectory(File directory) throws IOException {
        if (!directory.isDirectory()) {
            throw new IOException("No existe el directorio de datos: " + directory.getAbsolutePath());
        }

        File[] files = directory.listFiles(file -> file.isFile()
                && TEAM_FILE_REPAIR.matcher(file.getName()).matches());
        if (files == null) {
            throw new IOException("No se puede listar el directorio de datos: " + directory.getAbsolutePath());
        }

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return a.getName().compareTo(b.getName());
            }
        });

        int modifiedRecords = 0;
        for (File file : files) {
            modifiedRecords += repairFile(file.toPath());
        }
        return modifiedRecords;
    }

    private int repairFile(Path path) throws IOException {
        byte[] originalBytes = Files.readAllBytes(path);
        String original = new String(originalBytes, StandardCharsets.ISO_8859_1);
        RepairResult repaired = repairContent(original);
        if (repaired.modifiedRecords == 0) {
            return 0;
        }

        RepairResult secondPass = repairContent(repaired.content);
        if (secondPass.modifiedRecords != 0 || !secondPass.content.equals(repaired.content)) {
            throw new IOException("La reparación no es idempotente para " + path.getFileName());
        }
        validateProperties(repaired.content, path);

        // No pisar una actualización concurrente que haya llegado después de leer.
        if (!Arrays.equals(originalBytes, Files.readAllBytes(path))) {
            return 0;
        }

        Path parent = path.toAbsolutePath().getParent();
        Path temp = Files.createTempFile(parent, path.getFileName().toString(), ".edad13.tmp");
        try {
            Files.copy(path, temp, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            Files.write(temp,
                    repaired.content.getBytes(StandardCharsets.ISO_8859_1),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            String written = new String(Files.readAllBytes(temp), StandardCharsets.ISO_8859_1);
            if (!written.equals(repaired.content)) {
                throw new IOException("Error verificando el fichero temporal de " + path.getFileName());
            }
            if (!Arrays.equals(originalBytes, Files.readAllBytes(path))) {
                return 0;
            }

            try {
                Files.move(temp, path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return repaired.modifiedRecords;
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
            return new RepairResult(content, 0);
        }

        StringBuilder fixed = new StringBuilder(content);
        int modified = 0;
        for (int i = repairs.size() - 1; i >= 0; i--) {
            PlayerRepair repair = repairs.get(i);
            String[] rawTokens = repair.rawValue.split(",", -1);

            for (Integer ageIndex : repair.ageIndices) {
                if (ageIndex < 0 || ageIndex >= rawTokens.length) {
                    throw new IOException("Formato de jugador inconsistente durante la reparación");
                }
                rawTokens[ageIndex] = Integer.toString(repair.targetAge);
                modified++;
            }

            fixed.replace(repair.valueStart, repair.valueEnd, join(rawTokens));
        }

        return new RepairResult(fixed.toString(), modified);
    }

    private List<Integer> findAgeIndicesToRepair(ParsedPlayer parsed) {
        if (parsed.snapshots.isEmpty()) {
            return Collections.emptyList();
        }

        Snapshot latest = parsed.snapshots.get(0);
        if (getJornadaModRepair(latest.jornada) != 12) {
            return Collections.emptyList();
        }

        int season = getSeasonRepair(latest.jornada);
        int currentAge = latest.edad;
        int previousJornada = Integer.MAX_VALUE;
        List<Integer> sameAgeBlock = new ArrayList<Integer>();

        for (Snapshot snapshot : parsed.snapshots) {
            if (snapshot.jornada >= previousJornada) {
                return Collections.emptyList();
            }
            previousJornada = snapshot.jornada;

            if (getSeasonRepair(snapshot.jornada) != season) {
                break;
            }

            if (snapshot.edad == currentAge) {
                sameAgeBlock.add(snapshot.edadIndex);
                continue;
            }

            // Dentro de una misma temporada la edad no cambia. Si tras un bloque
            // final con la edad actual aparece la edad actual - 1, ese bloque es
            // el que se contaminó al actualizar después del cumpleaños.
            if (snapshot.edad == currentAge - 1) {
                return sameAgeBlock;
            }
            return Collections.emptyList();
        }

        // Sin una jornada anterior de la misma temporada que demuestre el +1
        // no tocamos el jugador. Es preferible dejar un caso ambiguo pendiente
        // antes que restar dos veces a un registro ya correcto.
        return Collections.emptyList();
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

    private int getJornadaModRepair(int jornada) {
        return jornada < JORNADA_NUEVO_SISTEMA_LIGAS_REPAIR
                ? jornada % 16
                : (jornada - JORNADA_NUEVO_SISTEMA_LIGAS_REPAIR) % JORNADAS_TEMPORADA_REPAIR;
    }

    private int getSeasonRepair(int jornada) {
        return jornada < JORNADA_NUEVO_SISTEMA_LIGAS_REPAIR
                ? jornada / 16
                : (jornada - JORNADA_NUEVO_SISTEMA_LIGAS_REPAIR) / JORNADAS_TEMPORADA_REPAIR;
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
        String contaminated =
                "# cabecera\n" +
                "future_key=keep\\:exactly\n" +
                player(1, snapshot(1209, 29) + "," + snapshot(1208, 29) + "," + snapshot(1207, 28)) +
                player(2, snapshot(1209, 30) + "," + snapshot(1208, 30) + "," + snapshot(1207, 30) + "," + snapshot(1206, 29)) +
                player(3, snapshot(1209, 28) + "," + snapshot(1208, 28) + "," + snapshot(1207, 28)) +
                player(4, snapshot(1210, 29) + "," + snapshot(1209, 28));

        RepairResult fixed = repairContent(contaminated);
        requireRepair(fixed.modifiedRecords == 5, "Debe corregir bloques finales de dos o tres jornadas");
        requireRepair(fixed.content.contains(snapshot(1209, 28) + "," + snapshot(1208, 28) + "," + snapshot(1207, 28)),
                "Debe corregir las dos jornadas contaminadas");
        requireRepair(fixed.content.contains(snapshot(1209, 29) + "," + snapshot(1208, 29) + "," + snapshot(1207, 29) + "," + snapshot(1206, 29)),
                "Debe corregir las tres jornadas contaminadas");
        requireRepair(fixed.content.contains("future_key=keep\\:exactly"), "Debe conservar claves desconocidas");

        RepairResult second = repairContent(fixed.content);
        requireRepair(second.modifiedRecords == 0, "La reparación debe ser idempotente");
        requireRepair(second.content.equals(fixed.content), "Una segunda ejecución no debe modificar datos");
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
        final int modifiedRecords;

        RepairResult(String content, int modifiedRecords) {
            this.content = content;
            this.modifiedRecords = modifiedRecords;
        }
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

    String result = null;
    String error = null;
    String csrf = (String) session.getAttribute("repairAge13Csrf");

    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String supplied = request.getParameter("csrf");
        if (csrf == null || supplied == null || !csrf.equals(supplied)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        session.removeAttribute("repairAge13Csrf");

        try {
            selfTestRepair();
            File directory = new File(SystemUtil.getVar(SystemUtil.PATH));
            int modified = repairDirectory(directory);
            result = "Registros modificados: " + modified;
        } catch (Throwable e) {
            error = e.getClass().getName() + ": " + e.getMessage();
        }
    } else {
        csrf = UUID.randomUUID().toString();
        session.setAttribute("repairAge13Csrf", csrf);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Corregir edad jornada 13</title>
</head>
<body>
<% if (result != null) { %>
    <h2><%= escapeHtmlRepair(result) %></h2>
    <p>Ya puedes eliminar este JSP.</p>
<% } else if (error != null) { %>
    <h2>Error</h2>
    <pre><%= escapeHtmlRepair(error) %></pre>
<% } else { %>
    <h2>Corregir edad de la jornada 13</h2>
    <p>Corrige el bloque final de jornadas de la temporada que heredó la edad posterior al cumpleaños.</p>
    <form method="post">
        <input type="hidden" name="csrf" value="<%= escapeHtmlRepair(csrf) %>">
        <button type="submit">Corregir ahora</button>
    </form>
<% } %>
</body>
</html>
