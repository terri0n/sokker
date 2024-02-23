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
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Servlet implementation class ExaminarUsuario
 */
@WebServlet("/asistente/examinar_usuario")
public class ExaminarUsuario extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ExaminarUsuario() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (admin(request)) {
			Usuario usuario = UsuarioBO.leer_usuario(request.getParameter("usuario"), true);
		
			if (usuario != null) {
_log(request, usuario.getLogin());
				List<Jugador> jugadores = AsistenteBO.leer_jugadores(usuario.getTid(), usuario.getEquipo(), false, usuario);
				Collections.sort(jugadores, Jugador.getComparator());
				request.setAttribute("usuario", usuario);
				request.setAttribute("jugadores", jugadores);
			} else {
				throw new RuntimeException("Usuario incorrecto: " + request.getParameter("usuario"));
			}
			request.getRequestDispatcher("/jsp/asistente/examinar_usuario.jsp").forward(request, response);
		} else {
			response.sendRedirect(request.getContextPath() + "/asistente");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}
