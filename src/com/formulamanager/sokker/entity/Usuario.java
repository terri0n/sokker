package com.formulamanager.sokker.entity;

import java.math.BigDecimal;
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
	private boolean numeros;		// Mostrar números en desplegables de habilidades
	
	// Entrenamiento por jornada
	private HashMap<Integer, TIPO_ENTRENAMIENTO> tipo_entrenamiento;
	private HashMap<Integer, DEMARCACION> demarcacion;
	private HashMap<Integer, Jugador> entrenador_principal;
	private HashMap<Integer, BigDecimal> nivel_asistentes;
	private HashMap<Integer, BigDecimal> nivel_juveniles;

	/************
	 * Funciones
	 ************/
	public Usuario(Integer tid, String equipo) {
		this.tid = tid;
		this.def_tid = tid;
		this.equipo = equipo;

		tipo_entrenamiento = new HashMap<Integer, TIPO_ENTRENAMIENTO>();
		demarcacion = new HashMap<Integer, DEMARCACION>();
		entrenador_principal = new HashMap<Integer, Jugador>();
		nivel_asistentes = new HashMap<Integer, BigDecimal>();
		nivel_juveniles = new HashMap<Integer, BigDecimal>();
	}
	
	public Usuario(String login_sokker, String login, String password) {
		this.setLogin_sokker(login_sokker);
		this.login = login;
		this.password = password;
	}

	public Usuario(List<String> valores) {
		int i = 0;
		
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
					locale = valores.get(i++);
					if (!valores.get(i).equals("*")) {
						countryID = Util.stringToInteger(valores.get(i++));
						numeros = Util.stringToBoolean(valores.get(i++));
					}
				}
			}
		}

		tipo_entrenamiento = new HashMap<Integer, TIPO_ENTRENAMIENTO>();
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
		valores.add(locale);
		valores.add(Util.nvl(Util.integerToString(countryID)));
		valores.add(new Boolean(numeros).toString());

		// NOTA: es necesario acabar con ",*" porque String.split no añade las últimas cadenas al array si están vacías
		return String.join(",", valores) + ",*";
	}

	public int getPrimera_jornada() {
		if (tipo_entrenamiento.isEmpty()) {
			return jornada;
		} else {
			List<Integer> lista = new ArrayList<Integer>();
			lista.addAll(tipo_entrenamiento.keySet());
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

	public HashMap<Integer, TIPO_ENTRENAMIENTO> getTipo_entrenamiento() {
		return tipo_entrenamiento;
	}

	public void setTipo_entrenamiento(HashMap<Integer, TIPO_ENTRENAMIENTO> tipo_entrenamiento) {
		this.tipo_entrenamiento = tipo_entrenamiento;
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
}
