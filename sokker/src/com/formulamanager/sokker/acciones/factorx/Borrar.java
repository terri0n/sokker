package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Iterator;
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
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Servlet implementation class Main
 */
@WebServlet("/factorx/borrar")
public class Borrar extends SERVLET_FACTORX {
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
		SERVLET_FACTORX.TIPO_FACTORX tipo = getTipo(request);

		if (admin(request)) {
			try {
				new Navegador(true, request) {
					@Override
					protected void execute(WebClient navegador)	throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException {
						Integer pid = Integer.valueOf(request.getParameter("pid"));
						int edicion_actual = obtener_edicion(navegador);
						List<Jugador> jugadores = JugadorBO.leer_jugadores(tipo, edicion_actual, true);
						
						Iterator<Jugador> it = jugadores.iterator();
						while (it.hasNext()) {
							Jugador j = it.next();
							if (j.getPid().equals(pid)) {
								it.remove();
							}
						}
				
						Collections.sort(jugadores);
						FactorxBO.grabar_jugadores(jugadores, tipo, edicion_actual, false);
					}
				};
			} catch (FailingHttpStatusCodeException | LoginException | ParseException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		response.sendRedirect(request.getContextPath() + "/factorx?mensaje=Borrado&tipo=" + tipo.name());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
	}
}
