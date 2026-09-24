package com.formulamanager.sokker.acciones.asistente;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.bo.AccountDeletionService;
import com.formulamanager.sokker.bo.AccountDeletionService.DeletionManifest;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Usuario;

@WebServlet("/asistente/eliminar_cuenta_admin")
public class EliminarCuentaAdmin extends SERVLET_ASISTENTE {
    private static final long serialVersionUID = 1L;
    public static final String PENDING_ADMIN_DELETE_TARGET = "pending_admin_delete_target";

    static boolean canDelete(boolean admin, String currentLogin, String impersonatedLogin, String configuredAdminLogin) {
        return admin
                && currentLogin != null
                && impersonatedLogin != null
                && currentLogin.equalsIgnoreCase(impersonatedLogin)
                && (configuredAdminLogin == null || !currentLogin.equalsIgnoreCase(configuredAdminLogin));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String target = normalizeLogin(request.getParameter("usuario"));
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        HttpSession session = request.getSession();
        if (!admin(request)) {
            session.setAttribute(PENDING_ADMIN_DELETE_TARGET, target);
            response.sendRedirect(request.getContextPath() + "/asistente");
            return;
        }

        Usuario current = getUsuario(request);
        String impersonated = (String) session.getAttribute(LoginComo.ADMIN_IMPERSONATED_LOGIN);
        if (current == null || impersonated == null || !target.equalsIgnoreCase(current.getLogin()) || !target.equalsIgnoreCase(impersonated)) {
            response.sendRedirect(request.getContextPath() + "/asistente/login_como?usuario="
                    + URLEncoder.encode(target, "UTF-8") + "&destino=eliminar_cuenta_admin");
            return;
        }

        if (!canDelete(true, current.getLogin(), impersonated, SystemUtil.getVar(SystemUtil.LOGIN))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        DeletionManifest manifest = AccountDeletionService.buildManifest(current.getLogin());
        request.setAttribute("deletionManifest", manifest);
        request.setAttribute("deletionFiles", relativePaths(manifest));
        request.getRequestDispatcher("/jsp/asistente/eliminar_cuenta_admin.jsp").forward(request, response);
    }

    @Override
    protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Usuario current = getUsuario(request);
        String impersonated = (String) session.getAttribute(LoginComo.ADMIN_IMPERSONATED_LOGIN);
        String adminLogin = SystemUtil.getVar(SystemUtil.LOGIN);

        if (!canDelete(admin(request), current == null ? null : current.getLogin(), impersonated, adminLogin)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String confirmation = request.getParameter("confirm_login");
        if (confirmation == null || !confirmation.equals(current.getLogin())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        DeletionManifest manifest = AccountDeletionService.buildManifest(current.getLogin());
        AccountDeletionService.execute(manifest);

        Usuario originalAdmin = (Usuario) session.getAttribute(Login.ADMIN_ORIGINAL_USER);
        if (originalAdmin == null && adminLogin != null) {
            originalAdmin = UsuarioBO.leer_usuario(adminLogin, true);
        }
        session.setAttribute("usuario", originalAdmin);
        session.removeAttribute(LoginComo.ADMIN_IMPERSONATED_LOGIN);
        session.removeAttribute(PENDING_ADMIN_DELETE_TARGET);
        response.sendRedirect(request.getContextPath() + "/asistente?mensaje=account_deleted");
    }

    private static String normalizeLogin(String value) {
        if (value == null) return null;
        String login = value.trim().toLowerCase();
        if (login.isEmpty() || login.length() > 100) return null;
        for (int i = 0; i < login.length(); i++) {
            char c = login.charAt(i);
            if (c == '/' || c == '\\' || c == ',' || Character.isISOControl(c)) return null;
        }
        return login;
    }

    private static List<String> relativePaths(DeletionManifest manifest) throws IOException {
        List<String> result = new ArrayList<String>();
        File base = new File(SystemUtil.getVar(SystemUtil.PATH)).getCanonicalFile();
        String prefix = base.getPath() + File.separator;
        for (File file : manifest.getFilesToDeleteBeforeAccount()) {
            String path = file.getCanonicalPath();
            result.add(path.startsWith(prefix) ? path.substring(prefix.length()) : file.getName());
        }
        if (manifest.getAccountFile() != null) {
            String path = manifest.getAccountFile().getCanonicalPath();
            result.add(path.startsWith(prefix) ? path.substring(prefix.length()) : manifest.getAccountFile().getName());
        }
        return result;
    }
}
