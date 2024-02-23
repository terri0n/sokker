package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.entity.Competicion;
import com.formulamanager.sokker.entity.Equipo;
import com.formulamanager.sokker.entity.Equipo.TIPO_COMPETICION;
import com.formulamanager.sokker.entity.Grupo;
import com.formulamanager.sokker.entity.Jornada;
import com.formulamanager.sokker.entity.Partido;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class CompeticionBO {
	public static String IBERICA = JugadorBO.PATH_BASE + "iberica.properties";
	
	//---------
	// Guardar
	//---------
	
	public static void guardar(Competicion competicion) throws IOException {
		Properties prop = new Properties();

		if (competicion != null) {
			prop.setProperty("competicion_equipos_champions", competicion.getEquipos_champions()+"");
			prop.setProperty("competicion_equipos_uefa", competicion.getEquipos_uefa()+"");
			prop.setProperty("competicion_equipos_cp", competicion.getEquipos_cp()+"");
			prop.setProperty("competicion_num_grupos", competicion.getGrupos().size()+"");
			prop.setProperty("competicion_num_competiciones", competicion.getCompeticiones() == null ? "0" : competicion.getCompeticiones().size()+"");
			
			for (Grupo g : competicion.getGrupos()) {
				guardar(prop, "grupo" + g.getNumero() + "_", g);
			}
	
			if (competicion.getCompeticiones() != null) {
				for (Grupo g : competicion.getCompeticiones()) {
					guardar(prop, g.getTipo_competicion().name() + "_", g);
				}
			}
		}
		
		Util.guardar_properties(prop, IBERICA);
	}

	public static void guardar(Properties prop, String prefijo, Grupo grupo) {
		prop.setProperty(prefijo + "tipo_competicion", grupo.getTipo_competicion() == null ? "" : grupo.getTipo_competicion().name());
		prop.setProperty(prefijo + "num_jornadas", grupo.getNum_jornadas()+"");
		prop.setProperty(prefijo + "num_equipos", grupo.getEquipos().size()+"");

		int i = 0;
		for (Equipo e : grupo.getEquipos()) {
			guardar(prop, prefijo + "equipo" + i++ + "_", e);
		}

		for (Jornada j : grupo.getJornadas()) {
			guardar(prop, prefijo + "jornada" + j.getNumero() + "_", j);
		}
	}

	private static void guardar(Properties prop, String prefijo, Equipo equipo) {
		prop.setProperty(prefijo + "nombre", equipo.getNombre());
		prop.setProperty(prefijo + "tid", equipo.getTid()+"");
		prop.setProperty(prefijo + "puntos_base", equipo.getPuntos_base()+"");
		prop.setProperty(prefijo + "gf_base", equipo.getGf_base()+"");
		prop.setProperty(prefijo + "gc_base", equipo.getGc_base()+"");
	}

	private static void guardar(Properties prop, String prefijo, Jornada jornada) {
		prop.setProperty(prefijo + "num_partidos", jornada.getPartidos().size()+"");
		
		int i = 0;
		for (Partido p : jornada.getPartidos()) {
			guardar(prop, prefijo + "partido" + i++ + "_", p);
		}
	}

	private static void guardar(Properties prop, String prefijo, Partido partido) {
		prop.setProperty(prefijo + "tid_local", Util.nvl(Util.integerToString(partido.getLocal().getTid())));
		prop.setProperty(prefijo + "tid_visitante", Util.nvl(Util.integerToString(partido.getVisitante().getTid())));
		prop.setProperty(prefijo + "goles_l", Util.nvl(Util.integerToString(partido.getGoles_l())));
		prop.setProperty(prefijo + "goles_v", Util.nvl(Util.integerToString(partido.getGoles_v())));
	}

	//------
	// Leer
	//------
	
	public static Competicion leer() throws IOException {
		Properties prop = new Properties();

		File file = new File(IBERICA);

		if (!file.exists()) {
			return null;
		}
		
		InputStream input = new FileInputStream(file);
		
		// load a properties file
		prop.load(input);

		if (prop.size() > 0) {
			Competicion competicion = new Competicion();
			competicion.setEquipos_champions(Integer.valueOf(prop.getProperty("competicion_equipos_champions")));
			competicion.setEquipos_uefa(Integer.valueOf(prop.getProperty("competicion_equipos_uefa")));
			competicion.setEquipos_cp(Util.invl(Util.stringToInteger(prop.getProperty("competicion_equipos_cp"))));
			int num_grupos = Integer.valueOf(prop.getProperty("competicion_num_grupos"));
	
			for (int i = 1; i <= num_grupos; i++) {
				competicion.getGrupos().add(leer_grupo(prop, "grupo" + i + "_", i));
			}
			
			int num_competiciones = Integer.valueOf(prop.getProperty("competicion_num_competiciones"));
	
			if (num_competiciones > 0) {
				int i = 1;
				for (TIPO_COMPETICION tipo : TIPO_COMPETICION.values()) {
					competicion.getCompeticiones().add(leer_grupo(prop, tipo + "_", i++));
				}
			}
			
			competicion.actualizar_clasificaciones();
	
			return competicion;
		} else {
			return null;
		}
	}

	public static void buscar_arcades(Competicion competicion, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		List<Grupo> todos = new ArrayList<Grupo>();
		todos.addAll(competicion.getGrupos());
		todos.addAll(competicion.getCompeticiones());
		
		for (Grupo g : todos) {
			Jornada j = g.getJornadas().get(g.getNum_jornada() - 1);
			
			for (Partido p : j.getPartidos()) {
				if (p.getGoles_l() == null && p.getGoles_v() == null) {
					XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/matches-team-" + p.getLocal().getTid() + ".xml");
					List<DomNode> partidos = (List<DomNode>)pagina.getByXPath("//matches/match");

					for (DomNode partido : partidos) {
						int tid_visitante = Integer.valueOf(((DomText) partido.getFirstByXPath("awayTeamID/text()")).asText());
						if (tid_visitante == p.getVisitante().getTid().intValue()) {
							// ¿Es un partido tipo Arcade / Arcade Cup?
							int leagueID = Integer.valueOf(((DomText) partido.getFirstByXPath("leagueID/text()")).asText()).intValue();
							if (leagueID == 51530 || leagueID == 51531) {
								// Fecha
								try {
									p.setFecha(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(((DomText) partido.getFirstByXPath("dateExpected/text()")).asText()));
								} catch (ParseException e) {
									e.printStackTrace();
								}
								
								// mid
								p.setMid(Integer.valueOf(((DomText) partido.getFirstByXPath("matchID/text()")).asText()));
								
								// Marcador
								if (partido.getFirstByXPath("homeTeamScore/text()") != null) {
									p.setGoles_l(Integer.valueOf(((DomText) partido.getFirstByXPath("homeTeamScore/text()")).asText()));
									p.setGoles_v(Integer.valueOf(((DomText) partido.getFirstByXPath("awayTeamScore/text()")).asText()));
								}
								
								// NOTA: no rompemos el for para que el resultado que quede sea el último
							}
						}
					}
				}
			}
		}
	}

	private static Grupo leer_grupo(Properties prop, String prefijo, int numero) {
		Grupo grupo = new Grupo(numero);

		String tipo_competicion = Util.nnvl(prop.getProperty(prefijo + "tipo_competicion"));
		grupo.setTipo_competicion(tipo_competicion == null ? null : TIPO_COMPETICION.valueOf(tipo_competicion));
		
		grupo.setNum_jornadas(Integer.valueOf(prop.getProperty(prefijo + "num_jornadas")));
		int num_equipos = Integer.valueOf(prop.getProperty(prefijo + "num_equipos"));

		for (int i = 0; i < num_equipos; i++) {
			grupo.getEquipos().add(leer_equipo(prop, prefijo + "equipo" + i + "_"));
		}
		for (int i = 1; i <= grupo.getNum_jornadas(); i++) {
			Jornada j = leer_jornada(prop, prefijo + "jornada" + i + "_", i, grupo.getEquipos(), tipo_competicion == null ? Equipo.DESCANSO : Equipo.VACIO);
			if (j != null) {
				// Si la liga es de tipo juveniles, pueden no haberse generado aún todas las jornadas
				grupo.getJornadas().add(j);
			}
		}
		
		return grupo;
	}

	private static Equipo leer_equipo(Properties prop, String prefijo) {
		Equipo equipo = new Equipo();
		
		equipo.setNombre(prop.getProperty(prefijo + "nombre"));
		equipo.setTid(Util.stringToInteger(prop.getProperty(prefijo + "tid")));
		equipo.setPuntos_base(Util.invl(Util.stringToInteger(prop.getProperty(prefijo + "puntos_base"))));
		equipo.setGf_base(Util.invl(Util.stringToInteger(prop.getProperty(prefijo + "gf_base"))));
		equipo.setGc_base(Util.invl(Util.stringToInteger(prop.getProperty(prefijo + "gc_base"))));
		
		return equipo;
	}

	private static Jornada leer_jornada(Properties prop, String prefijo, int numero, List<Equipo> equipos, Equipo vacio) {
		Jornada jornada = new Jornada(numero);
		
		Integer num_partidos = Util.stringToInteger(prop.getProperty(prefijo + "num_partidos"));

		if (num_partidos == null) {
			// La jornada no existe
			return null;
		}
		
		for (int i = 0; i < num_partidos; i++) {
			jornada.getPartidos().add(leer_partido(prop, prefijo + "partido" + i + "_", equipos, vacio));
		}
		
		return jornada;
	}

	private static Partido leer_partido(Properties prop, String prefijo, List<Equipo> equipos, Equipo vacio) {
		Partido partido = new Partido();
				
		Integer tid_local = Util.stringToInteger(prop.getProperty(prefijo + "tid_local"));
		Integer tid_visitante = Util.stringToInteger(prop.getProperty(prefijo + "tid_visitante"));
		
		for (Equipo e : equipos) {
			if (e.getTid() != null) {
				if (e.getTid().equals(tid_local)) {
					partido.setLocal(e);
				} else if (e.getTid().equals(tid_visitante)) {
					partido.setVisitante(e);
				}
			}
		}
		
		if (partido.getLocal() == null) {
			partido.setLocal(vacio);
		}
		if (partido.getVisitante() == null) {
			partido.setVisitante(vacio);
		}

		partido.setGoles_l(Util.stringToInteger(prop.getProperty(prefijo + "goles_l")));
		partido.setGoles_v(Util.stringToInteger(prop.getProperty(prefijo + "goles_v")));
				
		return partido;
	}

	public static int menorOIgualMultiplo2(int num_equipos) {
		return (int) Math.pow(2, (int) Util.log(2, num_equipos));
	}
}
