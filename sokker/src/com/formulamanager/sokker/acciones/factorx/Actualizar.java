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
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION_ASISTENTE;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Actualiza las habilidades originales de un jugador
 */
@WebServlet("/factorx/actualizar")
public class Actualizar extends SERVLET_FACTORX {
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
		TIPO_FACTORX tipo = getTipo(request);
		
		if (admin(request)) {
			try {
				new Navegador(true, request) {
					@Override
					protected void execute(WebClient navegador)	throws FailingHttpStatusCodeException, MalformedURLException, IOException {
						int edicion_actual = obtener_edicion(navegador);
						int jornada_actual = obtener_jornada(navegador);

						List<Jugador> jugadores = JugadorBO.leer_jugadores(tipo, edicion_actual, true);
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
						FactorxBO.grabar_jugadores(jugadores, tipo, jornada_actual, false);
					}
				};
			} catch (FailingHttpStatusCodeException | LoginException | ParseException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		response.sendRedirect(request.getContextPath() + "/factorx?mensaje=Actualizado&tipo=" + tipo.name());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doPost(request, response);
	}
}
