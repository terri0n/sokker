import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

public final class PersistenceRoundTrip {
    private static final String PLAYER_RECORD = "Player,123,DEF,1,18/09/2026 12:00,true,0,,,,note,-1000,180,75,23,-1.5,false,#,-,-false,1200,25,100000,10,11,12,13,14,15,16,17,-0,10,MID,90.0,-5,6,7,true,*";

    private PersistenceRoundTrip() {}

    public static void main(String[] args) throws Exception {
        playerKnownFormatRoundTrip();
        playerUnmanagedEntryRoundTrip();
        playerColorChangeRoundTrip();
        userUnknownFieldsRoundTrip();
    }

    private static void playerKnownFormatRoundTrip() {
        Jugador first = new Jugador(42, Arrays.asList(PLAYER_RECORD.split(",", -1)), true, null, null, null, null, null);
        String saved = first.serializar(true);
        Jugador second = new Jugador(42, Arrays.asList(saved.split(",", -1)), true, null, null, null, null, null);
        String savedAgain = second.serializar(true);

        require(saved.equals(savedAgain), "Jugador known-format round-trip changed after second read");
        require(saved.contains(",#,-,-false,"), "Jugador bot/login2 compatibility fields were not preserved");
    }

    private static void playerUnmanagedEntryRoundTrip() throws Exception {
        Path data = configureTempDataPath("sokker-player-persistence-");
        File playerFile = data.resolve("123.properties").toFile();
        String orphan = "12,15,18,false,*";

        Properties initial = new Properties();
        initial.setProperty("42", PLAYER_RECORD);
        initial.setProperty(orphan, "");
        try (FileOutputStream output = new FileOutputStream(playerFile)) {
            initial.store(output, null);
        }

        Usuario usuario = new Usuario(123, "Team");
        List<Jugador> loaded = AsistenteBO.leer_jugadores(123, "Team", false, usuario);
        require(loaded.size() == 1 && Integer.valueOf(42).equals(loaded.get(0).getPid()),
                "Unmanaged player entry was interpreted as a PID");

        AsistenteBO.grabar_jugadores(loaded, 123, 1200, false);

        Properties saved = new Properties();
        try (FileInputStream input = new FileInputStream(playerFile)) {
            saved.load(input);
        }
        require(saved.containsKey(orphan) && "".equals(saved.getProperty(orphan)),
                "Unmanaged player property entry was lost on save");
        require(saved.getProperty("42") != null, "Managed player entry was lost on save");

        List<Jugador> loadedAgain = AsistenteBO.leer_jugadores(123, "Team", false, usuario);
        require(loadedAgain.size() == 1 && Integer.valueOf(42).equals(loadedAgain.get(0).getPid()),
                "Player database could not be re-read after preserving unmanaged entry");
    }

    private static void playerColorChangeRoundTrip() throws Exception {
        Path data = configureTempDataPath("sokker-player-color-");
        File playerFile = data.resolve("123.properties").toFile();
        String orphan = "future.player.key";

        Properties initial = new Properties();
        initial.setProperty("42", PLAYER_RECORD);
        initial.setProperty(orphan, "future-value");
        try (FileOutputStream output = new FileOutputStream(playerFile)) {
            initial.store(output, null);
        }

        Usuario usuario = new Usuario(123, "Team");
        List<Jugador> loaded = AsistenteBO.leer_jugadores(123, "Team", false, usuario);
        require(loaded.size() == 1, "Player color fixture could not be read");
        loaded.get(0).setColor("#112233");
        AsistenteBO.grabar_jugadores(loaded, 123, 1200, false);

        Properties saved = new Properties();
        try (FileInputStream input = new FileInputStream(playerFile)) {
            saved.load(input);
        }
        require("future-value".equals(saved.getProperty(orphan)),
                "Changing player color lost an unmanaged player property");

        List<Jugador> loadedAgain = AsistenteBO.leer_jugadores(123, "Team", false, usuario);
        require(loadedAgain.size() == 1, "Player color database could not be re-read");
        require("#112233".equals(loadedAgain.get(0).getColor()),
                "Player color was not preserved by load-save-reload");
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

        Usuario loaded = UsuarioBO.leer_usuario("alice", false);
        require(loaded != null, "Usuario could not be read");
        UsuarioBO.grabar_usuario(loaded);
        assertUserFutureData(userFile);

        Usuario loadedAgain = UsuarioBO.leer_usuario("alice", false);
        require(loadedAgain != null, "Usuario could not be re-read after save");
        UsuarioBO.grabar_usuario(loadedAgain);
        assertUserFutureData(userFile);
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

    private static void assertUserFutureData(File userFile) throws Exception {
        Properties saved = new Properties();
        try (FileInputStream input = new FileInputStream(userFile)) {
            saved.load(input);
        }
        require("future-value".equals(saved.getProperty("future.key")), "Unknown Usuario.properties key was lost");
        String usuario = saved.getProperty("usuario");
        require(usuario != null && usuario.endsWith(",future-one,future-two,*"), "Future usuario= fields were lost");
        String entrenamiento = saved.getProperty("entrenamiento1200");
        require(entrenamiento != null && entrenamiento.endsWith(",future-training,*"),
                "Future entrenamiento fields were lost");
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
