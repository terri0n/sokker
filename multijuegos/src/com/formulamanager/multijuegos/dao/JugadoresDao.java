package com.formulamanager.multijuegos.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.formulamanager.multijuegos.websockets.Jugador;

public class JugadoresDao extends DaoBase {
	public static Jugador obtener(String nombre, String contrasenya, String email) throws SQLException, ParseException {
        String select = "select * from jugadores";
        String where = "";
        if (nombre != null) {
        	where = " where nombre = ?";
        }
        if (contrasenya != null) {
        	where += (where.equals("") ? " where " : " and ") + "contrasenya = ?";
        }
        if (email != null) {
        	where += (where.equals("") ? " where " : " and ") + "email = ?";
        }
        String sql = select + where;
		PreparedStatement ps = getConnection().prepareStatement(sql);

		try {
			int i = 1;
	        if (nombre != null) {
	        	ps.setString(i++, nombre);
	        }
	        if (contrasenya != null) {
	        	ps.setString(i++, contrasenya);
	        }
	        if (email != null) {
	        	ps.setString(i++, email);
	        }

	        ResultSet rs = ps.executeQuery();

	        if (rs.next()) {
	        	return new Jugador(
	        		rs.getString("nombre"), 
	        		rs.getString("contrasenya"), 
	        		rs.getInt("puntos"), 
	        		rs.getInt("num_partidos"), 
	        		rs.getString("email"), 
	        		new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("fecha_alta")),
	        		false
	        	);
	        } else {
	        	return null;
	        }
        } finally {
        	ps.close();
        }
	}

	public static List<Jugador> buscar(String nombre, String contrasenya, String email, String orden, Integer limite) throws SQLException, ParseException {
        String select = "select * from jugadores";
        String where = "";
        if (nombre != null) {
        	where = " where nombre = ?";
        }
        if (contrasenya != null) {
        	where += (where.equals("") ? " where " : " and ") + "contrasenya = ?";
        }
        if (email != null) {
        	where += (where.equals("") ? " where " : " and ") + "email = ?";
        }

        String orderBy = orden != null ? " order by " + orden : "";

        String limit = limite != null ? " limit " + limite : "";

        String sql = select + where + orderBy + limit;
        PreparedStatement ps = getConnection().prepareStatement(sql);
        List<Jugador> lista = new ArrayList<Jugador>();

		try {
			int i = 1;
	        if (nombre != null) {
	        	ps.setString(i++, nombre);
	        }
	        if (contrasenya != null) {
	        	ps.setString(i++, contrasenya);
	        }
	        if (email != null) {
	        	ps.setString(i++, email);
	        }

	        ResultSet rs = ps.executeQuery();

	        while (rs.next()) {
	        	lista.add(new Jugador(
	        		rs.getString("nombre"), 
	        		rs.getString("contrasenya"), 
	        		rs.getInt("puntos"), 
	        		rs.getInt("num_partidos"), 
	        		rs.getString("email"), 
	        		getDateFormat().parse(rs.getString("fecha_alta")),
	        		false
	        	));
	        }
        } finally {
        	ps.close();
        }
		return lista;
	}

	public static void insertar(Jugador j) throws SQLException {
		String sql = "insert into jugadores values (?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = getConnection().prepareStatement(sql);

		try {
			int i = 1;
	        ps.setString(i++, j.nombre);
	        ps.setString(i++, j.contrasenya);
	        ps.setInt(i++, j.puntos);
	        ps.setInt(i++, j.num_partidos);
	        ps.setString(i++, j.email);
	        ps.setString(i++, getDateFormat().format(j.fecha_alta));
        
	        System.out.println(sql + " [" + j + "]");
			ps.executeUpdate();
		} finally {
			ps.close();
		}
	}

	public static void actualizar(Jugador j) throws SQLException {
		String sql = "update jugadores set puntos = ?, num_partidos = ?, contrasenya = ? where nombre = ?";

		PreparedStatement ps = getConnection().prepareStatement(sql);

		try {
			int i = 1;
			ps.setInt(i++, j.puntos);
			ps.setInt(i++, j.num_partidos);
			ps.setString(i++, j.contrasenya);
	        ps.setString(i++, j.nombre);
        
	        System.out.println(sql + " [" + j + "]");
			ps.executeUpdate();
		} finally {
			ps.close();
		}
	}
}
