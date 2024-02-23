package com.formulamanager.sokker.auxiliares;

import javax.security.auth.login.LoginException;

/**
 * La creo para diferenciar un error en el usuario o contraseña de otro error
 * De esta forma lo podré capturar para aumentar el nº de intentos fallidos
 * 
 * @author Levi
 *
 */
public class LoginExceptionExt extends LoginException {
	private static final long serialVersionUID = -3618803381842017364L;
	private String usuario;
	private String contrasenya;
	
	public LoginExceptionExt(String msg, String usuario, String contrasenya) {
		super(msg);
		this.usuario = usuario;
		this.contrasenya = contrasenya;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getContrasenya() {
		return contrasenya;
	}

	public void setContrasenya(String contrasenya) {
		this.contrasenya = contrasenya;
	}
}
