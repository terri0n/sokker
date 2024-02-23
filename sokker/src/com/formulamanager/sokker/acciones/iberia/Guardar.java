package com.formulamanager.sokker.acciones.iberia;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.CompeticionBO;
import com.formulamanager.sokker.entity.Competicion;

/**
 * Servlet implementation class Guardar
 */
@WebServlet("/iberia/guardar")
public class Guardar extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Guardar() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//request.getRequestDispatcher("/jsp/iberica/iberica.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Competicion competicion = (Competicion) request.getSession().getAttribute("competicion");

		// Si se ha perdido la sesión, recuperamos la competición
		if (competicion == null) {
			competicion = CompeticionBO.leer();
			request.getSession().setAttribute("competicion", competicion);
		}
		
		Enumeration<String> e = request.getParameterNames();
		while (e.hasMoreElements()) {
			String s = e.nextElement();
			if (s.startsWith("resultado")) {
				String[] array = s.split("_");

				// Los resultados de jornadas futuras en las competiciones no tienen tid
				if (array.length == 4) {
					int num_grupo = Integer.valueOf(array[1]);
					int num_jornada = Integer.valueOf(array[2]);
					int tid = Integer.valueOf(array[3]);
					
					if (s.startsWith("resultado_")) {
						competicion.getGrupos().get(num_grupo - 1).getJornadas().get(num_jornada - 1).actualizar_resultado(tid, Util.getInteger(request, s));
					} else if (s.startsWith("resultadoc_")) {
						competicion.getCompeticiones().get(num_grupo - 1).getJornadas().get(num_jornada - 1).actualizar_resultado(tid, Util.getInteger(request, s));
					}
				}
			}
		}

		competicion.actualizar_clasificaciones();
		competicion.comprobar_fin_jornada();
		competicion.comprobar_fin_liga();

		CompeticionBO.guardar(competicion);
		
		response.sendRedirect(request.getContextPath() + "/iberia");
	}
}
