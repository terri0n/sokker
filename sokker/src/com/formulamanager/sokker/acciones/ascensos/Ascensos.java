package com.formulamanager.sokker.acciones.ascensos;

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

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.bo.EquipoBO;
import com.formulamanager.sokker.bo.LigaBO;
import com.formulamanager.sokker.entity.Equipo;
import com.formulamanager.sokker.entity.Liga;
import com.formulamanager.sokker.entity.Partido;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Servlet implementation class Main
 */
@WebServlet("/ascensos")
public class Ascensos extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;

    public static int MAX_DIVISIONES = 4;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Ascensos() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			new Navegador(true, request) {
				@Override
				protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
					if (request.getSession().getServletContext().getAttribute("partidos") == null) {
						// 1. Leemos los equipos
						
						List<Equipo>[] equipos = new ArrayList[MAX_DIVISIONES];
						for (int i = 0; i < MAX_DIVISIONES; i++) {
							equipos[i] = new ArrayList<Equipo>();
						}
						
						for (int division = 0; division < MAX_DIVISIONES; division++) {
							for (int numero_liga = 0; numero_liga < Math.pow(3,  division); numero_liga++) {
								Liga liga = LigaBO.obtener_equipos_liga(navegador, 17, division + 1, numero_liga + 1);
								if (liga != null) {
									equipos[division].addAll(liga.getEquipos());
									for (Equipo e : liga.getEquipos()) {
										e.setNombre(EquipoBO.obtener_nombre(e.getTid(), navegador));
									}
								}
							}
	
							// 1º ordenamos por posición, puntos...
							equipos[division].sort(Equipo.getComparator_ascensos());
							
							// 2º ordenamos cada bloque (ascenso, promoción, descenso) sin tener en cuenta la posición
							List<Equipo> ascenso = new ArrayList<Equipo>();
							List<Equipo> promocion_a = new ArrayList<Equipo>();
							List<Equipo> promocion_d = new ArrayList<Equipo>();
							List<Equipo> descenso = new ArrayList<Equipo>();

							// Para el ascenso cuento tantos equipos como habría en caso de estar todos los grupos
							// Para el descenso, en IV no cuadraría porque hay menos grupos, pero como no descienden... simplemente me lo salto para que no dé error
							int total = 12 * (int)Math.pow(3, division);
							ascenso.addAll(equipos[division].subList(0, total / 12));
							promocion_a.addAll(equipos[division].subList(total / 12, total * 2 / 12));
							if (division < MAX_DIVISIONES - 1) {
								promocion_d.addAll(equipos[division].subList(total * 6 / 12, total * 9 / 12));
								descenso.addAll(equipos[division].subList(total * 9 / 12, total));
							}

							ascenso.sort(Equipo.getComparator_emparejamientos());
							promocion_a.sort(Equipo.getComparator_emparejamientos());
							promocion_d.sort(Equipo.getComparator_emparejamientos());
							descenso.sort(Equipo.getComparator_emparejamientos());
							
							equipos[division].clear();
							equipos[division].addAll(ascenso);
							equipos[division].addAll(promocion_a);
							equipos[division].addAll(promocion_d);
							equipos[division].addAll(descenso);
						}
						
						// 2. Emparejamientos
	
						List<Partido> partidos = new ArrayList<Partido>();
						
						for (int division = 0; division < MAX_DIVISIONES - 1; division++) {
							// Si quito los que no promocionan, habrá 8 equipos por grupo. Pero necesitamos 6 partidos por grupo:
							int num_partidos = equipos[division].size() * 6 / 8;
							
							for (int i = 0; i < num_partidos; i++) {
								Equipo e1 = equipos[division].get(equipos[division].size() - i - 1);
								Equipo e2 = equipos[division + 1].get(i);
								
								partidos.add(new Partido(e1, e2));
							}
						}
						
						request.getSession().getServletContext().setAttribute("partidos", partidos);
					}
				}
			};
		} catch (FailingHttpStatusCodeException | LoginException | ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		request.getRequestDispatcher("/jsp/ascensos/ascensos.jsp").forward(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		execute(req, resp);
	}
}
