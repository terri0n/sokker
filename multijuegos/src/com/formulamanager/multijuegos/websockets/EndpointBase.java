package com.formulamanager.multijuegos.websockets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import com.formulamanager.multijuegos.dao.JugadoresDao;
import com.formulamanager.multijuegos.idiomas.Idiomas;
import com.formulamanager.multijuegos.util.Util;

public class EndpointBase {
	protected static enum COLOR { blancas, negras;
		public COLOR cambiar() {
			return this == blancas ? negras : blancas;
		}
	}

	public static List<Partido> partidos = new ArrayList<Partido>();
	public static Map<String, Session> sesiones = new HashMap<String, Session>();
	public static Map<String, Jugador> jugadores_en_espera = new HashMap<String, Jugador>();

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
			return getNombre(usuario1).equals(getNombre(usuario2));
		} else {
			return false;
		}
	}

	protected static String getNombre(Session s) {
		Jugador j = (Jugador)s.getUserProperties().get("jugador");
		return j == null ? null : j.nombre;
	}

	protected static Integer getPuntos(Session s) {
		Jugador j = (Jugador)s.getUserProperties().get("jugador");
		return j == null ? null : j.puntos;
	}

	protected static void setPuntos(Session s, Integer puntos) {
		Jugador j = (Jugador)s.getUserProperties().get("jugador");
		j.puntos = puntos;
		j.num_partidos++;
		try {
			JugadoresDao.actualizar(j);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected static void enviar(Session s, String accion, String params) {
		try {
			if (s != null && s.isOpen()) {
System.out.println(getNombre(s) + " -> " + accion + "," + Util.nvl(params));
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
			cerrar_sesion(sesiones.get(j.getNombre()), mensaje);
		}
	}

	public static void cerrar_sesion(Session sesion, String mensaje) {
		if (sesion != null) {
	    	try {
	    		Thread.sleep(500);
			    synchronized (sesiones) {
					if (sesion.isOpen()) {
						sesion.close(new CloseReason(CloseCodes.CLOSED_ABNORMALLY, mensaje));
					}
			    }
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
}
