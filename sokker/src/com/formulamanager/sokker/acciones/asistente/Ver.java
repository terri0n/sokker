package com.formulamanager.sokker.acciones.asistente;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.SystemUtil;

/**
 * Servlet implementation class Ver
 * 
 * Muestra un archivo
 */
@WebServlet("/asistente/ver")
public class Ver extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Ver() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (admin(request)) {
			_log(request, "");

			//File f = new File(AsistenteBO.PATH_BASE + "/logs/_BLOQUEO.log");
			String requested = request.getQueryString();
            if (requested != null && requested.startsWith("logs/")) requested = requested.substring("logs/".length());
            if (requested == null || !requested.matches("_[A-Za-z0-9._-]+\\.log|_NTDB\\.log")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            File logDirectory = new File(SystemUtil.getLogsPath()).getCanonicalFile();
            File f = new File(logDirectory, requested).getCanonicalFile();
            if (!f.getPath().startsWith(logDirectory.getPath() + File.separator) || !f.isFile()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            try (FileInputStream bis = new FileInputStream(f)) {
				BufferedReader br = new BufferedReader(new InputStreamReader(bis));
		        String linea;
				response.getWriter().println("<pre>");
				while ((linea = br.readLine()) != null) {
					if (linea.length() > 0) {
						response.getWriter().println(linea);
					}
				}
				response.getWriter().println("</pre>");
			}
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
}
