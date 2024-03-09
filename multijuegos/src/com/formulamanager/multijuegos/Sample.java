package com.formulamanager.multijuegos;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.formulamanager.multijuegos.dao.DaoBase;
import com.formulamanager.multijuegos.util.Util;

public class Sample extends DaoBase
    {
      public static void main(String[] args)
      {
        Connection connection = null;
        try
        {
        	//System.out.println(new File(".").getAbsolutePath());
        	
          // create a database connection
          connection = getConnection();
          Statement statement = connection.createStatement();
          statement.setQueryTimeout(30);  // set timeout to 30 sec.

          statement.executeUpdate("drop table if exists jugadores");
          statement.executeUpdate("create table jugadores (nombre string collate nocase PRIMARY KEY, contrasenya string not null, puntos integer not null, num_partidos int not null, email string unique collate nocase, fecha_alta string not null, pais string not null)");
          statement.executeUpdate("insert into jugadores values('Hector', '" + Util.getMD5("hector") + "', 1600, 0, null, date('now'), 'DE')");
          statement.executeUpdate("insert into jugadores values('Yusep', '" + Util.getMD5("yusep") + "', 1600, 0, null, date('now'), 'ES')");
          statement.executeUpdate("insert into jugadores values('Terrion', '" + Util.getMD5("swyvern79") + "', 1600, 0, 'tejedor@gmail.com', date('now'), 'ES')");
          statement.executeUpdate("insert into jugadores values('Terrion2', '" + Util.getMD5("swyvern79") + "', 1600, 0, null, date('now'), 'ES')");
          statement.executeUpdate("insert into jugadores values('Charmed', 'ba9f9c72cb0514a4c9724d7de2289c3f', 1600, 0, 'alberto_vicente_lopez@yahoo.es', '2020-08-25', 'ES')");
          statement.executeUpdate("insert into jugadores values('Rodejet', 'b6b853582c21845088e8cf529d5a707d', 1600, 0, 'rodejet@gmail.com', '2020-08-26', 'ES')");
          statement.executeUpdate("insert into jugadores values('Hivenfour', '5f449b146ce14c780eee323dfc5391e8', 1600, 0, 'hivenfour@gmail.com', '2020-08-31', 'ES')");

          /*ResultSet rs2 = *///statement.executeUpdate("delete from jugadores where nombre='Terrion4'");
//          System.out.println(rs2.getObject(1));
//          rs2.close();

          ResultSet rs = statement.executeQuery("select rowid, * from jugadores");

          for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
	    		System.out.print(String.format("%-33s", rs.getMetaData().getColumnName(i)));
	      }
          System.out.println();
          for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
	    		System.out.print("-------------------------------- ");
	      }
          System.out.println();

      	  while(rs.next()) {
            // read the result set
        	for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
        		System.out.print(String.format("%-33s", rs.getObject(i)));
        	}
        	System.out.println();
          }
        }
        catch(SQLException e)
        {
          // if the error message is "out of memory",
          // it probably means no database file is found
          e.printStackTrace();
        }
        finally
        {
          try
          {
            if(connection != null)
              connection.close();
          }
          catch(SQLException e)
          {
            // connection close failed.
            System.err.println(e.getMessage());
          }
        }
      }
    }