package com.formulamanager.multijuegos.websockets;

public class Movimiento {
	public static enum FIGURA { blackKing, blackPawn1, blackPawn2, blackPawn3, blackBishop1, blackBishop2, blackRook1, blackRook2, blackKnight1, blackKnight2, blackQueen, 
								whiteKing, whitePawn1, whitePawn2, whitePawn3, whiteBishop1, whiteBishop2, whiteRook1, whiteRook2, whiteKnight1, whiteKnight2, whiteQueen,
								pelota }
	
	public FIGURA id;
	public Integer x;
	public Integer y;

	public Movimiento(FIGURA id, Integer x, Integer y) {
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	public Integer getCasilla() {
		return y * 9 + x;
	}

	@Override
	public String toString() {
		return id + " " + x + " " + y;
	}
}
