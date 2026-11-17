package com.formulamanager.sokker.persistence;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Imports a read-only snapshot of every .properties file below the configured
 * data directory. Each property is stored once as a key/value row. Unknown
 * keys are retained, and source files are never modified or deleted.
 */
public final class LegacyPropertiesImporter {
    private final SqliteDatabase database;
    private final File sourceDirectory;

    public LegacyPropertiesImporter(SqliteDatabase database, File sourceDirectory) {
        if (database == null || sourceDirectory == null) {
            throw new IllegalArgumentException("database and sourceDirectory are required");
        }
        this.database = database;
        this.sourceDirectory = sourceDirectory.getAbsoluteFile();
    }

    public ImportReport importAll() throws IOException, SQLException {
        if (!sourceDirectory.isDirectory()) {
            throw new IOException("Legacy data directory does not exist: " + sourceDirectory);
        }
        database.initialize();
        List<File> files = new ArrayList<File>();
        collect(sourceDirectory, files);
        Collections.sort(files);

        int imported = 0;
        int unchanged = 0;
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                for (File file : files) {
                    String relativePath = sourceDirectory.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    String checksum = sha256(bytes);
                    if (isImported(connection, relativePath)) {
                        unchanged++;
                        continue;
                    }
                    Properties properties = new Properties();
                    properties.load(new ByteArrayInputStream(bytes));
                    storeFile(connection, relativePath, checksum, file.lastModified());
                    storeEntries(connection, relativePath, properties);
                    imported++;
                }
                if (database.isPlayerGroupsEnabled()) {
                    migratePlayerGroupData(connection);
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new SQLException("Legacy properties import failed", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
        return new ImportReport(files.size(), imported, unchanged);
    }

    private void migratePlayerGroupData(Connection connection) throws SQLException {
        try (PreparedStatement marker = connection.prepareStatement(
                "SELECT 1 FROM migration_marker WHERE name=?")) {
            marker.setString(1, "player_groups_legacy_imported");
            try (java.sql.ResultSet result = marker.executeQuery()) {
                if (result.next()) return;
            }
        }

        try (PreparedStatement userInsert = connection.prepareStatement(
                "INSERT OR IGNORE INTO app_user(login) VALUES(?)")) {
            File[] rootFiles = sourceDirectory.listFiles();
            if (rootFiles == null) throw new SQLException("Cannot enumerate legacy account files");
            for (File file : rootFiles) {
                String name = file.getName();
                if (file.isFile() && name.startsWith("_") && name.endsWith(".properties")
                        && name.length() > ".properties".length() + 1) {
                    userInsert.setString(1, name.substring(1, name.length() - ".properties".length()));
                    userInsert.addBatch();
                }
            }
            userInsert.executeBatch();
        }

        java.util.Map<String, Integer> defaultGroups = new java.util.HashMap<String, Integer>();
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT code,group_id FROM player_group WHERE user_id IS NULL");
             java.sql.ResultSet result = query.executeQuery()) {
            while (result.next()) defaultGroups.put(result.getString(1), result.getInt(2));
        }

        try (PreparedStatement entries = connection.prepareStatement(
                "SELECT property_key,property_value FROM legacy_properties_entry WHERE path=?");
             PreparedStatement assignment = connection.prepareStatement(
                "INSERT OR IGNORE INTO player_group_assignment(user_id,tid,pid,group_id) VALUES(NULL,?,?,?)")) {
            File[] rootFiles = sourceDirectory.listFiles();
            if (rootFiles == null) throw new SQLException("Cannot enumerate legacy team files");
            for (File file : rootFiles) {
                if (!file.isFile() || !file.getName().matches("[0-9]+\\.properties")) continue;
                int tid;
                try {
                    tid = Integer.parseInt(file.getName().substring(0, file.getName().length() - ".properties".length()));
                } catch (NumberFormatException invalidTid) {
                    throw new SQLException("Invalid team data filename: " + file.getName(), invalidTid);
                }
                entries.setString(1, file.getName());
                try (java.sql.ResultSet result = entries.executeQuery()) {
                    while (result.next()) {
                        String key = result.getString(1);
                        if (!key.matches("[0-9]+")) continue;
                        int pid = Integer.parseInt(key);
                        if (pid <= 0) continue;
                        String[] fields = result.getString(2).split(",", 4);
                        if (fields.length < 3 || fields[2].isEmpty()) continue;
                        Integer groupId = defaultGroups.get(fields[2]);
                        if (groupId == null) {
                            throw new SQLException("Unknown legacy player group '" + fields[2]
                                    + "' for " + tid + "/" + pid);
                        }
                        assignment.setInt(1, tid);
                        assignment.setInt(2, pid);
                        assignment.setInt(3, groupId.intValue());
                        assignment.addBatch();
                    }
                }
            }
            assignment.executeBatch();
        }

        try (PreparedStatement complete = connection.prepareStatement(
                "INSERT INTO migration_marker(name,completed_at) VALUES(?,?)")) {
            complete.setString(1, "player_groups_legacy_imported");
            complete.setLong(2, System.currentTimeMillis());
            complete.executeUpdate();
        }
    }

    private void collect(File directory, List<File> files) throws IOException {
        File[] children = directory.listFiles();
        if (children == null) {
            throw new IOException("Cannot list legacy data directory: " + directory);
        }
        for (File child : children) {
            if (child.isDirectory()) {
                collect(child, files);
            } else if (child.isFile()
                    && isPropertiesDocument(child.getName())
                    && !isDatabaseArtifact(child)) {
                files.add(child);
            }
        }
    }


    private boolean isPropertiesDocument(String name) {
        String lower=name.toLowerCase(java.util.Locale.ROOT);
        return lower.endsWith(".properties")
                || lower.matches("(?:config|historico|factorx)\\\\.properties.*");
    }

    private boolean isDatabaseArtifact(File file) throws IOException {
        String candidate = file.getCanonicalPath();
        String databasePath = database.getDatabaseFile().getCanonicalPath();
        return candidate.equals(databasePath)
                || candidate.equals(databasePath + "-wal")
                || candidate.equals(databasePath + "-shm");
    }

    private boolean isImported(Connection connection, String path) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM legacy_properties_file WHERE path=?")) {
            statement.setString(1, path);
            try (java.sql.ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private void storeFile(Connection connection, String path, String checksum, long modifiedAt) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO legacy_properties_file(path,sha256,imported_at) VALUES(?,?,?)")) {
            statement.setString(1, path);
            statement.setString(2, checksum);
            statement.setLong(3, modifiedAt);
            statement.executeUpdate();
        }
    }

    private void storeEntries(Connection connection, String path, Properties properties) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM legacy_properties_entry WHERE path=?")) {
            delete.setString(1, path);
            delete.executeUpdate();
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO legacy_properties_entry(path,property_key,property_value) VALUES(?,?,?)")) {
            for (String key : properties.stringPropertyNames()) {
                insert.setString(1, path);
                insert.setString(2, key);
                insert.setString(3, properties.getProperty(key));
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder value = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                value.append(String.format("%02x", b & 0xff));
            }
            return value.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public static final class ImportReport {
        private final int discovered;
        private final int imported;
        private final int unchanged;

        ImportReport(int discovered, int imported, int unchanged) {
            this.discovered = discovered;
            this.imported = imported;
            this.unchanged = unchanged;
        }

        public int getDiscovered() { return discovered; }
        public int getImported() { return imported; }
        public int getUnchanged() { return unchanged; }
    }
}
