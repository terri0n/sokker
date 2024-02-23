package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.NtdbBO;
import com.formulamanager.sokker.entity.Partido;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Horario
 */
@WebServlet("/asistente/horario")
public class Horario extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Horario() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Integer tid = Util.getInteger(request, "tid");
		if (tid != null) {
			try {
				new Navegador(true, request) {
					@Override
					protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
						// Sumo uno porque en la función le resto uno a partir del jueves
						// La U21 juega una semana antes que la NT
						int jornada = obtener_jornada(navegador) + (tid < 400 ? 1 : 0);
						Integer jornada_grabada = (Integer)request.getSession().getServletContext().getAttribute("jornada" + tid);

						// Si no hay partidos o son antiguos, los buscamos
						if (jornada_grabada == null || !jornada_grabada.equals(jornada)) {
							List<Partido> partidos = new ArrayList<Partido>();
							List<Integer> anyadidos = new ArrayList<>();
		
							for (int i = 1; i <= NtdbBO.paises.length; i++) {
								if (!anyadidos.contains(i)) {
									Partido p = Partido.nuevo_nt(tid + i, jornada, navegador);
									
									if (p != null) {
										partidos.add(p);
										anyadidos.add(p.getLocal().getTid() % 400);
										anyadidos.add(p.getVisitante().getTid() % 400);
									}
								}
							}
		
							Collections.sort(partidos, Partido.getComparator());
							request.getSession().getServletContext().setAttribute("partidos" + tid, partidos);
							request.getSession().getServletContext().setAttribute("jornada" + tid, jornada);
						} else {
							Calendar c = Calendar.getInstance();
							c.add(Calendar.MINUTE, -20);

							for (Partido p : (List<Partido>) request.getSession().getServletContext().getAttribute("partidos" + tid)) {
								if (p.getGoles_l() == null && p.getFecha().before(c.getTime())) {
									Partido p2 = Partido.nuevo_nt(p.getLocal().getTid(), jornada, navegador);
									if (p2 != null) {
										p.setGoles_l(p2.getGoles_l());
										p.setGoles_v(p2.getGoles_v());
									}
								}
							}
						}
					}
				};
			} catch (FailingHttpStatusCodeException | LoginException | ParseException e) {
				e.printStackTrace();
			}
			
			request.setAttribute("partidos", request.getSession().getServletContext().getAttribute("partidos" + tid));
			request.getRequestDispatcher("/jsp/asistente/horario.jsp").forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doGet(request, response);
	}
}
