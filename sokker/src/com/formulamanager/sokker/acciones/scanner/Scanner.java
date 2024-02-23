package com.formulamanager.sokker.acciones.scanner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Pais;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Scanner
 */
@WebServlet("/scanner")
public class Scanner extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Scanner() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebClient navegador = new WebClient();
		navegador.getOptions().setJavaScriptEnabled(false);
		navegador.getOptions().setCssEnabled(false);
		navegador.getOptions().setUseInsecureSSL(true);
//
//		List<Jugador> jugadores = new ArrayList<Jugador>();
		JugadorBO.hacer_login_xml(navegador, request);
//
//		boolean todos = "".equals(request.getParameter("id_country_filtro"));
//		Integer id_country_filtro = Util.getInteger(request, "id_country_filtro");
//		Integer id_country_origen = Util.getInteger(request, "id_country_origen");
//		Integer valor = Util.getInteger(request, "valor");
//		Integer edad = Util.getInteger(request, "edad");
//		boolean solo_copa = Util.getBoolean(request, "solo_copa");
//		
		List<Pais> paises = Pais.obtener_paises(navegador);
//		
//		for (Pais pais : paises) {
//			if (todos || pais.getId() == id_country_filtro) {
//				int pagina = 1;
//				boolean seguir;
//				do {
//					System.out.println(pais.getNombre() + " - P�gina " + pagina + " - " + jugadores.size());
//		
//					HtmlPage html_pagina = navegador.getPage("https://sokker.org/country_ranking/ID_country/" + pais.getId() + "/action/ranking/pg/" + pagina);
//					List<HtmlAnchor> lista = (List<HtmlAnchor>)html_pagina.getByXPath((solo_copa ? "//tr[not(contains(@class, 'bg-stripe-3'))]" : "") + "//a[contains(@href,'team/teamID/')]");
//					for (HtmlAnchor e : lista) {
//						Integer tid = Util.stringToInteger(e.getHrefAttribute().split("/")[2]);
//						jugadores.addAll(JugadorBO.obtener_jugadores(tid, id_country_origen, valor, edad, navegador));
//					}
//					seguir = lista.size() > 0;
//					pagina++;
//				} while (seguir);
//			}
//		}
//
//		Collections.sort(jugadores, Jugador.getComparator());
//		request.setAttribute("jugadores", jugadores);
		request.setAttribute("paises",  paises);
		
		request.getRequestDispatcher("/jsp/scanner/scanner.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			new Navegador(true, request) {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
					List<Pais> paises = Pais.obtener_paises(navegador);
					request.setAttribute("paises", paises);
				}
			};
		} catch (LoginException | FailingHttpStatusCodeException | ParseException e) {
			throw new RuntimeException(e);
		}
		
		request.getRequestDispatcher("/jsp/scanner/scanner.jsp").forward(request, response);
	}
	
	public static void scan(HttpServletRequest request, JspWriter out) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
		new Navegador(true, request) {
			@Override
			protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
				boolean todos = "".equals(request.getParameter("id_country_filtro"));
				Integer id_country_filtro = Util.getInteger(request, "id_country_filtro");
				Integer id_country_origen = Util.getInteger(request, "id_country_origen");
				Integer valor = Util.getInteger(request, "valor");
				Integer edad = Util.getInteger(request, "edad");
				boolean solo_copa = Util.getBoolean(request, "solo_copa");
				
				List<Jugador> jugadores = new ArrayList<Jugador>();
				request.setAttribute("jugadores", jugadores);

				List<Pais> paises = Pais.obtener_paises(navegador);
				out.flush();

//HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
				
				int i = 1;
				for (Pais pais : paises) {
					if (todos || pais.getId() == id_country_filtro) {
						int pagina = 1;
						boolean seguir;
						do {
							HtmlPage html_pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/country_ranking/ID_country/" + pais.getId() + "/action/ranking/pg/" + pagina);
							List<HtmlAnchor> lista = (List<HtmlAnchor>)html_pagina.getByXPath((solo_copa ? "//tr[not(contains(@class, 'bg-stripe-3'))]" : "") + "//a[contains(@href,'/app/team/')]");
							System.out.println(pais.getNombre() + " " + pagina + " " + jugadores.size());

							for (HtmlAnchor e : lista) {
								Integer tid = Util.stringToInteger(e.getHrefAttribute().split("/")[4]);	// \es\app\team\[TID]
								ArrayList<Jugador> busqueda = JugadorBO.obtener_jugadores(tid, /*null*/id_country_origen, valor, edad, navegador);
								jugadores.addAll(busqueda);

								for (Jugador j : busqueda) {
//if (j.getForma() != null) {
//	hm.put(j.getForma(), hm.getOrDefault(j.getForma(), 0) + 1);
//}

									out.print(
										"<tr class=\"" + j.getDemarcacion() + "\">" + 
										"	<td align=\"right\">" + i++ + "</td>" + 
										"	<td>" + pais.getNombre() + "</td>" + 
										"	<td><a href=\"" + AsistenteBO.SOKKER_URL + "/player/PID/" + j.getPid() + "\">" + j.getNombre() + "</a></td>" + 
										"	<td>" + j.getEdad() + "</td>" + 
										"	<td align=\"right\">" + String.format("%,d", j.getValor()) + "</td>" + 
										"	<td>" + j.getDemarcacion() + "</td>" + 
										"	<td>" + j.getPuntos() + "</td>" + 
										"</tr>"
									);
									out.flush();
			 					}
							}
							seguir = lista.size() > 0;
							pagina++;
						} while (seguir);
					}
				}
				
//for (int j : hm.keySet()) {
//	System.out.println(j + "\t" + hm.get(j));
//}

			}
		};
	}
}
