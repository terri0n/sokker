package com.formulamanager.multijuegos.websockets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import com.formulamanager.multijuegos.dao.JugadoresDao;
import com.formulamanager.multijuegos.entity.Jugador;
import com.formulamanager.multijuegos.idiomas.Idiomas;
import com.formulamanager.multijuegos.util.Util;
import com.google.gson.Gson;

public class EndpointBase {
	protected static enum COLOR { blancas, negras;
		public COLOR cambiar() {
			return this == blancas ? negras : blancas;
		}
	}

	public static List<Partido> partidos = new CopyOnWriteArrayList<>();
	public static Map<String, Session> sesiones = new ConcurrentHashMap<>();
	public static Map<String, Jugador> jugadores_en_espera = new ConcurrentHashMap<>();

	protected Session sesion;
	protected HttpSession httpSession;
	protected Partido partido = null;

	protected Session getRival() {
		if (partido == null) {
			return null;
		} else {
			return partido.getColor(sesion) == COLOR.blancas ? partido.negras : partido.getColor(sesion) == COLOR.negras ? partido.blancas : null;
		}
	}

	protected static boolean equals(Session usuario1, Session usuario2) {
		if (usuario1.isOpen() && usuario2.isOpen()) {
			return getJugador(usuario1).equals(getJugador(usuario2));
		} else {
			return false;
		}
	}

	protected static Jugador getJugador(HttpSession s) {
		return (Jugador) s.getAttribute("jugador");
	}

	protected static Jugador getJugador(Session s) {
		return (Jugador)s.getUserProperties().get("jugador");
	}

	protected static void setPuntos(Session s, Integer puntos) {
		Jugador j = getJugador(s);
		j.puntos = puntos;
		j.num_partidos++;

		try {
			JugadoresDao.actualizar(j);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected static void enviar(Session s, String accion, HashMap<String, Object> hm) {
		enviar(s, accion, new Gson().toJson(hm));
	}

	protected static void enviar(Session s, String accion, String params) {
		try {
			if (s != null && s.isOpen()) {
System.out.println(getJugador(s).nombre + " \t<- " + accion + "," + Util.nvl(params));
				s.getBasicRemote().sendText(accion + "," + Util.nvl(params));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void open(Session sesion, EndpointConfig config) throws IOException {
		this.sesion = sesion;
	    this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
	    if (httpSession == null) {
	    	sesion.close(new CloseReason(CloseCodes.TRY_AGAIN_LATER, "Sesión http no accesible"));
    		return;
	    }
	}

	protected Locale getLocale() {
		Locale locale = (Locale)httpSession.getAttribute("javax.servlet.jsp.jstl.fmt.locale.session");
		return locale == null ? Locale.ENGLISH : locale;
	}

	protected String getParameter(Session sesion, String name) {
		try {
			List<String> lista = sesion.getRequestParameterMap().get(name);
			if (lista.size() > 0) {
				return Util.nnvl(URLDecoder.decode(lista.get(0), "UTF-8").trim());
			} else {
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getTexto(String key, Locale locale) {
		ResourceBundle rb = ResourceBundle.getBundle(Idiomas.APPLICATION_RESOURCES, locale);
		try {
			return rb.getString(key);
		} catch (Exception e) {
e.printStackTrace();
			return "???" + key + "???";
		}
	}

	public String getTexto(String key) {
		return getTexto(key, getLocale());
	}

	public static String getSesiones() {
		return sesiones.size() + " jugador" + (sesiones.size() == 1 ? "" : "es");
	}

	public static void cerrar_sesion(Jugador j, String mensaje) {
		if (j != null) {
			cerrar_sesion(sesiones.get(j.getNombre()), new CloseReason(CloseCodes.CLOSED_ABNORMALLY, mensaje));
		}
	}

	public static void cerrar_sesion(Session sesion, CloseReason close_reason) {
		if (sesion != null) {
	    	try {
	    		Thread.sleep(500);
			    synchronized (sesiones) {
					if (sesion.isOpen()) {
						sesion.close(close_reason);
					}
			    }
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	// Elimina la sesión del websocket cuando caduca la sesión
	public static void sesion_destruida(HttpSession session) {
		if (getJugador(session) != null) {
			cerrar_sesion(sesiones.get(getJugador(session).getNombre()), new CloseReason(CloseCodes.GOING_AWAY, "La sesión ha caducado"));
		}
	}
}
