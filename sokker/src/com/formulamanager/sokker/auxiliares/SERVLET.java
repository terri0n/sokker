package com.formulamanager.sokker.auxiliares;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class SERVLET extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Deprecated
	protected boolean login(HttpServletRequest request) {
		return request.getSession().getAttribute("login") != null;
	}
	
	protected boolean admin(HttpServletRequest request) {
		return SystemUtil.getVar("factorx_login").equals(request.getSession().getAttribute("login"));
	}
}
