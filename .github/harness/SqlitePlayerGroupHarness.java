import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.formulamanager.sokker.dao.GrupoJugadorDAO;
import com.formulamanager.sokker.entity.GrupoJugador;
import com.formulamanager.sokker.persistence.SqliteDatabase;
import com.formulamanager.sokker.persistence.SqlitePersistence;

public final class SqlitePlayerGroupHarness {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("sokker-player-groups-");
        Path asistente = root.resolve("asistente");
        Path factorx = root.resolve("factorx");
        Files.createDirectories(asistente);
        Files.createDirectories(factorx);
        try {
            store(asistente.resolve("_alice.properties"), "usuario", "alice");
            store(asistente.resolve("_bob.properties"), "usuario", "bob");
            store(asistente.resolve("123.properties"), "10", "Test Player,123,GK,1,2026-01-01,true");
            SqlitePersistence.initialize(asistente.toFile(), factorx.toFile());

            List<GrupoJugador> aliceGroups = GrupoJugadorDAO.obtenerGrupos("alice");
            require(aliceGroups.size() == 6, "Expected six shared default groups");
            int goalkeeperId = findDefault(aliceGroups, "GK").getId();
            int customId = GrupoJugadorDAO.crearGrupo("alice", "Prospects").getId();
            require(GrupoJugadorDAO.obtenerGrupos("ALICE").size() == 7,
                    "Group ownership was case-sensitive for login names");
            GrupoJugadorDAO.renombrarGrupo("ALICE", customId, "Prospects");

            Map<Integer, GrupoJugador> initialAlice = GrupoJugadorDAO.obtenerAsignaciones("alice", 123);
            Map<Integer, GrupoJugador> initialBob = GrupoJugadorDAO.obtenerAsignaciones("bob", 123);
            require(initialAlice.get(10).getId() == goalkeeperId, "Legacy GK assignment was not migrated");
            require(initialBob.get(10).getId() == goalkeeperId, "Default assignment was not shared");

            GrupoJugadorDAO.asignarGrupo("alice", 123, 10, customId);
            require(GrupoJugadorDAO.obtenerAsignaciones("alice", 123).get(10).getId() == customId,
                    "User custom group did not override the shared default");
            require(GrupoJugadorDAO.obtenerAsignaciones("bob", 123).get(10).getId() == goalkeeperId,
                    "Alice's custom group leaked to Bob");

            boolean rejected = false;
            try {
                GrupoJugadorDAO.asignarGrupo("bob", 123, 10, customId);
            } catch (IllegalArgumentException expected) {
                rejected = true;
            }
            require(rejected, "A user was allowed to assign another user's custom group");

            GrupoJugadorDAO.eliminarGrupo("alice", customId);
            require(GrupoJugadorDAO.obtenerAsignaciones("alice", 123).get(10).getId() == goalkeeperId,
                    "Deleting a custom group did not restore the shared default assignment");

            int secondCustomId = GrupoJugadorDAO.crearGrupo("alice", "Private").getId();
            GrupoJugadorDAO.asignarGrupo("alice", 123, 10, secondCustomId);
            require(SqlitePersistence.delete(asistente.resolve("_alice.properties").toFile()),
                    "Account properties were not deleted");
            try (Connection connection = new SqliteDatabase(SqlitePersistence.getAsistenteDatabaseFile(), true).openConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT COUNT(*) FROM app_user WHERE login='alice'");
                 ResultSet result = statement.executeQuery()) {
                require(result.next() && result.getInt(1) == 0, "Deleting the account did not delete its user identity");
            }
            try (Connection connection = new SqliteDatabase(SqlitePersistence.getAsistenteDatabaseFile(), true).openConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT COUNT(*) FROM player_group WHERE user_id IS NOT NULL");
                 ResultSet result = statement.executeQuery()) {
                require(result.next() && result.getInt(1) == 0, "Deleting the account did not cascade its custom groups");
            }
            require(GrupoJugadorDAO.obtenerAsignaciones("bob", 123).get(10).getId() == goalkeeperId,
                    "Deleting Alice damaged the shared default assignment");
            System.out.println("SqlitePlayerGroupHarness: OK");
        } finally {
            SqlitePersistence.shutdown();
        }
    }

    private static GrupoJugador findDefault(List<GrupoJugador> groups, String code) {
        for (GrupoJugador group : groups) {
            if (code.equals(group.getCode()) && group.isDefault()) return group;
        }
        throw new AssertionError("Missing default group " + code);
    }

    private static void store(Path path, String key, String value) throws Exception {
        Properties properties = new Properties();
        properties.setProperty(key, value);
        try (FileOutputStream output = new FileOutputStream(path.toFile())) {
            properties.store(output, null);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
