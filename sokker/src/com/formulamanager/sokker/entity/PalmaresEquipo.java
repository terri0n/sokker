package com.formulamanager.sokker.entity;

public class PalmaresEquipo implements Comparable<PalmaresEquipo> {
	private int tid;
	private String nombre;
	private int[] posicion = new int[3];

	public PalmaresEquipo(Integer tid, String nombre) {
		this.setTid(tid);
		this.setNombre(nombre);
	}
	
	@Override
	public int compareTo(PalmaresEquipo o) {
		return -(getPosicion()[0] == o.getPosicion()[0] ?
				getPosicion()[1] == o.getPosicion()[1] ?
				Integer.valueOf(getPosicion()[2]).compareTo(o.getPosicion()[2]) :
				Integer.valueOf(getPosicion()[1]).compareTo(o.getPosicion()[1]) :
				Integer.valueOf(getPosicion()[0]).compareTo(o.getPosicion()[0]));
	}
	
	@Override
	public boolean equals(Object obj) {
		return Integer.valueOf(tid).equals(((PalmaresEquipo)obj).tid);
	}

	public boolean esta_vacio() {
		return posicion[0] + posicion[1] + posicion[2] == 0;
	}
	
	//---------------
	
	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int[] getPosicion() {
		return posicion;
	}

	public void setPosicion(int[] posicion) {
		this.posicion = posicion;
	}
}
