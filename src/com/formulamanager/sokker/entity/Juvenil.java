package com.formulamanager.sokker.entity;

import java.math.BigDecimal;
import java.math.MathContext;
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
	
	public Juvenil(int pid, String nombre, int jornada, int edad, int nivel, int semanas, boolean jugador_campo) {
		setPid(pid);
		setNombre(nombre);
		this.setJugador_campo(jugador_campo);

		setJornada(jornada);
		setEdad(edad);
		this.setNivel(nivel);
		this.setSemanas(semanas);
	}

	public Juvenil(int pid, List<String> valores, boolean primero, String nombre) {
		setPid(pid);
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
			setOriginal(new Juvenil(pid, sublista, false, getNombre()));
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
		return getClase_cambio(nivel, getOriginal() == null ? null : ((Juvenil)getOriginal()).nivel, null);
	}
	
	public String getTitle_nivel() {
		return getOriginal() == null || ((Juvenil)getOriginal()).nivel == nivel ? "" : new DecimalFormat("+#;-#").format(nivel - ((Juvenil)getOriginal()).nivel);
	}

	public String getEdad_proyectada() {
		int anyos = ((getJornada() % 16) + getSemanas()) / 16;
		String clase;
		switch (getEdad()) {
			case 16:
			case 17: clase = "negrita";	break;
			default: clase = "";
		}
		return "<span class='" + clase + "'>" + getEdad() + "</span>" + (anyos > 0 ? " <span style='color: gray'>(+" + anyos + ")</span>" : "");
	}

	public void calcular_niveles() {
		Integer anterior = null;
		BigDecimal max_anterior = null;
		BigDecimal min_anterior = null;
		List<Juvenil> inverso = new ArrayList<Juvenil>();

		Juvenil j = this;
		// Primero acoto los valores en una dirección
		do {
			inverso.add(j);
			j.nivel_max = new BigDecimal(j.nivel + 3).min(max_anterior == null ? new BigDecimal(18) : max_anterior);
			j.nivel_min = new BigDecimal(j.nivel - 2).max(Util.bdnvl(min_anterior).subtract(un_tercio)).max(BigDecimal.ZERO);
			
			Integer siguiente = j.getOriginal() == null ? null : ((Juvenil)j.getOriginal()).getNivel();
			// Máximos y mínimos
			if (anterior != null && siguiente != null) {
				if (anterior < j.nivel && siguiente < j.nivel) {
					j.nivel_max = j.nivel_max.min(new BigDecimal(j.nivel + 1));
				} else if (anterior > j.nivel && siguiente > j.nivel) {
					j.nivel_min = j.nivel_min.max(new BigDecimal(j.nivel));
				} else if (siguiente == j.nivel && j.getOriginal().getOriginal() != null) {
					Integer siguiente2 = ((Juvenil)j.getOriginal().getOriginal()).getNivel();
					if (anterior < j.nivel && siguiente2 < j.nivel) {
						j.nivel_max = j.nivel_max.min(new BigDecimal(j.nivel + 1).add(un_tercio));
					} else if (anterior > j.nivel && siguiente2 > j.nivel) {
						j.nivel_min = j.nivel_min.max(new BigDecimal(j.nivel));
					} else if (siguiente2 == j.nivel && j.getOriginal().getOriginal().getOriginal() != null) {
						Integer siguiente3 = ((Juvenil)j.getOriginal().getOriginal().getOriginal()).getNivel();
						if (anterior < j.nivel && siguiente3 < j.nivel) {
							j.nivel_max = j.nivel_max.min(new BigDecimal(j.nivel + 1).add(un_tercio.multiply(new BigDecimal(2))));
						} else if (anterior > j.nivel && siguiente3 > j.nivel) {
							j.nivel_min = j.nivel_min.max(new BigDecimal(j.nivel));
						} else if (siguiente3 == j.nivel && j.getOriginal().getOriginal().getOriginal().getOriginal() != null) {
							Integer siguiente4 = ((Juvenil)j.getOriginal().getOriginal().getOriginal().getOriginal()).getNivel();
							if (anterior < j.nivel && siguiente4 < j.nivel) {
								j.nivel_max = j.nivel_max.min(new BigDecimal(j.nivel + 1).add(un_tercio.multiply(new BigDecimal(3))));
							} else if (anterior > j.nivel && siguiente4 > j.nivel) {
								j.nivel_min = j.nivel_min.max(new BigDecimal(j.nivel));
							}
						}
					}
				}
			}

			max_anterior = j.nivel_max;
			min_anterior = j.nivel_min;
			anterior = j.nivel;
			j = (Juvenil)j.getOriginal();
		} while (j != null);
		
		// Y luego en la dirección inversa
		Collections.reverse(inverso);
		max_anterior = null;
		min_anterior = null;
		for (Juvenil juv : inverso) {
			if (max_anterior != null) {
				juv.nivel_max = juv.nivel_max.min(max_anterior.add(un_tercio));
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
		int[] x = new int[inverso.size()];
		int[] y = new int[inverso.size()];
		int i = 0;
		for (Juvenil juv : inverso) {
			x[i] = i + 1;	// Suno 1 porque el resultado aparece desplazado una unidad, no sé por qué :?
			y[i] = juv.nivel;
			i++;
		}
		
//		double[] resultado1 = AsistenteBO.calcRectaRegresionYsobreX(x, y);
//		m = (float)resultado1[0];
//		n = (float)resultado1[1];
//
		// x = m * y + n
		// y = x/m - n/m
//		double[] resultado2 = AsistenteBO.calcRectaRegresionXsobreY(x, y);
//		m2 = (float)(1f/resultado2[0]);
//		n2 = (float)(-resultado2[1]/resultado2[0]);
//
//		// Calculo la media
//		m = (m + m2) / 2f;
//		n = (n + n2) / 2f;

		// Parece equivalente a calcRectaRegresionYsobreX
		float[] resultado = AsistenteBO.regresion_lineal(inverso);
		m = resultado[0];
		n = resultado[1];
	}

	public String getDatos_grafica_nivel() {
		return getDatos_grafica(new Funcion[] {new Funcion() {
			@Override
			public Float get(Jugador j, int x) {
				return m * x + n + 0.5f;
			}
			@Override
			protected Float get(Jugador j) {
				return null;
			}
		}, new Funcion() {
			@Override
			public Float get(Jugador j) {
				return ((Juvenil)j).getNivel_max().floatValue();
			}
		}, new Funcion() {
			@Override
			public Float get(Jugador j) {
				return ((Juvenil)j).getNivel_min().floatValue();
			}
		}, new Funcion() {
			@Override
			public Float get(Jugador j) {
				return ((Juvenil)j).nivel + 0.5f;
			}
		}}, null, null, "['pink', 'orange', 'orange', 'blue']");
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
