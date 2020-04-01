package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Juvenil;

/**
 * Servlet implementation class ServletSokker
 */
@WebServlet("/asistente/cambiar_vista")
public class CambiarVista extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CambiarVista() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean juveniles = Util.getBoolean(request, "juveniles");
		boolean historico = Util.getBoolean(request, "historico");
		
		if (login(request)) {
			Integer tid = getUsuario(request).getDef_tid();

			List<? extends Jugador> lista = juveniles ? AsistenteBO.leer_juveniles(tid, historico) : AsistenteBO.leer_jugadores(tid, historico);
			if (juveniles) {
				if (historico) {
					Collections.sort(lista, Jugador.getComparator_nombre());
				} else {
					Collections.sort(lista);
				}
				AsistenteBO.calcular_juveniles((List<Juvenil>)lista);
			} else {
				Collections.sort(lista, Jugador.getComparator());
				AsistenteBO.calcular((List<Jugador>)lista, getUsuario(request));
			}
			request.getSession().setAttribute("jugadores", lista);
		}

		response.sendRedirect(request.getContextPath() + "/asistente?juveniles=" + (juveniles ? "1" : "") + "&historico=" + (historico ? "1" : ""));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}
