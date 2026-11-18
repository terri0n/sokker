import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Juvenil;
import com.formulamanager.sokker.auxiliares.SokkerJuniorsXmlCompat;
import com.formulamanager.sokker.entity.Usuario;
import com.formulamanager.sokker.persistence.SqlitePersistence;
import com.formulamanager.sokker.persistence.TeamPlayerDAO;
import com.formulamanager.sokker.persistence.SqliteDatabase;
import com.formulamanager.sokker.persistence.UserDAO;

public final class PersistenceRoundTrip {
    private static final String PLAYER_RECORD = "Player,123,DEF,1,18/09/2026 12:00,true,0,,,,note,-1000,180,75,23,-1.5,false,#,-,-false,1200,25,100000,10,11,12,13,14,15,16,17,-0,10,MID,90.0,-5,6,7,true,*";

    private PersistenceRoundTrip() {}

    public static void main(String[] args) throws Exception {
        playerKnownFormatRoundTrip();
        unknownTrailingPlayerFieldsRoundTrip();
        unknownTrailingJuniorFieldsRoundTrip();
        juniorPositionsReadFromSerializedRecords();
        playerUnmanagedEntryRoundTrip();
        userUnknownFieldsRoundTrip();
        userDeletionRequestRoundTrip();
        retiredAutomaticUpdateSecretIsPurged();
    }

    private static void playerKnownFormatRoundTrip() {
        Jugador first = new Jugador(42, Arrays.asList(PLAYER_RECORD.split(",", -1)), true, null, null, null, null, null);
        String saved = first.serializar(true);
        Jugador second = new Jugador(42, Arrays.asList(saved.split(",", -1)), true, null, null, null, null, null);
        String savedAgain = second.serializar(true);

        require(saved.equals(savedAgain), "Jugador known-format round-trip changed after second read");
        require(saved.contains(",#,-,-false,"), "Jugador bot/login2 compatibility fields were not preserved");
    }

    private static void unknownTrailingPlayerFieldsRoundTrip() {
        String future = PLAYER_RECORD.substring(0, PLAYER_RECORD.length() - 2) + ",future-player-field,*";
        Jugador player = new Jugador(42, Arrays.asList(future.split(",", -1)), true, null, null, null, null, null);
        String actual = player.serializar(true);
        require(actual.contains(",future-player-field,*"), "Unknown trailing Jugador field was lost. actual=" + actual);
    }

    private static void unknownTrailingJuniorFieldsRoundTrip() {
        String known = new Juvenil(42, "Junior", 1200, 16, 5, 10, true, null).serializar(true);
        String future = known + ",future-junior-field";
        Juvenil junior = new Juvenil(42, Arrays.asList(future.split(",", -1)), true, null, null);
        require(future.equals(junior.serializar(true)), "Unknown trailing Juvenil field was lost");
    }

    private static void juniorPositionsReadFromSerializedRecords() {
        Map<Integer, String> juniors = new HashMap<Integer, String>();
        juniors.put(Integer.valueOf(12), "Junior,true,1200,16,5,10");
        juniors.put(Integer.valueOf(13), "Junior,false,1200,16,5,10");
        Map<Integer, Boolean> positions = SokkerJuniorsXmlCompat.readSavedPositions(juniors);
        require(Boolean.TRUE.equals(positions.get(12)), "SQLite junior position true was not read");
        require(Boolean.FALSE.equals(positions.get(13)), "SQLite junior position false was not read");
    }

