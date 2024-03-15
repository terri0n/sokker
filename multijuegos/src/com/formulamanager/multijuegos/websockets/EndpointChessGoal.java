package com.formulamanager.multijuegos.websockets;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.text.StringEscapeUtils;

import com.formulamanager.multijuegos.dao.JugadoresDao;
import com.formulamanager.multijuegos.entity.Jugador;
import com.formulamanager.multijuegos.util.Util;
import com.google.gson.Gson;

/**
 * https://examples.javacodegeeks.com/enterprise-java/tomcat/apache-tomcat-websocket-tutorial/#toc600
 * 
 * @author Levi
 *
 */

@ServerEndpoint(value = "/websocket/chessgoal", configurator = GetHttpSessionConfigurator.class)
public class EndpointChessGoal extends EndpointBase {
	@OnOpen
	public void open(Session sesion, EndpointConfig config) throws IOException {
		super.open(sesion, config);

	    if (httpSession.getAttribute("jugador") == null) {
	    	// Ha habido algún error, esto no debería pasar
	    	httpSession.invalidate();
	    	cerrar_sesion(sesion, "Error: sesión sin jugador");
	    } else {
	    	// Recuperamos el usuario de la sesión
	    	sesion.getUserProperties().put("jugador", httpSession.getAttribute("jugador"));
		    System.out.println("onOpen " + sesion.getUserProperties().get("jugador"));

		    // Esperamos un poco
		    thread_sleep(500);
		    
		    // CERRAR SESIÓN ANTERIOR
		    synchronized (sesiones) {
		    	Session s = sesiones.get(getJugador(httpSession).nombre);
		    	if (s != null && s.isOpen()) {
	    			try {
	    				s.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "Nueva conexión desde la misma sesión"));
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    			}
		    	}
		    }

		    // CONTINUAR PARTIDO
		    synchronized (partidos) {
		    	for (Partido p : partidos) {
		    		if (getJugador(httpSession).equals(p.getJugador(COLOR.blancas))) {
		    			partido = p;
		    			p.blancas = sesion;
		    			break;
		    		} else if (getJugador(httpSession).equals(p.getJugador(COLOR.negras))) {
		    			partido = p;
		    			p.negras = sesion;
		    			break;
		    		}
		    	}
		    }
		    
		    // Cuando se conecta un usuario le enviamos la lista de usuarios conectados y los partidos abiertos o en juego
		    // además del nombre de su rival si estaba en mitad de una partida
		    Respuesta respuesta = new Respuesta();
		    respuesta.jugador = getJugador(sesion).nombre;
		    
		    Session rival = getRival();
		    if (rival != null) {
		    	respuesta.color = partido.getColor(sesion);
		    	respuesta.rival = getJugador(rival).nombre;
		    }

		    // ENVIAR MENSAJE DE NUEVO USUARIO
		    synchronized (sesiones) {
		    	sesiones.put(getJugador(sesion).nombre, sesion);
		    	
		    	for (Session s : sesiones.values()) {
		    		if (s.isOpen()) {
		    			Jugador j = getJugador(s);
		    			respuesta.jugadores.add(j);
		    			// Nuevo usuario
		    			if (!equals(sesion, s)) {
		    				enviar(s, "nuevo_usuario", getJugador(sesion).toJson());
		    			}
		    		}
		    	}
		    }
		   
