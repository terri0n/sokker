package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;

/**
 * Históricos
 */
@WebServlet("/factorx/historico")
public class Historico extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public enum TIPO_HISTORICO { puntos, subidas, valor;
		public Comparator<Jugador> getComparator() {
			switch (this) {
				case puntos: return Jugador.comparator_puntos();
				case subidas: return Jugador.comparator_subidas();
				case valor: return Jugador.comparator_valor();
				default: return null;
			}
		}
	}
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Historico() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean senior = "1".equals(request.getParameter("senior"));
		TIPO_HISTORICO tipo = TIPO_HISTORICO.valueOf(Util.getString(request, "tipo"));

		List<Jugador> jugadores = JugadorBO.leer_historico(senior);

		Collections.sort(jugadores, tipo.getComparator());
		
		if (jugadores.size() > 50) {
			jugadores.subList(50, jugadores.size()).clear();
		}

		request.setAttribute("jugadores", jugadores);
		request.setAttribute("historico", "Jugadores con más " + tipo.name());
		
		request.getRequestDispatcher("/jsp/factorx/factorx.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
