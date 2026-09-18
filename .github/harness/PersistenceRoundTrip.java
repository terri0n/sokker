import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

public final class PersistenceRoundTrip {
    private PersistenceRoundTrip() {}

    public static void main(String[] args) throws Exception {
        playerKnownFormatRoundTrip();
        userUnknownFieldsRoundTrip();
    }

    private static void playerKnownFormatRoundTrip() {
        String record = "Player,123,DEF,1,18/09/2026 12:00,true,0,,,,note,-1000,180,75,23,-1.5,false,#,-,-false,1200,25,100000,10,11,12,13,14,15,16,17,-0,10,MID,90.0,-5,6,7,true,*";

        Jugador first = new Jugador(42, Arrays.asList(record.split(",", -1)), true, null, null, null, null, null);
        String saved = first.serializar(true);
        Jugador second = new Jugador(42, Arrays.asList(saved.split(",", -1)), true, null, null, null, null, null);
        String savedAgain = second.serializar(true);

        require(saved.equals(savedAgain), "Jugador known-format round-trip changed after second read");
        require(saved.contains(",#,-,-false,"), "Jugador bot/login2 compatibility fields were not preserved");
    }

    private static void userUnknownFieldsRoundTrip() throws Exception {
        Path tomcat = Files.createTempDirectory("sokker-persistence-");
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

    private static void assertUserFutureData(File userFile) throws Exception {
        Properties saved = new Properties();
        try (FileInputStream input = new FileInputStream(userFile)) {
            saved.load(input);
        }
        require("future-value".equals(saved.getProperty("future.key")), "Unknown Usuario.properties key was lost");
        String usuario = saved.getProperty("usuario");
        require(usuario != null && usuario.endsWith(",future-one,future-two,*"), "Future usuario= fields were lost");
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
