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

/**
 * Servlet implementation class ServletSokker
 */
@WebServlet("/asistente/login")
public class Login extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
    }

	/**
	 * @throws LoginExceptionExt 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, LoginExceptionExt {
		String alogin = request.getParameter("alogin");
		String apassword = request.getParameter("apassword");
		String error = "";

		//--------------
		// COMPROBAR IP
		//--------------
		String ip = getIP(request);
		HashMap<String, String> ips = Util.leer_hashmap("IPs");
		if (ips.containsKey(ip) && Integer.valueOf(ips.get(ip)) >= 8) {
			// IP baneada
			_log(request, "Acceso denegado: " + ip);
			_log_linea("_BLOQUEO", "Acceso denegado: " + ip + " " + alogin);
			error = "?mensaje=user_disabled";
		} else {
			//-----------------
			// COMPROBAR LOGIN
			//-----------------
			Usuario usuario = UsuarioBO.leer_usuario(alogin, true);
	
			if (usuario != null && (usuario.getPassword().equals(Util.getMD5(apassword)))) {
				// Recordar
				boolean recordar = Util.nnvl(request.getParameter("recordar")) != null;
				response.addCookie(new Cookie("alogin", URLEncoder.encode(alogin, "UTF-8")));
				response.addCookie(new Cookie("apassword", recordar ? apassword : null));
				
				// Admin?
				request.getSession().setAttribute("usuario", usuario);
				if (alogin.equalsIgnoreCase(SystemUtil.getVar(SystemUtil.LOGIN))) {
					setAdmin(true, request);
				}
					
_log(request, "");
	
				// Idioma
				if (usuario.getLocale() != null) {
					Config.set(request.getSession(), Config.FMT_LOCALE, new Locale(usuario.getLocale()));
				}
			} else {
				if (usuario != null) {
_log(request, "Contraseña errónea");
					throw new LoginExceptionExt(Util.getTexto(request.getLocale().getLanguage(), "messages.login_error"), alogin, apassword);
				}
				error = "?mensaje=login_error&error=1";
			}
		}

		response.sendRedirect(request.getContextPath() + "/asistente" + error);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doPost(request, response);
	}

}
