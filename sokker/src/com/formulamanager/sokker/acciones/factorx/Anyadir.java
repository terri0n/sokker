	package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION_ASISTENTE;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Servlet implementation class Main
 */
@WebServlet("/factorx/anyadir")
public class Anyadir extends SERVLET_FACTORX {
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
		SERVLET_FACTORX.TIPO_FACTORX tipo = getTipo(request);
		boolean forzar = "on".equals(request.getParameter("forzar")) && admin(request);	// Solo el admin puede "forzar"
		
		Integer pid = Util.getInteger(request, "pid");
		try {
			if (pid == null) {
				throw new RuntimeException("Jugador no encontrado");
			}
				
			DEMARCACION_ASISTENTE demarcacion = DEMARCACION_ASISTENTE.valueOf(request.getParameter("demarcacion"));
	
			int[] jornada_actual = new int[1];
			int[] edicion_actual = new int[1];

			new Navegador(true, request) {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
					jornada_actual[0] = obtener_jornada(navegador);
					edicion_actual[0] = obtener_edicion(navegador);
				}
			};
			
			synchronized (Anyadir.class) {
				List<Jugador> jugadores = JugadorBO.leer_jugadores(tipo, edicion_actual[0], true);
				
				// Buscamos al jugador entre los que tenemos
				for (Jugador j : jugadores) {
					if (j.getPid().equals(pid)) {
						throw new RuntimeException("Jugador repetido");
					}
				}
				
				// Lo añadimos
				new Navegador(request) {
					@Override
					protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
						Jugador nuevo = FactorxBO.buscar(navegador, pid, forzar, jornada_actual[0]);
						if (nuevo != null) {
							if (tipo != TIPO_FACTORX.internacional && nuevo.getPais() != 17) {
								throw new RuntimeException("El jugador no es espa�ol");
							} else {
								nuevo.setDemarcacion(demarcacion);
								
								if (!forzar && !nuevo.validar(tipo)) {
									throw new RuntimeException("El jugador no cumple los requisitos mínimos para " + demarcacion);
								}
								
								jugadores.add(nuevo);
								Collections.sort(jugadores);
								FactorxBO.grabar_jugadores(jugadores, tipo, jornada_actual[0], false);
								
								JugadorBO.escribir_anuncio(navegador, nuevo, true);
							}
						} else {
							throw new RuntimeException("Jugador no encontrado");
						}
					}
				};
			}
			
			response.sendRedirect(request.getContextPath() + "/factorx?mensaje=A�adido&tipo=" + tipo.name());
		} catch (Exception e) {
			e.printStackTrace();
			String error = e instanceof RuntimeException ? e.getMessage() : e.toString();
			response.sendRedirect(request.getContextPath() + "/factorx?error=Error: " + error + "&tipo=" + tipo.name());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doPost(request, response);
	}
}
