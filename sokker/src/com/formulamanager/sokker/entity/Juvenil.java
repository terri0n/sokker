package com.formulamanager.sokker.entity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;

public class Juvenil extends Jugador {
	private static BigDecimal un_tercio = BigDecimal.ONE.divide(new BigDecimal(3), MathContext.DECIMAL32);
	
	private int nivel;
	private int semanas;
	private boolean jugador_campo;
	private BigDecimal nivel_min;
	private BigDecimal nivel_max;

	// y = m * x + n
	private float m;	// Inversa del talento
	private float n;

	public Juvenil(int pid) {
		setPid(pid);
	}

	public Juvenil(int pid, String nombre, int jornada, int edad, int nivel, int semanas, boolean jugador_campo, Usuario usuario) {
		setPid(pid);
		setUsuario(usuario);
		setNombre(nombre);
		this.setJugador_campo(jugador_campo);

		setJornada(jornada);
		setEdad(edad);
		this.setNivel(nivel);
		this.setSemanas(semanas);
	}

	public Juvenil(int pid, List<String> valores, boolean primero, String nombre, Usuario usuario) {
		setPid(pid);
		setUsuario(usuario);
		int i = 0;

		if (primero) {
			setNombre(valores.get(i++));
			setJugador_campo(Util.stringToBoolean(valores.get(i++)));
		} else {
			setNombre(nombre);
		}
		
		setJornada(Util.stringToInteger(valores.get(i++)));
		setEdad(Util.stringToInteger(valores.get(i++)));
		setNivel(Util.stringToInteger(valores.get(i++)));
		setSemanas(Util.stringToInteger(valores.get(i++)));
		
		if (valores.size() > i) {
			List<String> sublista = valores.subList(i, valores.size());
			setOriginal(new Juvenil(pid, sublista, false, getNombre(), usuario));
		}
	}
	
	@Override
	public String serializar(boolean primero) {
		List<String> valores = new ArrayList<String>();
		if (primero) {
			valores.add(getNombre());
			valores.add(isJugador_campo()+"");
		}
		
		valores.add(getJornada()+"");
		valores.add(getEdad()+"");
		
		valores.add(getNivel()+"");
		valores.add(getSemanas()+"");
		
		String serializacion_original = "";
		// Evito bucles infinitos
		if (getOriginal() != null && !getJornada().equals(getOriginal().getJornada())) {
			serializacion_original = "," + getOriginal().serializar(false);
		}
		
		return String.join(",", valores) + serializacion_original;
	}
	
	@Override
	public int compareTo(Jugador j) {
		if (j != null && j instanceof Juvenil) {
			return new Integer(getSemanas()).compareTo(((Juvenil)j).getSemanas());
		} else {
			return 0;
		}
	}

	public String getClase_nivel() {
		return getNum_jornadas() < 2 ? "nuevo_juvenil" : getClase_cambio(Math.abs(nivel), getOriginal() == null ? null : Math.abs(getOriginal().nivel), null);
	}
	
	public String getTitle_nivel() {
		return getOriginal() == null || Math.abs(getOriginal().nivel) == Math.abs(nivel) ? "" : new DecimalFormat("+#;-#").format(Math.abs(nivel) - Math.abs(getOriginal().nivel));
	}

	public String getEdad_proyectada() {
		int anyos = ((getJornada() % AsistenteBO.JORNADAS_TEMPORADA) + getSemanas()) / AsistenteBO.JORNADAS_TEMPORADA;
		int extra = ((getJornada() % AsistenteBO.JORNADAS_TEMPORADA) + getSemanas()) % AsistenteBO.JORNADAS_TEMPORADA;
		String clase;
		switch (getEdad()) {
			case 16:
			case 17: clase = "negrita";	break;
			default: clase = "";
		}
		return "<span class='" + clase + "'>" + getEdad() + "</span>" + (anyos > 0 ? " <span style='color: gray'>(+" + anyos + (extra > 0 ? "<small>." + extra + "</small>" : "") + ")</span>" : "");
	}

