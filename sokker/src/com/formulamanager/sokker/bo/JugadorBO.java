package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class JugadorBO {

	/** Shared filesystem root used by non-player application resources. */
	public static String PATH_BASE = System.getProperty("os.name").contains("Windows") ? "d:\\levi\\home\\" : "/home/";

	@Deprecated
	public static WebClient hacer_login(HttpServletRequest request) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient navegador = new WebClient();
		navegador.getOptions().setJavaScriptEnabled(false);
		navegador.getOptions().setCssEnabled(false);
		navegador.getOptions().setUseInsecureSSL(true);

		//Proceso de login.
		HtmlPage paginaLogin = navegador.getPage(AsistenteBO.SOKKER_URL + "/logon");
		
		HtmlForm formularioLogin = paginaLogin.getElementByName("logform");
		
		HtmlButton botonFormularioLogin = formularioLogin.getFirstByXPath("//button[@type='submit']");
		formularioLogin.getInputByName("ilogin").setValueAttribute(SystemUtil.getVar(SystemUtil.LOGIN));
		formularioLogin.getInputByName("ipassword").setValueAttribute(SystemUtil.getVar(SystemUtil.PASSWORD));
	
		botonFormularioLogin.click();
		
		return navegador;
	}

	public static HtmlPage hacer_login_xml(WebClient navegador, HttpServletRequest request) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		//Proceso de login.
		HtmlPage paginaLogin = navegador.getPage(AsistenteBO.SOKKER_URL + "/xmlinfo.php");
		
		HtmlForm formularioLogin = paginaLogin.getFirstByXPath("//form");
		
		HtmlSubmitInput botonFormularioLogin = formularioLogin.getFirstByXPath("//input[@type='submit']");
		formularioLogin.getInputByName("ilogin").setValueAttribute(SystemUtil.getVar(SystemUtil.LOGIN));
		formularioLogin.getInputByName("ipassword").setValueAttribute(SystemUtil.getVar(SystemUtil.PASSWORD));
	
		return botonFormularioLogin.click();
	}

	// OJO: esta función se llama tanto desde el factorx como del asistente
	public static boolean actualizar_jugador(WebClient navegador, Jugador jugador, boolean forzar) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/player/PID/" + jugador.getPid());

		HtmlAnchor a = (HtmlAnchor)pagina.getFirstByXPath("//div[@class='panel-heading']//a[contains(@href,'player/PID/" + jugador.getPid() + "')]");
		if (a == null) {
			// Jugador no encontrado
			return false;
		}
		jugador.setNombre(a.getTextContent());
		jugador.setEdad(Math.min(jugador.getEdad(), Integer.valueOf(a.getNextElementSibling().getTextContent())));
	
		a = (HtmlAnchor)pagina.getFirstByXPath("//a[contains(@href,'app/team/')]");
		if (a == null) {
			// Jugador despedido
			jugador.setEquipo(null);
		} else {
			jugador.setEquipo(a.getTextContent());
			jugador.setTid(Integer.valueOf(a.getAttributes().getNamedItem("href").getNodeValue().split("/")[2]));
		}
		
		a = (HtmlAnchor)pagina.getFirstByXPath("//div[@class='media-body']//a[contains(@href,'country/ID_country/')]");
		jugador.setPais(Integer.valueOf(a.getHrefAttribute().split("/")[2]));
		
		HtmlSpan span = (HtmlSpan)pagina.getFirstByXPath("//li[contains(.,'value') or contains(.,'valor')]/span");
		jugador.setValor(Integer.valueOf(span.getTextContent().replaceAll("\\D+", "")));
		
		span = (HtmlSpan)pagina.getFirstByXPath("//td[contains(.,'stamina') or contains(.,'condición')]/strong/span[@class='skillNameNumber']");
		if (span == null) {
			// Sin anuncio de transferencia
			return forzar;
		}
		jugador.setCondicion(Integer.valueOf(span.getTextContent().replaceAll("\\D+", "")));

		span = (HtmlSpan)pagina.getFirstByXPath("//td[contains(.,'pace') or contains(.,'rapidez')]/strong/span[@class='skillNameNumber']");
		jugador.setRapidez(Integer.valueOf(span.getTextContent().replaceAll("\\D+", "")));
	
		span = (HtmlSpan)pagina.getFirstByXPath("//td[contains(.,'technique') or contains(.,'técnica')]/strong/span[@class='skillNameNumber']");
		jugador.setTecnica(Integer.valueOf(span.getTextContent().replaceAll("\\D+", "")));
	
		span = (HtmlSpan)pagina.getFirstByXPath("//td[contains(.,'passing') or contains(.,'pases')]/strong/span[@class='skillNameNumber']");
		jugador.setPases(Integer.valueOf(span.getTextContent().replaceAll("\\D+", "")));
	
		span = (HtmlSpan)pagina.getFirstByXPath("//td[contains(.,'keeper') or contains(.,'portería')]/strong/span[@class='skillNameNumber']");
		jugador.setPorteria(Integer.valueOf(span.getTextContent().replaceAll("\\D+", "")));
	
		span = (HtmlSpan)pagina.getFirstByXPath("//td[contains(.,'defender') or contains(.,'defensa')]/strong/span[@class='skillNameNumber']");
		jugador.setDefensa(Integer.valueOf(span.getTextContent().replaceAll("\\D+", "")));
	
		span = (HtmlSpan)pagina.getFirstByXPath("//td[contains(.,'playmaker') or contains(.,'creación')]/strong/span[@class='skillNameNumber']");
		jugador.setCreacion(Integer.valueOf(span.getTextContent().replaceAll("\\D+", "")));
	
		span = (HtmlSpan)pagina.getFirstByXPath("//td[contains(.,'striker') or contains(.,'anotación')]/strong/span[@class='skillNameNumber']");
		jugador.setAnotacion(Integer.valueOf(span.getTextContent().replaceAll("\\D+", "")));
		
		return true;
	}

