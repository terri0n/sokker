package com.formulamanager.sokker.acciones.sete;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Servlet implementation class ServletSokker
 */
@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ilogin = request.getParameter("ilogin");
		String ipassword = request.getParameter("ipassword");
		
		//Configuración del navegador.
		WebClient navegador = new WebClient();
		navegador.getOptions().setJavaScriptEnabled(false);
		navegador.getOptions().setCssEnabled(false);
		navegador.getOptions().setUseInsecureSSL(true);
		
		HtmlPage pagina_principal = hacer_login(navegador, ilogin, ipassword);
		
		//-----------------
		// COMPROBAR LOGIN
		//-----------------
		String error = "";
		if (pagina_principal.getFirstByXPath("//div[@class='alert alert-danger']") == null) {
			request.getSession().setAttribute("ilogin", ilogin);
			request.getSession().setAttribute("ipassword", ipassword);
		} else {
			error = "?error=" + URLEncoder.encode("Error in user or password", "UTF-8");
		}
		navegador.close();
		
		response.sendRedirect(request.getContextPath() + "/sete" + error);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doPost(request, response);
	}

	public static HtmlPage hacer_login(WebClient navegador, String ilogin, String ipassword) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		//Proceso de login.
		HtmlPage paginaLogin = navegador.getPage("http://sokker.org/logon");
		
		HtmlForm formularioLogin = paginaLogin.getElementByName("logform");
		
		HtmlButton botonFormularioLogin = formularioLogin.getFirstByXPath("//button[@type='submit']");
		formularioLogin.getInputByName("ilogin").setValueAttribute(ilogin);
		formularioLogin.getInputByName("ipassword").setValueAttribute(ipassword);

		return botonFormularioLogin.click();
	}
}
