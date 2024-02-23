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
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Históricos
 */
@WebServlet("/factorx/historico")
public class Historico extends SERVLET_FACTORX {
	private static final long serialVersionUID = 1L;

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
		SERVLET_FACTORX.TIPO_FACTORX tipo = getTipo(request);
		TIPO_HISTORICO tipo_hist = getTipo_hist(request);

		try {
			new Navegador(true, request) {
				@Override
				protected void execute(WebClient navegador)	throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException {
					int edicion_actual = obtener_edicion(navegador);
					List<Jugador> jugadores = JugadorBO.leer_historico(tipo, edicion_actual);
					
					Collections.sort(jugadores, tipo_hist.getComparator());
					
					if (jugadores.size() > 50) {
						jugadores.subList(50, jugadores.size()).clear();
					}
					
					request.setAttribute("jugadores", jugadores);
					request.setAttribute("historico", "Jugadores con más " + tipo_hist.name());
				}
			};
		} catch (FailingHttpStatusCodeException | LoginException | ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		request.getRequestDispatcher("/jsp/factorx/factorx.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
