package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION_ASISTENTE;

/**
 * Actualiza las habilidades originales de un jugador
 */
@WebServlet("/factorx/actualizar")
public class Actualizar extends SERVLET {
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
		boolean senior = "1".equals(request.getParameter("senior"));
		
		if (login(request)) {
			List<Jugador> jugadores = JugadorBO.leer_jugadores(senior, null);
			int pid = Integer.valueOf(request.getParameter("pid"));
			int jornada = Integer.valueOf(request.getParameter("jornada"));

			for (Jugador j : jugadores) {
				if (pid == j.getPid()) {
					j.setDemarcacion(DEMARCACION_ASISTENTE.valueOf(request.getParameter("demarcacion")));
					j.getOriginal().setJornada(jornada);
					j.getOriginal().setCondicion(Integer.valueOf(request.getParameter("condicion")));
					j.getOriginal().setRapidez(Integer.valueOf(request.getParameter("rapidez")));
					j.getOriginal().setTecnica(Integer.valueOf(request.getParameter("tecnica")));
					j.getOriginal().setPases(Integer.valueOf(request.getParameter("pases")));
					j.getOriginal().setPorteria(Integer.valueOf(request.getParameter("porteria")));
					j.getOriginal().setDefensa(Integer.valueOf(request.getParameter("defensa")));
					j.getOriginal().setCreacion(Integer.valueOf(request.getParameter("creacion")));
					j.getOriginal().setAnotacion(Integer.valueOf(request.getParameter("anotacion")));
					break;
				}
			}
			
			Collections.sort(jugadores);
			JugadorBO.grabar_jugadores(jugadores, senior, false);
		}

		response.sendRedirect(request.getContextPath() + "/factorx?mensaje=Actualizado&senior=" + (senior ? "1" : ""));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doPost(request, response);
	}
}
