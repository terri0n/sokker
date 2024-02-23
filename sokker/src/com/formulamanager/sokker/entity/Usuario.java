package com.formulamanager.sokker.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.EquipoBO.TIPO_ENTRENAMIENTO;
import com.formulamanager.sokker.entity.Jugador.DEMARCACION;

public class Usuario {
	private String login;
	private String password;
	private String login_sokker;
	private Integer tid;
	private Integer tid_nt;
	private Integer def_tid;		// Equipo por defecto
	private String equipo;
	private String equipo_nt;
	private Integer jornada;		// Jornada última actualización
	private Integer jornada_nt;		// Jornada última actualización NT
	private Integer intentos_fallidos;
	private boolean ntdb;
	private String locale;
	private Integer countryID;
	private boolean recibir_ntdb;
	
	// Entrenamiento por jornada
	private HashMap<Integer, TIPO_ENTRENAMIENTO> tipo_entrenamiento[];
	private HashMap<Integer, DEMARCACION> demarcacion;	// Esto ya no se usará en el nuevo entrenamiento
	private HashMap<Integer, Jugador> entrenador_principal;
	private HashMap<Integer, BigDecimal> nivel_asistentes;
	private HashMap<Integer, BigDecimal> nivel_juveniles;

	// Config entrenamiento
	private boolean numeros;		// Mostrar números en desplegables de habilidades
	private int numero_decimales = 2;

	// Config habilidades
	private float factor_edad;
	private float factor_edad_rapidez;
	private float factor_habilidad;
	private float factor_talento;
	private float factor_residual;
	private float factor_formacion;
	private float factor_rapidez;
	private float factor_tecnica;
	private float factor_pases;
	private float factor_porteria;
	private float factor_defensa;
	private float factor_creacion;
	private float factor_anotacion;

	// Config jugadores
	private boolean mostrar_salario;
	private boolean mostrar_experiencia;
	private boolean mostrar_disciplina_tactica;
	private boolean mostrar_trabajo_en_equipo;
	private boolean mostrar_altura;
	private boolean mostrar_peso;
	private boolean mostrar_IMC;
	private boolean mostrar_banquillo;
	private boolean mostrar_suma_habilidades;

	private int sumskills_gk_rapidez;
	private int sumskills_gk_tecnica;
	private int sumskills_gk_pases;
	private int sumskills_gk_porteria;
	private int sumskills_gk_defensa;
	private int sumskills_gk_creacion;
	private int sumskills_gk_anotacion;

	private int sumskills_def_rapidez;
	private int sumskills_def_tecnica;
	private int sumskills_def_pases;
	private int sumskills_def_porteria;
	private int sumskills_def_defensa;
	private int sumskills_def_creacion;
	private int sumskills_def_anotacion;

	private int sumskills_mid_rapidez;
	private int sumskills_mid_tecnica;
	private int sumskills_mid_pases;
	private int sumskills_mid_porteria;
	private int sumskills_mid_defensa;
	private int sumskills_mid_creacion;
	private int sumskills_mid_anotacion;

	private int sumskills_att_rapidez;
	private int sumskills_att_tecnica;
	private int sumskills_att_pases;
	private int sumskills_att_porteria;
	private int sumskills_att_defensa;
	private int sumskills_att_creacion;
	private int sumskills_att_anotacion;
	
	// Config scouts
	private String scouts;
	private HashMap<String, Usuario> scout_de = new HashMap<String, Usuario>();
	
	private String notas;
	private String actualizacion_automatica;	// Guarda la contraseña sin cifrar para realizar las actualizaciones automáticamente
	
	/************
	 * Funciones
	 ************/
	public Usuario(Integer tid, String equipo) {
		this.tid = tid;
		this.def_tid = tid;
		this.equipo = equipo;

		tipo_entrenamiento = new HashMap[4];
		tipo_entrenamiento[0] = new HashMap<Integer, TIPO_ENTRENAMIENTO>();
		tipo_entrenamiento[1] = new HashMap<Integer, TIPO_ENTRENAMIENTO>();
		tipo_entrenamiento[2] = new HashMap<Integer, TIPO_ENTRENAMIENTO>();
		tipo_entrenamiento[3] = new HashMap<Integer, TIPO_ENTRENAMIENTO>();
		
		demarcacion = new HashMap<Integer, DEMARCACION>();
		entrenador_principal = new HashMap<Integer, Jugador>();
		nivel_asistentes = new HashMap<Integer, BigDecimal>();
		nivel_juveniles = new HashMap<Integer, BigDecimal>();
	}

