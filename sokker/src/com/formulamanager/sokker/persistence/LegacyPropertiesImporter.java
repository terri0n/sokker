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
                    if (isUnchanged(connection, relativePath, checksum)) {
                        unchanged++;
                        continue;
                    }
                    Properties properties = new Properties();
                    properties.load(new ByteArrayInputStream(bytes));
                    storeFile(connection, relativePath, checksum);
                    storeEntries(connection, relativePath, properties);
                    imported++;
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

    private void collect(File directory, List<File> files) throws IOException {
        File[] children = directory.listFiles();
        if (children == null) {
            throw new IOException("Cannot list legacy data directory: " + directory);
        }
        for (File child : children) {
            if (child.isDirectory()) {
                collect(child, files);
            } else if (child.isFile()
                    && child.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".properties")
                    && !isDatabaseArtifact(child)) {
                files.add(child);
            }
        }
    }

    private boolean isDatabaseArtifact(File file) throws IOException {
        String candidate = file.getCanonicalPath();
        String databasePath = database.getDatabaseFile().getCanonicalPath();
        return candidate.equals(databasePath)
                || candidate.equals(databasePath + "-wal")
                || candidate.equals(databasePath + "-shm");
    }

    private boolean isUnchanged(Connection connection, String path, String checksum) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT sha256 FROM legacy_properties_file WHERE path=?")) {
            statement.setString(1, path);
            try (java.sql.ResultSet result = statement.executeQuery()) {
                return result.next() && checksum.equals(result.getString(1));
            }
        }
    }

    private void storeFile(Connection connection, String path, String checksum) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO legacy_properties_file(path,sha256,imported_at) VALUES(?,?,?)")) {
            statement.setString(1, path);
            statement.setString(2, checksum);
            statement.setLong(3, System.currentTimeMillis());
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
