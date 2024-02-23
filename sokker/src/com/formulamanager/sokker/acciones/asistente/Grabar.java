package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.dao.AsistenteDAO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION_ASISTENTE;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Actualiza las habilidades originales de un jugador
 * 
 * NOTA: si el jugador es de prueba el PID llegará vacío
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
	 * @throws ParseException 
	 * @throws FailingHttpStatusCodeException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, LoginException, FailingHttpStatusCodeException, ParseException {
		String mensaje[] = {""};
		
		if (login(request)) {
			final Usuario _usuario = getUsuario(request);
			final Integer tid = _usuario.getDef_tid();

			final Integer pid = Util.getInteger(request, "pid");
			final Integer condicion = Util.getInteger(request, "stamina");
			final Integer rapidez = Util.getInteger(request, "pace");
			final Integer tecnica = Util.getInteger(request, "technique");
			final Integer pases = Util.getInteger(request, "passing");
			final Integer porteria = Util.getInteger(request, "keeper");
			final Integer defensa = Util.getInteger(request, "defender");
			final Integer creacion = Util.getInteger(request, "playmaker");
			final Integer anotacion = Util.getInteger(request, "striker");
			final DEMARCACION_ASISTENTE demarcacion = DEMARCACION_ASISTENTE.valueOf(Util.getString(request, "demarcacion"));
			final boolean fiable = Util.getBoolean(request, "fiable");
			final boolean destacar = Util.getBoolean(request, "destacar");
			final String[] notas = new String[] { Util.getString(request, "notas") };
			if (notas[0] != null && notas[0].length() > 5000) {
				notas[0] = notas[0].substring(0, 5000);
			}
			final String color = Util.getString(request, "color");
			final Integer edad = Util.getInteger(request, "edad");	// Para jugador de pruebas

_log(request, pid + " " + condicion + " " + rapidez + " " + tecnica + " " + pases + " " + porteria + " " + defensa + " " + creacion + " " + anotacion + " " + demarcacion + " " + fiable + " " + destacar + " " + notas[0]);
			
			new Navegador(true, request) {
				@Override
				protected void execute(WebClient navegadorXML) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
					int jornada_actual = obtener_jornada(navegadorXML);
					
					try {
						// Leo los valores públicos del jugador
						new Navegador(false, request) {
							@Override
							protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
								Jugador j_nuevo;
								if (pid == null) {
									j_nuevo = new Jugador(0, Util.getTexto(request.getLocale().getLanguage(), "menu.test_player"), edad, 0, tid, _usuario);
									Usuario usuario2 = new Usuario("prueba/" + _usuario.getTid(), _usuario);
									j_nuevo.setUsuario2(usuario2);
									j_nuevo.setJornada(jornada_actual);
									j_nuevo.setPais(_usuario.getCountryID());
									j_nuevo.setPeso(0);
									j_nuevo.setAltura(0);
									j_nuevo.setIMC(0);
									
									UsuarioBO.grabar_usuario(usuario2);
								} else {
									j_nuevo = AsistenteDAO.obtener_jugador(pid, tid < 1000, jornada_actual, isIncrementar_edad(), _usuario, navegador);

									// Entrenamiento avanzado
									Set<Integer> avanzados = AsistenteDAO.obtener_entrenamiento_avanzado(navegador);
									j_nuevo.setEntrenamiento_avanzado(avanzados.contains(j_nuevo.getPid()));
								}
								
								// Leo los jugadores y busco la versión anterior
								List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, _usuario.getDef_equipo(), false, _usuario);
								Jugador j_anterior;
								if (jugadores.contains(j_nuevo)) {
									j_anterior = jugadores.get(jugadores.indexOf(j_nuevo));
								} else {
									j_anterior = null;
								}
								
								// Actualizo los valores de los jugadores NT para que se cree una nueva semana en caso de que cambien
								if (tid < 1000 && Util.invl(j_nuevo.getEn_venta()) == 0 && (Util.invl(condicion) != 0 || Util.invl(rapidez) != 0 || Util.invl(tecnica) != 0 || Util.invl(pases) != 0 || Util.invl(porteria) != 0 || Util.invl(defensa) != 0 || Util.invl(creacion) != 0 || Util.invl(anotacion) != 0)
										|| pid == null) {	 // Jugador de prueba
									// Solo actualizo la fecha si ha cambiado el valor de alguna habilidad
									if (j_anterior == null || !rapidez.equals(j_anterior.getRapidez()) || !tecnica.equals(j_anterior.getTecnica()) || !pases.equals(j_anterior.getPases()) || !porteria.equals(j_anterior.getPorteria()) || !defensa.equals(j_anterior.getDefensa()) || !creacion.equals(j_anterior.getCreacion()) || !condicion.equals(j_anterior.getCondicion()) || !anotacion.equals(j_anterior.getAnotacion())) {
										j_nuevo.setCondicion(condicion);
										j_nuevo.setRapidez(rapidez);
										j_nuevo.setTecnica(tecnica);
										j_nuevo.setPases(pases);
										j_nuevo.setPorteria(porteria);
										j_nuevo.setDefensa(defensa);
										j_nuevo.setCreacion(creacion);
										j_nuevo.setAnotacion(anotacion);
										j_nuevo.setActualizado(fiable);
										j_nuevo.setFecha(new Date());
									}
								}
								
								// Repesca del histórico
								List<Jugador> jugadores_historico = AsistenteBO.leer_jugadores(tid, _usuario.getDef_equipo(), true, _usuario);
								if (jugadores_historico.contains(j_nuevo)) {
									Jugador historico = jugadores_historico.get(jugadores_historico.indexOf(j_nuevo));
									j_nuevo = AsistenteBO.combinar_jugadores(j_nuevo, historico);
									jugadores_historico.remove(historico);
								}
								
								// Actualizar actual
								if (j_anterior != null) {
									j_nuevo = AsistenteBO.combinar_jugadores(j_nuevo, j_anterior);
									jugadores.remove(j_anterior);
								}
								
								// Estos valores los pongo tal cual me llegan. Los antiguos no me interesan
								j_nuevo.setDemarcacion(demarcacion);
								j_nuevo.setColor(color);
								j_nuevo.setDestacar(destacar);
								j_nuevo.setNotas(notas[0]);
								
								jugadores.add(j_nuevo);
								
								AsistenteBO.grabar_jugadores(jugadores, tid, jornada_actual, false);
								AsistenteBO.grabar_jugadores(jugadores_historico, tid, jornada_actual, true);
							}
						};

						mensaje[0] = "updated";
					} catch (Exception e) {
						e.printStackTrace();
						mensaje[0] = "Error connecting to Sokker: " + e.toString();
					}
				}
			};
		}

		response.sendRedirect(request.getContextPath() + "/asistente?mensaje=" + mensaje[0]);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doPost(request, response);
	}
}
