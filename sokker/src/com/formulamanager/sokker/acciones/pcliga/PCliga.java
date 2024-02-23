package com.formulamanager.sokker.acciones.pcliga;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlBold;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * Scanner
 */
@WebServlet("/pcliga")
public class PCliga extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public PCliga() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Configuración del navegador.
		WebClient navegador = new WebClient();
		navegador.getOptions().setJavaScriptEnabled(false);
		navegador.getOptions().setCssEnabled(false);
		navegador.getOptions().setUseInsecureSSL(true);
		
		//Proceso de login.
		HtmlPage paginaLogin = navegador.getPage("http://www.pcliga.com/home.asp");
		
		HtmlForm formularioLogin = paginaLogin.getForms().get(0);
		HtmlSubmitInput botonFormularioLogin = formularioLogin.getInputByValue("Acceder");
		formularioLogin.getInputByName("login").setValueAttribute(SystemUtil.getVar("pcliga_login"));
		formularioLogin.getInputByName("pass").setValueAttribute(SystemUtil.getVar("pcliga_password"));
		botonFormularioLogin.click();
	
		List<Integer> grupos = new ArrayList<Integer>();
		List<String> equipos = new ArrayList<String>();
		TreeMap<Integer, String> tm = new TreeMap<Integer, String>();
		
		//Ejemplo de acceso a una página (tras login).
		HtmlPage pagina = navegador.getPage("https://www.pcliga.com/calendario_ver.asp");
		DomElement dom_grupos = pagina.getElementByName("grupo");
		Iterator<DomNode> it = dom_grupos.getChildNodes().iterator();
		while (it.hasNext()) {
			DomNode n = it.next();
			Integer i = Integer.valueOf(n.getAttributes().getNamedItem("value").getNodeValue());
			grupos.add(i);
		}
	
		//--------
		// GRUPOS
		//--------
		for (Integer i : grupos) {
			pagina = navegador.getPage("http://www.pcliga.com/calendario_ver.asp?grupo=" + i);
			
			List<Object> enlaces = (List<Object>)pagina.getByXPath("//table[2]//a");
			Iterator<Object> it2 = enlaces.iterator();
	 		while (it2.hasNext()) {
				HtmlAnchor n2 = (HtmlAnchor)it2.next();
				String href = n2.getAttributes().getNamedItem("href").getNodeValue();
	
				if (href.startsWith("clasificacion_verequipo.asp")) {
					String equipo = href.split("equipo=")[1].split("&")[0];
					// Quitamos los filiales
					if (!equipo.endsWith("%2DB%2D")) {
						equipos.add(equipo);
					}
				}
	 		}
		}
		equipos.add("Terrinator");
		
		//---------
		// EQUIPOS
		//---------
		int n = 0;
		for (String equipo : equipos) {	
			pagina = navegador.getPage("http://www.pcliga.com/cromos_ver_otro.asp?equipo=" + equipo);
	
			HtmlBold cromos = (HtmlBold)pagina.getByXPath("//div[@id='content']//b").get(0);
			int num = Integer.valueOf(cromos.asText());
			tm.put(num, "<a href='http://www.pcliga.com/clasificacion_verequipo.asp?equipo=" + equipo + "'>" + URLDecoder.decode(equipo, "UTF-8") + "</a>" + (tm.get(num) == null ? "" : ", " + tm.get(num)));
			
			if (n++ % 50 == 0) {
				System.out.println(n + "/" + equipos.size());
			}
	 	}
		navegador.close();
		
		//--------
		// CROMOS
		//--------
		List<Integer> claves = new ArrayList<Integer>();
		claves.addAll(tm.keySet());
		claves.sort(Collections.reverseOrder());
		
		response.setContentType("text/html");
	    PrintWriter out = response.getWriter();

	    out.print("<table>");
		for (Integer i : claves) {
			out.println("<tr>");
			out.println("	<td valign='top'><b>" + i + "</b></td>");
			out.println("	<td>" + tm.get(i) + "</td>");
			out.println("</tr>");
		}
		out.print("</table>");
		
		out.close();
	}
}