	// Crea un usuario2 para un jugador
	public Usuario(String login, Usuario usuario) {
		this((Integer)null, (String)null);
		this.login = login;
		countryID = usuario.getCountryID();
		tid = usuario.getTid();
		def_tid = tid;
		jornada = usuario.getJornada();
		intentos_fallidos = 0;
	}

	public Usuario(String login_sokker, String login, String password) {
		this.setLogin_sokker(login_sokker);
		this.login = login;
		this.password = password;
	}

	public Usuario(List<String> valores) {
		int i = 0;
		// Valores por defecto
		locale = "EN";
		
		login = valores.get(i++).toLowerCase();
		password = valores.get(i++);
		login_sokker = valores.get(i++);
		tid = Util.stringToInteger(valores.get(i++));
		tid_nt = Util.stringToInteger(valores.get(i++));
		def_tid = Util.stringToInteger(valores.get(i++));
		equipo = valores.get(i++);
		equipo_nt = valores.get(i++);
		jornada = Util.stringToInteger(valores.get(i++));
		jornada_nt = Util.stringToInteger(valores.get(i++));
		if (valores.get(i).equals("*")) {
			intentos_fallidos = 0;
		} else {
			intentos_fallidos = Util.stringToInteger(valores.get(i++));
			if (!valores.get(i).equals("*")) {
				ntdb = Util.stringToBoolean(valores.get(i++));
				if (!valores.get(i).equals("*")) {
					if (!valores.get(i++).equals("null")) {
						locale = valores.get(i-1);
					}
					if (!valores.get(i).equals("*")) {
						countryID = Util.stringToInteger(valores.get(i++));
						numeros = Util.stringToBoolean(valores.get(i++));
						if (!valores.get(i).equals("*")) {
							recibir_ntdb = Util.stringToBoolean(valores.get(i++));
							if (!valores.get(i).equals("*")) {
								i++;//porcentaje_oficiales_viejo = Util.stringToInteger(valores.get(i++));
								i++;//porcentaje_amistosos_viejo = Util.stringToInteger(valores.get(i++));
								if (valores.get(i).charAt(0) == '-') {
									factor_edad = Util.stringToFloat(valores.get(i++).substring(1));
									factor_habilidad = Util.stringToFloat(valores.get(i++));
									factor_talento = Util.stringToFloat(valores.get(i++));
									factor_residual = Util.stringToFloat(valores.get(i++));
									if (valores.get(i).charAt(0) == '-') {
										factor_formacion = Util.stringToFloat(valores.get(i++).substring(1));
									} else {
										// Si todavía no se ha guardado el factor_formación, establezco el factor_residual a un 15%
										inicializar_residual_formacion();
									}

									// Fuerzo el cambio a quienes no lo hayan cambiado
									if (factor_formacion == 1f && factor_residual == 1f) {
										inicializar_residual_formacion();
									}

									factor_rapidez = Util.stringToFloat(valores.get(i++));
									factor_tecnica = Util.stringToFloat(valores.get(i++));
									factor_pases = Util.stringToFloat(valores.get(i++));
									factor_porteria = Util.stringToFloat(valores.get(i++));
									factor_defensa = Util.stringToFloat(valores.get(i++));
									factor_creacion = Util.stringToFloat(valores.get(i++));
									factor_anotacion = Util.stringToFloat(valores.get(i++));
									sumskills_gk_rapidez = Util.stringToInteger(valores.get(i++));
									sumskills_def_rapidez = Util.stringToInteger(valores.get(i++));
									sumskills_mid_rapidez = Util.stringToInteger(valores.get(i++));
									sumskills_att_rapidez = Util.stringToInteger(valores.get(i++));
									sumskills_gk_tecnica = Util.stringToInteger(valores.get(i++));
									sumskills_def_tecnica = Util.stringToInteger(valores.get(i++));
									sumskills_mid_tecnica = Util.stringToInteger(valores.get(i++));
									sumskills_att_tecnica = Util.stringToInteger(valores.get(i++));
									sumskills_gk_pases = Util.stringToInteger(valores.get(i++));
									sumskills_def_pases = Util.stringToInteger(valores.get(i++));
									sumskills_mid_pases = Util.stringToInteger(valores.get(i++));
									sumskills_att_pases = Util.stringToInteger(valores.get(i++));
									sumskills_gk_porteria = Util.stringToInteger(valores.get(i++));
									sumskills_def_porteria = Util.stringToInteger(valores.get(i++));
									sumskills_mid_porteria = Util.stringToInteger(valores.get(i++));
									sumskills_att_porteria = Util.stringToInteger(valores.get(i++));
									sumskills_gk_defensa = Util.stringToInteger(valores.get(i++));
									sumskills_def_defensa = Util.stringToInteger(valores.get(i++));
									sumskills_mid_defensa = Util.stringToInteger(valores.get(i++));
									sumskills_att_defensa = Util.stringToInteger(valores.get(i++));
									sumskills_gk_creacion = Util.stringToInteger(valores.get(i++));
									sumskills_def_creacion = Util.stringToInteger(valores.get(i++));
									sumskills_mid_creacion = Util.stringToInteger(valores.get(i++));
									sumskills_att_creacion = Util.stringToInteger(valores.get(i++));
									sumskills_gk_anotacion = Util.stringToInteger(valores.get(i++));
									sumskills_def_anotacion = Util.stringToInteger(valores.get(i++));
									sumskills_mid_anotacion = Util.stringToInteger(valores.get(i++));
									sumskills_att_anotacion = Util.stringToInteger(valores.get(i++));
								} else {
									i += 2;	// Nos saltamos los valores de porcentaje_oficiales_nuevo y porcentaje_amistosos_nuevo
									inicializar();
								}
								mostrar_salario = Util.stringToBoolean(valores.get(i++));
								mostrar_experiencia = Util.stringToBoolean(valores.get(i++));
								mostrar_disciplina_tactica = Util.stringToBoolean(valores.get(i++));
								mostrar_trabajo_en_equipo = Util.stringToBoolean(valores.get(i++));
								mostrar_altura = Util.stringToBoolean(valores.get(i++));
								mostrar_peso = Util.stringToBoolean(valores.get(i++));
								mostrar_IMC = Util.stringToBoolean(valores.get(i++));
								mostrar_banquillo = Util.stringToBoolean(valores.get(i++));
								mostrar_suma_habilidades = Util.stringToBoolean(valores.get(i++));
								if (!valores.get(i).equals("*")) {
									actualizacion_automatica = Util.nnvl(valores.get(i++));
									if (!valores.get(i).equals("*")) {
										factor_edad_rapidez = Util.stringToFloat(valores.get(i++));
									} else {
										factor_edad_rapidez = 1.1f;
									}
								}
							}
						}
					}
				}
			}
		}

		tipo_entrenamiento = new HashMap[4];
		tipo_entrenamiento[0] = new HashMap<Integer, TIPO_ENTRENAMIENTO>();
		tipo_entrenamiento[1] = new HashMap<Integer, TIPO_ENTRENAMIENTO>();
		tipo_entrenamiento[2] = new HashMap<Integer, TIPO_ENTRENAMIENTO>();
		tipo_entrenamiento[3] = new HashMap<Integer, TIPO_ENTRENAMIENTO>();

		demarcacion = new HashMap<Integer, DEMARCACION>();
		entrenador_principal = new HashMap<Integer, Jugador>();
		nivel_asistentes = new HashMap<Integer, BigDecimal>();
		nivel_juveniles = new HashMap<Integer, BigDecimal>();
	}
	
