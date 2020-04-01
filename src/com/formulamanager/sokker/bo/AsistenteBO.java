package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION_ASISTENTE;
import com.formulamanager.sokker.entity.Juvenil;
import com.formulamanager.sokker.entity.Pais;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class AsistenteBO extends JugadorBO {
	public static String PATH_BASE = System.getProperty("os.name").contains("Windows") ? "d:\\home\\asistente\\" : "/home/asistente/";
	public static String PATH_BACKUP = PATH_BASE + "backup/";
	public static int ID_ARCADE = 51530;
	public static int ID_ARCADE_CUP = 51531;
	public enum TIPO_ENTRENADOR { PRINCIPAL, ASISTENTE, JUVENILES, OTRO }	// Empieza en 1
	
	public static List<Jugador> leer_jugadores (Integer tid, boolean historico) {
		List<Jugador> jugadores = new ArrayList<Jugador>();
		Properties prop = new Properties();
		InputStream input = null;
	
		try {
			String BD = PATH_BASE + tid + (historico ? "_historico" : "") + ".properties";
			
			File file = new File(BD);
			if (!file.exists()) {
				return jugadores;
			}
			
			input = new FileInputStream(file);
	
			// load a properties file
			prop.load(input);
	
			Set<String> keys = prop.stringPropertyNames();
			for (String key : keys) {
				String linea = prop.getProperty(key);
				Jugador j = new Jugador(Integer.valueOf(key), Arrays.asList(linea.split(",")), true, null);
				jugadores.add(j);
		    }
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return jugadores;
	}

	// Obtiene los valores públicos de un jugador
	public static Jugador obtener_jugador(Integer pid, boolean nt, WebClient navegador) {
		try {
			XmlPage pagina = navegador.getPage("http://sokker.org/xml/player-" + pid + ".xml");
		
			DomNode domNodeN = pagina.getFirstByXPath("player/name/text()");
			String nombre = domNodeN == null ? null : ((DomText) domNodeN).asText();
			DomNode domNodeA = pagina.getFirstByXPath("player/surname/text()");
			String apellido = domNodeA == null ? null : ((DomText) domNodeA).asText();
			Integer edad = new Integer(((DomText) pagina.getFirstByXPath("player/age/text()")).asText());
			Integer valor = new Integer(((DomText) pagina.getFirstByXPath("player/value/text()")).asText()) / 4;
			Integer tid = new Integer(((DomText) pagina.getFirstByXPath("player/teamID/text()")).asText());

			Jugador j = new Jugador(pid, nombre + " " + apellido, edad, valor, tid);
			
			j.setJornada(JugadorBO.obtener_jornada(navegador));
			j.setForma(new Integer(((DomText) pagina.getFirstByXPath("player/skillForm/text()")).asText()));
			j.setPais(new Integer(((DomText) pagina.getFirstByXPath("player/countryID/text()")).asText()));
			j.setFecha(new Date());
			j.setActualizado(true); // Actualizado automáticamente
			j.setTarjetas(new Integer(((DomText) pagina.getFirstByXPath(nt ? "player/ntCards/text()" : "player/cards/text()")).asText()));
			j.setNt(new Integer(((DomText) pagina.getFirstByXPath("player/national/text()")).asText()));
			j.setLesion(new Integer(((DomText) pagina.getFirstByXPath("player/injuryDays/text()")).asText()));
			j.setEn_venta(new Integer(((DomText) pagina.getFirstByXPath("player/transferList/text()")).asText()));

			if (j.getEn_venta() > 0) {
				new Navegador(false) {
					@Override
					protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
						actualizar_jugador(navegador, j, true);
					}
				};
			}

			return j;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<Jugador> obtener_jugadores(Integer tid, WebClient navegador) {
		ArrayList<Jugador> lista = new ArrayList<Jugador>();
		int jornada = JugadorBO.obtener_jornada(navegador);

		try {
			XmlPage pagina = navegador.getPage("http://sokker.org/xml/players-" + tid + ".xml");

			ArrayList<DomNode> jugadores = (ArrayList<DomNode>) pagina.getByXPath("//player");
			for (DomNode jugador : jugadores) {
				Integer id = new Integer(((DomText) jugador.getFirstByXPath("ID/text()")).asText());
				DomNode domNodeN = jugador.getFirstByXPath("name/text()");
				String nombre = domNodeN == null ? null : ((DomText) domNodeN).asText();
				DomNode domNodeA = jugador.getFirstByXPath("surname/text()");
				String apellido = domNodeA == null ? null : ((DomText) domNodeA).asText();
				Integer edad = new Integer(((DomText) jugador.getFirstByXPath("age/text()")).asText());
				Integer valor = new Integer(((DomText) jugador.getFirstByXPath("value/text()")).asText()) / 4;

				Jugador j = new Jugador(id, nombre + " " + apellido, edad, valor, tid);

				j.setJornada(jornada);
				j.setForma(new Integer(((DomText) jugador.getFirstByXPath("skillForm/text()")).asText()));
				j.setPais(new Integer(((DomText) jugador.getFirstByXPath("countryID/text()")).asText()));
				j.setFecha(new Date());
				j.setTarjetas(new Integer(((DomText) jugador.getFirstByXPath(tid < 1000 ? "ntCards/text()" : "cards/text()")).asText()));
				j.setNt(new Integer(((DomText) jugador.getFirstByXPath("national/text()")).asText()));
				j.setLesion(new Integer(((DomText) jugador.getFirstByXPath("injuryDays/text()")).asText()));
				j.setEn_venta(new Integer(((DomText) jugador.getFirstByXPath("transferList/text()")).asText()));
				
				// NTDB
				j.setDisciplina_tactica(new Integer(((DomText) jugador.getFirstByXPath("skillDiscipline/text()")).asText()));
				j.setValor_original(valor * 4);

				if (j.getEn_venta() > 0) {
					new Navegador(false) {
						@Override
						protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
							actualizar_jugador(navegador, j, true);
							j.setActualizado(true); // Actualizado automáticamente
						}
					};
				} else {
					try {
						j.setCondicion(new Integer(((DomText) jugador.getFirstByXPath("skillStamina/text()")).asText()));
						j.setRapidez(new Integer(((DomText) jugador.getFirstByXPath("skillPace/text()")).asText()));
						j.setTecnica(new Integer(((DomText) jugador.getFirstByXPath("skillTechnique/text()")).asText()));
						j.setPases(new Integer(((DomText) jugador.getFirstByXPath("skillPassing/text()")).asText()));
						j.setPorteria(new Integer(((DomText) jugador.getFirstByXPath("skillKeeper/text()")).asText()));
						j.setDefensa(new Integer(((DomText) jugador.getFirstByXPath("skillDefending/text()")).asText()));
						j.setCreacion(new Integer(((DomText) jugador.getFirstByXPath("skillPlaymaking/text()")).asText()));
						j.setAnotacion(new Integer(((DomText) jugador.getFirstByXPath("skillScoring/text()")).asText()));
						j.setActualizado(true); // Actualizado automáticamente
					} catch (Exception e) {
						// En las NTs no tendremos las habilidades
					}
				}
				
				lista.add(j);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	public static void grabar_jugadores(List<Jugador> jugadores, int tid, boolean historico) throws IOException {
		Properties prop = new Properties();
		
		for (Jugador j : jugadores) {
			prop.setProperty(j.getPid()+"", j.serializar(true));
		}
	
		String ruta = PATH_BASE + tid + (historico ? "_historico" : "") + ".properties";
		Util.guardar_properties(prop, ruta);

		ruta = PATH_BACKUP + tid + "_" + obtener_jornada() + (historico ? "_historico" : "") + ".properties";
		Util.guardar_properties(prop, ruta);
	}

	public static void grabar_juveniles(List<Juvenil> juveniles, int tid, boolean historico) throws IOException {
		Properties prop = new Properties();
		
		for (Jugador j : juveniles) {
			prop.setProperty(j.getPid()+"", j.serializar(true));
		}
	
		String ruta = PATH_BASE + tid + (historico ? "_historico" : "") + "_juveniles.properties";
		Util.guardar_properties(prop, ruta);

		ruta = PATH_BACKUP + tid + "_" + obtener_jornada() + (historico ? "_historico" : "") + "_juveniles.properties";
		Util.guardar_properties(prop, ruta);
	}
	
	public static HashMap<String, Integer> obtener_entrenamiento(int tid, int ultima_jornada, List<Jugador> jugadores, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HashMap<String, Integer> minutos = new HashMap<String, Integer>();
		int jornada = obtener_jornada(navegador);
		
		if (tid > 1000) {
			// Comprobamos si el jugador ha jugado un partido internacional
			for (Jugador j : jugadores) {
				// Si pertenece a la selección
				if (j.getNt() > 0) {
					List<Jugador> jugadores_nt = new ArrayList<Jugador>();
					jugadores_nt.add(j);
					HashMap<String, Integer> minutos_nt = obtener_entrenamiento(j.getPais() + (j.getNt() == 1 ? 0 : 400), ultima_jornada, jugadores_nt, navegador);
					
					for (String key : minutos_nt.keySet()) {
						minutos.put(key, minutos_nt.get(key));
					}
				}
			}
		}
		
		XmlPage pagina = Navegador.getXmlPage(navegador, "http://sokker.org/xml/matches-team-" + tid + ".xml");
		ArrayList<DomNode> partidos = (ArrayList<DomNode>) pagina.getByXPath("//match");
		
		for (DomNode partido : partidos) {
			int week = JugadorBO.obtener_jornada(partido);

			if (week >= ultima_jornada - 1 && week <= jornada) {
				if (((DomText) partido.getFirstByXPath("isFinished/text()")).asText().equals("1")) {
					Integer mid = new Integer(((DomText) partido.getFirstByXPath("matchID/text()")).asText());
					pagina = Navegador.getXmlPage(navegador, "http://sokker.org/xml/match-" + mid + ".xml");

					int leagueID = new Integer(((DomText) pagina.getFirstByXPath("//info/leagueID/text()")).asText());

					if (leagueID != ID_ARCADE && leagueID != ID_ARCADE_CUP) {
						for (Jugador j : jugadores) {
							DomNode nodoJugador = pagina.getFirstByXPath("//playerStats[playerID='" + j.getPid() + "']");
							if (nodoJugador != null) {
								int number = new Integer(((DomText) nodoJugador.getFirstByXPath("number/text()")).asText());
//								int timePlaying = new Integer(((DomText) nodoJugador.getFirstByXPath("timePlaying/text()")).asText());
//								int timeDefending = new Integer(((DomText) nodoJugador.getFirstByXPath("timeDefending/text()")).asText());
								int timeIn = new Integer(((DomText) nodoJugador.getFirstByXPath("timeIn/text()")).asText());
								int timeOut = new Integer(((DomText) nodoJugador.getFirstByXPath("timeOut/text()")).asText());
								DEMARCACION demarcacion = DEMARCACION.values()[new Integer(((DomText) nodoJugador.getFirstByXPath("formation/text()")).asText())];
//								int rating = new Integer(((DomText) nodoJugador.getFirstByXPath("rating/text()")).asText());
								
								// Si el jugador aún no tiene una demarcación asignada en el asistente, le ponemos esta por defecto
								if (j.getDemarcacion() == null) {
									j.setDemarcacion(demarcacion);
								}
								
								if (number <= 11 || timeIn > 0) {
									// Nos situamos en la jornada correspondiente para colocar la demarcación en el sitio adecuado
									Jugador j2 = j.buscar_jornada(week);

									if (j2 != null) {
										j2.setDemarcacion_entrenamiento(demarcacion);
									}
									String clave = j.getPid() + "_" + week + "_" + demarcacion.name();
	
									// Guardamos los minutos jugados con cada demarcación por separado
									float porcentaje_entrenamiento = obtener_porcentaje_entrenamiento_liga(tid, leagueID, navegador);
									int tiempo_total = timeIn >= 90 ? 0 : Util.invl(minutos.get(clave)) + (int) (((timeOut == 0 || timeOut > 89 ? 89 : timeOut) - timeIn + 1) * porcentaje_entrenamiento / 100);
	
									minutos.put(clave, tiempo_total);
								}
							}
						}
					}
				}
			}
		}
		
		return minutos;
	}

	private static float obtener_porcentaje_entrenamiento_liga(int tid, int leagueID, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		if (tid < 1000) {			// NT
			return 50;
		} else {
			switch (leagueID) {
				case 3:				// Amistoso
				case 200:			// Amistoso copa
					return 75f*100/90;
				case 39933:			// Champions Cup
					return 100;
				case 39934:			// Amistoso NT
					return 50;
				case 51530:			// Arcade
				case 51531:			// Arcade Cup
					return 0;
				default:
					XmlPage pagina = Navegador.getXmlPage(navegador, "http://sokker.org/xml/league-" + leagueID + ".xml");
					int tipo = new Integer(((DomText) pagina.getFirstByXPath("league/info/type/text()")).asText());
					boolean oficial = new Integer(((DomText) pagina.getFirstByXPath("league/info/isOfficial/text()")).asText()) > 0;
					
					if (tipo == 0 && oficial	// Liga
							|| tipo == 1		// Copa
							|| tipo == 2		// Promoción
							|| tipo == 9) {		// Champions Cup
						return 100;
					} else if (tipo == 0 && !oficial) {
						return 75f*100/90;
					} else if (tipo == 7) {		// Juniors
						return 0;
					}
	
					throw new RuntimeException("Liga " + leagueID + ", tipo " + tipo + " desconocido");
			}
		}
	}

	public static Jugador combinar_jugadores(Jugador nuevo, Jugador historico) {
		if (!nuevo.getJornada().equals(historico.getJornada())) {
			if (nuevo.getCondicion() != null) {
				// Si tiene valores y los valores públicos han cambiado, mantengo la demarcación antigua y las notas
				nuevo.setDemarcacion(historico.getDemarcacion());
				nuevo.setNotas(historico.getNotas());
				nuevo.anyadir_original(historico);
				
				return nuevo;
			} else {
				// Si no, solo actualizo los valores públicos
				Jugador copia = new Jugador(historico);
				copia.copiar_valores_publicos(historico);
				copia.setOriginal(historico.getOriginal());
				historico.setOriginal(copia);
				historico.copiar_valores_publicos(nuevo);

				return historico;
			}
		} else if (nuevo.getCondicion() != null) {
			// Si el nuevo tiene habilidades pero no ha variado el valor ni la forma, solo actualizamos las habilidades
			nuevo.setDemarcacion(historico.getDemarcacion());
			nuevo.setNotas(historico.getNotas());
			nuevo.setOriginal(historico.getOriginal());
			
			return nuevo;
		} else {
			// Solo actualizo tarjetas, nt, lesion, en_venta 
			historico.copiar_valores_publicos(nuevo);

			return historico;
		}
	}
	
	public static Juvenil combinar_juveniles(Juvenil nuevo, Juvenil historico) {
		if (!nuevo.getJornada().equals(historico.getJornada())) {
			nuevo.setOriginal(historico);
			return nuevo;
		} else {
			return historico;
		}
	}

	public static List<Jugador> actualizar_equipo(String ilogin, String ipassword, Usuario _usuario) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		final List<Jugador> jugadores_actualizados = new ArrayList<Jugador>();

		new Navegador(true, ilogin, ipassword) {
			@Override
			protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
				int tid = _usuario.getDef_tid();
				
				List<Jugador> jugadores_nuevos = AsistenteBO.obtener_jugadores(tid, navegador);
				List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, false);
				List<Jugador> jugadores_historico = AsistenteBO.leer_jugadores(tid, true);

				// Añado o actualizo jugadores
				for (Jugador nuevo : jugadores_nuevos) {
					Jugador anyadir = nuevo;
					if (jugadores != null) {
						if (jugadores.contains(nuevo)) {
							Jugador j = jugadores.get(jugadores.indexOf(nuevo));
							anyadir = combinar_jugadores(nuevo, j);
						}
					}

					jugadores_actualizados.add(anyadir);
				}
				
				// Jugadores no pertenecientes al equipo
				for (Jugador j : jugadores) {
					if (!jugadores_nuevos.contains(j)) {
						Jugador actualilzado = AsistenteBO.obtener_jugador(j.getPid(), tid < 1000, navegador);
						j.copiar_valores_publicos(actualilzado);

						if (tid < 1000) {
							// Actualizo a los jugadores no seleccionados actualmente
							jugadores_actualizados.add(j);
						} else {
							// Archivo los jugadores desaparecidos
							jugadores_historico.add(j);
						}
					}
				}

				// Entrenamiento
				HashMap<String, Integer> minutos = AsistenteBO.obtener_entrenamiento(tid, _usuario.getDef_jornada(), jugadores_actualizados, navegador);

				// Asignamos los minutos jugados a cada jugador en la última posición
				for (Jugador j : jugadores_actualizados) {
					Jugador jugador = j;
					do {
						if (jugador.getDemarcacion_entrenamiento() != null) {
							Integer min = minutos.get(jugador.getPid() + "_" + jugador.getJornada() + "_" + jugador.getDemarcacion_entrenamiento().name());
							if (min != null) {
								jugador.setMinutos(min);
							}
						}
						jugador = jugador.getOriginal();
					} while (jugador != null && jugador.getJornada().intValue() >= _usuario.getDef_jornada().intValue());
				}

				// Repesca
				for (Jugador j : jugadores_actualizados) {
					// Si el jugador está en el histórico, lo repescamos
					if (jugadores_historico.contains(j)) {
						j.setOriginal(jugadores_historico.get(jugadores_historico.indexOf(j)));
						jugadores_historico.remove(j);
					}
				}
				
				// Actualizamos jornada, país y tipo de entrenamiento
				int jornada_anterior = _usuario.getDef_jornada();
				EquipoBO.obtener_datos_equipo(_usuario, jornada_anterior, navegador);
				
				// Actualizamos entrenadores
				AsistenteBO.leer_entrenadores(_usuario, jornada_anterior, navegador);

				// Juveniles
				List<Juvenil> juveniles_nuevos = obtener_juveniles(navegador);
				List<Juvenil> juveniles = leer_juveniles(tid, false);
				List<Juvenil> juveniles_historico = leer_juveniles(tid, true);
				List<Juvenil> juveniles_actualizados = new ArrayList<Juvenil>();

				// Añado o actualizo juveniles
				for (Juvenil nuevo : juveniles_nuevos) {
					Juvenil anyadir = nuevo;
					if (juveniles != null) {
						if (juveniles.contains(nuevo)) {
							Juvenil j = juveniles.get(juveniles.indexOf(nuevo));
							anyadir = combinar_juveniles(nuevo, j);
						}
					}

					juveniles_actualizados.add(anyadir);
				}
				
				// Archivamos los juveniles desaparecidos
				juveniles.removeAll(juveniles_actualizados);
				juveniles_historico.addAll(juveniles);
				
				// Guardamos jugadores y usuario
				Collections.sort(jugadores_actualizados, Jugador.getComparator());
				grabar_jugadores(jugadores_actualizados, tid, false);
				grabar_jugadores(jugadores_historico, tid, true);
				grabar_juveniles(juveniles_actualizados, tid, false);
				grabar_juveniles(juveniles_historico, tid, true);
				UsuarioBO.grabar_usuario(_usuario);
				
				calcular(jugadores_actualizados, _usuario);
				
				// Envío jugadores a las NTDB
				if (tid > 1000 && _usuario.isNtdb()) {
					NtdbBO.enviar_jugadores(navegador, _usuario, jugadores_nuevos);
				}
			}
		};
		
		return jugadores_actualizados;
	}

	public static List<Juvenil> leer_juveniles(int tid, boolean historico) {
		List<Juvenil> juveniles = new ArrayList<Juvenil>();
		Properties prop = new Properties();
		InputStream input = null;
	
		try {
			String BD = PATH_BASE + tid + (historico ? "_historico" : "") + "_juveniles.properties";
			
			File file = new File(BD);
			if (!file.exists()) {
				return juveniles;
			}
			
			input = new FileInputStream(file);
	
			// load a properties file
			prop.load(input);
	
			Set<String> keys = prop.stringPropertyNames();
			for (String key : keys) {
				String linea = prop.getProperty(key);
				Juvenil j = new Juvenil(Integer.valueOf(key), Arrays.asList(linea.split(",")), true, null);
				juveniles.add(j);
		    }
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return juveniles;
	}

	protected static List<Juvenil> obtener_juveniles(WebClient navegador) {
		int jornada = obtener_jornada();
		List<Juvenil> juveniles = new ArrayList<Juvenil>();

		try {
			XmlPage pagina = navegador.getPage("http://sokker.org/xml/juniors.xml");

			ArrayList<DomNode> juniors = (ArrayList<DomNode>) pagina.getByXPath("//junior");
			for (DomNode junior : juniors) {
				int pid = new Integer(((DomText) junior.getFirstByXPath("ID/text()")).asText());
				String nombre = ((DomText) junior.getFirstByXPath("name/text()")).asText()
						+ " " + ((DomText) junior.getFirstByXPath("surname/text()")).asText();
				int edad = new Integer(((DomText) junior.getFirstByXPath("age/text()")).asText());
				int skill = new Integer(((DomText) junior.getFirstByXPath("skill/text()")).asText());
				int weeks = new Integer(((DomText) junior.getFirstByXPath("weeks/text()")).asText());
				boolean formation = Util.stringToBoolean(((DomText) junior.getFirstByXPath("formation/text()")).asText());
				
				Juvenil j = new Juvenil(pid, nombre, jornada, edad, skill, weeks, formation);
				juveniles.add(j);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return juveniles;
	}

	public static void calcular(List<Jugador> jugadores, Usuario usuario) {
		if (usuario.getDef_tid() > 1000) {
			for (Jugador j : jugadores) {
				j.calcular_entrenamiento(usuario);
				j.calcular_talento();
			}
		}
	}
	
	public static String listar_usuarios() {
		String[] archivos = UsuarioBO.obtener_usuarios();

		String salida = "Total: " + archivos.length + "<br/>"
				+ "<label id='' for='reset_intentos'><input type='checkbox' id='reset_intentos' /> Resetear</label><br/>";
		
		Arrays.sort(archivos);
		
		for (String s : archivos) {
			File fUsuario = new File(PATH_BASE + "/" + s);
			Date fecha = new Date(fUsuario.lastModified());
			long tamanyo = fUsuario.length();
			
			String usuario = s.split(".properties")[0].substring(1);
			salida += Util.dateToString(fecha) + " <a href='javascript:void(0)' onclick=\"location.href='asistente/login_como?usuario=" + usuario + "&reset_intentos=' + $('#reset_intentos').val()\">" + usuario + "</a> " + NumberFormat.getInstance().format(tamanyo) + "<br/>";
		}

		return salida;
	}

	public static void codificar_passwords() throws IOException {
		File f = new File(PATH_BASE);

		String[] archivos = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("_");
			}
		});
		
		for (String s : archivos) {
			String str_usuario = s.split(".properties")[0].substring(1);
			if (!str_usuario.equals("terrion")) {
				Usuario usuario = UsuarioBO.leer_usuario(str_usuario);
				
				usuario.setPassword(Util.getMD5(usuario.getPassword()));
				UsuarioBO.grabar_usuario(usuario);
			}
		}
	}
	
	public static Set<Integer> buscar_selecciones() {
		File f = new File(PATH_BASE);
		Set<Integer> selecciones = new HashSet<Integer>();

		String[] archivos = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				try {
					return Util.stringToInteger(name.split("\\.")[0]) < 1000;
				} catch (Exception e) {
					return false;
				}
			}
		});
		
		for (String s : archivos) {
			Integer id_pais = Integer.valueOf(s.split("\\.")[0]);
			selecciones.add(id_pais > 400 ? id_pais - 400 : id_pais);
		}

		return selecciones;
	}
	
	public static List<Jugador> buscar_transferencias(WebClient navegador, int tid) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage pagina = navegador.getPage("http://sokker.org/transfers");
		HtmlForm form = pagina.getFormByName("searchform");
		form.getSelectByName("nationality").setSelectedAttribute(tid+"", true);
		HtmlButton botonFormulario = form.getFirstByXPath("//button[@type='submit']");
		HtmlPage p = botonFormulario.click();

		// Primero obtengo los PIDs
		List<Integer> pids = new ArrayList<Integer>();
		List<HtmlDivision> playerCells = (List<HtmlDivision>) p.getByXPath("//div[@id='playerCell']");
		for (HtmlDivision playerCell : playerCells) {
			HtmlAnchor a = (HtmlAnchor)playerCell.getFirstByXPath("div//a[contains(@href,'player/PID/')]");
			Integer pid = Integer.valueOf(a.getHrefAttribute().split("/")[2]);
			pids.add(pid);
		}

		// TODO: pasar de página

		List<Jugador> jugadores = new ArrayList<Jugador>();

		// Y luego leo el jugador entero
		// NOTA: En la lista de transferencias no aparece la forma
		new Navegador(true) {
			@Override
			protected void execute(WebClient navegador)	throws FailingHttpStatusCodeException, MalformedURLException, IOException {
				for (Integer pid : pids) {
					jugadores.add(obtener_jugador(pid, true, navegador));
				}
			}
		};

		return jugadores;
	}
	
	
	public static void enviar_skmail(WebClient navegador, String destinatario, String asunto, String texto) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		try {
			StringWebResponse response = new StringWebResponse(getFormSkmail(destinatario, asunto, texto), new URL("file:///."));
			HtmlPage form = HTMLParser.parseHtml(response, navegador.getCurrentWindow());

			HtmlSubmitInput botonFormulario = form.getFormByName("form").getFirstByXPath("//input[@type='submit']");
			HtmlPage p = botonFormulario.click();
//System.out.println(p.asXml());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getFormSkmail(String destinatario, String asunto, String texto) {
		return "<form name='form' action='http://sokker.org/mailbox/sent.php' method='post'>"
					+ "<input type='hidden' name='typeto' value='login'/>"
					+ "<input type='text' name='send_to' value='" + destinatario + "'/>"
					+ "<input type='hidden' name='mailtype' value='sokker'/>"
					+ "<input type='text' name='title' id='title' value='" + asunto + "' />"
					+ "<textarea name='text'>" + texto + "</textarea>"
					+ "<input type='submit' />"
				+ "</form>";
	}
	
	public static void leer_entrenadores(Usuario usuario, int jornada_anterior, WebClient navegador) {
		if (usuario.getDef_tid() > 1000) {
			try {
				XmlPage pagina = Navegador.getXmlPage(navegador, "http://sokker.org/xml/trainers.xml");
				ArrayList<DomNode> entrenadores = (ArrayList<DomNode>) pagina.getByXPath("//trainer");
				
				List<BigDecimal> nivel_asistentes = new ArrayList<>();
				
				for (DomNode entrenador : entrenadores) {
					TIPO_ENTRENADOR tipo = TIPO_ENTRENADOR.values()[new Integer(((DomText) entrenador.getFirstByXPath("job/text()")).asText()) - 1];
					int condicion = new Integer(((DomText) entrenador.getFirstByXPath("skillStamina/text()")).asText());
					int rapidez = new Integer(((DomText) entrenador.getFirstByXPath("skillPace/text()")).asText());
					int tecnica = new Integer(((DomText) entrenador.getFirstByXPath("skillTechnique/text()")).asText());
					int pases = new Integer(((DomText) entrenador.getFirstByXPath("skillPassing/text()")).asText());
					int porteria = new Integer(((DomText) entrenador.getFirstByXPath("skillKeeper/text()")).asText());
					int defensa = new Integer(((DomText) entrenador.getFirstByXPath("skillDefending/text()")).asText());
					int creacion = new Integer(((DomText) entrenador.getFirstByXPath("skillPlaymaking/text()")).asText());
					int anotacion = new Integer(((DomText) entrenador.getFirstByXPath("skillScoring/text()")).asText());
					int media = new Integer(((DomText) entrenador.getFirstByXPath("skillCoach/text()")).asText());
					
					Jugador j = new Jugador();
					j.setCondicion(condicion);
					j.setRapidez(rapidez);
					j.setTecnica(tecnica);
					j.setPases(pases);
					j.setPorteria(porteria);
					j.setDefensa(defensa);
					j.setCreacion(creacion);
					j.setAnotacion(anotacion);
					j.setForma(media);
					
					switch (tipo) {
						case PRINCIPAL:
							usuario.getEntrenador_principal().put(usuario.getDef_jornada(), j);
							break;
						case ASISTENTE:
							nivel_asistentes.add(j.getNivel_entrenador());
							break;
						case JUVENILES:
							usuario.getNivel_juveniles().put(usuario.getDef_jornada(), j.getNivel_entrenador());
							break;
						case OTRO:
					}
				}
				
				usuario.getNivel_asistentes().put(usuario.getDef_jornada(), nivel_asistentes.size() == 0 ? BigDecimal.ZERO : Util.sumar_bd(nivel_asistentes).divide(new BigDecimal(3), MathContext.DECIMAL32));
				
				// Actualizamos las jornadas anteriores desde la última actualización si no estaban establecidas
				for (int i = jornada_anterior; i < usuario.getDef_jornada(); i++) {
					if (usuario.getEntrenador_principal().get(i) == null) {
						usuario.getEntrenador_principal().put(i, usuario.getEntrenador_principal().get(usuario.getDef_jornada()));
						usuario.getNivel_asistentes().put(i, usuario.getNivel_asistentes().get(usuario.getDef_jornada()));
						usuario.getNivel_juveniles().put(i, usuario.getNivel_juveniles().get(usuario.getDef_jornada()));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void calcular_juveniles(List<Juvenil> juveniles) {
		for (Juvenil j : juveniles) {
			j.calcular_niveles();
		}
	}

	// funcion y = ax+b
	
	// posición[0] = pendiente de la recta (a)
	// posición[1] = término independiente (b)
	// posición[2] = correlación (grado de unión de -1 a 1)
    public static double[] calcRectaRegresionYsobreX(int[] lasX, int[] lasY) {
        double[] retVal = new double[3];
        double mediaX = calcMedia(lasX);
        double mediaY = calcMedia(lasY);
        double varianzaX = (calcMediaDeLosCuadrados(lasX) - Math.pow(mediaX, 2));
        double varianzaY = (calcMediaDeLosCuadrados(lasY) - Math.pow(mediaY, 2));
        double covarianza = calcMediaDeLosProductos(lasX, lasY) - (mediaX * mediaY);
        double diviCovaX = covarianza / varianzaX;

        retVal[0] = diviCovaX;                     
        // aqui devuelve la pendiente de la recta
        retVal[1] = -(diviCovaX * mediaX) + mediaY;   
        // aqui devuelve el parametro independiente
        if ((Math.sqrt(varianzaX) * Math.sqrt(varianzaY))==0){
            retVal[2]=1;
        } else {
            retVal[2] = covarianza / (Math.sqrt(varianzaX) * Math.sqrt(varianzaY)); // esto es la correlacion r
        }
        return retVal;
    }

    // funcion x= ay + b
    public static double[] calcRectaRegresionXsobreY(int[] lasX, int[] lasY) {
        double[] retVal = new double[3];
        double mediaX = calcMedia(lasX);
        double mediaY = calcMedia(lasY);
        double varianzaX = (calcMediaDeLosCuadrados(lasX) - Math.pow(mediaX, 2));
        double varianzaY = (calcMediaDeLosCuadrados(lasY) - Math.pow(mediaY, 2));
        double covarianza = calcMediaDeLosProductos(lasX, lasY) - (mediaX * mediaY);
        double diviCovaY = covarianza / varianzaY;

        retVal[0] = diviCovaY;                     
        // aqui devuelve la pendiente de la recta
        retVal[1] = -(diviCovaY * mediaY) + mediaX;   
        // aqui devuelve el parametro independiente
        retVal[2] = covarianza / (Math.sqrt(varianzaX) * Math.sqrt(varianzaY)); // esta es la correlacion r
        return retVal;
    }

    public static double calcMedia(int[] valores) {
        double retVal = 0;
        for (int i = 0; i < valores.length; i++) {
            retVal += valores[i];
        }
        return retVal / valores.length;
    }

    public static double calcMediaDeLosCuadrados(int[] valores) {
        double retVal = 0;
        for (int i = 0; i < valores.length; i++) {
            retVal += Math.pow(valores[i], 2);
        }
        return retVal / valores.length;
    }

    public static double calcMediaDeLosProductos(int[] valores1, int[] valores2) {
        double retVal = 0;
        for (int i = 0; i < valores1.length; i++) {
            retVal += valores1[i] * valores2[i];
        }
        return retVal / valores1.length;
    }

    /**
     * https://es.wikipedia.org/wiki/Regresión_lineal
     * y = b * x + a
     * @param juveniles
     * @return new Float[] {b, a}
     */
    public static float[] regresion_lineal(List<Juvenil> juveniles) {
    	int cantidad = juveniles.size();
    	int x = 0;
    	int y = 0;
    	int xy = 0;
    	int xx = 0;
    	
    	// Empiezo en 1 porque el resultado aparece desplazado una unidad, no sé por qué :?
    	for (int i = 1; i <= cantidad; i++) {
    		x += i;
			y += juveniles.get(i - 1).getNivel();
			xy += i * juveniles.get(i - 1).getNivel();
			xx += i * i;
    	}
    	
    	float b = ((cantidad * xy) - (x * y)) / (float)((cantidad * xx) - (x * x));
    	float a = (y - (b * x)) / (float)cantidad;

    	return new float[] {b, a};
    }

	public static List<Jugador> filtrar_demarcacion(List<Jugador> jugadores, DEMARCACION_ASISTENTE demarcacion) {
		List<Jugador> lista = new ArrayList<Jugador>();

		for (Jugador j : jugadores) {
			if (j.getDemarcacion() == demarcacion) {
				lista.add(j);
			}
		}

		return lista;
	}

	public static void actualizar_seleccionadores(WebClient navegador) throws IOException {
		List<Pais> paises = Pais.obtener_paises(navegador);
		
		Map<String, Integer> seleccion_usuario = new HashMap<String, Integer>();
		Map<Integer, String> nombre_seleccion = new HashMap<Integer, String>();

		// 1. Buscamos los usuarios de cada selección
		
		// Hacamos dos pasadas, para U21 y NT
		for (int i = 0; i <= 400; i += 400) {
			for (Pais pais : paises) {
				int teamID = pais.getId() + i;
			
				XmlPage pagina = navegador.getPage("http://sokker.org/xml/team-" + teamID + ".xml");
				String nombre = ((DomText) pagina.getFirstByXPath("//teamdata/team/name/text()")).asText();
				String login = ((DomText) pagina.getFirstByXPath("//teamdata/user/login/text()")).asText();
				seleccion_usuario.put(login.toLowerCase(), teamID);
				nombre_seleccion.put(teamID, nombre);

				System.out.println(login + " -> " + nombre);
			}
		}
		
		// 2. Actualizamos la selección de cada usuario
		
		String[] archivos = UsuarioBO.obtener_usuarios();
		for (String login : archivos) {
			Usuario usuario = UsuarioBO.leer_usuario(login.split(".properties")[0].substring(1));
			if (usuario != null) {
				Integer seleccion = seleccion_usuario.get(usuario.getLogin_sokker().toLowerCase());
				if (usuario.getTid_nt() == null && seleccion != null || usuario.getTid_nt() != null && !usuario.getTid_nt().equals(seleccion)) {
					usuario.setTid_nt(seleccion);
					usuario.setEquipo_nt(seleccion == null ? null : nombre_seleccion.get(seleccion));
					usuario.setDef_tid(usuario.getTid());
					UsuarioBO.grabar_usuario(usuario);

					System.out.println("Cambio: " + usuario.getLogin_sokker() + " -> " + usuario.getEquipo_nt());
				}
			}
		}
	}

	public static String getLogin() {
		try {
			Context initialContext = new InitialContext();
			return (String) initialContext.lookup("java:comp/env/login");
		} catch (NamingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String getPassword() {
		try {
			Context initialContext = new InitialContext();
			return (String) initialContext.lookup("java:comp/env/password");
		} catch (NamingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
