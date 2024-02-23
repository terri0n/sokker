package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.NtdbBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Juvenil;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Servlet implementation class Main
 */
@WebServlet("/asistente")
public class Asistente extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Asistente() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!login(request)) {
			request.getRequestDispatcher("/jsp/asistente/login.jsp").forward(request, response);
		} else {
			boolean juveniles = Util.getInt(request.getSession(), "juveniles") > 0;
			boolean historico = Util.getInt(request.getSession(), "historico") > 0;
			
			// Leer jugadores
			Usuario usuario = getUsuario(request);
			Integer tid = usuario.getDef_tid();
			AsistenteBO.setActualizado(request, usuario, null);

			if (juveniles) {
				List<Juvenil> lista_juveniles = AsistenteBO.leer_juveniles(tid, historico, usuario);
				if (historico) {
					Collections.sort(lista_juveniles, Jugador.getComparator_nombre());
				} else {
					Collections.sort(lista_juveniles);
				}
				AsistenteBO.calcular_juveniles(lista_juveniles);

//for (Juvenil j : lista_juveniles) {
//	Juvenil ultimo = j;
//	while (ultimo.getOriginal() != null) {
//		ultimo = ultimo.getOriginal();
//	}
//	System.out.println(j.getNombre() + " " + ultimo.getJornada() + " " + ultimo.getEdad());
//}
				request.setAttribute("jugadores", lista_juveniles);
			} else {
				List<Jugador> lista_jugadores = AsistenteBO.leer_jugadores(tid, usuario.getDef_equipo(), historico, usuario);
				Collections.sort(lista_jugadores, Jugador.getComparator());
				AsistenteBO.calcular(lista_jugadores, usuario);

				if (tid > NtdbBO.MAX_ID_SELECCION) {
					List<Juvenil> lista_juveniles_historico = AsistenteBO.leer_juveniles(tid, true, usuario);
				
					// Asociamos los juveniles a los jugadores
					HashMap<String, Juvenil> hm_juveniles = new HashMap<>();
					for (Juvenil juv : lista_juveniles_historico) {
						hm_juveniles.put(juv.getNombre(), juv);
					}
					for (Jugador jug : lista_jugadores) {
						Juvenil juv = hm_juveniles.get(jug.getNombre());
						if (juv != null) {
							juv.calcular_niveles();
							jug.setJuvenil(juv);
						}
					}
				}
				
				request.setAttribute("jugadores", lista_jugadores);
			}
			
			request.getRequestDispatcher("/jsp/asistente/asistente.jsp").forward(request, response);
		}
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
