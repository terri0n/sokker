package com.formulamanager.sokker.auxiliares;

import javax.security.auth.login.LoginException;

/**
 * La creo para diferenciar un error en el usuario o contraseña de otro error.
 * De esta forma lo podré capturar para aumentar el nº de intentos fallidos.
 */
public class LoginExceptionExt extends LoginException {
	private static final long serialVersionUID = -3618803381842017364L;
	private String usuario;

	public LoginExceptionExt(String msg, String usuario) {
		super(msg);
		this.usuario = usuario;
	}

	/**
	 * Compatibilidad de fuente con llamadores antiguos. La contraseña se ignora
	 * deliberadamente y nunca se almacena en la excepción.
	 */
	@Deprecated
	public LoginExceptionExt(String msg, String usuario, String ignoredPassword) {
		this(msg, usuario);
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
}
