package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Usuario;

@WebServlet("/asistente/login_como")
public class LoginComo extends SERVLET_ASISTENTE {
    private static final long serialVersionUID = 1L;
    public static final String ADMIN_IMPERSONATED_LOGIN = "admin_impersonated_login";

    public LoginComo() {
        super();
    }

    protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String error = "";

        if (admin(request)) {
            boolean reset_intentos = Util.getBoolean(request, "reset_intentos");
            Usuario usuario = UsuarioBO.leer_usuario(request.getParameter("usuario"), true);

            if (usuario != null) {
                _log(request, usuario.getLogin());
                request.getSession().setAttribute("usuario", usuario);
                request.getSession().setAttribute(ADMIN_IMPERSONATED_LOGIN, usuario.getLogin());

                if (reset_intentos && usuario.getIntentos_fallidos() > 0) {
                    usuario.setIntentos_fallidos(0);
                    UsuarioBO.grabar_usuario(usuario);
                }
            } else {
                request.getSession().removeAttribute(ADMIN_IMPERSONATED_LOGIN);
                error = "?mensaje=" + URLEncoder.encode("Usuario incorrecto", "UTF-8");
            }
        }

        response.sendRedirect(request.getContextPath() + "/asistente" + error);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
