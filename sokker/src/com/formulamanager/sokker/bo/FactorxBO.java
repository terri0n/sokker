package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX.TIPO_FACTORX;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.entity.factorx.JugadorFactorX;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

public final class FactorxBO {
    public static final String FACTORX_PROPERTIES = FactorxJugadorBO.PATH_BASE + "factorx/factorx.properties";

    public enum FORMACIONES {
        _541("5-4-1"), _532("5-3-2"), _523("5-2-3"), _442("4-4-2"),
        _451("4-5-1"), _433("4-3-3"), _352("3-5-2"), _343("3-4-3"), _253("2-5-3");
        private final String texto;
        FORMACIONES(String texto) { this.texto=texto; }
        @Override public String toString() { return texto; }
    }

    private FactorxBO() {}

    public static void grabar_formacion(FORMACIONES formation,TIPO_FACTORX type) throws IOException {
        Properties properties=new Properties();
        properties.setProperty("formacion",formation.name());
        Util.guardar_properties(properties,FACTORX_PROPERTIES+type.getSufijo());
    }

    public static FORMACIONES leer_formacion(TIPO_FACTORX type) {
        try {
            Properties properties=Util.cargar_properties(new File(FACTORX_PROPERTIES+type.getSufijo()));
            if(properties==null)return FORMACIONES._442;
            return FORMACIONES.valueOf(properties.getProperty("formacion"));
        } catch(IOException e) { throw new IllegalStateException("Cannot read Factor X formation for "+type,e); }
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
