package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.formulamanager.sokker.auxiliares.EmailSenderService;
import com.formulamanager.sokker.auxiliares.FileUtil;
import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.auxiliares.Zip;
import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;
import com.formulamanager.sokker.dao.AsistenteDAO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION_ASISTENTE;
import com.formulamanager.sokker.entity.Jugador.Entrenamiento;
import com.formulamanager.sokker.entity.Juvenil;
import com.formulamanager.sokker.entity.Pais;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class AsistenteBO extends JugadorBO {
	public final static String SOKKER_URL = "https://sokker.org";
//	public final static String PATH_BASE = getPath();
//	public final static String PATH_BACKUP = PATH_BASE + "backup/";
	public final static String ASISTENTE_BACKUP = "asistente_backup.zip";
	public final static String FACTORX_BACKUP = "factorx_backup.zip";
	public final static int ID_ARCADE = 51530;
	public final static int ID_ARCADE_CUP = 51531;
	public final static int ID_CHAMPIONS_CUP = 39933;
	public final static int ID_NT_FRIENDLY = 39934;
	public final static int ID_FRIENDLY = 3;
	public final static int ID_FRIENDLY_CUP = 200;
	public final static int JORNADAS_TEMPORADA = 13;
	public final static int JORNADA_NUEVO_SISTEMA_LIGAS = 976;
	public final static int JORNADA_NUEVO_ENTRENO = 993;
	public final static int PORCENTAJE_AMISTOSOS = 70;
	public final static int PORCENTAJE_OFICIALES = 93;
	public final static float PORCENTAJE_OFICIALES_AVANZADO = 96.5f;
	public final static float LIMITE_ENTRENAMIENTO_RESIDUAL = 18f;
	public final static float LIMITE_ENTRENAMIENTO_FORMACION = 40f;

	public enum TIPO_ENTRENADOR { PRINCIPAL, ASISTENTE, JUVENILES, OTRO }	// Empieza en 1

	private static String getFormSKMail() {
		return "<form action=\"" + AsistenteBO.SOKKER_URL + "/sent.php\" method=\"post\">\r\n" + 
				"	<input type=\"hidden\" name=\"sendmail\" value=\"1\">\r\n" + 
				"	<input type=\"hidden\" name=\"back\" value=\"mailbox\">\r\n" + 
				"	<input type=\"hidden\" name=\"mailtype\" value=\"sokker\" />\r\n" + 
				"	<input type=\"hidden\" name=\"typeto\" value=\"login\">\r\n" + 
				"	<input type=\"text\" name=\"send_to\" />\r\n" + 
				"	<input type=\"text\" name=\"title\" >\r\n" + 
				"	<textarea name=\"text\"></textarea>\r\n" + 
				"	<button type=\"submit\" />" + 
				"</form>";
	}
	
	public static List<Jugador> leer_jugadores (Integer tid, String equipo, boolean historico, Usuario usuario) {
		return leer_jugadores (tid, equipo, historico, usuario, null);
	}

	public static List<Jugador> leer_jugadores (Integer tid, String equipo, boolean historico, Usuario usuario, Integer jornada) {
		List<Jugador> jugadores = new ArrayList<Jugador>();
		Properties prop = new Properties();
		InputStream input = null;
	
		try {
			String BD;
			if (jornada == null) {
				BD = SystemUtil.getVar(SystemUtil.PATH) + tid + (historico ? "_historico" : "") + ".properties";
			} else {
				BD = SystemUtil.getVar(SystemUtil.PATH) + "/backup/" + tid + "_" + jornada + (historico ? "_historico" : "") + ".properties";
			}
			
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
//System.out.println(linea);
				Jugador j = new Jugador(Integer.valueOf(key), Arrays.asList(linea.split(",")), true, null, null, usuario, null, null);
				if (equipo != null) {
					j.setEquipo(equipo);
				}
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

	public static boolean existe_jugador(Integer pid, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/player-" + pid + ".xml");
	
		DomNode domNodeN = pagina.getFirstByXPath("player/name/text()");
		DomNode domNodeT = pagina.getFirstByXPath("player/teamID/text()");
		if (domNodeN == null || Integer.valueOf(domNodeT.asText()) == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	// Obtiene los valores p�blicos de un jugador
/*	public static Jugador obtener_jugador(Integer pid, boolean nt, int jornada_actual, Usuario usuario, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException  {
		XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/player-" + pid + ".xml");
	
		DomNode domNodeN = pagina.getFirstByXPath("player/name/text()");
		if (domNodeN == null) {
			return null;
		}
		
		String nombre = ((DomText) domNodeN).asText();
		DomNode domNodeA = pagina.getFirstByXPath("player/surname/text()");
		String apellido = domNodeA == null ? null : ((DomText) domNodeA).asText();
		Integer edad = new Integer(((DomText) pagina.getFirstByXPath("player/age/text()")).asText());
		Integer valor = new Integer(((DomText) pagina.getFirstByXPath("player/value/text()")).asText()) / 4;
		Integer tid = new Integer(((DomText) pagina.getFirstByXPath("player/teamID/text()")).asText());

		Jugador j = new Jugador(pid, nombre + " " + apellido, edad, valor, tid, usuario);

		j.setJornada(jornada_actual);
		j.setForma(new Integer(((DomText) pagina.getFirstByXPath("player/skillForm/text()")).asText()));
		j.setPais(new Integer(((DomText) pagina.getFirstByXPath("player/countryID/text()")).asText()));
		j.setFecha(new Date());
		j.setTarjetas(new Integer(((DomText) pagina.getFirstByXPath(nt ? "player/ntCards/text()" : "player/cards/text()")).asText()));
		j.setNt(new Integer(((DomText) pagina.getFirstByXPath("player/national/text()")).asText()));
		j.setLesion(new Integer(((DomText) pagina.getFirstByXPath("player/injuryDays/text()")).asText()));
		j.setEn_venta(new Integer(((DomText) pagina.getFirstByXPath("player/transferList/text()")).asText()));

		j.setDisciplina_tactica(new Integer(((DomText) pagina.getFirstByXPath("player/skillDiscipline/text()")).asText()));
		j.setSalario(new Integer(((DomText) pagina.getFirstByXPath("player/wage/text()")).asText()));
		j.setExperiencia(new Integer(((DomText) pagina.getFirstByXPath("player/skillExperience/text()")).asText()));
		j.setTrabajo_en_equipo(new Integer(((DomText) pagina.getFirstByXPath("player/skillTeamwork/text()")).asText()));
		j.setAltura(new Integer(((DomText) pagina.getFirstByXPath("player/height/text()")).asText()));
		j.setPeso(new Integer(((DomText) pagina.getFirstByXPath("player/weight/text()")).asText().replace(".", "")));
		j.setIMC(new Integer(((DomText) pagina.getFirstByXPath("player/BMI/text()")).asText().replace(".", "")));

		if (nt && j.getEn_venta() > 0) {
			try {
				new Navegador(false, null) {
					@Override
					protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
						actualizar_jugador(navegador, j, true);
						j.setActualizado(true); // Actualizado autom�ticamente
					}
				};
			} catch (Exception e) {
				e.printStackTrace();
				
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				SERVLET_ASISTENTE._log_linea("_EXCEPTIONS", "__TID: " + tid + "__PID: " + j.getPid() + sw.toString() + "\n");
			}
		}

		return j;
	}
	
	public static List<Jugador> obtener_jugadores(Integer tid, int jornada_actual, Usuario usuario, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
		ArrayList<Jugador> lista = new ArrayList<Jugador>();

		XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/players-" + tid + ".xml");
SERVLET_ASISTENTE._log_linea("_XMLS", "__TID: " + tid + "__\n" + pagina.asXml());
		ArrayList<DomNode> jugadores = (ArrayList<DomNode>) pagina.getByXPath("//player");
		for (DomNode jugador : jugadores) {
			Integer id = new Integer(((DomText) jugador.getFirstByXPath("ID/text()")).asText());
			DomNode domNodeN = jugador.getFirstByXPath("name/text()");
			String nombre = domNodeN == null ? null : ((DomText) domNodeN).asText();
			DomNode domNodeA = jugador.getFirstByXPath("surname/text()");
			String apellido = domNodeA == null ? null : ((DomText) domNodeA).asText();
			Integer edad = new Integer(((DomText) jugador.getFirstByXPath("age/text()")).asText());
			Integer valor = new Integer(((DomText) jugador.getFirstByXPath("value/text()")).asText()) / 4;
			Integer tid_duenyo = new Integer(((DomText) jugador.getFirstByXPath("teamID/text()")).asText());

			Jugador j = new Jugador(id, nombre + " " + apellido, edad, valor, tid_duenyo, usuario);

			j.setJornada(jornada_actual);
			j.setForma(new Integer(((DomText) jugador.getFirstByXPath("skillForm/text()")).asText()));
			j.setPais(new Integer(((DomText) jugador.getFirstByXPath("countryID/text()")).asText()));
			j.setFecha(new Date());
			j.setTarjetas(new Integer(((DomText) jugador.getFirstByXPath(tid < 1000 ? "ntCards/text()" : "cards/text()")).asText()));
			j.setNt(new Integer(((DomText) jugador.getFirstByXPath("national/text()")).asText()));
			j.setLesion(new Integer(((DomText) jugador.getFirstByXPath("injuryDays/text()")).asText()));
			j.setEn_venta(new Integer(((DomText) jugador.getFirstByXPath("transferList/text()")).asText()));
			
			// NTDB
			j.setDisciplina_tactica(new Integer(((DomText) jugador.getFirstByXPath("skillDiscipline/text()")).asText()));
			// Ahora ya no har�a falta multiplicar por 4 porque al mostrar al jugador multiplico por la moneda del pa�s del usuario. Pero, por compatibilizarlo con lo que ten�a, lo guardo en euros 
			j.setValor_original(valor * 4);

			j.setSalario(new Integer(((DomText) jugador.getFirstByXPath("wage/text()")).asText()));
			j.setExperiencia(new Integer(((DomText) jugador.getFirstByXPath("skillExperience/text()")).asText()));
			j.setTrabajo_en_equipo(new Integer(((DomText) jugador.getFirstByXPath("skillTeamwork/text()")).asText()));
			j.setAltura(new Integer(((DomText) jugador.getFirstByXPath("height/text()")).asText()));
			j.setPeso(new Integer(((DomText) jugador.getFirstByXPath("weight/text()")).asText().replace(".",  "")));
			j.setIMC(new Integer(((DomText) jugador.getFirstByXPath("BMI/text()")).asText().replace(".", "")));

			DomText dem = jugador.getFirstByXPath("trainingPosition/text()");
			j.setDemarcacion_entrenamiento(dem == null ? null : DEMARCACION.values()[Integer.valueOf(dem.asText())]);
			
			if (j.getEn_venta() > 0 && tid < NtdbBO.MAX_ID_SELECCION) {
				try {
					new Navegador(false, null) {
						@Override
						protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
							actualizar_jugador(navegador, j, true);
							j.setActualizado(true); // Actualizado autom�ticamente
						}
					};
				} catch (Exception e) {
					e.printStackTrace();
					SERVLET_ASISTENTE._log_linea("_EXCEPTIONS", "__TID: " + tid + "__PID: " + j.getPid() + "\n");
				}
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

					j.setEntrenamiento_avanzado(new Boolean(((DomText) jugador.getFirstByXPath("isInTrainingSlot/text()")).asText().replace(".", "")));
					j.setActualizado(true); // Actualizado autom�ticamente
				} catch (Exception e) {
					// En las NTs no tendremos las habilidades
				}
			}
			
			lista.add(j);
		}

		return lista;
	}*/
	
	public static void grabar_jugadores(List<Jugador> jugadores, int tid, int jornada_actual, boolean historico) throws IOException {
		Properties prop = new Properties();
		
		for (Jugador j : jugadores) {
			prop.setProperty(j.getPid()+"", j.serializar(true));
		}
	
		String ruta = SystemUtil.getVar(SystemUtil.PATH) + tid + (historico ? "_historico" : "") + ".properties";
		Util.guardar_properties(prop, ruta);

/*		File f = new File(PATH_BACKUP);
		if (!f.exists()) {
			f.mkdirs();
		}
		
		ruta = PATH_BACKUP + tid + "_" + jornada_actual + (historico ? "_historico" : "") + ".properties";
		Util.guardar_properties(prop, ruta);
*/	}

	public static void grabar_juveniles(List<Juvenil> juveniles, int tid, int jornada_actual, boolean historico) throws IOException {
		Properties prop = new Properties();
		
		for (Jugador j : juveniles) {
			prop.setProperty(j.getPid()+"", j.serializar(true));
		}
	
		String ruta = SystemUtil.getVar(SystemUtil.PATH) + tid + (historico ? "_historico" : "") + "_juveniles.properties";
		Util.guardar_properties(prop, ruta);

//		ruta = PATH_BACKUP + tid + "_" + jornada_actual + (historico ? "_historico" : "") + "_juveniles.properties";
//		Util.guardar_properties(prop, ruta);
	}
	
	public static int obtener_jornada(DomNode pagina) {
		Integer week = Integer.valueOf(((DomText) pagina.getFirstByXPath("week/text()")).asText());
		Integer dia = Integer.valueOf(((DomText) pagina.getFirstByXPath("day/text()")).asText());
		int resta = dia >= 5 ? 0 : 1;
		// El día 5 es el jueves después del entrenamiento
		
		return week - resta;
	}
	
	// 0 = jueves, 6 = miércoles
	public static int obtener_dia(DomNode pagina) {
		return (Integer.valueOf(((DomText) pagina.getFirstByXPath("day/text()")).asText()) + 2) % 7;
	}

	// Asigna los minutos jugados a cada jugador en la última posición
	public static void calcular_minutos_entrenamiento(List<Jugador> jugadores, HashMap<String, String> minutos, Usuario usuario) {
		for (Jugador j : jugadores) {
			Jugador jugador = j;
			do {
				Integer ultimo_dia = null;
				jugador.setDemarcacion_entrenamiento(null);

				// PID_jornada_dia
				for (String clave : minutos.keySet()) {
					if (clave.startsWith(jugador.getPid() + "_" + jugador.getJornada() + "_")) {
						int dia = Integer.valueOf(clave.split("_")[2]);
						if (ultimo_dia == null || dia > ultimo_dia) {
							ultimo_dia = dia;
							jugador.setDemarcacion_entrenamiento(DEMARCACION.valueOf(minutos.get(clave).split("_")[1]));
						}
					}
				}

				jugador.setMinutos(0f);
				if (jugador.getDemarcacion_entrenamiento() != null) {
					// Recorremos todos los d�as de la semana desde el �ltimo d�a en que jug�
					for (int d = ultimo_dia; d >= 0; d--) {
						String min_dem = minutos.get(jugador.getPid() + "_" + jugador.getJornada() + "_" + d);
						if (min_dem != null) {
							String[] split = min_dem.split("_");
							DEMARCACION dem = DEMARCACION.valueOf(split[1]);
							
							// Si jugó en la demarcación del último día, sumamos los minutos
							if (dem == jugador.getDemarcacion_entrenamiento()) {
								int min = Integer.valueOf(split[0]);
								jugador.setMinutos(jugador.getMinutos() + min);
							} else {
								// Si no, terminamos aquí
								break;
							}
						}
					}
				}
				jugador = jugador.getOriginal();
			} while (jugador != null && jugador.getJornada().intValue() >= usuario.getDef_jornada().intValue());
		}
	}

/*	public static void obtener_entrenamiento_nuevo(int tid, Usuario usuario, int jornada_actual, List<Jugador> jugadores, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		XmlPage pagina = Navegador.getXmlPage(navegador, AsistenteBO.SOKKER_URL + "/xml/matches-team-" + tid + ".xml");
		ArrayList<DomNode> partidos = (ArrayList<DomNode>) pagina.getByXPath("//match");
		
		for (DomNode partido : partidos) {
			int week = obtener_jornada(partido);

			// Si el partido es posterior a la �ltima actualizaci�n...
			if (week >= usuario.getDef_jornada() - 1 && week <= jornada_actual) {
				if (((DomText) partido.getFirstByXPath("isFinished/text()")).asText().equals("1")) {
					Integer mid = new Integer(((DomText) partido.getFirstByXPath("matchID/text()")).asText());
					pagina = Navegador.getXmlPage(navegador, AsistenteBO.SOKKER_URL + "/xml/match-" + mid + ".xml");

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
								
								// Si el jugador a�n no tiene una demarcaci�n asignada en el asistente, le ponemos esta por defecto
								if (j.getDemarcacion() == null) {
									j.setDemarcacion(demarcacion);
								}

								if (number <= 11 || timeIn > 0) {
									if (j.getDemarcacion_entrenamiento() == demarcacion) {
										float porcentaje_entrenamiento = obtener_porcentaje_entrenamiento_liga(tid, usuario, leagueID, navegador);
	
										float tiempo_total = timeIn > 90 ? 0f : ((timeOut == 0 || timeOut > 90 ? 90 : timeOut) - (timeIn == 0 ? 0 : timeIn - 1)) * porcentaje_entrenamiento;
										
										Jugador jug_semana = j.buscar_jornada(week);
										if (jug_semana != null) {
											int minutos = Util.invl(jug_semana.getMinutos());
											jug_semana.setMinutos(minutos + (int)((100 - minutos) * tiempo_total * 0.01f + 0.5f));
SERVLET_ASISTENTE._log_linea(usuario.getLogin(), "\t" + j.getPid() + " " + week + ": " + tiempo_total);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		// Selecciones
		if (tid > 1000) {
			// Comprobamos si el jugador ha jugado un partido internacional
			for (Jugador j : jugadores) {
				// Si pertenece a la selecci�n
				if (j.getNt() > 0) {
					List<Jugador> jugadores_nt = new ArrayList<Jugador>();
					jugadores_nt.add(j);
					obtener_entrenamiento_nuevo(j.getPais() + (j.getNt() == 1 ? 0 : 400), usuario, jornada_actual, jugadores_nt, navegador);
				}
			}
		}
	}
*/	
	
	public static HashMap<String, String> obtener_entrenamiento(int tid, Usuario usuario, int jornada_actual, List<Jugador> jugadores, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HashMap<String, String> minutos = new HashMap<String, String>();
		
		XmlPage pagina = Navegador.getXmlPage(navegador, AsistenteBO.SOKKER_URL + "/xml/matches-team-" + tid + ".xml");
		ArrayList<DomNode> partidos = (ArrayList<DomNode>) pagina.getByXPath("//match");
		
		for (DomNode partido : partidos) {
			int week = obtener_jornada(partido);
			
			//////////////////////////////////////////
			int dia = obtener_dia(partido);
			//////////////////////////////////////////

			// Si el partido es posterior a la última actualización...
			if (week >= usuario.getDef_jornada() - 1 && week <= jornada_actual) {
				
				if (((DomText) partido.getFirstByXPath("isFinished/text()")).asText().equals("1")) {
					Integer mid = new Integer(((DomText) partido.getFirstByXPath("matchID/text()")).asText());
					pagina = Navegador.getXmlPage(navegador, AsistenteBO.SOKKER_URL + "/xml/match-" + mid + ".xml");

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
									float porcentaje_entrenamiento = obtener_porcentaje_entrenamiento_liga(tid, usuario, leagueID, navegador);
									int tiempo_total = (int) (timeIn > 90 ? 0f : ((timeOut == 0 || timeOut > 90 ? 90 : timeOut) - (timeIn == 0 ? 0 : timeIn - 1)) * porcentaje_entrenamiento);

									if (week >= JORNADA_NUEVO_ENTRENO) {
										if (j.getDemarcacion_entrenamiento() == demarcacion) {
											Jugador jug_semana = j.buscar_jornada(week);
											if (jug_semana != null) {
												jug_semana.setMinutos(Util.fnvl(jug_semana.getMinutos()));
												// Deshago la conversión de puntos de entrenamiento a efectividad y le sumo los nuevos puntos de entrenamiento
												float puntos_entrenamiento = jug_semana.getPuntos_entrenamiento() + tiempo_total;

												// Vuelvo a calcular la efectividad
												if (jug_semana.isEntrenamiento_avanzado()) {
													jug_semana.setMinutos((puntos_entrenamiento < PORCENTAJE_OFICIALES_AVANZADO ? (50f + puntos_entrenamiento * 0.5f) : PORCENTAJE_OFICIALES_AVANZADO + ((puntos_entrenamiento - PORCENTAJE_OFICIALES_AVANZADO) / PORCENTAJE_OFICIALES_AVANZADO) * (100f - PORCENTAJE_OFICIALES_AVANZADO)));
												} else {
													jug_semana.setMinutos((puntos_entrenamiento < PORCENTAJE_OFICIALES ? puntos_entrenamiento : PORCENTAJE_OFICIALES + ((puntos_entrenamiento - PORCENTAJE_OFICIALES) / PORCENTAJE_OFICIALES) * (100f - PORCENTAJE_OFICIALES)));
												}
												
SERVLET_ASISTENTE._log_linea(usuario.getLogin(), "\t" + j.getPid() + " " + week + ": " + tiempo_total);
											}
										}
									} else {
										String clave = j.getPid() + "_" + week + "_" + dia;
	
//										int tiempo_total = timeIn > 90 ? 0 : (int) (((timeOut == 0 || timeOut > 90 ? 90 : timeOut) - (timeIn == 0 ? 0 : timeIn - 1)) * porcentaje_entrenamiento);
										minutos.put(clave, tiempo_total + "_" + demarcacion.name());
SERVLET_ASISTENTE._log_linea(usuario.getLogin(), "\t" + clave + ": " + tiempo_total);
									}
								}
							}
						}
					}
				}
			}
		}
		
		// Selecciones
		if (tid > 1000) {
			// Comprobamos si el jugador ha jugado un partido internacional
			for (Jugador j : jugadores) {
				// Si pertenece a la selección
				if (j.getNt() > 0) {
					List<Jugador> jugadores_nt = new ArrayList<Jugador>();
					jugadores_nt.add(j);
					HashMap<String, String> minutos_nt = obtener_entrenamiento(j.getPais() + (j.getNt() == 1 ? 0 : 400), usuario, jornada_actual, jugadores_nt, navegador);
					
					for (String key : minutos_nt.keySet()) {
						minutos.put(key, minutos_nt.get(key));
					}
				}
			}
		}

		return minutos;
	}

	private static float obtener_porcentaje_entrenamiento_liga(int tid, Usuario usuario, int leagueID, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		try {
			if (tid < 1000) {			// NT
				if (leagueID == ID_NT_FRIENDLY) {
					return 0f;
				} else {
					return AsistenteBO.PORCENTAJE_OFICIALES / 90f;
				}
			} else {
				switch (leagueID) {
					case ID_FRIENDLY:
					case ID_FRIENDLY_CUP:
						return AsistenteBO.PORCENTAJE_AMISTOSOS / 90f;
					case ID_CHAMPIONS_CUP:
						return AsistenteBO.PORCENTAJE_OFICIALES / 90f;
					case ID_NT_FRIENDLY:
						return 0f;
					case ID_ARCADE:
					case ID_ARCADE_CUP:
						return 0f;
					default:
						XmlPage pagina = Navegador.getXmlPage(navegador, AsistenteBO.SOKKER_URL + "/xml/league-" + leagueID + ".xml");
						DomText domText = (DomText) pagina.getFirstByXPath("league/info/type/text()");
						if (domText == null) {
							// La liga no existe. Pas� con una reestructuraci�n de ligas -> lo tomo como oficial
							return AsistenteBO.PORCENTAJE_OFICIALES / 90f;
						} else {
							int tipo = new Integer(domText.asText());
							boolean oficial = new Integer(((DomText) pagina.getFirstByXPath("league/info/isOfficial/text()")).asText()) > 0;
							
							if (tipo == 0 && oficial	// Liga
									|| tipo == 1		// Copa
									|| tipo == 2		// Promoci�n
									|| tipo == 9) {		// Champions Cup
								return AsistenteBO.PORCENTAJE_OFICIALES / 90f;
							} else if (tipo == 0 && !oficial) {
								return AsistenteBO.PORCENTAJE_AMISTOSOS / 90f;
							} else if (tipo == 7) {		// Juniors
								return 0f;
							}
							
							throw new RuntimeException("Liga " + leagueID + ", tipo " + tipo + " desconocido");
						}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Error al calcular el porcentaje de entrenamiento de la liga " + leagueID, e);
		}
	}

	public static Jugador combinar_jugadores(Jugador nuevo, Jugador historico) {
		if (!nuevo.getJornada().equals(historico.getJornada())) {
			if (nuevo.getCondicion() != null) {
				// Si tiene valores y los valores públicos han cambiado, mantengo la demarcación antigua, las notas, etc
				if (historico.getDemarcacion() != null) {
					nuevo.setDemarcacion(historico.getDemarcacion());
				}
				historico.addNotas(nuevo.getNotas());
				nuevo.setNotas(historico.getNotas());
				nuevo.setDestacar(historico.isDestacar());
				nuevo.setColor(historico.getColor());
				nuevo.setTalento(historico.getTalento());
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
			if (historico.getDemarcacion() != null) {
				nuevo.setDemarcacion(historico.getDemarcacion());
			}
			historico.addNotas(nuevo.getNotas());
			nuevo.setNotas(historico.getNotas());
			nuevo.setOriginal(historico.getOriginal());
			nuevo.setDestacar(historico.isDestacar());
			nuevo.setColor(historico.getColor());
			nuevo.setTalento(historico.getTalento());
			
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
			// Actualizamos la edad por si estamos en la semana del cambio de edad
			historico.setEdad(nuevo.getEdad());
			return historico;
		}
	}

	public static List<Jugador> actualizar_equipo(Usuario _usuario, int jornada_actual, boolean incrementar_edad, boolean registro, WebClient navegadorXML, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
		List<Jugador> jugadores_actualizados = new ArrayList<Jugador>();
		
		int tid = _usuario.getDef_tid();
		
//		List<Jugador> jugadores_nuevos = AsistenteBO.obtener_jugadores(tid, jornada_actual, _usuario, navegador);
		List<Jugador> jugadores_nuevos;
		if (tid > NtdbBO.MAX_ID_SELECCION) {
			jugadores_nuevos = AsistenteDAO.obtener_entrenamiento(_usuario, jornada_actual, incrementar_edad, navegador);
		} else {
			jugadores_nuevos = AsistenteDAO.obtener_jugadores(tid, jornada_actual, incrementar_edad, _usuario, navegador);
		}
		List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, _usuario.getDef_equipo(), false, _usuario);
		List<Jugador> jugadores_historico = AsistenteBO.leer_jugadores(tid, _usuario.getDef_equipo(), true, _usuario);

		// Añado o actualizo jugadores
		for (Jugador nuevo : jugadores_nuevos) {
if (nuevo.getLesion() > 0) {
	SERVLET_ASISTENTE._log_linea(_usuario.getLogin(), "\tLesión " + nuevo.getPid() + ": " + nuevo.getLesion());
}			
			Jugador anyadir = nuevo;
			if (jugadores != null && jugadores.contains(nuevo)) {
				Jugador j = jugadores.get(jugadores.indexOf(nuevo));
//				while (j != null && j.getJornada() > _usuario.getDef_jornada()) {
//					j = j.getOriginal();
//				}
				if (j != null) {
					anyadir = combinar_jugadores(nuevo, j);
				}
			}

			jugadores_actualizados.add(anyadir);
		}
		
		// Jugadores que ya no pertenecen al equipo
		for (Jugador j : jugadores) {
			boolean ascender = j.getPid() > 0 && tid < NtdbBO.MAX_ID_SELECCION && tid > NtdbBO.DIF_NT_U21 && j.getEdad() > 21;
			
			if (j.getPid() > 0 && !jugadores_nuevos.contains(j) || ascender) {
				Jugador actualizado = AsistenteDAO.obtener_jugador(j.getPid(), tid < NtdbBO.MAX_ID_SELECCION, jornada_actual, incrementar_edad, _usuario, navegador);
				if (actualizado == null) {
					// Archivo los jugadores despedidos
					jugadores_historico.add(j);
				} else {
					actualizado = combinar_jugadores(actualizado, j);
					if (tid < NtdbBO.MAX_ID_SELECCION && !ascender) {
						// Actualizo a los jugadores no seleccionados actualmente
						jugadores_actualizados.add(actualizado);
					} else {
						// Archivo los jugadores vendidos o aquellos de la U21 con más de 21 años
						jugadores_historico.add(actualizado);
					}
				}
			}
		}

		// Entrenamientos pasados
		AsistenteDAO.obtener_entrenamientos_pasados(tid, _usuario, jornada_actual, jugadores_actualizados, navegador);
		
		// Esta semana
//		for (Jugador j : jugadores_actualizados) {
			// NOTA: es necesario actualizar a 50 minutos los jugadores con entrenamiento avanzado, por si no juegan durante la semana, que tengan esos minutos
			// Dio algún problema y lo quité. Ahora lo he vuelto a dejar como estaba, pero no recuerdo cuál era el problema exactamente
//			j.prepararacion_entrenamiento(null, j.isEntrenamiento_avanzado());
//		}
		
//		HashMap<String, String> minutos = obtener_entrenamiento(tid, _usuario, jornada_actual, jugadores_actualizados, navegadorXML);
//		if (jornada_actual < JORNADA_NUEVO_ENTRENO) {
//			calcular_minutos_entrenamiento(jugadores_actualizados, minutos, _usuario);
//		}
		
		// Repesca
		for (Jugador j : jugadores_actualizados) {
			j.setEquipo(_usuario.getDef_equipo());
			
			// Si el jugador está en el histórico, lo repescamos
			if (jugadores_historico.contains(j)) {
				Jugador historico = jugadores_historico.get(jugadores_historico.indexOf(j));
//System.out.println(j.getNombre() + " " + jugadores_historico.indexOf(j));
				if (historico.getJornada().equals(j.getJornada())) {
					j.anyadir_original(historico.getOriginal());
				} else {
					j.anyadir_original(historico);
				}
				jugadores_historico.remove(historico);
			}
		}
		
		// Actualizamos jornada, país, nombre del equipo y tipo de entrenamiento
		int jornada_anterior = _usuario.getDef_jornada();
		EquipoBO.obtener_datos_equipo(_usuario, jornada_anterior, jornada_actual, navegadorXML);
		
		// Actualizamos entrenadores
		AsistenteBO.leer_entrenadores(_usuario, jornada_anterior, navegadorXML);

		// Juveniles
		List<Juvenil> juveniles_nuevos = obtener_juveniles(jornada_actual, incrementar_edad, _usuario, navegadorXML);
		List<Juvenil> juveniles = leer_juveniles(tid, false, _usuario);
		List<Juvenil> juveniles_historico = leer_juveniles(tid, true, _usuario);
		List<Juvenil> juveniles_actualizados = new ArrayList<Juvenil>();
		
		// Añado o actualizo juveniles
/*		if (registro) {
			// NOTA: los no plus solo reciben unas 11 semanas. Por eso solo lo uso al registrarte
			for (Juvenil nuevo : juveniles_nuevos) {
				AsistenteDAO.obtener_juvenil(nuevo, navegador);
			}
			juveniles_actualizados = juveniles_nuevos;
		} else {
*/			for (Juvenil nuevo : juveniles_nuevos) {
				Juvenil anyadir = nuevo;
				if (juveniles != null) {
					if (juveniles.contains(nuevo)) {
						Juvenil j = juveniles.get(juveniles.indexOf(nuevo));
						anyadir = combinar_juveniles(nuevo, j);
					}
				}
				
				// Obtenemos semanas previas
				AsistenteDAO.obtener_juvenil(anyadir, navegador);
	
				juveniles_actualizados.add(anyadir);
//			}
		}
		
		// Archivamos los juveniles desaparecidos
		juveniles.removeAll(juveniles_actualizados);
		juveniles_historico.addAll(juveniles);

		// Guardamos jugadores y usuario
		grabar_jugadores(jugadores_actualizados, tid, jornada_actual, false);
		grabar_jugadores(jugadores_historico, tid, jornada_actual, true);
		grabar_juveniles(juveniles_actualizados, tid, jornada_actual, false);
		grabar_juveniles(juveniles_historico, tid, jornada_actual, true);
		UsuarioBO.grabar_usuario(_usuario);
		
		// Envío jugadores a las NTDB
		if (tid > NtdbBO.MAX_ID_SELECCION && _usuario.isNtdb()) {
			new Thread() {
				@Override
				public void run() {
					try {
						// No importa qué navegador pasar, es para conectar con la BD externa
						NtdbBO.enviar_jugadores(navegador, _usuario, jugadores_nuevos, jornada_actual);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}.start();
		}
		
		return jugadores_actualizados;
	}

	public static List<Juvenil> leer_juveniles(int tid, boolean historico, Usuario usuario) {
		List<Juvenil> juveniles = new ArrayList<Juvenil>();
		Properties prop = new Properties();
		InputStream input = null;
	
		try {
			String BD = SystemUtil.getVar(SystemUtil.PATH) + tid + (historico ? "_historico" : "") + "_juveniles.properties";
			
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
				Juvenil j = new Juvenil(Integer.valueOf(key), Arrays.asList(linea.split(",")), true, null, usuario);
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

	protected static List<Juvenil> obtener_juveniles(int jornada_actual, boolean incrementar_edad, Usuario usuario, WebClient navegador) {
		List<Juvenil> juveniles = new ArrayList<Juvenil>();

		try {
			XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/juniors.xml");

			ArrayList<DomNode> juniors = (ArrayList<DomNode>) pagina.getByXPath("//junior");
			for (DomNode junior : juniors) {
				int pid = new Integer(((DomText) junior.getFirstByXPath("ID/text()")).asText());
				String nombre = ((DomText) junior.getFirstByXPath("name/text()")).asText()
						+ " " + ((DomText) junior.getFirstByXPath("surname/text()")).asText();
				int edad = new Integer(((DomText) junior.getFirstByXPath("age/text()")).asText()) + (incrementar_edad ? 1 : 0);
				int skill = new Integer(((DomText) junior.getFirstByXPath("skill/text()")).asText());
				int weeks = new Integer(((DomText) junior.getFirstByXPath("weeks/text()")).asText());
				boolean formation = Util.stringToBoolean(((DomText) junior.getFirstByXPath("formation/text()")).asText());
				
				Juvenil j = new Juvenil(pid, nombre, jornada_actual, edad, skill, weeks, formation, usuario);
				juveniles.add(j);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return juveniles;
	}

	public static void calcular(List<Jugador> jugadores, Usuario usuario) {
		if (usuario.getDef_tid() > NtdbBO.MAX_ID_SELECCION) {
			for (Jugador j : jugadores) {
				j.calcular_entrenamiento2();
				j.calcular_talento2();
			}
		}
	}
	
	/**
	 * Comprueba si el equipo se ha actualizado esta semana
	 * 
	 * @param request
	 * @param usuario
	 * @param *jornada_actual
	 */
	public static void setActualizado(HttpServletRequest request, Usuario usuario, Integer jornada_actual) {
		try {
			Integer[] jornada = { jornada_actual };
			if (jornada[0] == null) {
				new Navegador(true, request) {
					@Override
					protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException {
						jornada[0] = obtener_jornada(navegador);
					}
				};
			}

			request.setAttribute("actualizado", jornada[0].equals(usuario.getDef_jornada()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String listar_usuarios() throws UnsupportedEncodingException {
		String[] archivos = UsuarioBO.obtener_usuarios();

		String salida = "Total: " + archivos.length + "<br/>"
				+ "<label id='' for='reset_intentos'><input type='checkbox' id='reset_intentos' /> Resetear</label><br/>";
		
		Arrays.sort(archivos);
		
		for (String s : archivos) {
			try {
				File fUsuario = new File(SystemUtil.getVar(SystemUtil.PATH) + "/" + s);
				Date fecha = new Date(fUsuario.lastModified());
				String usuario = s.split(".properties")[0].substring(1);
				Usuario usr = UsuarioBO.leer_usuario(usuario, false);
	
				salida += Util.dateToString(fecha) 
						+ " <a href='javascript:void(0)' onclick=\"location.href='asistente/login_como?usuario=" + URLEncoder.encode(usuario, "UTF-8") + "&reset_intentos=' + $('#reset_intentos').val()\">" + usuario + "</a> ";
				salida += "[" + usr.getTid() + "] <a href='javascript:borrar_usuario_click(\"" + URLEncoder.encode(usuario, "UTF-8") + "\")'><i class='fas fa-trash-alt'></i></a>"
						+ " <a href='javascript:void(0)' onclick=\"location.href='asistente/examinar_usuario?usuario=" + URLEncoder.encode(usuario, "UTF-8") + "'\"><i class=\"fa-solid fa-binoculars azul\"></i></a>"
						+ " <br/>";
			} catch (Exception e) {
				e.printStackTrace();
				salida += "-- Error leyendo " + s + "<br />";
			}
		}

		return salida;
	}

	public static String listar_jornadas(Usuario usuario) {
		String salida = "<select onchange=\"location.href = 'asistente/backup?jornada=' + this.value + '&recuperar=' + ($('#recuperar:checked').val() || '')\">";
		salida += "<option></option>";
		
		String[] backups = UsuarioBO.obtener_backups(usuario.getDef_tid());
		if (backups != null) {
			for (String backup : backups) {
				salida += "<option>" + backup.split("_")[1].split("\\.")[0] + "</option>";
			}
		}
		salida += "</select>";
		
		return salida;
	}
	
	public static void codificar_passwords() throws IOException {
		File f = new File(SystemUtil.getVar(SystemUtil.PATH));

		String[] archivos = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("_");
			}
		});
		
		for (String s : archivos) {
			String str_usuario = s.split(".properties")[0].substring(1);
			if (!str_usuario.equals("terrion")) {
				Usuario usuario = UsuarioBO.leer_usuario(str_usuario, false);
				
				usuario.setPassword(Util.getMD5(usuario.getPassword()));
				UsuarioBO.grabar_usuario(usuario);
			}
		}
	}
	
	public static Set<Integer> buscar_selecciones() {
		File f = new File(SystemUtil.getVar(SystemUtil.PATH));
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
	
/*	public static List<Jugador> buscar_transferencias(WebClient navegador, int tid, int jornada_actual) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
		HtmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/transfers");
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

		// TODO: pasar de p�gina

		List<Jugador> jugadores = new ArrayList<Jugador>();

		// Y luego leo el jugador entero
		// NOTA: En la lista de transferencias no aparece la forma
		for (Integer pid : pids) {
			jugadores.add(AsistenteDAO.obtener_jugador(pid, true, jornada_actual, false, null, navegador));
		}

		return jugadores;
	}
*/	
	
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
		return "<form name='form' action='" + AsistenteBO.SOKKER_URL + "/mailbox/sent.php' method='post'>"
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
				XmlPage pagina = Navegador.getXmlPage(navegador, AsistenteBO.SOKKER_URL + "/xml/trainers.xml");
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
					
					Jugador j = new Jugador(new Integer[] { condicion, rapidez, tecnica, pases, porteria, defensa, creacion, anotacion, media });
					
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
				
				// Actualizamos las jornadas anteriores desde la �ltima actualizaci�n si no estaban establecidas
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
	
	// posici�n[0] = pendiente de la recta (a)
	// posici�n[1] = t�rmino independiente (b)
	// posici�n[2] = correlaci�n (grado de uni�n de -1 a 1)
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
     * https://es.wikipedia.org/wiki/Regresi�n_lineal
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
    	
    	for (int i = 0; i < cantidad; i++) {
    		int x_ = juveniles.get(i).getJornada() - juveniles.get(i).getPrimera_jornada();
    		x += x_;
			y += Math.abs(juveniles.get(i).getNivel());
			xy += x_ * Math.abs(juveniles.get(i).getNivel());
			xx += x_ * x_;
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

	public static Usuario obtener_usuario_seleccion(int tid) {
		String[] archivos = UsuarioBO.obtener_usuarios();
		for (String login : archivos) {
			Usuario usuario = UsuarioBO.leer_usuario(login.split(".properties")[0].substring(1), false);
			if (usuario != null && new Integer(tid).equals(usuario.getTid_nt())) {
				return usuario;
			}
		}
		return null;
	}
	
	
	public static void actualizar_seleccionadores(WebClient navegador) throws IOException {
		List<Pais> paises = Pais.obtener_paises(navegador);
		
		Map<String, Integer> seleccion_usuario = new HashMap<String, Integer>();
		Map<Integer, String> nombre_seleccion = new HashMap<Integer, String>();

		// 1. Buscamos los usuarios de cada selección
		
		// Hacemos dos pasadas, para U21 y NT
		for (int i = 0; i <= 400; i += 400) {
			for (Pais pais : paises) {
				int teamID = pais.getId() + i;
				
				try {
					XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/team-" + teamID + ".xml");
					String nombre = ((DomText) pagina.getFirstByXPath("//teamdata/team/name/text()")).asText();
					String login = ((DomText) pagina.getFirstByXPath("//teamdata/user/login/text()")).asText();
					seleccion_usuario.put(login.toLowerCase(), teamID);
					nombre_seleccion.put(teamID, nombre);
	
					//System.out.println(login + " -> " + nombre + " (" + teamID + ")");
				} catch (Exception e) {
					System.out.println("Error al acceder a " + AsistenteBO.SOKKER_URL + "/xml/team-" + teamID + ".xml");
					e.printStackTrace();
				}
			}
		}
		
		// 2. Actualizamos la selección de cada usuario
		
		String[] archivos = UsuarioBO.obtener_usuarios();
		for (String login : archivos) {
			Usuario usuario = UsuarioBO.leer_usuario(login.split(".properties")[0].substring(1), false);
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

	/**
	 * Devuelve los equipos actualizados la última semana
	 * @return
	 */
	public static String[] obtener_equipos() {
		File f = new File(SystemUtil.getVar(SystemUtil.PATH));

		String[] archivos = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File f = new File(SystemUtil.getVar(SystemUtil.PATH) + "/" + name);

				return f.isFile()
					&& !name.startsWith("_") 
					&& !name.endsWith("_historico.properties") 
					&& !name.endsWith("_juveniles.properties")
					&& !name.equals("IPs.properties")
					&& !name.equals("NTDB.properties")
					&& !name.equals("URLs.properties");
			}
		});
		
		return archivos;
	}

	/**
	 * Devuelve los equipos actualizados la última semana
	 * @return
	 */
	public static String[] obtener_ultimos_equipos() {
		File f = new File(SystemUtil.getVar(SystemUtil.PATH));
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -7);

		String[] archivos = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File f = new File(SystemUtil.getVar(SystemUtil.PATH) + "/" + name);

				return f.isFile()
					&& c.getTime().before(new Date(f.lastModified()))
					&& !name.startsWith("_") 
					&& !name.endsWith("_historico.properties") 
					&& !name.endsWith("_juveniles.properties")
					&& !name.equals("IPs.properties")
					&& !name.equals("NTDB.properties")
					&& !name.equals("URLs.properties");
			}
		});
		
		return archivos;
	}
	
	public static String getDatos_estadistica_forma(boolean todos, boolean jugando) {
		// Leo todos los equipos y sus jugadores
		List<Jugador> jugadores = new ArrayList<Jugador>();
		String[] archivos = AsistenteBO.obtener_ultimos_equipos();

		for (String archivo : archivos) {
			Integer tid = Integer.valueOf(archivo.split(".properties")[0]);
			jugadores.addAll(AsistenteBO.leer_jugadores(tid, null, false, null/*usuario*/));
		}

		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		for (Jugador j : jugadores) {
			if (j.getForma() != null) {
				boolean ok = false;
				if (!todos) {
					Jugador original = j.getOriginal();
					int jornada = j.getJornada();
					for (int i = 0; i < 10; i++) {
						if (original != null && jornada - 1 == original.getJornada()) {
							jornada = original.getJornada();
							if (jugando && original.getMinutos() >= 90f
								|| !jugando && original.getMinutos() == 0f) {
								ok = true;
							} else {
								ok = false;
								break;
							}
							original = original.getOriginal();
						} else {
							ok = false;
							break;
						}
					}
				}
				
				if (todos || ok) {
					hm.put(j.getForma(), hm.getOrDefault(j.getForma(), 0) + 1);
				}
			}
		}

		List<String> datos = new ArrayList<String>();
		List<String> hTicks = new ArrayList<String>();

		int maximo = 0;
		for (int i : hm.keySet()) {
			datos.add("[" + i + "," + hm.get(i) + ",false]");
			hTicks.add("{'v':" + i + ",'f':'" + i + "'}");
			maximo = Math.max(maximo, hm.get(i));
		}

		return "[" + String.join(",", datos) + "]," + "[" + String.join(",", hTicks) + "]," + 0 + "," + maximo + "," + 0 + "," + 18 + ",null"; // 0 = max_jornada
	}

	public static String getDatos_estadisticas_entrenamientos(int pos) {
		// Leo todos los usuarios
		List<Usuario> usuarios = UsuarioBO.leer_ultimos_usuarios(7);

		HashMap<TIPO_ENTRENAMIENTO, Integer> hm = new HashMap<>();
		for (Usuario u : usuarios) {
			TIPO_ENTRENAMIENTO tipo = u.getTipo_entrenamiento(pos).get(u.getJornada());
			if (tipo != null) {
				hm.put(tipo, hm.getOrDefault(tipo, 0) + 1);
			}
		}

		List<String> datos = new ArrayList<String>();
		List<String> hTicks = new ArrayList<String>();

		int maximo = 0;
		for (TIPO_ENTRENAMIENTO t : hm.keySet()) {
			datos.add("[" + t.ordinal() + "," + hm.get(t) + ",false]");
			hTicks.add("{'v':" + t.ordinal() + ",'f':'" + t.getIngles() + "'}");
			maximo = Math.max(maximo, hm.get(t));
		}

		return "[" + String.join(",", datos) + "]," + "[" + String.join(",", hTicks) + "]," + 0 + "," + maximo + "," + 0 + "," + 18 + ",null"; // 0 = max_jornada
	}

	public static List<Jugador> leer_jugadores_y_usuarios() {
		// Leo todos los equipos y sus jugadores
		List<Jugador> jugadores = new ArrayList<Jugador>();
		String[] archivos = AsistenteBO.obtener_equipos();

		List<Usuario> usuarios = UsuarioBO.leer_usuarios();
		
		for (String archivo : archivos) {
			Integer tid = Integer.valueOf(archivo.split(".properties")[0]);
			if (tid > NtdbBO.MAX_ID_SELECCION) {
				List<Jugador> lista = AsistenteBO.leer_jugadores(tid, null, false, null/*usuario*/);

				for (Usuario usuario : usuarios) {
					if (usuario.getTid().equals(tid)) {
						Iterator<Jugador> it = lista.iterator();
						while (it.hasNext()) {
							Jugador j = it.next();
							if (j.getPid() <= 0) {	// Jugador de prueba
								it.remove();
							} else {
								j.setUsuario_recursivo(usuario);
							}
						}
						AsistenteBO.calcular(lista, usuario);
						jugadores.addAll(lista);
						break;
					}
				}
			}
		}
		
		return jugadores;
	}

	public static void exportar_todo() {
		// Leo todos los equipos y sus jugadores
		List<Jugador> jugadores = leer_jugadores_y_usuarios();
		Collections.shuffle(jugadores);
		
		int i = 1;
		for (Jugador j : jugadores) {
			while (j != null && j.getPid() > 0 && j.getJornada() > 1025) {
				if (j.getLesion() < 7
						&& j.getDemarcacion_entrenamiento() != null 
						&& j.getUsuario().getEntrenador_principal().get(j.getJornada()) != null) {
					List<String> linea = new ArrayList<>();
					linea.add(i+"");
					linea.add(j.getJornada()+"");
					linea.add(j.getEdad()+"");
					linea.add(j.getMinutos()+"");
					linea.add(j.getDemarcacion_entrenamiento()+"");
					TIPO_ENTRENAMIENTO tipo = j.getUsuario().getTipo_entrenamiento(j.getDemarcacion_entrenamiento().ordinal()).get(j.getJornada());
					linea.add(tipo+"");
					linea.add(j.getValor_habilidad(tipo)+"");
					linea.add(j.getUsuario().getEntrenador_principal().get(j.getJornada()).getValor_habilidad(tipo)+"");
					linea.add(j.getUsuario().getNivel_asistentes().get(j.getJornada())+"");
					System.out.println(String.join(",", linea));
				}
				j = j.getOriginal();
			}
			i++;
		}
	}
				
	public static void getDatos_estadisticas_entrenamiento() {
		// Leo todos los equipos y sus jugadores
		List<Jugador> jugadores = leer_jugadores_y_usuarios();

		List<Float> puntos_e = new ArrayList<Float>();

		for (TIPO_ENTRENAMIENTO habilidad : TIPO_ENTRENAMIENTO.values()) {
			if (habilidad != TIPO_ENTRENAMIENTO.Condicion) {
		
				for (Jugador j : jugadores) {
					List<Entrenamiento> entrenamientos = j.total_entrenamientos2.get(habilidad);
					if (entrenamientos != null && entrenamientos.size() > 1) {
						for (Entrenamiento e : entrenamientos) {
							// Cuento a partir de la jornada 1025 ya que es el inicio de la temporada donde aumentaron el entrenamiento residual y de formación
							if (!e.abierto_izq && !e.abierto_der && e.jornadas.get(e.jornadas.size() - 1).getJornada() >= 1025) {
								boolean ok = false;
								for (Float f : e.puntos_entrenamiento) {
									if (f == 0f ) {
										ok = false;
										break;
									} else {
										ok = true;
									}
								}
		
								if (ok) {
									float exponente_edad = e.jornadas.get(0).getEdad() - 16;
									float exponente_habilidad = e.valor_habilidad;
									float suma = (float) (Util.sumar(e.puntos_entrenamiento) * Math.pow(Jugador.BASE_TALENTO_EDAD, -exponente_edad) * Math.pow(Jugador.BASE_TALENTO_HABILIDAD, -exponente_habilidad));
									puntos_e.add(suma);
								}
							}
						}
					}
				}
	
				System.out.println(habilidad + ": " + Util.sumar(puntos_e) / puntos_e.size() + " (" + puntos_e.size() + ")");
			}
		}
	}
	
	public static String exportar_jugadores(List<Jugador> jugadores) {
		if (jugadores != null && jugadores.size() > 0) {
			if (jugadores.get(0) instanceof Juvenil) {
				String nombre = Util.getTexto(jugadores.get(0).getUsuario().getLocale(), "skills.name");
				String edad = Util.getTexto(jugadores.get(0).getUsuario().getLocale(), "skills.age");
				String posicion = Util.getTexto(jugadores.get(0).getUsuario().getLocale(), "common.position");
				String nivel = Util.getTexto(jugadores.get(0).getUsuario().getLocale(), "common.level");
				String semanas = Util.getTexto(jugadores.get(0).getUsuario().getLocale(), "common.weeks");
				
				String res = "[family=monospace]&nbsp;&nbsp;&nbsp; "
						+ nombre + StringUtils.rightPad("", (30 - nombre.length()) * 6, "&nbsp;") + " "
						+ edad + StringUtils.rightPad("", (4 - edad.length()) * 6, "&nbsp;") + " "
						+ posicion + StringUtils.rightPad("", (11 - posicion.length()) * 6, "&nbsp;") + " "
						+ nivel + StringUtils.rightPad("", (12 - nivel.length() + (jugadores.get(0).getUsuario().isNumeros() ? 5 : 0)) * 6, "&nbsp;") + " "
						+ semanas + "[/family]\n"
						+ "[family=monospace]" + StringUtils.rightPad("", 65 + semanas.length(), "-") + "[/family]\n";
		
				int i = 1;
				for (Jugador jug : jugadores) {
					Juvenil j = (Juvenil) jug;
					res += "[family=monospace]" + StringUtils.leftPad(i + "", 2, "0") + ". "
						+ "[b]" + StringUtils.rightPad(j.getNombre(), 30, ".") + "[/b] "
						+ StringUtils.leftPad(j.getEdad() + "", 2, "0") + ".. "
						+ StringUtils.rightPad(Util.getTexto(j.getUsuario().getLocale(), j.isJugador_campo() ? "players.outfield" : "players.gk"), 11, ".") + " "
						+ j.getHabilidad_bbcode(j.getNivel(), j.getOriginal() == null ? null : j.getOriginal().getNivel(), "", true) + " "
						+ StringUtils.leftPad(j.getSemanas() + "", 2, "0") + "[/family]\n";
					i++;
				}
	
				return res;
			} else {
				String res = "[family=monospace][color=transparent]...[/color] Name[color=transparent]..........................[/color] Age Dem[/family]\n"
					+ "[family=monospace]------------------------------------------[/family]\n";
	
				int i = 1;
				for (Jugador j : jugadores) {
					res += "[color=" + j.getColor_demarcacion() + "][family=monospace]" + StringUtils.leftPad(i + "", 2, "0") + ". "
						+ "[pid=" + j.getPid() + "]" + StringUtils.rightPad(j.getNombre(), 30, ".") + "[/pid] "
						+ StringUtils.leftPad(j.getEdad() + "", 2, "0") + " &nbsp;"
						+ (j.getDemarcacion() == null ? "" : j.getDemarcacion().name()) + "[/family][/color]\n";
					i++;
				}
	
				return res;
			}
		} else {
			return "";
		}
	}

	public static void mandar_skmail(WebClient navegador, String asunto, String mensaje, Jugador j) {
		System.out.println("Enviabdo skmail a " + j.getLogin_duenyo() + " (" + j.getNombre() + ")...");

		asunto = asunto.replaceAll("%pid", j.getPid()+"");
		asunto = asunto.replaceAll("%player", j.getNombre());
		asunto = asunto.replaceAll("%user", j.getLogin_duenyo());

		mensaje = mensaje.replaceAll("%pid", j.getPid()+"");
		mensaje = mensaje.replaceAll("%player", j.getNombre());
		mensaje = mensaje.replaceAll("%user", j.getLogin_duenyo());
		
		try {
//			HtmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/player/PID/" + j.getPid());
//			HtmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/mailbox");

//			HtmlForm form = pagina.getFormByName("replyForm");
//			form.getInputByName("send_to").setValueAttribute(j.getLogin_duenyo());
//			form.getInputByName("title").setValueAttribute(asunto);
//			form.getInputByName("text").setTextContent(mensaje);
//			HtmlButton botonFormulario = form.getFirstByXPath("//button[@type='submit']");
			
			StringWebResponse response = new StringWebResponse(getFormSKMail(), new URL("file:///."));
			HtmlPage form = HTMLParser.parseHtml(response, navegador.getCurrentWindow());
			((HtmlInput)form.getElementByName("send_to")).setValueAttribute(j.getLogin_duenyo());
			((HtmlInput)form.getElementByName("title")).setValueAttribute(asunto);
			((HtmlTextArea)form.getElementByName("text")).setTextContent(mensaje);
			
			HtmlButton botonFormulario = form.getForms().get(0).getFirstByXPath("//button[@type='submit']");
			HtmlPage p = botonFormulario.click();
System.out.println(p.asXml());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void enviar_backups() {
System.out.print("Enviando email...");
		HashMap<String, String> destinatarios = new HashMap<>();
		destinatarios.put("Terrion", "tejedor@gmail.com");
		
		HashMap<String, byte[]> listaArchivos = new HashMap<>();
		try {
			listaArchivos.put(ASISTENTE_BACKUP, FileUtil.leerFichero(JugadorBO.PATH_BASE + ASISTENTE_BACKUP));
			listaArchivos.put(FACTORX_BACKUP, FileUtil.leerFichero(JugadorBO.PATH_BASE + FACTORX_BACKUP));

			String asunto = "Sokker Asistente BACKUP " + Util.dateToString(new Date());
			EmailSenderService.sendEmail("tejedor@gmail.com", asunto, "", listaArchivos);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				SERVLET_ASISTENTE._log_linea("_ENVIAR_BACKUPS", e.getMessage() + e.getLocalizedMessage() + e.toString());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
System.out.println("OK");
	}

    public static void tareas_NTs(WebClient navegador, int jornada_actual) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
    	Set<Integer> selecciones = AsistenteBO.buscar_selecciones();
	
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -4);
		Date fecha_comprobacion = c.getTime();

		for (int tid : selecciones) {
System.out.println("Limpiando selección " + tid);
			
			// Leemos todos los jugadores del país
			List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, null, false, null/*usuario*/);
			List<Jugador> jugadores_u21 = AsistenteBO.leer_jugadores(tid + 400, null, false, null/*usuario*/);
			List<Jugador> jugadores_historico = AsistenteBO.leer_jugadores(tid, null, true, null/*usuario*/);
			List<Jugador> jugadores_historico_u21 = AsistenteBO.leer_jugadores(tid + 400, null, true, null/*usuario*/);

			// Movemos los U21 mayores de 21 a la NT
			for (Jugador j : jugadores_u21.toArray(new Jugador[jugadores_u21.size()])) {
				if (j.getEdad() > 21) {
					jugadores_u21.remove(j);
					jugadores.add(j);
				}
			}

			// Y los del histórico
			for (Jugador j : jugadores_historico_u21.toArray(new Jugador[jugadores_historico_u21.size()])) {
				if (j.getEdad() > 21) {
					jugadores_historico_u21.remove(j);
					jugadores_historico.add(j);
				}
			}

			// Comprobamos que los jugadores del histórico no actualizados en los últimos meses aún existan
			for (Jugador j : jugadores_historico_u21.toArray(new Jugador[jugadores_historico_u21.size()])) {
				if (j.getFecha().before(fecha_comprobacion)) {
					if (!AsistenteBO.existe_jugador(j.getPid(), navegador)) {
						jugadores_historico_u21.remove(j);
					}
				}
			}
			for (Jugador j : jugadores_historico.toArray(new Jugador[jugadores_historico.size()])) {
//								if (j.getPid() == 34322411) {
//									System.out.println(j.getNombre() + " " + j.getPid() + " " + j.getFecha());
//								}
				if (j.getFecha().before(fecha_comprobacion)) {
					if (!AsistenteBO.existe_jugador(j.getPid(), navegador)) {
						jugadores_historico.remove(j);
					}
				}
			}
			
			// Grabamos todo
			AsistenteBO.grabar_jugadores(jugadores, tid, jornada_actual, false);
			AsistenteBO.grabar_jugadores(jugadores_u21, tid + NtdbBO.DIF_NT_U21, jornada_actual, false);
			AsistenteBO.grabar_jugadores(jugadores_historico, tid, jornada_actual, true);
			AsistenteBO.grabar_jugadores(jugadores_historico_u21, tid + NtdbBO.DIF_NT_U21, jornada_actual, true);
		}
    }
	
	/**
	 * NOTA: /home debe pertenecer a "tomcat"
	 */
	public static void crear_backups() {
System.out.print("Creando backups...");

		// ASISTENTE
		
		String zipFile = JugadorBO.PATH_BASE + ASISTENTE_BACKUP;
		File f = new File(SystemUtil.getVar(SystemUtil.PATH));
		String[] files = f.list();

		for (int i = 0; i < files.length; i++) {
			files[i] = SystemUtil.getVar(SystemUtil.PATH) + files[i];
		}

		try {
			Zip.comprimir(files, zipFile);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		// FACTOR X
		
		zipFile = JugadorBO.PATH_BASE + FACTORX_BACKUP;
		f = new File(JugadorBO.PATH_BASE + "factorx");
		files = f.list();

		for (int i = 0; i < files.length; i++) {
			files[i] = JugadorBO.PATH_BASE + "factorx/" + files[i];
		}

		try {
			Zip.comprimir(files, zipFile);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
System.out.println("OK");
	}

	public static int getJornadaMod(int jornada) {
		// NOTA: a partir de la jornada 976 las temporadas pasan a ser de 13 semanas
		return jornada < JORNADA_NUEVO_SISTEMA_LIGAS ? jornada % 16 : (jornada - JORNADA_NUEVO_SISTEMA_LIGAS) % JORNADAS_TEMPORADA;
	}

	/**
	 * 
	 * @param request
	 * @param pid
	 * @param historico Buscar en el hist�rico de juveniles
	 * @return
	 */
	public static Jugador buscar_jugador(HttpServletRequest request, int pid, boolean historico, boolean juveniles) {
		Usuario usuario = (Usuario)request.getSession().getAttribute("usuario");

		List<? extends Jugador> jugadores;
		if (juveniles) {
			jugadores = AsistenteBO.leer_juveniles(usuario.getDef_tid(), historico, usuario);
		} else {
			jugadores = AsistenteBO.leer_jugadores(usuario.getDef_tid(), usuario.getDef_equipo(), historico, usuario);
		}

		for (Jugador j : jugadores) {
			if (j.getPid() == pid) {
				return j;
			}
		}
		
		return null;
	}

	public static void actualizacion_automatica() {
System.out.print("Actualizaciones automáticas...");
		try {
			Integer[] jornada_actual = new Integer[1];
			new Navegador(true, null) {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
					jornada_actual[0] = obtener_jornada(navegador);
				}
			};

			// Ignoramos a los usuarios que no han logeado en los �ltimos 120 d�as
			List<Usuario> usuarios = UsuarioBO.leer_ultimos_usuarios(120);
			
			for (Usuario usuario : usuarios) {
				// Si la opción está marcada y el equipo no se ha actualizado durante la semana...
				if (usuario.getActualizacion_automatica() != null && !jornada_actual[0].equals(usuario.getJornada())) {
					try {
						new Navegador(true, usuario.getLogin_sokker(), usuario.getActualizacion_automatica(), null) {
							@Override
							protected void execute(WebClient navegadorXML) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
								// Me guardo la fecha de modificación para restaurarla después de la actualización
								String ruta = SystemUtil.getVar(SystemUtil.PATH) + "_" + usuario.getLogin() + ".properties";
								File file = new File(ruta);
								long lastModified = file.lastModified();

								new Navegador(false, usuario.getLogin_sokker(), usuario.getActualizacion_automatica(), null) {
									@Override
									protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
										SERVLET_ASISTENTE._log_linea(usuario.getLogin(), "Actualización automática");
										actualizar_equipo(usuario, jornada_actual[0], isIncrementar_edad(), false, navegadorXML, navegador);
										file.setLastModified(lastModified);
									}
								};
							}
						};
					} catch (FailingHttpStatusCodeException | IOException | ParseException e) {
						e.printStackTrace();
						SERVLET_ASISTENTE._log_linea(usuario.getLogin(), "Error en actualización automática: " + e);
						SERVLET_ASISTENTE._log_linea("_CONTEXT_LISTENER", "Error en actualización automática: " + usuario.getLogin() + " -> " + e);
						if (e instanceof LoginException) {
							// Si falla el login quitamos la actualización automática del usuario y relanzamos la excepción para que no continúe con el resto de usuarios
							usuario.setActualizacion_automatica(null);
							UsuarioBO.grabar_usuario(usuario);
							throw e;
						}
					}
				}
			}
		} catch (FailingHttpStatusCodeException | LoginException | IOException | ParseException e) {
			e.printStackTrace();
			try {
				SERVLET_ASISTENTE._log_linea("_CONTEXT_LISTENER", "Error en actualización automática: " + e);
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			}
		}
System.out.println("OK");
	}
}
