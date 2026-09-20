package com.formulamanager.sokker.mantenimiento;

public final class AgeWeek13RepairHarness {
    public static void main(String[] args) throws Exception {
        String contaminated =
                "# cabecera\n" +
                "future_key=keep\\:exactly\n" +
                "1=Jugador Uno,1000,DEF,1,20/09/2026 10\\:00,true,0,0,0,,,-1000,180,750,2400,-,false,\\#,-,-false,1001,21,100000,5,5,5,5,5,5,5,5,-0,10,DEF,100.0,-1,1,1,true,1000,20,90000,5,5,5,5,5,5,5,5,-0,10,DEF,100.0,-1,1,1,true,*\n" +
                "2=Jugador Nuevo,1000,MID,1,20/09/2026 10\\:00,true,0,0,0,,,-1000,180,750,2400,-,false,\\#,-,-false,1001,30,100000,5,5,5,5,5,5,5,5,-0,10,MID,100.0,-1,1,1,true,*\n";

        CorregirEdadJornada13.RepairResult fixed = CorregirEdadJornada13.repairContent(contaminated);
        require(fixed.modifiedRecords == 2, "Debe corregir todos los jugadores del fichero contaminado");
        require(fixed.content.contains(",1001,20,100000,"), "Debe restar uno a la edad del jugador con histórico");
        require(fixed.content.contains(",1001,29,100000,"), "Debe restar uno también al jugador nuevo sin histórico");
        require(fixed.content.contains("future_key=keep\\:exactly"), "No debe alterar claves desconocidas");

        CorregirEdadJornada13.RepairResult second = CorregirEdadJornada13.repairContent(fixed.content);
        require(second.modifiedRecords == 0, "La reparación debe ser idempotente");
        require(second.content.equals(fixed.content), "Una segunda ejecución no debe modificar el fichero");

        String ordinary = contaminated.replace(",1001,21,", ",1002,21,").replace(",1001,30,", ",1002,30,");
        CorregirEdadJornada13.RepairResult untouched = CorregirEdadJornada13.repairContent(ordinary);
        require(untouched.modifiedRecords == 0, "No debe tocar jornadas que no sean la última semana de temporada");

        System.out.println("AgeWeek13RepairHarness OK");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