	public String serializar() {
		List<String> valores = new ArrayList<String>();

		valores.add(login.toLowerCase());
		valores.add(password);
		valores.add(login_sokker);
		valores.add(tid+"");
		valores.add(Util.nvl(Util.integerToString(tid_nt)));
		valores.add(def_tid+"");
		valores.add(equipo);
		valores.add(Util.nvl(equipo_nt));
		valores.add(jornada+"");
		valores.add(Util.nvl(Util.integerToString(jornada_nt)));
		valores.add(intentos_fallidos+"");
		valores.add(ntdb + "");
		valores.add(locale == null ? "EN" : locale);
		valores.add(Util.nvl(Util.integerToString(countryID)));
		valores.add(new Boolean(numeros).toString());
		valores.add(new Boolean(recibir_ntdb).toString());
		valores.add("");//porcentaje_oficiales_viejo
		valores.add("");//porcentaje_amistosos_viejo
		valores.add("-" + factor_edad + "");
		valores.add(factor_habilidad + "");
		valores.add(factor_talento + "");
		valores.add(factor_residual + "");
		valores.add("-" + factor_formacion + "");
		valores.add(factor_rapidez + "");
		valores.add(factor_tecnica + "");
		valores.add(factor_pases + "");
		valores.add(factor_porteria + "");
		valores.add(factor_defensa + "");
		valores.add(factor_creacion + "");
		valores.add(factor_anotacion + "");
		valores.add(sumskills_gk_rapidez + "");
		valores.add(sumskills_def_rapidez + "");
		valores.add(sumskills_mid_rapidez + "");
		valores.add(sumskills_att_rapidez + "");
		valores.add(sumskills_gk_tecnica + "");
		valores.add(sumskills_def_tecnica + "");
		valores.add(sumskills_mid_tecnica + "");
		valores.add(sumskills_att_tecnica + "");
		valores.add(sumskills_gk_pases + "");
		valores.add(sumskills_def_pases + "");
		valores.add(sumskills_mid_pases + "");
		valores.add(sumskills_att_pases + "");
		valores.add(sumskills_gk_porteria + "");
		valores.add(sumskills_def_porteria + "");
		valores.add(sumskills_mid_porteria + "");
		valores.add(sumskills_att_porteria + "");
		valores.add(sumskills_gk_defensa + "");
		valores.add(sumskills_def_defensa + "");
		valores.add(sumskills_mid_defensa + "");
		valores.add(sumskills_att_defensa + "");
		valores.add(sumskills_gk_creacion + "");
		valores.add(sumskills_def_creacion + "");
		valores.add(sumskills_mid_creacion + "");
		valores.add(sumskills_att_creacion + "");
		valores.add(sumskills_gk_anotacion + "");
		valores.add(sumskills_def_anotacion + "");
		valores.add(sumskills_mid_anotacion + "");
		valores.add(sumskills_att_anotacion + "");
		valores.add(new Boolean(mostrar_salario).toString());
		valores.add(new Boolean(mostrar_experiencia).toString());
		valores.add(new Boolean(mostrar_disciplina_tactica).toString());
		valores.add(new Boolean(mostrar_trabajo_en_equipo).toString());
		valores.add(new Boolean(mostrar_altura).toString());
		valores.add(new Boolean(mostrar_peso).toString());
		valores.add(new Boolean(mostrar_IMC).toString());
		valores.add(new Boolean(mostrar_banquillo).toString());
		valores.add(new Boolean(mostrar_suma_habilidades).toString());
		valores.add(Util.nvl(actualizacion_automatica));
		valores.add(factor_edad_rapidez + "");

		// NOTA: es necesario acabar con ",*" porque String.split no añade las últimas cadenas al array si están vacías
		return String.join(",", valores) + ",*";
	}

