package com.formulamanager.multijuegos.websockets;

import java.util.ArrayList;
import java.util.List;

import com.formulamanager.multijuegos.websockets.EndpointBase.COLOR;
import com.google.gson.annotations.Expose;

class Respuesta {
	@Expose
	public List<Jugador> jugadores = new ArrayList<Jugador>();
	@Expose
	public List<String> partidos = new ArrayList<String>();
	@Expose
	public String nombre = null;
	@Expose
	public String nombre_rival = null;
	@Expose
	public COLOR color = null;
}