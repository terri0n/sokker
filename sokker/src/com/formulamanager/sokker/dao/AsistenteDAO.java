package com.formulamanager.sokker.dao;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.formulamanager.sokker.auxiliares.JSONUtil;
import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.bo.NtdbBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Juvenil;
import com.formulamanager.sokker.entity.PalmaresEquipo;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.jayway.jsonpath.JsonPath;

public class AsistenteDAO {
	// Leemos los datos de un jugador a partir de un JSON, tanto si viene de la página del jugador como de la del equipo
	public static Jugador leer_jugador(Integer pid, boolean nt, int jornada_actual, boolean incrementar_edad, Usuario usuario, LinkedHashMap<String, Object> jugador) throws IOException {
		String nombre = JSONUtil.getString(jugador, "info.name.full");
		// NOTA: el jueves de la jornada 0 el jugador todavía no ha cumplido años. Le sumamos un año
		Integer edad = JSONUtil.getInteger(jugador, "info.characteristics.age") + (incrementar_edad ? 1 : 0);
		// NOTA: con la api, el valor viene en la moneda del usuario. La guardamos en euros para compatibilizar los valores anteriores
		Integer value = usuario.convertir_euros(JSONUtil.getInteger(jugador, "info.value.value"));
		Integer tid = JSONUtil.getInteger(jugador, "info.team.id");
		
		Jugador j = new Jugador(pid, nombre, edad, value, tid, usuario);

		j.setJornada(jornada_actual);
		j.setForma(JSONUtil.getInteger(jugador, "info.skills.form"));
		j.setPais(JSONUtil.getInteger(jugador, "info.country.code"));
		j.setFecha(new Date());
		j.setTarjetas(JSONUtil.getInteger(jugador, nt ? "info.nationalStats.cards.cards" : "info.stats.cards.cards"));
		j.setNt(JSONUtil.getInteger(jugador, "info.team.nationalType"));
		j.setLesion(JSONUtil.getInteger(jugador, "info.injury.daysRemaining"));
		j.setEn_venta(JSONUtil.getJSON(jugador, "transfer.deadline.date") == null ? null : 1);	// Sin info de anuncio
		
		// NTDB
		j.setDisciplina_tactica(JSONUtil.getInteger(jugador, "info.skills.tacticalDiscipline"));

		j.setSalario(JSONUtil.getInteger(jugador, "info.wage.value"));
		j.setExperiencia(JSONUtil.getInteger(jugador, "info.skills.experience"));
		j.setTrabajo_en_equipo(JSONUtil.getInteger(jugador, "info.skills.teamwork"));
		j.setAltura(JSONUtil.getInteger(jugador, "info.characteristics.height"));
		Double peso = JSONUtil.getDouble(jugador, "info.characteristics.weight");	// Devuelve 84.7707
		j.setPeso(peso == null ? null : (int)(peso * 10D));	// Multiplico por 10 porque antes era un entero y al mostrarlo lo divido
		Double imc = JSONUtil.getDouble(jugador, "info.characteristics.bmi");	// Devuelve 25.03860467863894
		j.setIMC(imc == null ? null : (int)(imc * 100));	// Multiplico por 100 porque antes era un entero y al mostrarlo lo divido

		// Solo aparece en el json del jugador
		Integer dem = JSONUtil.getInteger(jugador, "info.formation.code");
		j.setDemarcacion_entrenamiento(dem == null ? null : DEMARCACION.values()[dem]);
		
		try {
			j.setCondicion(JSONUtil.getInteger(jugador, "info.skills.stamina"));
			j.setRapidez(JSONUtil.getInteger(jugador, "info.skills.pace"));
			j.setTecnica(JSONUtil.getInteger(jugador, "info.skills.technique"));
			j.setPases(JSONUtil.getInteger(jugador, "info.skills.passing"));
			j.setPorteria(JSONUtil.getInteger(jugador, "info.skills.keeper"));
			j.setDefensa(JSONUtil.getInteger(jugador, "info.skills.defending"));
			j.setCreacion(JSONUtil.getInteger(jugador, "info.skills.playmaking"));
			j.setAnotacion(JSONUtil.getInteger(jugador, "info.skills.striker"));

			j.setActualizado(true); // Actualizado automáticamente
		} catch (Exception e) {
			// En las NTs no tendremos las habilidades
		}

		if (nt && j.getEn_venta() != null) {
			try {
				new Navegador(false, null) {
					@Override
					protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
						JugadorBO.actualizar_jugador(navegador, j, true);
						j.setActualizado(true); // Actualizado automáticamente
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
	
	public static Jugador obtener_jugador(Integer pid, boolean nt, int jornada_actual, boolean incrementar_edad, Usuario usuario, WebClient navegador) throws MalformedURLException, IOException {
		try {
			Object pagina = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/player/" + pid);
			LinkedHashMap<String, Object> jugador = JsonPath.read(pagina, "$");
			
			if (jugador == null) {
				return null;
			}
			
			return leer_jugador(pid, nt, jornada_actual, incrementar_edad, usuario, jugador);
		} catch (FailingHttpStatusCodeException e) {
			if (e.getStatusCode() == 404) {
				// El jugador no existe. La página devuelve este código de error
				return null;
			} else {
				throw e;
			}
		}
	}
		
	public static List<Jugador> obtener_jugadores(int tid, int jornada_actual, boolean incrementar_edad, Usuario usuario, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		ArrayList<Jugador> lista = new ArrayList<Jugador>();

		Object pagina = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/player?filter[limit]=200&filter[offset]=0&filter[team]=" + tid);
SERVLET_ASISTENTE._log_linea("_XMLS", "__TID: " + tid + "__\n" + pagina.toString());

		List<LinkedHashMap<String, Object>> jugadores = JsonPath.read(pagina, "$.players.*");
		for (LinkedHashMap<String, Object> jugador : jugadores) {

			Integer id = JSONUtil.getInteger(jugador, "id");
			Jugador j;
			
			if (tid > NtdbBO.MAX_ID_SELECCION) {
				// En equipos leemos cada jugador uno a uno porque desde aquí no podemos acceder al tipo de entrenamiento
				j = obtener_jugador(id, false, jornada_actual, incrementar_edad, usuario, navegador);
			} else {
				j = leer_jugador(id, true, jornada_actual, incrementar_edad, usuario, jugador);
			}
			
			if (j != null) {
				lista.add(j);
			}
		}

		if (tid > NtdbBO.MAX_ID_SELECCION) {
			// Compruebo qué jugadores tienen marcado el entrenamiento avanzado
			Set<Integer> avanzados = obtener_entrenamiento_avanzado(navegador);
			for (Jugador j : lista) {
				j.setEntrenamiento_avanzado(avanzados.contains(j.getPid()));
			}
		}
		
		return lista;
	}
	
	public static Set<Integer> obtener_entrenamiento_avanzado(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Set<Integer> avanzados = new HashSet<>();
		
		Object document = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/training/players");
		List<Integer> lista = JsonPath.read(document, "$.advanced[*].id");
		for (Integer pid : lista) {
			avanzados.add(pid);
		}
		
		return avanzados;
	}

	public static Jugador leer_jugador_entrenamiento(boolean avanzado, int jornada_actual, boolean incrementar_edad, Usuario usuario, LinkedHashMap<String, Object> jugador) throws IOException {
		String nombre = JSONUtil.getString(jugador, "info.name.full");
		// NOTA: el jueves de la jornada 0 el jugador todavía no ha cumplido años. Le sumamos un año
		Integer edad = JSONUtil.getInteger(jugador, "info.characteristics.age") + (incrementar_edad ? 1 : 0);
		// NOTA: con la api, el valor viene en la moneda del usuario. La guardamos en euros para compatibilizar los valores anteriores
		Integer value = usuario.convertir_euros(JSONUtil.getInteger(jugador, "info.value.value"));
		Integer tid = JSONUtil.getInteger(jugador, "info.team.id");
		Integer pid = JSONUtil.getInteger(jugador, "id");
		
		Jugador j = new Jugador(pid, nombre, edad, value, tid, usuario);

		j.setJornada(jornada_actual);
		j.setForma(JSONUtil.getInteger(jugador, "info.skills.form"));
		j.setPais(JSONUtil.getInteger(jugador, "info.country.code"));
		j.setFecha(new Date());
		j.setTarjetas(JSONUtil.getInteger(jugador, "info.stats.cards.cards"));
		// No está disponible, le ponemos false
		//j.setNt(JSONUtil.getInteger(jugador, "info.team.nationalType"));
		j.setNt(0);
		j.setLesion(JSONUtil.getInteger(jugador, "info.injury.daysRemaining"));
//		j.setEn_venta(JSONUtil.getJSON(jugador, "transfer.deadline.date") == null ? null : 1);	// Sin info de anuncio
		
		// NTDB
		j.setDisciplina_tactica(JSONUtil.getInteger(jugador, "info.skills.tacticalDiscipline"));

		j.setSalario(JSONUtil.getInteger(jugador, "info.wage.value"));
		j.setExperiencia(JSONUtil.getInteger(jugador, "info.skills.experience"));
		j.setTrabajo_en_equipo(JSONUtil.getInteger(jugador, "info.skills.teamwork"));
		j.setAltura(JSONUtil.getInteger(jugador, "info.characteristics.height"));
		Double peso = JSONUtil.getDouble(jugador, "info.characteristics.weight");	// Devuelve 84.7707
		j.setPeso(peso == null ? null : (int)(peso * 10D));	// Multiplico por 10 porque antes era un entero y al mostrarlo lo divido

		Double imc = JSONUtil.getDouble(jugador, "info.characteristics.bmi");	// Devuelve 25.03860467863894
		j.setIMC(imc == null ? null : (int)(imc * 100D));	// Multiplico por 100 porque antes era un entero y al mostrarlo lo divido

		Integer dem = JSONUtil.getInteger(jugador, "formation.code");
		j.setDemarcacion_entrenamiento(dem == null ? null : DEMARCACION.values()[dem]);
		if (j.getDemarcacion() == null) {
			j.setDemarcacion(j.getDemarcacion_entrenamiento());
		}
		
		try {
			j.setCondicion(JSONUtil.getInteger(jugador, "info.skills.stamina"));
			j.setRapidez(JSONUtil.getInteger(jugador, "info.skills.pace"));
			j.setTecnica(JSONUtil.getInteger(jugador, "info.skills.technique"));
			j.setPases(JSONUtil.getInteger(jugador, "info.skills.passing"));
			j.setPorteria(JSONUtil.getInteger(jugador, "info.skills.keeper"));
			j.setDefensa(JSONUtil.getInteger(jugador, "info.skills.defending"));
			j.setCreacion(JSONUtil.getInteger(jugador, "info.skills.playmaking"));
			j.setAnotacion(JSONUtil.getInteger(jugador, "info.skills.striker"));

			j.setActualizado(true); // Actualizado automáticamente
		} catch (Exception e) {
			// En las NTs no tendremos las habilidades
		}
		
		j.setMinutos((float)JSONUtil.getInteger(jugador, "effectiveness"));	// Tb "intensity", tiene el mismo valor
		j.setEntrenamiento_avanzado(avanzado);
		
		return j;
	}
	
	public static List<Jugador> obtener_entrenamiento(Usuario usuario, int jornada_actual, boolean incrementar_edad, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		List<Jugador> jugadores = new ArrayList<>();
		
		Object pagina = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/training/players");
		List<LinkedHashMap<String, Object>> general = JsonPath.read(pagina, "$.general");
		for (LinkedHashMap<String, Object> g : general) {
			jugadores.add(leer_jugador_entrenamiento(false, jornada_actual, incrementar_edad, usuario, g));
		}

		List<LinkedHashMap<String, Object>> avanzado = JsonPath.read(pagina, "$.advanced");
		for (LinkedHashMap<String, Object> a : avanzado) {
			jugadores.add(leer_jugador_entrenamiento(true, jornada_actual, incrementar_edad, usuario, a));
		}

		return jugadores;
	}
	
	public static void obtener_entrenamientos_pasados(int tid, Usuario usuario, int jornada_actual, List<Jugador> jugadores, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		/***************************************************************************
		** NOTA: pido una jornada más para evitar un bug de Sokker que me manda los datos vacíos en la primera jornada al filtrar las jornadas
		* 17/03/2024: /api/training/[pid]/report es solo para pluses
		****************************************************************************/
		int num_jornadas = jornada_actual - usuario.getDef_jornada() + 1;
//System.out.println(jornada_actual + " - " + usuario.getDef_jornada() + " = " + num_jornadas);

		if (num_jornadas > 1) {
			for (Jugador j : jugadores) {
				Object pagina = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/training/" + j.getPid() + "/report?filter[limit]=" + num_jornadas);
SERVLET_ASISTENTE._log_linea("_XMLS", "__TID: " + tid + "__\n" + pagina.toString());
				List<LinkedHashMap<String, Object>> entrenamientos = JsonPath.read(pagina, "$.reports");
				
//System.out.println(j.getPid() + "[" + j.getEdad() + "] -> " + entrenamientos.size());
				for (int i = 0; i < entrenamientos.size() - 1; i++) {
					LinkedHashMap<String, Object> entrenamiento = entrenamientos.get(i);
//				for (LinkedHashMap<String, Object> entrenamiento : entrenamientos) {
					// NOTA: Sokker le suma 1 a la semana del entrenamiento
					// En realidad los datos corresponden al momento justo después del entrenamiento, incluida la jornada
					// Pero nosotros tenemos que asignar los datos del entrenamiento a la semana anterior y a las habilidades restarles las subidas
					int week = JSONUtil.getInteger(entrenamiento, "week") - 1;
//System.out.println(week);
					// Este es el valor después del entrenamiento -> no hay una forma fácil de saber cuál era el valor antes
					int valor = usuario.convertir_euros(JSONUtil.getInteger(entrenamiento, "playerValue.value"));
	
					// Buscamos al jugador de la semana del entrenamiento
					Jugador jug_semana = j.buscar_jornada(week);
					if (jug_semana == null) {
						// Si la semana es nueva, lo creamos
						jug_semana = new Jugador(j.getPid(), j.getNombre(), j.getEdad(), valor, tid, usuario);
						jug_semana.setJornada(week);
						j = AsistenteBO.combinar_jugadores(j, jug_semana);
					}
	
					Integer cod_dem = JSONUtil.getInteger(entrenamiento, "formation.code");
					if (cod_dem != null) {
						DEMARCACION dem = DEMARCACION.values()[cod_dem];
						jug_semana.setDemarcacion_entrenamiento(dem);
						int tipo = JSONUtil.getInteger(entrenamiento, "type.code") - 1;
						// Ha habido un caso en que el tipo era 0 (General): ¿aún no efectuado?
						if (tipo >= 0) {
							TIPO_ENTRENAMIENTO ent = TIPO_ENTRENAMIENTO.values()[tipo];
							usuario.getTipo_entrenamiento(cod_dem).put(week, ent);
						}
					}
					
					jug_semana.setMinutos(JSONUtil.getDouble(entrenamiento, "intensity").floatValue());
					jug_semana.setEntrenamiento_avanzado(JSONUtil.getInteger(entrenamiento, "kind.code") == 1);	// 0 = General (aún no efectuado, creo), 1 = Avanzado, 2 = formación
					jug_semana.setLesion(JSONUtil.getInteger(entrenamiento, "injury.daysRemaining"));
					if (JSONUtil.getBoolean(entrenamiento, "injury.severe") && jug_semana.getLesion() == 7) {
						// Como para mí 7 significa que la lesión ya no es severa, le sumo 1
						jug_semana.setLesion(8);
					}
//if (j.getPid() == 38745399) {
//	System.out.println(j.getNombre() + " " + week + " " + JSONUtil.getInteger(entrenamiento, "type.code") + " " + j.getMinutos());
//}
					
					// Habilidades
					jug_semana.setForma(JSONUtil.getSkills(entrenamiento, "form"));
					jug_semana.setDisciplina_tactica(JSONUtil.getSkills(entrenamiento, "tacticalDiscipline"));
					jug_semana.setTrabajo_en_equipo(JSONUtil.getSkills(entrenamiento, "teamwork"));
					jug_semana.setExperiencia(JSONUtil.getSkills(entrenamiento, "experience"));
	
					jug_semana.setCondicion(JSONUtil.getSkills(entrenamiento, "stamina"));
					jug_semana.setRapidez(JSONUtil.getSkills(entrenamiento, "pace"));
					jug_semana.setTecnica(JSONUtil.getSkills(entrenamiento, "technique"));
					jug_semana.setPases(JSONUtil.getSkills(entrenamiento, "passing"));
					jug_semana.setPorteria(JSONUtil.getSkills(entrenamiento, "keeper"));
					jug_semana.setDefensa(JSONUtil.getSkills(entrenamiento, "defending"));
					jug_semana.setCreacion(JSONUtil.getSkills(entrenamiento, "playmaking"));
					jug_semana.setAnotacion(JSONUtil.getSkills(entrenamiento, "striker"));
				}
			}
		}
	}
	
	public static void obtener_datos_NT(WebClient navegador, Usuario usuario) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Object document = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/current");
		LinkedHashMap<String, Object> datos = JsonPath.read(document, "$");

		int tid_nt = JSONUtil.getInteger(datos, "nationalTeamId");
		usuario.setTid_nt(tid_nt == 0 ? null : tid_nt);
		usuario.setEquipo_nt(JSONUtil.getString(datos, "nationalTeam.name"));
	}

	public static void obtener_juvenil(Juvenil juvenil, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Object document = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/junior/" + juvenil.getPid() + "/graph");
		List<LinkedHashMap<String, Object>> datos = JsonPath.read(document, "$.values");
		//juvenil.setOriginal(null);
		
		for (LinkedHashMap<String, Object> dato : datos) {
			Integer week = JSONUtil.getInteger(dato, "x") - 1;
			if (week < juvenil.getJornada() && juvenil.buscar_jornada(week) == null) {
				Integer nivel = JSONUtil.getInteger(dato, "y");
				Juvenil j = new Juvenil(juvenil.getPid(), juvenil.getNombre(), week, juvenil.getEdad(), nivel, juvenil.getSemanas() + juvenil.getJornada() - week, juvenil.isJugador_campo(), juvenil.getUsuario());
				//juvenil.anyadir_original(j);
				juvenil.insertar_jornada(j);
			}
		}
	}

	public static PalmaresEquipo obtener_palmares(PalmaresEquipo palmares, String competicion, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Object document = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/team/" + palmares.getTid() + "/trophies");
		List<LinkedHashMap<String, Object>> trofeos = JsonPath.read(document, "$.trophies");
		for (LinkedHashMap<String, Object> trofeo : trofeos) {
			 int posicion = JSONUtil.getInteger(trofeo, "position");
			 int nivel = JSONUtil.getInteger(trofeo, "level");
			 int ocurrencias = JSONUtil.getInteger(trofeo, "occurrences");
			 String tipo = JSONUtil.getString(trofeo, "type");
			 if (nivel == 1 && tipo.equals(competicion)) {
				 palmares.getPosicion()[posicion - 1] = ocurrencias;
			}
		}
		
		return palmares;
	}
	
	public static boolean es_plus(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Object document = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/current");
		Boolean plus = JsonPath.read(document, "$.plus");
		return plus;
	}
}
