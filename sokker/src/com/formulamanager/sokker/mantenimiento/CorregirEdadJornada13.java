package com.formulamanager.sokker.mantenimiento;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.bo.AsistenteBO;

/**
 * Reparación puntual de las edades guardadas con +1 en la última semana de
 * temporada. Se puede eliminar después de ejecutarla una vez en producción.
 *
 * Solo considera bases principales <tid>.properties modificadas hoy. Un
 * fichero se considera afectado únicamente si al menos un jugador demuestra
 * el patrón contaminado (edad de la última jornada = edad anterior + 1). Una
 * vez identificado el fichero, corrige todos sus jugadores cuya última jornada
 * sea la 13, incluidos jugadores nuevos sin histórico propio.
 *
 * El fichero se modifica de forma textual: no se reserializan Jugador ni las
 * Properties, de modo que claves desconocidas y campos futuros quedan intactos.
 */
public final class CorregirEdadJornada13 {
    private static final Pattern TEAM_FILE = Pattern.compile("[0-9]+\\.properties");
    private static final Pattern PLAYER_LINE = Pattern.compile("(?m)^([0-9]+)([=:])(.*?)(\\r?)$");

    private CorregirEdadJornada13() {}

    public static void main(String[] args) throws Exception {
        File directory = resolveDataDirectory(args);
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
                && TEAM_FILE.matcher(file.getName()).matches()
                && file.lastModified() >= startToday
                && file.lastModified() < startTomorrow);

        if (files == null) {
            throw new IOException("No se puede listar el directorio de datos: " + directory.getAbsolutePath());
        }

        Arrays.sort(files, Comparator.comparing(File::getName));
        int modifiedRecords = 0;
        for (File file : files) {
            modifiedRecords += repairFile(file.toPath());
        }

        System.out.println("Registros modificados: " + modifiedRecords);
    }

    private static File resolveDataDirectory(String[] args) throws IOException {
        if (args != null && args.length > 1) {
            throw new IOException("Uso: CorregirEdadJornada13 [directorio_datos]");
        }
        if (args != null && args.length == 1 && args[0] != null && !args[0].trim().isEmpty()) {
            return new File(args[0]);
        }

        if (SystemUtil.REAL_PATH == null) {
            try {
                File classes = new File(CorregirEdadJornada13.class
                        .getProtectionDomain().getCodeSource().getLocation().toURI());
                File webInf = classes.getParentFile();
                if (classes.isDirectory()
                        && "classes".equals(classes.getName())
                        && webInf != null
                        && "WEB-INF".equals(webInf.getName())
                        && webInf.getParentFile() != null) {
                    SystemUtil.REAL_PATH = webInf.getParentFile().getAbsolutePath() + File.separator;
                }
            } catch (Exception e) {
                throw new IOException("No se puede localizar automáticamente el despliegue de Tomcat", e);
            }
        }

        if (SystemUtil.REAL_PATH == null) {
            throw new IOException("Indica el directorio de datos como único argumento");
        }
        return new File(SystemUtil.getVar(SystemUtil.PATH));
    }

    private static int repairFile(Path path) throws IOException {
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

            // Comprobación final antes del reemplazo atómico.
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

    static RepairResult repairContent(String content) throws IOException {
        List<PlayerLine> players = new ArrayList<PlayerLine>();
        Matcher matcher = PLAYER_LINE.matcher(content);
        boolean contaminated = false;

        while (matcher.find()) {
            String rawValue = matcher.group(3);
            if (hasContinuation(rawValue)) {
                continue;
            }

            ParsedPlayer parsed = parsePlayer(unescapePropertyValue(rawValue));
            if (parsed == null || AsistenteBO.getJornadaMod(parsed.jornada) != 12) {
                continue;
            }

            PlayerLine line = new PlayerLine(matcher.start(3), matcher.end(3), rawValue, parsed);
            players.add(line);

            if (parsed.previousJornada != null
                    && parsed.previousEdad != null
                    && parsed.jornada > parsed.previousJornada
                    && parsed.jornada - parsed.previousJornada < AsistenteBO.JORNADAS_TEMPORADA
                    && parsed.edad == parsed.previousEdad + 1) {
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
            String[] rawTokens = player.rawValue.split(",", -1);
            if (player.parsed.edadIndex >= rawTokens.length) {
                throw new IOException("Formato de jugador inconsistente durante la reparación");
            }

            rawTokens[player.parsed.edadIndex] = Integer.toString(player.parsed.edad - 1);
            String newValue = join(rawTokens);
            fixed.replace(player.valueStart, player.valueEnd, newValue);
            modified++;
        }

        return new RepairResult(fixed.toString(), modified);
    }

    private static ParsedPlayer parsePlayer(String value) {
        try {
            String[] tokens = value.split(",", -1);
            int index = 11; // nombre, tid, demarcación, país, fecha, actualizado, tarjetas, nt, lesión, venta, notas
            if (index >= tokens.length) {
                return null;
            }

            // Bloque opcional introducido por salario negativo.
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

    private static int afterSnapshot(String[] tokens, int jornadaIndex, int jornada) {
        int index = jornadaIndex + 2;

        // valor + 8 habilidades
        if (index + 9 > tokens.length) {
            return -1;
        }
        index += 9;

        // Lesión, cuando existe, se guarda con signo negativo.
        if (startsWithMinus(tokens, index)) {
            index++;
        }

        // forma, demarcación de entrenamiento, minutos
        if (index + 3 > tokens.length) {
            return -1;
        }
        index += 3;

        // experiencia, disciplina táctica y trabajo en equipo.
        if (startsWithMinus(tokens, index)) {
            if (index + 3 > tokens.length) {
                return -1;
            }
            index += 3;
        }

        // Desde el nuevo sistema de entrenamiento se guarda el flag avanzado.
        if (jornada >= AsistenteBO.JORNADA_NUEVO_ENTRENO) {
            if (index >= tokens.length) {
                return -1;
            }
            index++;
        }

        return index;
    }

    private static boolean startsWithMinus(String[] tokens, int index) {
        return index < tokens.length && tokens[index] != null && tokens[index].startsWith("-");
    }

    private static boolean hasContinuation(String rawValue) {
        int backslashes = 0;
        for (int i = rawValue.length() - 1; i >= 0 && rawValue.charAt(i) == '\\'; i--) {
            backslashes++;
        }
        return (backslashes & 1) != 0;
    }

    private static String unescapePropertyValue(String rawValue) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader("value=" + rawValue + "\n"));
        return properties.getProperty("value");
    }

    private static void validateProperties(String content, Path path) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        if (properties.isEmpty() && content.length() > 0) {
            throw new IOException("El fichero reparado no contiene propiedades: " + path.getFileName());
        }
    }

    private static String join(String[] tokens) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                result.append(',');
            }
            result.append(tokens[i]);
        }
        return result.toString();
    }

    static final class RepairResult {
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
}
