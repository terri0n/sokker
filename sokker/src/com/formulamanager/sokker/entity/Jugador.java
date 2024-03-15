package com.formulamanager.sokker.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX.TIPO_FACTORX;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;
import com.formulamanager.sokker.bo.NtdbBO;
import com.formulamanager.sokker.bo.UsuarioBO;

public class Jugador implements Comparable<Jugador> {
	// Bloque de entrenamiento
	public class Entrenamiento {
		public int valor_habilidad;
		public boolean abierto_izq;
		public boolean abierto_der;
		public List<Float> puntos_entrenamiento;
		public List<Jugador> jornadas;
		public boolean editable;
		
		public Entrenamiento(int valor_habilidad) {
			this.valor_habilidad = valor_habilidad;
			abierto_izq = true;
			abierto_der = true;
			puntos_entrenamiento = new ArrayList<Float>();
			jornadas = new ArrayList<Jugador>();
			editable = true;
		}
		
		// Suma los puntos obtenidos hasta la edad indicada (relativos a ella)
		public float getPuntos(int edad, TIPO_ENTRENAMIENTO habilidad) {
			float suma = 0F;
			for (int i = 0; i < jornadas.size(); i++) {
				if (jornadas.get(i).getEdad() <= edad) {
					suma += puntos_entrenamiento.get(i) * (habilidad == TIPO_ENTRENAMIENTO.Condicion ? 1 : Math.pow(usuario.getFactor_edad(habilidad), edad - jornadas.get(i).getEdad()));
				}
			}
			return suma;
		}

		// Devuelve la suma de pe absoluta
		public float getPuntos(TIPO_ENTRENAMIENTO habilidad, boolean incluir_ultimo) {
			float suma = 0F;

//System.out.println("-");
			for (int i = incluir_ultimo ? 0 : 1; i < jornadas.size(); i++) {
				float exponente_edad = jornadas.get(i).edad - 16;
				float exponente_habilidad = valor_habilidad - habilidad.getFactor(usuario);
				// Si el entrenamiento está completo y estamos en la última semana, le sumo 0.5
				// 25/08/2023: lo descarto. En niveles bajos podría causar un error incomprensible para el usuario
//				if (i == 0 && !abierto_der) {
//					exponente_habilidad += 0.5F;
//				}
				suma += (float) (puntos_entrenamiento.get(i) * Math.pow(usuario.getFactor_edad(habilidad), -exponente_edad) * Math.pow(usuario.getFactor_habilidad(), -exponente_habilidad) / (FACTOR_TALENTO * usuario.getFactor_talento()));	// 180 / 45 = 4
//System.out.println(suma + " " + puntos_entrenamiento + " " + Math.pow(usuario.getFactor_edad(), -exponente_edad) + " " + Math.pow(usuario.getFactor_habilidad(), -exponente_habilidad) + " " + FACTOR_TALENTO * usuario.getFactor_talento());
//System.out.println(suma + " " + usuario.getFactor_edad() + " "  + exponente_edad);
			}
			return suma;
		}
	}
	
	protected abstract class Dataset {
		public List<String> data = new ArrayList<String>();
		public List<String> labels = new ArrayList<String>();	// Etiquetas del eje X
		public List<String> borderColor = new ArrayList<String>();
		public String backgroundColor;
		public Float max_valor = 0F;
		public String label;									// T�tulo del dataSet
		
		public Dataset(String borderColor, String backgroundColor, String label) {
			if (borderColor != null) {
				this.borderColor.add("\"" + borderColor + "\"");
			}
			this.backgroundColor = backgroundColor == null ? null : "\"" + backgroundColor + "\"";
			this.label = label == null ? null : "\"" + label + "\"";
		}
		
		public Dataset(String borderColor, String backgroundColor, String label, Jugador jugador, TIPO_ENTRENAMIENTO entrenamiento) {
			this(borderColor, backgroundColor, label);
			
			HashMap<Integer, String> hm_labels = new HashMap<>();
			int min_jornada = 0;

			Jugador it = jugador;
//			Jugador posterior = null;
			while (it != null) {
				// La edad a veces falla en la jornada 12. ¿Será porque no se actualiza el jugador después de cumplir años? En esos casos cojo la de la jornada anterior o la de la siguiente menos 1
				// Tb tengo que restar 1 igualmente. Mirar la gráfica de Euzebiusz Szostek
				// 13/03/2023: lo comento porque falla con los juveniles. De todas formas al añadir el nº de jornada a la etiqueta esto ya no es tan importante
				int edad_label = it.edad;
/*				if (AsistenteBO.getJornadaMod(it.jornada) == 12) {
					if (original != null) {
						edad_label = original.edad - 1;
					} else if (posterior != null) {
						edad_label = posterior.edad - 1;
					}
				}
*/
				// Si no pongo la jornada como prefijo hay veces que se lían los datos
				String prefijo = "[" + it.jornada + "] ";
				String label_x = "\"" + prefijo + edad_label + " " + AsistenteBO.getJornadaMod(it.jornada) + "/13\"";

				Float valor = Util.fnvl(getValor(it, it.getJornada() - it.getPrimera_jornada()));

				if (valor > max_valor) {
					max_valor = valor;
				}
				data.add("{\"x\":" + label_x + ",\"y\":" + valor + "}");
				
				hm_labels.put(it.jornada, label_x);
				if (borderColor == null) {
					this.borderColor.add("\"" + it.getColor_entrenamiento(entrenamiento) + "\"");
				}

				if (it.original == null) {
					min_jornada = it.jornada;
				}
//				posterior = it;
				it = it.original;
			}
			
			// Duplico el primer color para que se pinte tb el punto del principio (estar� al final porque recorremos las jornadas en sentido inverso)
			// y elimino el de la jornada actual (el 1�) porque todav�a no se ha entrenado
			if (borderColor == null && this.borderColor.size() > 0) {
				this.borderColor.add(this.borderColor.get(this.borderColor.size() - 1));
				this.borderColor.remove(0);
			}

			// Añado una etiqueta en blanco en aquellas jornadas en las que no haya habido actualizaci�n 
			for (int i = min_jornada; i <= jornada; i++) {
				labels.add(hm_labels.get(i));
			}
		}
		
		public Float getValor(Jugador j, int x) {
			return getValor(j);
		}

		protected abstract Float getValor(Jugador j);
	}

	public Dataset crearDataset(String color, String backgroundColor, String titulo) {
		return new Dataset(color, backgroundColor, titulo) {
			@Override
			protected Float getValor(Jugador j) {
				return null;
			}
		};
	}

