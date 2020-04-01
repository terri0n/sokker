	package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION_ASISTENTE;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Servlet implementation class Main
 */
@WebServlet("/factorx/anyadir")
public class Anyadir extends SERVLET {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Anyadir() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean senior = "1".equals(request.getParameter("senior"));
		boolean forzar = "on".equals(request.getParameter("forzar")) && login(request);	// Solo el admin puede "forzar"
		
		Integer pid = Util.getInteger(request, "pid");
		if (pid == null) {
			throw new RuntimeException("Jugador no encontrado");
		}
			
		DEMARCACION_ASISTENTE demarcacion = DEMARCACION_ASISTENTE.valueOf(request.getParameter("demarcacion"));
		
		synchronized (Anyadir.class) {
			List<Jugador> jugadores = JugadorBO.leer_jugadores(senior, null);
				
			// Buscamos al jugador entre los que tenemos
			for (Jugador j : jugadores) {
				if (j.getPid().equals(pid)) {
					throw new RuntimeException("Jugador repetido");
				}
			}
	
			// Lo añadimos
			new Navegador() {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
					Jugador nuevo = JugadorBO.buscar(navegador, pid, forzar);
					if (nuevo != null) {
						if (nuevo.getPais() != 17) {
							throw new RuntimeException("El jugador no es español");
						} else {
							nuevo.setDemarcacion(demarcacion);
		
							if (!forzar && !nuevo.validar(senior)) {
								throw new RuntimeException("El jugador no cumple los requisitos mínimos");
							}
		
							jugadores.add(nuevo);
							Collections.sort(jugadores);
							JugadorBO.grabar_jugadores(jugadores, senior, false);
		
							JugadorBO.escribir_anuncio(navegador, nuevo, true);
						}
					} else {
						throw new RuntimeException("Jugador no encontrado");
					}
				}
			};
		}

		response.sendRedirect(request.getContextPath() + "/factorx?mensaje=Añadido&senior=" + (senior ? "1" : ""));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doPost(request, response);
	}
}