    private static void playerUnmanagedEntryRoundTrip() throws Exception {
        Path data = configureTempDataPath("sokker-player-persistence-");
        Path factorx = Files.createTempDirectory("sokker-factorx-persistence-");
        File playerFile = data.resolve("123.properties").toFile();
        String orphan = "12,15,18,false,*";

        Properties initial = new Properties();
        initial.setProperty("42", PLAYER_RECORD);
        initial.setProperty("0", PLAYER_RECORD.replace("Player", "Test player"));
        initial.setProperty(orphan, "");
        try (FileOutputStream output = new FileOutputStream(playerFile)) {
            initial.store(output, null);
        }
        Properties otherTeam = new Properties();
        otherTeam.setProperty("42", PLAYER_RECORD.replace("Player", "Different team player"));
        try (FileOutputStream output = new FileOutputStream(data.resolve("124.properties").toFile())) {
            otherTeam.store(output, null);
        }
        try (FileOutputStream output = new FileOutputStream(data.resolve("123_historico.properties").toFile())) {
            initial.store(output, null);
        }
        Properties juniors = new Properties();
        juniors.setProperty("77", "Junior,true,1200,17,5,3,");
        try (FileOutputStream output = new FileOutputStream(data.resolve("123_juveniles.properties").toFile())) {
            juniors.store(output, null);
        }
        try (FileOutputStream output = new FileOutputStream(data.resolve("123_historico_juveniles.properties").toFile())) {
            juniors.store(output, null);
        }
        Path backup = Files.createDirectories(data.resolve("backup"));
        try (FileOutputStream output = new FileOutputStream(backup.resolve("123_1200.properties").toFile())) {
            initial.store(output, null);
        }

        File genericProperties = data.resolve("NTDB.properties").toFile();
        Properties generic = new Properties();
        generic.setProperty("alice", "bob");
        try (FileOutputStream output = new FileOutputStream(genericProperties)) {
            generic.store(output, null);
        }

        try {
            SqlitePersistence.initialize(data.toFile(), factorx.toFile());
            Path outsideProperties = Files.createTempFile(data.getParent(), "outside-properties-", ".properties");
            byte[] outsideBytes = "legacy=must-not-be-read\\n".getBytes(StandardCharsets.ISO_8859_1);
            Files.write(outsideProperties, outsideBytes);
            boolean outsideReadRejected = false;
            try {
                Util.cargar_properties(outsideProperties.toFile());
            } catch (IOException expected) {
                outsideReadRejected = true;
            }
            require(outsideReadRejected, "Properties reader retained a file-based fallback outside SQLite");
            boolean outsideWriteRejected = false;
            try {
                Util.guardar_properties(new Properties(), outsideProperties.toString());
            } catch (IOException expected) {
                outsideWriteRejected = true;
            }
            require(outsideWriteRejected, "Properties writer retained a file-based fallback outside SQLite");
            require(Arrays.equals(outsideBytes, Files.readAllBytes(outsideProperties)),
                    "Rejected legacy properties access modified the external file");
            Files.deleteIfExists(outsideProperties);
            require(!genericProperties.exists(), "Imported generic properties source was not retired");
            require("bob".equals(SqlitePersistence.load(genericProperties).getProperty("alice")),
                    "Generic properties data was not retained in SQLite");
            Usuario usuario = new Usuario(123, "Team");
            List<Jugador> loaded = AsistenteBO.leer_jugadores(123, "Team", false, usuario);
            require(loaded.size() == 2, "Test player PID zero was not loaded");
            require(loaded.stream().anyMatch(j -> Integer.valueOf(0).equals(j.getPid())),
                    "Synthetic test player was not preserved");

            AsistenteBO.grabar_jugadores(loaded, 123, 1200, false);
            require(!playerFile.exists(), "Legacy player file was not retired after migration");
            TeamPlayerDAO dao = new TeamPlayerDAO(new SqliteDatabase(SqlitePersistence.getAsistenteDatabaseFile(), true));
            require(dao.load(123, "PLAYER", 0).size() == 2 && dao.load(123, "PLAYER", 0).containsKey(0),
                    "Managed player rows, including the synthetic test player, were not persisted in SQLite");
            require(dao.load(124, "PLAYER", 0).size() == 1, "Same PID in a different tid was not isolated");
            require(dao.load(123, "PLAYER_HISTORY", 0).size() == 2 && dao.load(123, "PLAYER_HISTORY", 0).containsKey(0), "Player history was not migrated");
            require(dao.load(123, "JUVENILE", 0).size() == 1, "Juveniles were not migrated");
            require(dao.load(123, "JUVENILE_HISTORY", 0).size() == 1, "Juvenile history was not migrated");
            require(dao.load(123, "PLAYER", 1200).size() == 2 && dao.load(123, "PLAYER", 1200).containsKey(0), "Round snapshot was not migrated");
            require(AsistenteBO.obtener_equipos().length == 2, "Team listing did not use migrated team documents");
            require(AsistenteBO.obtener_ultimos_equipos().length == 2, "Recent team listing lost migrated timestamps");
            require(Arrays.asList(UsuarioBO.obtener_backups(123)).contains("123_1200.properties"),
                    "Migrated team backup was not listed");
            require(AsistenteBO.leer_juveniles(123, false, usuario).size() == 1, "Juvenile DAO read failed");
            require(AsistenteBO.leer_juveniles(123, false, usuario).get(0).serializar(true).endsWith(","),
                    "Trailing empty juvenile serialization field was lost");
            require(AsistenteBO.leer_juveniles(123, true, usuario).size() == 1, "Historical juvenile DAO read failed");
            require(!data.resolve("124.properties").toFile().exists(), "Second legacy team file was not retired");
            require(!data.resolve("123_historico.properties").toFile().exists(), "Legacy player history was not retired");
            require(!data.resolve("123_juveniles.properties").toFile().exists(), "Legacy juvenile file was not retired");
            require(!data.resolve("123_historico_juveniles.properties").toFile().exists(), "Legacy juvenile history was not retired");
            require(!backup.resolve("123_1200.properties").toFile().exists(), "Legacy player snapshot was not retired");
            try (java.sql.Connection connection = java.sql.DriverManager.getConnection(
                    "jdbc:sqlite:" + SqlitePersistence.getAsistenteDatabaseFile().getAbsolutePath());
                 java.sql.PreparedStatement query = connection.prepareStatement(
                    "SELECT property_value FROM team_player_extension WHERE tid=? AND property_key=?")) {
                query.setInt(1, 123);
                query.setString(2, orphan);
                try (java.sql.ResultSet result = query.executeQuery()) {
                    require(result.next() && "".equals(result.getString(1)),
                            "Unmanaged player property entry was lost during migration");
                }
            }

            List<Jugador> loadedAgain = AsistenteBO.leer_jugadores(123, "Team", false, usuario);
            require(loadedAgain.size() == 2 && loadedAgain.stream().anyMatch(j -> Integer.valueOf(42).equals(j.getPid())),
                    "Player database could not be re-read after saving to SQLite");
        } finally {
            SqlitePersistence.shutdown();
        }
    }

