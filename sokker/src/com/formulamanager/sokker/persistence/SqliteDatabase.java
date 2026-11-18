package com.formulamanager.sokker.persistence;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    private static final int SCHEMA_VERSION = 7;
    private final File databaseFile;
    private final boolean playerGroupsEnabled;

    public SqliteDatabase(File databaseFile) {
        this(databaseFile, true);
    }

    public SqliteDatabase(File databaseFile, boolean playerGroupsEnabled) {
        if (databaseFile == null) {
            throw new IllegalArgumentException("databaseFile is required");
        }
        this.databaseFile = databaseFile.getAbsoluteFile();
        this.playerGroupsEnabled = playerGroupsEnabled;
    }

    public File getDatabaseFile() {
        return databaseFile;
    }

    public boolean isPlayerGroupsEnabled() {
        return playerGroupsEnabled;
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
                    if (playerGroupsEnabled) {
                        createPlayerGroupSchema(connection);
                    } else {
                        createFactorxSchema(connection);
                    }
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


    private static void migrateTeamPlayerPidZero(Connection connection) throws SQLException {
        String tableSql = null;
        try (PreparedStatement q = connection.prepareStatement(
                "SELECT sql FROM sqlite_master WHERE type='table' AND name='team_player'");
             ResultSet r = q.executeQuery()) {
            if (r.next()) tableSql = r.getString(1);
        }
        if (tableSql == null || !tableSql.contains("CHECK(pid>0)")) return;
        try (Statement s = connection.createStatement()) {
            s.execute("DROP INDEX IF EXISTS idx_team_player_lookup");
            s.execute("ALTER TABLE team_player RENAME TO team_player_before_pid_zero");
            s.execute("CREATE TABLE team_player (" +
                    "tid INTEGER NOT NULL CHECK(tid>0), pid INTEGER NOT NULL CHECK(pid>=0), " +
                    "record_type TEXT NOT NULL CHECK(record_type IN ('PLAYER','PLAYER_HISTORY','JUVENILE','JUVENILE_HISTORY')), " +
                    "snapshot_round INTEGER NOT NULL DEFAULT 0, serialized_data TEXT NOT NULL, " +
                    "PRIMARY KEY(tid,pid,record_type,snapshot_round))");
            s.execute("INSERT INTO team_player(tid,pid,record_type,snapshot_round,serialized_data) " +
                    "SELECT tid,pid,record_type,snapshot_round,serialized_data FROM team_player_before_pid_zero");
            s.execute("DROP TABLE team_player_before_pid_zero");
        }
    }

    private static void createFactorxSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS factorx_migration (name TEXT PRIMARY KEY NOT NULL, completed_at INTEGER NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS factorx_formation (scope TEXT PRIMARY KEY NOT NULL, formation TEXT NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS factorx_property_extension (path TEXT NOT NULL, property_key TEXT NOT NULL, property_value TEXT NOT NULL, PRIMARY KEY(path,property_key))");
            statement.execute("CREATE TABLE IF NOT EXISTS factorx_player (" +
                    "scope TEXT NOT NULL CHECK(scope IN ('junior','senior','Venezuela','Paraguay'))," +
                    "record_type TEXT NOT NULL CHECK(record_type IN ('CURRENT','HISTORY','SNAPSHOT'))," +
                    "edition INTEGER NOT NULL, snapshot_round INTEGER NOT NULL DEFAULT 0, pid INTEGER NOT NULL," +
                    "name TEXT, team TEXT, tid INTEGER, age INTEGER, value INTEGER, round_no INTEGER, position TEXT," +
                    "condicion INTEGER, rapidez INTEGER, tecnica INTEGER, pases INTEGER, porteria INTEGER, defensa INTEGER, creacion INTEGER, anotacion INTEGER," +
                    "original_value INTEGER, original_round INTEGER, original_condicion INTEGER, original_rapidez INTEGER," +
                    "original_tecnica INTEGER, original_pases INTEGER, original_porteria INTEGER, original_defensa INTEGER," +
                    "original_creacion INTEGER, original_anotacion INTEGER," +
                    "PRIMARY KEY(scope,record_type,edition,snapshot_round,pid))");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_factorx_player_history ON factorx_player(scope,record_type,edition,pid)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_factorx_player_tid ON factorx_player(scope,tid,pid)");
        }
    }

    private static void createPlayerGroupSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS migration_marker (" +
                    "name TEXT PRIMARY KEY NOT NULL," +
                    "completed_at INTEGER NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS team_player (" +
                    "tid INTEGER NOT NULL CHECK(tid>0), pid INTEGER NOT NULL CHECK(pid>=0)," +
                    "record_type TEXT NOT NULL CHECK(record_type IN ('PLAYER','PLAYER_HISTORY','JUVENILE','JUVENILE_HISTORY'))," +
                    "snapshot_round INTEGER NOT NULL DEFAULT 0, serialized_data TEXT NOT NULL," +
                    "PRIMARY KEY(tid,pid,record_type,snapshot_round))");
            migrateTeamPlayerPidZero(connection);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_team_player_lookup ON team_player(tid,record_type,snapshot_round,pid)");
            statement.execute("CREATE TABLE IF NOT EXISTS team_player_document (" +
                    "source_path TEXT PRIMARY KEY NOT NULL, tid INTEGER NOT NULL, " +
                    "record_type TEXT NOT NULL CHECK(record_type IN ('PLAYER','PLAYER_HISTORY','JUVENILE','JUVENILE_HISTORY')), " +
                    "snapshot_round INTEGER NOT NULL DEFAULT 0, updated_at INTEGER NOT NULL)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_team_player_document_lookup " +
                    "ON team_player_document(tid,record_type,snapshot_round,updated_at)");
            statement.execute("CREATE TABLE IF NOT EXISTS team_player_extension (" +
                    "tid INTEGER NOT NULL, record_type TEXT NOT NULL, snapshot_round INTEGER NOT NULL," +
                    "property_key TEXT NOT NULL, property_value TEXT NOT NULL," +
                    "PRIMARY KEY(tid,record_type,snapshot_round,property_key))");
            statement.execute("CREATE TABLE IF NOT EXISTS app_user ("+
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "login TEXT NOT NULL COLLATE NOCASE UNIQUE)");
            addColumn(connection, "app_user", "serialized_data", "TEXT");
            addColumn(connection, "app_user", "notes", "TEXT");
            addColumn(connection, "app_user", "account_deletion_requested_at", "INTEGER");
            addColumn(connection, "app_user", "updated_at", "INTEGER NOT NULL DEFAULT 0");
            statement.execute("CREATE TABLE IF NOT EXISTS user_training (" +
                    "user_id INTEGER NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE," +
                    "round_no INTEGER NOT NULL, serialized_data TEXT NOT NULL, PRIMARY KEY(user_id,round_no))");
            statement.execute("CREATE TABLE IF NOT EXISTS user_property_extension (" +
                    "user_id INTEGER NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE," +
                    "property_key TEXT NOT NULL, property_value TEXT NOT NULL, PRIMARY KEY(user_id,property_key))");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_app_user_updated ON app_user(updated_at)");
            statement.execute("CREATE TABLE IF NOT EXISTS player_group (" +
                    "group_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER REFERENCES app_user(user_id) ON DELETE CASCADE," +
                    "code TEXT," +
                    "name TEXT NOT NULL," +
                    "sort_order INTEGER NOT NULL," +
                    "CHECK ((user_id IS NULL AND code IS NOT NULL) OR (user_id IS NOT NULL AND code IS NULL)))");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_player_group_default_code " +
                    "ON player_group(code) WHERE user_id IS NULL");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_player_group_user_name " +
                    "ON player_group(user_id, lower(name)) WHERE user_id IS NOT NULL");
            statement.execute("CREATE TABLE IF NOT EXISTS player_group_assignment (" +
                    "user_id INTEGER REFERENCES app_user(user_id) ON DELETE CASCADE," +
                    "tid INTEGER NOT NULL CHECK (tid > 0)," +
                    "pid INTEGER NOT NULL CHECK (pid > 0)," +
                    "group_id INTEGER NOT NULL REFERENCES player_group(group_id) ON DELETE CASCADE)");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_player_group_assignment_default " +
                    "ON player_group_assignment(tid, pid) WHERE user_id IS NULL");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_player_group_assignment_user " +
                    "ON player_group_assignment(user_id, tid, pid) WHERE user_id IS NOT NULL");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_player_group_assignment_group " +
                    "ON player_group_assignment(group_id)");
            statement.execute("CREATE TRIGGER IF NOT EXISTS trg_player_group_assignment_owner_insert " +
                    "BEFORE INSERT ON player_group_assignment " +
                    "WHEN NOT EXISTS (SELECT 1 FROM player_group g WHERE g.group_id=NEW.group_id AND g.user_id IS NEW.user_id) " +
                    "BEGIN SELECT RAISE(ABORT, 'player group owner mismatch'); END");
            statement.execute("CREATE TRIGGER IF NOT EXISTS trg_player_group_assignment_owner_update " +
                    "BEFORE UPDATE OF user_id, group_id ON player_group_assignment " +
                    "WHEN NOT EXISTS (SELECT 1 FROM player_group g WHERE g.group_id=NEW.group_id AND g.user_id IS NEW.user_id) " +
                    "BEGIN SELECT RAISE(ABORT, 'player group owner mismatch'); END");
        }
        String[][] defaults = {
            {"GK", "common.goalkeepers", "1"},
            {"DEF", "common.defenders", "2"},
            {"MID", "common.midfielders", "3"},
            {"ATT", "common.forwards", "4"},
            {"BANQUILLO", "common.bench", "5"},
            {"OTRA", "common.other_position", "6"}
        };
        try (java.sql.PreparedStatement insert = connection.prepareStatement(
                "INSERT OR IGNORE INTO player_group(user_id,code,name,sort_order) VALUES(NULL,?,?,?)")) {
            for (String[] group : defaults) {
                insert.setString(1, group[0]);
                insert.setString(2, group[1]);
                insert.setInt(3, Integer.parseInt(group[2]));
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }


    private static void addColumn(Connection connection,String table,String column,String definition) throws SQLException {
        if(!hasColumn(connection,table,column)) {
            try(Statement statement=connection.createStatement()) {
                statement.execute("ALTER TABLE "+table+" ADD COLUMN "+column+" "+definition);
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
