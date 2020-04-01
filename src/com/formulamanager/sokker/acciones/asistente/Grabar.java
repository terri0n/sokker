package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION_ASISTENTE;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Actualiza las habilidades originales de un jugador
 */
@WebServlet("/asistente/grabar")
public class Grabar extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Grabar() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String mensaje = "";
		
		if (login(request)) {
			Integer tid = getUsuario(request).getDef_tid();

			Integer pid = Util.getInteger(request, "pid");
			Integer condicion = Util.getInteger(request, "stamina");
			Integer rapidez = Util.getInteger(request, "pace");
			Integer tecnica = Util.getInteger(request, "technique");
			Integer pases = Util.getInteger(request, "passing");
			Integer porteria = Util.getInteger(request, "keeper");
			Integer defensa = Util.getInteger(request, "defender");
			Integer creacion = Util.getInteger(request, "playmaker");
			Integer anotacion = Util.getInteger(request, "striker");
			DEMARCACION_ASISTENTE demarcacion = DEMARCACION_ASISTENTE.valueOf(Util.getString(request, "demarcacion"));
			boolean fiable = Util.getBoolean(request, "fiable");
			String notas = Util.getString(request, "notas");
			
			new Navegador(true) {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
					Jugador j = AsistenteBO.obtener_jugador(pid, tid < 1000, navegador);

					// Repesca del histórico
					List<Jugador> jugadores_historico = AsistenteBO.leer_jugadores(tid, true);
					if (jugadores_historico.contains(j)) {
						Jugador historico = jugadores_historico.get(jugadores_historico.indexOf(j));
						j = AsistenteBO.combinar_jugadores(j, historico);
						jugadores_historico.remove(historico);
					}
					
					// Actualizar actual
					List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, false);
					if (jugadores.contains(j)) {
						Jugador antiguo = jugadores.get(jugadores.indexOf(j));
						j = AsistenteBO.combinar_jugadores(j, antiguo);
						jugadores.remove(antiguo);
					}

					jugadores.add(j);
				
					if (tid < 1000 && j.getEn_venta() == 0 && (condicion != 0 || rapidez != 0 || tecnica != 0 || pases != 0 || porteria != 0 || defensa != 0 || creacion != 0 || anotacion != 0)) {
						j.setCondicion(condicion);
						j.setRapidez(rapidez);
						j.setTecnica(tecnica);
						j.setPases(pases);
						j.setPorteria(porteria);
						j.setDefensa(defensa);
						j.setCreacion(creacion);
						j.setAnotacion(anotacion);
						j.setActualizado(fiable);
						j.setFecha(new Date());
					}
					j.setDemarcacion(demarcacion);
					j.setNotas(notas);
					
					Collections.sort(jugadores, Jugador.getComparator());
					AsistenteBO.grabar_jugadores(jugadores, tid, false);
					AsistenteBO.grabar_jugadores(jugadores_historico, tid, true);
					AsistenteBO.calcular(jugadores, getUsuario(request));
					request.getSession().setAttribute("jugadores", jugadores);
				}
			};
			
			mensaje = "updated";
		}

		response.sendRedirect(request.getContextPath() + "/asistente?mensaje=" + mensaje);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doPost(request, response);
	}
}
