package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;

import com.formulamanager.sokker.auxiliares.LoginExceptionExt;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Usuario;

@WebServlet("/asistente/login")
public class Login extends SERVLET_ASISTENTE {
    private static final long serialVersionUID = 1L;
    public static final String ADMIN_ORIGINAL_USER = "admin_original_user";

    static Cookie expiredLegacyPasswordCookie(String contextPath) {
        Cookie cookie = new Cookie("apassword", "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath((contextPath == null ? "" : contextPath) + "/asistente");
        return cookie;
    }

    public Login() {
        super();
    }

    protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, LoginExceptionExt {
        String alogin = request.getParameter("alogin");
        String apassword = request.getParameter("apassword");
        String error = "";

        response.addCookie(expiredLegacyPasswordCookie(request.getContextPath()));

        String ip = getIP(request);
        HashMap<String, String> ips = Util.leer_hashmap("IPs");
        if (ips.containsKey(ip) && Integer.valueOf(ips.get(ip)) >= 8) {
            _log(request, "Acceso denegado: " + ip);
            _log_linea("_BLOQUEO", "Acceso denegado: " + ip + " " + alogin);
            error = "?mensaje=user_disabled";
        } else {
            Usuario usuario = UsuarioBO.leer_usuario(alogin, true);

            if (usuario != null && usuario.getPassword().equals(Util.getMD5(apassword))) {
                response.addCookie(new Cookie("alogin", URLEncoder.encode(alogin, "UTF-8")));
                request.getSession().setAttribute("usuario", usuario);

                boolean configuredAdmin = alogin.equalsIgnoreCase(SystemUtil.getVar(SystemUtil.LOGIN));
                if (configuredAdmin) {
                    setAdmin(true, request);
                    request.getSession().setAttribute(ADMIN_ORIGINAL_USER, usuario);
                    request.getSession().removeAttribute(LoginComo.ADMIN_IMPERSONATED_LOGIN);
                } else {
                    request.getSession().removeAttribute("admin");
                    request.getSession().removeAttribute(ADMIN_ORIGINAL_USER);
                    request.getSession().removeAttribute(LoginComo.ADMIN_IMPERSONATED_LOGIN);
                    request.getSession().removeAttribute(EliminarCuentaAdmin.PENDING_ADMIN_DELETE_TARGET);
                }

                _log(request, "");

                if (usuario.getLocale() != null) {
                    Config.set(request.getSession(), Config.FMT_LOCALE, new Locale(usuario.getLocale()));
                }

                if (configuredAdmin) {
                    Object pending = request.getSession().getAttribute(EliminarCuentaAdmin.PENDING_ADMIN_DELETE_TARGET);
                    if (pending instanceof String) {
                        request.getSession().removeAttribute(EliminarCuentaAdmin.PENDING_ADMIN_DELETE_TARGET);
                        response.sendRedirect(request.getContextPath() + "/asistente/login_como?usuario="
                                + URLEncoder.encode((String) pending, "UTF-8") + "&destino=eliminar_cuenta_admin");
                        return;
                    }
                }
            } else {
                if (usuario != null) {
                    _log(request, "Contraseña errónea");
                    throw new LoginExceptionExt(Util.getTexto(request.getLocale().getLanguage(), "messages.login_error"), alogin);
                }
                error = "?mensaje=login_error&error=1";
            }
        }

        response.sendRedirect(request.getContextPath() + "/asistente" + error);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Login is POST-only; keep historical no-op GET behavior.
    }
}