    private static void userUnknownFieldsRoundTrip() throws Exception {
        Path data = configureTempDataPath("sokker-user-persistence-");

        Usuario seed = new Usuario(Arrays.asList("alice,pw,sokkerAlice,123,,123,Team,,1200,,0,*".split(",", -1)));
        String known = seed.serializar();
        int marker = known.lastIndexOf(",*");
        require(marker >= 0, "Usuario serialization has no sentinel");
        String withFutureFields = known.substring(0, marker) + ",future-one,future-two,*";

        File userFile = data.resolve("_alice.properties").toFile();
        Properties initial = new Properties();
        initial.setProperty("usuario", withFutureFields);
        initial.setProperty("notas", "round-trip");
        initial.setProperty("future.key", "future-value");
        initial.setProperty("entrenamiento1200",
                "Rapidez-Pases-Rapidez-Tecnica,,16;16;16;16;11;16;16;16,15.39583,16.5,future-training,*");
        try (FileOutputStream output = new FileOutputStream(userFile)) {
            initial.store(output, null);
        }
        initializeSqlite(data);

        Usuario loaded = UsuarioBO.leer_usuario("alice", false);
        require(loaded != null, "Usuario could not be read");
        UsuarioBO.grabar_usuario(loaded);
        assertUserFutureData(userFile);

        Usuario loadedAgain = UsuarioBO.leer_usuario("alice", false);
        require(loadedAgain != null, "Usuario could not be re-read after save");
        UsuarioBO.grabar_usuario(loadedAgain);
        assertUserFutureData(userFile);
    }

    private static void userDeletionRequestRoundTrip() throws Exception {
        Path data = configureTempDataPath("sokker-user-deletion-marker-");
        Usuario seed = new Usuario(Arrays.asList("alice,pw,sokkerAlice,123,,123,Team,,1200,,0,*".split(",", -1)));
        File userFile = data.resolve("_alice.properties").toFile();

        Properties initial = new Properties();
        initial.setProperty("usuario", seed.serializar());
        initial.setProperty("notas", "marker-round-trip");
        initial.setProperty("future.key", "future-value");
        initial.setProperty("account_deletion_requested_at", "1727186400000");
        try (FileOutputStream output = new FileOutputStream(userFile)) {
            initial.store(output, null);
        }
        initializeSqlite(data);

        for (int pass = 0; pass < 2; pass++) {
            Usuario loaded = UsuarioBO.leer_usuario("alice", false);
            require(loaded != null, "Usuario with deletion marker could not be read");
            UsuarioBO.grabar_usuario(loaded);
            UserDAO.UserRecord saved = userDao().load("alice");
            require("future-value".equals(saved.getExtensions().getProperty("future.key")), "Unknown key was lost with deletion marker");
            require(Long.valueOf(1727186400000L).equals(saved.getDeletionRequestedAt()),
                    "Deletion request marker was lost on save");
            require(Long.valueOf(1727186400000L).equals(UsuarioBO.obtener_fecha_solicitud_borrado("alice")),
                    "Deletion request timestamp helper returned a different value");
        }
    }