	public static String puntos_condicion[] = {"0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "1.0", "1.2", "1.5"};
	public static String puntos_rapidez[] = {"0.0", "1.4", "1.7", "1.9", "2.2", "2.4", "2.6", "3.0", "3.4", "3.7", "4.1", "4.6", "5.0", "5.5", "6.0", "7.2", "8.4", "9.6", "11.5"};
	public static String puntos_otros[] = {"0.0", "1.2", "1.4", "1.6", "1.8", "2.0", "2.2", "2.5", "2.8", "3.1", "3.4", "3.8", "4.2", "4.6", "5.0", "6.0", "7.0", "8.0", "9.5"};
	public static String valores[] = {"tragic", "hopeless", "unsatisfactory", "poor", "weak", "average", "adequate", "good", "solid", "very good", "excellent", "formidable", "outstanding", "incredible", "brilliant", "magical", "unearthly", "divine", "superdivine", "" };
	public static String valores_m[] = {"tr�gico", "terrible", "deficiente", "pobre", "d�bil", "regular", "aceptable", "bueno", "s�lido", "muy bueno", "excelente", "formidable", "destacado", "incre�ble", "brillante", "m�gico", "sobrenatural", "divino", "superdivino", "" };
	public static String valores_f[] = {"tr�gica", "terrible", "deficiente", "pobre", "d�bil", "regular", "aceptable", "buena", "s�lida", "muy buena", "excelente", "formidable", "destacada", "incre�ble", "brillante", "m�gica", "sobrenatural", "divina", "superdivina" };

	public static enum DEMARCACION { GK, DEF, MID, ATT };
	public static enum DEMARCACION_ASISTENTE { GK, DEF, MID, ATT, BANQUILLO, OTRA };	// Los �ndices comunes con Sokker deben coincidir para compatibilizarlo con Sokker -> AsistenteBO#obtener_entrenamiento()

	// Los mantengo para las estad�sticas:
	public static float BASE_TALENTO_HABILIDAD = 1.094f;
	public static float BASE_TALENTO_EDAD = 1.094f;
	public static int FACTOR_TALENTO = 47;

	public final static String GRAY = "#CCCCCC";
	public final static String RED = "#FF0000";
	public final static String ORANGE = "#FF8800";
	public final static String GREEN = "#00AA00";
	public final static String BLUE = "#0000FF";
	public final static String PINK = "#FFC0CB";

/*	public static float FACTOR_RAP = 4.5f;	// 121%
	public static float FACTOR_POR = 5.5f;	// 112%
	public static float FACTOR_DEF = 5.5f;	// 112%
	public static float FACTOR_ANO = 5.5f;	// 112%
	public static float FACTOR_TEC = 6f;	// 107%
	public static float FACTOR_PAS = 6.8f;	// 100%
	public static float FACTOR_CRE = 6.8f;	// 100%
*/	
	private Usuario usuario;

	// Si se trata de un jugador traspasado, el usuario tras el traspaso será el actual y antes del traspaso será usuario2
	private Usuario usuario2;

	private String nombre;
	private Integer pid;
	private String equipo;
	private Integer tid;
	private Integer edad;
	private Integer valor;
	private Integer jornada;
	
	private Integer jornada_traspaso;	// Jornada en la que el jugador ha sido traspasado. Si se especifica, el usuario que se tendrá en cuenta para calcular los entrenamientos antes de esta jornada será usuario2

	private String color;
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
	private Integer en_venta;						// { 0: no | 1: on sale | 2: transfer ad }
	private Float minutos;							// Realmente es la efectividad del enternamiento
	private DEMARCACION demarcacion_entrenamiento;	// La del partido jugado -> la reemplazo por la que selecciona el usuario en el nuevo sistema de entreno
	private boolean entrenamiento_avanzado;
	private String notas;
	
	private BigDecimal puntos = BigDecimal.ZERO;
	private String icono;
	private String icono_foro;
	private Integer edicion;

	private Jugador original;

	private boolean actualizado;					// "Fiable" para el asistente
	private double talento_min;
	private double talento_max;
	private Double talento;
	private boolean destacar;
	
	private Juvenil juvenil;
	
	// NTDB
	private Integer disciplina_tactica;

	private Integer salario;
	private Integer experiencia;
	private Integer trabajo_en_equipo;
	private Integer altura;
	private Integer peso;	// 3 dígitos sin coma (Dividir entre 10)
	private Integer IMC;	// 4 dígitos sin coma (Dividir entre 100)
	
	private transient String login_duenyo;	// Para enviar sk-mails, no se guarda
	
//	private HashMap<String, List<Entrenamiento>> total_entrenamientos = new HashMap<String, List<Entrenamiento>>();
	public HashMap<TIPO_ENTRENAMIENTO, List<Entrenamiento>> total_entrenamientos2 = new HashMap<TIPO_ENTRENAMIENTO, List<Entrenamiento>>();

	
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
	
	// Entrenador
	public Jugador(Integer[] tr_entrenador) {
		this.setCondicion(tr_entrenador[0]);
		this.setRapidez(tr_entrenador[1]);
		this.setTecnica(tr_entrenador[2]);
		this.setPases(tr_entrenador[3]);
		this.setPorteria(tr_entrenador[4]);
		this.setDefensa(tr_entrenador[5]);
		this.setCreacion(tr_entrenador[6]);
		this.setAnotacion(tr_entrenador[7]);
		if (tr_entrenador.length > 8) {
			// Media
			// Solo se pasa al leer el entrenador de los XML, pero no se guarda
			this.setForma(tr_entrenador[8]);
		}
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
		usuario = j.usuario;
		usuario2 = j.usuario2;
		nombre = j.nombre;
		pid = j.pid;
		tid = j.tid;
		fecha = j.fecha;
		edad = j.edad;
		pais = j.pais;
		actualizado = j.actualizado;	// Si las habilidades originales eran fiables, las de la copia tb
		
		salario = j.salario;
		experiencia = j.experiencia;
		disciplina_tactica = j.disciplina_tactica;
		trabajo_en_equipo = j.trabajo_en_equipo;
		altura = j.altura;
		peso = j.peso;
		IMC = j.IMC;
		
		talento = j.talento;
		destacar = j.destacar;
		color = j.color;
	}

	public Jugador(Integer pid, String nombre, Integer edad, Integer valor, Integer tid, Usuario usuario) {
		this.pid = pid;
		this.nombre = nombre;
		this.edad = edad;
		this.valor = valor;
		this.tid = tid;
		this.usuario = usuario;
	}

	/**
	 * @param pid
	 * @param valores
	 * @param primero Indica si es el primer jugador del listado. Los originales no tendrán la cabecera
	 * @param nombre Nombre del 1er jugador, para que se vaya propagando
	 * @param edad_anterior Edad de la jornada anterior, para corregir los casos en los que se actualizó el equipo el jueves antes de cumplir años
	 */
	public Jugador(Integer pid, List<String> valores, boolean primero, String nombre, DEMARCACION_ASISTENTE demarcacion, Usuario usuario, Usuario usuario2, Integer edad_anterior) {
		this.pid = pid;
		this.usuario = usuario;
		this.usuario2 = usuario2;
		int i = 0;

		if (primero) {
			this.nombre = valores.get(i++);
			tid = Integer.valueOf(valores.get(i++));
			this.demarcacion = valores.get(i++).equals("") ? null : DEMARCACION_ASISTENTE.valueOf(valores.get(i-1));
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
			en_venta = valores.get(i++).equals("") ? null : Integer.valueOf(valores.get(i-1));
			notas = Util.nnvl(valores.get(i++));
			
			String aux = Util.nvl(valores.get(i));
			if (aux.length() > 0 && aux.charAt(0) == '-') {
				salario = Math.abs(Integer.valueOf(valores.get(i++)));
				altura = valores.get(i++).equals("") ? null : Integer.valueOf(valores.get(i-1));
				peso = valores.get(i++).equals("") ? null : Integer.valueOf(valores.get(i-1));
				IMC = valores.get(i++).equals("") ? null : Integer.valueOf(valores.get(i-1));

				aux = Util.nvl(valores.get(i));
				if (aux.length() > 0 && aux.charAt(0) == '-') {
					talento = valores.get(i++).equals("-") ? null : Math.abs(Double.valueOf(valores.get(i-1)));
					destacar = Boolean.valueOf(valores.get(i++));
				}
				
				if (valores.get(i).startsWith("#")) {
					color = valores.get(i++);
					
					if (valores.get(i).startsWith("-")) {
						String login2 = Util.nnvl(valores.get(i++).substring(1));
						if (login2 != null) {
							this.usuario2 = UsuarioBO.leer_usuario(login2, false);
						}
					}
				}
			}
		} else {
			this.nombre = nombre;
			this.demarcacion = demarcacion;
		}

		jornada = Integer.valueOf(valores.get(i++));
		edad = Integer.valueOf(valores.get(i++));
		// Corrección de la edad
		if (AsistenteBO.getJornadaMod(jornada + 1) == 0 && edad_anterior != null) {
			edad = edad_anterior;
		}
		
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
		String aux = Util.nvl(valores.get(i));
		if (aux.length() > 0 && aux.charAt(0) == '-') {
			lesion = Math.abs(Integer.valueOf(valores.get(i++)));
		}
		forma = Util.stringToInteger(valores.get(i++));
//System.out.println(this.nombre + " " + jornada);
		demarcacion_entrenamiento = valores.get(i++).equals("") ? null : DEMARCACION.valueOf(valores.get(i-1));
		minutos = Float.valueOf(valores.get(i++));

		aux = i >= valores.size() ? "" : Util.nvl(valores.get(i));
		if (aux.length() > 0 && aux.charAt(0) == '-') {
			experiencia = Math.abs(Integer.valueOf(valores.get(i++)));
			disciplina_tactica = valores.get(i++).equals("") ? null : Integer.valueOf(valores.get(i-1));
			trabajo_en_equipo = valores.get(i++).equals("") ? null : Integer.valueOf(valores.get(i-1));
		}
		
		if (jornada >= AsistenteBO.JORNADA_NUEVO_ENTRENO) {
			entrenamiento_avanzado = Boolean.valueOf(valores.get(i++));
		}
		
		if (valores.size() > i && !valores.get(i).equals("*")) {
			List<String> sublista = valores.subList(i, valores.size());
			original = new Jugador(pid, sublista, false, this.nombre, this.demarcacion, usuario, this.usuario2, edad);
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
		if (edad < 21 || jornada < 1080) {
			puntos = BigDecimal.ZERO;
		} else {
			puntos = new BigDecimal(edad - 21);
		}
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

	public Integer getJornadaProyeccion() {
		// Sumo uno porque "jornada" es la de la �ltima actualizaci�n, q es la jornada anterior
		return AsistenteBO.getJornadaMod(jornada + 1);
	}

	public Integer getJornadaMod() {
		return AsistenteBO.getJornadaMod(jornada);
	}

	public boolean validar(TIPO_FACTORX tipo) {
		if (tipo == TIPO_FACTORX.senior && (edad < 21 || edad > 27)
				|| tipo != TIPO_FACTORX.senior && (edad < 18 || edad > 20)) {
			return false;
		}
		
		if (tipo == TIPO_FACTORX.senior) {
			if (edad <= 22) {
				// GK
				if (demarcacion == DEMARCACION_ASISTENTE.GK && porteria + rapidez + pases >= 27 && porteria >= 13) {
					return true;
				}

				// DEF
				if (demarcacion == DEMARCACION_ASISTENTE.DEF && defensa + rapidez >= 22 && pases + tecnica + creacion >= 11) {
					return true;
				}

				// MID
				if (demarcacion == DEMARCACION_ASISTENTE.MID && rapidez + creacion + pases + tecnica + defensa >= 39) {
					return true;
				}

				// ATT
				if (demarcacion == DEMARCACION_ASISTENTE.ATT && rapidez + tecnica + anotacion >= 29) {
					return true;
				}
			} else if (edad <= 24) {
				// GK
				if (demarcacion == DEMARCACION_ASISTENTE.GK && porteria + rapidez + pases >= 32 && porteria >= 14) {
					return true;
				}

				// DEF
				if (demarcacion == DEMARCACION_ASISTENTE.DEF && defensa + rapidez >= 25 && pases + tecnica + creacion >= 16) {
					return true;
				}

				// MID
				if (demarcacion == DEMARCACION_ASISTENTE.MID && rapidez + creacion + pases + tecnica + defensa >= 48) {
					return true;
				}

				// ATT
				if (demarcacion == DEMARCACION_ASISTENTE.ATT && rapidez + tecnica + anotacion >= 34) {
					return true;
				}
			} else {
				// GK
				if (demarcacion == DEMARCACION_ASISTENTE.GK && porteria + rapidez + pases >= 36 && porteria >= 15) {
					return true;
				}

				// DEF
				if (demarcacion == DEMARCACION_ASISTENTE.DEF && defensa + rapidez >= 28 && pases + tecnica + creacion >= 21) {
					return true;
				}

				// MID
				if (demarcacion == DEMARCACION_ASISTENTE.MID && rapidez + creacion + pases + tecnica + defensa >= 57) {
					return true;
				}

				// ATT
				if (demarcacion == DEMARCACION_ASISTENTE.ATT && rapidez + tecnica + anotacion >= 39) {
					return true;
				}
			}
		} else {
			// --- JUNIOR / INTER --- //
			
			// GK
			int min_gk = edad == 18 ? 9 : edad == 19 ? 11 : 12;
			if (demarcacion == DEMARCACION_ASISTENTE.GK
					&& porteria >= min_gk
					&& porteria + rapidez + pases >= 17 + (edad-18)*2) {
				return true;
			}

			// DEF
			int min_def = edad == 18 ? 5 : edad == 19 ? 7 : 8;
			if (demarcacion == DEMARCACION_ASISTENTE.DEF
					&& defensa + rapidez >= 13 + (edad-18)*2
					&& creacion + tecnica + pases >= min_def) {
				return true;
			}
			
			// MID
			int min = edad == 18 ? 23 : edad == 19 ? 27 : 30;
			if (demarcacion == DEMARCACION_ASISTENTE.MID
					&& (creacion + rapidez + tecnica + pases >= min
					|| creacion + rapidez + tecnica + defensa >= min
					|| creacion + rapidez + defensa + pases >= min
					|| creacion + defensa + tecnica + pases >= min
					|| defensa + rapidez + tecnica + pases >= min)) {
				return true;
			}

			// ATT
			if (demarcacion == DEMARCACION_ASISTENTE.ATT && anotacion + rapidez + tecnica >= 18 + (edad-18)*3) {
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
			valores[i] = (numeros ? i + " - " : "") + Util.getTexto(lang, "skills.skill" + i + gender);
		}
		
		String cad = "<select name='" + name + "'>";
		Integer i = 0;
		for (String s : valores) {
			if (i <= max) {
				cad += "<option value='" + i + "' " + (i.equals(indice) ? "selected" : "") + ">" + s + "</option>";
				i++;
			}
		}
		cad += "</select>";
		
		return cad;
	}

//	public static String despDemarcacion(DEMARCACION demarcacion) {
//		return despDemarcacion(demarcacion.name(), DEMARCACION.values(), null);
//	}

	public static String despDemarcacion(DEMARCACION_ASISTENTE demarcacion) {
		return despDemarcacion(demarcacion.name(), DEMARCACION.values(), null);
	}

	public static String despDemarcacion_asistente(DEMARCACION_ASISTENTE demarcacion, Usuario usuario) {
		return despDemarcacion(demarcacion == null ? null : demarcacion.name(), DEMARCACION_ASISTENTE.values(), usuario);
	}

	public static String despDemarcacion(String demarcacion, Enum<?>[] values, Usuario usuario) {
		try {
			String cad = "<select name='demarcacion'>";
			for (Enum<?> d : values) {
				if (d != DEMARCACION_ASISTENTE.BANQUILLO || usuario.isMostrar_banquillo()) {
					cad += "<option value='" + d.name() + "' " + (d.name().equals(demarcacion) ? "selected" : "") + ">";
					if (d == DEMARCACION_ASISTENTE.BANQUILLO) {
						cad += Util.getTexto(usuario.getLocale(), "common.bench");
					} else if (d == DEMARCACION_ASISTENTE.OTRA) {
						cad += Util.getTexto(usuario.getLocale(), "common.other_position");
					} else {
						cad += d.name();
					}
					cad +=  "</option>";
				}
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
			
			valores.add("-" + Util.invl(salario));
			valores.add(Util.nvl(altura));
			valores.add(Util.nvl(peso));
			valores.add(Util.nvl(IMC));

			valores.add("-" + Util.nvl(talento));
			valores.add(Util.nvl(destacar));

			valores.add(color == null ? "#" : color);
			valores.add("-" + (usuario2 == null ? "" : usuario2.getLogin()));
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
		valores.add(Util.fnvl(minutos)+"");
		
		valores.add("-" + Util.invl(experiencia));
		valores.add(Util.nvl(disciplina_tactica));
		valores.add(Util.nvl(trabajo_en_equipo));

		if (jornada >= AsistenteBO.JORNADA_NUEVO_ENTRENO) {
			valores.add(entrenamiento_avanzado + "");
		}

		String serializacion_original = "";
		// Evito bucles infinitos
		if (original != null && !jornada.equals(original.getJornada())) {
			serializacion_original = "," + original.serializar(false);
		}

		// Concateno los datos y un asterisco al final para asegurarme de que no disminuye el nº de campos al leer el archivo
		return String.join(",", valores) + serializacion_original + (primero ? ",*" : "");
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
	
	public String getValor_bbcode() {
		if (valor == null) {
			return "";
		} else {
//			if (original == null || original.valor == null || valor.compareTo(original.valor) == 0) {
				return "[money=" + usuario.getCountryID() + "]" + getValor_pais() + "[/money]";
/*			} else if (valor.compareTo(original.valor) < 0) {
				return "[color=salmon][money=" + usuario.getCountryID() + "]" + getValor_pais() + "[/money][/color]";
			} else {
				return "[color=lightgreen][money=" + usuario.getCountryID() + "]" + getValor_pais() + "[/money][/color]";
			}
*/		}
	}

	public String getHabilidad_bbcode(Integer valor, Integer original, String habilidad, boolean juveniles) {
		if (valor == null) {
			return "";
		} else {
			String gender;
			try {
				gender = Util.getTexto(usuario.getLocale(), "skills.gender_" + habilidad);
			} catch (Exception e) {
				gender = "m";
			}

			String texto = Util.getTexto(usuario.getLocale(), "skills.skill" + valor + gender);
			if (juveniles) {
				texto = StringUtils.rightPad(texto, 12 + (usuario.isNumeros() ? 5 : 0), ".");
			}
			
			if (juveniles && original == null) {
				return "[b][color=silver]" + texto + "[/color]" + (usuario.isNumeros() ? " [" + valor + "]" : "") + "[/b]";
			} else if (original == null || valor.compareTo(original) == 0) {
				return "[b]" + texto + (usuario.isNumeros() ? " [" + valor + "]" : "") + "[/b]";
			} else if (valor.compareTo(original) < 0) {
				return "[b][color=salmon]" + texto + "[/color]" + (usuario.isNumeros() ? " [" + valor + "]" : "") + "[/b]";
			} else {
				return "[b][color=lightgreen]" + texto + "[/color]" + (usuario.isNumeros() ? " [" + valor + "]" : "") + "[/b]";
			}
		}
	}

	public String getClase_cambio(Integer valor, Integer original, String skill) {
		String resp = "";
		if (demarcacion != null && skill != null) {
			resp += " " + demarcacion.name() + "_" + skill;
		}
		
		if (this instanceof Juvenil || getFecha() != null) {
			// Solo compara valores si el �ltimo valor fue actualizado en la �ltima semana
			Calendar c = Calendar.getInstance();
			if (getFecha() != null) {
				c.setTime(getFecha());
				c.add(Calendar.DAY_OF_MONTH, 7);
			}
			if (this instanceof Juvenil || !c.getTime().before(new Date())) {
				resp += (valor == null || original == null || valor.equals(original) ? "" : valor.compareTo(original) < 0 ? " cambio_bajada" : " cambio_subida");
			}
		}
		
		return resp;
	}

	public String getClase_minimo(Integer valor, Integer original, String skill) {
		return (valor == null || valor <= 5 ? " " : valor >= 8 ? " negrita" : "")
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

	/**
	 * Copiada de @link #getEntrenamientosTemporada2(TIPO_ENTRENAMIENTO habilidad, Entrenamiento entrenamiento_act)
	 * @param habilidad
	 * @return
	 */
	private String getClase_cerca_de_subir(TIPO_ENTRENAMIENTO habilidad) {
		try {
			Integer valor = getValor_habilidad(habilidad);
			
			// Si el valor de la habilidad está al máximo, no puede subir más
			if (valor != null && (habilidad == TIPO_ENTRENAMIENTO.Condicion && valor < 11 || habilidad != TIPO_ENTRENAMIENTO.Condicion && valor < 18)) {
				List<Entrenamiento> entrenamientos = total_entrenamientos2.get(habilidad);
		
				if (entrenamientos != null) {
					// Puntos de entrenamientos efectivos - puntos de entrenamiento necesitados -> lo acumulamos en el siguiente entrenamiento
					float resto = 0F;
					int edad_resto = 0;
					int prediccion = Integer.MAX_VALUE;
					float suma = 0f;
					for (Entrenamiento entrenamiento : entrenamientos) {
						suma = entrenamiento.getPuntos(edad, habilidad);
						float exponente_edad = edad - 16;
						float exponente_habilidad = entrenamiento.valor_habilidad - habilidad.getFactor(usuario);
						prediccion = habilidad == TIPO_ENTRENAMIENTO.Condicion ? 80 : (int)Math.round(getTalento_medio() * Math.pow(usuario.getFactor_edad(habilidad), exponente_edad) * Math.pow(usuario.getFactor_habilidad(), exponente_habilidad) * FACTOR_TALENTO * usuario.getFactor_talento());
						
						if (habilidad != TIPO_ENTRENAMIENTO.Condicion) {
							float resto_actual = resto * (float)Math.pow(usuario.getFactor_edad(habilidad), entrenamiento.jornadas.get(0).getEdad() - edad_resto);
							resto = suma + resto_actual > prediccion ? suma + resto_actual - prediccion : 0F;
							// Limito el resto a los puntos de entrenamiento del �ltimo entrenamiento
							// NOTA: como el resto va aumentando en funci�n de los a�os cumplidos, tengo que hacer lo mismo con los puntos del último entrenamiento
							float equivalente_ultimo_entrenamiento = entrenamiento.puntos_entrenamiento.get(0) * (float)Math.pow(usuario.getFactor_edad(habilidad), entrenamiento.jornadas.get(0).getEdad() - edad_resto);
							resto = Math.min(resto, equivalente_ultimo_entrenamiento);
							edad_resto = entrenamiento.jornadas.get(0).getEdad();
						}
					}
					int extra = (int)(suma + resto * Math.pow(usuario.getFactor_edad(habilidad), edad - edad_resto));
					
					if (prediccion - extra < 100) {
						return " cerca";
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}

	public String getClase_condicion() {
		return getClase_minimo(condicion, original == null ? null : original.condicion, "CON") + getClase_cerca_de_subir(TIPO_ENTRENAMIENTO.Condicion);
	}

	public String getClase_rapidez() {
		return getClase_minimo(rapidez, original == null ? null : original.rapidez, "RAP") + getClase_cerca_de_subir(TIPO_ENTRENAMIENTO.Rapidez);
	}

	public String getClase_tecnica() {
		return getClase_minimo(tecnica, original == null ? null : original.tecnica, "TEC") + getClase_cerca_de_subir(TIPO_ENTRENAMIENTO.Tecnica);
	}

	public String getClase_pases() {
		return getClase_minimo(pases, original == null ? null : original.pases, "PAS") + getClase_cerca_de_subir(TIPO_ENTRENAMIENTO.Pases);
	}

	public String getClase_porteria() {
		return getClase_minimo(porteria, original == null ? null : original.porteria, "POR") + getClase_cerca_de_subir(TIPO_ENTRENAMIENTO.Porteria);
	}

	public String getClase_defensa() {
		return getClase_minimo(defensa, original == null ? null : original.defensa, "DEF") + getClase_cerca_de_subir(TIPO_ENTRENAMIENTO.Defensa);
	}

	public String getClase_creacion() {
		return getClase_minimo(creacion, original == null ? null : original.creacion, "CRE") + getClase_cerca_de_subir(TIPO_ENTRENAMIENTO.Creacion);
	}

	public String getClase_anotacion() {
		return getClase_minimo(anotacion, original == null ? null : original.anotacion, "ANO") + getClase_cerca_de_subir(TIPO_ENTRENAMIENTO.Anotacion);
	}
	
	public String getClase_fecha() {
		Calendar rojo = Calendar.getInstance();
		rojo.add(Calendar.MONTH, -3);
		Calendar verde = Calendar.getInstance();
		verde.add(Calendar.DAY_OF_MONTH, -7);
		return fecha == null ? "" : fecha.compareTo(rojo.getTime()) <= 0 ? "rojo" : fecha.compareTo(verde.getTime()) >= 0 ? "verde" : "";
	}

	public String getClase_experiencia() {
		return getClase(experiencia, original == null ? null : original.experiencia, null);
	}

	public String getClase_disciplina_tactica() {
		return getClase(disciplina_tactica, original == null ? null : original.disciplina_tactica, null);
	}

	public String getClase_trabajo_en_equipo() {
		return getClase(trabajo_en_equipo, original == null ? null : original.trabajo_en_equipo, null);
	}

	public String getTitle_forma() {
		return original == null || original.forma == null || original.forma.equals(forma) ? "" : new DecimalFormat("+#;-#").format(forma - original.forma);
	}
	
	public String getTitle_valor() {
		return original == null || original.valor == null || original.valor.equals(valor) ? "" : new DecimalFormat("+#,###;-#,###").format(valor - original.valor);
	}

	public String getTitle_condicion() {
		return condicion == null || original == null || original.condicion == null || original.condicion.equals(condicion) ? "" : new DecimalFormat("+#;-#").format(condicion - original.condicion);
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
			tid = j.tid;	// Para que se actualice en jugadores de las selecciones
			experiencia = j.experiencia;
			trabajo_en_equipo = j.trabajo_en_equipo;
			disciplina_tactica = j.disciplina_tactica;
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
//		return Math.max(nivel_entrenador - 6, 0) / 10f;
		return (nivel_entrenador + 0.5f) / 16.5f;
	}
	
	public BigDecimal getNivel_asistentes(Integer jornada) {
		BigDecimal nivel_asistentes = getUsuario_jornada().getNivel_asistentes().getOrDefault(jornada, getUsuario_jornada().getNivel_asistentes().get(getUsuario_jornada().getDef_jornada()));
//		return nivel_asistentes == null ? new BigDecimal(16) : nivel_asistentes;
		return nivel_asistentes == null ? new BigDecimal(16.25) : nivel_asistentes.min(new BigDecimal("16.25"));
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

	public float getMinutos_entrenamiento_nuevo(TIPO_ENTRENAMIENTO habilidad) {
		BigDecimal nivel_asistentes = getNivel_asistentes(jornada);
		float factor_entrenamiento_directo = getFactor_entrenamiento_directo(getUsuario_jornada(), jornada, habilidad);
		float factor_entrenamiento_residual = usuario.getFactor_residual() * (factor_entrenamiento_directo + nivel_asistentes.divide(new BigDecimal("16.25"), RoundingMode.HALF_UP).floatValue()) / 2f;
		float factor_entrenamiento_directo_sin_residual = (1f - usuario.getFactor_residual());	// 90%: el resto será el entrenamiento residual

		float residual = Math.min(100f, this.minutos) * factor_entrenamiento_residual;

		if (!entrenamiento_avanzado) {
			// Entrenamiento de formación
			// NOTA: hay que hacerlo después de calcular el residual
			factor_entrenamiento_directo *= usuario.getFactor_formacion();
		}

		float directo = Math.min(100f, this.minutos) * factor_entrenamiento_directo * factor_entrenamiento_directo_sin_residual;

		// Si no ha jugado entrena normal, por si está con entrenamiento avanzado
		if (demarcacion_entrenamiento == null || getUsuario_jornada().getTipo_entrenamiento(demarcacion_entrenamiento.ordinal()).get(jornada) == habilidad) {
			// Directo
			switch (habilidad) {
				case Condicion: 
					return 100f * factor_entrenamiento_directo * factor_entrenamiento_directo_sin_residual + residual;
				case Rapidez:
				case Tecnica:
				case Pases:
					return directo + residual;
				case Porteria:
					if (demarcacion_entrenamiento == DEMARCACION.GK) {
						return directo + residual;
					} else {
						return 0f;	// No permitido
					}
				case Defensa:
					if (demarcacion_entrenamiento == DEMARCACION.DEF) {
						return directo + residual;
					} else {
						return residual;
					}
				case Creacion:
					if (demarcacion_entrenamiento == DEMARCACION.MID) {
						return directo + residual;
					} else {
						return residual;
					}
				case Anotacion:
					if (demarcacion_entrenamiento == DEMARCACION.ATT) {
						return directo + residual;
					} else {
						return residual;
					}
				default:
					throw new RuntimeException("Entrenamiento incorrecto: " + habilidad);
			}
		} else {
			// Residual
			switch (habilidad) {
				case Condicion: 
					return 100f * factor_entrenamiento_residual - 5f;	// Residual para condici�n menos lo que baja de media por jornada
				case Porteria:
					if (demarcacion_entrenamiento == DEMARCACION.GK) {
						return residual;
					} else {
						return 0f;
					}
				case Anotacion:
					if (demarcacion_entrenamiento == DEMARCACION.ATT) {
						return residual;
					} else {
						return residual;	// El residual de anotaci�n parece ser menor si el jugador no es ATT <- lo quito de momento, a ver c�mo va el nuevo s� de entrenamiento
					}
				default: 
					return residual;
			}
		}
	}
	
	public float getMinutos_entrenamiento(TIPO_ENTRENAMIENTO habilidad) {
		if (jornada >= AsistenteBO.JORNADA_NUEVO_ENTRENO) {
			return Math.min(100f, getMinutos_entrenamiento_nuevo(habilidad));
		} else {
			BigDecimal nivel_asistentes = getNivel_asistentes(jornada);
			float factor_entrenamiento_directo = getFactor_entrenamiento_directo(usuario, jornada, habilidad);
	//		float factor_entrenamiento_residual = (factor_entrenamiento_directo + nivel_asistentes.subtract(new BigDecimal("6.5")).max(BigDecimal.ZERO).divide(BigDecimal.TEN).floatValue()) / 2f;
			// Multiplico por 10 porque con el nuevo entrenamiento he cambiado el significado del factor residual, pero aqu� se debe seguir usando como antes
			float factor_entrenamiento_residual = usuario.getFactor_residual() * 10f * (factor_entrenamiento_directo + nivel_asistentes.divide(new BigDecimal("16.25"), RoundingMode.HALF_UP).floatValue()) / 2f;
	
			float minutos = Math.min(this.minutos == null ? 0 : this.minutos, 100);
			float directo = minutos * factor_entrenamiento_directo;
	//		float residual = minutos * factor_entrenamiento_residual / 9f;	// Máximo: 11,11...
			float residual = minutos * factor_entrenamiento_residual / 9.45f;	// Máximo: 10.58
	
			if (demarcacion_entrenamiento == null && habilidad != TIPO_ENTRENAMIENTO.Condicion) {
				// Si no ha jugado ni se pide la condici�n, no devuelvo nada
				return 0f;
			} else {
				if (usuario.getTipo_entrenamiento(0).get(jornada) == habilidad) {
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
									return directo * 0.2f;
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
									return directo / 2f;			// Mejora la seleccionada y empeora MID (Greg: both formations (DEF/MID) will get half of training.)
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
							return 100f * factor_entrenamiento_residual / 9f - 5f;	// Residual para condici�n menos lo que baja de media por jornada
						case Porteria:
							if (demarcacion_entrenamiento == DEMARCACION.GK) {
								return residual;
							} else {
								return 0f;
							}
						case Anotacion:
							if (demarcacion_entrenamiento == DEMARCACION.ATT) {
								return residual;
							} else {
								return residual / 1.1f;	// El residual de anotaci�n parece ser menor si el jugador no es ATT
							}
						default: 
							return residual;
					}
				}
			}
		}
	}
	
	public String getEntrenamientoExtra() {
		return getEntrenamientoExtraNivel(TIPO_ENTRENAMIENTO.Condicion, null) / 80f
				+ "," +
				getEntrenamientoExtraNivel(TIPO_ENTRENAMIENTO.Rapidez, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(TIPO_ENTRENAMIENTO.Tecnica, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(TIPO_ENTRENAMIENTO.Pases, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(TIPO_ENTRENAMIENTO.Porteria, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(TIPO_ENTRENAMIENTO.Defensa, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(TIPO_ENTRENAMIENTO.Creacion, null) / 100f
				+ "," +
				getEntrenamientoExtraNivel(TIPO_ENTRENAMIENTO.Anotacion, null) / 100f;
	}
	
	public float getEntrenamientoExtraNivel(TIPO_ENTRENAMIENTO habilidad, Integer nivel) {
		Integer nivel_actual = getValor_habilidad(habilidad);
		
		if (nivel == null || nivel.equals(nivel_actual)) {
			float puntos_entrenamiento = lesion != null && lesion > 7 ? 0 : getMinutos_entrenamiento(habilidad);
			puntos_entrenamiento = puntos_entrenamiento / (float)Math.pow(usuario.getFactor_edad(habilidad), this.edad - 16);

			// NOTA: al acceder a esta función desde jugadores.tag, en los originales se pierde el usuario. No sé por qué
			// Lo apaño de esta forma
			if (original != null) {
				original.setUsuario(usuario);
			}
			
			return puntos_entrenamiento + (original == null ? 0f : original.getEntrenamientoExtraNivel(habilidad, nivel_actual));
		} else {
			return 0f;
		}
	}
	
	public List<Entrenamiento> getEntrenamientosTemporada2(TIPO_ENTRENAMIENTO habilidad, Entrenamiento entrenamiento_act) {
		List<Entrenamiento> entrenamientos = new ArrayList<Entrenamiento>();
		float puntos_entrenamiento = lesion != null && lesion > 7 ? 0 : getMinutos_entrenamiento(habilidad);

		// Mstyslav Pidchenko tenía las habilidades antiguas a null y fallaba aquí
		Integer valor = getValor_habilidad(habilidad);
		if (valor != null) {
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
				entrenamientos.addAll(original.getEntrenamientosTemporada2(habilidad, entrenamiento_act));
			} else if (entrenamiento_act.jornadas.size() > 0) {
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
		
		// Si el 1er entrenamiento est� cerrado por la izquierda o la temporada anterior no ten�a entrenamientos
		if (!entrenamientos.get(0).abierto_izq || entrenamientos_pretemporada == null) {
			return 0f;
		} else {
			float acumulado;
			// Si solo hay un entrenamiento y est� abierto por la izquierda
			if (entrenamientos_pretemporada.size() == 1 && entrenamientos_pretemporada.get(0).abierto_izq) {
				acumulado = puntos_pretemporada(total_entrenamientos, habilidad, edad - 1);
			} else {
				acumulado = 0f;
			}
			
			return acumulado + Util.sumar(entrenamientos_pretemporada.get(entrenamientos_pretemporada.size() - 1).puntos_entrenamiento) * (habilidad == TIPO_ENTRENAMIENTO.Condicion ? 1f : usuario.getFactor_edad(habilidad));
		}
	}
	
	public double getTalento_medio() {
		return talento != null ? talento : Math.max(3d, (Util.dnvl(talento_min) + Util.dnvl(talento_max)) / 2d);
	}
	
	/**
	 * Muestra los entrenamientos calculados en {@link #calcular_entrenamiento()}
	 * NOTA: copiada de #getEntrenamiento(TIPO_ENTRENAMIENTO habilidad, int edad)
	 * 
	 * @param habilidad
	 * @param edad
	 * @return
	 */
	public String getEntrenamiento(TIPO_ENTRENAMIENTO habilidad, int edad) {
		String gender = Util.getTexto(usuario.getLocale(), "skills.gender_" + habilidad.getIngles());
		List<Entrenamiento> entrenamientos = total_entrenamientos2.get(habilidad);
		String salida = "";

		if (entrenamientos != null) {
			if (edad == getMin_edad()) {
				Jugador j = entrenamientos.get(0).jornadas.get(entrenamientos.get(0).jornadas.size() - 1);
				if (j.getJornada() - 1 < AsistenteBO.JORNADA_NUEVO_ENTRENO) {
					salida += "<div title='" + Util.getTexto(usuario.getLocale(), "common.append") + "' class='nuevo' onclick='entrenamiento_click($(this), \"" + Util.escapeJS(nombre) + "\", " + 0 + ", " + 0 + ", " + (j.getJornada() - 1) + ", \"" + (j.demarcacion_entrenamiento == null ? "" : j.demarcacion_entrenamiento.name()) + "\", \"" + j.getUsuario_jornada().getTipo_entrenamiento(0).get(j.jornada) + "\", \"" + j.getUsuario_jornada().getDemarcacion().get(j.jornada) + "\", " + pid + ", " + entrenamientos.get(0).valor_habilidad + ", " + habilidad.ordinal() + ", true)'>+</div>";
				} else {
					Jugador entrenador = j.getUsuario_jornada().getEntrenador_principal().get(j.jornada);
					salida += "<div title='" + Util.getTexto(usuario.getLocale(), "common.append") + "' class='nuevo' onclick='entrenamiento_click($(this), \"" + Util.escapeJS(nombre) + "\", " + 0 + ", " + 0 + ", " + (j.getJornada() - 1) + ", \"" + (j.demarcacion_entrenamiento == null ? "" : j.demarcacion_entrenamiento.name()) + "\", null, null, " + pid + ", " + entrenamientos.get(0).valor_habilidad + ", " + habilidad.ordinal() + ", true, \"" + (entrenador == null ? "" : entrenador.getHabilidades()) + "\", " + j.getUsuario_jornada().getNivel_asistentes().get(j.jornada) + ", " + j.getUsuario_jornada().getNivel_juveniles().get(j.getJornada()) + ", " + j.isEntrenamiento_avanzado() + ", \"" + j.getUsuario_jornada().getTipo_entrenamiento(0).get(j.jornada) + "\", \"" + j.getUsuario_jornada().getTipo_entrenamiento(1).get(j.jornada) + "\", \"" + j.getUsuario_jornada().getTipo_entrenamiento(2).get(j.jornada) + "\", \"" + j.getUsuario_jornada().getTipo_entrenamiento(3).get(j.jornada) + "\")'>+</div>";
				}
			}

			// Puntos de entrenamientos efectivos - puntos de entrenamiento necesitados -> lo acumulamos en el siguiente entrenamiento
			float resto = 0F;
			int edad_resto = 0;
			for (Entrenamiento entrenamiento : entrenamientos) {
				try {
					float suma = entrenamiento.getPuntos(edad, habilidad);
					float exponente_edad = edad - 16;
					float exponente_habilidad = entrenamiento.valor_habilidad - habilidad.getFactor(usuario);
					int prediccion = habilidad == TIPO_ENTRENAMIENTO.Condicion ? 80 : (int)Math.round(getTalento_medio() * Math.pow(usuario.getFactor_edad(habilidad), exponente_edad) * Math.pow(usuario.getFactor_habilidad(), exponente_habilidad) * FACTOR_TALENTO * usuario.getFactor_talento());
					
					if (entrenamiento.jornadas.get(0).getEdad() >= edad && entrenamiento.jornadas.get(entrenamiento.jornadas.size() - 1).getEdad() <= edad) {
						String valor = Util.getTexto(usuario.getLocale(), "skills.skill" + entrenamiento.valor_habilidad + gender);
						salida += "<div>" + valor + "<br/><div class='" + (entrenamiento.abierto_izq || entrenamiento.jornadas.get(entrenamiento.jornadas.size() - 1).getEdad() != edad ? "abierto_izq" : "") + " " + (entrenamiento.abierto_der || entrenamiento.jornadas.get(0).getEdad() != edad ? "abierto_der" : "") + "'>";
	
						// Recorremos las jornadas en orden inverso, ya que es así como están almacenadas
						for (int i = entrenamiento.jornadas.size() - 1; i >= 0; i--) {
							Jugador j = entrenamiento.jornadas.get(i);
							if (j.getEdad() == edad) {
								Float f = entrenamiento.puntos_entrenamiento.get(i);
								if (entrenamiento.editable) {
									Jugador entrenador = j.getUsuario_jornada().getEntrenador_principal().get(j.jornada);
									BigDecimal asistentes = j.getUsuario_jornada().getNivel_asistentes().get(j.jornada);
									BigDecimal juveniles = j.getUsuario_jornada().getNivel_juveniles().get(j.jornada);

									if (j.getJornada() < AsistenteBO.JORNADA_NUEVO_ENTRENO) {
										salida += "<span onclick='entrenamiento_click($(this), \"" + Util.escapeJS(nombre) + "\", " + j.minutos + ", " + j.lesion + ", " + j.jornada + ", \"" + (j.demarcacion_entrenamiento == null ? "" : j.demarcacion_entrenamiento) + "\", \"" + j.getUsuario_jornada().getTipo_entrenamiento(0).get(j.jornada) + "\", \"" + j.getUsuario_jornada().getDemarcacion().get(j.jornada) + "\", " + pid + ", " + entrenamiento.valor_habilidad + ", " + habilidad.ordinal() + ", false, \"" + (entrenador == null ? "" : entrenador.getHabilidades()) + "\", " + asistentes + ", " + juveniles + ")' ";
									} else {
										salida += "<span onclick='entrenamiento_click($(this), \"" + Util.escapeJS(nombre) + "\", " + j.minutos + ", " + j.lesion + ", " + j.jornada + ", \"" + (j.demarcacion_entrenamiento == null ? "" : j.demarcacion_entrenamiento) + "\", null, null, " + pid + ", " + entrenamiento.valor_habilidad + ", " + habilidad.ordinal() + ", false, \"" + (entrenador == null ? "" : entrenador.getHabilidades()) + "\", " + asistentes + ", " + juveniles + ", " + j.isEntrenamiento_avanzado() + ", \"" + j.getUsuario_jornada().getTipo_entrenamiento(0).get(j.jornada) + "\", \"" + j.getUsuario_jornada().getTipo_entrenamiento(1).get(j.jornada) + "\", \"" + j.getUsuario_jornada().getTipo_entrenamiento(2).get(j.jornada) + "\", \"" + j.getUsuario_jornada().getTipo_entrenamiento(3).get(j.jornada) + "\")' ";
									}
									if (f == 0f) {
										salida += "class='perdido' title='0'>&nbsp;";
									} else if (f <= AsistenteBO.LIMITE_ENTRENAMIENTO_RESIDUAL) {
										salida += "class='residual' title='" + f.intValue() + "'>&nbsp;";
									} else if (f <= AsistenteBO.LIMITE_ENTRENAMIENTO_FORMACION) {
										salida += "class='medio' title='" + f.intValue() + "' style='width: " + (f/10f + 10f) + "px'>" + f.intValue();
									} else {
										salida += "class='normal' title='" + f.intValue() + "' style='width: " + (f/10f + 10f) + "px'>" + f.intValue();
									}
									salida += "</span>";
								} else {
									salida += "<span>&nbsp;</span>";
								}
							}
						}

						salida += "</div><br/>(" + (int)(suma + resto * Math.pow(usuario.getFactor_edad(habilidad), edad - edad_resto)) + " / " + prediccion + ")</div>";
					}

					if (habilidad != TIPO_ENTRENAMIENTO.Condicion) {
						float resto_actual = resto * (float)Math.pow(usuario.getFactor_edad(habilidad), entrenamiento.jornadas.get(0).getEdad() - edad_resto);
						resto = suma + resto_actual > prediccion ? suma + resto_actual - prediccion : 0F;
						// Limito el resto a los puntos de entrenamiento del último entrenamiento
						// NOTA: como el resto va aumentando en función de los años cumplidos, tengo que hacer lo mismo con los puntos del último entrenamiento
						float equivalente_ultimo_entrenamiento = entrenamiento.puntos_entrenamiento.get(0) * (float)Math.pow(usuario.getFactor_edad(habilidad), entrenamiento.jornadas.get(0).getEdad() - edad_resto);
						resto = Math.min(resto, equivalente_ultimo_entrenamiento);
						edad_resto = entrenamiento.jornadas.get(0).getEdad();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return salida;
	}
	
	// Añade jugadores recursivamente hasta rellenar las jornadas faltantes entre la actual y la del original
	public void anyadir_original(Jugador j) {
		if (j != null) {
			if (jornada - j.getJornada() > 1) {
				// Si el original existe y es de la semana anterior, le pasamos al jugador
				if (original != null && jornada - original.getJornada() == 1) {
					original.anyadir_original(j);
				} else {
					// Copiamos los valores del anterior, ya que no sabemos si han variado
					original = new Jugador(j);
					original.setJornada(jornada - 1);
					original.setDemarcacion_entrenamiento(j.getDemarcacion_entrenamiento());
					
					// Al pasar de la jornada 0 a la 15 (12) resto un año a la edad
					original.setEdad(edad - (original.jornada < AsistenteBO.JORNADA_NUEVO_SISTEMA_LIGAS && (original.jornada % 16 == 15) || original.jornada >= AsistenteBO.JORNADA_NUEVO_SISTEMA_LIGAS && (original.jornada % 13 == 12) ? 1 : 0));
		
					original.anyadir_original(j);
				}
			} else {
				original = j;
				original.setEdad(edad - (original.jornada < AsistenteBO.JORNADA_NUEVO_SISTEMA_LIGAS && (original.jornada % 16 == 0) || original.jornada >= AsistenteBO.JORNADA_NUEVO_SISTEMA_LIGAS && (original.jornada % 13 == 0) ? 1 : 0));
			}
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

/*	public void calcular_entrenamiento(Usuario usuario) {
		for (TIPO_ENTRENAMIENTO habilidad : TIPO_ENTRENAMIENTO.values()) {
			Integer valor = getValor_habilidad(habilidad);
	
			if (valor != null && original != null) {
				for (int edad = getMin_edad(); edad <= this.edad; edad++) {
					// Obtenemos los entrenamientos de la temporada
					Entrenamiento entrenamiento_act = new Entrenamiento(getValor_habilidad(habilidad));
					List<Entrenamiento> entrenamientos = original.getEntrenamientosTemporada(usuario, habilidad, edad, entrenamiento_act);
					Collections.reverse(entrenamientos);
					
					if (entrenamientos.size() > 0) {
						// Comprobamos si se cerr� el �ltimo entrenamiento de la temporada anterior
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
*/
	public void calcular_entrenamiento2() {
		for (TIPO_ENTRENAMIENTO habilidad : TIPO_ENTRENAMIENTO.values()) {
			Integer valor = getValor_habilidad(habilidad);
	
			if (valor != null) {
				// Obtenemos los entrenamientos de la habilidad
				Entrenamiento entrenamiento_act = new Entrenamiento(getValor_habilidad(habilidad));
				List<Entrenamiento> entrenamientos = original == null ? new ArrayList<Entrenamiento>() : original.getEntrenamientosTemporada2(habilidad, entrenamiento_act);
				Collections.reverse(entrenamientos);
//System.out.println(nombre + " " + entrenamientos.size());				
				// Si el último entrenamiento está cerrado, añadimos un entrenamiento vacío
				if (entrenamientos.size() == 0 || !entrenamientos.get(entrenamientos.size() - 1).abierto_der) {
					Entrenamiento actual = new Entrenamiento(valor);
					actual.abierto_izq = entrenamientos.size() == 0;
					actual.jornadas.add(this);
					actual.puntos_entrenamiento.add(0f);
					actual.editable = false;
					entrenamientos.add(actual);
				}
				
				total_entrenamientos2.put(habilidad, entrenamientos);
			}
		}
	}

	public void calcular_talento2() {
		talento_min = 0d;
		talento_max = 7d;

		for (TIPO_ENTRENAMIENTO habilidad : TIPO_ENTRENAMIENTO.values()) {
			if (habilidad != TIPO_ENTRENAMIENTO.Condicion) {
				List<Entrenamiento> entrenamientos = total_entrenamientos2.get(habilidad);
				if (entrenamientos != null) {
					int max = entrenamientos.size();
					for (int n = entrenamientos.size() - 1; n >= 0; n--) {
						// No contamos los entrenamientos a partir de los 28 porque ya no son fiables
						if (entrenamientos.get(n).jornadas.get(0).edad >= 28) {
							max--;
						} else {
							break;
						}
					}
					
					// Hacemos todas las combinaciones posibles de entrenamientos consecutivos
					for (int i = 0; i < max; i++) {
						for (int j = i; j < max; j++) {
							// Talento máximo
							//----------------
							// Sumamos de más, es decir, incluimos los entrenamientos inicial y final (que propiciaron las subidas)
	
							if (j > i && !entrenamientos.get(j).abierto_der) {
								// Del primer entrenamiento (i) solo nos interesa la última jornada, la de la subida
								Entrenamiento e = entrenamientos.get(i);
								float exponente_edad = e.jornadas.get(0).edad - 16;
								float exponente_habilidad = e.valor_habilidad - habilidad.getFactor(usuario);
								// El entrenamiento que propicia la subida lo cuento como un valor intermedio entre ambos niveles de la habilidad (este y el siguiente, de ahí el +0.5)
								// 25/08/2023: lo descarto. En niveles bajos podría causar un error incomprensible para el usuario
								// En su lugar le sumno 1 ya que lo contamos como que todo lo entrenado cuenta para la siguiente subida
								//exponente_habilidad += 0.5F;
								exponente_habilidad += 1F;
								float pe = (float) (e.puntos_entrenamiento.get(0) * Math.pow(usuario.getFactor_edad(habilidad), -exponente_edad) * Math.pow(usuario.getFactor_habilidad(), -exponente_habilidad) / (FACTOR_TALENTO * usuario.getFactor_talento()));	// 180 / 45 = 4
	
								for (int k = i + 1; k <= j; k++) {
									pe += entrenamientos.get(k).getPuntos(habilidad, true);
								}
	
								double media = pe / (j - i); 
								if (media < talento_max) {
									talento_max = media;
								}
							}
	
							// Talento mínimo
							//----------------
							// Sumamos solo lo seguro, es decir, excluimos el entrenamiento inicial y final
	
							{
								float pe = 0F;
		
								for (int k = i; k < j; k++) {
									pe += entrenamientos.get(k).getPuntos(habilidad, true);
								}
								
								pe += entrenamientos.get(j).getPuntos(habilidad, entrenamientos.get(j).abierto_der);
		
								double media = pe / (j - i + 1);
								if (media > talento_min) {
									talento_min = media;
								}
							}
						}
					}
				}
			}
		}
//System.out.println(nombre + " >> " + talento_min + " " + talento_max);
	}
	
/*	public void calcular_talento() {
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
									float exponente_edad = edad - 16;
									float exponente_habilidad = e.valor_habilidad - (habilidad == TIPO_ENTRENAMIENTO.Rapidez || habilidad == TIPO_ENTRENAMIENTO.Anotacion ? FACTOR_RAP_ANO : 6);
									float talento = (float) (Util.sumar(e.puntos_entrenamiento) * Math.pow(BASE_TALENTO_EDAD, -exponente_edad) * Math.pow(BASE_TALENTO_HABILIDAD, -exponente_habilidad) / FACTOR_TALENTO);	// 180 / 45 = 4
									float resta = (float) (e.puntos_entrenamiento.get(0) * Math.pow(BASE_TALENTO_EDAD, -exponente_edad) * Math.pow(BASE_TALENTO_HABILIDAD, -exponente_habilidad) / FACTOR_TALENTO);
		
									if (num_subidas == 0 && !e.abierto_der) {
										// Si el 1er entrenamiento est� cerrado por la derecha, restamos el �ltimo entrenamiento para comnprobar el talento m�nimo
										if (talento - resta > talento_min) {
											talento_min = talento - resta;
										}
										
										talento_min_acumulado = 0f;
										talento_max_acumulado = (float) (e.puntos_entrenamiento.get(0) * Math.pow(BASE_TALENTO_HABILIDAD, 1 - exponente_edad) * Math.pow(BASE_TALENTO_HABILIDAD, 1 - exponente_habilidad) / FACTOR_TALENTO);
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
*/
	public int getNum_jornadas() {
		return original == null ? 1 : jornada - original.getPrimera_jornada() + 1;
	}

	public int getPrimera_jornada() {
		return original == null ? jornada : original.getPrimera_jornada();
	}

	public String getDatos_grafica_valor() {
		return valor == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(RED, RED + "55", Util.getTexto(usuario.getLocale(), "players.original_value"), this, null) {
			@Override
			public Float getValor(Jugador j) {
				BigDecimal val = j.getValor_pais();
				return val == null ? null : val.floatValue();
			}
		},new Dataset(BLUE, BLUE + "55", Util.getTexto(usuario.getLocale(), "players.top_value"), this, null) {
			@Override
			public Float getValor(Jugador j) {
				BigDecimal val = j.getValor_pais();
				return val == null || j.forma == null ? null : val.divide(new BigDecimal(0.55F + (j.forma/18F) * 0.45F), RoundingMode.FLOOR).floatValue();
			}
		}}, null, Util.getTexto(getUsuario().getLocale(), "skills.value") + " - " + nombre);
	}

	public String getDatos_grafica_forma() {
		return forma == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(BLUE, BLUE + "55", null, this, null) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.forma);
			}
		}}, 18F, Util.getTexto(getUsuario().getLocale(), "skills.form") + " - " + nombre);
	}

	public String getDatos_grafica_condicion() {
		return condicion == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(null, null, null, this, TIPO_ENTRENAMIENTO.Condicion) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.condicion);
			}
		},	crearDataset(GREEN, GREEN + "55", Util.getTexto(usuario.getLocale(), "training.advanced_training")),
			crearDataset(ORANGE, ORANGE + "55", Util.getTexto(usuario.getLocale(), "training.formation_training")),
			crearDataset(RED, RED + "55", Util.getTexto(usuario.getLocale(), "training.general_training"))
		}, 11F, Util.getTexto(getUsuario().getLocale(), "skills.stamina") + " - " + nombre);
	}

	public String getDatos_grafica_rapidez() {
		return rapidez == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(null, null, null, this, TIPO_ENTRENAMIENTO.Rapidez) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.rapidez);
			}
		},	crearDataset(GREEN, GREEN + "55", Util.getTexto(usuario.getLocale(), "training.advanced_training")),
			crearDataset(ORANGE, ORANGE + "55", Util.getTexto(usuario.getLocale(), "training.formation_training")),
			crearDataset(RED, RED + "55", Util.getTexto(usuario.getLocale(), "training.general_training"))
		}, 18F, Util.getTexto(getUsuario().getLocale(), "skills.pace") + " - " + nombre);
	}

	public String getDatos_grafica_tecnica() {
		return tecnica == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(null, null, null, this, TIPO_ENTRENAMIENTO.Tecnica) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.tecnica);
			}
		},	crearDataset(GREEN, GREEN + "55", Util.getTexto(usuario.getLocale(), "training.advanced_training")),
			crearDataset(ORANGE, ORANGE + "55", Util.getTexto(usuario.getLocale(), "training.formation_training")),
			crearDataset(RED, RED + "55", Util.getTexto(usuario.getLocale(), "training.general_training"))
		}, 18F, Util.getTexto(getUsuario().getLocale(), "skills.technique") + " - " + nombre);
	}
	
	public String getDatos_grafica_pases() {
		return pases == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(null, null, null, this, TIPO_ENTRENAMIENTO.Pases) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.pases);
			}
		},	crearDataset(GREEN, GREEN + "55", Util.getTexto(usuario.getLocale(), "training.advanced_training")),
			crearDataset(ORANGE, ORANGE + "55", Util.getTexto(usuario.getLocale(), "training.formation_training")),
			crearDataset(RED, RED + "55", Util.getTexto(usuario.getLocale(), "training.general_training"))
		}, 18F, Util.getTexto(getUsuario().getLocale(), "skills.passing") + " - " + nombre);
	}

	public String getDatos_grafica_porteria() {
		return porteria == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(null, null, null, this, TIPO_ENTRENAMIENTO.Porteria) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.porteria);
			}
		},	crearDataset(GREEN, GREEN + "55", Util.getTexto(usuario.getLocale(), "training.advanced_training")),
			crearDataset(ORANGE, ORANGE + "55", Util.getTexto(usuario.getLocale(), "training.formation_training")),
			crearDataset(RED, RED + "55", Util.getTexto(usuario.getLocale(), "training.general_training"))
		}, 18F, Util.getTexto(getUsuario().getLocale(), "skills.keeper") + " - " + nombre);
	}

	public String getDatos_grafica_defensa() {
		return defensa == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(null, null, null, this, TIPO_ENTRENAMIENTO.Defensa) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.defensa);
			}
		},	crearDataset(GREEN, GREEN + "55", Util.getTexto(usuario.getLocale(), "training.advanced_training")),
			crearDataset(ORANGE, ORANGE + "55", Util.getTexto(usuario.getLocale(), "training.formation_training")),
			crearDataset(RED, RED + "55", Util.getTexto(usuario.getLocale(), "training.general_training"))
		}, 18F, Util.getTexto(getUsuario().getLocale(), "skills.defender") + " - " + nombre);
	}
	
	public String getDatos_grafica_creacion() {
		return creacion == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(null, null, null, this, TIPO_ENTRENAMIENTO.Creacion) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.creacion);
			}
		},	crearDataset(GREEN, GREEN + "55", Util.getTexto(usuario.getLocale(), "training.advanced_training")),
			crearDataset(ORANGE, ORANGE + "55", Util.getTexto(usuario.getLocale(), "training.formation_training")),
			crearDataset(RED, RED + "55", Util.getTexto(usuario.getLocale(), "training.general_training"))
		}, 18F, Util.getTexto(getUsuario().getLocale(), "skills.playmaker") + " - " + nombre);
	}

	public String getDatos_grafica_anotacion() {
		return anotacion == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(null, null, null, this, TIPO_ENTRENAMIENTO.Anotacion) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.anotacion);
			}
		},	crearDataset(GREEN, GREEN + "55", Util.getTexto(usuario.getLocale(), "training.advanced_training")),
			crearDataset(ORANGE, ORANGE + "55", Util.getTexto(usuario.getLocale(), "training.formation_training")),
			crearDataset(RED, RED + "55", Util.getTexto(usuario.getLocale(), "training.general_training"))
		}, 18F, Util.getTexto(getUsuario().getLocale(), "skills.striker") + " - " + nombre);
	}

	public String getDatos_grafica_suma_habilidades() {
		return anotacion == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(BLUE, BLUE + "55", null, this, null) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.getSuma_habilidades());
			}
		}}, null, Util.getTexto(getUsuario().getLocale(), "skills.sumskills") + " - " + nombre);
	}

	public String getDatos_grafica_experiencia() {
		return experiencia == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(BLUE, BLUE + "55", null, this, null) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.experiencia);
			}
		}}, 18F, Util.getTexto(getUsuario().getLocale(), "skills.experience") + " - " + nombre);
	}

	public String getDatos_grafica_disciplina_tactica() {
		return disciplina_tactica == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(BLUE, BLUE + "55", null, this, null) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.disciplina_tactica);
			}
		}}, 18F, Util.getTexto(getUsuario().getLocale(), "skills.tactical_discipline") + " - " + nombre);
	}

	public String getDatos_grafica_trabajo_en_equipo() {
		return trabajo_en_equipo == null ? "null" : getDatos_graficas(new Dataset[] {new Dataset(BLUE, BLUE + "55", null, this, null) {
			@Override
			public Float getValor(Jugador j) {
				return Util.integerToFloat(j.trabajo_en_equipo);
			}
		}}, 18F, Util.getTexto(getUsuario().getLocale(), "skills.team_work") + " - " + nombre);
	}

	public String getDatos_graficas(Dataset[] datasets, Float max_valor, String titulo) {
		float max_valor2 = 0;

		String resp = "{ \"datasets\":[";
		for (int i = 0; i < datasets.length; i++) {
			resp += "{ \"data\": " + datasets[i].data.toString() + ", "
				+ "\"labels\": " + datasets[i].labels.toString() + ", "
				+ "\"borderColor\": " + datasets[i].borderColor.toString() + ", "
				+ "\"backgroundColor\": " + datasets[i].backgroundColor + ", "
				+ "\"label\": " + datasets[i].label
				+ " }";
			if (i < datasets.length - 1) {
				resp += ",";
			}
			max_valor2 = Math.max(max_valor2, datasets[i].max_valor);
		}
		resp +=	"],"
				+ "\"max_valor\": " + (max_valor != null ? max_valor : max_valor2) + ","
				+ "\"titulo\": \"" + titulo + "\"}";
		
		return resp;
	}

	private String getColor_entrenamiento(TIPO_ENTRENAMIENTO entrenamiento) {
		// NOTA: Ojo con las selecciones
		HashMap<Integer, TIPO_ENTRENAMIENTO> hm = demarcacion_entrenamiento == null ? null : usuario.getTipo_entrenamiento(jornada < AsistenteBO.JORNADA_NUEVO_ENTRENO ? 0 : demarcacion_entrenamiento.ordinal());
		if (entrenamiento == null || hm == null || lesion != null && lesion > 7 || getMinutos_entrenamiento(entrenamiento) == 0) {
			return GRAY;
		} else if (hm.get(jornada) == entrenamiento) {
			if (getMinutos_entrenamiento(entrenamiento) <= AsistenteBO.LIMITE_ENTRENAMIENTO_RESIDUAL) {
				return RED;
			} else if (getMinutos_entrenamiento(entrenamiento) <= AsistenteBO.LIMITE_ENTRENAMIENTO_FORMACION) {
				return ORANGE;
			} else {
				return GREEN;
			}
		} else {
			return RED;
		}
	}
	
	public String getColor_demarcacion() {
		if (demarcacion != null) {
			switch (demarcacion) {
				case GK:	return "SkyBlue";
				case DEF:	return "lightgreen";
				case MID:	return "orange";
				case ATT:	return "salmon";
				default:
			}
		}
		return "silver";
	}

	public void insertar_inicio(Jugador j) {
		if (original == null) {
			original = j;
			j.setJornada(jornada - 1);
			// Si comparo con la jornada 15, a la semana 0 se le resta un a�o. �Por qu�? Pues no lo s� pero as� funciona bien
			j.setEdad(edad - (j.getJornada() < AsistenteBO.JORNADA_NUEVO_SISTEMA_LIGAS && j.getJornada() % 16 == 14 || j.getJornada() >= AsistenteBO.JORNADA_NUEVO_SISTEMA_LIGAS && (j.getJornada() - AsistenteBO.JORNADA_NUEVO_SISTEMA_LIGAS) % AsistenteBO.JORNADAS_TEMPORADA == 11 ? 1 : 0));
			j.setValor(valor);
			j.setCondicion(condicion);
			j.setRapidez(rapidez);
			j.setTecnica(tecnica);
			j.setPases(pases);
			j.setPorteria(porteria);
			j.setDefensa(defensa);
			j.setCreacion(creacion);
			j.setAnotacion(anotacion);
		} else {
			original.insertar_inicio(j);
		}
	}

	// Resetea los minutos desde la última actualización
	// y actualizo la demarcación en jornadas posteriores a la de la última actualización
	public void prepararacion_entrenamiento(DEMARCACION demarcacion, boolean avanzado) {
		if (jornada >= usuario.getDef_jornada()) {
			minutos = avanzado ? 50f : 0F;
			if (demarcacion != null) {
				demarcacion_entrenamiento = demarcacion;
				entrenamiento_avanzado = avanzado;
			}
			if (original != null) {
				original.prepararacion_entrenamiento(demarcacion_entrenamiento, entrenamiento_avanzado);
			}
		}
	}

	// Añade una línea a las notas del jugador
	public void addNotas(String notas) {
		if (notas != null) {
			this.notas = Util.dateToString(new Date()) + ": " + notas + (this.notas == null ? "" : "\n" + this.notas);
		}
	}

	/** CAMPOS CALCULADOS **/

	private int sumskills(int factor_rapidez, int factor_tecnica, int factor_pases, int factor_porteria, int factor_defensa, int factor_creacion, int factor_anotacion) {
		return (Util.invl(rapidez)*factor_rapidez + Util.invl(tecnica)*factor_tecnica + Util.invl(pases)*factor_pases + Util.invl(porteria)*factor_porteria + Util.invl(defensa)*factor_defensa + Util.invl(creacion)*factor_creacion + Util.invl(anotacion)*factor_anotacion) / 100;
	}
	
	public Integer getSuma_habilidades() {
		if (demarcacion != null) {
			switch (demarcacion) {
				case GK:	return sumskills(usuario.getSumskills_gk_rapidez(), usuario.getSumskills_gk_tecnica(), usuario.getSumskills_gk_pases(), usuario.getSumskills_gk_porteria(), usuario.getSumskills_gk_defensa(), usuario.getSumskills_gk_creacion(), usuario.getSumskills_gk_anotacion());
				case DEF:	return sumskills(usuario.getSumskills_def_rapidez(), usuario.getSumskills_def_tecnica(), usuario.getSumskills_def_pases(), usuario.getSumskills_def_porteria(), usuario.getSumskills_def_defensa(), usuario.getSumskills_def_creacion(), usuario.getSumskills_def_anotacion());
				case MID:	return sumskills(usuario.getSumskills_mid_rapidez(), usuario.getSumskills_mid_tecnica(), usuario.getSumskills_mid_pases(), usuario.getSumskills_mid_porteria(), usuario.getSumskills_mid_defensa(), usuario.getSumskills_mid_creacion(), usuario.getSumskills_mid_anotacion());
				case ATT:	return sumskills(usuario.getSumskills_att_rapidez(), usuario.getSumskills_att_tecnica(), usuario.getSumskills_att_pases(), usuario.getSumskills_att_porteria(), usuario.getSumskills_att_defensa(), usuario.getSumskills_att_creacion(), usuario.getSumskills_att_anotacion());
				default:
			}
		}
		return sumskills(100, 100, 100, 100, 100, 100, 100);
	}

	public String getStr_bbcode() {
		try {
			return "[b][pid=" + pid + "]" + nombre + "[/pid][/b], " + Util.getTexto(usuario.getLocale(), "skills.age").toLowerCase() + ": [b]" + edad + "[/b]\n" +
					Util.getTexto(usuario.getLocale(), "common.club") + ": [tid=" + tid + "]" + equipo + "[/tid], " + Util.getTexto(usuario.getLocale(), "common.country").toLowerCase() + ": [url=country/ID_country/" + pais + "]" + NtdbBO.paises[pais - 1] + "[/url]\n" +
					Util.getTexto(usuario.getLocale(), "skills.value").toLowerCase() + ": " + getValor_bbcode() + ", " + Util.getTexto(usuario.getLocale(), "skills.wage").toLowerCase() + ": [money=" + usuario.getCountryID() + "]" + getSalario_pais() + "[/money]\n" +
					getHabilidad_bbcode(forma, null/*original == null ? null : original.forma*/, "form", false) + " " + Util.getTexto(usuario.getLocale(), "skills.form").toLowerCase() + ", " +
					getHabilidad_bbcode(disciplina_tactica, null/*original == null ? null : original.disciplina_tactica*/, "tactical_discipline", false) + " " + Util.getTexto(usuario.getLocale(), "skills.tactical_discipline").toLowerCase() + "\n" +
					Util.getTexto(usuario.getLocale(), "skills.height").toLowerCase() + ": [b]" + altura + "[/b] cm, " + Util.getTexto(usuario.getLocale(), "skills.weight").toLowerCase() + ": [b]" + (peso/10.0) + "[/b] kg, " + Util.getTexto(usuario.getLocale(), "skills.BMI") + ": [b]" + (IMC/100.0) + "[/b]\n\n" +
	
					getHabilidad_bbcode(condicion, original == null ? null : original.condicion, "stamina", false) + " " + Util.getTexto(usuario.getLocale(), "skills.stamina").toLowerCase() + ", " +
					getHabilidad_bbcode(porteria, original == null ? null : original.porteria, "keeper", false) + " " + Util.getTexto(usuario.getLocale(), "skills.keeper").toLowerCase() + "\n" +
					getHabilidad_bbcode(rapidez, original == null ? null : original.rapidez, "pace", false) + " " + Util.getTexto(usuario.getLocale(), "skills.pace").toLowerCase() + ", " +
					getHabilidad_bbcode(defensa, original == null ? null : original.defensa, "defender", false) + " " + Util.getTexto(usuario.getLocale(), "skills.defender").toLowerCase() + "\n" +
					getHabilidad_bbcode(tecnica, original == null ? null : original.tecnica, "technique", false) + " " + Util.getTexto(usuario.getLocale(), "skills.technique").toLowerCase() + ", " +
					getHabilidad_bbcode(creacion, original == null ? null : original.creacion, "playmaker", false) + " " + Util.getTexto(usuario.getLocale(), "skills.playmaker").toLowerCase() + "\n" +
					getHabilidad_bbcode(pases, original == null ? null : original.pases, "passing", false) + " " + Util.getTexto(usuario.getLocale(), "skills.passing").toLowerCase() + ", " +
					getHabilidad_bbcode(anotacion, original == null ? null : original.anotacion, "striker", false) + " " + Util.getTexto(usuario.getLocale(), "skills.striker").toLowerCase() + "\n";
		} catch (Exception e) {
			e.printStackTrace();
			return "Missing data";
		}
	}

	// Se usa para mostrar el entrenador tanto en el entrenamiento individual como en el menú
	public String getHabilidades() {
		return getCondicion() + "," + getRapidez() + "," + getTecnica() + "," + getPases() + "," + getPorteria() + "," + getDefensa() + "," + getCreacion() + "," + getAnotacion();
	}

	@Override
	public String toString() {
		return nombre + " [" + pid + "]";
	}
	
	public BigDecimal getValor_pais() {
		if (valor == null) {
			return null;
		} else if (usuario == null || usuario.getCountryID() == null || usuario.getCountryID().equals(0)) {
			return new BigDecimal(valor);
		} else {
			// El valor está dividido por 4 por razones de compatibilidad, por eso ahora lo multiplicamos
			return new BigDecimal(valor * 4).divide(Pais.currency_rates[usuario.getCountryID() - 1], RoundingMode.FLOOR);
		}
	}

	public Integer getValor_original() {
		if (valor == null) {
			return null;
		} else if (usuario == null || usuario.getCountryID() == null || usuario.getCountryID().equals(0)) {
			return valor;
		} else {
			// El valor está en euros, calculamos el valor original
			return new BigDecimal(valor * 4).divide(Pais.currency_rates[usuario.getCountryID() - 1], RoundingMode.FLOOR).intValue();
		}
	}

	// El salario ahora está en la moneda del usuario
	// A diferencia del valor, no guardamos hist�rico, por lo que no hace falta convertir nada
	public BigDecimal getSalario_pais() {
/*		if (salario == null) {
			return null;
		} else if (usuario.getCountryID() == null || usuario.getCountryID().equals(0)) {
			return new BigDecimal(salario);
		} else {
			return new BigDecimal(salario).divide(Pais.currency_rates[usuario.getCountryID() - 1], RoundingMode.FLOOR);
		}
*/
		return salario == null ? null : new BigDecimal(salario);
	}
	
	public float getPuntos_entrenamiento() {
		if (isEntrenamiento_avanzado()) {
			return getMinutos() == 0 ? 0f : getMinutos() < AsistenteBO.PORCENTAJE_OFICIALES_AVANZADO ? (getMinutos() - 50) * 2 : AsistenteBO.PORCENTAJE_OFICIALES_AVANZADO + (getMinutos() - AsistenteBO.PORCENTAJE_OFICIALES_AVANZADO) * (186f - AsistenteBO.PORCENTAJE_OFICIALES_AVANZADO) / (100f - AsistenteBO.PORCENTAJE_OFICIALES_AVANZADO);
		} else {
			return getMinutos() < AsistenteBO.PORCENTAJE_OFICIALES ? getMinutos() : AsistenteBO.PORCENTAJE_OFICIALES + (getMinutos() - AsistenteBO.PORCENTAJE_OFICIALES) * (186f - AsistenteBO.PORCENTAJE_OFICIALES) / (100f - AsistenteBO.PORCENTAJE_OFICIALES);
		}
	}

	// Devuelve el usuario correspondiente en la jornada indicada
	// Si se trata de un jugador de prueba, el usuario será usuario2
	// Si se trata de un jugador traspasado, el usuario tras el traspaso será el actual y antes del traspaso será usuario2
	public Usuario getUsuario_jornada() {
		if (usuario2 != null && (pid <= 0 || jornada_traspaso != null && jornada < jornada_traspaso)) {
			return usuario2;
		} else {
			return usuario;
		}
	}

	public List<Jugador> getLista_originales() {
		List<Jugador> lista = new ArrayList<>();
		Jugador it = original;
		while (it != null) {
			lista.add(it);
			it = it.original;
		}
		return lista;
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

	public Float getMinutos() {
		return minutos;
	}

	public void setMinutos(Float minutos) {
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

	public double getTalento_min() {
		return talento_min;
	}

	public void setTalento_min(Double talento_min) {
		this.talento_min = talento_min;
	}

	public double getTalento_max() {
		return talento_max;
	}

	public void setTalento_max(Double talento_max) {
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

	public Integer getSalario() {
		return salario;
	}

	public void setSalario(Integer salario) {
		this.salario = salario;
	}

	public Integer getExperiencia() {
		return experiencia;
	}

	public void setExperiencia(Integer experiencia) {
		this.experiencia = experiencia;
	}

	public Integer getTrabajo_en_equipo() {
		return trabajo_en_equipo;
	}

	public void setTrabajo_en_equipo(Integer trabajo_en_equipo) {
		this.trabajo_en_equipo = trabajo_en_equipo;
	}

	public Integer getAltura() {
		return altura;
	}

	public void setAltura(Integer altura) {
		this.altura = altura;
	}

	public Integer getPeso() {
		return peso;
	}

	public void setPeso(Integer peso) {
		this.peso = peso;
	}

	public Integer getIMC() {
		return IMC;
	}

	public void setIMC(Integer iMC) {
		IMC = iMC;
	}

	public Double getTalento() {
		return talento;
	}

	public void setTalento(Double talento) {
		this.talento = talento;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public void setUsuario_recursivo(Usuario usuario) {
		this.usuario = usuario;
		if (original != null) {
			original.setUsuario_recursivo(usuario);
		}
	}

	public boolean isDestacar() {
		return destacar;
	}

	public void setDestacar(boolean destacar) {
		this.destacar = destacar;
	}

	public String getLogin_duenyo() {
		return login_duenyo;
	}

	public void setLogin_duenyo(String login_duenyo) {
		this.login_duenyo = login_duenyo;
	}

	public Juvenil getJuvenil() {
		return juvenil;
	}

	public void setJuvenil(Juvenil juvenil) {
		this.juvenil = juvenil;
	}

	public boolean isEntrenamiento_avanzado() {
		return entrenamiento_avanzado;
	}

	public void setEntrenamiento_avanzado(boolean entrenamiento_avanzado) {
		this.entrenamiento_avanzado = entrenamiento_avanzado;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}

	public void setUsuario2(Usuario usuario2) {
		this.usuario2 = usuario2;
	}

	public Usuario getUsuario2() {
		return usuario2;
	}
}
