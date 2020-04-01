package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Servlet implementation class LoginComo
 */
@WebServlet("/asistente/login_como")
public class LoginComo extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginComo() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String error = "";
		
		if (admin(request)) {
			boolean reset_intentos = Util.getBoolean(request, "reset_intentos");
			Usuario usuario = UsuarioBO.leer_usuario(request.getParameter("usuario"));
		
			if (usuario != null) {
				request.getSession().setAttribute("usuario", usuario);	

				List<Jugador> jugadores = AsistenteBO.leer_jugadores(usuario.getDef_tid(), false);
				Collections.sort(jugadores, Jugador.getComparator());
				request.getSession().setAttribute("jugadores", jugadores);

				AsistenteBO.calcular(jugadores, usuario);
			
				if (reset_intentos && usuario.getIntentos_fallidos() > 0) {
					usuario.setIntentos_fallidos(0);
					UsuarioBO.grabar_usuario(usuario);
				}
			} else {
				error = "?mensaje=" + URLEncoder.encode("Usuario incorrecto", "UTF-8");
			}
		}
		
		response.sendRedirect(request.getContextPath() + "/asistente" + error);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}
