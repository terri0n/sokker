package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
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
 * Borra el entrenamiento de una jornada de un jugador y de las anteriores jornadas
 */
@WebServlet("/asistente/borrar_entrenamiento")
public class BorrarEntrenamiento extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BorrarEntrenamiento() {
        super();
    }

	/**
	 * @throws ParseException 
	 * @throws FailingHttpStatusCodeException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, LoginException, FailingHttpStatusCodeException, ParseException {
		String mensaje = "";
		
		if (admin(request)) {
			Usuario usuario = getUsuario(request);

			// Jugador
			int pid = Util.getInteger(request, "pid");
			int jornada = Util.getInteger(request, "jornada");

_log(request, pid + " " + jornada);
			
			List<Jugador> jugadores = AsistenteBO.leer_jugadores(usuario.getDef_tid(), usuario.getDef_equipo(), false, usuario);
			Jugador j = new Jugador();
			j.setPid(pid);
			
			if (jugadores.contains(j)) {
				Jugador it = jugadores.get(jugadores.indexOf(j));
				while (it.getOriginal() != null) {
					if (it.getOriginal().getJornada() <= jornada) {
						it.setOriginal(null);
						break;
					}
					it = it.getOriginal();
				}

				new Navegador(true, request) {
					@Override
					protected void execute(WebClient navegador)	throws FailingHttpStatusCodeException, MalformedURLException, IOException {
						int jornada_actual = obtener_jornada(navegador);
						AsistenteBO.grabar_jugadores(jugadores, usuario.getDef_tid(), jornada_actual, false);
					}
				};
			}

			mensaje = "updated";
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
