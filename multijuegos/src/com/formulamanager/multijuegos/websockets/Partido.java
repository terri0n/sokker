package com.formulamanager.multijuegos.websockets;

import java.awt.PageAttributes.ColorType;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.websocket.Session;

import com.formulamanager.multijuegos.entity.EntityBase;
import com.formulamanager.multijuegos.entity.Jugador;
import com.formulamanager.multijuegos.util.CustomTypeAdapterFactory;
import com.formulamanager.multijuegos.util.Util;
import com.formulamanager.multijuegos.websockets.EndpointBase.COLOR;
import com.formulamanager.multijuegos.websockets.Movimiento.FIGURA;
import com.formulamanager.multijuegos.websockets.Movimiento.TIPO_FIGURA;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Partido extends EntityBase {
	public Session blancas = null;
	public Session negras = null;
	public List<Session> observadores = new ArrayList<Session>();
	private HashMap<FIGURA, Movimiento> tactica_blancas;
	private HashMap<FIGURA, Movimiento> tactica_negras;
	private HashMap<FIGURA, Movimiento> tablero = new HashMap<FIGURA, Movimiento>();
	public HashSet<FIGURA> ultimo_movimiento_blancas = new HashSet<FIGURA>();
	public HashSet<FIGURA> ultimo_movimiento_negras = new HashSet<FIGURA>();
	public boolean pelota_movida = false;	// Indica si la pelota se ha movido por 2ª vez
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
		if (blancas != null && EndpointChessGoal.equals(blancas, sesion)) {
			return COLOR.blancas;
		} else if (negras != null && EndpointChessGoal.equals(negras, sesion)) {
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
		return Util.nvl(duracion) + "," + (jugador_blancas == null ? "" : Util.nvl(jugador_blancas.nombre)) + "-" + (jugador_negras == null ? "" : Util.nvl(jugador_negras.nombre));
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
		pelota_movida = false;
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

	public void hacer_movimientos_turno(String mov, Session sesion) throws Exception {
		List<Movimiento> lista = jsonToList(mov);
		
		validar_movimientos(getColor(sesion), lista);

		// Solo se mandan los movimientos completos al enviar la táctica
		if (lista != null) {
			for (Movimiento m : lista) {
				anyadir_movimiento(m);
			}
		}
		actualizar_cronometro();
		turno = turno.cambiar();
		
		// Reseteamos los movimientos del turno anterior de este jugador
		resetear_ultimos_movimientos();
	}

	// Movimiento antes de finalizar el turno. Si termina el partido no se manda por aquí sino que se finaliza el turno
	public void hacer_movimiento(String mov, Session sesion) {
		anyadir_movimiento(jsonToMovimiento(mov));
	}

	private void anyadir_movimiento(Movimiento m) {
		Movimiento figura_destino = getFigureInCell(m.getCasilla());
		HashSet<FIGURA> ultimo = getUltimo_movimiento();
		if (ultimo != null) {
			if (m.id == FIGURA.pelota && ultimo.contains(m.id)) {
				// Si ya ha movido la pelota una vez, marco el 2º movimiento
				pelota_movida = true;
			} else {
				ultimo.add(m.id);
			}
		}

		if (m.id != FIGURA.pelota ) {
			Movimiento old_m = tablero.get(m.id);
			Movimiento p = tablero.get(FIGURA.pelota);
			
			if (figura_destino != null) {
				// Si hay una pieza en la casilla de destino...
				if (ultimo != null) {
					ultimo.add(figura_destino.id);
				}
				boolean con_pelota = p.getCasilla().equals(old_m.getCasilla()) || p.getCasilla().equals(figura_destino.getCasilla());
				
				if (m.id.getTipo() == TIPO_FIGURA.ROOK) {
					// Si es torre, la empujamos
					int newX = figura_destino.x * 2 - old_m.x;
					int newY = figura_destino.y * 2 - old_m.y;
					figura_destino.setCasilla(newX, newY);
				} else {
					// Si no es torre, intercambiamos las piezas
					figura_destino.setCasilla(old_m.getCasilla());
				}
				
				// Si alguna de las dos figuras tenía la pelota, se la queda la pieza que tiene el turno
				if (con_pelota) {
					p.setCasilla(m.getCasilla());
				}
			} else if (p.getCasilla().equals(old_m.getCasilla())) {
				// Si la figura lleva la pelota...
				p.setCasilla(m.getCasilla());
			}
		} else {
			if (ultimo != null && figura_destino != null) {
				// Si muevo la pelota sobre una pieza, marco la pieza
				ultimo.add(figura_destino.id);
			}
			if (turno == null) {
				// Saque inicial
				turno = COLOR.blancas;
			}
		}

		// Colocamos la pieza en el tablero
		tablero.put(m.id, m);
System.out.println((turno == null || turno == COLOR.blancas ? COLOR.blancas : COLOR.negras) + ": " + tablero.size());
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

	private Movimiento jsonToMovimiento(String mov) {
		Type fooType = new TypeToken<Movimiento>() {}.getType();
		Movimiento movimiento = new Gson().fromJson(mov, fooType);
		return movimiento;
	}

	private void validar_movimientos(COLOR color, Collection<Movimiento> lista) throws Exception {
		if (lista != null) {
			for (Movimiento m : lista) {
				if (m.id.getColor() != color) {
					throw new Exception("Movimiento incorrecto: " + m);
				}
			}
		}
	}

	public void actualizar_cronometro() {
		if (duracion != null) {
			Short estado = getEstado(null);
			if (estado != null && estado >= 2) {
				long tiempo = new Date().getTime() - ultimo_movimiento.getTime();
				if (turno == null || turno == COLOR.blancas) {
					tiempo_blancas = new Date(tiempo_blancas.getTime() - tiempo);
				} else {
					tiempo_negras = new Date(tiempo_negras.getTime() - tiempo);
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
	 * 
	 * @return si los dos jugadores han enviado su táctica
	 * @throws Exception 
	 */
	public boolean setTactica(COLOR color, String tactica) throws Exception {
		HashMap<FIGURA, Movimiento> hashmap = jsonToHashmap(tactica);
		
		validar_movimientos(color, hashmap.values());
		
		if (color == COLOR.blancas) {
			tactica_blancas = hashmap;
		} else {
			tactica_negras = hashmap;
		}

		// Si ya tenemos las dos tácticas...
		if (tactica_blancas != null && tactica_negras != null) {
			// Las aplicamos al tablero
			tablero.putAll(tactica_blancas);
			tablero.putAll(tactica_negras);
			
			// Y las guardamos como el último movimiento de las negras (el turno será para las blancas así que empieza de cero)
			for (Movimiento m : tactica_negras.values()) {
				ultimo_movimiento_negras.add(m.id);
			}

			// Fecha de inicio
			ultimo_movimiento = new Date();
		
			return true;
		} else {
			return false;
		}
	}

	public Short getEstado(Session sesion) {
		// El estado depende de quién pregunte, ya que si solo uno ha mandado la táctica, este estará esperando al otro (1)
		// TODO: observadores

		if (ganador != null) {
			return null;	// partido finalizado
		} else if (sesion != null && (
				getColor(sesion) == COLOR.blancas && tactica_blancas == null ||
				getColor(sesion) == COLOR.negras && tactica_negras == null)) {
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
		
		Short estado = getEstado(null);
		if (estado == null || estado >= 2) {
			for (Movimiento m : lista) {
				m.setMovido(this);
				if (m.id == FIGURA.pelota) {
					m.pelota_movida = pelota_movida;
				}
			}
		}

		// Evito que se manden campos nulos/booleanos falsos
		Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT)
				.registerTypeAdapterFactory(new CustomTypeAdapterFactory())
				.create();
		return gson.toJson(lista);
	}

	public HashSet<FIGURA> getUltimo_movimiento() {
		Short estado = getEstado(turno == COLOR.blancas ? blancas : negras);
		
		if (estado == 2 || estado == 3) {
			return ultimo_movimiento_blancas;
		} else if (turno == COLOR.negras) {
			return ultimo_movimiento_negras;
		} else {
			return null;
		}
	}
	
	public void resetear_ultimos_movimientos() {
		HashSet<FIGURA> ultimo = getUltimo_movimiento();
		if (ultimo != null) {
System.out.println("resetea " + turno);
			ultimo.clear();
			pelota_movida = false;
		}
	}

	public boolean comprobar_fin() {
		Movimiento mov = tablero.get(FIGURA.pelota);
		if (mov.y == 0 || mov.y == 10) {
			ganador = mov.y == 0 ? COLOR.blancas : COLOR.negras;
			return true;
		} else {
			return false;
		}
	}
}
