package com.formulamanager.sokker.tomcat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Application Lifecycle Listener implementation class ServletContextListener
 *
 */
@WebListener
public class ServletContextListener implements javax.servlet.ServletContextListener {
    /**
     * Default constructor. 
     */
    public ServletContextListener() {

    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  { 

    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  { 
		System.out.println("Iniciando ServletContextListener...");
		
    	new Thread() {
    		@Override
    		public synchronized void run() {
    			Calendar c = Calendar.getInstance();
    			long milis = c.getTimeInMillis();
    			c.set(Calendar.HOUR_OF_DAY, 8);
    			c.set(Calendar.MINUTE, 0);
    			c.set(Calendar.SECOND, 0);
    			if (c.getTime().before(new Date())) {
    				c.add(Calendar.DAY_OF_MONTH, 1);
    			}
    			milis = c.getTimeInMillis() - milis;

    			try {
    				Thread.sleep(milis);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}

    			actualizar_seleccionadores();
    			buscar_jugadores();

    			try {
	    			FactorxBO.actualizar_todos(false);
	    			FactorxBO.actualizar_todos(true);
    	    	} catch (Exception e) {
    				e.printStackTrace();
    	    	}

    	    	System.out.println("Finalizando ServletContextListener...");
    		}
    	}.start();
    }

    public void actualizar_seleccionadores() {
    	try {
			new Navegador(true) {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
	    			AsistenteBO.actualizar_seleccionadores(navegador);
				}
			};
		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
    
    public void buscar_jugadores() {
    	try {
			new Navegador() {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
					Set<Integer> selecciones = AsistenteBO.buscar_selecciones();
				
					for (int tid : selecciones) {
						List<Jugador> jugadores_transferencias = AsistenteBO.buscar_transferencias(navegador, tid);
						System.out.println("Leidos " + jugadores_transferencias.size() + " jugadores de " + tid);

						// Leemos todos los jugadores del país
						List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, false);
						List<Jugador> jugadores_u21 = AsistenteBO.leer_jugadores(tid + 400, false);
						List<Jugador> jugadores_historico = AsistenteBO.leer_jugadores(tid, true);
						List<Jugador> jugadores_historico_u21 = AsistenteBO.leer_jugadores(tid + 400, true);

						for (Jugador j : jugadores_transferencias) {
							j.setFecha(new Date());
							j.setTid(tid + j.getEdad() > 21 ? 0 : 400);
							j.setPais(tid);
							j.setActualizado(true);
							j.setJornada(JugadorBO.obtener_jornada());
							j.setEn_venta(1);

							List<Jugador> destino;

							// Si el jugador pertenece al equipo, lo actualizamos ahí
							// Si no, lo actualizamos/insertamos en el histórico correspondiente según la edad
							if (jugadores.contains(j)) {
								destino = jugadores;
							} else if (jugadores_u21.contains(j)) {
								destino = jugadores_u21;
							} else if (jugadores_historico_u21.contains(j) && j.getEdad() > 21) {
								// Ha pasado de 21 años a 22, lo cambiamos de histórico
								Jugador j2 = jugadores_historico_u21.get(jugadores_historico_u21.indexOf(j));
								jugadores_historico_u21.remove(j2);
								jugadores_historico.add(j2);
								destino = jugadores_historico;
							} else if (j.getEdad() > 21) {
								destino = jugadores_historico;
							} else {
								destino = jugadores_historico_u21;
							}
							
							if (destino.contains(j)) {
								Jugador antiguo = destino.get(destino.indexOf(j));
								j = AsistenteBO.combinar_jugadores(j, antiguo);
								destino.remove(antiguo);
							}
							destino.add(j);
						}

						// Grabamos todo
						AsistenteBO.grabar_jugadores(jugadores, tid, false);
						AsistenteBO.grabar_jugadores(jugadores_u21, tid + 400, false);
						AsistenteBO.grabar_jugadores(jugadores_historico, tid, true);
						AsistenteBO.grabar_jugadores(jugadores_historico_u21, tid + 400, true);
					}
				}
			};

		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
	
}
