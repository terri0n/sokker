import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class AccountDeletionUiHarness {
    private AccountDeletionUiHarness() {}

    public static void main(String[] args) throws Exception {
        String loginComo = read("sokker/src/com/formulamanager/sokker/acciones/asistente/LoginComo.java");
        String login = read("sokker/src/com/formulamanager/sokker/acciones/asistente/Login.java");
        String assistant = read("sokker/WebContent/jsp/asistente/asistente.jsp");
        String reset = read("sokker/src/com/formulamanager/sokker/acciones/asistente/Borrar_jugadores.java");
        String bo = read("sokker/src/com/formulamanager/sokker/bo/AsistenteBO.java");
        String request = read("sokker/src/com/formulamanager/sokker/acciones/asistente/SolicitarEliminacionCuenta.java");

        require(loginComo.contains("ADMIN_IMPERSONATED_LOGIN"), "LoginComo has no explicit impersonation marker");
        require(loginComo.contains("setAttribute(ADMIN_IMPERSONATED_LOGIN, usuario.getLogin())"), "LoginComo does not bind marker to loaded user");
        require(login.contains("ADMIN_ORIGINAL_USER"), "Admin original user is not preserved");
        require(request.contains("UsuarioBO.solicitar_borrado"), "Authenticated deletion request is not persisted");
        require(!request.contains("getParameter(\"usuario\")"), "Deletion request accepts arbitrary target user");
        require(bo.contains("obtener_usuarios_con_borrado_solicitado"), "Admin list does not prioritize pending deletions");
        require(bo.contains("Pending account deletion requests") || bo.contains("account.delete.pending_admin"), "Admin list has no pending deletion section");
        require(assistant.contains("solicitar_eliminacion_cuenta"), "Assistant has no in-app deletion request control");
        require(reset.contains("@WebServlet(\"/asistente/borrar_jugadores\")"), "Existing player reset route was changed");
        require(assistant.contains("borrar_jugadores_click"), "Existing player reset UI was changed");
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
