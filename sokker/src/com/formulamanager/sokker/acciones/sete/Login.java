package com.formulamanager.sokker.acciones.sete;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.LoginExceptionExt;
import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_SETE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Servlet implementation class ServletSokker
 */
@WebServlet("/Login")
public class Login extends SERVLET_SETE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
    }

	/**
	 * @throws ParseException 
	 * @throws LoginException 
	 * @throws FailingHttpStatusCodeException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FailingHttpStatusCodeException, LoginException, ParseException {
		if (!Util.getBoolean(request, "confirmed")) {
			throw new RuntimeException("Login not confirmed");
		} else {
			String ilogin = request.getParameter("ilogin");
			String ipassword = request.getParameter("ipassword");
			
			//Configuración del navegador.
//			WebClient navegador = new WebClient();
//			navegador.getOptions().setJavaScriptEnabled(false);
//			navegador.getOptions().setCssEnabled(false);
//			navegador.getOptions().setUseInsecureSSL(true);
//			
//			HtmlPage pagina_principal = hacer_login(navegador, ilogin, ipassword);
//			
//			//-----------------
//			// COMPROBAR LOGIN
//			//-----------------
//			if (pagina_principal.getFirstByXPath("//div[@class='alert alert-danger']") == null) {
//				request.getSession().setAttribute("ilogin", ilogin);
//				request.getSession().setAttribute("ipassword", ipassword);
//			} else {
//				throw new LoginExceptionExt("SETE: contraseña incorrecta", ilogin, ipassword);
//			}
//			navegador.close();
			
			new Navegador(false, ilogin, ipassword, request) {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
					request.getSession().setAttribute("ilogin", ilogin);
					request.getSession().setAttribute("ipassword", ipassword);
				}
			};
		}
		response.sendRedirect(request.getContextPath() + "/oldsete");
	}

//	public static HtmlPage hacer_login(WebClient navegador, String ilogin, String ipassword) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
//		//Proceso de login.
//		HtmlPage paginaLogin = navegador.getPage(AsistenteBO.SOKKER_URL + "/logon");
//		
//		HtmlForm formularioLogin = paginaLogin.getElementByName("logform");
//		
//		HtmlButton botonFormularioLogin = formularioLogin.getFirstByXPath("//button[@type='submit']");
//		formularioLogin.getInputByName("ilogin").setValueAttribute(ilogin);
//		formularioLogin.getInputByName("ipassword").setValueAttribute(ipassword);
//
//		return botonFormularioLogin.click();
//	}
}
