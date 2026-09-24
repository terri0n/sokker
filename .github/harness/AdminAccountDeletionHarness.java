package com.formulamanager.sokker.acciones.asistente;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class AdminAccountDeletionHarness {
    private AdminAccountDeletionHarness() {}

    public static void main(String[] args) throws Exception {
        require(!EliminarCuentaAdmin.canDelete(false, "alice", "alice", "terrion"), "Normal user can delete accounts");
        require(!EliminarCuentaAdmin.canDelete(true, "alice", null, "terrion"), "Admin without impersonation can delete");
        require(!EliminarCuentaAdmin.canDelete(true, "alice", "bob", "terrion"), "Admin can delete a different target");
        require(EliminarCuentaAdmin.canDelete(true, "alice", "alice", "terrion"), "Exact admin impersonation cannot delete");
        require(!EliminarCuentaAdmin.canDelete(true, "terrion", "terrion", "terrion"), "Configured admin account can be deleted");

        String servlet = read("sokker/src/com/formulamanager/sokker/acciones/asistente/EliminarCuentaAdmin.java");
        String jsp = read("sokker/WebContent/jsp/asistente/eliminar_cuenta_admin.jsp");
        require(servlet.contains("AccountDeletionService.buildManifest"), "Deletion manifest is not rebuilt server-side");
        require(servlet.contains("AccountDeletionService.execute"), "Final deletion service is not invoked");
        require(servlet.contains("confirm_login"), "Exact-login confirmation is missing");
        require(jsp.contains("method=\"post\""), "Final deletion form is not POST-only");
        require(!jsp.contains("filesToDeleteBeforeAccount"), "Browser must not submit the deletion file list");
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