//	public static boolean actualizar_jugador_transferencia(DomNode pagina, Jugador jugador, boolean forzar) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
//		HtmlAnchor a = (HtmlAnchor)pagina.getFirstByXPath("div//a[contains(@href,'player/PID/')]");
//		jugador.setPid(Integer.valueOf(a.getHrefAttribute().split("/")[2]));
//		jugador.setNombre(a.getTextContent());
//		jugador.setEdad(Integer.valueOf(a.getParentNode().getTextContent().split("edad")[1].trim()));
//	
//		a = (HtmlAnchor)pagina.getFirstByXPath("div//a[contains(@href,'team/teamID/')]");
//		if (a == null) {
//			// Jugador despedido
//			jugador.setEquipo(null);
//		} else {
//			jugador.setEquipo(a.getTextContent());
//			jugador.setTid(Integer.valueOf(a.getAttributes().getNamedItem("href").getNodeValue().split("/")[2]));
//		}
//		
//		HtmlUnknownElement strng = (HtmlUnknownElement)pagina.getFirstByXPath("div[contains(., 'valor')]/strng");	// <- Falta una "o" en la p�gina devuelta por Sokker
//		jugador.setValor(Integer.valueOf(strng.getTextContent().split("�")[0].replace(" ", "")));
//
//		HtmlStrong strong = (HtmlStrong)pagina.getFirstByXPath("div//table[@class='table table-condensed table-skills small']//td[contains(.,'condici�n')]/strong");
//		if (strong == null) {
//			// Sin anuncio de transferencia
//			return forzar;
//		}
//		jugador.setCondicion(buscar_indice(jugador.getCondicion(), strong.getTextContent().trim()));
//		
//		strong = (HtmlStrong)pagina.getFirstByXPath("div//table[@class='table table-condensed table-skills small']//td[contains(.,'rapidez')]/strong");
//		jugador.setRapidez(buscar_indice(jugador.getRapidez(), strong.getTextContent().trim()));
//	
//		strong = (HtmlStrong)pagina.getFirstByXPath("div//table[@class='table table-condensed table-skills small']//td[contains(.,'t�cnica')]/strong");
//		jugador.setTecnica(buscar_indice(jugador.getTecnica(), strong.getTextContent().trim()));
//	
//		strong = (HtmlStrong)pagina.getFirstByXPath("div//table[@class='table table-condensed table-skills small']//td[contains(.,'pases')]/strong");
//		jugador.setPases(buscar_indice(jugador.getPases(), strong.getTextContent().trim()));
//	
//		strong = (HtmlStrong)pagina.getFirstByXPath("div//table[@class='table table-condensed table-skills small']//td[contains(.,'porter�a')]/strong");
//		jugador.setPorteria(buscar_indice(jugador.getPorteria(), strong.getTextContent().trim()));
//	
//		strong = (HtmlStrong)pagina.getFirstByXPath("div//table[@class='table table-condensed table-skills small']//td[contains(.,'defensa')]/strong");
//		jugador.setDefensa(buscar_indice(jugador.getDefensa(), strong.getTextContent().trim()));
//	
//		strong = (HtmlStrong)pagina.getFirstByXPath("div//table[@class='table table-condensed table-skills small']//td[contains(.,'creaci�n')]/strong");
//		jugador.setCreacion(buscar_indice(jugador.getCreacion(), strong.getTextContent().trim()));
//	
//		strong = (HtmlStrong)pagina.getFirstByXPath("div//table[@class='table table-condensed table-skills small']//td[contains(.,'anotaci�n')]/strong");
//		jugador.setAnotacion(buscar_indice(jugador.getAnotacion(), strong.getTextContent().trim()));
//		
//		return true;
//	}
	
