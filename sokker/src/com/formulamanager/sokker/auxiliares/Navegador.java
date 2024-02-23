package com.formulamanager.sokker.auxiliares;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.conn.HttpHostConnectException;

import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public abstract class Navegador {
	protected HttpServletRequest request;
	private Integer jornada;
	private Usuario usuario;
	private boolean incrementar_edad;

	// Crea un navegador sin hacer login en Sokker
	public Navegador() throws FailingHttpStatusCodeException, MalformedURLException, LoginException, IOException, ParseException {
		WebClient navegador = null;
		try {
			navegador = crear_navegador();
			execute(navegador);
		} finally {
			if (navegador != null) {
				navegador.close();
			}
		}
	}
	
	public Navegador(HttpServletRequest request) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
		this(false, request);
	}

	public Navegador(boolean xml, HttpServletRequest request) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
		this(xml, SystemUtil.getVar(SystemUtil.LOGIN), SystemUtil.getVar(SystemUtil.PASSWORD), request);
	}

	public Navegador(boolean xml, String login, String password, HttpServletRequest request) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
		this.request = request;

		WebClient navegador = null;
		try {
			navegador = xml ? hacer_login_xml(login, password) : hacer_login(login, password);
			execute(navegador);
		} finally {
			if (navegador != null) {
				navegador.close();
			}
		}
	}

	protected abstract void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException;

	public WebClient hacer_login(String login, String password) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException {
		WebClient navegador = crear_navegador();

		//Proceso de login.
		URL url = new URL(AsistenteBO.SOKKER_URL + "/api/auth/login");
		WebRequest requestSettings = new WebRequest(url, HttpMethod.POST);
		requestSettings.setRequestBody("{\"login\" : \"" + login + "\", \"password\" : \"" + password + "\", \"remember\" : false }");
		requestSettings.setAdditionalHeader("Content-Type", "application/json; charset=utf-8");
		Page paginaLogin = navegador.getPage(requestSettings);
//System.out.println(paginaLogin.getWebResponse().getContentAsString());
		return navegador;
	}

	private WebClient crear_navegador() {
		WebClient navegador = new WebClient();
		navegador.getOptions().setJavaScriptEnabled(false);
		navegador.getOptions().setCssEnabled(false);
		navegador.getOptions().setUseInsecureSSL(true);
		
		return navegador;
	}
	
	public WebClient hacer_login_xml(String login, String password) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException {
		login = login.replace("ñ",  "n");
		login = login.replace("áàäâ",  "a");
		login = login.replace("éèëê",  "e");
		login = login.replace("íìïî",  "i");
		login = login.replace("óòöô",  "o");
		login = login.replace("úùüû",  "u");
		
		WebClient navegador = crear_navegador();

		//Proceso de login.
		HtmlPage paginaLogin = navegador.getPage(AsistenteBO.SOKKER_URL + "/xmlinfo.php");
		
		HtmlForm formularioLogin = paginaLogin.getFirstByXPath("//form");
		
		HtmlSubmitInput botonFormularioLogin = formularioLogin.getFirstByXPath("//input[@type='submit']");
		formularioLogin.getInputByName("ilogin").setValueAttribute(login);
		formularioLogin.getInputByName("ipassword").setValueAttribute(password);
	
		HtmlPage page = botonFormularioLogin.click();

		if (!page.asText().startsWith("OK")) {
			String resp = page.asText();
			try {
				switch(Integer.valueOf(resp.split("\\=")[1])) {
					case 1: throw new LoginExceptionExt("Error when logging in to Sokker: 1 - bad password", login, password);
					case 3: throw new LoginExceptionExt("Error when logging in to Sokker: 3 - user has no team", login, password);
					case 4: throw new LoginExceptionExt("Error when logging in to Sokker: 4 - user is banned", login, password);
					case 5: throw new LoginExceptionExt("Error when logging in to Sokker: 5 - user is a bakrupt", login, password);
					case 6: throw new LoginException("Error when logging in to Sokker: the server is temporarily bloqued. Please, be patient and try again later (Error number 6)");	// user IP is blacklisted
					default: throw new LoginException(resp);
				}
			} catch (NumberFormatException | NullPointerException e) {
				throw new LoginException(resp);
			}
		}

		Usuario usuario = new Usuario(Util.stringToInteger(page.asText().split("teamID=")[1]), null);
		
		setUsuario(usuario);
		
		return navegador;
	}

	// Intenta acceder a una url hasta 3 veces
	public static XmlPage getXmlPage(WebClient navegador, String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		int intentos = 0;
		Exception ex;
		
		do {
			try {
				return navegador.getPage(url);
			} catch (HttpHostConnectException e) {
				e.printStackTrace();
				ex = e;
				intentos++;
			}
		} while (intentos < 3);
		
		throw new RuntimeException(ex);
	}

	public Integer obtener_edicion(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		return FactorxBO.getEdicion(obtener_jornada(navegador));
	}
	
	public Integer obtener_jornada(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		if (jornada != null) {
			return jornada;
		} else {
			XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/vars.xml");
			DomNode vars = (DomNode) pagina.getFirstByXPath("//vars");
			setJornada(AsistenteBO.obtener_jornada(vars));
			
			if (getJornadaMod(navegador) == 12) {
				Integer dia = Integer.valueOf(((DomText) vars.getFirstByXPath("day/text()")).asText());
				// Jueves
				if (dia == 5) {
					incrementar_edad = true;
				}
			}
			return jornada;
		}
	}

	//-----
	// G&S
	//-----
	
	private void setUsuario(Usuario usuario) {
		this.usuario = usuario;
		if (request != null) {
			request.setAttribute("usuario", usuario);
		}
	}

	protected Usuario getUsuario() {
		return usuario;
	}
	
	private void setJornada(Integer jornada) {
		this.jornada = jornada;
		if (request != null) {
			request.setAttribute("jornada", jornada);
		}
	}

	protected Integer getJornadaMod(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		int jornada = obtener_jornada(navegador);
		return jornada < 976 ? jornada % 16 : (jornada - 976) % AsistenteBO.JORNADAS_TEMPORADA;
	}

	public boolean isIncrementar_edad() {
		return incrementar_edad;
	}

	public void setIncrementar_edad(boolean incrementar_edad) {
		this.incrementar_edad = incrementar_edad;
	}
}
