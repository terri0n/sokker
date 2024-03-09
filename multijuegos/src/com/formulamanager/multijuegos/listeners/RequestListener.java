package com.formulamanager.multijuegos.listeners;

import java.sql.SQLException;
import java.text.ParseException;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

import com.formulamanager.multijuegos.entity.Jugador;
import com.formulamanager.multijuegos.servlets.ServletLogin;
import com.formulamanager.multijuegos.util.Util;

@WebListener
/**
 * GetHttpSessionConfigurator lanza un NPE porque httpSession es null. Esto lo soluciona:
 * https://stackoverflow.com/questions/20240591/websocket-httpsession-returns-null
 * 
 * @author Levi
 *
 */
public class RequestListener implements ServletRequestListener {
	@Override
    public void requestInitialized(ServletRequestEvent sre)  { 
        //((HttpServletRequest) sre.getServletRequest()).getSession();
		HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        
		// Si el usuario no ha hecho login, compruebo si tiene una cookie guardada y hago login
        Jugador j = (Jugador) request.getSession().getAttribute("jugador");
    	if (j != null && j.invitado) {
    		String usuario = Util.getCookie(request, "usuario");
			String contrasenya = Util.getCookie(request, "contrasenya");
			if (usuario != null) {
				try {
					ServletLogin.login(request, usuario, contrasenya);
				} catch (SQLException | ParseException e) {
					e.printStackTrace();
				}
			}
    	}
    }

	@Override
	public void requestDestroyed(ServletRequestEvent arg0)  { 
         // TODO Auto-generated method stub
    }
}