package com.formulamanager.multijuegos.websockets;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.websocket.Session;

import com.formulamanager.multijuegos.util.Util;
import com.formulamanager.multijuegos.websockets.EndpointBase.COLOR;
import com.formulamanager.multijuegos.websockets.Movimiento.FIGURA;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public class Partido {
	public Session blancas = null;
	public Session negras = null;
	public List<Session> observadores = new ArrayList<Session>();
	private HashMap<FIGURA, Movimiento> tactica_blancas;
	private HashMap<FIGURA, Movimiento> tactica_negras;
	private HashMap<FIGURA, Movimiento> tablero = new HashMap<FIGURA, Movimiento>();
	public boolean rival_quiere_otra = false;
	public String nombre_blancas = null;
	public String nombre_negras = null;
	public Date tiempo_blancas;
	public Date tiempo_negras;
	public Date ultimo_movimiento;
	public COLOR turno = null;
	public Integer duracion;
	public boolean privado;	// La invitación es privada pero el partido es público
	public COLOR ganador;
	
	public Partido(COLOR color, Integer duracion, String nombre, Session sesion, boolean privado) {
		this.duracion = duracion;
		this.privado = privado;

		if (color == COLOR.blancas) {
			this.blancas = sesion;
			this.nombre_blancas = nombre;
		} else {
			this.negras = sesion;
			this.nombre_negras = nombre;
		}
	}
	
	public COLOR getColor(Session sesion) {
		if (blancas != null && Endpoint.equals(blancas, sesion)) {
			return COLOR.blancas;
		} else if (negras != null && Endpoint.equals(negras, sesion)) {
			return COLOR.negras;
		} else {
			return null;
		}
	}
	
	public String getId() {
    	String s = nombre_blancas;
    	if (negras != null) {
    		s += "-" + nombre_negras;
    	}
    	return s;
	}
	
	public String getRespuesta() {
		return Util.nvl(duracion) + "," + Util.nvl(nombre_blancas) + "-" + Util.nvl(nombre_negras);
	}
	
	public String getTiempos() {
		if (duracion == null) {
			return "";
		} else {
			actualizar_cronometro();
			return getSegundos(tiempo_blancas) + "-" + getSegundos(tiempo_negras);
		}
	}
	
	public int getSegundos(Date tiempo) {
		return (int) tiempo.getMinutes() > duracion ? 0 : (tiempo.getMinutes() * 60 + tiempo.getSeconds());
	}
	
	public void reset() {
		rival_quiere_otra = false;
		tactica_blancas = null;
		tactica_negras = null;
		ganador = null;
		tablero.clear();
		turno = null;

		colocar_figura(FIGURA.blackKing, 4, 1);
		colocar_figura(FIGURA.blackPawn1, 3, 2);
		colocar_figura(FIGURA.blackPawn2, 4, 2);
		colocar_figura(FIGURA.blackPawn3, 5, 2);
		colocar_figura(FIGURA.blackBishop1, 2, 2);
		colocar_figura(FIGURA.blackBishop2, 6, 2);
		colocar_figura(FIGURA.blackRook1, 0, 2);
		colocar_figura(FIGURA.blackRook2, 8, 2);
		colocar_figura(FIGURA.blackKnight1, 1, 2);
		colocar_figura(FIGURA.blackKnight2, 7, 2);
		colocar_figura(FIGURA.blackQueen, 4, 3);
		
		colocar_figura(FIGURA.whiteKing, 4, 9);
		colocar_figura(FIGURA.whitePawn1, 3, 8);
		colocar_figura(FIGURA.whitePawn2, 4, 8);
		colocar_figura(FIGURA.whitePawn3, 5, 8);
		colocar_figura(FIGURA.whiteBishop1, 2, 8);
		colocar_figura(FIGURA.whiteBishop2, 6, 8);
		colocar_figura(FIGURA.whiteRook1, 0, 8);
		colocar_figura(FIGURA.whiteRook2, 8, 8);
		colocar_figura(FIGURA.whiteKnight1, 1, 8);
		colocar_figura(FIGURA.whiteKnight2, 7, 8);
		colocar_figura(FIGURA.whiteQueen, 4, 7);
		
		if (duracion != null) {
			tiempo_blancas = new Date(0, 0, 0, 0, duracion);
			tiempo_negras = new Date(0, 0, 0, 0, duracion);
		}
	}

	private void colocar_figura(FIGURA figura, int x, int y) {
		tablero.put(figura, new Movimiento(figura, x, y));
	}

	private void sumar_tiempo(Session sesion) {
		if (duracion != null) {
			long tiempo = new Date().getTime() - ultimo_movimiento.getTime();
			if (getColor(sesion) == COLOR.blancas) {
				tiempo_blancas = new Date(tiempo_blancas.getTime() - tiempo);
			} else {
				tiempo_negras = new Date(tiempo_negras.getTime() - tiempo);
			}
		}
	}
	
	public void anyadir_movimiento(String mov, Session sesion) {
		HashMap<FIGURA, Movimiento> hashmap = jsonToHashmap(mov);
		
		if (validar_movimientos(hashmap)) {
			tablero.putAll(hashmap);
			actualizar_cronometro();
			turno = getColor(sesion).cambiar();
		}
	}

	private HashMap<FIGURA, Movimiento> jsonToHashmap(String mov) {
		Type fooType = new TypeToken<ArrayList<Movimiento>>() {}.getType();
		List<Movimiento> lista = new Gson().fromJson(mov, fooType);
		
		HashMap<FIGURA, Movimiento> hashmap = new HashMap<>();
		for (Movimiento m : lista) {
			hashmap.put(m.id, m);
		}
		return hashmap;
	}
	
	private boolean validar_movimientos(HashMap<FIGURA, Movimiento> mov) {
		for (Movimiento m : mov.values()) {
			if (m.id == FIGURA.pelota && (m.y == 0 || m.y == 10)) {
				ganador = m.y == 0 ? COLOR.blancas : COLOR.negras;
			}
		}
		
		return true;
	}

	public void actualizar_cronometro() {
		if (duracion != null) {
			if (turno != null) {
				if (turno == COLOR.blancas) {
					sumar_tiempo(blancas);
				} else {
					sumar_tiempo(negras);
				}
				ultimo_movimiento = new Date();
			}
		}
	}

	public void anyadir_jugador(String nombre, Session sesion) {
		if (blancas == null) {
			this.blancas = sesion;
			this.nombre_blancas = nombre;
		} else {
			this.negras = sesion;
			this.nombre_negras = nombre;
		}
	}

	public String getTactica(COLOR color) {
		if (color == COLOR.blancas && tactica_blancas == null || color == COLOR.negras && tactica_negras == null) {
			return null;
		} else {
			return new Gson().toJson(color == COLOR.blancas ? tactica_blancas.values() : tactica_negras.values());
		}
	}

	/**
	 * Convertimos los movimientos a un HashMap para eliminar los duplicados. Tb nos ayudará a validarlos
	 * @param color
	 * @param tactica
	 */
	public void setTactica(COLOR color, String tactica) {
		HashMap<FIGURA, Movimiento> hashmap = jsonToHashmap(tactica);
		
		if (validar_movimientos(hashmap)) {
			if (color == COLOR.blancas) {
				tactica_blancas = hashmap;
			} else {
				tactica_negras = hashmap;
			}

			// Si ya tenemos las dos tácticas, las aplicamos al tablero
			if (tactica_blancas != null && tactica_negras != null) {
				tablero.putAll(tactica_blancas);
				tablero.putAll(tactica_negras);
			}
		}
	}

	public Short getEstado(Session sesion) {
		// El estado depende de quién pregunte, ya que si solo uno ha mandado la táctica, este estará esperando al otro (1)
		// TODO: observadores

		if (ganador != null) {
			return null;	// partido finalizado
		} else if (getColor(sesion) == COLOR.blancas && tactica_blancas == null
				|| getColor(sesion) == COLOR.negras && tactica_negras == null) {
			return 0;		// sin iniciar
		} else if (tactica_blancas == null || tactica_negras == null) {
			return 1;		// táctica mandada
		} else if (turno != null) {
			return 2;		// esperando saque inicial 
		} else if (turno == COLOR.blancas) {
			return 3;		// partido iniciado, turno blancas
		} else {
			return 4;		// partido iniciado, turno negras
		}
	}

	@Override
	public String toString() {
		return new Gson().toJson(tablero.values());
	}
}


