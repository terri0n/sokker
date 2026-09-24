package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Usuario;

@WebServlet("/asistente/solicitar_eliminacion_cuenta")
public class SolicitarEliminacionCuenta extends SERVLET_ASISTENTE {
    private static final long serialVersionUID = 1L;

    @Override
    protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!login(request)) {
            response.sendRedirect(request.getContextPath() + "/asistente");
            return;
        }

        Usuario usuario = getUsuario(request);
        if (UsuarioBO.obtener_fecha_solicitud_borrado(usuario.getLogin()) == null) {
            UsuarioBO.solicitar_borrado(usuario.getLogin(), System.currentTimeMillis());
        }
        response.sendRedirect(request.getContextPath() + "/asistente?mensaje=account_deletion_requested");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