	public void calcular_niveles() {
		Integer anterior = null;
		BigDecimal max_anterior = null;
		BigDecimal min_anterior = null;
		List<Juvenil> inverso = new ArrayList<Juvenil>();

		Juvenil j = this;
		int jornada_anterior = j.getJornada();

		// Primero acoto los valores en una direcci�n
		do {
			int niv = Math.abs(j.nivel);
			inverso.add(j);
			BigDecimal salto = new BigDecimal(jornada_anterior - j.getJornada()).multiply(un_tercio);
			j.nivel_max = new BigDecimal(niv + 3).min(max_anterior == null ? new BigDecimal(18) : max_anterior);
			j.nivel_min = new BigDecimal(niv - 2).max(Util.bdnvl(min_anterior).subtract(salto)).max(BigDecimal.ZERO);
			
			Integer siguiente = j.getOriginal() == null ? null : Math.abs(j.getOriginal().getNivel());
			// M�ximos y m�nimos
			if (anterior != null && siguiente != null) {
				if (anterior < niv && siguiente < niv) {
					j.nivel_max = j.nivel_max.min(new BigDecimal(niv + 1));
				} else if (anterior > niv && siguiente > niv) {
					j.nivel_min = j.nivel_min.max(new BigDecimal(niv));
				} else if (siguiente == niv && j.getOriginal().getOriginal() != null) {
					Integer siguiente2 = Math.abs(j.getOriginal().getOriginal().getNivel());
					if (anterior < niv && siguiente2 < niv) {
						j.nivel_max = j.nivel_max.min(new BigDecimal(niv + 1).add(salto));
					} else if (anterior > niv && siguiente2 > niv) {
						j.nivel_min = j.nivel_min.max(new BigDecimal(niv));
					} else if (siguiente2 == niv && j.getOriginal().getOriginal().getOriginal() != null) {
						Integer siguiente3 = Math.abs(j.getOriginal().getOriginal().getOriginal().getNivel());
						if (anterior < niv && siguiente3 < niv) {
							j.nivel_max = j.nivel_max.min(new BigDecimal(niv + 1).add(salto.add(un_tercio)));
						} else if (anterior > niv && siguiente3 > niv) {
							j.nivel_min = j.nivel_min.max(new BigDecimal(niv));
						} else if (siguiente3 == niv && j.getOriginal().getOriginal().getOriginal().getOriginal() != null) {
							Integer siguiente4 = Math.abs(j.getOriginal().getOriginal().getOriginal().getOriginal().getNivel());
							if (anterior < niv && siguiente4 < niv) {
								j.nivel_max = j.nivel_max.min(new BigDecimal(niv + 1).add(salto.add(un_tercio.multiply(new BigDecimal(2)))));
							} else if (anterior > niv && siguiente4 > niv) {
								j.nivel_min = j.nivel_min.max(new BigDecimal(niv));
							}
						}
					}
				}
			}

			max_anterior = j.nivel_max;
			min_anterior = j.nivel_min;
			anterior = niv;
			jornada_anterior = j.getJornada();
			j = j.getOriginal();
		} while (j != null);
		
		// Y luego en la direcci�n inversa
		Collections.reverse(inverso);
		max_anterior = null;
		min_anterior = null;
		for (Juvenil juv : inverso) {
			if (max_anterior != null) {
				BigDecimal salto = juv.getOriginal() == null ? BigDecimal.ZERO : new BigDecimal(juv.getJornada() - juv.getOriginal().getJornada()).multiply(un_tercio);
				juv.nivel_max = juv.nivel_max.min(max_anterior.add(salto));
			}
			if (min_anterior != null) {
				juv.nivel_min = juv.nivel_min.max(min_anterior);
			}

			max_anterior = juv.nivel_max;
			min_anterior = juv.nivel_min;
		}
		
		//---------
		// Talento
		// http://javaparainocentesalmas.blogspot.com/2017/01/java-regresion-lineal-codigo.html
		//---------

		// Parece equivalente a calcRectaRegresionYsobreX
		float[] resultado = AsistenteBO.regresion_lineal(inverso);
		m = resultado[0];
		n = resultado[1];

		// Para el JSP
		setTalento(m <= 0 || getNum_jornadas() < 5 ? null : Util.round(1D/m, getUsuario().getNumero_decimales()));
	}

