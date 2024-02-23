package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET;

/**
 * Actualiza las habilidades originales de un jugador
 */
@WebServlet("/factorx/capturar")
public class Capturar extends SERVLET {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Capturar() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
/*		if (login(request)) {
			HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
	        imageGenerator.loadUrl("http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/factorx?captura=1");

	        try {
				new Navegador(true, request) {
					@Override
					protected void execute(WebClient navegador)	throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException {
						String url = "factorx_j" + obtener_jornada(navegador) + ".png";
						imageGenerator.saveAsImage(request.getServletContext().getRealPath(url));
						
//			        HtmlPage page = subir_imagen(request.getServletContext().getRealPath(url));
//			        HtmlPage page = subir_imagen("D:\\workspace2\\html2image\\hello-world.png");
						
						request.setAttribute("url", "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/" + url);
					}
				};
			} catch (FailingHttpStatusCodeException | LoginException | ParseException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			request.getRequestDispatcher("/jsp/factorx/subir_imagen.jsp").forward(request, response);
//	        response.getOutputStream().print(page.getWebResponse().getContentAsString());
		} else {
			response.sendRedirect(request.getContextPath() + "/factorx?senior=" + Util.nvl(request.getParameter("senior")));
		}
*/	}

//	private HtmlPage subir_imagen(String url) {
//		//Configuración del navegador.
//		WebClient navegador = new WebClient();
//		navegador.getOptions().setJavaScriptEnabled(false);
//		navegador.getOptions().setCssEnabled(false);
//		navegador.getOptions().setUseInsecureSSL(true);
//
//		try {
//			HtmlPage pagina = navegador.getPage("http://es.tinypic.com");
//			HtmlForm formularioLogin = pagina.getFirstByXPath("//form");
//
//			HtmlButton botonFormularioLogin = formularioLogin.getFirstByXPath("//button[@type='button']");
//			((HtmlInput)formularioLogin.getFirstByXPath("//input[@type='file']")).setValueAttribute(url);
//			
//			return botonFormularioLogin.click();
//		} catch (FailingHttpStatusCodeException | IOException e) {
//			e.printStackTrace();
//			return null;
//		}
//
//		
//	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
	}
}
