import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX.TIPO_FACTORX;
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.bo.FactorxBO.FORMACIONES;
import com.formulamanager.sokker.bo.FactorxJugadorBO;
import com.formulamanager.sokker.entity.factorx.JugadorFactorX;
import com.formulamanager.sokker.entity.factorx.JugadorFactorX.DEMARCACION;
import com.formulamanager.sokker.persistence.SqlitePersistence;

public final class FactorxPlayerModelHarness {
    public static void main(String[] args) throws Exception {
        String previousRoot=FactorxJugadorBO.PATH_BASE;
        Path root=Files.createTempDirectory("factorx-player-model-");
        Path factorxDirectory=Files.createDirectories(root.resolve("factorx"));
        Path assistant=Files.createTempDirectory("asistente-player-model-");
        FactorxJugadorBO.setPathBase(root.toString()+File.separator);
        try {
            for(TIPO_FACTORX type:TIPO_FACTORX.values()) {
                int pid=7000+type.ordinal(), age=type==TIPO_FACTORX.senior?23:20;
                writeLegacyPlayerFile(factorxDirectory.resolve("config.properties"+type.getSufijo()),pid,age,1100);
                writeLegacyPlayerFile(factorxDirectory.resolve("historico.properties30"+type.getSufijo()),pid,age,1099);
                writeLegacyPlayerFile(factorxDirectory.resolve("config.properties1100"+type.getSufijo()),pid,age,1100);
                Properties formation=new Properties(); formation.setProperty("formacion","_541");
                try(FileOutputStream out=new FileOutputStream(factorxDirectory.resolve("factorx.properties"+type.getSufijo()).toFile())) {
                    formation.store(out,null);
                }
            }
            SqlitePersistence.initialize(assistant.toFile(),factorxDirectory.toFile());
            for(TIPO_FACTORX type:TIPO_FACTORX.values()) {
                JugadorFactorX player=new JugadorFactorX();
                player.setPid(7000+type.ordinal()); player.setNombre("Player "+type.name());
                player.setEquipo("Team"); player.setTid(8000+type.ordinal());
                player.setEdad(type==TIPO_FACTORX.senior?23:20); player.setValor(250000);
                player.setJornada(1100); player.setDemarcacion(DEMARCACION.MID);
                player.setCondicion(2); player.setRapidez(3); player.setTecnica(2); player.setPases(2);
                player.setPorteria(1); player.setDefensa(2); player.setCreacion(3); player.setAnotacion(2);
                JugadorFactorX original=new JugadorFactorX();
                original.setJornada(1099); original.setValor(200000);
                original.setCondicion(1); original.setRapidez(1); original.setTecnica(1); original.setPases(1);
                original.setPorteria(1); original.setDefensa(1); original.setCreacion(1); original.setAnotacion(1);
                player.setOriginal(original);

                List<JugadorFactorX> importedCurrent=FactorxJugadorBO.leer_jugadores(type,FactorxBO.getEdicion(1100),true);
                require(importedCurrent.size()==1 && importedCurrent.get(0).getPid()==7000+type.ordinal(),"Legacy current players were not migrated for "+type);
                List<JugadorFactorX> importedHistory=FactorxJugadorBO.leer_jugadores(type,30,false);
                require(importedHistory.size()==1,"Legacy history was not migrated for "+type);
                require(FactorxBO.leer_formacion(type)==FORMACIONES._541,"Legacy formation was not migrated for "+type);
                require(!Files.exists(factorxDirectory.resolve("config.properties"+type.getSufijo())),"Legacy current file was not retired for "+type);
                require(!Files.exists(factorxDirectory.resolve("historico.properties30"+type.getSufijo())),"Legacy history file was not retired for "+type);
                require(!Files.exists(factorxDirectory.resolve("config.properties1100"+type.getSufijo())),"Legacy snapshot file was not retired for "+type);
                require(!Files.exists(factorxDirectory.resolve("factorx.properties"+type.getSufijo())),"Legacy formation file was not retired for "+type);
                try (Connection connection = DriverManager.getConnection("jdbc:sqlite:"
                        + SqlitePersistence.getFactorxDatabaseFile().getAbsolutePath());
                     PreparedStatement snapshot = connection.prepareStatement(
                        "SELECT COUNT(*) FROM factorx_player WHERE scope=? AND record_type='SNAPSHOT' AND snapshot_round=1100")) {
                    snapshot.setString(1, type.name());
                    try (ResultSet result = snapshot.executeQuery()) {
                        require(result.next() && result.getInt(1) == 1,
                                "Legacy Factor X round snapshot was not migrated for " + type);
                    }
                }

                FactorxJugadorBO.grabar_jugadores(Collections.singletonList(player),type,1100,false);
                File database=SqlitePersistence.getFactorxDatabaseFile();
                require(database!=null && database.isFile(),"Missing Factor X SQLite database for "+type);
                List<JugadorFactorX> loaded=FactorxJugadorBO.leer_jugadores(type,FactorxBO.getEdicion(1100),true);
                require(loaded.size()==1,"Player was not loaded for "+type);
                JugadorFactorX copy=loaded.get(0);
                require(copy.getPid().equals(player.getPid()),"PID mismatch for "+type);
                require(copy.getDemarcacion()==DEMARCACION.MID,"Position mismatch for "+type);
                require(copy.getOriginal().getCondicion()==1,"Original skill mismatch for "+type);
                require(copy.getOriginal().getValor()==200000,"Original value mismatch for "+type);
                require(copy.getSubidas()==player.getSubidas(),"Skill rise mismatch for "+type);
            }
            require("_inter".equals(TIPO_FACTORX.Venezuela.getSufijo()),"Venezuela must retain its legacy storage suffix");
            File database=SqlitePersistence.getFactorxDatabaseFile();
            try(Connection connection=DriverManager.getConnection("jdbc:sqlite:"+database.getAbsolutePath());
                PreparedStatement query=connection.prepareStatement("SELECT property_value FROM factorx_property_extension WHERE path=? AND property_key=?")) {
                query.setString(1,"config.properties"); query.setString(2,"0_future_field");
                try(ResultSet result=query.executeQuery()) {
                    require(result.next() && "preserve-me".equals(result.getString(1)),"Unknown legacy Factor X fields were lost");
                }
            }
            System.out.println("FactorxPlayerModelHarness: OK");
        } finally {
            SqlitePersistence.shutdown();
            FactorxJugadorBO.PATH_BASE=previousRoot;
        }
    }

    private static void writeLegacyPlayerFile(Path file,int pid,int age,int round) throws Exception {
        Properties p=new Properties();
        p.setProperty("0_nombre","Legacy player"); p.setProperty("0_pid",String.valueOf(pid));
        p.setProperty("0_equipo","Legacy team"); p.setProperty("0_tid","8000");
        p.setProperty("0_edad",String.valueOf(age)); p.setProperty("0_valor","250000");
        p.setProperty("0_jornada",String.valueOf(round)); p.setProperty("0_demarcacion","MID");
        String[] skills={"condicion","rapidez","tecnica","pases","porteria","defensa","creacion","anotacion"};
        for(String skill:skills) { p.setProperty("0_"+skill,"2"); p.setProperty("0_"+skill+"_orig","1"); }
        p.setProperty("0_jornada_orig",String.valueOf(round-1)); p.setProperty("0_valor_orig","200000");
        p.setProperty("0_future_field","preserve-me");
        try(FileOutputStream out=new FileOutputStream(file.toFile())) { p.store(out,null); }
    }

    private static void require(boolean condition,String message) {
        if(!condition)throw new AssertionError(message);
    }
}
