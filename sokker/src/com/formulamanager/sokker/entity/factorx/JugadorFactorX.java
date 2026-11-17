package com.formulamanager.sokker.entity.factorx;

import java.math.BigDecimal;
import java.util.Comparator;

import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX.TIPO_FACTORX;
import com.formulamanager.sokker.auxiliares.Util;

/** Factor X player. This model deliberately has no dependency on Asistente entities. */
public final class JugadorFactorX implements Comparable<JugadorFactorX> {
    public enum DEMARCACION { GK, DEF, MID, ATT }

    private static final String[] PUNTOS_CONDICION = {"0.0","0.1","0.2","0.3","0.4","0.5","0.6","0.7","0.8","1.0","1.2","1.5"};
    private static final String[] PUNTOS_RAPIDEZ = {"0.0","1.4","1.7","1.9","2.2","2.4","2.6","3.0","3.4","3.7","4.1","4.6","5.0","5.5","6.0","7.2","8.4","9.6","11.5"};
    private static final String[] PUNTOS_OTROS = {"0.0","1.2","1.4","1.6","1.8","2.0","2.2","2.5","2.8","3.1","3.4","3.8","4.2","4.6","5.0","6.0","7.0","8.0","9.5"};

    private String nombre;
    private Integer pid;
    private String equipo;
    private Integer tid;
    private Integer edad = 100;
    private Integer valor;
    private Integer jornada;
    private Integer edicion;
    private Integer pais;
    private DEMARCACION demarcacion;
    private Integer condicion = 0, rapidez = 0, tecnica = 0, pases = 0;
    private Integer porteria = 0, defensa = 0, creacion = 0, anotacion = 0;
    private BigDecimal puntos = BigDecimal.ZERO;
    private JugadorFactorX original;
    private String icono, icono_foro;
    private boolean actualizado;

    public JugadorFactorX() { }

    public JugadorFactorX(JugadorFactorX source) {
        nombre=source.nombre; pid=source.pid; equipo=source.equipo; tid=source.tid;
        edad=source.edad; valor=source.valor; jornada=source.jornada; edicion=source.edicion;
        pais=source.pais; demarcacion=source.demarcacion; condicion=source.condicion;
        rapidez=source.rapidez; tecnica=source.tecnica; pases=source.pases;
        porteria=source.porteria; defensa=source.defensa; creacion=source.creacion;
        anotacion=source.anotacion; puntos=source.puntos; actualizado=source.actualizado;
    }

    public void calcular_puntos() {
        if (edad < 21 || jornada < 1080) puntos=BigDecimal.ZERO;
        else puntos=new BigDecimal(edad-21);
        puntos=addSkillPoints(puntos, original.condicion, condicion, PUNTOS_CONDICION);
        puntos=addSkillPoints(puntos, original.rapidez, rapidez, PUNTOS_RAPIDEZ);
        puntos=addSkillPoints(puntos, original.tecnica, tecnica, PUNTOS_OTROS);
        puntos=addSkillPoints(puntos, original.pases, pases, PUNTOS_OTROS);
        puntos=addSkillPoints(puntos, original.porteria, porteria, PUNTOS_OTROS);
        puntos=addSkillPoints(puntos, original.defensa, defensa, PUNTOS_OTROS);
        puntos=addSkillPoints(puntos, original.creacion, creacion, PUNTOS_OTROS);
        puntos=addSkillPoints(puntos, original.anotacion, anotacion, PUNTOS_OTROS);
    }

    private static BigDecimal addSkillPoints(BigDecimal total, int before, int after, String[] values) {
        for (int i=before; i<after; i++) total=total.add(new BigDecimal(values[i+1]));
        return total;
    }

    public Integer max_habilidad() {
        return Math.max(Math.max(Math.max(condicion, rapidez), Math.max(tecnica, pases)),
                Math.max(Math.max(porteria, defensa), Math.max(creacion, anotacion)));
    }

