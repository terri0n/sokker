package com.formulamanager.multijuegos.websockets;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.websocket.Session;

import com.formulamanager.multijuegos.entity.EntityBase;
import com.formulamanager.multijuegos.entity.Jugador;
import com.formulamanager.multijuegos.util.Util;
import com.formulamanager.multijuegos.websockets.EndpointBase.COLOR;
import com.formulamanager.multijuegos.websockets.Movimiento.FIGURA;
import com.formulamanager.multijuegos.websockets.Movimiento.TIPO_FIGURA;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Partido extends EntityBase {
	public Session blancas = null;
	public Session negras = null;
	public List<Session> observadores = new ArrayList<Session>();
	private HashMap<FIGURA, Movimiento> tactica_blancas;
	private HashMap<FIGURA, Movimiento> tactica_negras;
	private HashMap<FIGURA, Movimiento> tablero = new HashMap<FIGURA, Movimiento>();
	public boolean rival_quiere_otra = false;
	private Jugador jugador_blancas = null;
	private Jugador jugador_negras = null;
	public Date tiempo_blancas;
	public Date tiempo_negras;
	public Date ultimo_movimiento;
	public COLOR turno = null;
	public Integer duracion;
	public boolean privado;	// La invitación es privada pero el partido es público
	public COLOR ganador;
	public boolean oficial = true;	// Solo si los dos jugadores están registrados
	
	public Partido(COLOR color, Integer duracion, Jugador jugador, Session sesion, boolean privado) {
		this.duracion = duracion;
		this.privado = privado;

		if (color == COLOR.blancas) {
			this.blancas = sesion;
			this.jugador_blancas = jugador;
		} else {
			this.negras = sesion;
			this.jugador_negras = jugador;
		}
		
		actualizar_oficial(sesion);
	}
	
	private void actualizar_oficial(Session s) {
		Jugador j = (Jugador)s.getUserProperties().get("jugador");
		oficial &= !j.invitado;
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
    	String s = jugador_blancas.nombre;
    	if (jugador_negras != null) {
    		s += "-" + jugador_negras.nombre;
    	}
    	return s;
	}

	public Jugador getJugador(COLOR color) {
		return color == COLOR.blancas ? jugador_blancas : jugador_negras;
	}
	
	public String getRespuesta() {
		return Util.nvl(duracion) + "," + Util.nvl(jugador_blancas.nombre) + "-" + Util.nvl(jugador_negras.nombre);
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

		colocar_figura(FIGURA.pelota, 4, 5);

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
	
	public void hacer_movimiento(String mov, Session sesion) {
		List<Movimiento> lista = jsonToList(mov);
		
		if (validar_movimientos(lista)) {
			for (Movimiento m : lista) {
				anyadir_movimiento(m);
			}
			actualizar_cronometro();
			if (turno == null) {
				// Saque inicial, sacaban las blancas
				turno = COLOR.negras;
			} else {
				turno = getColor(sesion).cambiar();
			}
		}
	}

	private void anyadir_movimiento(Movimiento m) {
		if (m.id != FIGURA.pelota ) {
			Movimiento old_m = tablero.get(m.id);
			Movimiento t = getFigureInCell(m.getCasilla());
			Movimiento p = tablero.get(FIGURA.pelota);
			
			if (t != null) {
				// Si hay una pieza en la casilla de destino...
				boolean con_pelota = p.getCasilla().equals(old_m.getCasilla()) || p.getCasilla().equals(t.getCasilla());
				
				if (m.id.getTipo() == TIPO_FIGURA.ROOK) {
					// Si es torre, la empujamos
					int newX = t.x * 2 - old_m.x;
					int newY = t.y * 2 - old_m.y;
					t.setCasilla(newX, newY);
				} else {
					// Si no es torre, intercambiamos las piezas
					t.setCasilla(old_m.getCasilla());
				}
				
				// Si alguna de las dos figuras tenía la pelota, se la queda la pieza que tiene el turno
				if (con_pelota) {
					p.setCasilla(m.getCasilla());
				}
			} else if (p.getCasilla().equals(old_m.getCasilla())) {
				// Si la figura lleva la pelota...
				p.setCasilla(m.getCasilla());
			}
		}

		// Por último colocamos la pieza en el tablero
		tablero.put(m.id, m);
	}

	// Devuelve la pieza del tablero que se encuentra en la casilla indicada, o null si no hay ninguna
	// NOTA: no tenemos en cuenta la pelota
	private Movimiento getFigureInCell(int casilla) {
		for (Movimiento t : tablero.values()) {
			if (t.id != FIGURA.pelota) {
				if (t.getCasilla().equals(casilla)) {
					return t;
				}
			}
		}
		return null;
	}

	private List<Movimiento> jsonToList(String mov) {
		Type fooType = new TypeToken<ArrayList<Movimiento>>() {}.getType();
		List<Movimiento> lista = new Gson().fromJson(mov, fooType);
		return lista;
	}
	
	private HashMap<FIGURA, Movimiento> jsonToHashmap(String mov) {
		List<Movimiento> lista = jsonToList(mov);
		
		HashMap<FIGURA, Movimiento> hashmap = new HashMap<>();
		for (Movimiento m : lista) {
			hashmap.put(m.id, m);
		}
		return hashmap;
	}
	
	private boolean validar_movimientos(Collection<Movimiento> lista) {
		for (Movimiento m : lista) {
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

	public void anyadir_jugador(Jugador jugador, Session sesion) {
		if (blancas == null) {
			this.blancas = sesion;
			this.jugador_blancas = jugador;
		} else {
			this.negras = sesion;
			this.jugador_negras = jugador;
		}
		actualizar_oficial(sesion);
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
		
		if (validar_movimientos(hashmap.values())) {
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
		} else if (turno == null) {
			return 2;		// esperando saque inicial 
		} else if (turno == COLOR.blancas) {
			return 3;		// partido iniciado, turno blancas
		} else {
			return 4;		// partido iniciado, turno negras
		}
	}

	@Override
	public String toJson() {
		List<Movimiento> lista = new ArrayList<>();
		lista.addAll(tablero.values());
		Collections.sort(lista);
		return new Gson().toJson(lista);
	}
}


