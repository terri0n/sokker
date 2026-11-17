package com.formulamanager.sokker.persistence;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite bootstrap for the legacy Sokker Asistente data directory.
 *
 * The database uses DELETE journaling so its directory contains no persistent
 * WAL/SHM sidecar files. Legacy properties are represented once, as key/value
 * rows; the importer does not retain a second copy of each file's bytes.
 */
public final class SqliteDatabase {
    private static final int SCHEMA_VERSION = 2;
    private final File databaseFile;

    public SqliteDatabase(File databaseFile) {
        if (databaseFile == null) {
            throw new IllegalArgumentException("databaseFile is required");
        }
        this.databaseFile = databaseFile.getAbsoluteFile();
    }

    public File getDatabaseFile() {
        return databaseFile;
    }

    public Connection openConnection() throws SQLException {
        File parent = databaseFile.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new SQLException("Cannot create SQLite directory: " + parent);
        }
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        boolean ok = false;
        try {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys=ON");
                statement.execute("PRAGMA busy_timeout=10000");
                statement.execute("PRAGMA synchronous=FULL");
            }
            ok = true;
            return connection;
        } finally {
            if (!ok) {
                connection.close();
            }
        }
    }

    public void initialize() throws SQLException, IOException {
        try (Connection connection = openConnection()) {
            // DELETE journaling avoids persistent -wal and -shm files beside the DB.
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode=DELETE");
            }
            connection.setAutoCommit(false);
            try {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("CREATE TABLE IF NOT EXISTS schema_version (version INTEGER NOT NULL)");
                    statement.execute("INSERT INTO schema_version(version) SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM schema_version)");
                }

                int currentVersion;
                try (Statement statement = connection.createStatement();
                     java.sql.ResultSet result = statement.executeQuery("SELECT version FROM schema_version LIMIT 1")) {
                    currentVersion = result.next() ? result.getInt(1) : 0;
                }
                if (currentVersion > SCHEMA_VERSION) {
                    throw new SQLException("Database schema " + currentVersion +
                            " is newer than supported schema " + SCHEMA_VERSION);
                }

                // Version 1 kept both the original file BLOB and its parsed
                // entries. Drop only the redundant BLOB; retain every key/value.
                if (currentVersion == 1 && hasColumn(connection, "legacy_properties_file", "content")) {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("ALTER TABLE legacy_properties_file DROP COLUMN content");
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    statement.execute("CREATE TABLE IF NOT EXISTS legacy_properties_file (" +
                            "path TEXT PRIMARY KEY NOT NULL," +
                            "sha256 TEXT NOT NULL," +
                            "imported_at INTEGER NOT NULL)");
                    statement.execute("CREATE TABLE IF NOT EXISTS legacy_properties_entry (" +
                            "path TEXT NOT NULL REFERENCES legacy_properties_file(path) ON DELETE CASCADE," +
                            "property_key TEXT NOT NULL," +
                            "property_value TEXT NOT NULL," +
                            "PRIMARY KEY(path, property_key))");
                    statement.execute("CREATE INDEX IF NOT EXISTS idx_legacy_properties_key " +
                            "ON legacy_properties_entry(property_key)");
                    statement.executeUpdate("UPDATE schema_version SET version=" + SCHEMA_VERSION);
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
                throw new SQLException("Cannot initialize SQLite schema", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static boolean hasColumn(Connection connection, String table, String column) throws SQLException {
        try (Statement statement = connection.createStatement();
             java.sql.ResultSet result = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (result.next()) {
                if (column.equalsIgnoreCase(result.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }
}
