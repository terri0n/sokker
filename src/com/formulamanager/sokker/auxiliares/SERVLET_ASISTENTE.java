package com.formulamanager.sokker.auxiliares;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import com.formulamanager.sokker.entity.Usuario;

public class SERVLET_ASISTENTE extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected boolean login(HttpServletRequest request) {
		return getUsuario(request) != null;
	}
	
	protected Usuario getUsuario(HttpServletRequest request) {
		return (Usuario)request.getSession().getAttribute("usuario");
	}

	protected boolean admin(HttpServletRequest request) {
		return login(request) && getUsuario(request).getLogin().equals("terrion");
	}
}
