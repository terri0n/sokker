package com.formulamanager.sokker.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.GrupoBO;
import com.formulamanager.sokker.entity.Equipo.TIPO_COMPETICION;

public class Grupo implements Comparable<Grupo> {
	private int numero;
	private List<Equipo> equipos;
	private List<Jornada> jornadas;
	private int num_jornadas;
	private TIPO_COMPETICION tipo_competicion;

	public Grupo(int numero) {
		this.numero = numero;

		equipos = new ArrayList<Equipo>();
		jornadas = new ArrayList<Jornada>();
	}

	public Grupo(int numero, int num_jornadas, TIPO_COMPETICION tipo_competicion) {
		this.numero = numero;
		this.num_jornadas = num_jornadas;
		this.setTipo_competicion(tipo_competicion);
		
		equipos = new ArrayList<Equipo>();
		jornadas = new ArrayList<Jornada>();
	}

	public void actualizar_clasificacion() {
		for (Equipo e : equipos) {
			e.reset();
		}
		
		for (Jornada j : jornadas) {
			for (Partido p : j.getPartidos()) {
				p.sumar_clasificacion();
			}

			if (!j.terminada()) {
				// Si la jornada no ha terminado, dejamos de sumar puntos
				break;
			}
		}

		actualizar_posiciones();
	}

	public void actualizar_posiciones() {
		Collections.sort(equipos, Equipo.getComparator_puntos());
		
		for (int i = 0; i < equipos.size(); i++) {
			equipos.get(i).setPosicion(i + 1);
		}
	}

	public boolean comprobar_fin_jornada() {
		if (jornadas.size() < num_jornadas) {
			// Comprobamos si todos los partidos de la última jornada tienen resultado
			if (jornadas.get(getNum_jornada() - 1).terminada()) {
				GrupoBO.generar_jornada(this);
				return true;
			}
		}
		return false;
	}

	public int getNum_jornada() {
		int i = 1;
		for (Jornada j : jornadas) {
			if (!j.terminada()) {
				return i;
			}
			i++;
		}
		return jornadas.size();
	}
	
	public boolean grupo_termuinado() {
		return jornadas.get(getNum_jornada() - 1).terminada();
	}

	@Override
	public int compareTo(Grupo o) {
		return new Integer(numero).compareTo(new Integer(o.numero));
	}

	public String getNombre() {
		return tipo_competicion == null ? "GROUP " + numero : tipo_competicion.getNombre() + "";
	}
	
	public String getTexto_foro() {
		String res = "[h1]" + getNombre() + "[/h1]\n\n";

		if (getNum_jornada() > 0) {
			for (int jornada = 1; jornada <= jornadas.size(); jornada++) {
				res += "[b]Day " + jornada + "[/b]\n\n";
				Jornada j = jornadas.get(jornada - 1);
				
				for (Partido p : j.getPartidos()) {
					res += p.getLocal() + " - " + p.getVisitante() 
						+ " - [url=studio/matchID/" + Util.nvl(Util.integerToString(p.getMid())) + "]" + (p.getFecha() == null ? "?" : Util.dateTimeToString(p.getFecha())) + "[/url]\n\n";
				}
			}
		}
			
		return res;
	}
	
	//-----
	// G&S
	//-----

	public List<Equipo> getEquipos() {
		return equipos;
	}

	public void setEquipos(List<Equipo> equipos) {
		this.equipos = equipos;
	}

	public List<Jornada> getJornadas() {
		return jornadas;
	}

	public void setJornadas(List<Jornada> jornadas) {
		this.jornadas = jornadas;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public int getNum_jornadas() {
		return num_jornadas;
	}

	public void setNum_jornadas(int num_jornadas) {
		this.num_jornadas = num_jornadas;
	}

	public TIPO_COMPETICION getTipo_competicion() {
		return tipo_competicion;
	}

	public void setTipo_competicion(TIPO_COMPETICION tipo_competicion) {
		this.tipo_competicion = tipo_competicion;
	}
}
