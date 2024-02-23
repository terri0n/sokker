package com.formulamanager.sokker.entity;

import java.util.ArrayList;
import java.util.List;

public class Jornada {
	private List<Partido> partidos;
	private int numero;

	public List<Partido> getPartidos() {
		return partidos;
	}

	public void setPartidos(List<Partido> partidos) {
		this.partidos = partidos;
	}

	public Jornada(int numero) {
		this.numero = numero;
		
		partidos = new ArrayList<Partido>();
	}

	public void actualizar_resultado(Integer tid, Integer goles) {
		for (Partido p : partidos) {
			if (tid.equals(p.getLocal().getTid())) {
				p.setGoles_l(goles);
				return;
			} else if (tid.equals(p.getVisitante().getTid())) {
				p.setGoles_v(goles);
				return;
			}
		}
		
		throw new RuntimeException("Equipo no encontrado: " + tid + " en jornada " + numero);
	}

	public boolean terminada() {
		for (Partido p : partidos) {
			// Los partidos de DESCANSO solo tienen el marcador del equipo con tid
			if (p.getGoles_l() == null && p.getGoles_v() == null) {
				return false;
			}
		}
		return true;
	}

	public int getNum_partidos() {
		return partidos.size();
	}

	public int getRowspan() {
		return (int) Math.pow(2, numero - 1);
	}
	
	//-----
	// G&S
	//-----

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

}
