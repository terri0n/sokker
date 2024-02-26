package com.formulamanager.multijuegos.websockets;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.text.StringEscapeUtils;

import com.formulamanager.multijuegos.dao.JugadoresDao;
import com.formulamanager.multijuegos.util.Util;
import com.google.gson.GsonBuilder;

/**
 * https://examples.javacodegeeks.com/enterprise-java/tomcat/apache-tomcat-websocket-tutorial/#toc600
 * 
 * @author Levi
 *
 */

@ServerEndpoint(value = "/websocket", configurator = GetHttpSessionConfigurator.class)
public class Endpoint extends EndpointBase {
	@OnOpen
	public void open(Session sesion, EndpointConfig config) throws IOException {
		super.open(sesion, config);

	    if (httpSession.getAttribute("jugador") == null) {
	    	// LOGIN/REGISTRO: lo comento. Mejor que esté por separado
	    	httpSession.invalidate();
	    	cerrar_sesion(sesion, "Error: sesión sin jugador");
	    } else {
	    	// Recuperamos el usuario de la sesión
	    	sesion.getUserProperties().put("jugador", httpSession.getAttribute("jugador"));
		    System.out.println("onOpen (vuelve) " + sesion.getUserProperties().get("jugador"));

		    // Esperamos un poco
		    thread_sleep(500);
		    
		    // CERRAR SESIÓN ANTERIOR
		    synchronized (sesiones) {
		    	Session s = (sesiones.get(((Jugador)httpSession.getAttribute("jugador")).nombre));
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
		    		if (p.nombre_blancas != null && p.nombre_blancas.equals(((Jugador)httpSession.getAttribute("jugador")).nombre)) {
		    			partido = p;
		    			p.blancas = sesion;
		    			break;
		    		} else if (p.nombre_negras != null && p.nombre_negras.equals(((Jugador)httpSession.getAttribute("jugador")).nombre)) {
		    			partido = p;
		    			p.negras = sesion;
		    			break;
		    		}
		    	}
		    }
		    
		    // Cuando se conecta un usuario le enviamos la lista de usuarios conectados y los partidos abiertos o en juego
		    // además del nombre de su rival si estaba en mitad de una partida
		    Respuesta respuesta = new Respuesta();
		    respuesta.nombre = getNombre(sesion);
		    
		    Session rival = getRival();
		    if (rival != null) {
		    	respuesta.nombre_rival = partido.getColor(sesion) == COLOR.blancas ? partido.nombre_negras : partido.nombre_blancas;
		    	respuesta.color = partido.getColor(sesion);
		    }

		    // ENVIAR MENSAJE DE NUEVO USUARIO
		    synchronized (sesiones) {
		    	sesiones.put(getNombre(sesion), sesion);
		    	
		    	for (Session s : sesiones.values()) {
		    		if (s.isOpen()) {
		    			respuesta.jugadores.add((Jugador)s.getUserProperties().get("jugador"));
		    			// Nuevo usuario
		    			if (!equals(sesion, s)) {
		    				enviar(s, "nuevo_usuario", new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(
	    						sesion.getUserProperties().get("jugador"))
	    					);
		    			}
		    		}
		    	}
		    }
		   
		    // CANCELAR PARTIDOS SIN JUGADORES
		    synchronized (partidos) {
		    	Iterator<Partido> itp = partidos.iterator();
		    	while (itp.hasNext()) {
		    		Partido p = itp.next();
		    		try {
		    			if (!p.privado || (p.blancas != null && p.negras != null)) {
		    				respuesta.partidos.add(p.getRespuesta());
		    			}
		    		} catch (Exception e) {
		    			// Cancelamos los partidos sin jugadores
		    			if ((p.blancas == null || !p.blancas.isOpen()) && (p.negras == null || !p.negras.isOpen())) {
		    				itp.remove();
		    				for (Session s : sesiones.values()) {
		    					enviar(s, "partido_cancelado", (p.nombre_blancas != null ? p.nombre_blancas : p.nombre_negras));
		    				}
		    			}
		    		}
		    	}
		    	enviar(sesion, "conexion_abierta", new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(respuesta));
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
		System.out.println(getNombre(sesion) + "> Text message: " + message);
		
		// Actualizo el tiempo de la sesión
        long lastAccessedTime = httpSession.getLastAccessedTime();
        long currentTime = System.currentTimeMillis();
        long durationSinceLastAccess = currentTime - lastAccessedTime;
        long newSessionDuration = durationSinceLastAccess + (30 * 60 * 1000); // 30 minutos en milisegundos
        httpSession.setMaxInactiveInterval((int) (newSessionDuration / 1000)); // Convertir a segundos
		
		int indice = message.indexOf(",");
		String accion = message.substring(0, indice);
		String params = message.substring(indice + 1);
		
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
				case "crear_partido":	// Crear partido (color, duracion)
					String[] split = (params + ",*").split(",");
					crear_partido(COLOR.valueOf(split[0]), Util.stringToInteger(split[1]));
					break;
				case "fin_turno":	// Fin de turno
					finalizar_turno(params);
					break;
				case "mandar_tactica":	// Manda su táctica
					tactica(params);
					break;
				case "otro_partido":	// Otro partido
					otro_partido();
					break;
				case "partido_privado":	// Partido privado (color, duracion, usuario)
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
			throw new RuntimeException(e);
		}
		return null;
	}

