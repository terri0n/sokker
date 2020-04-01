package com.formulamanager.sokker.auxiliares;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class SERVLET extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected boolean login(HttpServletRequest request) {
		//return request.getSession().getAttribute("login") != null && Util.nvl(request.getParameter("senior")).equals(request.getSession().getAttribute("senior"));
		return request.getSession().getAttribute("login") != null;
	}
}
