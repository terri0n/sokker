package com.formulamanager.multijuegos.listeners;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.formulamanager.multijuegos.entity.Jugador;
import com.formulamanager.multijuegos.websockets.EndpointBase;

/**
 * Application Lifecycle Listener implementation class SessionListener
 *
 */
@WebListener
public class SessionListener implements HttpSessionListener {
	public static List<HttpSession> sesiones = new CopyOnWriteArrayList<>();

	/**
     * @see SessionListener#sessionCreated(HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent se)  { 
    	crear_jugador(se.getSession());
    }

	/**
     * @see SessionListener#sessionDestroyed(HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent se)  { 
        Jugador j = (Jugador)se.getSession().getAttribute("jugador");
       	EndpointBase.cerrar_sesion(j, "La sesión ha caducado");
    	
    	synchronized (sesiones) {
        	sesiones.remove(se.getSession());
        	EndpointBase.sesion_destruida(se.getSession());
        }
 	
        if (se.getSession().getAttribute("jugador") != null) {
        	System.out.println("Sale " + ((Jugador)se.getSession().getAttribute("jugador")).getNombre());
        }
    }

    public static void crear_jugador(HttpSession s) {
    	Jugador j = Jugador.getDefault();
        s.setAttribute("jugador", j);
        synchronized (sesiones) {
        	sesiones.add(s);
        }
        System.out.println("Creando a " + j.getNombre());
    }
}
