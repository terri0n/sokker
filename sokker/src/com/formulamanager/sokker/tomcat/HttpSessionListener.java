package com.formulamanager.sokker.tomcat;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;

/**
 * Application Lifecycle Listener implementation class HttpSessionListener
 *
 */
@WebListener
public class HttpSessionListener implements javax.servlet.http.HttpSessionListener {
    private static int activeSessions = 0;

    public synchronized void sessionCreated(HttpSessionEvent se) {
      activeSessions++;
    }

    public synchronized void sessionDestroyed(HttpSessionEvent se) {
      if(activeSessions > 0)
        activeSessions--;
      }

    public static synchronized int getActiveSessions() {
       return activeSessions;
    }
	
}