    public boolean validar(TIPO_FACTORX tipo) {
        if ((tipo == TIPO_FACTORX.senior && (edad < 21 || edad > 27))
                || (tipo != TIPO_FACTORX.senior && (edad < 18 || edad > 20))) return false;
        if (tipo == TIPO_FACTORX.senior) {
            if (edad <= 22) {
                if (demarcacion == DEMARCACION.GK && porteria+rapidez+pases>=27 && porteria>=13) return true;
                if (demarcacion == DEMARCACION.DEF && defensa+rapidez>=22 && pases+tecnica+creacion>=11) return true;
                if (demarcacion == DEMARCACION.MID && rapidez+creacion+pases+tecnica+defensa>=39) return true;
                if (demarcacion == DEMARCACION.ATT && rapidez+tecnica+anotacion>=29) return true;
            } else if (edad <= 24) {
                if (demarcacion == DEMARCACION.GK && porteria+rapidez+pases>=32 && porteria>=14) return true;
                if (demarcacion == DEMARCACION.DEF && defensa+rapidez>=25 && pases+tecnica+creacion>=16) return true;
                if (demarcacion == DEMARCACION.MID && rapidez+creacion+pases+tecnica+defensa>=48) return true;
                if (demarcacion == DEMARCACION.ATT && rapidez+tecnica+anotacion>=34) return true;
            } else {
                if (demarcacion == DEMARCACION.GK && porteria+rapidez+pases>=36 && porteria>=15) return true;
                if (demarcacion == DEMARCACION.DEF && defensa+rapidez>=28 && pases+tecnica+creacion>=21) return true;
                if (demarcacion == DEMARCACION.MID && rapidez+creacion+pases+tecnica+defensa>=57) return true;
                if (demarcacion == DEMARCACION.ATT && rapidez+tecnica+anotacion>=39) return true;
            }
        } else {
            int ageOffset=edad-18;
            int minGk=edad==18?9:edad==19?11:12;
            if (demarcacion==DEMARCACION.GK && porteria>=minGk && porteria+rapidez+pases>=17+ageOffset*2) return true;
            int minDef=edad==18?5:edad==19?7:8;
            if (demarcacion==DEMARCACION.DEF && defensa+rapidez>=13+ageOffset*2 && creacion+tecnica+pases>=minDef) return true;
            int minMid=edad==18?23:edad==19?27:30;
            if (demarcacion==DEMARCACION.MID && (creacion+rapidez+tecnica+pases>=minMid
                    ||creacion+rapidez+tecnica+defensa>=minMid||creacion+rapidez+defensa+pases>=minMid
                    ||creacion+defensa+tecnica+pases>=minMid||defensa+rapidez+tecnica+pases>=minMid)) return true;
            if (demarcacion==DEMARCACION.ATT && anotacion+rapidez+tecnica>=18+ageOffset*3) return true;
        }
        return false;
    }

    public Integer getJornadaMod() {
        return jornada < 976 ? jornada % 16 : (jornada - 976) % 13;
    }
    public Integer getSubidas() {
        return condicion-original.condicion+rapidez-original.rapidez+tecnica-original.tecnica+pases-original.pases
                +porteria-original.porteria+defensa-original.defensa+creacion-original.creacion+anotacion-original.anotacion;
    }
    public Integer getDifValor() { return valor==null||original.valor==null?0:valor-original.valor; }
    public String getColor_demarcacion() {
        if(demarcacion==null)return "silver";
        switch(demarcacion){case GK:return "SkyBlue";case DEF:return "lightgreen";case MID:return "orange";case ATT:return "salmon";default:return "silver";}
    }
    public String getColorDemarcacion() { return getColor_demarcacion(); }

