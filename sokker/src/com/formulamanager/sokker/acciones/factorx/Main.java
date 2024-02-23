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
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.bo.FactorxBO.FORMACIONES;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Servlet implementation class Main
 */
@WebServlet("/factorx")
public class Main extends SERVLET_FACTORX {
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
		SERVLET_FACTORX.TIPO_FACTORX tipo = getTipo(request);

		final Integer[] jornada = { null };
		final Integer[] edicion = { null };
		final Integer[] edicion_actual = { null };
		
		try {
			new Navegador(true, request) {
				@Override
				protected void execute(WebClient navegador)	throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException {
					edicion[0] = Util.getInteger(request, "edicion");
					jornada[0] = AsistenteBO.getJornadaMod(obtener_jornada(navegador));
					edicion_actual[0] = obtener_edicion(navegador);
					if (edicion[0] == null) {
						edicion[0] = edicion_actual[0];
					}
				}
			};
		} catch (FailingHttpStatusCodeException | LoginException | ParseException e) {
			e.printStackTrace();
		}

		List<Jugador> jugadores = JugadorBO.leer_jugadores(tipo, edicion[0], edicion_actual[0] == null || edicion[0].equals(edicion_actual[0]));
		Collections.sort(jugadores);
		
		FORMACIONES formacion = FactorxBO.leer_formacion(tipo);
		JugadorBO.poner_estrellas(jugadores, formacion, tipo);
		
		request.setAttribute("jugadores", jugadores);
		request.setAttribute("jornada", jornada[0]);
		request.setAttribute("edicion", edicion[0]);
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
