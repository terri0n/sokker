package com.formulamanager.sokker.tomcat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX.TIPO_FACTORX;
import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.FactorxBO;
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
    public void contextInitialized(ServletContextEvent event)  { 
		System.out.println("Iniciando ServletContextListener...");
		
		SystemUtil.saveRealPath(event.getServletContext());
		
    	new Thread() {
    		@Override
    		public synchronized void run() {
    			boolean seguir = true;
    			
    			do {
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
	    				
	    				borrar_backups_y_logs_antiguos();
	    				actualizar_seleccionadores();
	    				actualizar_factor_x();
//		    			buscar_jugadores();
	    				tareas_miercoles();	// Act. automáticas, backups
	    			} catch (InterruptedException e) {
	    				seguir = false;
	    			}
    			} while (seguir);

    	    	System.out.println("Finalizando ServletContextListener...");
    		}
    	}.start();
    }

//	private String[] obtener_archivos(String carpeta) {
//		File f = new File(AsistenteBO.PATH_BASE + carpeta);
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.DAY_OF_MONTH, -30);
//
//		String[] archivos = f.list(new FilenameFilter() {
//			@Override
//			public boolean accept(File dir, String name) {
//				File f = new File(AsistenteBO.PATH_BASE + carpeta + "/" + name);
//				return f.isFile() && c.getTime().after(new Date(f.lastModified()));
//			}
//		});
//		
//		return archivos;
//	}

	private void borrar_backups_y_logs_antiguos() {
System.out.print("Borrando backups...");
    	try {
    		new Navegador(true, null) {
    			@Override
    			protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
    				// IPs
    				// Restamos una unidad a cada entrada
    				synchronized (SERVLET_ASISTENTE.class) {
	    				HashMap<String, String> ips = Util.leer_hashmap("IPs");
	    				List<String> borrar = new ArrayList<>();
	    				for (String key : ips.keySet()) {
							Integer n = Integer.valueOf(ips.get(key)) - 1;
							if (n <= 0) {
								borrar.add(key);
							} else {
								ips.put(key, n + "");
							}
						}
						
						for (String key : borrar) {
							ips.remove(key);
						}
						
						Util.guardar_hashmap(ips, "IPs");
    				}


    				// Backup
					// Borramos los archivos anteriores al último mes
//    				for (String archivo : obtener_archivos("backup/")) {
//   					File f2 = new File(AsistenteBO.PATH_BACKUP + archivo);
//  					f2.delete();
//    				}
    				
    				// Logs
    				// Borramos las líneas anteriores al último mes
    				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy"); 
    				Calendar c = Calendar.getInstance();
    				c.add(Calendar.DAY_OF_MONTH, -30);

    				for (String archivo : new File(SystemUtil.getVar("path") + "logs/").list()) {
    					String salida = "";
    					try (FileInputStream bis = new FileInputStream(SystemUtil.getVar("path") + "logs/" + archivo)) {
    						BufferedReader br = new BufferedReader(new InputStreamReader(bis));
    				        String linea;
    						while ((linea = br.readLine()) != null) {
    							if (linea.length() > 0) {
    								try {
										if (linea.split(" ")[0].length() > 0) {
	    									Date d = sdf.parse(linea.split(" ")[0]);
											// Si la línea es del último mes, la mantenemos
											if (c.getTime().before(d)) {
												salida += linea + "\r\n";
											}
										}
									} catch (ParseException e) {
//										e.printStackTrace();
									} catch (Exception e) {
										System.out.println(archivo + " - " + e + ": " + linea);										
									}
    							}
    						}
    					}

    					try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(SystemUtil.getVar("path") + "logs/" + archivo, false))) {
    						// Guardamos las líneas filtradas
    						bos.write(salida.getBytes());
    					}
    				}
					
    			}
    		};
		} catch (FailingHttpStatusCodeException | IOException | LoginException | ParseException e) {
			e.printStackTrace();
			try {
				SERVLET_ASISTENTE._log_linea("_CONTEXT_LISTENER", "Error borrando backups: " + e);
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			}
		}
System.out.println("OK");
	}
	
    private void actualizar_factor_x() {
System.out.print("Actualizando Factor X...");
    	try {
    		int[] jornada_actual = new int[1];

    		new Navegador(true, null) {
    			@Override
    			protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
					jornada_actual[0] = obtener_jornada(navegador);
    			}
    		};
    		
    		new Navegador(false, null) {
    			@Override
    			protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		    		FactorxBO.actualizar_todos(TIPO_FACTORX.junior, navegador, jornada_actual[0]);
		    		FactorxBO.actualizar_todos(TIPO_FACTORX.senior, navegador, jornada_actual[0]);
		    		FactorxBO.actualizar_todos(TIPO_FACTORX.internacional, navegador, jornada_actual[0]);
    			}
    		};
		} catch (FailingHttpStatusCodeException | IOException | LoginException | ParseException e) {
			e.printStackTrace();
			try {
				SERVLET_ASISTENTE._log_linea("_CONTEXT_LISTENER", "Error actualizando Factor X: " + e);
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			}
		}
