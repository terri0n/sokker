package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;

/**
 * Servlet implementation class Main
 */
@WebServlet("/factorx/borrar")
public class Borrar extends SERVLET {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Borrar() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean senior = "1".equals(request.getParameter("senior"));

		if (login(request)) {
			Integer pid = Integer.valueOf(request.getParameter("pid"));
			List<Jugador> jugadores = JugadorBO.leer_jugadores(senior, null);
			
			Iterator<Jugador> it = jugadores.iterator();
			while (it.hasNext()) {
				Jugador j = it.next();
				if (j.getPid().equals(pid)) {
					it.remove();
				}
			}
			
			Collections.sort(jugadores);
			JugadorBO.grabar_jugadores(jugadores, senior, false);
		}

		response.sendRedirect(request.getContextPath() + "/factorx?mensaje=Borrado&senior=" + (senior ? "1" : ""));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
	}
}
