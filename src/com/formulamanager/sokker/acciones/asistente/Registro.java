package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Actualiza las habilidades originales de un jugador
 */
@WebServlet("/asistente/registro")
public class Registro extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Registro() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String mensaje = "";
		
		String login = request.getParameter("login").replace("\\", "").replace("/", "").trim().toLowerCase();
		String password = request.getParameter("password");
		String ilogin = request.getParameter("ilogin");
		String ipassword = request.getParameter("ipassword");
		
		if (login.length() == 0 || UsuarioBO.leer_usuario(login) != null) {
			throw new RuntimeException(Util.getTexto(request.getLocale().getLanguage(), "messages.user_already_exists"));
		}
		
		new Navegador(false, ilogin, ipassword) {
			@Override
			protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
				usuario.setLogin(login);
				usuario.setPassword(Util.getMD5(password));
				usuario.setLogin_sokker(ilogin);
				usuario.setJornada(JugadorBO.obtener_jornada());
				usuario.setJornada_nt(JugadorBO.obtener_jornada() - 1);
				usuario.setIntentos_fallidos(0);
				usuario.setLocale(request.getLocale().getLanguage());
				
				List<Jugador> jugadores_actualizados = AsistenteBO.actualizar_equipo(ilogin, ipassword, usuario);
				request.getSession().setAttribute("usuario", usuario);
				request.getSession().setAttribute("jugadores", jugadores_actualizados);
			}
		};
		
		mensaje = "registered";

		response.sendRedirect(request.getContextPath() + "/asistente?mensaje=" + mensaje);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
