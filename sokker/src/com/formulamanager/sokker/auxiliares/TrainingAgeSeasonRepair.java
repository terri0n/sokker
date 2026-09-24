package com.formulamanager.sokker.auxiliares;

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
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reparación puntual para las edades históricas dañadas en el cambio de
 * temporada de septiembre de 2026.
 *
 * La edad del snapshot más reciente es la única referencia. Ese snapshot
 * puede ser la jornada 1 de la temporada nueva (1210) o la jornada 13 de la
 * anterior (1209) si el equipo todavía no se ha actualizado esta semana.
 * Solo los snapshots anteriores de la temporada 1197..1209 deben tener un
 * año menos que esa edad de referencia.
 */
public final class TrainingAgeSeasonRepair {
    public static final int PREVIOUS_SEASON_START = 1197;
    public static final int PREVIOUS_SEASON_END = 1209;
    public static final int PREVIOUS_WEEK_REFERENCE = 1209;
    public static final int CURRENT_WEEK_REFERENCE = 1210;

    private static final int MIN_CLUB_TID = 801;
    private static final int NEW_TRAINING_FORMAT_WEEK = 993;
    private static final Pattern TEAM_FILE = Pattern.compile("[0-9]+\\.properties");
    private static final Pattern PLAYER_LINE = Pattern.compile("(?m)^([0-9]+)([=:])(.*?)(\\r?)$");

    private TrainingAgeSeasonRepair() {
    }

    public static Integer expectedHistoricalAge(int latestWeek, int latestAge, int snapshotWeek) {
        if (latestWeek != PREVIOUS_WEEK_REFERENCE && latestWeek != CURRENT_WEEK_REFERENCE) {
            return null;
        }

        // El snapshot usado como referencia representa el estado actual y no se toca.
        if (snapshotWeek >= latestWeek) {
            return null;
        }

        if (snapshotWeek < PREVIOUS_SEASON_START || snapshotWeek > PREVIOUS_SEASON_END) {
            return null;
        }

        return Integer.valueOf(latestAge - 1);
    }

    public static RepairSummary repairDirectory(File directory) throws IOException {
        if (directory == null || !directory.isDirectory()) {
            throw new IOException("No existe el directorio de datos: "
                    + (directory == null ? "null" : directory.getAbsolutePath()));
        }

        File[] files = directory.listFiles(file -> file.isFile()
                && TEAM_FILE.matcher(file.getName()).matches()
                && clubTid(file.getName()) >= MIN_CLUB_TID);
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

    public static String repairContentForTest(String content) throws IOException {
        return repairContent(content).content;
    }

    private static FileRepairResult repairFile(Path path) throws IOException {
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

    private static RepairResult repairContent(String content) throws IOException {
        List<PlayerRepair> repairs = new ArrayList<PlayerRepair>();
        Matcher matcher = PLAYER_LINE.matcher(content);

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

    private static List<AgeReplacement> findAgeReplacements(ParsedPlayer parsed) {
        List<AgeReplacement> result = new ArrayList<AgeReplacement>();
        Snapshot latest = parsed.snapshots.get(0);
        int previousWeek = Integer.MAX_VALUE;

        for (Snapshot snapshot : parsed.snapshots) {
            if (snapshot.week >= previousWeek) {
                // Cadena dañada o en un formato inesperado: no tocamos este jugador.
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

    private static ParsedPlayer parsePlayer(String value) {
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

    private static int afterSnapshot(String[] tokens, int weekIndex, int week) {
        int index = weekIndex + 2;

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

        if (week >= NEW_TRAINING_FORMAT_WEEK) {
            if (index >= tokens.length) {
                return -1;
            }
            index++; // entrenamiento avanzado
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

    private static int clubTid(String fileName) {
        try {
            return Integer.parseInt(fileName.substring(0, fileName.length() - ".properties".length()));
        } catch (RuntimeException e) {
            return -1;
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

    public static final class RepairSummary {
        public int scannedFiles;
        public int modifiedFiles;
        public int modifiedPlayers;
        public int modifiedSnapshots;
        public int concurrentSkips;
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
}
