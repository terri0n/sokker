import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import com.formulamanager.sokker.acciones.factorx.SERVLET_FACTORX.TIPO_FACTORX;
import com.formulamanager.sokker.bo.FactorxBO;
import com.formulamanager.sokker.bo.FactorxJugadorBO;
import com.formulamanager.sokker.entity.factorx.JugadorFactorX;
import com.formulamanager.sokker.entity.factorx.JugadorFactorX.DEMARCACION;

public final class FactorxPlayerModelHarness {
    public static void main(String[] args) throws Exception {
        String previousRoot=FactorxJugadorBO.PATH_BASE;
        Path root=Files.createTempDirectory("factorx-player-model-");
        Files.createDirectories(root.resolve("factorx"));
        FactorxJugadorBO.setPathBase(root.toString()+File.separator);
        try {
            for(TIPO_FACTORX type:TIPO_FACTORX.values()) {
                JugadorFactorX player=new JugadorFactorX();
                player.setPid(7000+type.ordinal());
                player.setNombre("Player "+type.name());
                player.setEquipo("Team");
                player.setTid(8000+type.ordinal());
                player.setEdad(type==TIPO_FACTORX.senior?23:20);
                player.setValor(250000);
                player.setJornada(1100);
                player.setDemarcacion(DEMARCACION.MID);
                player.setCondicion(2); player.setRapidez(3); player.setTecnica(2); player.setPases(2);
                player.setPorteria(1); player.setDefensa(2); player.setCreacion(3); player.setAnotacion(2);
                JugadorFactorX original=new JugadorFactorX();
                original.setJornada(1099); original.setValor(200000);
                original.setCondicion(1); original.setRapidez(1); original.setTecnica(1); original.setPases(1);
                original.setPorteria(1); original.setDefensa(1); original.setCreacion(1); original.setAnotacion(1);
                player.setOriginal(original);
                FactorxJugadorBO.grabar_jugadores(Collections.singletonList(player),type,1100,false);

                String suffix=type.getSufijo();
                File current=new File(FactorxJugadorBO.CONFIG_PROPERTIES+suffix);
                require(current.isFile(),"Missing current store for "+type+": "+current);
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
            System.out.println("FactorxPlayerModelHarness: OK");
        } finally {
            FactorxJugadorBO.PATH_BASE=previousRoot;
        }
    }
    private static void require(boolean condition,String message) {
        if(!condition)throw new AssertionError(message);
    }
}
