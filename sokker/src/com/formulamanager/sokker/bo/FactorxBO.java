package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX.TIPO_FACTORX;
import com.formulamanager.sokker.entity.factorx.JugadorFactorX;
import com.formulamanager.sokker.persistence.FactorxDAO;
import com.formulamanager.sokker.persistence.SqliteDatabase;
import com.formulamanager.sokker.persistence.SqlitePersistence;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

public final class FactorxBO {

    public enum FORMACIONES {
        _541("5-4-1"), _532("5-3-2"), _523("5-2-3"), _442("4-4-2"),
        _451("4-5-1"), _433("4-3-3"), _352("3-5-2"), _343("3-4-3"), _253("2-5-3");
        private final String texto;
        FORMACIONES(String texto) { this.texto=texto; }
        @Override public String toString() { return texto; }
    }

    private FactorxBO() {}

    private static FactorxDAO dao() throws IOException {
        File database=SqlitePersistence.getFactorxDatabaseFile();
        if(database==null)throw new IOException("Factor X SQLite is not initialized");
        return new FactorxDAO(new SqliteDatabase(database,false));
    }

    public static void grabar_formacion(FORMACIONES formation,TIPO_FACTORX type) throws IOException {
        try { dao().saveFormation(type,formation.name()); }
        catch(java.sql.SQLException e) { throw new IOException("Cannot save Factor X formation for "+type,e); }
    }

    public static FORMACIONES leer_formacion(TIPO_FACTORX type) {
        try {
            String formation=dao().loadFormation(type);
            return formation==null?FORMACIONES._442:FORMACIONES.valueOf(formation);
        } catch(Exception e) { throw new IllegalStateException("Cannot read Factor X formation for "+type,e); }
    }

    public static void actualizar_todos(TIPO_FACTORX type,WebClient browser,int currentRound)
            throws FailingHttpStatusCodeException,java.net.MalformedURLException,IOException {
        java.util.List<JugadorFactorX> players=FactorxJugadorBO.leer_jugadores(type,getEdicion(currentRound),true);
        FactorxJugadorBO.actualizar_jugadores(browser,players,currentRound);
        java.util.Collections.sort(players);
        FactorxJugadorBO.grabar_jugadores(players,type,currentRound,false);
        for(JugadorFactorX player:players)FactorxJugadorBO.escribir_anuncio(browser,player,false);
    }

    public static int getEdicion(int round) {
        return round+1<976?(round+1)/16-25:(round+1-976)/13+36;
    }

    public static JugadorFactorX buscar(WebClient browser,Integer pid,boolean force,int currentRound)
            throws FailingHttpStatusCodeException,java.net.MalformedURLException,IOException {
        JugadorFactorX player=new JugadorFactorX();
        player.setPid(pid);
        if(!FactorxJugadorBO.actualizar_jugador(browser,player,force))return null;
        player.setJornada(currentRound);
        player.setOriginal(new JugadorFactorX(player));
        player.setActualizado(true);
        return player;
    }

    public static void grabar_jugadores(java.util.List<JugadorFactorX> players,TIPO_FACTORX type,int currentRound,boolean historical)
            throws IOException {
        FactorxJugadorBO.grabar_jugadores(players,type,currentRound,historical);
    }

    public static void actualizar_jugadores(WebClient browser,java.util.List<JugadorFactorX> players,int currentRound)
            throws FailingHttpStatusCodeException,java.net.MalformedURLException,IOException {
        java.util.Iterator<JugadorFactorX> iterator=players.iterator();
        while(iterator.hasNext()) {
            JugadorFactorX player=iterator.next();
            if(FactorxJugadorBO.actualizar_jugador(browser,player,false)) {
                player.setActualizado(player.getJornada()==null||!player.getJornada().equals(currentRound));
                player.setJornada(currentRound);
            }
            if(player.getEquipo()==null)iterator.remove();
        }
    }
}
