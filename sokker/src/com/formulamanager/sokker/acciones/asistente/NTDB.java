package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.NtdbBO;
import com.formulamanager.sokker.entity.Jugador;

/**
 * Formulario para enviar las habilidades de un jugador
 */
@WebServlet("/asistente/ntdb")
public class NTDB extends HttpServlet {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NTDB() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Integer tid = Util.getInteger(request, "tid");

		// Terminamos la sesión para que no se muestren datos de los jugadores
		request.getSession().invalidate();

		if (tid != null && tid < NtdbBO.MAX_ID_SELECCION) {
			List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, tid + "", false, null);
			Collections.sort(jugadores, Jugador.getComparator());
			// Quitamos información sensible para no mostrarla en público
			for (Jugador j : jugadores) {
				j.setDestacar(false);
				j.setLesion(0);
				j.setNotas(null);
				j.setNt(null);
				j.setTarjetas(null);
				j.setEn_venta(null);
			}
			request.setAttribute("jugadores", jugadores);
			request.setAttribute("equipo", NtdbBO.paises[(tid % 400) - 1] + (tid < NtdbBO.DIF_NT_U21 ? " NT" : " U21"));

			request.getRequestDispatcher("/jsp/asistente/seleccion.jsp").forward(request, response);
		} else {
			request.getRequestDispatcher("/jsp/asistente/ntdb.jsp").forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
