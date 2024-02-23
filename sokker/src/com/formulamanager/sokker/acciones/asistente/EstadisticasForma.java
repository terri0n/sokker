package com.formulamanager.sokker.acciones.asistente;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.bo.AsistenteBO;

/**
 * Estadísticas Forma
 */
@WebServlet("/asistente/estadisticasForma")
public class EstadisticasForma extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EstadisticasForma() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
// 		16/01/2021:
//		usuarios: 913
		
//		Porteria: 83.25525 (66)
//		Creacion: 72.230606 (639)
//		Pases: 72.189445 (1190)
//		Tecnica: 78.38207 (2217)
//		Defensa: 82.44144 (3207)
//		Anotacion: 81.988304 (3564)
//		Rapidez: 95.54699 (6584)
	
//		07/05/2023:
//		Desde la jornada 1025
		
//		Porteria: 89.86271 (15)
//		Creacion: 80.88289 (25)
//		Pases: 77.24697 (34)
//		Tecnica: 76.64333 (62)
//		Defensa: 77.940765 (82)
//		Anotacion: 80.0577 (99)
//		Rapidez: 94.84982 (160)
		
//		03/02/2024:
		
//		Porteria: 87.25026 (489)
//		Creacion: 77.65487 (1755)
//		Pases: 75.85474 (2946)
//		Tecnica: 81.35554 (5083)
//		Defensa: 83.12447 (6721)
//		Anotacion: 83.61134 (7762)
//		Rapidez: 92.14835 (11512)
		
//		AsistenteBO.getDatos_estadisticas_entrenamiento();
//		AsistenteBO.exportar_todo();
		request.getRequestDispatcher("/jsp/asistente/forma.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
