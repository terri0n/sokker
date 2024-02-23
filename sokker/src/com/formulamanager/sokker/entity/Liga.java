package com.formulamanager.sokker.entity;

import java.util.ArrayList;
import java.util.List;

public class Liga {
	private int id_liga;
	private String nombre;
	private int division;
	private int numero;

	//-----------
	// Entidades
	//-----------
	
	private List<Equipo> equipos;

	//---------------
	// Constructores
	//---------------
	
	public Liga() {
		equipos = new ArrayList<Equipo>();
	}
	
	//---------
	// Métodos
	//---------
	
	//-------
	// GG&SS
	//-------
	
	public int getId_liga() {
		return id_liga;
	}
	public void setId_liga(int id_liga) {
		this.id_liga = id_liga;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public List<Equipo> getEquipos() {
		return equipos;
	}
	public void setEquipos(List<Equipo> equipos) {
		this.equipos = equipos;
	}
	public int getDivision() {
		return division;
	}
	public void setDivision(int division) {
		this.division = division;
	}
	public int getNumero() {
		return numero;
	}
	public void setNumero(int numero) {
		this.numero = numero;
	}
}
