package com.formulamanager.sokker.acciones.iberia;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.CompeticionBO;
import com.formulamanager.sokker.entity.Competicion;

/**
 * Servlet implementation class Generar
 */
@WebServlet("/iberia/generar")
public class Generar extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Generar() {
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
		String equipos = Util.getString(request, "equipos");
		Integer equipos_grupo = Util.getInteger(request, "equipos_grupo");
		Integer num_jornadas = Util.getInteger(request, "num_jornadas");
		int equipos_champions = Util.invl(Util.getInteger(request, "equipos_champions"));
		int equipos_uefa = Util.invl(Util.getInteger(request, "equipos_uefa"));
		Integer equipos_cp = Util.getInteger(request, "equipos_cp");
		boolean doble_vuelta = Util.getBoolean(request, "doble_vuelta");
		
		if (equipos != null) {
			List<Integer> lista_ids = Util.splitInt(equipos, " ");
			Competicion competicion = new Competicion(lista_ids, equipos_grupo, num_jornadas, equipos_champions, equipos_uefa, equipos_cp, doble_vuelta);
			CompeticionBO.guardar(competicion);
			
			request.getSession().setAttribute("competicion", competicion);
		}
		
		response.sendRedirect(request.getContextPath() + "/iberia");
	}
}
