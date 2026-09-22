package com.formulamanager.sokker.auxiliares;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.conn.HttpHostConnectException;

import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public abstract class Navegador {
	public static final String USER_AGENT = "Sokker Asistente (+https://raqueto.com/sokker/asistente)";
	public static final String SOKKER_CLIENT_HEADER = "X-Sokker-Client";
	public static final String SOKKER_CLIENT_KEY = "skc_bae02f2686bf9038d248";

	protected HttpServletRequest request;
	private Integer jornada;
	private Usuario usuario;
	private boolean incrementar_edad;
	private int ajuste_edad;

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
		try {
			Page paginaLogin = navegador.getPage(requestSettings);
//System.out.println(paginaLogin.getWebResponse().getContentAsString());
		} catch (FailingHttpStatusCodeException e) {
			navegador.close();
			if (e.getStatusCode() == 401) {
				throw new LoginExceptionExt("Error when logging in to Sokker: bad password", login, password);
			}
			throw e;
		} catch (IOException e) {
			navegador.close();
			throw e;
		}

		try {
			startXmlSession(navegador, AsistenteBO.SOKKER_URL, login, password);
		} catch (IOException | LoginException e) {
			navegador.close();
			throw e;
		}

		return navegador;
	}

	static void startXmlSession(WebClient navegador, String baseUrl, String login, String password)
			throws IOException, LoginException {
		WebRequest peticion = new WebRequest(new URL(baseUrl + "/start.php?session=xml"), HttpMethod.POST);
		peticion.setRequestParameters(Arrays.asList(
				new NameValuePair("ilogin", login),
				new NameValuePair("ipassword", password)));

		String respuesta = navegador.getPage(peticion).getWebResponse().getContentAsString().trim();
		if (!respuesta.startsWith("OK")) {
			throw new LoginException("Could not open the Sokker XML session: " + respuesta);
		}
	}

	public static BrowserVersion createBrowserVersion() {
		BrowserVersion version = BrowserVersion.CHROME.clone();
		version.setUserAgent(USER_AGENT);
		return version;
	}

	public static WebClient createSokkerWebClient() {
		WebClient navegador = new WebClient(createBrowserVersion());
		configureSokkerWebClient(navegador);
		return navegador;
	}

	private static void configureSokkerWebClient(WebClient navegador) {
		navegador.addRequestHeader(SOKKER_CLIENT_HEADER, SOKKER_CLIENT_KEY);
		navegador.getOptions().setJavaScriptEnabled(false);
		navegador.getOptions().setCssEnabled(false);
		navegador.getOptions().setUseInsecureSSL(true);
	}

	private WebClient crear_navegador() {
		WebClient navegador = new WebClient(createBrowserVersion()) {
			private static final long serialVersionUID = 1L;
			private XmlPage paginaJuniors;
			private XmlPage paginaEntrenadores;

			@SuppressWarnings("unchecked")
			@Override
			public <P extends Page> P getPage(String url) throws IOException, FailingHttpStatusCodeException, MalformedURLException {
				XmlPage paginaJson = SokkerPlayerXmlCompat.getXmlPage(this, url);
				if (paginaJson != null) {
					return (P) paginaJson;
				}

				if ((AsistenteBO.SOKKER_URL + "/xml/juniors.xml").equals(url)) {
					if (paginaJuniors != null) {
						return (P) paginaJuniors;
					}

					paginaJson = SokkerJuniorsXmlCompat.getXmlPage(this, url);
					if (paginaJson != null) {
						paginaJuniors = paginaJson;
						return (P) paginaJuniors;
					}

					P pagina = super.getPage(url);
					if (pagina instanceof XmlPage) {
						paginaJuniors = (XmlPage) pagina;
					}
					return pagina;
				}

				if ((AsistenteBO.SOKKER_URL + "/xml/trainers.xml").equals(url)) {
					if (paginaEntrenadores != null) {
						return (P) paginaEntrenadores;
					}

					paginaJson = SokkerTrainerXmlCompat.getXmlPage(this, url);
					if (paginaJson != null) {
						paginaEntrenadores = paginaJson;
						return (P) paginaEntrenadores;
					}

					P pagina = super.getPage(url);
					if (pagina instanceof XmlPage) {
						paginaEntrenadores = (XmlPage) pagina;
					}
					return pagina;
				}
				return super.getPage(url);
			}
		};
		configureSokkerWebClient(navegador);
		
		return navegador;
	}
	
	/**
	 * Compatibilidad para los llamadores antiguos que pedían login XML.
	 * La autenticación se realiza con la API JSON actual y se mantiene el mismo
	 * contrato de getUsuario()/request para no obligar a migrar todos los llamadores a la vez.
	 */
	@SuppressWarnings("unchecked")
	public WebClient hacer_login_xml(String login, String password) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException {
		WebClient navegador = hacer_login(login, password);
		LinkedHashMap<String, Object> actual = (LinkedHashMap<String, Object>) JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/current");
		Integer tid = JSONUtil.getInteger(actual, "team.id");
		if (tid == null) {
			throw new LoginExceptionExt("Error when logging in to Sokker: user has no team", login, password);
		}

		setUsuario(new Usuario(tid, null));
		return navegador;
	}

	// Intenta acceder a una url hasta 3 veces
	public static XmlPage getXmlPage(WebClient navegador, String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		XmlPage paginaJson = SokkerPlayerXmlCompat.getXmlPage(navegador, url);
		if (paginaJson != null) {
			return paginaJson;
		}

		paginaJson = SokkerJuniorsXmlCompat.getXmlPage(navegador, url);
		if (paginaJson != null) {
			return paginaJson;
		}

		paginaJson = SokkerXmlCompat.getXmlPage(navegador, url);
		if (paginaJson != null) {
			return paginaJson;
		}

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
	
	/**
	 * Mantiene el nombre histórico del método, pero la jornada se obtiene de /api/current.
	 */
	public Integer obtener_jornada(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		return obtener_jornada_json(navegador);
	}

	public static Integer calcular_jornada_actual(LinkedHashMap<String, Object> actual) throws IOException {
		if (actual == null) {
			throw new IOException("Incomplete /api/current payload: missing root object");
		}
		Integer week = JSONUtil.getInteger(actual, "today.week");
		Integer dia = JSONUtil.getInteger(actual, "today.day");
		if (week == null || dia == null) {
			throw new IOException("Incomplete /api/current payload: missing today.week or today.day");
		}
		return week - (dia >= 5 ? 0 : 1);
	}

	/**
	 * Ajuste entre la edad que devuelve Sokker hoy y la edad que corresponde
	 * a la jornada de entrenamiento que estamos guardando. El cumpleaños de
	 * temporada ocurre el sábado: hasta el miércoles seguimos guardando el
	 * entrenamiento del jueves anterior, por lo que hay que restar un año.
	 * Se conserva la corrección histórica del jueves de la última jornada.
	 */
	public static int calcular_ajuste_edad(LinkedHashMap<String, Object> actual) throws IOException {
		int jornada_actual = calcular_jornada_actual(actual);
		Integer dia = JSONUtil.getInteger(actual, "today.day");
		if (dia == null) {
			throw new IOException("Incomplete /api/current payload: missing today.day");
		}
		if (AsistenteBO.getJornadaMod(jornada_actual) == 12 && dia < 5) {
			return -1;
		}
		if (AsistenteBO.getJornadaMod(jornada_actual) == 12 && dia == 5) {
			return 1;
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public Integer obtener_jornada_json(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		if (jornada != null) {
			return jornada;
		}

		LinkedHashMap<String, Object> actual = (LinkedHashMap<String, Object>) JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/current");
		int jornada_actual = calcular_jornada_actual(actual);
		setJornada(jornada_actual);

		ajuste_edad = calcular_ajuste_edad(actual);
		incrementar_edad = ajuste_edad > 0;

		return jornada;
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

	private int getJornadaMod(int jornada) {
		return jornada < 976 ? jornada % 16 : (jornada - 976) % AsistenteBO.JORNADAS_TEMPORADA;
	}
	
	protected Integer getJornadaMod(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		return getJornadaMod(obtener_jornada(navegador));
	}
	
	public boolean isIncrementar_edad() {
		return incrementar_edad;
	}

	public int getAjuste_edad() {
		return ajuste_edad;
	}
	
	public void setIncrementar_edad(boolean incrementar_edad) {
		this.incrementar_edad = incrementar_edad;
		this.ajuste_edad = incrementar_edad ? 1 : 0;
	}
}