	private void inicializar_residual_formacion() {
		factor_residual = 0.15f;	// Sube un 50%
		factor_formacion = 0.14f;	// Sube un 40%
	}

	public void inicializar() {
		factor_edad = 1.094f;
		factor_edad_rapidez = 1.1f;
		factor_habilidad = 1.094f;
		factor_talento = 1f;
		inicializar_residual_formacion();
		factor_rapidez = Util.round(4.5f / 6.8f, 3);
		factor_tecnica = Util.round(6f / 6.8f, 3);
		factor_pases = Util.round(6.8f / 6.8f, 3);
		factor_porteria = Util.round(6.8f / 6.8f, 3);
		factor_defensa = Util.round(5.5f / 6.8f, 3);
		factor_creacion = Util.round(6.8f / 6.8f, 3);
		factor_anotacion = Util.round(5.5f / 6.8f, 3);
		sumskills_gk_rapidez = 50;
		sumskills_gk_tecnica = 0;
		sumskills_gk_pases = 10;
		sumskills_gk_porteria = 100;
		sumskills_gk_defensa = 0;
		sumskills_gk_creacion = 0;
		sumskills_gk_anotacion = 0;
		sumskills_def_rapidez = 100;
		sumskills_def_tecnica = 20;
		sumskills_def_pases = 10;
		sumskills_def_porteria = 0;
		sumskills_def_defensa = 100;
		sumskills_def_creacion = 20;
		sumskills_def_anotacion = 0;
		sumskills_mid_rapidez = 80;
		sumskills_mid_tecnica = 80;
		sumskills_mid_pases = 100;
		sumskills_mid_porteria = 0;
		sumskills_mid_defensa = 40;
		sumskills_mid_creacion = 100;
		sumskills_mid_anotacion = 0;
		sumskills_att_rapidez = 100;
		sumskills_att_tecnica = 100;
		sumskills_att_pases = 0;
		sumskills_att_porteria = 0;
		sumskills_att_defensa = 10;
		sumskills_att_creacion = 10;
		sumskills_att_anotacion = 100;
	}

	public int getPrimera_jornada() {
		if (tipo_entrenamiento[0].isEmpty()) {
			return jornada;
		} else {
			List<Integer> lista = new ArrayList<Integer>();
			lista.addAll(tipo_entrenamiento[0].keySet());
			Collections.sort(lista);
			return lista.get(0);
		}
	}
	
