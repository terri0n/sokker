package com.formulamanager.sokker.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Consistent SQLite online backup and bounded-size email attachments.
 * Snapshot files are created in the system temp directory; only numbered
 * backup parts are placed beside the database.
 */
public final class SqliteBackupService {
    public static final long MAX_EMAIL_PART_BYTES = 9L * 1024L * 1024L;
    private static final int BUFFER_SIZE = 64 * 1024;

    private SqliteBackupService() {}

    public static List<File> createParts(File databaseFile, File destinationDirectory,
            String databasePrefix) throws IOException, SQLException {
        if (databaseFile == null || !databaseFile.isFile()) {
            throw new IOException("SQLite database does not exist: " + databaseFile);
        }
        if (destinationDirectory == null || !destinationDirectory.isDirectory()) {
            throw new IOException("Backup destination directory does not exist: " + destinationDirectory);
        }
        if (databasePrefix == null || !databasePrefix.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("Invalid backup prefix");
        }

        File snapshot = Files.createTempFile("sokker-sqlite-", ".db").toFile();
        List<File> created = new ArrayList<File>();
        boolean completed = false;
        String generation = databasePrefix + "-" + timestamp();
        try {
            Files.delete(snapshot.toPath());
            createSnapshot(databaseFile, snapshot);
            verifyIntegrity(snapshot);
            long snapshotLength = snapshot.length();
            int totalParts = (int)((snapshotLength + MAX_EMAIL_PART_BYTES - 1L) / MAX_EMAIL_PART_BYTES);
            if (totalParts < 1 || totalParts > 9999) {
                throw new IOException("SQLite backup requires an unsupported number of email fragments: " + totalParts);
            }

            byte[] buffer = new byte[BUFFER_SIZE];
            try (InputStream input = new FileInputStream(snapshot)) {
                int partNumber = 1;
                while (true) {
                    File staging = Files.createTempFile("sokker-part-", ".tmp").toFile();
                    long written = 0L;
                    try {
                        try (OutputStream output = new FileOutputStream(staging)) {
                            while (written < MAX_EMAIL_PART_BYTES) {
                                int requested = (int)Math.min(buffer.length, MAX_EMAIL_PART_BYTES - written);
                                int count = input.read(buffer, 0, requested);
                                if (count < 0) break;
                                output.write(buffer, 0, count);
                                written += count;
                            }
                        }
                        if (written == 0L) break;

                        File part = new File(destinationDirectory,
                                generation + ".part" + String.format(Locale.ROOT, "%04dof%04d", partNumber++, totalParts));
                        moveAtomically(staging, part);
                        if (part.length() > MAX_EMAIL_PART_BYTES) {
                            Files.deleteIfExists(part.toPath());
                            throw new IOException("Backup part exceeds email size limit: " + part);
                        }
                        created.add(part);
                        if (written < MAX_EMAIL_PART_BYTES) break;
                    } finally {
                        Files.deleteIfExists(staging.toPath());
                    }
                }
            }
            if (created.isEmpty()) throw new IOException("SQLite backup produced no data");
            completed = true;
            return Collections.unmodifiableList(created);
        } finally {
            Files.deleteIfExists(snapshot.toPath());
            if (!completed) {
                for (File part : created) Files.deleteIfExists(part.toPath());
            }
        }
    }

    public static List<File> listParts(File directory, String databasePrefix) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) throw new IOException("Cannot list backup directory: " + directory);
        List<File> parts = new ArrayList<File>();
        String prefix = databasePrefix + "-";
        for (File file : files) {
            if (file.isFile() && file.getName().startsWith(prefix)
                    && file.getName().matches(java.util.regex.Pattern.quote(prefix)
                            + "[0-9]{8}T[0-9]{6}Z\\.part[0-9]{4}of[0-9]{4}")) {
                if (file.length() > MAX_EMAIL_PART_BYTES) {
                    throw new IOException("Existing backup part exceeds 10 MB: " + file);
                }
                parts.add(file);
            }
        }
        Collections.sort(parts);
        return parts;
    }

    public static void restoreParts(List<File> parts, File destination) throws IOException {
        if (parts == null || parts.isEmpty() || destination == null) {
            throw new IllegalArgumentException("Backup parts and destination are required");
        }
        List<File> ordered = new ArrayList<File>(parts);
        Collections.sort(ordered);
        String generation = null;
        int expectedPart = 1;
        java.util.regex.Pattern partPattern = java.util.regex.Pattern.compile("^([a-zA-Z0-9_-]+-[0-9]{8}T[0-9]{6}Z)\\.part([0-9]{4})of([0-9]{4})$");
        int totalParts = -1;
        for (File part : ordered) {
            java.util.regex.Matcher matcher = partPattern.matcher(part.getName());
            if (!matcher.matches()) throw new IOException("Invalid SQLite backup part name: " + part);
            if (generation == null) generation = matcher.group(1);
            if (!generation.equals(matcher.group(1))) throw new IOException("Cannot mix SQLite backup generations");
            int partNumber = Integer.parseInt(matcher.group(2));
            int declaredTotal = Integer.parseInt(matcher.group(3));
            if (totalParts < 0) totalParts = declaredTotal;
            if (declaredTotal != totalParts || totalParts < 1)
                throw new IOException("Inconsistent SQLite backup fragment count: " + part);
            if (partNumber != expectedPart++) throw new IOException("Missing or duplicated SQLite backup part: " + part);
            if (!part.isFile() || part.length() > MAX_EMAIL_PART_BYTES)
                throw new IOException("Invalid SQLite backup part size: " + part);
        }
        if (ordered.size() != totalParts) {
            throw new IOException("Incomplete SQLite backup: expected " + totalParts + " fragments, received " + ordered.size());
        }
        File temporary = File.createTempFile("sokker-restore-", ".db", destination.getAbsoluteFile().getParentFile());
        boolean completed = false;
        try (OutputStream output = new FileOutputStream(temporary)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            for (File part : ordered) {
                try (InputStream input = new FileInputStream(part)) {
                    int count;
                    while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                }
            }
            output.flush();
            verifyIntegrity(temporary);
            moveAtomically(temporary, destination);
            completed = true;
        } catch (SQLException e) {
            throw new IOException("Restored SQLite database failed integrity check", e);
        } finally {
            if (!completed) Files.deleteIfExists(temporary.toPath());
        }
    }

    private static void createSnapshot(File databaseFile, File snapshot) throws SQLException {
        String target = snapshot.getAbsolutePath().replace("'", "''");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout=30000");
            statement.execute("VACUUM INTO '" + target + "'");
        }
    }

    private static void verifyIntegrity(File databaseFile) throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA integrity_check")) {
            if (!result.next() || !"ok".equalsIgnoreCase(result.getString(1))) {
                throw new SQLException("SQLite integrity_check failed for " + databaseFile);
            }
        }
    }

    private static void moveAtomically(File source, File destination) throws IOException {
        try {
            Files.move(source.toPath(), destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String timestamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ROOT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }
}
