package com.formulamanager.multijuegos.websockets;

import java.util.Date;

import com.google.gson.annotations.Expose;

public class Jugador {
	@Expose
	public String nombre;
	public String contrasenya;
	@Expose
	public int puntos;
	public int num_partidos;
	public String email;
	public Date fecha_alta;
	public boolean invitado;	// Los invitados no se guardan en la BD
	
	public Jugador(String nombre, String contrasenya, int puntos, int num_partidos, String email, Date fecha_alta, boolean invitado) {
		this.nombre = nombre;
		this.contrasenya = contrasenya;
		this.puntos = puntos;
		this.num_partidos = num_partidos;
		this.email = email;
		this.fecha_alta = fecha_alta;
		this.invitado = invitado;
	}

	@Override
	public String toString() {
		return nombre + " [" + puntos + "]";
	}
	
	/* Para EL */

	public String getNombre() {
		return nombre;
	}

	public boolean isInvitado() {
		return invitado;
	}

	public void setInvitado(boolean invitado) {
		this.invitado = invitado;
	}
}
