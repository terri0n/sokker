package com.formulamanager.sokker.bo;

import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class EquipoBO {
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
	};
	
	public static void obtener_datos_equipo(Usuario usuario, int jornada_anterior, WebClient navegador) {
		try {
			// Actualizo la jornada de la última actualización
			usuario.setDef_jornada(JugadorBO.obtener_jornada(navegador));

			XmlPage pagina = navegador.getPage("http://sokker.org/xml/team-" + usuario.getDef_tid() + ".xml");
			
			usuario.setCountryID(new Integer(((DomText) pagina.getFirstByXPath("//teamdata/team/countryID/text()")).asText()));
			
			if (usuario.getDef_tid() > 1000) {
				// Resto 1 porque empiezan en 1
				TIPO_ENTRENAMIENTO tipo = TIPO_ENTRENAMIENTO.values()[
				     new Integer(((DomText) pagina.getFirstByXPath("//teamdata/team/trainingType/text()")).asText()) - 1
				];
	
				// Aquí empiezan en 0
				DEMARCACION demarcacion = DEMARCACION.values()[
				     new Integer(((DomText) pagina.getFirstByXPath("//teamdata/team/trainingFormation/text()")).asText())
				];
	
				// Actualizo todas las jornadas desde la última actualización
				for (int j = jornada_anterior; j <= usuario.getDef_jornada(); j++) {
					usuario.getTipo_entrenamiento().put(j, tipo);
					usuario.getDemarcacion().put(j, demarcacion);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