	private void ranking() {
		try {
			List<Jugador> jugadores = JugadoresDao.buscar(null, null, null, "puntos desc", 50);
			enviar(sesion, "ranking", new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(jugadores));
		} catch (SQLException | ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void poner_away() {
		synchronized (sesiones) {
			for (Session s : sesiones.values()) {
				enviar(s, "poner_away", getNombre(sesion));
			}
		}
	}

	private void quitar_away() {
		synchronized (sesiones) {
			for (Session s : sesiones.values()) {
				enviar(s, "quitar_away", getNombre(sesion));
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
			enviar(sesiones.get(usuario), "cancelar_partido", usuario);
		}
	}

	private void partido_privado(COLOR color, Integer duracion, String usuario) {
		synchronized (partidos) {
			Session rival = sesiones.get(usuario);
			if (rival != null) {
				borrar_partido(partido);
				partido = new Partido(color, duracion, getNombre(sesion), sesion, true);
				partidos.add(partido);
				enviar(rival, "partido_privado", color.cambiar() + "," + Util.nvl(duracion) + "," + getNombre(sesion));
			}
		}
	}

	private void tiempo_agotado(boolean primera) {
		synchronized (partido) {
			partido.actualizar_cronometro();

			if (partido.getSegundos(partido.tiempo_blancas) <= 0 || partido.getSegundos(partido.tiempo_negras) <= 0) {
				String params = partido.getTiempos();
				enviar(partido.blancas, "tiempo_agotado", params);
				enviar(partido.negras, "tiempo_agotado", params);
				for (Session s : partido.observadores) {
					enviar(s, "tiempo_agotado", params);
				}
			} else if (primera) {
				// Esperamos un segundo y lo volvemos a comprobar
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				tiempo_agotado(false);
			}
		}
	}

	private void tactica(String params) {
		synchronized (partido) {
			partido.setTactica(partido.getColor(sesion), params);
			
			if (partido.getTactica(COLOR.negras) != null && partido.getTactica(COLOR.blancas) != null) {
				// Si ya se han enviado las dos tácticas, empezamos
				enviar(sesion, "manda_tactica", partido.getTactica(partido.getColor(sesion).cambiar()));

				// Enviamos la táctica al rival
				enviar(getRival(), "manda_tactica", partido.getTactica(partido.getColor(sesion)));
				
				// Y a los observadores como si acabara de empezar el partido
				mandar_observadores("observar_partido", partido.getId() + "," + partido.getTiempos() + "," + partido.duracion + "," + partido.getEstado(sesion) + "@" + partido.toString());
				
				partido.ultimo_movimiento = new Date();
				partido.turno = COLOR.blancas;
			}
		}
	}

	private void finalizar_turno(String params) {
		synchronized (partido) {
			partido.anyadir_movimiento(params, sesion);

			// Añadimos la información sobre los tiempos
			String nuevo_mensaje = partido.getTiempos() + "@" + params;
			enviar(getRival(), "movimiento_rival", nuevo_mensaje);
			enviar(sesion, "actualizar_tiempos", partido.getTiempos());
			mandar_observadores("movimiento_rival", nuevo_mensaje);
			
			if (partido.ganador != null) {
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
		String mensaje = getNombre(sesion) + "," + StringEscapeUtils.escapeEcmaScript(texto);
		
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
		} else {
			synchronized (sesiones) {
				for (Session s : sesiones.values()) {
					if (!esta_en_partido(s) && !equals(s, sesion)) {
						enviar(s, "texto", mensaje);
					}
				}
			}
		}
	}

	private boolean esta_en_partido(Session s) {
		for (Partido p : partidos) {
			if (p.getColor(s) != null || p.observadores.contains(s)) {
				return true;
			}
		}
		
		return false;
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
					enviar(partido.blancas, "volver_a_jugar", null);
					enviar(partido.negras, "volver_a_jugar", null);
					
					for (Session o : partido.observadores) {
						enviar(o, "volver_a_jugar", null);
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
					enviar(usuario, "partido_cancelado", getNombre(sesion));
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
				if (usuario.equals(p.nombre_blancas) || usuario.equals(p.nombre_negras)) {
					if (p.blancas == null || p.negras == null) {
						// Acepta el partido
						borrar_partido(partido);
						
						partido = p;
						p.anyadir_jugador(getNombre(sesion), sesion);
						p.reset();
	
						enviar(p.blancas, "aceptar_partido", "blancas," + Util.nvl(p.duracion) + "," + p.nombre_negras);
						enviar(p.negras, "aceptar_partido", "negras," + Util.nvl(p.duracion) + "," + p.nombre_blancas);
						
						for (Session u : sesiones.values()) {
							if (!equals(u, p.blancas) && !equals(u, p.negras)) {
								enviar(u, "partido_aceptado", p.getRespuesta());
							}
						}
						return;
					} else if (p.nombre_blancas.equals(getNombre(sesion)) || p.nombre_negras.equals(getNombre(sesion))) {
						// Reconecta como jugador (blancas o negras)
						partido = p;
						if (p.nombre_blancas.equals(getNombre(sesion))) {
							p.blancas = sesion;
						} else {
							p.negras = sesion;
						}
						
						if (p.turno == null && p.getTactica(p.getColor(sesion)) != null) {
							// Si el partido no ha empezado y ya había mandado la táctica, se la recuerdo
							enviar(sesion, "observar_partido", p.getId() + "," + p.getTiempos() + "," + p.duracion + "," + p.getEstado(sesion) + "@" + p.getTactica(p.getColor(sesion)));
						} else {
							// Si no, mando el tablero completo
							enviar(sesion, "observar_partido", p.getId() + "," + p.getTiempos() + "," + p.duracion + "," + p.getEstado(sesion) + "@" + p.toString());
						}
						
					} else {
						// El partido ya ha empezado. Entra como observador
						for (Partido p2 : partidos) {
							p2.observadores.remove(sesion);
						}
						
						enviar(p.blancas, "nuevo_observador", getNombre(sesion));
						enviar(p.negras, "nuevo_observador", getNombre(sesion));
						for (Session o : p.observadores) {
							enviar(o, "nuevo_observador", getNombre(sesion));
						}

						partido = p;
						p.observadores.add(sesion);
						enviar(sesion, "observar_partido", p.getId() + "," + p.getTiempos() + "," + p.duracion + "," + p.getEstado(sesion) + "@" + p.toString());
					}
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
					System.out.println(getNombre(sesion) + " intenta crear otro partido");
					return;
				} else {
					partido.observadores.remove(sesion);
				}
			}
			
			partido = new Partido(color, duracion, getNombre(sesion), sesion, false);
			partidos.add(partido);
		}
		
		synchronized (sesiones) {
			for (Session s : sesiones.values()) {
				enviar(s, "nuevo_partido", Util.nvl(duracion) + "," + (color == COLOR.blancas ? getNombre(sesion) : "") + "," + (color == COLOR.negras ? getNombre(sesion) : ""));
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
		System.out.println("onClose " + getNombre(sesion));

		try {
			synchronized (partidos) {
				if (partido != null) {
					if (getNombre(sesion).equals(partido.nombre_blancas)) {
						if (partido.negras == null || !partido.negras.isOpen()) {
							partidos.remove(partido);
						} else {
							// Si pierde la conexión le sumamos el tiempo transcurrido desde el último movimiento
							partido.actualizar_cronometro();
							partido.blancas = null;
						}
					} else if (getNombre(sesion).equals(partido.nombre_negras)) {
						if (partido.blancas == null || !partido.blancas.isOpen()) {
							partidos.remove(partido);
							System.out.println(getNombre(sesion) + " borra partido");
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

			if (getNombre(sesion) != null) {
				synchronized (sesiones) {
					sesiones.remove(getNombre(sesion));
					
					for (Session usuario : sesiones.values()) {
						enviar(usuario, "usuario_desconectado", getNombre(sesion));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	public void fin_partido() {
	    double myChanceToWin = 1D / (1D + Math.pow(10D, (getPuntos(partido.negras) - getPuntos(partido.blancas)) / 400D));

	    int ratingDelta = (int) Math.round(32D * ((partido.ganador == COLOR.blancas ? 1 : 0) - myChanceToWin));

	    setPuntos(partido.blancas, getPuntos(partido.blancas) + ratingDelta);
	    setPuntos(partido.negras, getPuntos(partido.negras) - ratingDelta);

	    synchronized (EndpointBase.sesiones) {
	    	for (Session s : sesiones.values()) {
	    		enviar(s, "Actualizar ranking", partido.nombre_blancas + "," + getPuntos(partido.blancas) + "," + partido.nombre_negras + "," + getPuntos(partido.negras));
	    	}
	    }
	}


}