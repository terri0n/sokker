package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.text.ParseException;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

/**
 * Borra un usuario
 */
@WebServlet("/asistente/borrar_usuario")
public class Borrar_usuario extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Borrar_usuario() {
        super();
    }

	/**
	 * @throws ParseException 
	 * @throws FailingHttpStatusCodeException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, LoginException, FailingHttpStatusCodeException, ParseException {
		String mensaje = "";
		
		if (admin(request)) {
			UsuarioBO.borrar_usuario(Util.getString(request, "usuario"));
			mensaje = "?mensaje=Usuario borrado correctamente";
		}

		response.sendRedirect(request.getContextPath() + "/asistente" + mensaje);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
