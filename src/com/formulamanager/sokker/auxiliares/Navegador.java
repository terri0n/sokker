package com.formulamanager.sokker.auxiliares;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.conn.HttpHostConnectException;

import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public abstract class Navegador {
	protected Usuario usuario;
	
	public Navegador() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		this(false);
	}

	public Navegador(boolean xml) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		this(xml, AsistenteBO.getLogin(), AsistenteBO.getPassword());
	}

	public Navegador(boolean xml, String login, String password) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
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
	
	protected abstract void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException;

	public WebClient hacer_login(String login, String password) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient navegador = new WebClient();
		navegador.getOptions().setJavaScriptEnabled(false);
		navegador.getOptions().setCssEnabled(false);
		navegador.getOptions().setUseInsecureSSL(true);

		//Proceso de login.
		HtmlPage paginaLogin = navegador.getPage("http://sokker.org/logon");

//		URL url = new URL("http://sokker.org/logon");
//		WebRequest requestSettings = new WebRequest(url);
//		requestSettings.setCharset("utf-8");
//		HtmlPage paginaLogin = navegador.getPage(requestSettings);		
		
		HtmlForm formularioLogin = paginaLogin.getElementByName("logform");
		
		HtmlButton botonFormularioLogin = formularioLogin.getFirstByXPath("//button[@type='submit']");
		formularioLogin.getInputByName("ilogin").setValueAttribute(login);
		formularioLogin.getInputByName("ipassword").setValueAttribute(password);
	
		HtmlPage page = botonFormularioLogin.click();

		HtmlHeading2 h2 = page.getFirstByXPath("//h2[@class='subtitle']");
		if (h2 == null) {
			throw new RuntimeException(Util.getTexto("en", "messages.login_sokker_error") + ": " + Util.getTexto("en", "messages.login_error"));
		}
		
		String equipo = h2.asText();
		usuario = new Usuario (Util.stringToInteger(equipo.substring(0, equipo.length() - 1).split("\\[")[1]), equipo.split("\\[")[0].trim());

		HtmlDivision div = (HtmlDivision) page.getFirstByXPath("//div[@class='panel-body']//div[@class=' ']/img[contains(@src,'/flags/')]/..");
		if (div != null) {
			Integer tid_nt = Integer.valueOf(((DomAttr)div.getFirstByXPath("a/@href")).getNodeValue().split("team/teamID/")[1]);
			String equipo_nt = ((HtmlAnchor)div.getFirstByXPath("a")).asText();
			usuario.setTid_nt(tid_nt);
			usuario.setEquipo_nt(equipo_nt);
		}
		
		return navegador;
	}

	public WebClient hacer_login_xml(String login, String password) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient navegador = new WebClient();
		navegador.getOptions().setJavaScriptEnabled(false);
		navegador.getOptions().setCssEnabled(false);
		navegador.getOptions().setUseInsecureSSL(true);

		//Proceso de login.
		HtmlPage paginaLogin = navegador.getPage("http://sokker.org/xmlinfo.php");
		
		HtmlForm formularioLogin = paginaLogin.getFirstByXPath("//form");
		
		HtmlSubmitInput botonFormularioLogin = formularioLogin.getFirstByXPath("//input[@type='submit']");
		formularioLogin.getInputByName("ilogin").setValueAttribute(login);
		formularioLogin.getInputByName("ipassword").setValueAttribute(password);
	
		HtmlPage page = botonFormularioLogin.click();
		if (!page.asText().startsWith("OK")) {
			throw new RuntimeException(Util.getTexto("en", "messages.login_sokker_error") + ": " + page.asText());
		}

		usuario = new Usuario(Util.stringToInteger(page.asText().split("teamID=")[1]), null);
		
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
}
