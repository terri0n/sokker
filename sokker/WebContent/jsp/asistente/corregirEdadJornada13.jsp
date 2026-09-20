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
<%@ page import="java.util.Calendar" %>
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

        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        long startToday = start.getTimeInMillis();
        start.add(Calendar.DAY_OF_MONTH, 1);
        long startTomorrow = start.getTimeInMillis();

        File[] files = directory.listFiles(file -> file.isFile()
                && TEAM_FILE_REPAIR.matcher(file.getName()).matches()
                && file.lastModified() >= startToday
                && file.lastModified() < startTomorrow);
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
        List<PlayerLine> players = new ArrayList<PlayerLine>();
        Matcher matcher = PLAYER_LINE_REPAIR.matcher(content);
        boolean contaminated = false;

        while (matcher.find()) {
            String rawValue = matcher.group(3);
            if (hasContinuation(rawValue)) {
                continue;
            }

            ParsedPlayer parsed = parsePlayer(unescapePropertyValue(rawValue));
            if (parsed == null || getJornadaModRepair(parsed.jornada) != 12) {
                continue;
            }

            PlayerLine line = new PlayerLine(matcher.start(3), matcher.end(3), rawValue, parsed);
            players.add(line);
            if (isReliablePrevious(parsed) && parsed.edad == parsed.previousEdad.intValue() + 1) {
                contaminated = true;
            }
        }

        if (!contaminated || players.isEmpty()) {
            return new RepairResult(content, 0);
        }

        StringBuilder fixed = new StringBuilder(content);
        int modified = 0;
        for (int i = players.size() - 1; i >= 0; i--) {
            PlayerLine player = players.get(i);

            // Si hay una jornada anterior fiable y la edad ya coincide, este
            // jugador pudo haberse corregido después: no lo tocamos otra vez.
            if (isReliablePrevious(player.parsed)
                    && player.parsed.edad != player.parsed.previousEdad.intValue() + 1) {
                continue;
            }

            String[] rawTokens = player.rawValue.split(",", -1);
            if (player.parsed.edadIndex >= rawTokens.length) {
                throw new IOException("Formato de jugador inconsistente durante la reparación");
            }

            rawTokens[player.parsed.edadIndex] = Integer.toString(player.parsed.edad - 1);
            fixed.replace(player.valueStart, player.valueEnd, join(rawTokens));
            modified++;
        }

        return new RepairResult(fixed.toString(), modified);
    }

    private boolean isReliablePrevious(ParsedPlayer parsed) {
        return parsed.previousJornada != null
                && parsed.previousEdad != null
                && parsed.jornada > parsed.previousJornada.intValue()
                && parsed.jornada - parsed.previousJornada.intValue() < JORNADAS_TEMPORADA_REPAIR;
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

            if (index + 1 >= tokens.length) {
                return null;
            }

            int jornadaIndex = index;
            int edadIndex = index + 1;
            int jornada = Integer.parseInt(tokens[jornadaIndex]);
            int edad = Integer.parseInt(tokens[edadIndex]);

            int next = afterSnapshot(tokens, jornadaIndex, jornada);
            Integer previousJornada = null;
            Integer previousEdad = null;
            if (next >= 0 && next + 1 < tokens.length && !"*".equals(tokens[next])) {
                previousJornada = Integer.valueOf(tokens[next]);
                previousEdad = Integer.valueOf(tokens[next + 1]);
            }

            return new ParsedPlayer(jornada, edad, edadIndex, previousJornada, previousEdad);
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

    private void selfTestRepair() throws Exception {
        String contaminated =
                "# cabecera\n" +
                "future_key=keep\\:exactly\n" +
                "1=Jugador Uno,1000,DEF,1,20/09/2026 10\\:00,true,0,0,0,,,-1000,180,750,2400,-,false,\\#,-,-false,1001,21,100000,5,5,5,5,5,5,5,5,-0,10,DEF,100.0,-1,1,1,true,1000,20,90000,5,5,5,5,5,5,5,5,-0,10,DEF,100.0,-1,1,1,true,*\n" +
                "2=Jugador Nuevo,1000,MID,1,20/09/2026 10\\:00,true,0,0,0,,,-1000,180,750,2400,-,false,\\#,-,-false,1001,30,100000,5,5,5,5,5,5,5,5,-0,10,MID,100.0,-1,1,1,true,*\n" +
                "3=Jugador Ya Corregido,1000,DEF,1,20/09/2026 10\\:00,true,0,0,0,,,-1000,180,750,2400,-,false,\\#,-,-false,1001,25,100000,5,5,5,5,5,5,5,5,-0,10,DEF,100.0,-1,1,1,true,1000,25,90000,5,5,5,5,5,5,5,5,-0,10,DEF,100.0,-1,1,1,true,*\n";

        RepairResult fixed = repairContent(contaminated);
        requireRepair(fixed.modifiedRecords == 2, "Debe corregir solo los registros contaminados o sin histórico fiable");
        requireRepair(fixed.content.contains(",1001,20,100000,"), "Debe restar uno al jugador contaminado con histórico");
        requireRepair(fixed.content.contains(",1001,29,100000,"), "Debe restar uno al jugador nuevo del equipo contaminado");
        requireRepair(fixed.content.contains(",1001,25,100000,"), "No debe tocar un jugador que ya esté corregido");
        requireRepair(fixed.content.contains("future_key=keep\\:exactly"), "Debe conservar claves desconocidas");

        RepairResult second = repairContent(fixed.content);
        requireRepair(second.modifiedRecords == 0, "La reparación debe ser idempotente");
        requireRepair(second.content.equals(fixed.content), "Una segunda ejecución no debe modificar datos");

        String ordinary = contaminated
                .replace(",1001,21,", ",1002,21,")
                .replace(",1001,30,", ",1002,30,")
                .replace(",1001,25,", ",1002,25,");
        RepairResult untouched = repairContent(ordinary);
        requireRepair(untouched.modifiedRecords == 0, "No debe tocar jornadas que no sean la 13");
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

    private static final class PlayerLine {
        final int valueStart;
        final int valueEnd;
        final String rawValue;
        final ParsedPlayer parsed;

        PlayerLine(int valueStart, int valueEnd, String rawValue, ParsedPlayer parsed) {
            this.valueStart = valueStart;
            this.valueEnd = valueEnd;
            this.rawValue = rawValue;
            this.parsed = parsed;
        }
    }

    private static final class ParsedPlayer {
        final int jornada;
        final int edad;
        final int edadIndex;
        final Integer previousJornada;
        final Integer previousEdad;

        ParsedPlayer(int jornada, int edad, int edadIndex, Integer previousJornada, Integer previousEdad) {
            this.jornada = jornada;
            this.edad = edad;
            this.edadIndex = edadIndex;
            this.previousJornada = previousJornada;
            this.previousEdad = previousEdad;
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
    <p>Corrige únicamente los equipos actualizados hoy en los que se detecte el +1 erróneo.</p>
    <form method="post">
        <input type="hidden" name="csrf" value="<%= escapeHtmlRepair(csrf) %>">
        <button type="submit">Corregir ahora</button>
    </form>
<% } %>
</body>
</html>
