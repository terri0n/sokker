package com.formulamanager.multijuegos.websockets;

import java.util.ArrayList;
import java.util.List;

import com.formulamanager.multijuegos.entity.EntityBase;
import com.formulamanager.multijuegos.entity.Jugador;
import com.formulamanager.multijuegos.websockets.EndpointBase.COLOR;

/**
 * Respuesta que se envía al conectar o reconectar
 * La lista de jugadores incluye al propio jugador y al posible rival, por eso solo mandamos sus nombres
 */
class Respuesta extends EntityBase {
	public List<Jugador> jugadores = new ArrayList<Jugador>();
	public List<String> partidos = new ArrayList<String>();
	public String jugador = null;
	public String rival = null;
	public COLOR color = null;
}