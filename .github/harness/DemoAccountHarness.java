import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DemoAccountHarness {
    public static void main(String[] args) throws Exception {
        String jsp = read("sokker/WebContent/jsp/asistente/asistente.jsp");
        String servlet = read("sokker/src/com/formulamanager/sokker/acciones/asistente/CambiarEquipo.java");

        require(jsp.contains("sessionScope.usuario.login ne 'demo'"),
                "Demo account must not expose the team-switch link");
        require(servlet.contains("\"demo\".equalsIgnoreCase(usuario.getLogin())"),
                "Demo account must be protected server-side from team switching");

        System.out.println("Demo account harness OK");
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
