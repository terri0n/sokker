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

import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX;
import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX.TIPO_FACTORX;
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
	public static String PATH_BASE = System.getProperty("os.name").contains("Windows") ? "d:\\home\\" : "/home/";
	public static String CONFIG_PROPERTIES = PATH_BASE + "factorx/config.properties";
	public static String HISTORICO_PROPERTIES = PATH_BASE + "factorx/historico.properties";
	
	private static String getFormForo() {
		return "<form action=\"" + AsistenteBO.SOKKER_URL + "/forum_topic/#new\" method=\"post\" name=\"newpost\" role=\"form\">\r\n" + 
				"	<input type=\"hidden\" name=\"pg\" value=\"0\">\r\n" + 
				"	<input type=\"hidden\" name=\"post_no\" value=\"1\">\r\n" + 
				"	<input type=\"hidden\" name=\"ID_topic\" value=\"?\">\r\n" + 
				"	<input type=\"hidden\" name=\"action\" value=\"Add\">\r\n" + 
				"	\r\n" + 
				"	<textarea id=\"forum-topic-reply-text\" name=\"text\" rows=\"8\" class=\"form-control\"></textarea>\r\n" + 
				"	<button type=\"submit\" name=\"submit\" value=\"submit\" class=\"btn btn-primary\">Enviar</button>\r\n" + 
				"</form>";
	}
	
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

	public static void escribir_anuncio(WebClient navegador, Jugador j, boolean nuevo) {
		if (j.isActualizado()) {
			System.out.println("Comentario en " + j.getNombre());
			
			try {
				HtmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/player/PID/" + j.getPid());
				pagina = ((HtmlAnchor)pagina.getFirstByXPath("//a[contains(@href,'forum_topic/ID_topic/')]")).click();
				Integer id_topic = new Integer(pagina.getUrl().getPath().split("/")[3]);
				
				System.out.println(" " + id_topic);
				
				StringWebResponse response = new StringWebResponse(getFormForo(), new URL("file:///."));
				HtmlPage form = HTMLParser.parseHtml(response, navegador.getCurrentWindow());
				((HtmlInput)form.getElementByName("ID_topic")).setValueAttribute(id_topic+"");
				((HtmlTextArea)form.getElementByName("text")).setTextContent(nuevo ? "A�adido" : "Actualizado");
				
				HtmlButton botonFormulario = form.getFormByName("newpost").getFirstByXPath("//button[@type='submit']");
				HtmlPage p = botonFormulario.click();
//System.out.println(p.asXml());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static List<Jugador> leer_historico(TIPO_FACTORX tipo, int edicion_actual) {
		List<Jugador> jugadores = new ArrayList<Jugador>();
		
		for (int i = edicion_actual; i > 23; i--) {
			jugadores.addAll(JugadorBO.leer_jugadores(tipo, i, i == edicion_actual));
		}
		
		return jugadores;
	}

	public static List<Jugador> leer_jugadores (SERVLET_FACTORX.TIPO_FACTORX tipo, Integer edicion, boolean es_edicion_actual) {
		List<Jugador> jugadores = new ArrayList<Jugador>();
		Properties prop = new Properties();
		InputStream input = null;
	
		try {
			String BD;
			
			if (es_edicion_actual) {
				BD = JugadorBO.CONFIG_PROPERTIES + tipo.getSufijo();
			} else {
				BD = JugadorBO.HISTORICO_PROPERTIES + edicion + tipo.getSufijo();
			}
			
			File file = new File(BD);
			if (!file.exists()) {
				return jugadores;
			}
			
			input = new FileInputStream(file);
	
			// load a properties file
			prop.load(input);
	
			// Buscamos el mayor �ndice de jugadores
			Set<String> keys = prop.stringPropertyNames();
		    int max = -1;
			for (String key : keys) {
		    	max = Math.max(max, Integer.valueOf(key.split("_")[0]));
		    }
	
			for (int i = 0; i <= max; i++) {
				Jugador j = new Jugador();
				j.setEdad(Integer.valueOf(prop.getProperty(i + "_edad")));
				if (j.getEdad() < 21 ^ tipo == SERVLET_FACTORX.TIPO_FACTORX.senior) {
					j.setOriginal(new Jugador());
					j.setNombre(prop.getProperty(i + "_nombre"));
					j.setPid(Integer.valueOf(prop.getProperty(i + "_pid")));
					j.setEquipo(prop.getProperty(i + "_equipo"));
					j.setTid(Integer.valueOf(prop.getProperty(i + "_tid")));
					j.setValor(Integer.valueOf(prop.getProperty(i + "_valor")));
					j.setJornada(Integer.valueOf(prop.getProperty(i + "_jornada")));
					
					j.setDemarcacion(Jugador.DEMARCACION.valueOf(prop.getProperty(i + "_demarcacion")));
					
					j.setCondicion(Integer.valueOf(prop.getProperty(i + "_condicion")));
					j.setRapidez(Integer.valueOf(prop.getProperty(i + "_rapidez")));
					j.setTecnica(Integer.valueOf(prop.getProperty(i + "_tecnica")));
					j.setPases(Integer.valueOf(prop.getProperty(i + "_pases")));
					j.setPorteria(Integer.valueOf(prop.getProperty(i + "_porteria")));
					j.setDefensa(Integer.valueOf(prop.getProperty(i + "_defensa")));
					j.setCreacion(Integer.valueOf(prop.getProperty(i + "_creacion")));
					j.setAnotacion(Integer.valueOf(prop.getProperty(i + "_anotacion")));
	
					j.getOriginal().setCondicion(Integer.valueOf(prop.getProperty(i + "_condicion_orig")));
					j.getOriginal().setRapidez(Integer.valueOf(prop.getProperty(i + "_rapidez_orig")));
					j.getOriginal().setTecnica(Integer.valueOf(prop.getProperty(i + "_tecnica_orig")));
					j.getOriginal().setPases(Integer.valueOf(prop.getProperty(i + "_pases_orig")));
					j.getOriginal().setPorteria(Integer.valueOf(prop.getProperty(i + "_porteria_orig")));
					j.getOriginal().setDefensa(Integer.valueOf(prop.getProperty(i + "_defensa_orig")));
					j.getOriginal().setCreacion(Integer.valueOf(prop.getProperty(i + "_creacion_orig")));
					j.getOriginal().setAnotacion(Integer.valueOf(prop.getProperty(i + "_anotacion_orig")));
	
					j.getOriginal().setJornada(Integer.valueOf(prop.getProperty(i + "_jornada_orig")));
					if (Util.nnvl(prop.getProperty(i + "_valor_orig")) != null) {
						j.getOriginal().setValor(Util.stringToInteger(prop.getProperty(i + "_valor_orig")));
					}
					j.setEdicion(edicion);
					
					j.calcular_puntos();
					
					jugadores.add(j);
				}
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

	// OJO: esta función se llama tanto desde el factorx como del asistente
	public static boolean actualizar_jugador(WebClient navegador, Jugador jugador, boolean forzar) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/player/PID/" + jugador.getPid());
SERVLET_FACTORX._log_linea(pagina.asXml());

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

	public static void poner_estrellas(List<Jugador> jugadores, FactorxBO.FORMACIONES formacion, SERVLET_FACTORX.TIPO_FACTORX tipo) {
		int[] titulares = new int[] { 0, 0, 0, 0 };
		int[] suplentes = new int[] { 0, 1, 1, 1 };
		int total_titulares = 0;
		int total_suplentes = 0;
		int[] num_demarcacion = new int[] { 0, 0, 0, 0 };	// N�mero de jugadores por cada demarcaci�n
	
		if (tipo == SERVLET_FACTORX.TIPO_FACTORX.senior) {
			titulares = new int[] { 1, 4, 3, 3 };
			suplentes = new int[] { 1, 1, 1, 1 };
			total_suplentes = 4;
		} else {
			if (jugadores.size() >= 20) {
				titulares[Jugador.DEMARCACION.GK.ordinal()] = 1;
				suplentes[Jugador.DEMARCACION.GK.ordinal()] = 1;

				titulares[Jugador.DEMARCACION.DEF.ordinal()] = new Integer(formacion.name().charAt(1) - '0');
				titulares[Jugador.DEMARCACION.MID.ordinal()] = new Integer(formacion.name().charAt(2) - '0');
				titulares[Jugador.DEMARCACION.ATT.ordinal()] = new Integer(formacion.name().charAt(3) - '0');
				
				if (jugadores.size() >= 40) {
					total_suplentes = 3;
				} else {
					if (titulares[Jugador.DEMARCACION.DEF.ordinal()] > 3) {
						titulares[Jugador.DEMARCACION.DEF.ordinal()]--;
					}
					if (titulares[Jugador.DEMARCACION.MID.ordinal()] > 3) {
						titulares[Jugador.DEMARCACION.MID.ordinal()]--;
					}
					if (titulares[Jugador.DEMARCACION.ATT.ordinal()] > 2) {
						titulares[Jugador.DEMARCACION.ATT.ordinal()]--;
					}
				
					if (jugadores.size() >= 30) {
						total_suplentes = 2;
					} else {
						titulares[Jugador.DEMARCACION.DEF.ordinal()]--;
						titulares[Jugador.DEMARCACION.MID.ordinal()]--;
						titulares[Jugador.DEMARCACION.ATT.ordinal()]--;
						total_suplentes = 1;
					}
				}
			}
			
			// Contamos los jugadores de cada demarcaci�n
			for (Jugador j : jugadores) {
				num_demarcacion[j.getDemarcacion().ordinal()]++;
			}
			
			if (num_demarcacion[Jugador.DEMARCACION.DEF.ordinal()] <= 5) {
				titulares[Jugador.DEMARCACION.DEF.ordinal()] = 1;
			} else if (num_demarcacion[Jugador.DEMARCACION.DEF.ordinal()] < 10) {
				titulares[Jugador.DEMARCACION.DEF.ordinal()] = 2;
			}
			
			if (num_demarcacion[Jugador.DEMARCACION.MID.ordinal()] <= 5) {
				titulares[Jugador.DEMARCACION.MID.ordinal()] = 1;
			} else if (num_demarcacion[Jugador.DEMARCACION.MID.ordinal()] < 10) {
				titulares[Jugador.DEMARCACION.MID.ordinal()] = 2;
			}
		
			if (num_demarcacion[Jugador.DEMARCACION.ATT.ordinal()] <= 5) {
				titulares[Jugador.DEMARCACION.ATT.ordinal()] = 1;
				suplentes[Jugador.DEMARCACION.ATT.ordinal()] = 0;
			} else if (num_demarcacion[Jugador.DEMARCACION.ATT.ordinal()] < 8) {
				titulares[Jugador.DEMARCACION.ATT.ordinal()] = 1;
			}
		}
	
		total_titulares = titulares[Jugador.DEMARCACION.GK.ordinal()] + titulares[Jugador.DEMARCACION.DEF.ordinal()] + titulares[Jugador.DEMARCACION.MID.ordinal()] + titulares[Jugador.DEMARCACION.ATT.ordinal()];
		
		for (Jugador j : jugadores) {
			if (total_titulares > 0 && titulares[j.getDemarcacion().ordinal()] > 0) {
				total_titulares--;
				titulares[j.getDemarcacion().ordinal()]--;
				j.setIcono("<span class='estrella'>&#x2605;</span>");
				j.setIcono_foro("[/color][color=gold][size=16px]&#x2605;[/size][/color][/family][family=monospace][color=" + j.getColor_demarcacion() + "]");
			} else if (total_suplentes > 0 && suplentes[j.getDemarcacion().ordinal()] > 0) {
				total_suplentes--;
				suplentes[j.getDemarcacion().ordinal()]--;
				j.setIcono("<span class='estrella_sup'>&#x2605;</span>");
				j.setIcono_foro("[/color][color=OliveDrab][size=16px]&#x2605;[/size][/color][/family][family=monospace][color=" + j.getColor_demarcacion() + "]");
			}
		}
	}

	public static void archivar_temporada(TIPO_FACTORX tipo, int jornada_actual) throws IOException {
		List<Jugador> jugadores = JugadorBO.leer_jugadores(tipo, FactorxBO.getEdicion(jornada_actual), true);
		FactorxBO.grabar_jugadores(jugadores, tipo, jornada_actual, true);

		List<Jugador> nuevos = new ArrayList<Jugador>();
		Iterator<Jugador> it = jugadores.iterator();
		
		while (it.hasNext()) {
			Jugador j = it.next();
			
			// Incremento la edad, ya que no se hace durante la temporada
			j.setEdad(j.getEdad() + 1);
			
			// Solo mantengo a los jugadores que cumplan los requisitos y que hayan sido actualizados la �ltima jornada
			if (j.validar(tipo) && j.getJornadaMod() == 0 && j.getPuntos().compareTo(BigDecimal.ZERO) > 0) {
				j.setOriginal(new Jugador(j));
				j.setJornada(jornada_actual);
				nuevos.add(j);
			}
		}

		FactorxBO.grabar_jugadores(nuevos, tipo, jornada_actual, false);
	}

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