	public Integer getDef_jornada() {
		return def_tid.equals(tid) ? jornada : jornada_nt;
	}

	public void setDef_jornada(int jornada) {
		if (def_tid.equals(tid)) {
			this.jornada = jornada;
		} else {
			this.jornada_nt = jornada;
		}
	}

	public String getDef_equipo() {
		return def_tid.equals(tid) ? equipo : equipo_nt;
	}

	/** CAMPOS CALCULADOS **/
	
	public String getStr_entrenador() {
		Jugador entrenador = entrenador_principal.get(jornada);
		return entrenador == null ? "" : entrenador.getHabilidades();
	}
	
	public String getStr_nivel_asistentes() {
		return Util.nvl(nivel_asistentes.get(jornada));
	}

	public String getStr_nivel_juveniles() {
		return Util.nvl(nivel_juveniles.get(jornada));
	}

	public String getStr_tipo_entrenamiento(int num) {
		Jugador entrenador = entrenador_principal.get(jornada);
		TIPO_ENTRENAMIENTO tipo = tipo_entrenamiento[num].get(jornada);
		if (tipo == null || entrenador == null) {
			return null;
		} else {
			boolean ok = entrenador.getValor_habilidad(tipo) == 16;
			return (ok ? "" : "<span title='" + Util.getTexto(locale, "messages.coach_below_16") + "' class='warning'>") + Util.getTexto(locale, "skills." + tipo.getIngles()) + (ok ? "" : " <i class='fa-solid fa-triangle-exclamation'></i></span>");
		}
	}
	
	public String getStr_demarcacion() {
		return Util.nvl(demarcacion.get(jornada));
	}
	
	// Convierte un valor de la moneda del usuario a euros
	public Integer convertir_euros(Integer valor) {
		if (valor == null) {
			return null;
		} else if (getCountryID() == null || getCountryID().equals(0)) {
			return valor;
		} else {
			// El valor está en su conversión original, lo deshacemos y dividimos por 4 para convertir a euros
			return new BigDecimal(valor).multiply(Pais.currency_rates[getCountryID() - 1]).divide(new BigDecimal("4"), RoundingMode.FLOOR).intValue();
		}		
	}

	/******
	 * G&S
	 ******/
	
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Integer getTid() {
		return tid;
	}
	public void setTid(Integer tid) {
		this.tid = tid;
	}
	public Integer getDef_tid() {
		return def_tid;
	}
	public void setDef_tid(Integer def_tid) {
		this.def_tid = def_tid;
	}

	public Integer getTid_nt() {
		return tid_nt;
	}

	public void setTid_nt(Integer tid_nt) {
		this.tid_nt = tid_nt;
	}

	public String getEquipo() {
		return equipo;
	}

	public void setEquipo(String equipo) {
		this.equipo = equipo;
	}

	public String getEquipo_nt() {
		return equipo_nt;
	}

	public void setEquipo_nt(String equipo_nt) {
		this.equipo_nt = equipo_nt;
	}

	public Integer getJornada() {
		return jornada;
	}

	public void setJornada(Integer jornada) {
		this.jornada = jornada;
	}

	public HashMap<Integer, TIPO_ENTRENAMIENTO> getTipo_entrenamiento(int i) {
		return tipo_entrenamiento[i];
	}

	public void setTipo_entrenamiento(HashMap<Integer, TIPO_ENTRENAMIENTO> tipo_entrenamiento, int i) {
		this.tipo_entrenamiento[i] = tipo_entrenamiento;
	}

	public HashMap<Integer, DEMARCACION> getDemarcacion() {
		return demarcacion;
	}

	public void setDemarcacion(HashMap<Integer, DEMARCACION> demarcacion) {
		this.demarcacion = demarcacion;
	}

	public String getLogin_sokker() {
		return login_sokker;
	}

	public void setLogin_sokker(String login_sokker) {
		this.login_sokker = login_sokker;
	}

	public Integer getJornada_nt() {
		return jornada_nt;
	}

	public void setJornada_nt(Integer jornada_nt) {
		this.jornada_nt = jornada_nt;
	}

	public HashMap<Integer, Jugador> getEntrenador_principal() {
		return entrenador_principal;
	}

	public void setEntrenador_principal(HashMap<Integer, Jugador> entrenador_principal) {
		this.entrenador_principal = entrenador_principal;
	}

	public HashMap<Integer, BigDecimal> getNivel_asistentes() {
		return nivel_asistentes;
	}

