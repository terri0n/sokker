package com.formulamanager.sokker.acciones.asistente;

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
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Borra un jugador
 * 
 * NOTA: reemplazado por "borrar jugadores"
 */
@Deprecated
@WebServlet("/asistente/borrar")
public class Borrar extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Borrar() {
        super();
    }

	/**
	 * @throws ParseException 
	 * @throws FailingHttpStatusCodeException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, LoginException, FailingHttpStatusCodeException, ParseException {
		String mensaje = "";
		
		if (login(request)) {
			Usuario usuario = (Usuario)request.getSession().getAttribute("usuario");
			Integer tid = usuario.getDef_tid();
			Integer pid = Util.getInteger(request, "pid");

_log(request, pid+"");
			
			List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, usuario.getDef_equipo(), false, usuario);
			List<Jugador> jugadores_historico = AsistenteBO.leer_jugadores(tid, usuario.getDef_equipo(), true, usuario);

			for (Jugador j : jugadores) {
				if (j.getPid().equals(pid)) {
					if (pid > 0) {	// Jugador de prueba
						jugadores_historico.add(j);
					}
					jugadores.remove(j);
					break;
				}
			}
				
			Collections.sort(jugadores, Jugador.getComparator());
			new Navegador(true, request) {
				@Override
				protected void execute(WebClient navegador)	throws FailingHttpStatusCodeException, MalformedURLException, IOException {
					int jornada_actual = obtener_jornada(navegador);
					AsistenteBO.grabar_jugadores(jugadores, tid, jornada_actual, false);
					AsistenteBO.grabar_jugadores(jugadores_historico, tid, jornada_actual, true);
				}
			};

			mensaje = "removed";
		}

		response.sendRedirect(request.getContextPath() + "/asistente?mensaje=" + mensaje);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
