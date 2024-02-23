package com.formulamanager.sokker.entity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.NtdbBO;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

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

	public static Comparator<? super Partido> getComparator() {
		return new Comparator<Partido>() {
			@Override
			public int compare(Partido o1, Partido o2) {
				return o1.fecha.compareTo(o2.fecha);
			}
		};
	}
	
	public static Partido nuevo_nt(int tid, int jornada_actual, WebClient navegador) throws ParseException, FailingHttpStatusCodeException, MalformedURLException, IOException {
		XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/matches-team-" + tid + ".xml");

		ArrayList<DomNode> partidos = (ArrayList<DomNode>) pagina.getByXPath("//match");
		Date hora = null;
		
		for (DomNode partido : partidos) {
			Date fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(((DomText) partido.getFirstByXPath("dateExpected/text()")).asText());
			int jornada = Integer.valueOf(((DomText)partido.getFirstByXPath("week/text()")).asText());
			Integer tid_local = Integer.valueOf(((DomText) partido.getFirstByXPath("homeTeamID/text()")).asText());

			// Me guardo la hora de otro partido como local por si el partido de la jornada todavía no tiene hora
			if (tid_local == tid && fecha.getYear() != -1898) {
				hora = fecha;
			}
			
			// Si el partido es el de esta jornada y además tiene fecha o bien "tid" juega de local (con lo que la fecha del partido anterior en casa nos vale)...
			if (jornada == jornada_actual && (tid_local == tid || fecha.getYear() != -1898)) {
				Integer tid_visitante = Integer.valueOf(((DomText) partido.getFirstByXPath("awayTeamID/text()")).asText());
				Integer mid = Integer.valueOf(((DomText) partido.getFirstByXPath("matchID/text()")).asText());
				boolean isFinished = ((DomText) partido.getFirstByXPath("isFinished/text()")).asText().equals("1");

				Partido p = new Partido (new Equipo(NtdbBO.paises[tid_local % 400 - 1], null, tid_local, null), new Equipo(NtdbBO.paises[tid_visitante % 400 - 1], null, tid_visitante, null));
				
				p.setFecha(Util.limpiar_dia(fecha.getYear() == -1898 && hora != null ? hora : fecha));
				if (p.getFecha().getHours() < 7) {
					p.getFecha().setDate(2);
				}
				p.setMid(mid);
				if (isFinished) {
					p.setGoles_l(Integer.valueOf(((DomText) partido.getFirstByXPath("homeTeamScore/text()")).asText()));
					p.setGoles_v(Integer.valueOf(((DomText) partido.getFirstByXPath("awayTeamScore/text()")).asText()));
				}
				return p;
			}
		}
		
		return null;
	}

	@Override
	public boolean equals(Object p2) {
		return p2 != null && ((Partido)p2).local.getTid().equals(local.getTid()) && ((Partido)p2).visitante.getTid().equals(visitante.getTid());
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
