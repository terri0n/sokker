package com.formulamanager.sokker.acciones.factorx;

import java.io.IOException;
import java.net.URLEncoder;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ServletSokker
 */
@WebServlet("/factorx/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static String getVar(String var) {
		try {
			Context initialContext = new InitialContext();
			return (String) initialContext.lookup("java:comp/env/" + var);
		} catch (NamingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
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
		boolean senior = "1".equals(request.getParameter("senior"));
		String ilogin = request.getParameter("login");
		String ipassword = request.getParameter("password");

		String error = "";
		if (ilogin.equals(getVar("factorx_login")) && ipassword.equals(getVar("factorx_password"))
				|| !senior && ilogin.equals(getVar("factorx_loginJ")) && ipassword.equals(getVar("factorx_passwordJ"))
				|| senior && ilogin.equals(getVar("factorx_loginS")) && ipassword.equals(getVar("factorx_passwordS"))) {
			request.getSession().setAttribute("login", ilogin);
			request.getSession().setAttribute("password", ipassword);
			request.getSession().setAttribute("senior", senior ? "1" : "");
		} else {
			error = "&error=" + URLEncoder.encode("Error in user or password", "UTF-8");
		}
		
		response.sendRedirect(request.getContextPath() + "/factorx?senior=" + (senior ? "1" : "") + error);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//doPost(req, resp);
	}
}
