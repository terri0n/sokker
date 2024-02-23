package com.formulamanager.sokker.entity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import com.formulamanager.sokker.bo.AsistenteBO;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class Equipo {
	public enum TIPO_COMPETICION { CHAMPIONS, UEFA, OTROS("Losers Cup");
		private String nombre;

		private TIPO_COMPETICION() {
			nombre = name();
		}
		private TIPO_COMPETICION(String nombre) {
			this.nombre = nombre;
		}
		public String getNombre() {
			return nombre;
		}
	}
	
	public static Equipo VACIO = new Equipo("&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;", null, null, null);
	public static Equipo DESCANSO = new Equipo("BYE", null, null, null);
	
	private String nombre;
	private Date fecha;
	private Integer tid;
	private BigDecimal rank;

	// Clasificación
	private int puntos;
	private int j;
	private int g;
	private int e;
	private int p;
	private int gf;
	private int gc;
	private int posicion;

	private int puntos_base;
	private int gf_base;
	private int gc_base;
	
	private TIPO_COMPETICION tipo_competicion;

	// Ascensos/descensos
	private Liga liga;
	
	//---------------
	// Constructores
	//---------------
	
	public Equipo() {
	}
	
	public Equipo (String nombre, Date fecha, Integer tid, BigDecimal rank) {
		this.nombre = nombre;
		this.fecha = fecha;
		this.tid = tid;
		this.setRank(rank);
	}
	
	//---------
	// Métodos
	//---------
	
	public static Equipo nuevo(Integer tid, WebClient navegador) {
		try {
			XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/team-" + tid + ".xml");

			DomText usuario = (DomText) pagina.getFirstByXPath("//user//userID/text()");
			if (usuario != null) {
				DomText nombre = (DomText) pagina.getFirstByXPath("//team//name/text()");
				DomText fecha = (DomText) pagina.getFirstByXPath("//team//dateCreated/text()");
				DomText rank = (DomText) pagina.getFirstByXPath("//team//rank/text()");

				return new Equipo (nombre.asText(), new SimpleDateFormat("yyyy-MM-dd").parse(fecha.asText()), tid, new BigDecimal(rank.asText()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Comparator<? super Equipo> getComparator() {
		return new Comparator<Equipo>() {
			@Override
			public int compare(Equipo o1, Equipo o2) {
				return o1.fecha.compareTo(o2.fecha);
			}
		};
	}

	public static Comparator<? super Equipo> getComparator_rank() {
		return new Comparator<Equipo>() {
			@Override
			public int compare(Equipo o1, Equipo o2) {
				return -o1.rank.compareTo(o2.rank);
			}
		};
	}
	
	public static Comparator<? super Equipo> getComparator_puntos() {
		return new Comparator<Equipo>() {
			@Override
			public int compare(Equipo o1, Equipo o2) {
				if (o1.puntos != o2.puntos) {
					return -new Integer(o1.puntos).compareTo(o2.puntos);
				} else if (o1.getAvg() != o2.getAvg()) {
					return -o1.getAvg().compareTo(o2.getAvg());
				} else if (o1.gf != o2.gf) {
					return -new Integer(o1.gf).compareTo(o2.gf);
				} else {
					return new Integer(o1.posicion).compareTo(o2.posicion);
				}
			}
		};
	}

	public static Comparator<? super Equipo> getComparator_competicion() {
		return new Comparator<Equipo>() {
			@Override
			public int compare(Equipo o1, Equipo o2) {
				if (o1.posicion != o2.posicion) {
					return new Integer(o1.posicion).compareTo(o2.posicion);
				} else if (o1.puntos != o2.puntos) {
					return -new Integer(o1.puntos).compareTo(o2.puntos);
				} else if (o1.getAvg() != o2.getAvg()) {
					return -o1.getAvg().compareTo(o2.getAvg());
				} else {
					return -new Integer(o1.gf).compareTo(o2.gf);
				}
			}
		};
	}

	/**
	 * Pasa saber si el quipo asciende o promociona, se comprueba primero la posición
	 * @return
	 */
	public static Comparator<? super Equipo> getComparator_ascensos() {
		return new Comparator<Equipo>() {
			@Override
			public int compare(Equipo o1, Equipo o2) {
				if (o1.posicion != o2.posicion) {
					return new Integer(o1.posicion).compareTo(o2.posicion);
				} else if (o1.puntos != o2.puntos) {
					return -new Integer(o1.puntos).compareTo(o2.puntos);
				} else if (o1.getAvg() != o2.getAvg()) {
					return -o1.getAvg().compareTo(o2.getAvg());
				} else if (o1.gf != o2.gf) {
					return -new Integer(o1.gf).compareTo(o2.gf);
				} else {
					return -o1.getRank().compareTo(o2.getRank());
				}
			}
		};
	}

	/**
	 * Luego, para hacer los emparejamientos, se ordena cada grupo (ascenso, promoción) sin tener en cuenta la posición
	 * @return
	 */
	public static Comparator<? super Equipo> getComparator_emparejamientos() {
		return new Comparator<Equipo>() {
			@Override
			public int compare(Equipo o1, Equipo o2) {
				if (o1.puntos != o2.puntos) {
					return -new Integer(o1.puntos).compareTo(o2.puntos);
				} else if (o1.getAvg() != o2.getAvg()) {
					return -o1.getAvg().compareTo(o2.getAvg());
				} else if (o1.getRank() != null && o1.getRank().compareTo(o2.getRank()) != -1) {
					return -o1.getRank().compareTo(o2.getRank());
				} else if (o1.gf != o2.gf) {
					return -new Integer(o1.gf).compareTo(o2.gf);
				} else if (o1.posicion != o2.posicion) {
					return new Integer(o1.posicion).compareTo(o2.posicion);
				} else {
					return -o1.getRank().compareTo(o2.getRank());
				}
			}
		};
	}

	public Integer getAvg() {
		return gf - gc;
	}

	public void reset() {
		puntos = puntos_base;
		j = 0;
		g = 0;
		e = 0;
		p = 0;
		gf = gf_base;
		gc = gc_base;
	}

	@Override
	public String toString() {
		return this == DESCANSO ? getNombre() : ("[tid=" + getTid() + "]" + getNombre() + "[/tid]");
	}

	//-----
	// G&S
	//-----
	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Integer getTid() {
		return tid;
	}

	public void setTid(Integer tid) {
		this.tid = tid;
	}

	public int getPuntos() {
		return puntos;
	}

	public void setPuntos(int puntos) {
		this.puntos = puntos;
	}

	public int getJ() {
		return j;
	}

	public void setJ(int j) {
		this.j = j;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getE() {
		return e;
	}

	public void setE(int e) {
		this.e = e;
	}

	public int getP() {
		return p;
	}

	public void setP(int p) {
		this.p = p;
	}

	public int getGf() {
		return gf;
	}

	public void setGf(int gf) {
		this.gf = gf;
	}

	public int getGc() {
		return gc;
	}

	public void setGc(int gc) {
		this.gc = gc;
	}

	public TIPO_COMPETICION getTipo_competicion() {
		return tipo_competicion;
	}

	public void setTipo_competicion(TIPO_COMPETICION tipo_competicion) {
		this.tipo_competicion = tipo_competicion;
	}

	public int getPosicion() {
		return posicion;
	}

	public void setPosicion(int posicion) {
		this.posicion = posicion;
	}

	public BigDecimal getRank() {
		return rank;
	}

	public void setRank(BigDecimal rank) {
		this.rank = rank;
	}

	public int getPuntos_base() {
		return puntos_base;
	}

	public void setPuntos_base(int puntos_base) {
		this.puntos_base = puntos_base;
	}

	public int getGf_base() {
		return gf_base;
	}

	public void setGf_base(int gf_base) {
		this.gf_base = gf_base;
	}

	public int getGc_base() {
		return gc_base;
	}

	public void setGc_base(int gc_base) {
		this.gc_base = gc_base;
	}

	public Liga getLiga() {
		return liga;
	}

	public void setLiga(Liga liga) {
		this.liga = liga;
	}
}
