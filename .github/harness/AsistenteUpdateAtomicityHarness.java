import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.formulamanager.sokker.persistence.AsistenteUpdateDAO;
import com.formulamanager.sokker.persistence.SqliteDatabase;
import com.formulamanager.sokker.persistence.SqlitePersistence;
import com.formulamanager.sokker.persistence.TeamPlayerDAO;
import com.formulamanager.sokker.persistence.UserDAO;

/** Proves that player, junior and user jornada state is one SQLite transaction. */
public final class AsistenteUpdateAtomicityHarness {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("asistente-atomic-");
        Path factorx = Files.createTempDirectory("factorx-atomic-");
        SqlitePersistence.initialize(root.toFile(), factorx.toFile());
        SqliteDatabase database = new SqliteDatabase(SqlitePersistence.getAsistenteDatabaseFile(), true);
        TeamPlayerDAO players = new TeamPlayerDAO(database);
        UserDAO users = new UserDAO(database);

        Map<Integer, String> oldPlayers = singleton(10, "old-player");
        Map<Integer, String> oldHistory = singleton(11, "old-history");
        Map<Integer, String> oldJuniors = singleton(12, "old-junior");
        Map<Integer, String> oldJuniorHistory = singleton(13, "old-junior-history");
        players.save(123, "PLAYER", 0, oldPlayers);
        players.save(123, "PLAYER_HISTORY", 0, oldHistory);
        players.save(123, "JUVENILE", 0, oldJuniors);
        players.save(123, "JUVENILE_HISTORY", 0, oldJuniorHistory);
        users.save("atomic", "old-user,*", "old-notes", null, Collections.<Integer, String>emptyMap(), new Properties());
        long expectedUserUpdatedAt = users.load("atomic").getUpdatedAt();

        Map<Integer, String> newPlayers = singleton(20, "new-player");
        Map<Integer, String> newHistory = singleton(21, "new-history");
        Map<Integer, String> newJuniors = singleton(22, "new-junior");
        Map<Integer, String> newJuniorHistory = singleton(23, "new-junior-history");
        AsistenteUpdateDAO update = new AsistenteUpdateDAO(database);

        boolean failed = false;
        try {
            update.save(123, oldPlayers, oldHistory, oldJuniors, oldJuniorHistory,
                    newPlayers, newHistory, newJuniors, newJuniorHistory,
                    new UserDAO.SaveData("atomic", null, "must-rollback", null,
                            Collections.<Integer, String>emptyMap(), new Properties()), expectedUserUpdatedAt);
        } catch (IllegalArgumentException expected) {
            failed = true;
        }
        require(failed, "Invalid user write did not fail the update");
        require("old-player".equals(players.load(123, "PLAYER", 0).get(10)), "Current players were partially committed");
        require("old-history".equals(players.load(123, "PLAYER_HISTORY", 0).get(11)), "Player history was partially committed");
        require("old-junior".equals(players.load(123, "JUVENILE", 0).get(12)), "Juniors were partially committed");
        require("old-junior-history".equals(players.load(123, "JUVENILE_HISTORY", 0).get(13)), "Junior history was partially committed");
        require("old-user,*".equals(users.load("atomic").getSerialized()), "User jornada/settings were partially committed");

        players.save(123, "PLAYER", 0, singleton(99, "concurrent-player"));
        boolean teamConflictRejected = false;
        try {
            update.save(123, oldPlayers, oldHistory, oldJuniors, oldJuniorHistory,
                    newPlayers, newHistory, newJuniors, newJuniorHistory,
                    new UserDAO.SaveData("atomic", "stale-user,*", "stale", null,
                            Collections.<Integer, String>emptyMap(), new Properties()), expectedUserUpdatedAt);
        } catch (SQLException expected) {
            teamConflictRejected = true;
        }
        require(teamConflictRejected, "A stale update overwrote concurrent team changes");
        require("concurrent-player".equals(players.load(123, "PLAYER", 0).get(99)),
                "Concurrent player changes were lost after conflict detection");
        require("old-user,*".equals(users.load("atomic").getSerialized()),
                "Team conflict changed the user record");

        Map<Integer, String> expectedCurrentAfterConcurrent = players.load(123, "PLAYER", 0);
        long staleUserVersion = users.load("atomic").getUpdatedAt();
        users.setUpdatedAt("atomic", staleUserVersion - 1L);
        boolean userConflictRejected = false;
        try {
            update.save(123, expectedCurrentAfterConcurrent, oldHistory, oldJuniors, oldJuniorHistory,
                    newPlayers, newHistory, newJuniors, newJuniorHistory,
                    new UserDAO.SaveData("atomic", "stale-user,*", "stale", null,
                            Collections.<Integer, String>emptyMap(), new Properties()), staleUserVersion);
        } catch (SQLException expected) {
            userConflictRejected = true;
        }
        require(userConflictRejected, "A stale update overwrote concurrent account changes");
        require("concurrent-player".equals(players.load(123, "PLAYER", 0).get(99)),
                "User conflict changed team data");

        long currentUserVersion = users.load("atomic").getUpdatedAt();
        Map<Integer, String> training = singleton(1200, "DEFENSA,,,,,*");
        Properties extensions = new Properties();
        extensions.setProperty("future-field", "preserved");
        update.save(123, expectedCurrentAfterConcurrent, oldHistory, oldJuniors, oldJuniorHistory,
                newPlayers, newHistory, newJuniors, newJuniorHistory,
                new UserDAO.SaveData("atomic", "new-user,*", "new-notes", null, training, extensions),
                currentUserVersion);
        require("new-player".equals(players.load(123, "PLAYER", 0).get(20)), "Current players were not committed");
        require("new-history".equals(players.load(123, "PLAYER_HISTORY", 0).get(21)), "Player history was not committed");
        require("new-junior".equals(players.load(123, "JUVENILE", 0).get(22)), "Juniors were not committed");
        require("new-junior-history".equals(players.load(123, "JUVENILE_HISTORY", 0).get(23)), "Junior history was not committed");
        UserDAO.UserRecord saved = users.load("atomic");
        require("new-user,*".equals(saved.getSerialized()) && "new-notes".equals(saved.getNotes()),
                "User state was not committed with team data");
        require("DEFENSA,,,,,*".equals(saved.getTraining().get(1200)), "Training data was not committed");
        require("preserved".equals(saved.getExtensions().getProperty("future-field")),
                "Unknown user fields were not committed");

        SqlitePersistence.shutdown();
        delete(root.toFile());
        delete(factorx.toFile());
    }

    private static Map<Integer, String> singleton(int pid, String value) {
        Map<Integer, String> result = new HashMap<Integer, String>();
        result.put(Integer.valueOf(pid), value);
        return result;
    }

    private static void delete(java.io.File file) {
        if (file.isDirectory()) {
            java.io.File[] children = file.listFiles();
            if (children != null) for (java.io.File child : children) delete(child);
        }
        file.delete();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
