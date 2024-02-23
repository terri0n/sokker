package com.formulamanager.sokker.acciones.fecha;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.JugadorBO;
import com.formulamanager.sokker.entity.Equipo;
import com.formulamanager.sokker.entity.Pais;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Fecha
 */
@WebServlet("/fecha")
public class Fecha extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Fecha() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		WebClient navegador = new WebClient();
		navegador.getOptions().setJavaScriptEnabled(false);
		navegador.getOptions().setCssEnabled(false);
		navegador.getOptions().setUseInsecureSSL(true);

		SortedSet<Equipo> equipos = new TreeSet<Equipo>(Equipo.getComparator());
		JugadorBO.hacer_login_xml(navegador, request);

		Integer id_country = Util.getInteger(request, "id_country");
		if (id_country != null) {
			int pagina = 1;
			boolean seguir;
			do {
				System.out.println("P�gina " + pagina);
	
				HtmlPage html_pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/country_ranking/ID_country/" + id_country + "/action/ranking/pg/" + pagina);
				List<HtmlAnchor> lista = (List<HtmlAnchor>)html_pagina.getByXPath("//tr[not(contains(@class, 'bg-stripe-3'))]//a[contains(@href,'team/teamID/')]");
				for (HtmlAnchor e : lista) {
					Integer tid = Util.stringToInteger(e.getHrefAttribute().split("/")[2]);
					Equipo equipo = Equipo.nuevo(tid, navegador);
					if (equipo != null) {
						equipos.add(equipo);
					}
				}
				seguir = lista.size() > 0;
				pagina++;
			} while (seguir);
		}
		
		request.setAttribute("equipos", equipos);
		request.setAttribute("paises",  Pais.obtener_paises(navegador));

//		Collections.sort((List)request.getAttribute("paises"));
//		for (Pais p : (List<Pais>)request.getAttribute("paises")) {
//			System.out.println("new BigDecimal(\"" + p.getCurrency_rate() + "\"), // " + p.getId());
//		}
		
		request.getRequestDispatcher("/jsp/fecha/fecha.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doGet(request, response);
	}
}