//	private static Integer buscar_indice(Integer actual, String nueva) {
////		Integer indice = Arrays.asList(Jugador.valores_m).indexOf(nueva);
////		if (indice == -1) {
////			indice = Arrays.asList(Jugador.valores_f).indexOf(nueva);
////		}
//		Integer indice = Arrays.asList(Jugador.valores).indexOf(nueva);
//		return Math.max(actual == null ? -1 : actual, indice);
//	}

	// Para el escaneador de jugadores
	public static ArrayList<Jugador> obtener_jugadores(Integer tid, Integer filtro_pais, Integer filtro_valor, Integer filtro_edad, WebClient navegador) {
		ArrayList<Jugador> lista = new ArrayList<Jugador>();

		try {
			XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/players-" + tid + ".xml");

			ArrayList<DomNode> jugadores = (ArrayList<DomNode>) pagina.getByXPath("//player");
			for (DomNode jugador : jugadores) {
				Integer id = new Integer(((DomText) jugador.getFirstByXPath("ID/text()")).asText());
				DomNode domNodeN = jugador.getFirstByXPath("name/text()");
				String nombre = domNodeN == null ? null : ((DomText) domNodeN).asText();
				DomNode domNodeA = jugador.getFirstByXPath("surname/text()");
				String apellido = domNodeA == null ? null : ((DomText) domNodeA).asText();
				Integer pais = new Integer(((DomText) jugador.getFirstByXPath("countryID/text()")).asText());
				Integer edad = new Integer(((DomText) jugador.getFirstByXPath("age/text()")).asText());
				Integer valor = new Integer(((DomText) jugador.getFirstByXPath("value/text()")).asText()) / 4;
				Integer forma = new Integer(((DomText) jugador.getFirstByXPath("skillForm/text()")).asText());

// filtro_pais = null en las estad�sticas de forma
				if (filtro_pais == null || pais.equals(filtro_pais) && (filtro_edad == null || edad <= filtro_edad)) {
					int valor_procesado = (int) (valor / (0.55F + (forma/18F) * 0.45F));

					int tope = filtro_valor != null ? filtro_valor : (Math.min(30, edad) - 15) * 60000;

					if (valor_procesado >= tope) {
						Jugador j = new Jugador(id, nombre + " " + apellido, edad, valor_procesado, tid, null/*usuario*/);
						List<Jugador> lista_jugadores = new ArrayList<Jugador>();
						lista_jugadores.add(j);
						if (filtro_pais == null) {
							j.setForma(forma);
						} else {
							obtener_rating(lista_jugadores, tid, navegador);
						}
						lista.add(j);
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
	}

	public static void obtener_rating(List<Jugador> jugadores, int tid, WebClient navegador) {
		try {
			XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/matches-team-" + tid + ".xml");
			ArrayList<DomNode> partidos = (ArrayList<DomNode>) pagina.getByXPath("//match");
			
			for (DomNode partido : partidos) {
				Integer mid = new Integer(((DomText) partido.getFirstByXPath("matchID/text()")).asText());
				pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/match-" + mid + ".xml");

				for (Jugador j : jugadores) {
					DomNode nodoJugador = pagina.getFirstByXPath("//playerStats[playerID='" + j.getPid() + "']");
					if (nodoJugador != null) {
						Integer demarcacion = new Integer(((DomText) nodoJugador.getFirstByXPath("formation/text()")).asText());
						Integer rating = new Integer(((DomText) nodoJugador.getFirstByXPath("rating/text()")).asText());
						if (rating > 0) {
							j.setDemarcacion(DEMARCACION.values()[demarcacion]);
							j.setPuntos(new BigDecimal(rating));
							return;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