    private static void retiredAutomaticUpdateSecretIsPurged() throws Exception {
        Path data = configureTempDataPath("sokker-user-retired-secret-");
        Usuario seed = new Usuario(Arrays.asList("alice,pw,sokkerAlice,123,,123,Team,,1200,,0,*".split(",", -1)));
        String known = seed.serializar();
        int marker = known.lastIndexOf(",*");
        require(marker >= 0, "Usuario serialization has no sentinel");
        String withSecretAndFutureFields = known.substring(0, marker)
                + "bGVnYWN5LXNlY3JldA==,future-one,future-two,*";

        File userFile = data.resolve("_alice.properties").toFile();
        Properties initial = new Properties();
        initial.setProperty("usuario", withSecretAndFutureFields);
        initial.setProperty("notas", "retired-secret");
        initial.setProperty("future.key", "future-value");
        try (FileOutputStream output = new FileOutputStream(userFile)) {
            initial.store(output, null);
        }

        initializeSqlite(data);
        Usuario loaded = UsuarioBO.leer_usuario("alice", false);
        require(loaded != null, "Usuario with retired secret could not be read");
        UsuarioBO.grabar_usuario(loaded);

        String savedUsuario = userDao().load("alice").getSerialized();
        require(savedUsuario != null, "Usuario record disappeared while purging retired secret");
        require(!savedUsuario.contains("bGVnYWN5LXNlY3JldA=="), "Retired automatic-update secret survived save");
        require(savedUsuario.endsWith(",future-one,future-two,*"), "Future usuario fields were lost while purging secret");
        require("future-value".equals(userDao().load("alice").getExtensions().getProperty("future.key")), "Unknown property was lost while purging retired secret");
    }

    private static Path configureTempDataPath(String prefix) throws Exception {
        Path tomcat = Files.createTempDirectory(prefix);
        Path webapp = tomcat.resolve("webapps/sokker");
        Path data = tomcat.resolve("data");
        Files.createDirectories(webapp);
        Files.createDirectories(data);
        Files.createDirectories(tomcat.resolve("conf"));

        String dataPath = data.toAbsolutePath().toString() + File.separator;
        String serverXml = "<Server><GlobalNamingResources>"
                + "<Environment name=\"path\" value=\"" + xml(dataPath) + "\" type=\"java.lang.String\"/>"
                + "</GlobalNamingResources></Server>";
        Files.write(tomcat.resolve("conf/server.xml"), serverXml.getBytes(StandardCharsets.UTF_8));
        SystemUtil.REAL_PATH = webapp.toAbsolutePath().toString() + File.separator;
        return data;
    }

    private static void initializeSqlite(Path data) throws Exception {
        Path factorx = Files.createTempDirectory("sokker-factorx-user-");
        SqlitePersistence.initialize(data.toFile(), factorx.toFile());
    }

    private static UserDAO userDao() throws Exception {
        return new UserDAO(new SqliteDatabase(SqlitePersistence.getAsistenteDatabaseFile(), true));
    }

    private static void assertUserFutureData(File userFile) throws Exception {
        UserDAO.UserRecord saved = userDao().load("alice");
        require(saved != null, "Migrated user row is missing");
        require("future-value".equals(saved.getExtensions().getProperty("future.key")),
                "Unknown Usuario.properties key was lost");
        String usuario = saved.getSerialized();
        require(usuario != null && usuario.endsWith(",future-one,future-two,*"),
                "Future usuario= fields were lost");
        String entrenamiento = saved.getTraining().get(1200);
        require(entrenamiento != null && entrenamiento.endsWith(",future-training,*"),
                "Future entrenamiento fields were lost");
        require(!userFile.exists(), "Legacy user file was not retired after SQLite migration");
    }

    private static Properties load(File file) throws Exception {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        }
        return properties;
    }

    private static String xml(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
