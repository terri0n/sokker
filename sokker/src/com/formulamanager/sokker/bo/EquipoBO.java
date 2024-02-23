package com.formulamanager.sokker.bo;

import java.io.IOException;
import java.net.MalformedURLException;

import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

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
	
	public static void obtener_datos_equipo(Usuario usuario, int jornada_anterior, int jornada_actual, WebClient navegador) {
		try {
			// Actualizo la jornada de la última actualización
			usuario.setDef_jornada(jornada_actual);

			XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/team-" + usuario.getDef_tid() + ".xml");
			
			String equipo = ((DomText) pagina.getFirstByXPath("//teamdata/team/name/text()")).asText();
			
			if (usuario.getDef_tid() < NtdbBO.MAX_ID_SELECCION) {
				usuario.setEquipo_nt(equipo);
			} else {
				usuario.setEquipo(equipo);
				usuario.setCountryID(new Integer(((DomText) pagina.getFirstByXPath("//teamdata/team/countryID/text()")).asText()));

				// Resto 1 porque empiezan en 1
				TIPO_ENTRENAMIENTO tipo = TIPO_ENTRENAMIENTO.values()[
				     new Integer(((DomText) pagina.getFirstByXPath("//teamdata/team/trainingType/text()")).asText()) - 1
				];
	
				// Aquí empiezan en 0
				DEMARCACION demarcacion = DEMARCACION.values()[
				     new Integer(((DomText) pagina.getFirstByXPath("//teamdata/team/trainingFormation/text()")).asText())
				];

				// Resto 1 porque empiezan en 1
				TIPO_ENTRENAMIENTO tipoGk = TIPO_ENTRENAMIENTO.values()[
				     new Integer(((DomText) pagina.getFirstByXPath("//teamdata/team/trainingTypeGk/text()")).asText()) - 1
				];
				TIPO_ENTRENAMIENTO tipoDef = TIPO_ENTRENAMIENTO.values()[
   				     new Integer(((DomText) pagina.getFirstByXPath("//teamdata/team/trainingTypeDef/text()")).asText()) - 1
   				];
				TIPO_ENTRENAMIENTO tipoMid = TIPO_ENTRENAMIENTO.values()[
				     new Integer(((DomText) pagina.getFirstByXPath("//teamdata/team/trainingTypeMid/text()")).asText()) - 1
				];
				TIPO_ENTRENAMIENTO tipoAtt = TIPO_ENTRENAMIENTO.values()[
   				     new Integer(((DomText) pagina.getFirstByXPath("//teamdata/team/trainingTypeAtt/text()")).asText()) - 1
   				];
				
				
				// Actualizo todas las jornadas desde la última actualización...
				// solo si no se actualizó al obtener entrenamientos pasados
				for (int j = jornada_anterior; j <= usuario.getDef_jornada(); j++) {
					if (j < AsistenteBO.JORNADA_NUEVO_ENTRENO) {
						usuario.getTipo_entrenamiento(0).put(j, tipo);
						usuario.getDemarcacion().put(j, demarcacion);
					} else {
						if (usuario.getTipo_entrenamiento(0).get(j) == null || j == jornada_actual) {
							usuario.getTipo_entrenamiento(0).put(j, tipoGk);
						}
						if (usuario.getTipo_entrenamiento(1).get(j) == null || j == jornada_actual) {
							usuario.getTipo_entrenamiento(1).put(j, tipoDef);
						}
						if (usuario.getTipo_entrenamiento(2).get(j) == null || j == jornada_actual) {
							usuario.getTipo_entrenamiento(2).put(j, tipoMid);
						}
						if (usuario.getTipo_entrenamiento(3).get(j) == null || j == jornada_actual) {
							usuario.getTipo_entrenamiento(3).put(j, tipoAtt);
						}
					}
				}

				
				//---------------
				// INTERFAZ JSON
				//---------------
/*				
//				Object document = Navegador.get_json(navegador, AsistenteBO.SOKKER_URL + "/api/training/formations");
				Object document = Navegador.get_json(navegador, "http://raqueto.com/var/formations.json");
				List<LinkedHashMap<String, LinkedHashMap>> lista = JsonPath.read(document, "$.formations.*");
				for (LinkedHashMap<String, LinkedHashMap> s : lista) {
					DEMARCACION demarcacion = DEMARCACION.values()[(Integer)s.get("formation").get("code")];
					TIPO_ENTRENAMIENTO tipo = TIPO_ENTRENAMIENTO.values()[(Integer)s.get("type").get("code")];

					// Actualizo todas las jornadas desde la �ltima actualizaci�n
					for (int j = jornada_anterior; j <= usuario.getDef_jornada(); j++) {
						usuario.getTipo_entrenamiento(demarcacion.ordinal()).put(j, tipo);
					}
				}
				
//				document = Navegador.get_json(navegador, "http://raqueto.com/var/players.json");
//				List<Integer> avanzados = JsonPath.read(document, "$.advanced[*].id");
//				System.out.println(avanzados);
*/				
			}
			


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String obtener_login(int tid, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/team-" + tid + ".xml");
		DomText login = pagina.getFirstByXPath("//teamdata/user/login/text()");
		return login == null ? null : login.asText();
	}

	public static String obtener_nombre(int tid, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/team-" + tid + ".xml");
		DomText nombre = pagina.getFirstByXPath("//teamdata/team/name/text()");
		return nombre.asText();
	}
}
