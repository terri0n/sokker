package com.formulamanager.sokker.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;

public class Jugador implements Comparable<Jugador> {
	// Bloque de entrenamiento
	public class Entrenamiento {
		public int valor_habilidad;
		public boolean abierto_izq;
		public boolean abierto_der;
		public List<Float> puntos_entrenamiento;
		public List<Jugador> jornadas;
		
		public Entrenamiento(int valor_habilidad) {
			this.valor_habilidad = valor_habilidad;
			abierto_izq = true;
			abierto_der = true;
			puntos_entrenamiento = new ArrayList<Float>();
			jornadas = new ArrayList<Jugador>();
		}
	}
	
	protected abstract class Funcion {
		public Float get(Jugador j, int x) {
			return get(j);
		}
		protected abstract Float get(Jugador j);
	}
	
	public static String puntos_condicion[] = {"0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "1.0", "1.2", "1.5"};
	public static String puntos_rapidez[] = {"0.0", "1.4", "1.7", "1.9", "2.2", "2.4", "2.6", "3.0", "3.4", "3.7", "4.1", "4.6", "5.0", "5.5", "6.0", "7.2", "8.4", "9.6", "11.5"};
	public static String puntos_otros[] = {"0.0", "1.2", "1.4", "1.6", "1.8", "2.0", "2.2", "2.5", "2.8", "3.1", "3.4", "3.8", "4.2", "4.6", "5.0", "6.0", "7.0", "8.0", "9.5"};
	public static String valores_m[] = {"trágico", "terrible", "deficiente", "pobre", "débil", "regular", "aceptable", "bueno", "sólido", "muy bueno", "excelente", "formidable", "destacado", "increíble", "brillante", "mágico", "sobrenatural", "divino", "superdivino", "" };
	public static String valores_f[] = {"trágica", "terrible", "deficiente", "pobre", "débil", "regular", "aceptable", "buena", "sólida", "muy buena", "excelente", "formidable", "destacada", "increíble", "brillante", "mágica", "sobrenatural", "divina", "superdivina" };

	public static enum DEMARCACION { GK, DEF, MID, ATT };
	public static enum DEMARCACION_ASISTENTE { GK, DEF, MID, ATT, OTRA };	// Los índices comunes deben coincidir para compatibilizarlo con Sokker -> AsistenteBO#obtener_entrenamiento()
	public static float BASE_TALENTO = 1.094f;
	public static float FACTOR_TALENTO = 45f;
	public static float FACTOR_RAP_ANO = 3.75f;
			
	private String nombre;
	private Integer pid;
	private String equipo;
	private Integer tid;
	private Integer edad;
	private Integer valor;
	private Integer jornada;

	private DEMARCACION_ASISTENTE demarcacion;				// La seleccionada por el usuario
	
	private Integer condicion;
	private Integer rapidez;
	private Integer tecnica;
	private Integer pases;
	private Integer porteria;
	private Integer defensa;
	private Integer creacion;
	private Integer anotacion;

	private Integer forma;							// Media para entrenadores
	private Integer pais;
	private Date fecha;
	private Integer tarjetas;
	private Integer nt;
	private Integer lesion;
	private Integer en_venta;
	private Integer minutos;
	private DEMARCACION demarcacion_entrenamiento;	// La del partido jugado
	private String notas;
	
	private BigDecimal puntos = BigDecimal.ZERO;
	private String icono;
	private String icono_foro;
	private Integer edicion;

	private Jugador original;

	private boolean actualizado;					// "Fiable" para el asistente
	private Float talento_min;
	private Float talento_max;
	
	// NTDB
	private Integer disciplina_tactica;
	private Integer valor_original;
	
	private HashMap<String, List<Entrenamiento>> total_entrenamientos = new HashMap<String, List<Entrenamiento>>();
	
	//-----------
	// Funciones
	//-----------

	public Jugador () {
		condicion = 0;
		rapidez = 0;
		tecnica = 0;
		pases = 0;
		porteria = 0;
		defensa = 0;
		creacion = 0;
		anotacion = 0;
		
		edad = 100;
	}

	public Jugador (Jugador j) {
		condicion = j.condicion;
		rapidez = j.rapidez;
		tecnica = j.tecnica;
		pases = j.pases;
		porteria = j.porteria;
		defensa = j.defensa;
		creacion = j.creacion;
		anotacion = j.anotacion;
		valor = j.valor;
		jornada = j.jornada;

		// Asistente
		nombre = j.nombre;
		pid = j.pid;
		tid = j.tid;
		fecha = j.fecha;
		edad = j.edad;
		pais = j.pais;
	}

	public Jugador(Integer pid, String nombre, Integer edad, Integer valor, Integer tid) {
		this.pid = pid;
		this.nombre = nombre;
		this.edad = edad;
		this.valor = valor;
		this.tid = tid;
	}

	/**
	 * @param pid
	 * @param valores
	 * @param primero Indica si es el primer jugador del listado. Los originales no tendrán la cabecera
	 * @param nombre Nombre del 1er jugador, para que se vaya propagando
	 */
	public Jugador(Integer pid, List<String> valores, boolean primero, String nombre) {
		this.pid = pid;
		int i = 0;

		if (primero) {
			this.nombre = valores.get(i++);
			tid = Integer.valueOf(valores.get(i++));
			demarcacion = valores.get(i++).equals("") ? null : DEMARCACION_ASISTENTE.valueOf(valores.get(i-1));
			pais = Integer.valueOf(valores.get(i++));
			try {
				fecha = Util.stringToDateTime(valores.get(i++));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			actualizado = Boolean.valueOf(valores.get(i++));

			tarjetas = valores.get(i++).equals("") ? 0 : Integer.valueOf(valores.get(i-1));
			nt = valores.get(i++).equals("") ? null : Integer.valueOf(valores.get(i-1));
			lesion = valores.get(i++).equals("") ? null : Integer.valueOf(valores.get(i-1));
			en_venta = Integer.valueOf(valores.get(i++));
			notas = Util.nnvl(valores.get(i++));
		} else {
			this.nombre = nombre;
		}

		jornada = Integer.valueOf(valores.get(i++));
		edad = Integer.valueOf(valores.get(i++));
		valor = Util.stringToInteger(valores.get(i++));

		condicion = Util.stringToInteger(valores.get(i++));
		rapidez = Util.stringToInteger(valores.get(i++));
		tecnica = Util.stringToInteger(valores.get(i++));
		pases = Util.stringToInteger(valores.get(i++));
		porteria = Util.stringToInteger(valores.get(i++));
		defensa = Util.stringToInteger(valores.get(i++));
		creacion = Util.stringToInteger(valores.get(i++));
		anotacion = Util.stringToInteger(valores.get(i++));

		// Para que funcione en jugadores con el formato sin lesión y con lesión
		String aux = Util.nvl(valores.get(i++));
		if (aux.length() > 0 && aux.charAt(0) == '-') {
			lesion = Math.abs(new Integer(aux));
			forma = Util.stringToInteger(valores.get(i++));
		} else {
			forma = aux.length() == 0 ? null : new Integer(aux);
		}
		demarcacion_entrenamiento = valores.get(i++).equals("") ? null : DEMARCACION.valueOf(valores.get(i-1));
		minutos = Integer.valueOf(valores.get(i++));

		if (valores.size() > i) {
			List<String> sublista = valores.subList(i, valores.size());
			original = new Jugador(pid, sublista, false, this.nombre);
		}
	}

	/**
	 * Inicializar entrenador
	 * @param valores
	 */
	public Jugador(String valores) {
		String[] split = valores.split(";");
		int i = 0;
		condicion = Util.stringToInteger(split[i++]);
		rapidez = Util.stringToInteger(split[i++]);
		tecnica = Util.stringToInteger(split[i++]);
		pases = Util.stringToInteger(split[i++]);
		porteria = Util.stringToInteger(split[i++]);
		defensa = Util.stringToInteger(split[i++]);
		creacion = Util.stringToInteger(split[i++]);
		anotacion = Util.stringToInteger(split[i++]);
	}

	
	@Override
	public boolean equals(Object j) {
		return j instanceof Jugador && ((Jugador)j).getPid().equals(pid);
	}
	
	public void calcular_puntos() {
		puntos = BigDecimal.ZERO;
		for (int i = original.condicion; i < condicion; i++) {
			puntos = puntos.add(new BigDecimal(puntos_condicion[i+1]));
		}
		for (int i = original.rapidez; i < rapidez; i++) {
			puntos = puntos.add(new BigDecimal(puntos_rapidez[i+1]));
		}
		for (int i = original.tecnica; i < tecnica; i++) {
			puntos = puntos.add(new BigDecimal(puntos_otros[i+1]));
		}
		for (int i = original.pases; i < pases; i++) {
			puntos = puntos.add(new BigDecimal(puntos_otros[i+1]));
		}
		for (int i = original.porteria; i < porteria; i++) {
			puntos = puntos.add(new BigDecimal(puntos_otros[i+1]));
		}
		for (int i = original.defensa; i < defensa; i++) {
			puntos = puntos.add(new BigDecimal(puntos_otros[i+1]));
		}
		for (int i = original.creacion; i < creacion; i++) {
			puntos = puntos.add(new BigDecimal(puntos_otros[i+1]));
		}
		for (int i = original.anotacion; i < anotacion; i++) {
			puntos = puntos.add(new BigDecimal(puntos_otros[i+1]));
		}
	}

	public Integer max_habilidad() {
		int max = condicion;
		max = Math.max(max, rapidez);
		max = Math.max(max, tecnica);
		max = Math.max(max, pases);
		max = Math.max(max, porteria);
		max = Math.max(max, defensa);
		max = Math.max(max, creacion);
		max = Math.max(max, anotacion);
		return max;
	}
	
	@Override
	public int compareTo(Jugador j) {
		if (puntos.compareTo(j.getPuntos()) != 0) {
			return -puntos.compareTo(j.getPuntos());
		} else if (original.jornada.compareTo(j.original.getJornada()) != 0) {
			return -original.jornada.compareTo(j.original.getJornada());
		} else if (edad.compareTo(j.getEdad()) != 0) {
			return edad.compareTo(j.getEdad());
		} else if (max_habilidad().compareTo(j.max_habilidad()) != 0) {
			return -max_habilidad().compareTo(j.max_habilidad());
		} else {
			return -valor.compareTo(j.getValor());
		}
	}

	public Integer getJornadaMod() {
		return jornada % 16;
	}

	public boolean validar(boolean senior) {
		if (senior && (edad < 21 || edad > 27)
				|| !senior && (edad < 18 || edad > 20)) {
			return false;
		}
		
		if (senior) {
			if (edad <= 22) {
				// GK
				if (porteria + rapidez + pases >= 27) {
					return true;
				}

				// DEF
				if (defensa + rapidez >= 22 && pases + tecnica + creacion >= 11) {
					return true;
				}

				// MID
				if (rapidez + creacion + pases + tecnica + defensa >= 39) {
					return true;
				}

				// ATT
				if (rapidez + tecnica + anotacion >= 29 && creacion + defensa >= 7) {
					return true;
				}
			} else if (edad <= 24) {
				// GK
				if (porteria + rapidez + pases >= 32) {
					return true;
				}

				// DEF
				if (defensa + rapidez >= 25 && pases + tecnica + creacion >= 16) {
					return true;
				}

				// MID
				if (rapidez + creacion + pases + tecnica + defensa >= 48) {
					return true;
				}

				// ATT
				if (rapidez + tecnica + anotacion >= 34 && creacion + defensa >= 11) {
					return true;
				}
			} else {
				// GK
				if (porteria >= 14 && rapidez >= 14 && pases >= 8) {
					return true;
				}

				// DEF
				if (defensa >= 14 && rapidez >= 14 && pases + tecnica + creacion >= 21) {
					return true;
				}

				// MID
				if (rapidez >= 14 && creacion >= 14 && pases >= 14 && tecnica + defensa >= 15) {
					return true;
				}

				// ATT
				if (rapidez >= 13 && tecnica >= 13 && anotacion >= 13 && creacion + defensa >= 15) {
					return true;
				}
			}
		} else {
			// --- JUNIOR --- //
			
			// GK
			if (porteria >= 10 + (edad-18)*2
					&& porteria + rapidez + pases >= 18 + (edad-18)*3) {
				return true;
			}

			// DEF
			if (defensa + rapidez >= 14 + (edad-18)*3
					&& creacion + tecnica + pases >= 6 + (edad-18)*2) {
				return true;
			}

			// MID
			int min = edad == 18 ? 25 : edad == 19 ? 30 : 36;
			if (creacion + rapidez + tecnica + pases >= min
					|| creacion + rapidez + tecnica + defensa >= min
					|| creacion + rapidez + defensa + pases >= min
					|| creacion + defensa + tecnica + pases >= min
					|| defensa + rapidez + tecnica + pases >= min) {
				return true;
			}

			// ATT
			if (anotacion + rapidez + tecnica >= 20 + (edad-18)*4) {
				return true;
			}
		}
		return false;
	}

	public static String desp(String lang, String name, Integer indice, boolean numeros) {
		return desp(lang, name, indice, numeros, 18);
	}
	
	public static String desp(String lang, String name, Integer indice, boolean numeros, Integer max) {
		String gender;
		try {
			gender = Util.getTexto(lang, "skills.gender_" + name);
		} catch (Exception e) {
			gender = "m";
		}
		
		String[] valores = new String[19];
		for (int i = 0 ; i <= 18; i++) {
			valores[i] = Util.getTexto(lang, "skills.skill" + i + gender) + (numeros ? " [" + i + "]" : "");
		}
		
		String cad = "<select name='" + name + "'>";
		int i = 0;
		for (String s : valores) {
			if (i <= max) {
				cad += "<option value='" + i + "' " + (i == indice ? "selected" : "") + ">" + s + "</option>";
				i++;
			}
		}
		cad += "</select>";
		
		return cad;
	}

	public static String despDemarcacion(DEMARCACION demarcacion) {
		return despDemarcacion(demarcacion.name(), DEMARCACION.values(), null);
	}

	public static String despDemarcacion_asistente(DEMARCACION_ASISTENTE demarcacion, Usuario usuario) {
		return despDemarcacion(demarcacion == null ? null : demarcacion.name(), DEMARCACION_ASISTENTE.values(), usuario);
	}

	public static String despDemarcacion(String demarcacion, Enum[] values, Usuario usuario) {
		try {
			String cad = "<select name='demarcacion'>";
			for (Enum d : values) {
				cad += "<option value='" + d.name() + "' " + (d.name().equals(demarcacion) ? "selected" : "") + ">";
				if (d == DEMARCACION_ASISTENTE.OTRA) {
					cad += Util.getTexto(usuario.getLocale(), "common.other_position");
				} else {
					cad += d.name();
				}
				cad +=  "</option>";
			}
			cad += "</select>";
			
			return cad;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public Integer getSubidas() {
		int subidas = 0;

		subidas += condicion - original.condicion;
		subidas += rapidez - original.rapidez;
		subidas += tecnica - original.tecnica;
		subidas += pases - original.pases;

		subidas += porteria - original.porteria;
		subidas += defensa - original.defensa;
		subidas += creacion - original.creacion;
		subidas += anotacion - original.anotacion;
		
		return subidas;
	}

	public Integer getDifValor() {
		return (valor == null || original.valor == null) ? 0 : valor - original.valor;
	}

	public static Comparator<? super Jugador> getComparator() {
		return new Comparator<Jugador>() {
			@Override
			public int compare(Jugador o1, Jugador o2) {
				if (o1.demarcacion == o2.demarcacion) {
					return -o1.valor.compareTo(o2.valor);
				} else if (o1.demarcacion == null) {
					return 1;
				} else if (o2.demarcacion == null) {
					return -1;
				} else {
					return o1.demarcacion.compareTo(o2.demarcacion);
				}
			}
		};
	}

	public static Comparator<Jugador> comparator_valor() {
		return new Comparator<Jugador>() {
			@Override
			public int compare(Jugador o1, Jugador o2) {
				return - o1.getDifValor().compareTo(o2.getDifValor());
			}
		};
	}

	public static Comparator<Jugador> comparator_subidas() {
		return new Comparator<Jugador>() {
			@Override
			public int compare(Jugador o1, Jugador o2) {
				return -o1.getSubidas().compareTo(o2.getSubidas());
			}
		};
	}

	public static Comparator<Jugador> comparator_puntos() {
		return new Comparator<Jugador>() {
			@Override
			public int compare(Jugador o1, Jugador o2) {
				return -o1.getPuntos().compareTo(o2.getPuntos());
			}
		};
	}
	
	public String serializar(boolean primero) {
		List<String> valores = new ArrayList<String>();
		if (primero) {
			valores.add(nombre);
			valores.add(tid+"");
			valores.add(demarcacion == null ? "" : demarcacion.name());
			valores.add(pais+"");
			valores.add(Util.dateTimeToString(fecha));
			valores.add(Util.nvl(actualizado));
			valores.add(Util.nvl(tarjetas));
			valores.add(Util.nvl(nt));
			valores.add(Util.nvl(lesion));
			valores.add(Util.nvl(en_venta));
			valores.add(Util.nvl(notas).replace(",", "."));
		}
		
		valores.add(jornada+"");
		valores.add(edad+"");
		valores.add(Util.nvl(Util.integerToString(valor)));
		
		valores.add(Util.nvl(Util.integerToString(condicion)));
		valores.add(Util.nvl(Util.integerToString(rapidez)));
		valores.add(Util.nvl(Util.integerToString(tecnica)));
		valores.add(Util.nvl(Util.integerToString(pases)));
		valores.add(Util.nvl(Util.integerToString(porteria)));
		valores.add(Util.nvl(Util.integerToString(defensa)));
		valores.add(Util.nvl(Util.integerToString(creacion)));
		valores.add(Util.nvl(Util.integerToString(anotacion)));
		
		valores.add("-" + Util.invl(lesion));
		valores.add(Util.nvl(Util.integerToString(forma)));
		valores.add(demarcacion_entrenamiento == null ? "" : demarcacion_entrenamiento.name());
		valores.add(Util.invl(minutos)+"");

		String serializacion_original = "";
		// Evito bucles infinitos
		if (original != null && !jornada.equals(original.getJornada())) {
			serializacion_original = "," + original.serializar(false);
		}
		
		return String.join(",", valores) + serializacion_original;
	}
	
	public String serializar_entrenador() {
		List<String> valores = new ArrayList<String>();
		
		valores.add(Util.nvl(Util.integerToString(condicion)));
		valores.add(Util.nvl(Util.integerToString(rapidez)));
		valores.add(Util.nvl(Util.integerToString(tecnica)));
		valores.add(Util.nvl(Util.integerToString(pases)));
		valores.add(Util.nvl(Util.integerToString(porteria)));
		valores.add(Util.nvl(Util.integerToString(defensa)));
		valores.add(Util.nvl(Util.integerToString(creacion)));
		valores.add(Util.nvl(Util.integerToString(anotacion)));
		
		return String.join(";", valores);
	}
	
	public String getClase_cambio(Integer valor, Integer original, String skill) {
		return (demarcacion == null || skill == null ? "" : " " + demarcacion.name() + "_" + skill)
				+ (valor == null || original == null || valor.equals(original) ? "" : valor.compareTo(original) < 0 ? " cambio_bajada" : " cambio_subida");
	}

	public String getClase_minimo(Integer valor, Integer original, String skill) {
		return (valor == null || valor <= 5 ? " gris" : valor >= 8 ? " negrita" : "")
				+ getClase_cambio(valor, original, skill);
	}

	public String getClase(Integer valor, Integer original, String skill) {
		return (demarcacion == null || skill == null ? "" : " " + demarcacion.name() + "_" + skill)
				+ (valor == null || original == null || valor.equals(original) ? "" : valor.compareTo(original) < 0 ? " bajada" : " subida");
	}

	public String getClase_valor() {
		return getClase(valor, original == null ? null : original.valor, null);
	}

	public String getClase_forma() {
		return getClase(forma, original == null ? null : original.forma, null);
	}

	public String getClase_condicion() {
		return getClase_minimo(condicion, original == null ? null : original.condicion, "CON");
	}

	public String getClase_rapidez() {
		return getClase_minimo(rapidez, original == null ? null : original.rapidez, "RAP");
	}

	public String getClase_tecnica() {
		return getClase_minimo(tecnica, original == null ? null : original.tecnica, "TEC");
	}

	public String getClase_pases() {
		return getClase_minimo(pases, original == null ? null : original.pases, "PAS");
	}

	public String getClase_porteria() {
		return getClase_minimo(porteria, original == null ? null : original.porteria, "POR");
	}

	public String getClase_defensa() {
		return getClase_minimo(defensa, original == null ? null : original.defensa, "DEF");
	}

	public String getClase_creacion() {
		return getClase_minimo(creacion, original == null ? null : original.creacion, "CRE");
	}

	public String getClase_anotacion() {
		return getClase_minimo(anotacion, original == null ? null : original.anotacion, "ANO");
	}
	
	public String getTitle_forma() {
		return original == null || original.forma == null || original.forma.equals(forma) ? "" : new DecimalFormat("+#;-#").format(forma - original.forma);
	}
	
	public String getTitle_valor() {
		return original == null || original.valor == null || original.valor.equals(valor) ? "" : new DecimalFormat("+#,###;-#,###").format(valor - original.valor);
	}

	public String getTitle_condicion() {
		return original == null || original.condicion == null || original.condicion.equals(condicion) ? "" : new DecimalFormat("+#;-#").format(condicion - original.condicion);
	}
	
	public void copiar_valores_publicos (Jugador j) {
		if (j != null) {
			jornada = j.jornada;
			valor = j.valor;
			forma = j.forma;
			tarjetas = j.tarjetas;
			nt = j.nt;
			lesion = j.lesion;
			en_venta = j.en_venta;
			edad = j.edad;
		}
	}
	
	public Integer getValor_habilidad(TIPO_ENTRENAMIENTO habilidad) {
		switch(habilidad) {
			case Condicion:	
				return condicion;
			case Rapidez:
				return rapidez;
			case Tecnica:
				return tecnica;
			case Pases:
				return pases;
			case Porteria:
				return porteria;
			case Defensa:
				return defensa;
			case Creacion:
				return creacion;
			case Anotacion:
				return anotacion;
		}
		return null;
	}

	public void setValor_habilidad(int nivel, TIPO_ENTRENAMIENTO habilidad) {
		switch(habilidad) {
			case Condicion:	
				condicion = nivel;
				break;
			case Rapidez:
				rapidez = nivel;
				break;
			case Tecnica:
				tecnica = nivel;
				break;
			case Pases:
				pases = nivel;
				break;
			case Porteria:
				porteria = nivel;
				break;
			case Defensa:
				defensa = nivel;
				break;
			case Creacion:
				creacion = nivel;
				break;
			case Anotacion:
				anotacion = nivel;
				break;
			default:
				throw new RuntimeException("Valor de habilidad incorrecto");
		}
	}

	public static float getFactor_entrenamiento_directo(Usuario usuario, Integer jornada, TIPO_ENTRENAMIENTO habilidad) {
		Jugador entrenador = usuario.getEntrenador_principal().getOrDefault(jornada, usuario.getEntrenador_principal().get(usuario.getDef_jornada()));
		int nivel_entrenador = entrenador == null ? 16 : entrenador.getValor_habilidad(habilidad);
		return Math.max(nivel_entrenador - 6, 0) / 10f;
	}
	
	public static BigDecimal getNivel_asistentes(Usuario usuario, Integer jornada) {
		BigDecimal nivel_asistentes = usuario.getNivel_asistentes().getOrDefault(jornada, usuario.getNivel_asistentes().get(usuario.getDef_jornada()));
		return nivel_asistentes == null ? new BigDecimal(16) : nivel_asistentes;
	}
	
	public static Comparator<Jugador> getComparator_nombre() {
		return new Comparator<Jugador>() {
			@Override
			public int compare(Jugador j1, Jugador j2) {
				if (j1 != null && j2 != null) {
					return j1.getNombre().compareTo(j2.getNombre());
				} else if (j1 == null) {
					return 1;
				} else {
					return -1;
				}
			}
		};
	}

	public float getMinutos_entrenamiento(Usuario usuario, TIPO_ENTRENAMIENTO habilidad) {
		BigDecimal nivel_asistentes = getNivel_asistentes(usuario, jornada);
		float factor_entrenamiento_directo = getFactor_entrenamiento_directo(usuario, jornada, habilidad);
		float factor_entrenamiento_residual = (factor_entrenamiento_directo + nivel_asistentes.subtract(new BigDecimal("6.5")).max(BigDecimal.ZERO).divide(BigDecimal.TEN).floatValue()) / 2f;

		float minutos = Math.min(this.minutos == null ? 0 : this.minutos, 100);
		float directo = minutos * factor_entrenamiento_directo;
		float residual = minutos * factor_entrenamiento_residual / 9f;	// Máximo: 11,11...

		if (demarcacion_entrenamiento == null && habilidad != TIPO_ENTRENAMIENTO.Condicion) {
			// Si no ha jugado ni se pide la condición, no devuelvo nada
			return 0f;
		} else {
			if (usuario.getTipo_entrenamiento().get(jornada) == habilidad) {
				// Directo
				switch (habilidad) {
					case Condicion: 
						return 100f * factor_entrenamiento_directo;
					case Rapidez:
						return directo;
					case Anotacion:
						switch (demarcacion_entrenamiento) {
							case ATT:
								return directo;
							case MID:
								return directo / 2f;
							default:
								return residual;
						}
					case Pases:
					case Creacion:
						if (usuario.getDemarcacion().get(jornada) == DEMARCACION.MID) {
							if (demarcacion_entrenamiento == DEMARCACION.MID) {
								return directo;
							} else {
								return residual;
							}
						} else {
							if (usuario.getDemarcacion().get(jornada) == demarcacion_entrenamiento || demarcacion_entrenamiento == DEMARCACION.MID) {
								return directo / 2f;			// A ojo: mejora la seleccionada y empeora MID
							} else {
								return residual;
							}
						}
					case Defensa:
					case Tecnica:
						if (usuario.getDemarcacion().get(jornada) == demarcacion_entrenamiento) {
							return directo;
						} else {
							return residual;
						}
					case Porteria:
						if (demarcacion_entrenamiento == DEMARCACION.GK) {
							return directo;
						} else {
							return 0f;
						}
					default:
						throw new RuntimeException("Entrenamiento incorrecto: " + habilidad);
				}
			} else {
				// Residual
				switch (habilidad) {
					case Condicion: 
						return 100f * factor_entrenamiento_residual / 9f - 5f;	// Residual para condición menos lo que baja de media por jornada
					case Porteria:
						if (demarcacion_entrenamiento == DEMARCACION.GK) {
							return residual;
						} else {
							return 0f;
						}
					default: 
						return residual;
				}
			}
		}
	}
	
	public String getEntrenamientoExtra(Usuario usuario) {
		return getEntrenamientoExtraNivel(usuario, TIPO_ENTRENAMIENTO.Condicion, null) / 80f
				+ "," +
				getEntrenamientoExtraNivel(usuario, TIPO_ENTRENAMIENTO.Rapidez, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(usuario, TIPO_ENTRENAMIENTO.Tecnica, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(usuario, TIPO_ENTRENAMIENTO.Pases, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(usuario, TIPO_ENTRENAMIENTO.Porteria, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(usuario, TIPO_ENTRENAMIENTO.Defensa, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(usuario, TIPO_ENTRENAMIENTO.Creacion, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(usuario, TIPO_ENTRENAMIENTO.Anotacion, null) / 100f;
	}
	
	public float getEntrenamientoExtraNivel(Usuario usuario, TIPO_ENTRENAMIENTO habilidad, Integer nivel) {
		Integer nivel_actual = getValor_habilidad(habilidad);
		
		if (nivel == null || nivel.equals(nivel_actual)) {
			float puntos_entrenamiento = lesion != null && lesion > 7 ? 0 : getMinutos_entrenamiento(usuario, habilidad);
			puntos_entrenamiento = puntos_entrenamiento * (float)Math.pow(BASE_TALENTO, this.edad - 16);

			return puntos_entrenamiento + (original == null ? 0f : original.getEntrenamientoExtraNivel(usuario, habilidad, nivel_actual));
		} else {
			return 0f;
		}
	}
	
	public List<Entrenamiento> getEntrenamientosTemporada(Usuario usuario, TIPO_ENTRENAMIENTO habilidad, int edad, Entrenamiento entrenamiento_act) {
		List<Entrenamiento> entrenamientos;

		if (edad == this.edad) {
			entrenamientos = new ArrayList<Entrenamiento>();
			float puntos_entrenamiento = lesion != null && lesion > 7 ? 0 : getMinutos_entrenamiento(usuario, habilidad);
			int valor = getValor_habilidad(habilidad);
			
			if (entrenamiento_act.valor_habilidad == valor) {
				entrenamiento_act.jornadas.add(this);
				entrenamiento_act.puntos_entrenamiento.add(puntos_entrenamiento);
			} else {
				entrenamiento_act.abierto_izq = false;
				if (entrenamiento_act.jornadas.size() > 0) {
					entrenamientos.add(entrenamiento_act);
				}
				
				entrenamiento_act = new Entrenamiento(valor);
				entrenamiento_act.abierto_der = false;
				entrenamiento_act.jornadas.add(this);
				entrenamiento_act.puntos_entrenamiento.add(puntos_entrenamiento);
			}
			
			if (original != null) {
				entrenamientos.addAll(original.getEntrenamientosTemporada(usuario, habilidad, edad, entrenamiento_act));
			} else if (entrenamiento_act.jornadas.size() > 0) {
				entrenamientos.add(entrenamiento_act);				
			}
		} else if (edad < this.edad) {
			if (original != null) {
				entrenamiento_act.valor_habilidad = getValor_habilidad(habilidad);
				entrenamientos = original.getEntrenamientosTemporada(usuario, habilidad, edad, entrenamiento_act);
			} else {
				entrenamientos = new ArrayList<Entrenamiento>();
				if (entrenamiento_act.jornadas.size() > 0) {
					entrenamientos.add(entrenamiento_act);
				}
			}
		} else {
			entrenamientos = new ArrayList<Entrenamiento>();
			if (entrenamiento_act.jornadas.size() > 0) {
				entrenamientos.add(entrenamiento_act);
			}
		}
		
		return entrenamientos;
	}

	public int getMin_edad() {
		if (original != null) {
			return original.getMin_edad();
		} else {
			return edad;
		}
	}
	
	public float puntos_pretemporada(HashMap<String, List<Entrenamiento>> total_entrenamientos, TIPO_ENTRENAMIENTO habilidad, int edad) {
		List<Entrenamiento> entrenamientos = total_entrenamientos.get(edad + "_" + habilidad.name());
		List<Entrenamiento> entrenamientos_pretemporada = total_entrenamientos.get((edad - 1) + "_" + habilidad.name());
		
		// Si el 1er entrenamiento está abierto por la izquierda y la temporada anterior tenía entrenamientos
		if (!entrenamientos.get(0).abierto_izq || entrenamientos_pretemporada == null) {
			return 0f;
		} else {
			float acumulado;
			// Si solo hay un entrenamiento y está abierto por la izquierda
			if (entrenamientos_pretemporada.size() == 1 && entrenamientos_pretemporada.get(0).abierto_izq) {
				acumulado = puntos_pretemporada(total_entrenamientos, habilidad, edad - 1);
			} else {
				acumulado = 0f;
			}
			
			return acumulado + Util.sumar(entrenamientos_pretemporada.get(entrenamientos_pretemporada.size() - 1).puntos_entrenamiento) * (habilidad == TIPO_ENTRENAMIENTO.Condicion ? 1f : BASE_TALENTO);
		}
	}
	
	public String getEntrenamiento(Usuario usuario, TIPO_ENTRENAMIENTO habilidad, int edad) {
		String salida = "";
		List<Entrenamiento> entrenamientos = total_entrenamientos.get(edad + "_" + habilidad.name());
		String gender = Util.getTexto(usuario.getLocale(), "skills.gender_" + habilidad.getIngles());
		
		if (entrenamientos != null) {
			// Puntos acumulados en la habilidad de la temporada anterior
			float puntos_pretemporada = puntos_pretemporada(total_entrenamientos, habilidad, edad);

			for (Entrenamiento entrenamiento : entrenamientos) {
				try {
					String valor = Util.getTexto(usuario.getLocale(), "skills.skill" + entrenamiento.valor_habilidad + gender);
					String salida_entrenamiento = "<div>" + valor + "<br/><div class='" + (entrenamiento.abierto_izq ? "abierto_izq" : "") + " " + (entrenamiento.abierto_der ? "abierto_der" : "") + "'>";
					float exponente = edad - 16 + entrenamiento.valor_habilidad - (habilidad == TIPO_ENTRENAMIENTO.Rapidez || habilidad == TIPO_ENTRENAMIENTO.Anotacion ? FACTOR_RAP_ANO : 6);
					int prediccion = habilidad == TIPO_ENTRENAMIENTO.Condicion ? 80 : (int) (((talento_min + talento_max) / 2f) * Math.pow(BASE_TALENTO, exponente) * FACTOR_TALENTO);

					for (int i = entrenamiento.jornadas.size() - 1; i >= 0; i--) {
						Jugador j = entrenamiento.jornadas.get(i);
						Float f = entrenamiento.puntos_entrenamiento.get(i);
						salida_entrenamiento += "<span onclick='entrenamiento_click($(this), \"" + nombre + "\", " + j.minutos + ", " + j.lesion + ", " + j.jornada + ", \"" + (j.demarcacion_entrenamiento == null ? "" : j.demarcacion_entrenamiento.name()) + "\", \"" + usuario.getTipo_entrenamiento().get(j.jornada).name() + "\", \"" + usuario.getDemarcacion().get(j.jornada).name() + "\", " + pid + ", " + entrenamiento.valor_habilidad + ", " + habilidad.ordinal() + ")' ";
						if (f == 0f) {
							salida_entrenamiento += "class='perdido' title='0'>&nbsp;";
						} else if (f <= 15f) {
							salida_entrenamiento += "class='residual' title='" + f.intValue() + "'>&nbsp;";
						} else if (f <= 50f) {
							salida_entrenamiento += "class='medio' title='" + f.intValue() + "' style='width: " + (f/10f + 10f) + "px'>" + f.intValue();
						} else {
							salida_entrenamiento += "class='normal' title='" + f.intValue() + "' style='width: " + (f/10f + 10f) + "px'>" + f.intValue();
						}
						salida_entrenamiento += "</span>";
					}
					salida_entrenamiento += "</div><br/>(" + (int)(Util.sumar(entrenamiento.puntos_entrenamiento) + puntos_pretemporada) + " / " + prediccion + ")</div>";
					puntos_pretemporada = 0f;
					
					salida += salida_entrenamiento;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return salida;
	}

	// Añade jugadores recursivamente hasta rellenar las jornadas faltantes entre la actual y la del original
	public void anyadir_original(Jugador j) {
		if (jornada - j.getJornada() > 1) {
			// Copiamos los valores del anterior, ya que no sabemos si han variado
			original = new Jugador(j);
			original.setJornada(jornada - 1);
			original.setDemarcacion_entrenamiento(j.getDemarcacion_entrenamiento());
			
			// Al pasar de la jornada 0 a la 15 resto un año a la edad
			original.setEdad(edad - (jornada % 16 == 15 ? 1 : 0));

			original.anyadir_original(j);
		} else {
			original = j;
		}
	}

	public Jugador buscar_jornada(int jornada) {
		if (this.jornada == jornada) {
			return this;
		} else if (this.jornada < jornada || original == null) {
			return null;
		} else {
			return original.buscar_jornada(jornada);
		}
	}

	// Devuelve el nivel medio (entrenador)
	// Calculo una cota superior y una inferior y devuelvo la media
	// El máximo nivel que devuelva será 16.5
	public BigDecimal getNivel_entrenador() {
		BigDecimal nivel = new BigDecimal(forma);
		BigDecimal media = new BigDecimal(condicion + rapidez + tecnica + pases + porteria + defensa + creacion + anotacion).divide(new BigDecimal(8));
		BigDecimal cota_inferior = media.max(nivel);
		BigDecimal cota_superior = media.add(BigDecimal.ONE).min(nivel.add(BigDecimal.ONE));
		
		return cota_superior.add(cota_inferior).divide(new BigDecimal(2));
	}

	public void calcular_entrenamiento(Usuario usuario) {
		for (TIPO_ENTRENAMIENTO habilidad : TIPO_ENTRENAMIENTO.values()) {
			Integer valor = getValor_habilidad(habilidad);
	
			if (valor != null && original != null) {
				for (int edad = getMin_edad(); edad <= this.edad; edad++) {
					// Obtenemos los entrenamientos de la temporada
					Entrenamiento entrenamiento_act = new Entrenamiento(getValor_habilidad(habilidad));
					List<Entrenamiento> entrenamientos = original.getEntrenamientosTemporada(usuario, habilidad, edad, entrenamiento_act);
					Collections.reverse(entrenamientos);
					
					if (entrenamientos.size() > 0) {
						// Comprobamos si se cerró el último entrenamiento de la temporada anterior
						total_entrenamientos.put(edad + "_" + habilidad, entrenamientos);

						List<Entrenamiento> entrenamientos_pretemporada = total_entrenamientos.get((edad - 1) + "_" + habilidad);
						if (entrenamientos_pretemporada != null && !entrenamientos_pretemporada.get(entrenamientos_pretemporada.size() - 1).abierto_der) {
							entrenamientos.get(0).abierto_izq = false;
						}
					}
				}
			}
		}
	}

	public void calcular_talento() {
		talento_min = 0.0f;
		talento_max = 7.0f;

		for (TIPO_ENTRENAMIENTO habilidad : TIPO_ENTRENAMIENTO.values()) {
			if (habilidad != TIPO_ENTRENAMIENTO.Condicion) {
				// Número de subidas que llevamos para ir dividiendo el talento acumulado entre ellas
				// Empezamos por cero (desde el principio), y luego nos vamos saltando subidas para hacer todas las combinaciones posibles
				for (int subida_inicial = 0; subida_inicial >= (getMin_edad() - this.edad); subida_inicial--) {
					int num_subidas = subida_inicial;
					float talento_min_acumulado = 0f;
					float talento_max_acumulado = 0f;
					int valor_anterior = -1;
					
					for (int edad = getMin_edad(); edad <= this.edad; edad++) {
						List<Entrenamiento> entrenamientos = total_entrenamientos.get(edad + "_" + habilidad);
						if (entrenamientos != null) {
							for (Entrenamiento e : entrenamientos) {
								// Si la habilidad no ha bajado de valor
								if (e.valor_habilidad >= valor_anterior) {
									float exponente = edad - 16 + e.valor_habilidad - (habilidad == TIPO_ENTRENAMIENTO.Rapidez || habilidad == TIPO_ENTRENAMIENTO.Anotacion ? FACTOR_RAP_ANO : 6);
									float talento = Util.sumar(e.puntos_entrenamiento) * (float) Math.pow(BASE_TALENTO, -exponente) / FACTOR_TALENTO;	// 180 / 45 = 4
									float resta = e.puntos_entrenamiento.get(0) * (float) Math.pow(BASE_TALENTO, -exponente) / FACTOR_TALENTO;
		
									if (num_subidas == 0 && !e.abierto_der) {
										// Si el 1er entrenamiento está cerrado por la derecha, restamos el último entrenamiento para comnprobar el talento mínimo
										if (talento - resta > talento_min) {
											talento_min = talento - resta;
										}
										
										talento_min_acumulado = 0f;
										talento_max_acumulado = e.puntos_entrenamiento.get(0) * (float) Math.pow(BASE_TALENTO, 1 - exponente) / FACTOR_TALENTO;
									} else if (num_subidas > 0) {
										talento_min_acumulado += talento;
										talento_max_acumulado += talento;
		
										if (!e.abierto_der) {
											talento_min_acumulado -= resta;
										}
		
										if (talento_min_acumulado / num_subidas > talento_min) {
											talento_min = talento_min_acumulado / num_subidas;
										}
										if (!e.abierto_der && talento_max_acumulado / num_subidas < talento_max) {
											talento_max = talento_max_acumulado / num_subidas;
										}
		
										if (!e.abierto_der) {
											talento_min_acumulado += resta;
										}
									} else if (num_subidas == 0) {
										// Sin subidas, sin cerrar por la derecha
										if (talento > talento_min) {
											talento_min = talento;
										}
									}
									
									if (!e.abierto_der) {
										num_subidas++;
									}
								}
								
								valor_anterior = e.valor_habilidad;
							}
						}
					}
				}
			}
		}
	}

	public int getNum_jornadas() {
		return original == null ? 1 : original.getNum_jornadas() + 1;
	}
	
	public String getDatos_grafica_valor(Integer countryID) {
		return valor == null ? "null" : getDatos_grafica(new Funcion[] {new Funcion() {
			@Override
			public Float get(Jugador j) {
				BigDecimal val = j.getValor_pais(countryID);
				return val == null || j.forma == null ? null : val.divide(new BigDecimal(0.55F + (j.forma/18F) * 0.45F), RoundingMode.FLOOR).floatValue();
			}
		},new Funcion() {
			@Override
			public Float get(Jugador j) {
				BigDecimal val = j.getValor_pais(countryID);
				return val == null ? null : val.floatValue();
			}
		}}, null, null, null);
	}

	public String getDatos_grafica_forma() {
		return forma == null ? "null" : getDatos_grafica(new Funcion[] {new Funcion() {
			@Override
			public Float get(Jugador j) {
				return Util.integerToFloat(j.forma);
			}
		}}, null, null, null);
	}

	public String getDatos_grafica_condicion(Usuario usuario) {
		return condicion == null ? "null" : getDatos_grafica(new Funcion[] {new Funcion() {
			@Override
			public Float get(Jugador j) {
				return Util.integerToFloat(j.condicion);
			}
		}}, usuario, TIPO_ENTRENAMIENTO.Condicion, null);
	}

	public String getDatos_grafica_rapidez(Usuario usuario) {
		return rapidez == null ? "null" : getDatos_grafica(new Funcion[] {new Funcion() {
			@Override
			public Float get(Jugador j) {
				return Util.integerToFloat(j.rapidez);
			}
		}}, usuario, TIPO_ENTRENAMIENTO.Rapidez, null);
	}

	public String getDatos_grafica_tecnica(Usuario usuario) {
		return tecnica == null ? "null" : getDatos_grafica(new Funcion[] {new Funcion() {
			@Override
			public Float get(Jugador j) {
				return Util.integerToFloat(j.tecnica);
			}
		}}, usuario, TIPO_ENTRENAMIENTO.Tecnica, null);
	}
	
	public String getDatos_grafica_pases(Usuario usuario) {
		return pases == null ? "null" : getDatos_grafica(new Funcion[] {new Funcion() {
			@Override
			public Float get(Jugador j) {
				return Util.integerToFloat(j.pases);
			}
		}}, usuario, TIPO_ENTRENAMIENTO.Pases, null);
	}

	public String getDatos_grafica_porteria(Usuario usuario) {
		return porteria == null ? "null" : getDatos_grafica(new Funcion[] {new Funcion() {
			@Override
			public Float get(Jugador j) {
				return Util.integerToFloat(j.porteria);
			}
		}}, usuario, TIPO_ENTRENAMIENTO.Porteria, null);
	}

	public String getDatos_grafica_defensa(Usuario usuario) {
		return defensa == null ? "null" : getDatos_grafica(new Funcion[] {new Funcion() {
			@Override
			public Float get(Jugador j) {
				return Util.integerToFloat(j.defensa);
			}
		}}, usuario, TIPO_ENTRENAMIENTO.Defensa, null);
	}
	
	public String getDatos_grafica_creacion(Usuario usuario) {
		return creacion == null ? "null" : getDatos_grafica(new Funcion[] {new Funcion() {
			@Override
			public Float get(Jugador j) {
				return Util.integerToFloat(j.creacion);
			}
		}}, usuario, TIPO_ENTRENAMIENTO.Creacion, null);
	}

	public String getDatos_grafica_anotacion(Usuario usuario) {
		return anotacion == null ? "null" : getDatos_grafica(new Funcion[] {new Funcion() {
			@Override
			public Float get(Jugador j) {
				return Util.integerToFloat(j.anotacion);
			}
		}}, usuario, TIPO_ENTRENAMIENTO.Anotacion, null);
	}
	
	public String getDatos_grafica(Funcion[] funciones, Usuario usuario, TIPO_ENTRENAMIENTO entrenamiento, String colores) {
		List<String> datos = new ArrayList<String>();
		List<String> hTicks = new ArrayList<String>();

		float min_valor = Float.MAX_VALUE;
		float max_valor = 0;
		int min_jornada = 0;
		int max_jornada = jornada;
		
		Jugador it = this;
		int j = getNum_jornadas();
		while (it != null) {
			Float[] valores = new Float[funciones.length];
			int i = 0;
			for (Funcion f : funciones) {
				valores[i] = Util.fnvl(f.get(it, j));

				if (valores[i] > max_valor) {
					max_valor = valores[i];
				}
				if (valores[i] < min_valor) {
					min_valor = valores[i];
				}
				i++;
			}
			
			datos.add("[" + (it.jornada - jornada) + "," + StringUtils.join(valores, ",") + "," + (entrenamiento == null ? "true" : usuario.getTipo_entrenamiento().get(it.jornada - 1) == entrenamiento) + "]");
			hTicks.add("{'v':" + (it.jornada - jornada) + ",'f':'" + it.edad + " " + ((it.jornada + 1) % 16) + "/16'}");

			if (it.original == null) {
				min_jornada = it.jornada;
			}
			it = it.original;
			j--;
		}

		return "[" + String.join(",", datos) + "]," + "[" + String.join(",", hTicks) + "]," + min_valor + "," + max_valor + "," + (min_jornada - max_jornada) + "," + 0 + "," + colores; // 0 = max_jornada
	}

	public String getColor() {
		switch (demarcacion) {
			case GK:	return "blue";
			case DEF:	return "green";
			case MID:	return "brown";
			case ATT:	return "red";
		}
		return null;
	}
	
	public BigDecimal getValor_pais(Integer countryID) {
		if (valor == null) {
			return null;
		} else if (countryID == null || countryID.equals(0)) {
			return new BigDecimal(valor);
		} else {
			return new BigDecimal(valor * 4).divide(Pais.currency_rates[countryID - 1], RoundingMode.FLOOR);
		}
	}
	
	//-------
	// G & S
	//-------

	public Jugador getOriginal() {
		return original;
	}

	public void setOriginal(Jugador original) {
		this.original = original;
	}

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Integer getPid() {
		return pid;
	}
	public void setPid(Integer pid) {
		this.pid = pid;
	}
	public String getEquipo() {
		return equipo;
	}
	public void setEquipo(String equipo) {
		this.equipo = equipo;
	}
	public Integer getTid() {
		return tid;
	}
	public void setTid(Integer tid) {
		this.tid = tid;
	}
	public Integer getEdad() {
		return edad;
	}
	public void setEdad(Integer edad) {
		this.edad = edad;
	}
	public DEMARCACION_ASISTENTE getDemarcacion() {
		return demarcacion;
	}
	public void setDemarcacion(DEMARCACION_ASISTENTE demarcacion) {
		this.demarcacion = demarcacion;
	}

	// Solo permitimos pasar demarcaciones en este sentido
	public void setDemarcacion(DEMARCACION demarcacion) {
		this.demarcacion = DEMARCACION_ASISTENTE.valueOf(demarcacion.name());
	}

	public BigDecimal getPuntos() {
		return puntos;
	}
	public void setPuntos(BigDecimal puntos) {
		this.puntos = puntos;
	}
	public Integer getCondicion() {
		return condicion;
	}
	public void setCondicion(Integer condicion) {
		this.condicion = condicion;
	}
	public Integer getRapidez() {
		return rapidez;
	}
	public void setRapidez(Integer rapidez) {
		this.rapidez = rapidez;
	}
	public Integer getTecnica() {
		return tecnica;
	}
	public void setTecnica(Integer tecnica) {
		this.tecnica = tecnica;
	}
	public Integer getPases() {
		return pases;
	}
	public void setPases(Integer pases) {
		this.pases = pases;
	}
	public Integer getPorteria() {
		return porteria;
	}
	public void setPorteria(Integer porteria) {
		this.porteria = porteria;
	}
	public Integer getDefensa() {
		return defensa;
	}
	public void setDefensa(Integer defensa) {
		this.defensa = defensa;
	}
	public Integer getCreacion() {
		return creacion;
	}
	public void setCreacion(Integer creacion) {
		this.creacion = creacion;
	}
	public Integer getAnotacion() {
		return anotacion;
	}
	public void setAnotacion(Integer anotacion) {
		this.anotacion = anotacion;
	}

	public Integer getJornada() {
		return jornada;
	}

	public void setJornada(Integer jornada) {
		this.jornada = jornada;
	}

	public Integer getValor() {
		return valor;
	}

	public void setValor(Integer valor) {
		this.valor = valor;
	}

	public String getIcono() {
		return icono;
	}

	public void setIcono(String icono) {
		this.icono = icono;
	}

	public Integer getEdicion() {
		return edicion;
	}

	public void setEdicion(Integer edicion) {
		this.edicion = edicion;
	}

	public boolean isActualizado() {
		return actualizado;
	}

	public void setActualizado(boolean actualizado) {
		this.actualizado = actualizado;
	}

	public Integer getForma() {
		return forma;
	}

	public void setForma(Integer forma) {
		this.forma = forma;
	}

	public Integer getPais() {
		return pais;
	}

	public void setPais(Integer pais) {
		this.pais = pais;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Integer getTarjetas() {
		return tarjetas;
	}

	public void setTarjetas(Integer tarjetas) {
		this.tarjetas = tarjetas;
	}

	public Integer getNt() {
		return nt;
	}

	public void setNt(Integer nt) {
		this.nt = nt;
	}

	public Integer getLesion() {
		return lesion;
	}

	public void setLesion(Integer lesion) {
		this.lesion = lesion;
	}

	public Integer getEn_venta() {
		return en_venta;
	}

	public void setEn_venta(Integer en_venta) {
		this.en_venta = en_venta;
	}

	public Integer getMinutos() {
		return minutos;
	}

	public void setMinutos(Integer minutos) {
		this.minutos = minutos;
	}

	public DEMARCACION getDemarcacion_entrenamiento() {
		return demarcacion_entrenamiento;
	}

	public void setDemarcacion_entrenamiento(DEMARCACION demarcacion_entrenamiento) {
		this.demarcacion_entrenamiento = demarcacion_entrenamiento;
	}

	public String getNotas() {
		return notas;
	}

	public void setNotas(String notas) {
		this.notas = notas;
	}

	public Float getTalento_min() {
		return talento_min;
	}

	public void setTalento_min(Float talento_min) {
		this.talento_min = talento_min;
	}

	public Float getTalento_max() {
		return talento_max;
	}

	public void setTalento_max(Float talento_max) {
		this.talento_max = talento_max;
	}

	public String getIcono_foro() {
		return icono_foro;
	}

	public void setIcono_foro(String icono_foro) {
		this.icono_foro = icono_foro;
	}

	public Integer getDisciplina_tactica() {
		return disciplina_tactica;
	}

	public void setDisciplina_tactica(Integer disciplina_tactica) {
		this.disciplina_tactica = disciplina_tactica;
	}

	public Integer getValor_original() {
		return valor_original;
	}

	public void setValor_original(Integer valor_original) {
		this.valor_original = valor_original;
	}
}
