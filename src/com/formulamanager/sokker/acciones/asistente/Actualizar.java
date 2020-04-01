package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Actualiza las habilidades originales de un jugador
 */
@WebServlet("/asistente/actualizar")
public class Actualizar extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Actualizar() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String mensaje = "";
		
		if (login(request)) {
			Usuario usuario = (Usuario)request.getSession().getAttribute("usuario");
			String ilogin = usuario.getDef_tid() < 1000 ? AsistenteBO.getLogin() : request.getParameter("ilogin");
			String ipassword = usuario.getDef_tid() < 1000 ? AsistenteBO.getPassword() : request.getParameter("ipassword");
			if (usuario.getDef_tid() > 1000) {
				usuario.setNtdb(Util.getBoolean(request, "ntdb"));
			}

			List<Jugador> jugadores_actualizados = AsistenteBO.actualizar_equipo(ilogin, ipassword, usuario);
			request.getSession().setAttribute("jugadores", jugadores_actualizados);
			
			mensaje = "updated";
		}

		response.sendRedirect(request.getContextPath() + "/asistente?mensaje=" + mensaje);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
