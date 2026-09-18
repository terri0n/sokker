package com.formulamanager.sokker.bo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import com.formulamanager.sokker.auxiliares.JSONUtil;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import com.jayway.jsonpath.JsonPath;

public class EquipoBO {
	// NOTA: En Sokker el índice viene con +1 en los XML/JSON!!!
	public static enum TIPO_ENTRENAMIENTO {
		Condicion("stamina"), 
		Porteria("keeper"), 
		Creacion("playmaker"), 
		Pases("passing"), 
		Tecnica("technique"), 
		Defensa("defender"), 
		Anotacion("striker"), 
		Rapidez("pace");
		
		private String ingles;

		private TIPO_ENTRENAMIENTO(String ingles) {
			this.ingles = ingles;
		}
		
		public String getIngles() {
			return ingles;
		}
		
		public float getFactor(Usuario usuario) {
			// En la configuración divido entre el valor máximo para que los factores tengan el valor 1 como referencia. Aquí deshago esa división
			switch (this) {
				case Rapidez:
					return 6.8f * usuario.getFactor_rapidez();
				case Defensa:
					return 6.8f * usuario.getFactor_defensa();
				case Anotacion:
					return 6.8f * usuario.getFactor_anotacion();
				case Porteria:
					return 6.8f * usuario.getFactor_porteria();
				case Tecnica:
					return 6.8f * usuario.getFactor_tecnica();
				case Creacion:
					return 6.8f * usuario.getFactor_creacion();
				case Pases:
					return 6.8f * usuario.getFactor_pases();
				default:
					return 0f;
			}
		}
	};

	private static TIPO_ENTRENAMIENTO obtener_tipo_entrenamiento(String tipo) {
		if (tipo == null) {
			return null;
		}

		switch (tipo.toLowerCase(Locale.ROOT)) {
			case "stamina":
				return TIPO_ENTRENAMIENTO.Condicion;
			case "keeper":
				return TIPO_ENTRENAMIENTO.Porteria;
			case "playmaker":
			case "playmaking":
				return TIPO_ENTRENAMIENTO.Creacion;
			case "passing":
				return TIPO_ENTRENAMIENTO.Pases;
			case "technique":
				return TIPO_ENTRENAMIENTO.Tecnica;
			case "defender":
			case "defending":
				return TIPO_ENTRENAMIENTO.Defensa;
			case "striker":
			case "scoring":
				return TIPO_ENTRENAMIENTO.Anotacion;
			case "pace":
				return TIPO_ENTRENAMIENTO.Rapidez;
			default:
				throw new IllegalArgumentException("Tipo de entrenamiento desconocido: " + tipo);
		}
	}

	@SuppressWarnings("unchecked")
	private static TIPO_ENTRENAMIENTO[] obtener_entrenamientos(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Object document = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/training/formations");
		List<LinkedHashMap<String, Object>> formaciones = JsonPath.read(document, "$.formations");
		TIPO_ENTRENAMIENTO[] tipos = new TIPO_ENTRENAMIENTO[DEMARCACION.values().length];

		for (LinkedHashMap<String, Object> formacion : formaciones) {
			Integer codigo = JSONUtil.getInteger(formacion, "formation.code");
			if (codigo == null || codigo < 0 || codigo >= tipos.length) {
				throw new IllegalArgumentException("Demarcación de entrenamiento desconocida: " + codigo);
			}
			tipos[codigo] = obtener_tipo_entrenamiento(JSONUtil.getString(formacion, "type.name"));
		}

		for (int i = 0; i < tipos.length; i++) {
			if (tipos[i] == null) {
				throw new IllegalArgumentException("Falta el entrenamiento de " + DEMARCACION.values()[i]);
			}
		}

		return tipos;
	}
	
	@SuppressWarnings("unchecked")
	public static void obtener_datos_equipo(Usuario usuario, int jornada_anterior, int jornada_actual, WebClient navegador)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Object document = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/team/" + usuario.getDef_tid());
		LinkedHashMap<String, Object> datos_equipo = JsonPath.read(document, "$");
		String equipo = JSONUtil.getString(datos_equipo, "name");
		if (equipo == null) {
			throw new IllegalArgumentException("El equipo no tiene nombre");
		}

		TIPO_ENTRENAMIENTO[] entrenamientos = null;
		Integer countryID = null;
		if (usuario.getDef_tid() > NtdbBO.MAX_ID_SELECCION) {
			countryID = JSONUtil.getInteger(datos_equipo, "country.code");
			if (countryID == null) {
				throw new IllegalArgumentException("El equipo no tiene país");
			}
			entrenamientos = obtener_entrenamientos(navegador);
		}

		// Aplicamos los datos solo después de haber validado todas las respuestas JSON necesarias.
		usuario.setDef_jornada(jornada_actual);
		if (usuario.getDef_tid() < NtdbBO.MAX_ID_SELECCION) {
			usuario.setEquipo_nt(equipo);
		} else {
			usuario.setEquipo(equipo);
			usuario.setCountryID(countryID);

			// La API moderna solo describe el sistema actual de cuatro demarcaciones.
			// No inventamos valores históricos anteriores al cambio de entrenamiento.
			for (int j = Math.max(jornada_anterior, AsistenteBO.JORNADA_NUEVO_ENTRENO); j <= usuario.getDef_jornada(); j++) {
				for (int i = 0; i < entrenamientos.length; i++) {
					if (usuario.getTipo_entrenamiento(i).get(j) == null || j == jornada_actual) {
						usuario.getTipo_entrenamiento(i).put(j, entrenamientos[i]);
					}
				}
			}
		}
	}

	public static String obtener_login(int tid, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/team-" + tid + ".xml");
		DomText login = pagina.getFirstByXPath("//teamdata/user/login/text()");
		return login == null ? null : login.asText();
	}

	@SuppressWarnings("unchecked")
	public static String obtener_nombre(int tid, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Object document = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/team/" + tid);
		LinkedHashMap<String, Object> datos_equipo = JsonPath.read(document, "$");
		String nombre = JSONUtil.getString(datos_equipo, "name");
		if (nombre == null) {
			throw new IllegalArgumentException("El equipo no tiene nombre");
		}
		return nombre;
	}
}