		    // CANCELAR PARTIDOS SIN JUGADORES
		    synchronized (partidos) {
/*for (Partido p : partidos) {
	System.out.println(p.getId() + " " + p.privado);
}
*/		    	Iterator<Partido> itp = partidos.iterator();
		    	while (itp.hasNext()) {
		    		Partido p = itp.next();
		    		try {
		    			if (!p.privado || (p.blancas != null && p.negras != null)) {
		    				respuesta.partidos.add(p.getRespuesta());
		    			}
		    		} catch (Exception e) {
		    			// TODO: mirar cúando puede pasar esto
		    			// Cancelamos los partidos sin jugadores
		    			if ((p.blancas == null || !p.blancas.isOpen()) && (p.negras == null || !p.negras.isOpen())) {
		    				itp.remove();
		    				for (Session s : sesiones.values()) {
		    					enviar(s, "partido_cancelado", (p.getJugador(COLOR.blancas) != null ? p.getJugador(COLOR.blancas).nombre : p.getJugador(COLOR.negras).nombre));
		    				}
		    			}
		    		}
		    	}
		    	enviar(sesion, "conexion_abierta", new Gson().toJson(respuesta));
		    }
	    }
	}
	
	private void thread_sleep(int milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@OnMessage
	public String onMessage(String message) {
		System.out.println(getJugador(sesion).nombre + " \t-> " + message);
		
		// Actualizo el tiempo de la sesión
        long lastAccessedTime = httpSession.getLastAccessedTime();
        long currentTime = System.currentTimeMillis();
        long durationSinceLastAccess = currentTime - lastAccessedTime;
        long newSessionDuration = durationSinceLastAccess + (30 * 60 * 1000); // 30 minutos en milisegundos
        httpSession.setMaxInactiveInterval((int) (newSessionDuration / 1000)); // Convertir a segundos

        HashMap<String, String> hm = new Gson().fromJson(message, HashMap.class);
		String accion = hm.get("accion");
		String params = hm.get("params");

		try {
			switch (accion) {
				case "aceptar_partido":	// Aceptar partido, tb para reconectar
					aceptar_partido(params);
					break;
				case "cancelar_invitacion":	// Cancelar invitación
					cancelar_invitacion(params);
					break;
				case "cancelar_partido":	// Cancelar partido
					cancelar_partido();
					break;
				case "crear_partido":		// Crear partido (color, duracion)
					String[] split = (params + ",*").split(",");
					crear_partido(COLOR.valueOf(split[0]), Util.stringToInteger(split[1]));
					break;
				case "fin_turno":			// Fin de turno
					finalizar_turno(params);
					break;
				case "hacer_movimiento":	// Estado >= 2
					hacer_movimiento(params);
					break;
				case "mandar_tactica":		// Manda su táctica
					manda_tactica(params);
					break;
				case "otro_partido":		// Otro partido
					otro_partido();
					break;
				case "partido_privado":		// Partido privado (color, duracion, usuario)
					String[] split2 = params.split(",");
					partido_privado(COLOR.valueOf(split2[0]), Util.stringToInteger(split2[1]), split2[2]);
					break;
				case "poner_away":	// Poner away
					poner_away();
					break;
				case "quitar_away":	// Quitar away
					quitar_away();
					break;
				case "ranking":	// Ranking
					ranking();
					break;
				case "rival_cancela":	// Rival cancela el partido
					// Al parecer el Endpoint del rival, que cancela el partido, no tiene acceso a este Endpoint. Por eso me mando este mensaje
					partido = null;
					break;
				case "texto":	// Texto
					escribir(params);
					break;
				case "tiempo_agotado":	// Tiempo agotado
					tiempo_agotado(true);
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			cerrar_sesion(sesion, e.getMessage());
		}
		return null;
	}

	private void ranking() throws SQLException, ParseException {
		List<Jugador> jugadores = JugadoresDao.buscar(null, null, null, "puntos desc", 50);
		enviar(sesion, "mostrar_ranking", new Gson().toJson(jugadores));
	}

	private void poner_away() {
		synchronized (sesiones) {
			for (Session s : sesiones.values()) {
				enviar(s, "poner_away", getJugador(sesion).nombre);
			}
		}
	}

	private void quitar_away() {
		synchronized (sesiones) {
			for (Session s : sesiones.values()) {
				enviar(s, "quitar_away", getJugador(sesion).nombre);
			}
		}
	}
	
	private void cancelar_invitacion(String usuario) {
		synchronized (partidos) {
			borrar_partido(partido);
			partido = null;
		}

		synchronized (sesiones) {
			// Cancelar partido
			enviar(sesiones.get(usuario), "partido_cancelado", usuario);
		}
	}

	private void partido_privado(COLOR color, Integer duracion, String usuario) {
		synchronized (partidos) {
			Session rival = sesiones.get(usuario);
			if (rival != null) {
				borrar_partido(partido);
				partido = new Partido(color, duracion, getJugador(sesion), sesion, true);
				partidos.add(partido);
				HashMap<String, Object> hm = new HashMap<>();
				hm.put("color", color.cambiar());
				hm.put("duracion", Util.nvl(duracion));
				hm.put("rival", getJugador(sesion).nombre);
				enviar(rival, "partido_privado", hm);
			}
		}
	}

	private void tiempo_agotado(boolean primera) {
		synchronized (partido) {
			partido.actualizar_cronometro();

			if (partido.getSegundos(partido.tiempo_blancas) <= 0 || partido.getSegundos(partido.tiempo_negras) <= 0) {
				fin_partido();
			} else if (primera) {
				// Esperamos un segundo y lo volvemos a comprobar
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				tiempo_agotado(false);
			} else {
				System.out.println("---> Aviso de tiempo agotado incorrecto: " + partido.getTiempos());
			}
		}
	}

	private void manda_tactica(String params) throws Exception {
		synchronized (partido) {
			if (partido.setTactica(partido.getColor(sesion), params)) {
				// Si ya se han enviado las dos tácticas, empezamos
				enviar(sesion, "manda_tactica", partido.getTactica(partido.getColor(sesion).cambiar()));

				// Enviamos la táctica al rival
				enviar(getRival(), "manda_tactica", partido.getTactica(partido.getColor(sesion)));
				
				// Y a los observadores como si acabara de empezar el partido
				mandar_observadores("observar_partido", partido.getId() + "," + partido.getTiempos() + "," + partido.duracion + "," + partido.getEstado(sesion) + "@" + partido.toJson());
			}
		}
	}

	private void finalizar_turno(String params) throws Exception {
		synchronized (partido) {
			partido.hacer_movimientos_turno(params, sesion);
			
			// Añadimos la información sobre los tiempos
			String nuevo_mensaje = partido.getTiempos() + "," + partido.getEstado(sesion) + "@" + partido.toJson();
			enviar(getRival(), "movimientos_turno_rival", nuevo_mensaje);
			enviar(sesion, "actualizar_tiempos", partido.getTiempos());
			mandar_observadores("movimientos_turno_rival", nuevo_mensaje);
			
			if (partido.comprobar_fin()) {
				fin_partido();
			}
		}
	}
	
	private void hacer_movimiento(String params) {
		synchronized (partido) {
			partido.hacer_movimiento(params, sesion);
			
			// Informamos sobre el movimiento
			enviar(getRival(), "movimiento_rival", params);
			mandar_observadores("movimiento_rival", params);
			
			if (partido.comprobar_fin()) {
				fin_partido();
			}
		}
	}

	private void mandar_observadores(String accion, String mensaje) {
		for (Session s : partido.observadores) {
			if (!equals(s, sesion)) {
				enviar(s, accion, mensaje);
			}
		}
	}
	
	private void escribir(String texto) {
		String mensaje = getJugador(sesion).nombre + "," + StringEscapeUtils.escapeEcmaScript(texto);
		
		if (partido != null && partido.blancas != null && partido.negras != null) {
			if (partido.getColor(sesion) != COLOR.blancas) {
				enviar(partido.blancas, "texto", mensaje);
			}

			if (partido.getColor(sesion) != COLOR.negras) {
				enviar(partido.negras, "texto", mensaje);
			}

			for (Session s : partido.observadores) {
				if (!equals(s, sesion) ) {
					enviar(s, "texto", mensaje);
				}
			}
		}
	}

	private void otro_partido() {
		synchronized (partidos) {
			if (partido != null) {
				if (partido.rival_quiere_otra) {
					// Intercambiamos los colores para el próximo partido
					Session aux = partido.blancas;
					partido.blancas = partido.negras;
					partido.negras = aux;
	
					// Reseteamos el partido
					partido.reset();
					
					// Y les mandamos un aviso
					enviar(partido.blancas, "volver_a_jugar", "");
					enviar(partido.negras, "volver_a_jugar", "");
					
					for (Session o : partido.observadores) {
						enviar(o, "volver_a_jugar", "");
					}
				} else {
					partido.rival_quiere_otra = true;
				}
			}
		}
	}
	
	private void cancelar_partido() {
		synchronized (sesiones) {
			for (Session usuario : sesiones.values()) {
				if (!equals(usuario, sesion)) {
					enviar(usuario, "partido_cancelado", getJugador(sesion).nombre);
				}
			}
		}

		synchronized (partidos) {
			partidos.remove(partido);
			partido = null;
		}
	}

	private void aceptar_partido(String usuario) {
		synchronized (partidos) {
			for (Partido p : partidos) {
				if (p.blancas != null && usuario.equals(p.getJugador(COLOR.blancas).nombre) || p.negras != null && usuario.equals(p.getJugador(COLOR.negras).nombre)) {
					if (p.blancas == null || p.negras == null) {
						// Partido esperando rival
						if (getJugador(sesion).nombre.equals(usuario)) {
							cerrar_sesion(sesion, "No puedes jugar contigo mismo");
						} else {
							// Acepta el partido
							borrar_partido(partido);
							
							partido = p;
							p.anyadir_jugador(getJugador(sesion), sesion);
							p.reset();
		
							enviar(p.blancas, "aceptar_partido", "blancas," + Util.nvl(p.duracion) + "," + p.getJugador(COLOR.negras).nombre);
							enviar(p.negras, "aceptar_partido", "negras," + Util.nvl(p.duracion) + "," + p.getJugador(COLOR.blancas).nombre);
							
							for (Session u : sesiones.values()) {
								if (!equals(u, p.blancas) && !equals(u, p.negras)) {
									enviar(u, "partido_aceptado", p.getRespuesta());
								}
							}
						}
					} else if (p.getJugador(COLOR.blancas).equals(getJugador(sesion)) || p.getJugador(COLOR.negras).equals(getJugador(sesion))) {
						// Reconecta como jugador (blancas o negras)
						partido = p;
						if (p.getJugador(COLOR.blancas).equals(getJugador(sesion))) {
							p.blancas = sesion;
						} else {
							p.negras = sesion;
						}
						
						if (p.turno == null && p.getTactica(p.getColor(sesion)) != null && p.getTactica(p.getColor(sesion).cambiar()) == null) {
							// Si el partido no ha empezado y ya había mandado la táctica (pero el rival no), se la recuerdo (aún no está en el tablero)
							enviar(sesion, "observar_partido", p.getId() + "," + p.getTiempos() + "," + p.duracion + "," + p.getEstado(sesion) + "@" + p.getTactica(p.getColor(sesion)));
						} else {
							// Si no, mando el tablero completo
							enviar(sesion, "observar_partido", p.getId() + "," + p.getTiempos() + "," + p.duracion + "," + p.getEstado(sesion) + "@" + p.toJson());
						}
					} else {
						// El partido ya ha empezado. Entra como observador
						for (Partido p2 : partidos) {
							p2.observadores.remove(sesion);
						}
						
						enviar(p.blancas, "nuevo_observador", getJugador(sesion).nombre);
						enviar(p.negras, "nuevo_observador", getJugador(sesion).nombre);
						for (Session o : p.observadores) {
							enviar(o, "nuevo_observador", getJugador(sesion).nombre);
						}

						partido = p;
						p.observadores.add(sesion);
						enviar(sesion, "observar_partido", p.getId() + "," + p.getTiempos() + "," + p.duracion + "," + p.getEstado(sesion) + "@" + p.toJson());
					}
					break;	// Ya ha encontrado su partido
				}
			}
		}
	}

	private void borrar_partido(Partido partido) {
		if (partido != null) {
			if (partido.getColor(sesion) != null) {
				// Si yo había creado otro partido, lo borramos
				partidos.remove(partido);
			} else {
				// Y si estaba de observador, me quito
				partido.observadores.remove(sesion);
			}
		}
	}

	private void crear_partido(COLOR color, Integer duracion) {
		synchronized (partidos) {
			if (partido != null) {
				if (partido.getColor(sesion) != null) {
					System.out.println(getJugador(sesion).nombre + " intenta crear otro partido");
					return;
				} else {
					partido.observadores.remove(sesion);
				}
			}
			
			partido = new Partido(color, duracion, getJugador(sesion), sesion, false);
			partidos.add(partido);
		}
		
		synchronized (sesiones) {
			for (Session s : sesiones.values()) {
				enviar(s, "nuevo_partido", Util.nvl(duracion) + "," + (color == COLOR.blancas ? getJugador(sesion).nombre : "") + "," + (color == COLOR.negras ? getJugador(sesion).nombre : ""));
			}
		}
	}

	@OnError
	public void onError(Throwable e){
		e.printStackTrace();
		cerrar_sesion(sesion, e.getMessage());
	}
	
	@OnClose
    public void onClose(Session sesion) {
		System.out.println("onClose " + getJugador(sesion).nombre);

		try {
			synchronized (partidos) {
				if (partido != null) {
					if (getJugador(sesion).equals(partido.getJugador(COLOR.blancas))) {
						if (partido.negras == null || !partido.negras.isOpen()) {
							partidos.remove(partido);
						} else {
							// Si pierde la conexión le sumamos el tiempo transcurrido desde el último movimiento
							partido.actualizar_cronometro();
							partido.blancas = null;
						}
					} else if (getJugador(sesion).equals(partido.getJugador(COLOR.negras))) {
						if (partido.blancas == null || !partido.blancas.isOpen()) {
							partidos.remove(partido);
							System.out.println(getJugador(sesion).nombre + " borra partido");
						} else {
							// Si pierde la conexión le sumamos el tiempo transcurrido desde el último movimiento
							partido.actualizar_cronometro();
							partido.negras = null;
						}
					} else {
						partido.observadores.remove(sesion);
					}
				}
			}

			if (getJugador(sesion) != null) {
				synchronized (sesiones) {
					sesiones.remove(getJugador(sesion).nombre);
					
					for (Session usuario : sesiones.values()) {
						enviar(usuario, "usuario_desconectado", getJugador(sesion).nombre);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	public void fin_partido() {
		if (partido.oficial) {
			// Actualización del ELO
			double myChanceToWin = 1D / (1D + Math.pow(10D, (getJugador(partido.negras).puntos - getJugador(partido.blancas).puntos) / 400D));
		    int ratingDelta = (int) Math.round(32D * ((partido.ganador == COLOR.blancas ? 1 : 0) - myChanceToWin));

		    setPuntos(partido.blancas, getJugador(partido.blancas).puntos + ratingDelta);
		    setPuntos(partido.negras, getJugador(partido.negras).puntos - ratingDelta);
	    }
	    
	    synchronized (EndpointBase.sesiones) {
	    	String tiempos = partido.getTiempos();
	    	for (Session s : sesiones.values()) {
	    		enviar(s, "partido_finalizado", tiempos + "," + partido.ganador + "," + getJugador(partido.blancas).puntos + "," + getJugador(partido.negras).puntos);
	    	}
	    }
	}
}