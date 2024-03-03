package com.formulamanager.multijuegos.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.SystemUtils;

import com.formulamanager.multijuegos.util.ConnectionExt;

public class DaoBase {
	private static ConnectionExt connection;

	public static Connection getConnection() throws SQLException {
		if (connection == null) {
			try {
				Class.forName("org.sqlite.JDBC");
				connection = new ConnectionExt(DriverManager.getConnection("jdbc:sqlite:" + (SystemUtils.IS_OS_LINUX ? "/home" : "D:\\home") + File.separator + "chessgoal" + File.separator + "chessgoal.db"));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return connection;
	}
	
	public static DateFormat getDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd");
	}
}
