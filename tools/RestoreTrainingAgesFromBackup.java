import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class RestoreTrainingAgesFromBackup {
    private static final int FIRST_AFFECTED_WEEK = 1197;
    private static final int LAST_AFFECTED_WEEK = 1208;
    private static final int MIN_CLUB_TID = 801;
    private static final int NEW_TRAINING_FORMAT_WEEK = 993;
    private static final String SAFETY_DIR = "age_restore_backup_20260924";

    private static final Pattern CURRENT_TEAM_FILE = Pattern.compile("[0-9]+\\.properties");
    private static final Pattern REFERENCE_TEAM_FILE = Pattern.compile("[0-9]+(?:_historico)?\\.properties");
    private static final Pattern PLAYER_LINE = Pattern.compile("(?m)^([0-9]+)([=:])(.*?)(\\r?)$");

    private RestoreTrainingAgesFromBackup() {}

    public static final class Summary {
        public int referenceSnapshots;
        public int filesScanned;
        public int candidateFiles;
        public int candidatePlayers;
        public int candidateSnapshots;
        public int modifiedFiles;
        public int modifiedPlayers;
        public int modifiedSnapshots;
        public int missingReferenceSnapshots;
        public int ambiguousSnapshots;

        @Override
        public String toString() {
            return "referenceSnapshots=" + referenceSnapshots
                    + ", filesScanned=" + filesScanned
                    + ", candidateFiles=" + candidateFiles
                    + ", candidatePlayers=" + candidatePlayers
                    + ", candidateSnapshots=" + candidateSnapshots
                    + ", modifiedFiles=" + modifiedFiles
                    + ", modifiedPlayers=" + modifiedPlayers
                    + ", modifiedSnapshots=" + modifiedSnapshots
                    + ", missingReferenceSnapshots=" + missingReferenceSnapshots
                    + ", ambiguousSnapshots=" + ambiguousSnapshots;
        }
    }

    private static final class SnapshotKey {
        final int pid;
        final int week;

        SnapshotKey(int pid, int week) {
            this.pid = pid;
            this.week = week;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof SnapshotKey)) {
                return false;
            }
            SnapshotKey key = (SnapshotKey) other;
            return pid == key.pid && week == key.week;
        }

        @Override
        public int hashCode() {
            return 31 * pid + week;
        }

        @Override
        public String toString() {
            return pid + "/" + week;
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

    private static final class ParsedPlayer {
        final List<Snapshot> snapshots;

        ParsedPlayer(List<Snapshot> snapshots) {
            this.snapshots = snapshots;
        }
    }

    private static final class PlayerChange {
        final int valueStart;
        final int valueEnd;
        final String rawValue;
        final Map<Integer, Integer> replacementAges;

        PlayerChange(int valueStart, int valueEnd, String rawValue, Map<Integer, Integer> replacementAges) {
            this.valueStart = valueStart;
            this.valueEnd = valueEnd;
            this.rawValue = rawValue;
            this.replacementAges = replacementAges;
        }
    }

    private static final class FilePlan {
        final Path path;
        final byte[] originalBytes;
        final byte[] repairedBytes;
        final int players;
        final int snapshots;

        FilePlan(Path path, byte[] originalBytes, byte[] repairedBytes, int players, int snapshots) {
            this.path = path;
            this.originalBytes = originalBytes;
            this.repairedBytes = repairedBytes;
            this.players = players;
            this.snapshots = snapshots;
        }
    }

    public static Summary restore(Path currentDirectory, List<Path> backupZips, boolean apply) throws IOException {
        if (currentDirectory == null || !Files.isDirectory(currentDirectory)) {
            throw new IOException("Current data directory does not exist: " + currentDirectory);
        }
        if (backupZips == null || backupZips.isEmpty()) {
            throw new IOException("At least one reference backup ZIP is required");
        }

        Map<SnapshotKey, Integer> reference = loadReferenceAges(backupZips);
        Summary summary = new Summary();
        summary.referenceSnapshots = reference.size();

        List<FilePlan> plans = planCurrentDirectory(currentDirectory, reference, summary);
        if (summary.ambiguousSnapshots > 0) {
            throw new IOException("Restore aborted: found " + summary.ambiguousSnapshots
                    + " ambiguous historical age difference(s). No files were written.");
        }

        if (!apply) {
            return summary;
        }

        Path safetyDirectory = currentDirectory.resolve(SAFETY_DIR);
        for (FilePlan plan : plans) {
            if (!Files.exists(safetyDirectory)) {
                Files.createDirectories(safetyDirectory);
            }

            Path safetyCopy = safetyDirectory.resolve(plan.path.getFileName().toString());
            if (!Files.exists(safetyCopy)) {
                Files.write(safetyCopy, plan.originalBytes);
            } else {
                byte[] existing = Files.readAllBytes(safetyCopy);
                if (!java.util.Arrays.equals(existing, plan.originalBytes)) {
                    throw new IOException("Safety copy already exists with different content: " + safetyCopy);
                }
            }

            if (!java.util.Arrays.equals(plan.originalBytes, Files.readAllBytes(plan.path))) {
                throw new IOException("Current file changed while planning restore: " + plan.path.getFileName());
            }

            Path temp = Files.createTempFile(currentDirectory, plan.path.getFileName().toString(), ".age-restore.tmp");
            try {
                Files.write(temp, plan.repairedBytes);
                if (!java.util.Arrays.equals(plan.repairedBytes, Files.readAllBytes(temp))) {
                    throw new IOException("Failed to verify temporary restored file: " + plan.path.getFileName());
                }
                if (!java.util.Arrays.equals(plan.originalBytes, Files.readAllBytes(plan.path))) {
                    throw new IOException("Current file changed before atomic replace: " + plan.path.getFileName());
                }
                try {
                    Files.move(temp, plan.path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temp, plan.path, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temp);
            }

            summary.modifiedFiles++;
            summary.modifiedPlayers += plan.players;
            summary.modifiedSnapshots += plan.snapshots;
        }

        return summary;
    }

    private static Map<SnapshotKey, Integer> loadReferenceAges(List<Path> backupZips) throws IOException {
        Map<SnapshotKey, Integer> reference = new HashMap<SnapshotKey, Integer>();
        for (Path backup : backupZips) {
            if (backup == null || !Files.isRegularFile(backup)) {
                throw new IOException("Reference backup ZIP does not exist: " + backup);
            }
            try (ZipFile zip = new ZipFile(backup.toFile())) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String baseName = baseName(entry.getName());
                    if (!REFERENCE_TEAM_FILE.matcher(baseName).matches()) {
                        continue;
                    }
                    byte[] bytes = readAll(zip, entry);
                    addReferenceContent(reference, new String(bytes, StandardCharsets.ISO_8859_1), backup + "!" + entry.getName());
                }
            }
        }
        return reference;
    }

    private static byte[] readAll(ZipFile zip, ZipEntry entry) throws IOException {
        try (java.io.InputStream in = zip.getInputStream(entry)) {
            byte[] buffer = new byte[8192];
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    private static void addReferenceContent(Map<SnapshotKey, Integer> reference, String content, String source) throws IOException {
        Matcher matcher = PLAYER_LINE.matcher(content);
        while (matcher.find()) {
            int pid = Integer.parseInt(matcher.group(1));
            String rawValue = matcher.group(3);
            if (hasContinuation(rawValue)) {
                continue;
            }
            ParsedPlayer player = parsePlayer(unescapePropertyValue(rawValue));
            if (player == null) {
                continue;
            }
            for (Snapshot snapshot : player.snapshots) {
                if (!isAffectedWeek(snapshot.week)) {
                    continue;
                }
                SnapshotKey key = new SnapshotKey(pid, snapshot.week);
                Integer previous = reference.put(key, Integer.valueOf(snapshot.age));
                if (previous != null && previous.intValue() != snapshot.age) {
                    throw new IOException("Conflicting reference age for " + key + ": "
                            + previous + " vs " + snapshot.age + " in " + source);
                }
            }
        }
    }

    private static List<FilePlan> planCurrentDirectory(Path currentDirectory,
            Map<SnapshotKey, Integer> reference, Summary summary) throws IOException {
        List<Path> files = new ArrayList<Path>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDirectory)) {
            for (Path path : stream) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }
                String name = path.getFileName().toString();
                if (!CURRENT_TEAM_FILE.matcher(name).matches()) {
                    continue;
                }
                int tid = parseTid(name);
                if (tid < MIN_CLUB_TID) {
                    continue;
                }
                files.add(path);
            }
        }
        Collections.sort(files, new Comparator<Path>() {
            @Override
            public int compare(Path a, Path b) {
                return a.getFileName().toString().compareTo(b.getFileName().toString());
            }
        });

        List<FilePlan> plans = new ArrayList<FilePlan>();
        for (Path path : files) {
            summary.filesScanned++;
            FilePlan plan = planFile(path, reference, summary);
            if (plan != null) {
                plans.add(plan);
                summary.candidateFiles++;
                summary.candidatePlayers += plan.players;
                summary.candidateSnapshots += plan.snapshots;
            }
        }
        return plans;
    }

    private static FilePlan planFile(Path path, Map<SnapshotKey, Integer> reference, Summary summary) throws IOException {
        byte[] originalBytes = Files.readAllBytes(path);
        String content = new String(originalBytes, StandardCharsets.ISO_8859_1);
        Matcher matcher = PLAYER_LINE.matcher(content);
        List<PlayerChange> changes = new ArrayList<PlayerChange>();

        while (matcher.find()) {
            int pid = Integer.parseInt(matcher.group(1));
            String rawValue = matcher.group(3);
            if (hasContinuation(rawValue)) {
                continue;
            }
            ParsedPlayer player = parsePlayer(unescapePropertyValue(rawValue));
            if (player == null) {
                continue;
            }

            Map<Integer, Integer> replacementAges = new HashMap<Integer, Integer>();
            for (Snapshot snapshot : player.snapshots) {
                if (!isAffectedWeek(snapshot.week)) {
                    continue;
                }
                Integer expected = reference.get(new SnapshotKey(pid, snapshot.week));
                if (expected == null) {
                    summary.missingReferenceSnapshots++;
                    continue;
                }
                if (snapshot.age == expected.intValue()) {
                    continue;
                }
                if (snapshot.age + 1 == expected.intValue()) {
                    replacementAges.put(Integer.valueOf(snapshot.ageIndex), expected);
                } else {
                    summary.ambiguousSnapshots++;
                }
            }

            if (!replacementAges.isEmpty()) {
                changes.add(new PlayerChange(matcher.start(3), matcher.end(3), rawValue, replacementAges));
            }
        }

        if (changes.isEmpty()) {
            return null;
        }

        StringBuilder repaired = new StringBuilder(content);
        int changedSnapshots = 0;
        for (int i = changes.size() - 1; i >= 0; i--) {
            PlayerChange change = changes.get(i);
            String[] rawTokens = change.rawValue.split(",", -1);
            for (Map.Entry<Integer, Integer> replacement : change.replacementAges.entrySet()) {
                int index = replacement.getKey().intValue();
                if (index < 0 || index >= rawTokens.length) {
                    throw new IOException("Player format changed while restoring " + path.getFileName());
                }
                rawTokens[index] = Integer.toString(replacement.getValue().intValue());
                changedSnapshots++;
            }
            repaired.replace(change.valueStart, change.valueEnd, join(rawTokens));
        }

        byte[] repairedBytes = repaired.toString().getBytes(StandardCharsets.ISO_8859_1);
        if (java.util.Arrays.equals(originalBytes, repairedBytes)) {
            throw new IOException("Internal restore error: planned changes produced identical file " + path.getFileName());
        }
        return new FilePlan(path, originalBytes, repairedBytes, changes.size(), changedSnapshots);
    }

    private static ParsedPlayer parsePlayer(String value) {
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

    private static int afterSnapshot(String[] tokens, int weekIndex, int week) {
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
        if (week >= NEW_TRAINING_FORMAT_WEEK) {
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

    private static boolean isAffectedWeek(int week) {
        return week >= FIRST_AFFECTED_WEEK && week <= LAST_AFFECTED_WEEK;
    }

    private static int parseTid(String name) {
        try {
            return Integer.parseInt(name.substring(0, name.length() - ".properties".length()));
        } catch (RuntimeException e) {
            return -1;
        }
    }

    private static String baseName(String name) {
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        return slash < 0 ? name : name.substring(slash + 1);
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

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java RestoreTrainingAgesFromBackup <current-data-dir> <backup.zip> [backup2.zip ...] [--apply]");
            System.err.println("Without --apply the command is a dry run and never writes current data.");
            System.exit(2);
        }

        boolean apply = "--apply".equals(args[args.length - 1]);
        int backupEnd = apply ? args.length - 1 : args.length;
        if (backupEnd < 2) {
            throw new IllegalArgumentException("At least one reference backup ZIP is required");
        }

        Path current = java.nio.file.Paths.get(args[0]);
        List<Path> backups = new ArrayList<Path>();
        for (int i = 1; i < backupEnd; i++) {
            backups.add(java.nio.file.Paths.get(args[i]));
        }

        Summary summary = restore(current, backups, apply);
        System.out.println((apply ? "APPLY " : "DRY-RUN ") + summary);
        if (!apply && summary.candidateSnapshots > 0) {
            System.out.println("No files were written. Re-run with --apply only after reviewing this summary.");
        }
    }
}
