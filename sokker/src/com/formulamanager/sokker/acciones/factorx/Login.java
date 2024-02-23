package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.SystemUtil;

/**
 * Servlet implementation class ServletSokker
 */
@WebServlet("/factorx/login")
public class Login extends SERVLET_FACTORX {
	private static final long serialVersionUID = 1L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SERVLET_FACTORX.TIPO_FACTORX tipo = getTipo(request);
		String ilogin = request.getParameter("login");
		String ipassword = request.getParameter("password");

		String error = "";
		if (ilogin.equals(SystemUtil.getVar("factorx_login")) && ipassword.equals(SystemUtil.getVar("factorx_password"))
				|| tipo == TIPO_FACTORX.junior && ilogin.equals(SystemUtil.getVar("factorx_loginJ")) && ipassword.equals(SystemUtil.getVar("factorx_passwordJ"))
				|| tipo == TIPO_FACTORX.senior && ilogin.equals(SystemUtil.getVar("factorx_loginS")) && ipassword.equals(SystemUtil.getVar("factorx_passwordS"))
				|| tipo == TIPO_FACTORX.internacional && ilogin.equals(SystemUtil.getVar("factorx_loginI")) && ipassword.equals(SystemUtil.getVar("factorx_passwordI"))) {
			request.getSession().setAttribute("login", ilogin);
			request.getSession().setAttribute("password", ipassword);
			request.getSession().setAttribute("tipo", tipo.name());
		} else {
			error = "&error=" + URLEncoder.encode("Error in user or password", "UTF-8");
		}
		
		response.sendRedirect(request.getContextPath() + "/factorx?tipo=" + tipo.name() + error);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//doPost(req, resp);
	}
}
