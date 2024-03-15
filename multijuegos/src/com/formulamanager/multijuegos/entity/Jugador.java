package com.formulamanager.multijuegos.entity;

import java.util.Date;

public class Jugador extends EntityBase implements Comparable<Jugador> {
	public transient static int num_invitados = 0;

	public String nombre;
	public Integer puntos;	// Solo para usuarios registrados
	public String pais;

	public transient String contrasenya;
	public transient int num_partidos;
	public transient String email;
	public transient Date fecha_alta;
	public transient boolean invitado;	// Los invitados no se guardan en la BD

	public Jugador(String nombre, String contrasenya, Integer puntos, String pais, int num_partidos, String email, Date fecha_alta, boolean invitado) {
		this.nombre = nombre;
		this.contrasenya = contrasenya;
		this.puntos = puntos;
		this.pais = pais;
		this.num_partidos = num_partidos;
		this.email = email;
		this.fecha_alta = fecha_alta;
		this.invitado = invitado;
	}

	@Override
	public String toString() {
		return nombre + " [" + puntos + "]";
	}

	@Override
	public int compareTo(Jugador j) {
		return nombre.compareTo(j.nombre);
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

	public static Jugador getDefault() {
		return new Jugador("_Guest" + ++num_invitados + "_", null, null, "_unknown", 0, null, null, true);
	}
}