    public static String desp(String lang,String name,Integer value,boolean numbers) { return desp(lang,name,value,numbers,18); }
    public static String desp(String lang,String name,Integer value,boolean numbers,Integer max) {
        String gender;
        try { gender=Util.getTexto(lang,"skills.gender_"+name); } catch(Exception e) { gender="m"; }
        StringBuilder html=new StringBuilder("<select name='").append(name).append("'>");
        for(int i=0;i<=18 && i<=max;i++) {
            html.append("<option value='").append(i).append("' ").append(Integer.valueOf(i).equals(value)?"selected":"")
                .append(">").append(numbers?i+" - ":"").append(Util.getTexto(lang,"skills.skill"+i+gender)).append("</option>");
        }
        return html.append("</select>").toString();
    }
    public static String despDemarcacion(DEMARCACION value) {
        StringBuilder html=new StringBuilder("<select name='demarcacion'>");
        for(DEMARCACION d:DEMARCACION.values()) html.append("<option value='").append(d.name()).append("' ")
                .append(d==value?"selected":"").append(">").append(d.name()).append("</option>");
        return html.append("</select>").toString();
    }
    public static Comparator<JugadorFactorX> comparator_puntos() {
        return (a,b)->-a.getPuntos().compareTo(b.getPuntos());
    }
    public static Comparator<JugadorFactorX> comparator_subidas() {
        return (a,b)->-a.getSubidas().compareTo(b.getSubidas());
    }
    public static Comparator<JugadorFactorX> comparator_valor() {
        return (a,b)->-a.getDifValor().compareTo(b.getDifValor());
    }
    public static Comparator<JugadorFactorX> getComparator() {
        return (a,b)->a.demarcacion==b.demarcacion?-a.valor.compareTo(b.valor)
                :a.demarcacion==null?1:b.demarcacion==null?-1:a.demarcacion.compareTo(b.demarcacion);
    }
    @Override public int compareTo(JugadorFactorX j) {
        int c=puntos.compareTo(j.puntos); if(c!=0)return -c;
        c=original.jornada.compareTo(j.original.jornada); if(c!=0)return -c;
        c=edad.compareTo(j.edad); if(c!=0)return c;
        c=max_habilidad().compareTo(j.max_habilidad()); if(c!=0)return -c;
        return -valor.compareTo(j.valor);
    }
    @Override public boolean equals(Object other) { return other instanceof JugadorFactorX && pid.equals(((JugadorFactorX)other).pid); }
    @Override public int hashCode() { return pid==null?0:pid.hashCode(); }

    public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
    public Integer getPid(){return pid;} public void setPid(Integer v){pid=v;}
    public String getEquipo(){return equipo;} public void setEquipo(String v){equipo=v;}
    public Integer getTid(){return tid;} public void setTid(Integer v){tid=v;}
    public Integer getEdad(){return edad;} public void setEdad(Integer v){edad=v;}
    public Integer getValor(){return valor;} public void setValor(Integer v){valor=v;}
    public Integer getJornada(){return jornada;} public void setJornada(Integer v){jornada=v;}
    public Integer getEdicion(){return edicion;} public void setEdicion(Integer v){edicion=v;}
    public Integer getPais(){return pais;} public void setPais(Integer v){pais=v;}
    public DEMARCACION getDemarcacion(){return demarcacion;} public void setDemarcacion(DEMARCACION v){demarcacion=v;}
    public BigDecimal getPuntos(){return puntos;} public void setPuntos(BigDecimal v){puntos=v;}
    public JugadorFactorX getOriginal(){return original;} public void setOriginal(JugadorFactorX v){original=v;}
    public String getIcono(){return icono;} public void setIcono(String v){icono=v;}
    public String getIcono_foro(){return icono_foro;} public void setIcono_foro(String v){icono_foro=v;}
    public boolean isActualizado(){return actualizado;} public void setActualizado(boolean v){actualizado=v;}
    public Integer getCondicion(){return condicion;} public void setCondicion(Integer v){condicion=v;}
    public Integer getRapidez(){return rapidez;} public void setRapidez(Integer v){rapidez=v;}
    public Integer getTecnica(){return tecnica;} public void setTecnica(Integer v){tecnica=v;}
    public Integer getPases(){return pases;} public void setPases(Integer v){pases=v;}
    public Integer getPorteria(){return porteria;} public void setPorteria(Integer v){porteria=v;}
    public Integer getDefensa(){return defensa;} public void setDefensa(Integer v){defensa=v;}
    public Integer getCreacion(){return creacion;} public void setCreacion(Integer v){creacion=v;}
    public Integer getAnotacion(){return anotacion;} public void setAnotacion(Integer v){anotacion=v;}
}
