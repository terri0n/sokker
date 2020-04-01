package com.formulamanager.sokker.entity;

import java.util.Date;

public class Partido {
	private Equipo local;
	private Equipo visitante;
	private Integer goles_l;
	private Integer goles_v;

	// Arcade
	private Integer mid;
	private Date fecha;
	
	public Partido() {
	}

	public Partido(Equipo local, Equipo visitante) {
		this.local = local;
		this.visitante = visitante;
		
		if (local == Equipo.DESCANSO) {
			goles_l = 0;
			goles_v = 3;
		} else if (visitante == Equipo.DESCANSO) {
			goles_l = 3;
			goles_v = 0;
		}
	}

	public void sumar_clasificacion() {
		// Los partidos de DESCANSO solo tienen el marcador del equipo con tid
		if (goles_l != null || goles_v != null) {
			sumar_clasificacion(local, goles_l == null ? 0 : goles_l, goles_v == null ? 0 : goles_v);
			sumar_clasificacion(visitante, goles_v == null ? 0 : goles_v, goles_l == null ? 0 : goles_l);
		}
	}

	public void sumar_clasificacion(Equipo e, int gf, int gc) {
		// Los partidos con goles negativos se consideran anulados
		if (gf >= 0 && gc >= 0) {
			e.setPuntos(e.getPuntos() + (gf > gc ? 3 : gf == gc ? 1 : 0));
			e.setJ(e.getJ() + 1);
			e.setG(e.getG() + (gf > gc ? 1 : 0));
			e.setE(e.getE() + (gf == gc ? 1 : 0));
			e.setP(e.getP() + (gf < gc ? 1 : 0));
			e.setGf(e.getGf() + gf);
			e.setGc(e.getGc() + gc);
		}
	}

	public String getClaseL() {
		return goles_l == null || goles_v == null || goles_l == goles_v ? "" : goles_l > goles_v ? "victoria" : "derrota";
	}

	public String getClaseV() {
		return goles_l == null || goles_v == null || goles_l == goles_v ? "" : goles_l < goles_v ? "victoria" : "derrota";
	}

	//-----
	// G&S
	//-----
	
	public Equipo getLocal() {
		return local;
	}
	public void setLocal(Equipo local) {
		this.local = local;
	}
	public Equipo getVisitante() {
		return visitante;
	}
	public void setVisitante(Equipo visitante) {
		this.visitante = visitante;
	}


	public Integer getGoles_l() {
		return goles_l;
	}


	public void setGoles_l(Integer goles_l) {
		this.goles_l = goles_l;
	}


	public Integer getGoles_v() {
		return goles_v;
	}


	public void setGoles_v(Integer goles_v) {
		this.goles_v = goles_v;
	}

	public Integer getMid() {
		return mid;
	}

	public void setMid(Integer mid) {
		this.mid = mid;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
}
