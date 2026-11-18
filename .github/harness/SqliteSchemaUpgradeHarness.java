import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;

import com.formulamanager.sokker.persistence.SqliteDatabase;
import com.formulamanager.sokker.persistence.TeamPlayerDAO;

/** Verifies schema v6 upgrades without losing existing rows and accepts PID zero. */
public final class SqliteSchemaUpgradeHarness {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("sqlite-schema-upgrade-");
        SqliteDatabase database = new SqliteDatabase(root.resolve("asistente.db").toFile(), true);
        database.initialize();

        try (Connection connection = database.openConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP INDEX IF EXISTS idx_team_player_lookup");
            statement.execute("DROP TABLE team_player");
            statement.execute("CREATE TABLE team_player (" +
                    "tid INTEGER NOT NULL CHECK(tid>0), pid INTEGER NOT NULL CHECK(pid>0), " +
                    "record_type TEXT NOT NULL CHECK(record_type IN ('PLAYER','PLAYER_HISTORY','JUVENILE','JUVENILE_HISTORY')), " +
                    "snapshot_round INTEGER NOT NULL DEFAULT 0, serialized_data TEXT NOT NULL, " +
                    "PRIMARY KEY(tid,pid,record_type,snapshot_round))");
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO team_player(tid,pid,record_type,snapshot_round,serialized_data) VALUES(123,42,'PLAYER',0,'old-player')")) {
                insert.executeUpdate();
            }
            statement.execute("UPDATE schema_version SET version=6");
        }

        database.initialize();
        TeamPlayerDAO dao = new TeamPlayerDAO(database);
        if (!"old-player".equals(dao.load(123, "PLAYER", 0).get(42)))
            throw new AssertionError("Schema upgrade lost an existing player row");
        java.util.Map<Integer, String> upgradedPlayers = new java.util.LinkedHashMap<Integer, String>();
        upgradedPlayers.put(Integer.valueOf(42), "old-player");
        upgradedPlayers.put(Integer.valueOf(0), "test-player");
        dao.save(123, "PLAYER", 0, upgradedPlayers);
        if (!"test-player".equals(dao.load(123, "PLAYER", 0).get(0)))
            throw new AssertionError("Upgraded schema does not accept synthetic PID zero");
        if (!"old-player".equals(dao.load(123, "PLAYER", 0).get(42)))
            throw new AssertionError("Saving PID zero lost an existing player");
        try (Connection connection = database.openConnection(); Statement statement = connection.createStatement();
             java.sql.ResultSet result = statement.executeQuery("PRAGMA integrity_check")) {
            if (!result.next() || !"ok".equalsIgnoreCase(result.getString(1)))
                throw new AssertionError("SQLite integrity check failed after schema upgrade");
        }
        delete(root.toFile());
    }

    private static void delete(java.io.File file) {
        if (file.isDirectory()) {
            java.io.File[] children = file.listFiles();
            if (children != null) for (java.io.File child : children) delete(child);
        }
        file.delete();
    }
}
