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
 * Servlet implementation class ActualizarConfiguracion
 */
@WebServlet("/asistente/actualizar_talento")
public class ActualizarTalento extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActualizarTalento() {
        super();
    }

	/**
	 * @throws ParseException 
	 * @throws FailingHttpStatusCodeException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, LoginException, FailingHttpStatusCodeException, ParseException {
		String mensaje = "";
		
		Integer pid = Util.getInteger(request, "pid");
		Double talento = Util.getDouble(request, "talento");
		
		if (login(request)) {
			Usuario usuario = getUsuario(request);
			List<Jugador> jugadores = AsistenteBO.leer_jugadores(usuario.getDef_tid(), usuario.getDef_equipo(), false, usuario);
			Jugador j = new Jugador();
			j.setPid(pid);

_log(request, pid+","+talento);

			if (jugadores.contains(j)) {
				Jugador jugador = jugadores.get(jugadores.indexOf(j));
				if (jugador != null) {
					// Actualizar
					jugador.setTalento(talento);

					Collections.sort(jugadores, Jugador.getComparator());
					new Navegador(true, request) {
						@Override
						protected void execute(WebClient navegador)	throws FailingHttpStatusCodeException, MalformedURLException, IOException {
							int jornada_actual = obtener_jornada(navegador);
							AsistenteBO.grabar_jugadores(jugadores, usuario.getDef_tid(), jornada_actual, false);
						}
					};
				}
			}
			
			AsistenteBO.calcular(jugadores, usuario);				

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
