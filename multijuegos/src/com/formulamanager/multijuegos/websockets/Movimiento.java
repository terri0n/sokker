package com.formulamanager.multijuegos.websockets;

import java.util.HashMap;

import com.formulamanager.multijuegos.websockets.EndpointBase.COLOR;

public class Movimiento {
	public static Integer CASILLAS = 9;
	public static enum TIPO_FIGURA { KING, PAWN, BISHOP, ROOK, KNIGHT, QUEEN, PELOTA }
	public static enum FIGURA { blackKing(TIPO_FIGURA.KING, COLOR.negras),
								blackPawn1(TIPO_FIGURA.PAWN, COLOR.negras),
								blackPawn2(TIPO_FIGURA.PAWN, COLOR.negras),
								blackPawn3(TIPO_FIGURA.PAWN, COLOR.negras),
								blackBishop1(TIPO_FIGURA.BISHOP, COLOR.negras),
								blackBishop2(TIPO_FIGURA.BISHOP, COLOR.negras),
								blackRook1(TIPO_FIGURA.ROOK, COLOR.negras),
								blackRook2(TIPO_FIGURA.ROOK, COLOR.negras),
								blackKnight1(TIPO_FIGURA.KNIGHT, COLOR.negras),
								blackKnight2(TIPO_FIGURA.KNIGHT, COLOR.negras),
								blackQueen(TIPO_FIGURA.QUEEN, COLOR.negras), 
								
								whiteKing(TIPO_FIGURA.KING, COLOR.blancas),
								whitePawn1(TIPO_FIGURA.PAWN, COLOR.blancas),
								whitePawn2(TIPO_FIGURA.PAWN, COLOR.blancas),
								whitePawn3(TIPO_FIGURA.PAWN, COLOR.blancas),
								whiteBishop1(TIPO_FIGURA.BISHOP, COLOR.blancas),
								whiteBishop2(TIPO_FIGURA.BISHOP, COLOR.blancas),
								whiteRook1(TIPO_FIGURA.ROOK, COLOR.blancas),
								whiteRook2(TIPO_FIGURA.ROOK, COLOR.blancas),
								whiteKnight1(TIPO_FIGURA.KNIGHT, COLOR.blancas),
								whiteKnight2(TIPO_FIGURA.KNIGHT, COLOR.blancas),
								whiteQueen(TIPO_FIGURA.QUEEN, COLOR.blancas),
								
								pelota(null, null);
		private TIPO_FIGURA tipo;
		private COLOR color;
		private FIGURA(TIPO_FIGURA tipo, COLOR color) {
			this.tipo = tipo;
			this.color = color;
		}
		public TIPO_FIGURA getTipo() {
			return tipo;
		}
		public COLOR getColor() {
			return color;
		}
	}
	
	public FIGURA id;
	public Integer x;
	public Integer y;

	public Movimiento(FIGURA id, Integer x, Integer y) {
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	public Integer getCasilla() {
		return y * CASILLAS + x;
	}

	@Override
	public String toString() {
		return id + " " + x + " " + y;
	}

	public void setCasilla(int casilla) {
		x = casilla % CASILLAS;
		y = casilla / CASILLAS;
	}
	
	public void setCasilla(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
