package com.formulamanager.sokker.acciones.iberia;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.bo.CompeticionBO;
import com.formulamanager.sokker.entity.Competicion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Servlet implementation class Iberia
 * 
 * 
 * <Funcionamiento>
 * ----------------
 * Obtiene los nombres y el ranking de los equipos cuya ID se especifica
 * 
 * La competición consta de 2 fases:
 * - Fase de grupos
 * 		Se ordenan los equipos por ranking. Cada n equipos se sortean en los n grupos
 * 		Puede ser tipo juveniles o todos contra todos, a una vuelta o a dos <pendiente>
 * - Fase de eliminatorias
 * 		Se generan 3 competiciones obteniendo los equipos necesarios de la fase previa con el siguiente criterio de selección: posición, puntos, avg, partidos ganados
 * 		Se crean dos bombos: la mitad mejor contra la mitad peor, y se emparejan aleatoriamente
 * 
 * Se puede saltar la fase de grupos indicando que tiene 0 jornadas <pendiente>
 */
@WebServlet("/iberia")
public class Iberia extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Iberia() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("user") != null && request.getParameter("user").equals("Antoś") || request.getSession().getAttribute("user").equals("Antoś")) {
			request.getSession().setAttribute("user", "antos");
			
			if (request.getSession().getAttribute("competicion") == null) {
				try {
					Competicion competicion = CompeticionBO.leer();
					request.getSession().setAttribute("competicion", competicion);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	
			try {
				new Navegador(true, request) {
					@Override
					protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
						CompeticionBO.buscar_arcades((Competicion)request.getSession().getAttribute("competicion"), navegador);
					}
				};
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			request.getRequestDispatcher("/jsp/iberia/iberia.jsp").forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//
	}
}
