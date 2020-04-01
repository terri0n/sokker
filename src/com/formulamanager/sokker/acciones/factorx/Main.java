package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.bo.FactorxBO.FORMACIONES;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;

/**
 * Servlet implementation class Main
 */
@WebServlet("/factorx")
public class Main extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Main() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean senior = "1".equals(request.getParameter("senior"));
		Integer edicion = Util.getInteger(request, "edicion");
		if (edicion == null) {
			edicion = JugadorBO.obtener_edicion();
		}
		
		List<Jugador> jugadores = JugadorBO.leer_jugadores(senior, edicion);
		Collections.sort(jugadores);

		FORMACIONES formacion = FactorxBO.leer_formacion(senior);
		JugadorBO.poner_estrellas(jugadores, formacion, senior);

		request.setAttribute("jugadores", jugadores);
		request.setAttribute("jornada", JugadorBO.obtener_jornada() % 16);
		request.setAttribute("edicion", edicion);
		request.setAttribute("formacion", formacion);
		
		request.getRequestDispatcher("/jsp/factorx/factorx.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
	}
}
