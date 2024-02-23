package com.formulamanager.sokker.acciones.palmares;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.NtdbBO;
import com.formulamanager.sokker.dao.AsistenteDAO;
import com.formulamanager.sokker.entity.Equipo;
import com.formulamanager.sokker.entity.Pais;
import com.formulamanager.sokker.entity.PalmaresEquipo;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Palmarés
 */
@WebServlet("/palmares")
public class Palmares extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Palmares() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String competicion = Util.getString(request, "competicion");
			Integer id_country = Util.getInteger(request, "id_country");
			
			if (id_country != null && competicion != null) {
				new Navegador(request) {
					@Override
					protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
						List<PalmaresEquipo> equipos = new ArrayList<PalmaresEquipo>();

						if (competicion.startsWith("world_cup")) {
							int offset = competicion.endsWith("U21") ? 400 : 0;
							String prefijo = competicion.endsWith("U21") ? " U21" : "";
							for (int i = 0; i < NtdbBO.paises.length; i++) {
								if (!NtdbBO.paises[i].equals("")) {
									PalmaresEquipo palmares = AsistenteDAO.obtener_palmares(new PalmaresEquipo(offset + i + 1, NtdbBO.paises[i] + prefijo), "national_cup", navegador);
									if (palmares != null && !palmares.esta_vacio()) {
										equipos.add(palmares);
									}
								}
							}
						} else {
							int pagina = 1;
							boolean seguir;
							do {
								System.out.println("Página " + pagina);
								
								HtmlPage html_pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/country_ranking/ID_country/" + id_country + "/action/ranking/pg/" + pagina);
								// bg-stripe-3: equipos que no juegan la copa
								List<HtmlAnchor> lista = (List<HtmlAnchor>)html_pagina.getByXPath("//tr[not(contains(@class, 'bg-stripe-3'))]//a[contains(@href,'/app/team/')]");
								for (HtmlAnchor e : lista) {
									Integer tid = Util.stringToInteger(e.getHrefAttribute().split("/")[4]);
									String nombre = e.getTextContent();

									PalmaresEquipo palmares = AsistenteDAO.obtener_palmares(new PalmaresEquipo(tid, nombre), competicion, navegador);
System.out.println(nombre + " " + palmares.getPosicion()[0] + "-" + palmares.getPosicion()[1] + "-" + palmares.getPosicion()[2]);
									if (palmares != null && !palmares.esta_vacio()) {
										equipos.add(palmares);
									}
								}
								seguir = lista.size() > 0;
								pagina++;
							} while (seguir);
						}

						//-----------
						
						Collections.sort(equipos);
						request.setAttribute("equipos", equipos);
 					}
				};
			}

			List<Pais> paises = new ArrayList<Pais>();
			for (int i = 0; i < NtdbBO.paises.length; i++) {
				if (!NtdbBO.paises[i].equals("")) {
					paises.add(new Pais(NtdbBO.paises[i], i + 1));
				}
			}
			request.setAttribute("paises", paises);
			
			request.getRequestDispatcher("/jsp/palmares/palmares.jsp").forward(request, response);
		} catch (FailingHttpStatusCodeException | LoginException | IOException | ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
