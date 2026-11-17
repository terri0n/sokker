package com.formulamanager.sokker.persistence;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX.TIPO_FACTORX;
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.entity.factorx.JugadorFactorX;
import com.formulamanager.sokker.entity.factorx.JugadorFactorX.DEMARCACION;

/** Normalized SQLite persistence for Factor X, isolated from Asistente. */
public final class FactorxDAO {
    private static final String[] SKILLS = {"condicion","rapidez","tecnica","pases","porteria","defensa","creacion","anotacion"};
    private static final Pattern PLAYER_KEY = Pattern.compile("([0-9]+)_([a-z_]+)");
    private static final String INSERT = "INSERT INTO factorx_player(scope,record_type,edition,snapshot_round,pid,name,team,tid,age,value,round_no,position,"+
            "condicion,rapidez,tecnica,pases,porteria,defensa,creacion,anotacion,original_value,original_round,"+
            "original_condicion,original_rapidez,original_tecnica,original_pases,original_porteria,original_defensa,original_creacion,original_anotacion)"+
            " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private final SqliteDatabase database;

    public FactorxDAO(SqliteDatabase database) {
        if (database == null || database.isPlayerGroupsEnabled()) throw new IllegalArgumentException("Factor X database required");
        this.database = database;
    }

    public void savePlayers(List<JugadorFactorX> players, TIPO_FACTORX scope, int round, boolean historical)
            throws SQLException {
        String recordType = historical ? "HISTORY" : "CURRENT";
        int edition = historical ? FactorxBO.getEdicion(round)-1 : 0;
        int snapshotRound = 0;
        try (Connection connection=database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement delete=connection.prepareStatement(
                        "DELETE FROM factorx_player WHERE scope=? AND record_type=? AND edition=? AND snapshot_round=?")) {
                    delete.setString(1,scope.name()); delete.setString(2,recordType);
                    delete.setInt(3,edition); delete.setInt(4,snapshotRound); delete.executeUpdate();
                }
                insertPlayers(connection,players,scope,recordType,edition,snapshotRound);
                if (historical || roundMod(round)==0) {
                    try (PreparedStatement delete=connection.prepareStatement(
                            "DELETE FROM factorx_player WHERE scope=? AND record_type='SNAPSHOT' AND snapshot_round=?")) {
                        delete.setString(1,scope.name()); delete.setInt(2,round); delete.executeUpdate();
                    }
                    insertPlayers(connection,players,scope,"SNAPSHOT",FactorxBO.getEdicion(round),round);
                }
                connection.commit();
            } catch(Exception e) {
                connection.rollback();
                if(e instanceof SQLException) throw (SQLException)e;
                throw new SQLException("Cannot save Factor X players",e);
            } finally { connection.setAutoCommit(true); }
        }
    }

    public List<JugadorFactorX> loadPlayers(TIPO_FACTORX scope,int edition,boolean current) throws SQLException {
        String recordType=current?"CURRENT":"HISTORY";
        try(Connection connection=database.openConnection();
            PreparedStatement statement=connection.prepareStatement(
                "SELECT * FROM factorx_player WHERE scope=? AND record_type=? AND edition=? AND snapshot_round=0 " +
                "AND ((scope='senior' AND age>=21) OR (scope<>'senior' AND age<21)) ORDER BY pid")) {
            statement.setString(1,scope.name()); statement.setString(2,recordType); statement.setInt(3,current?0:edition);
            List<JugadorFactorX> players=new ArrayList<JugadorFactorX>();
            try(ResultSet result=statement.executeQuery()) {
                while(result.next()) players.add(readPlayer(result,current?edition:edition));
            }
            return players;
        }
    }

    public void saveFormation(TIPO_FACTORX scope,String formation) throws SQLException {
        try(Connection connection=database.openConnection();
            PreparedStatement statement=connection.prepareStatement(
                "INSERT OR REPLACE INTO factorx_formation(scope,formation) VALUES(?,?)")) {
            statement.setString(1,scope.name()); statement.setString(2,formation); statement.executeUpdate();
        }
    }

    public String loadFormation(TIPO_FACTORX scope) throws SQLException {
        try(Connection connection=database.openConnection();
            PreparedStatement statement=connection.prepareStatement("SELECT formation FROM factorx_formation WHERE scope=?")) {
            statement.setString(1,scope.name());
            try(ResultSet result=statement.executeQuery()) { return result.next()?result.getString(1):null; }
        }
    }

    /** Imports all legacy Factor X player, snapshot, history and formation documents atomically. */
    public void migrateLegacy(SqlitePropertiesStore legacy,File root) throws IOException,SQLException {
        final String marker="factorx-normalized-v1";
        List<String> documents=legacy.list(null,null);
        try(Connection connection=database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean done;
                try(PreparedStatement q=connection.prepareStatement("SELECT 1 FROM factorx_migration WHERE name=?")) {
                    q.setString(1,marker); try(ResultSet r=q.executeQuery()){done=r.next();}
                }
                for(String path:documents) {
                    if(!isFactorxDocument(path)) continue;
                    if(!done) migrateDocument(connection,path,legacy.load(path));
                    try(PreparedStatement d=connection.prepareStatement("DELETE FROM legacy_properties_file WHERE path=?")) {
                        d.setString(1,path); d.executeUpdate();
                    }
                }
                if(!done) try(PreparedStatement insert=connection.prepareStatement(
                        "INSERT INTO factorx_migration(name,completed_at) VALUES(?,?)")) {
                    insert.setString(1,marker); insert.setLong(2,System.currentTimeMillis()); insert.executeUpdate();
                }
                connection.commit();
            } catch(Exception e) {
                connection.rollback();
                if(e instanceof IOException) throw (IOException)e;
                if(e instanceof SQLException) throw (SQLException)e;
                throw new SQLException("Factor X migration failed",e);
            } finally { connection.setAutoCommit(true); }
        }
        // Only remove source files after the complete normalized import committed.
        for(String path:documents) if(isFactorxDocument(path)) {
            File source=new File(root,path.replace('/',File.separatorChar));
            if(source.exists()&&!source.delete()) throw new IOException("Cannot remove migrated Factor X file: "+source);
        }
    }

    private void migrateDocument(Connection connection,String path,Properties properties) throws SQLException {
        if(properties==null) throw new SQLException("Missing imported Factor X document: "+path);
        if(path.startsWith("factorx.properties")) {
            TIPO_FACTORX scope=scope(path.substring("factorx.properties".length()));
            String formation=properties.getProperty("formacion");
            if(formation!=null) try(PreparedStatement s=connection.prepareStatement(
                    "INSERT OR REPLACE INTO factorx_formation(scope,formation) VALUES(?,?)")) {
                s.setString(1,scope.name()); s.setString(2,formation); s.executeUpdate();
            }
            preserveUnknown(connection,path,properties);
            return;
        }
        String base;
        String suffix;
        String recordType;
        int edition=0, snapshotRound=0;
        if(path.startsWith("historico.properties")) {
            base="historico.properties"; recordType="HISTORY";
            String tail=path.substring(base.length());
            Matcher m=Pattern.compile("([0-9]+)(.*)").matcher(tail);
            if(!m.matches()) throw new SQLException("Invalid Factor X history path: "+path);
            edition=Integer.parseInt(m.group(1)); suffix=m.group(2);
        } else if(path.startsWith("config.properties")) {
            base="config.properties";
            String tail=path.substring(base.length());
            Matcher m=Pattern.compile("([0-9]+)(.*)").matcher(tail);
            if(m.matches()) { recordType="SNAPSHOT"; snapshotRound=Integer.parseInt(m.group(1)); suffix=m.group(2); edition=FactorxBO.getEdicion(snapshotRound); }
            else { recordType="CURRENT"; suffix=tail; }
        } else return;
        TIPO_FACTORX scope=scope(suffix);
        if(properties.isEmpty()) { preserveUnknown(connection,path,properties); return; }
        int max=-1;
        for(String key:properties.stringPropertyNames()) {
            Matcher m=PLAYER_KEY.matcher(key);
            if(m.matches()&&"pid".equals(m.group(2))) max=Math.max(max,Integer.parseInt(m.group(1)));
        }
        List<JugadorFactorX> players=new ArrayList<JugadorFactorX>();
        for(int i=0;i<=max;i++) {
            String prefix=i+"_";
            if(properties.getProperty(prefix+"pid")==null) continue;
            JugadorFactorX p=new JugadorFactorX(), original=new JugadorFactorX();
            p.setPid(integer(properties,prefix+"pid",path)); p.setNombre(properties.getProperty(prefix+"nombre"));
            p.setEquipo(properties.getProperty(prefix+"equipo")); p.setTid(integer(properties,prefix+"tid",path));
            p.setEdad(integer(properties,prefix+"edad",path)); p.setValor(integer(properties,prefix+"valor",path));
            p.setJornada(integer(properties,prefix+"jornada",path));
            p.setDemarcacion(DEMARCACION.valueOf(required(properties,prefix+"demarcacion",path)));
            for(String skill:SKILLS) {
                setSkill(p,skill,integer(properties,prefix+skill,path));
                setSkill(original,skill,integer(properties,prefix+skill+"_orig",path));
            }
            original.setJornada(integer(properties,prefix+"jornada_orig",path));
            if(properties.getProperty(prefix+"valor_orig")!=null) original.setValor(integer(properties,prefix+"valor_orig",path));
            p.setOriginal(original); p.setEdicion(recordType.equals("HISTORY")?edition:FactorxBO.getEdicion(p.getJornada()));
            p.calcular_puntos(); players.add(p);
        }
        insertPlayers(connection,players,scope,recordType,edition,snapshotRound);
        preserveUnknown(connection,path,properties);
    }

    private static void insertPlayers(Connection c,List<JugadorFactorX> players,TIPO_FACTORX scope,String kind,int edition,int snapshot)
            throws SQLException {
        try(PreparedStatement s=c.prepareStatement(INSERT)) {
            for(JugadorFactorX p:players) {
                JugadorFactorX o=p.getOriginal();
                int i=1;
                s.setString(i++,scope.name()); s.setString(i++,kind); s.setInt(i++,edition); s.setInt(i++,snapshot);
                s.setInt(i++,p.getPid()); s.setString(i++,p.getNombre()); s.setString(i++,p.getEquipo());
                setNullable(s,i++,p.getTid()); setNullable(s,i++,p.getEdad()); setNullable(s,i++,p.getValor());
                setNullable(s,i++,p.getJornada()); s.setString(i++,p.getDemarcacion()==null?null:p.getDemarcacion().name());
                for(String skill:SKILLS) setNullable(s,i++,getSkill(p,skill));
                setNullable(s,i++,o==null?null:o.getValor()); setNullable(s,i++,o==null?null:o.getJornada());
                for(String skill:SKILLS) setNullable(s,i++,o==null?null:getSkill(o,skill));
                s.addBatch();
            }
            s.executeBatch();
        }
    }

    private static JugadorFactorX readPlayer(ResultSet r,int edition) throws SQLException {
        JugadorFactorX p=new JugadorFactorX(),o=new JugadorFactorX();
        p.setPid(r.getInt("pid")); p.setNombre(r.getString("name")); p.setEquipo(r.getString("team"));
        p.setTid(nullableInt(r,"tid")); p.setEdad(nullableInt(r,"age")); p.setValor(nullableInt(r,"value"));
        p.setJornada(nullableInt(r,"round_no"));
        String position=r.getString("position"); if(position!=null)p.setDemarcacion(DEMARCACION.valueOf(position));
        for(String skill:SKILLS){setSkill(p,skill,nullableInt(r,skill));setSkill(o,skill,nullableInt(r,"original_"+skill));}
        o.setValor(nullableInt(r,"original_value")); o.setJornada(nullableInt(r,"original_round"));
        p.setOriginal(o); p.setEdicion(edition); p.calcular_puntos(); return p;
    }

    private static void preserveUnknown(Connection c,String path,Properties p) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("INSERT OR REPLACE INTO factorx_property_extension(path,property_key,property_value) VALUES(?,?,?)")) {
            for(String key:p.stringPropertyNames()) {
                Matcher m=PLAYER_KEY.matcher(key);
                boolean known=m.matches() && (key.endsWith("_pid")||key.endsWith("_nombre")||key.endsWith("_equipo")||
                    key.endsWith("_tid")||key.endsWith("_edad")||key.endsWith("_valor")||key.endsWith("_jornada")||
                    key.endsWith("_demarcacion")||key.endsWith("_condicion")||key.endsWith("_rapidez")||key.endsWith("_tecnica")||
                    key.endsWith("_pases")||key.endsWith("_porteria")||key.endsWith("_defensa")||key.endsWith("_creacion")||
                    key.endsWith("_anotacion")||key.endsWith("_condicion_orig")||key.endsWith("_rapidez_orig")||
                    key.endsWith("_tecnica_orig")||key.endsWith("_pases_orig")||key.endsWith("_porteria_orig")||
                    key.endsWith("_defensa_orig")||key.endsWith("_creacion_orig")||key.endsWith("_anotacion_orig")||
                    key.endsWith("_jornada_orig")||key.endsWith("_valor_orig"));
                if(!known && !"formacion".equals(key)){s.setString(1,path);s.setString(2,key);s.setString(3,p.getProperty(key));s.addBatch();}
            }
            s.executeBatch();
        }
    }

    private static boolean isFactorxDocument(String path) {
        return path.matches("(?:config|historico|factorx)\\.properties.*");
    }
    private static TIPO_FACTORX scope(String suffix) throws SQLException {
        for(TIPO_FACTORX t:TIPO_FACTORX.values()) if(t.getSufijo().equals(suffix)) return t;
        throw new SQLException("Unknown Factor X scope suffix: "+suffix);
    }
    private static String required(Properties p,String key,String path) throws SQLException {
        String value=p.getProperty(key); if(value==null)throw new SQLException("Missing "+key+" in "+path); return value;
    }
    private static Integer integer(Properties p,String key,String path) throws SQLException {
        try{return Integer.valueOf(required(p,key,path));}catch(NumberFormatException e){throw new SQLException("Invalid integer "+key+" in "+path,e);}
    }
    private static void setSkill(JugadorFactorX p,String key,Integer value) {
        if("condicion".equals(key))p.setCondicion(value); else if("rapidez".equals(key))p.setRapidez(value);
        else if("tecnica".equals(key))p.setTecnica(value); else if("pases".equals(key))p.setPases(value);
        else if("porteria".equals(key))p.setPorteria(value); else if("defensa".equals(key))p.setDefensa(value);
        else if("creacion".equals(key))p.setCreacion(value); else if("anotacion".equals(key))p.setAnotacion(value);
    }
    private static Integer getSkill(JugadorFactorX p,String key) {
        if("condicion".equals(key))return p.getCondicion(); if("rapidez".equals(key))return p.getRapidez();
        if("tecnica".equals(key))return p.getTecnica(); if("pases".equals(key))return p.getPases();
        if("porteria".equals(key))return p.getPorteria(); if("defensa".equals(key))return p.getDefensa();
        if("creacion".equals(key))return p.getCreacion(); return p.getAnotacion();
    }
    private static void setNullable(PreparedStatement s,int index,Integer value)throws SQLException {
        if(value==null)s.setNull(index,java.sql.Types.INTEGER);else s.setInt(index,value);
    }
    private static Integer nullableInt(ResultSet r,String column)throws SQLException {
        int value=r.getInt(column);return r.wasNull()?null:Integer.valueOf(value);
    }
    private static int roundMod(int round){return round<976?round%16:(round-976)%13;}
}