	public void setNivel_asistentes(HashMap<Integer, BigDecimal> nivel_asistentes) {
		this.nivel_asistentes = nivel_asistentes;
	}

	public HashMap<Integer, BigDecimal> getNivel_juveniles() {
		return nivel_juveniles;
	}

	public void setNivel_juveniles(HashMap<Integer, BigDecimal> nivel_juveniles) {
		this.nivel_juveniles = nivel_juveniles;
	}

	public Integer getIntentos_fallidos() {
		return intentos_fallidos;
	}

	public void setIntentos_fallidos(Integer intentos_fallidos) {
		this.intentos_fallidos = intentos_fallidos;
	}

	public boolean isNtdb() {
		return ntdb;
	}

	public void setNtdb(boolean ntdb) {
		this.ntdb = ntdb;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public Integer getCountryID() {
		return countryID;
	}

	public void setCountryID(Integer countryID) {
		this.countryID = countryID;
	}

	public boolean isNumeros() {
		return numeros;
	}

	public void setNumeros(boolean numeros) {
		this.numeros = numeros;
	}

	public boolean isRecibir_ntdb() {
		return recibir_ntdb;
	}

	public void setRecibir_ntdb(boolean recibir_ntdb) {
		this.recibir_ntdb = recibir_ntdb;
	}

	public boolean isMostrar_experiencia() {
		return mostrar_experiencia;
	}

	public void setMostrar_experiencia(boolean mostrar_experiencia) {
		this.mostrar_experiencia = mostrar_experiencia;
	}

	public boolean isMostrar_disciplina_tactica() {
		return mostrar_disciplina_tactica;
	}

	public void setMostrar_disciplina_tactica(boolean mostrar_disciplina_tactica) {
		this.mostrar_disciplina_tactica = mostrar_disciplina_tactica;
	}

	public boolean isMostrar_trabajo_en_equipo() {
		return mostrar_trabajo_en_equipo;
	}

	public void setMostrar_trabajo_en_equipo(boolean mostrar_trabajo_en_equipo) {
		this.mostrar_trabajo_en_equipo = mostrar_trabajo_en_equipo;
	}

	public boolean isMostrar_altura() {
		return mostrar_altura;
	}

	public void setMostrar_altura(boolean mostrar_altura) {
		this.mostrar_altura = mostrar_altura;
	}

	public boolean isMostrar_peso() {
		return mostrar_peso;
	}

	public void setMostrar_peso(boolean mostrar_peso) {
		this.mostrar_peso = mostrar_peso;
	}

	public boolean isMostrar_banquillo() {
		return mostrar_banquillo;
	}

	public void setMostrar_banquillo(boolean mostrar_banquillo) {
		this.mostrar_banquillo = mostrar_banquillo;
	}

	public boolean isMostrar_IMC() {
		return mostrar_IMC;
	}

	public void setMostrar_IMC(boolean mostrar_IMC) {
		this.mostrar_IMC = mostrar_IMC;
	}

	public boolean isMostrar_suma_habilidades() {
		return mostrar_suma_habilidades;
	}

	public void setMostrar_suma_habilidades(boolean mostrar_suma_habilidades) {
		this.mostrar_suma_habilidades = mostrar_suma_habilidades;
	}

	public boolean isMostrar_salario() {
		return mostrar_salario;
	}

	public void setMostrar_salario(boolean mostrar_salario) {
		this.mostrar_salario = mostrar_salario;
	}

	public float getFactor_edad() {
		return factor_edad;
	}

	public float getFactor_edad(TIPO_ENTRENAMIENTO habilidad) {
		return habilidad != null && habilidad == TIPO_ENTRENAMIENTO.Rapidez ? factor_edad_rapidez : factor_edad;
	}

	public void setFactor_edad(float factor_edad) {
		this.factor_edad = factor_edad;
	}

	public float getFactor_habilidad() {
		return factor_habilidad;
	}

	public void setFactor_habilidad(float factor_habilidad) {
		this.factor_habilidad = factor_habilidad;
	}

	public float getFactor_residual() {
		return factor_residual;
	}

	public void setFactor_residual(float factor_residual) {
		this.factor_residual = factor_residual;
	}

	public float getFactor_rapidez() {
		return factor_rapidez;
	}

	public void setFactor_rapidez(float factor_rapidez) {
		this.factor_rapidez = factor_rapidez;
	}

	public float getFactor_tecnica() {
		return factor_tecnica;
	}

	public void setFactor_tecnica(float factor_tecnica) {
		this.factor_tecnica = factor_tecnica;
	}

	public float getFactor_pases() {
		return factor_pases;
	}

	public void setFactor_pases(float factor_pases) {
		this.factor_pases = factor_pases;
	}

	public float getFactor_porteria() {
		return factor_porteria;
	}

	public void setFactor_porteria(float factor_porteria) {
		this.factor_porteria = factor_porteria;
	}

	public float getFactor_defensa() {
		return factor_defensa;
	}

	public void setFactor_defensa(float factor_defensa) {
		this.factor_defensa = factor_defensa;
	}

	public float getFactor_creacion() {
		return factor_creacion;
	}

	public void setFactor_creacion(float factor_creacion) {
		this.factor_creacion = factor_creacion;
	}

	public float getFactor_anotacion() {
		return factor_anotacion;
	}

	public void setFactor_anotacion(float factor_anotacion) {
		this.factor_anotacion = factor_anotacion;
	}

	public float getFactor_talento() {
		return factor_talento;
	}

	public void setFactor_talento(float factor_talento) {
		this.factor_talento = factor_talento;
	}

	public int getSumskills_gk_rapidez() {
		return sumskills_gk_rapidez;
	}

	public void setSumskills_gk_rapidez(int sumskills_gk_rapidez) {
		this.sumskills_gk_rapidez = sumskills_gk_rapidez;
	}

	public int getSumskills_gk_tecnica() {
		return sumskills_gk_tecnica;
	}

	public void setSumskills_gk_tecnica(int sumskills_gk_tecnica) {
		this.sumskills_gk_tecnica = sumskills_gk_tecnica;
	}

	public int getSumskills_gk_pases() {
		return sumskills_gk_pases;
	}

	public void setSumskills_gk_pases(int sumskills_gk_pases) {
		this.sumskills_gk_pases = sumskills_gk_pases;
	}

	public int getSumskills_gk_porteria() {
		return sumskills_gk_porteria;
	}

	public void setSumskills_gk_porteria(int sumskills_gk_porteria) {
		this.sumskills_gk_porteria = sumskills_gk_porteria;
	}

	public int getSumskills_gk_defensa() {
		return sumskills_gk_defensa;
	}

	public void setSumskills_gk_defensa(int sumskills_gk_defensa) {
		this.sumskills_gk_defensa = sumskills_gk_defensa;
	}

	public int getSumskills_gk_creacion() {
		return sumskills_gk_creacion;
	}

	public void setSumskills_gk_creacion(int sumskills_gk_creacion) {
		this.sumskills_gk_creacion = sumskills_gk_creacion;
	}

	public int getSumskills_gk_anotacion() {
		return sumskills_gk_anotacion;
	}

	public void setSumskills_gk_anotacion(int sumskills_gk_anotacion) {
		this.sumskills_gk_anotacion = sumskills_gk_anotacion;
	}

	public int getSumskills_def_rapidez() {
		return sumskills_def_rapidez;
	}

	public void setSumskills_def_rapidez(int sumskills_def_rapidez) {
		this.sumskills_def_rapidez = sumskills_def_rapidez;
	}

	public int getSumskills_def_tecnica() {
		return sumskills_def_tecnica;
	}

	public void setSumskills_def_tecnica(int sumskills_def_tecnica) {
		this.sumskills_def_tecnica = sumskills_def_tecnica;
	}

	public int getSumskills_def_pases() {
		return sumskills_def_pases;
	}

	public void setSumskills_def_pases(int sumskills_def_pases) {
		this.sumskills_def_pases = sumskills_def_pases;
	}

	public int getSumskills_def_porteria() {
		return sumskills_def_porteria;
	}

	public void setSumskills_def_porteria(int sumskills_def_porteria) {
		this.sumskills_def_porteria = sumskills_def_porteria;
	}

	public int getSumskills_def_defensa() {
		return sumskills_def_defensa;
	}

	public void setSumskills_def_defensa(int sumskills_def_defensa) {
		this.sumskills_def_defensa = sumskills_def_defensa;
	}

	public int getSumskills_def_creacion() {
		return sumskills_def_creacion;
	}

	public void setSumskills_def_creacion(int sumskills_def_creacion) {
		this.sumskills_def_creacion = sumskills_def_creacion;
	}

	public int getSumskills_def_anotacion() {
		return sumskills_def_anotacion;
	}

	public void setSumskills_def_anotacion(int sumskills_def_anotacion) {
		this.sumskills_def_anotacion = sumskills_def_anotacion;
	}

	public int getSumskills_mid_rapidez() {
		return sumskills_mid_rapidez;
	}

	public void setSumskills_mid_rapidez(int sumskills_mid_rapidez) {
		this.sumskills_mid_rapidez = sumskills_mid_rapidez;
	}

	public int getSumskills_mid_tecnica() {
		return sumskills_mid_tecnica;
	}

	public void setSumskills_mid_tecnica(int sumskills_mid_tecnica) {
		this.sumskills_mid_tecnica = sumskills_mid_tecnica;
	}

	public int getSumskills_mid_pases() {
		return sumskills_mid_pases;
	}

	public void setSumskills_mid_pases(int sumskills_mid_pases) {
		this.sumskills_mid_pases = sumskills_mid_pases;
	}

	public int getSumskills_mid_porteria() {
		return sumskills_mid_porteria;
	}

	public void setSumskills_mid_porteria(int sumskills_mid_porteria) {
		this.sumskills_mid_porteria = sumskills_mid_porteria;
	}

	public int getSumskills_mid_defensa() {
		return sumskills_mid_defensa;
	}

	public void setSumskills_mid_defensa(int sumskills_mid_defensa) {
		this.sumskills_mid_defensa = sumskills_mid_defensa;
	}

	public int getSumskills_mid_creacion() {
		return sumskills_mid_creacion;
	}

	public void setSumskills_mid_creacion(int sumskills_mid_creacion) {
		this.sumskills_mid_creacion = sumskills_mid_creacion;
	}

	public int getSumskills_mid_anotacion() {
		return sumskills_mid_anotacion;
	}

	public void setSumskills_mid_anotacion(int sumskills_mid_anotacion) {
		this.sumskills_mid_anotacion = sumskills_mid_anotacion;
	}

	public int getSumskills_att_rapidez() {
		return sumskills_att_rapidez;
	}

	public void setSumskills_att_rapidez(int sumskills_att_rapidez) {
		this.sumskills_att_rapidez = sumskills_att_rapidez;
	}

	public int getSumskills_att_tecnica() {
		return sumskills_att_tecnica;
	}

	public void setSumskills_att_tecnica(int sumskills_att_tecnica) {
		this.sumskills_att_tecnica = sumskills_att_tecnica;
	}

	public int getSumskills_att_pases() {
		return sumskills_att_pases;
	}

	public void setSumskills_att_pases(int sumskills_att_pases) {
		this.sumskills_att_pases = sumskills_att_pases;
	}

	public int getSumskills_att_porteria() {
		return sumskills_att_porteria;
	}

	public void setSumskills_att_porteria(int sumskills_att_porteria) {
		this.sumskills_att_porteria = sumskills_att_porteria;
	}

	public int getSumskills_att_defensa() {
		return sumskills_att_defensa;
	}

	public void setSumskills_att_defensa(int sumskills_att_defensa) {
		this.sumskills_att_defensa = sumskills_att_defensa;
	}

	public int getSumskills_att_creacion() {
		return sumskills_att_creacion;
	}

	public void setSumskills_att_creacion(int sumskills_att_creacion) {
		this.sumskills_att_creacion = sumskills_att_creacion;
	}

	public int getSumskills_att_anotacion() {
		return sumskills_att_anotacion;
	}

	public void setSumskills_att_anotacion(int sumskills_att_anotacion) {
		this.sumskills_att_anotacion = sumskills_att_anotacion;
	}

	public int getNumero_decimales() {
		return numero_decimales;
	}

	public void setNumero_decimales(int numero_decimales) {
		this.numero_decimales = numero_decimales;
	}

	public String getScouts() {
		return scouts;
	}

	public void setScouts(String scouts) {
		this.scouts = scouts;
	}

	public HashMap<String, Usuario> getScout_de() {
		return scout_de;
	}

	public void setScout_de(HashMap<String, Usuario> scout_de) {
		this.scout_de = scout_de;
	}

	public String getNotas() {
		return notas;
	}

	public void setNotas(String notas) {
		this.notas = notas;
	}

	public float getFactor_formacion() {
		return factor_formacion;
	}

	public void setFactor_formacion(float factor_formacion) {
		this.factor_formacion = factor_formacion;
	}

	public String getActualizacion_automatica() {
		return actualizacion_automatica;
	}

	public void setActualizacion_automatica(String actualizacion_automatica) {
		this.actualizacion_automatica = actualizacion_automatica;
	}

	public float getFactor_edad_rapidez() {
		return factor_edad_rapidez;
	}

	public void setFactor_edad_rapidez(float factor_edad_rapidez) {
		this.factor_edad_rapidez = factor_edad_rapidez;
	}
}