System.out.println("OK");
    }

    public void actualizar_seleccionadores() {
System.out.print("Actualizando seleccionadores...");
    	try {
			new Navegador(true, null) {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
	    			AsistenteBO.actualizar_seleccionadores(navegador);
				}
			};
		} catch (FailingHttpStatusCodeException | IOException | LoginException | ParseException e) {
			e.printStackTrace();
			try {
				SERVLET_ASISTENTE._log_linea("_CONTEXT_LISTENER", "Error actualizando seleccionadores: " + e);
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			}
		}
System.out.println("OK");
    }
    
//    public void buscar_jugadores() {
//    	try {
//    		int[] jornada_actual = new int[1];
//
//    		new Navegador(true, null) {
//    			@Override
//    			protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
//					jornada_actual[0] = obtener_jornada(navegador);
//    			}
//    		};
//
//    		new Navegador(null) {
//				@Override
//				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException {
//					Set<Integer> selecciones = AsistenteBO.buscar_selecciones();
//				
//					for (int tid : selecciones) {
//						List<Jugador> jugadores_transferencias = AsistenteBO.buscar_transferencias(navegador, tid, jornada_actual[0]);
//						System.out.println("Leidos " + jugadores_transferencias.size() + " jugadores de " + tid);
//
//						// Leemos todos los jugadores del país
//						List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, null, false, null/*usuario*/);
//						List<Jugador> jugadores_u21 = AsistenteBO.leer_jugadores(tid + 400, null, false, null/*usuario*/);
//						List<Jugador> jugadores_historico = AsistenteBO.leer_jugadores(tid, null, true, null/*usuario*/);
//						List<Jugador> jugadores_historico_u21 = AsistenteBO.leer_jugadores(tid + 400, null, true, null/*usuario*/);
//
//						for (Jugador j : jugadores_transferencias) {
//							j.setFecha(new Date());
//							j.setTid(tid + j.getEdad() > 21 ? 0 : 400);
//							j.setPais(tid);
//							j.setActualizado(true);
//							j.setJornada(jornada_actual[0]);
//							j.setEn_venta(1);
//
//							List<Jugador> destino;
//
//							// Si el jugador pertenece al equipo, lo actualizamos ah�
//							// Si no, lo actualizamos/insertamos en el hist�rico correspondiente seg�n la edad
//							if (jugadores.contains(j)) {
//								destino = jugadores;
//							} else if (jugadores_u21.contains(j)) {
//								destino = jugadores_u21;
//							} else if (jugadores_historico_u21.contains(j) && j.getEdad() > 21) {
//								// Ha pasado de 21 a�os a 22, lo cambiamos de hist�rico
//								Jugador j2 = jugadores_historico_u21.get(jugadores_historico_u21.indexOf(j));
//								jugadores_historico_u21.remove(j2);
//								jugadores_historico.add(j2);
//								destino = jugadores_historico;
//							} else if (j.getEdad() > 21) {
//								destino = jugadores_historico;
//							} else {
//								destino = jugadores_historico_u21;
//							}
//							
//							if (destino.contains(j)) {
//								Jugador antiguo = destino.get(destino.indexOf(j));
//								j = AsistenteBO.combinar_jugadores(j, antiguo);
//								destino.remove(antiguo);
//							}
//							destino.add(j);
//						}
//
//						// Grabamos todo
//						AsistenteBO.grabar_jugadores(jugadores, tid, jornada_actual[0], false);
//						AsistenteBO.grabar_jugadores(jugadores_u21, tid + 400, jornada_actual[0], false);
//						AsistenteBO.grabar_jugadores(jugadores_historico, tid, jornada_actual[0], true);
//						AsistenteBO.grabar_jugadores(jugadores_historico_u21, tid + 400, jornada_actual[0], true);
//					}
//				}
//			};
//
//		} catch (FailingHttpStatusCodeException | IOException | LoginException e) {
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
//    }

	private void tareas_miercoles() {
		Calendar c = Calendar.getInstance();
		if (c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
			AsistenteBO.actualizacion_automatica();
			AsistenteBO.crear_backups();
			AsistenteBO.enviar_backups();
		}
	}
}