	public String getDatos_grafica_nivel() {
		return getDatos_graficas(new Dataset[] {new Dataset(BLUE, "transparent", Util.getTexto(getUsuario().getLocale(), "common.level"), this, null) {
			@Override
			public Float getValor(Jugador j) {
				return Math.abs(((Juvenil)j).nivel) + 0.5f;
			}
		}, new Dataset(ORANGE, "transparent", Util.getTexto(getUsuario().getLocale(), "common.maximum") + "/" + Util.getTexto(getUsuario().getLocale(), "common.minimum"), this, null) {
			@Override
			public Float getValor(Jugador j) {
				return ((Juvenil)j).getNivel_max().floatValue();
			}
		}, new Dataset(ORANGE, "transparent", null, this, null) {
			@Override
			public Float getValor(Jugador j) {
				return ((Juvenil)j).getNivel_min().floatValue();
			}
		}, new Dataset(PINK, "transparent", Util.getTexto(getUsuario().getLocale(), "skills.talent"), this, null) {
			@Override
			public Float getValor(Jugador j, int x) {
				return m * x + n + 0.5f;
			}
			@Override
			protected Float getValor(Jugador j) {
				return null;
			}
		}}, 18F, Util.getTexto(getUsuario().getLocale(), "players.talent_of", getNombre()) + ": " + getTalento());
	}

	public String getNiveles(Integer jornada_anterior) {
		String salida = "";
		
		if (jornada_anterior != null) {
			for (int i = 0; i < jornada_anterior - getJornada(); i++) {
				salida += ",";
			}
		}

		salida = getNivel() + salida;

		if (getOriginal() != null) {
			salida = getOriginal().getNiveles(getJornada()) + salida;
		}
		
		return salida;
	}

	public int getNivel_proyectado() {
		return Math.abs(nivel) + un_tercio.min(getNum_jornadas() < 5 ? un_tercio : new BigDecimal(m)).multiply(new BigDecimal(semanas)).add(new BigDecimal("0.5")).setScale(0, RoundingMode.FLOOR).intValue();
	}
	
	public String getClase_talento() {
		// NOTA: lo dejo a 16 porque el tama�o de la temporada no deber�a influir
		int anyos = getEdad() + (((getJornada() + 1) % 16) + getSemanas()) / 16;
		int nivel_minimo = (anyos - 15) * 3 + (int)((getSemanas() % 16) / 5.3);

		return getNivel_proyectado() >= nivel_minimo ? "" : "rojo";
	}

	@Override
	public Juvenil getOriginal() {
		return (Juvenil)super.getOriginal();
	}

	public Integer getTotal_semanas() {
		if (getOriginal() == null) {
			return semanas;
		} else {
			return getOriginal().getTotal_semanas();
		}
	}

	public Integer getNivel_inicial() {
		if (getOriginal() == null) {
			return Math.abs(nivel);
		} else {
			return getOriginal().getNivel_inicial();
		}
	}

	/** Las jornadas se insertan en orden inverso */
	@Override
	public void anyadir_original(Jugador j) {
		// Cuento los cumpleaños que han pasado
		int dif_jornadas = getJornada() - j.getJornada();
		int cumpleanyos = dif_jornadas / 13;
		int mod_jornadas = dif_jornadas % 13;
		cumpleanyos += AsistenteBO.getJornadaMod(getJornada()) < mod_jornadas ? 1 : 0;
		j.setEdad(getEdad() - cumpleanyos);

		j.setOriginal(getOriginal());
		setOriginal(j);
	}

	// Insertamos una jornada concreta en su sitio
	public void insertar_jornada(Juvenil j) {
		if (getOriginal() == null || getOriginal().getJornada() < j.getJornada()) {
			j.setOriginal(getOriginal());
			setOriginal(j);
		} else if (getOriginal().getJornada() > j.getJornada()) {
			getOriginal().insertar_jornada(j);
		} else {
			// La jornada ya existe. No hacemos nada
		}
	}
	
	/**
	 * G&S
	 */
	
	public int getNivel() {
		return nivel;
	}

	public void setNivel(int nivel) {
		this.nivel = nivel;
	}

	public int getSemanas() {
		return semanas;
	}

	public void setSemanas(int semanas) {
		this.semanas = semanas;
	}

	public boolean isJugador_campo() {
		return jugador_campo;
	}

	public void setJugador_campo(boolean jugador_campo) {
		this.jugador_campo = jugador_campo;
	}

	public BigDecimal getNivel_min() {
		return nivel_min;
	}

	public void setNivel_min(BigDecimal nivel_min) {
		this.nivel_min = nivel_min;
	}

	public BigDecimal getNivel_max() {
		return nivel_max;
	}

	public void setNivel_max(BigDecimal nivel_max) {
		this.nivel_max = nivel_max;
	}

	public float getM() {
		return m;
	}

	public void setM(float m) {
		this.m = m;
	}

	public float getN() {
		return n;
	}

	public void setN(float n) {
		this.n = n;
	}
}
