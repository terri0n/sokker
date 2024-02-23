package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Servlet implementation class ServletSokker
 */
@WebServlet("/asistente/idioma")
public class Idioma extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Idioma() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String lang = Util.getString(request, "lang");
		if (lang != null) {
			Config.set(request.getSession(), Config.FMT_LOCALE, new Locale(lang));
	
			if (login(request)) {
				Usuario usuario = (Usuario)request.getSession().getAttribute("usuario");
				usuario.setLocale(lang);
				UsuarioBO.grabar_usuario(usuario);
			}
		}

		response.sendRedirect(request.getContextPath() + "/asistente?juveniles=" + Util.nvl(request.getParameter("juveniles")) + "&historico=" + Util.nvl(request.getParameter("historico")));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}
