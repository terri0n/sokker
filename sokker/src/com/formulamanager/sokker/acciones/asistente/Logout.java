package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;

/**
 * Servlet implementation class ServletSokker
 */
@WebServlet("/asistente/logout")
public class Logout extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Logout() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		List<Jugador> jugadores = (List<Jugador>) request.getSession().getAttribute("jugadores");
		
//		for (Jugador j : jugadores) {
//			Jugador j2 = j.buscar_jornada(927);
//			j2.setEdad(j2.getEdad() + 1);
//		}
//		
//		AsistenteBO.grabar_jugadores(jugadores, jugadores.get(0).getTid(), 0, false);

_log(request, "");
		
		request.getSession().invalidate();
		
		response.sendRedirect(request.getContextPath() + "/asistente");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}
