package com.formulamanager.multijuegos.listeners;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.formulamanager.multijuegos.websockets.EndpointBase;
import com.formulamanager.multijuegos.websockets.Jugador;

/**
 * Application Lifecycle Listener implementation class SessionListener
 *
 */
@WebListener
public class SessionListener implements HttpSessionListener {
	private static int num_invitados = 0;
	public static List<HttpSession> sesiones = new ArrayList<>();

	/**
     * @see SessionListener#sessionCreated(HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent se)  { 
    	Jugador j = new Jugador("_Guest" + ++num_invitados + "_", null, 1600, 0, null, null, true);
        se.getSession().setAttribute("jugador", j);
        synchronized (sesiones) {
        	sesiones.add(se.getSession());
        }
        System.out.println("Entra " + j.getNombre());
    }

	/**
     * @see SessionListener#sessionDestroyed(HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent se)  { 
        Jugador j = (Jugador)se.getSession().getAttribute("jugador");
       	EndpointBase.cerrar_sesion(j, "La sesión ha caducado");
    	
    	synchronized (sesiones) {
        	sesiones.remove(se.getSession());
        }
        if (se.getSession().getAttribute("jugador") != null) {
        	System.out.println("Sale " + ((Jugador)se.getSession().getAttribute("jugador")).getNombre());
        }
    }
}
