import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NtdbSendingDisabledHarness {
    private static String read(String path) throws Exception {
        Path p = Paths.get(path);
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    private static void assertNotContains(String text, String needle, String message) {
        if (text.contains(needle)) {
            throw new AssertionError(message + ": found [" + needle + "]");
        }
    }

    private static void assertContains(String text, String needle, String message) {
        if (!text.contains(needle)) {
            throw new AssertionError(message + ": missing [" + needle + "]");
        }
    }

    public static void main(String[] args) throws Exception {
        String asistenteBO = read("sokker/src/com/formulamanager/sokker/bo/AsistenteBO.java");
        String ntdbJsp = read("sokker/WebContent/jsp/asistente/ntdb.jsp");
        String configJsp = read("sokker/WebContent/jsp/asistente/config.jsp");
        String actualizarConfiguracion = read("sokker/src/com/formulamanager/sokker/acciones/asistente/ActualizarConfiguracion.java");
        String servlet = read("sokker/src/com/formulamanager/sokker/servlets/Servlet.java");

        assertNotContains(asistenteBO, "NtdbBO.enviar_jugadores(", "Automatic NTDB sending must be disabled");
        assertNotContains(ntdbJsp, "/asistente/ntdb/send", "Manual NTDB send form must be removed");
        assertNotContains(configJsp, "name=\"ntdb\"", "NTDB send option must be removed from configuration");
        assertNotContains(configJsp, "name=\"recibir_ntdb\"", "NTDB receive option must be removed from configuration");
        assertNotContains(actualizarConfiguracion, "getBoolean(request, \"ntdb\")", "NTDB send option must not be processed");
        assertNotContains(actualizarConfiguracion, "getBoolean(request, \"recibir_ntdb\")", "NTDB receive option must not be processed");
        assertNotContains(actualizarConfiguracion, "setNtdb(", "NTDB send flag must remain untouched in persisted users");
        assertNotContains(actualizarConfiguracion, "setRecibir_ntdb(", "NTDB receive flag must remain untouched in persisted users");

        assertContains(servlet, "public String exportar_url(", "Generic export-to-URL must remain available");
        assertContains(servlet, "NtdbBO.actualizar_jugador_remoto(", "Generic export-to-URL must keep its current implementation");

        System.out.println("NtdbSendingDisabledHarness OK");
    }
}
