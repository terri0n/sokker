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
 * This class deliberately does not replace the existing BO/DAO persistence
 * yet. The database is opt-in until the complete import and compatibility
 * suite has been run against a copy of production data.
 */
public final class SqliteDatabase {
    private static final int SCHEMA_VERSION = 1;
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
            connection.setAutoCommit(false);
            try {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("PRAGMA journal_mode=WAL");
                    statement.execute("CREATE TABLE IF NOT EXISTS schema_version (version INTEGER NOT NULL)");
                    statement.execute("INSERT INTO schema_version(version) SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM schema_version)");
                    statement.execute("CREATE TABLE IF NOT EXISTS legacy_properties_file (" +
                            "path TEXT PRIMARY KEY NOT NULL," +
                            "sha256 TEXT NOT NULL," +
                            "content BLOB NOT NULL," +
                            "imported_at INTEGER NOT NULL)");
                    statement.execute("CREATE TABLE IF NOT EXISTS legacy_properties_entry (" +
                            "path TEXT NOT NULL REFERENCES legacy_properties_file(path) ON DELETE CASCADE," +
                            "property_key TEXT NOT NULL," +
                            "property_value TEXT NOT NULL," +
                            "PRIMARY KEY(path, property_key))");
                    statement.execute("CREATE INDEX IF NOT EXISTS idx_legacy_properties_key " +
                            "ON legacy_properties_entry(property_key)");
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
                if (currentVersion < SCHEMA_VERSION) {
                    try (Statement statement = connection.createStatement()) {
                        statement.executeUpdate("UPDATE schema_version SET version=" + SCHEMA_VERSION);
                    }
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
}
