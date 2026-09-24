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

    private static String methodBlock(String source, String signature, String nextMarker) {
        int start = source.indexOf(signature);
        if (start < 0) {
            throw new AssertionError("Method not found: " + signature);
        }
        int end = source.indexOf(nextMarker, start + signature.length());
        if (end < 0) {
            throw new AssertionError("End marker not found after: " + signature);
        }
        return source.substring(start, end);
    }

    public static void main(String[] args) throws Exception {
        String ntdbBO = read("sokker/src/com/formulamanager/sokker/bo/NtdbBO.java");
        String ntdbJsp = read("sokker/WebContent/jsp/asistente/ntdb.jsp");
        String ntdbSend = read("sokker/src/com/formulamanager/sokker/acciones/asistente/NTDB_send.java");
        String configJsp = read("sokker/WebContent/jsp/asistente/config.jsp");
        String actualizarConfiguracion = read("sokker/src/com/formulamanager/sokker/acciones/asistente/ActualizarConfiguracion.java");
        String servlet = read("sokker/src/com/formulamanager/sokker/servlets/Servlet.java");
        String usuario = read("sokker/src/com/formulamanager/sokker/entity/Usuario.java");

        String legacyAutomaticSender = methodBlock(ntdbBO, "public static void enviar_jugadores(", "/**");
        assertNotContains(legacyAutomaticSender, "leer_hashmap", "Legacy automatic NTDB sender must not load destinations");
        assertNotContains(legacyAutomaticSender, "actualizar_jugador_remoto", "Legacy automatic NTDB sender must not perform remote exports");
        assertNotContains(legacyAutomaticSender, "actualizar_jugador_local", "Legacy automatic NTDB sender must not update NT databases");
        assertNotContains(legacyAutomaticSender, "new URL", "Legacy automatic NTDB sender must not open destinations");

        assertNotContains(ntdbJsp, "/asistente/ntdb/send", "Manual NTDB receive/send form must be removed");
        assertContains(ntdbSend, "HttpServletResponse.SC_GONE", "Legacy NTDB receive endpoint must explicitly reject requests");
        assertNotContains(ntdbSend, "actualizar_jugador_local", "Legacy NTDB receive endpoint must not write player data");
        assertNotContains(ntdbSend, "isRecibir_ntdb", "Legacy NTDB receive endpoint must not depend on the retired receive flag");
        assertNotContains(ntdbSend, "new Navegador", "Legacy NTDB receive endpoint must not connect to Sokker");
        assertNotContains(configJsp, "name=\"ntdb\"", "NTDB send option must be removed from configuration");
        assertNotContains(configJsp, "name=\"recibir_ntdb\"", "NTDB receive option must be removed from configuration");
        assertNotContains(actualizarConfiguracion, "getBoolean(request, \"ntdb\")", "NTDB send option must not be processed");
        assertNotContains(actualizarConfiguracion, "getBoolean(request, \"recibir_ntdb\")", "NTDB receive option must not be processed");
        assertNotContains(actualizarConfiguracion, "setNtdb(", "NTDB send flag must remain untouched in persisted users");
        assertNotContains(actualizarConfiguracion, "setRecibir_ntdb(", "NTDB receive flag must remain untouched in persisted users");

        assertContains(usuario, "private boolean ntdb;", "Legacy NTDB send value must remain readable/writable for compatibility");
        assertContains(usuario, "private boolean recibir_ntdb;", "Legacy NTDB receive value must remain readable/writable for compatibility");
        assertContains(usuario, "valores.add(ntdb + \"\");", "Legacy NTDB send value must remain serialized");
        assertContains(usuario, "valores.add(new Boolean(recibir_ntdb).toString());", "Legacy NTDB receive value must remain serialized");

        assertContains(servlet, "public String exportar_url(", "Generic export-to-URL must remain available");
        assertContains(servlet, "NtdbBO.actualizar_jugador_remoto(", "Generic export-to-URL must keep its current implementation");

        System.out.println("NtdbSendingDisabledHarness OK");
    }
}
