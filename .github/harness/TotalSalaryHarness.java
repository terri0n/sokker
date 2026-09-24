import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.formulamanager.sokker.acciones.asistente.Asistente;
import com.formulamanager.sokker.entity.Jugador;

public class TotalSalaryHarness {
    public static void main(String[] args) throws Exception {
        Jugador first = new Jugador();
        first.setSalario(Integer.valueOf(100));
        Jugador second = new Jugador();
        second.setSalario(Integer.valueOf(250));

        Method method;
        try {
            method = Asistente.class.getDeclaredMethod("getSalario", List.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Asistente no conserva el calculo del salario total recuperado del compilado", e);
        }
        method.setAccessible(true);
        BigDecimal total = (BigDecimal) method.invoke(null, Arrays.asList(first, second));
        if (total.compareTo(new BigDecimal("350")) != 0) {
            throw new AssertionError("Salario total incorrecto: " + total);
        }

        String servlet = new String(Files.readAllBytes(Paths.get(
                "sokker/src/com/formulamanager/sokker/acciones/asistente/Asistente.java")), StandardCharsets.UTF_8);
        if (!servlet.contains("request.setAttribute(\"salario_jugadores\", getSalario(lista_jugadores));")) {
            throw new AssertionError("Asistente no expone salario_jugadores al JSP");
        }

        System.out.println("Total salary compatibility OK");
    }
}
