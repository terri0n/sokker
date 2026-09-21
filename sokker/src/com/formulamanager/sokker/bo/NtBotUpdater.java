package com.formulamanager.sokker.bo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import com.formulamanager.sokker.auxiliares.JSONUtil;
import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.entity.Jugador;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.jayway.jsonpath.JsonPath;

/**
 * Completa en segundo plano los datos de propietario/bot de los jugadores de una selección.
 *
 * El id de selección se recibe ya capturado por el request que inició la actualización. No se
 * consulta el Usuario de sesión desde el hilo: el usuario puede cambiar de selección mientras
 * esta tarea sigue ejecutándose.
 */
public final class NtBotUpdater {
    private NtBotUpdater() {
    }

    public static void programar(final Integer tid, final int jornada_actual) {
        if (tid == null || tid >= NtdbBO.MAX_ID_SELECCION) {
            return;
        }

        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new Navegador(false, null) {
                        @Override
                        protected void execute(WebClient navegador) throws FailingHttpStatusCodeException,
                                MalformedURLException, IOException, LoginException, ParseException {
                            actualizar(tid, jornada_actual, navegador);
                        }
                    };
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        SERVLET_ASISTENTE._log_linea("_EXCEPTIONS",
                                "__TID: " + tid + " -> Error comprobando bots: " + sw.toString() + "\n");
                    } catch (IOException logException) {
                        logException.printStackTrace();
                    }
                }
            }
        }, "nt-bot-refresh-" + tid);
        hilo.start();
    }

    static void actualizar(Integer tid, int jornada_actual, WebClient navegador)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        List<Jugador> jugadores = AsistenteBO.leer_jugadores(tid, null, false, null);
        Map<Integer, Integer> tids = new HashMap<Integer, Integer>();
        Map<Integer, Boolean> bots = new HashMap<Integer, Boolean>();

        // Primero hacemos todas las llamadas. Si una petición falla no guardamos una actualización parcial.
        for (Jugador jugador : jugadores) {
            if (jugador.getPid() == null || jugador.getPid() <= 0) {
                continue;
            }

            Integer tid_jugador = obtener_tid(navegador, jugador.getPid());
            if (tid_jugador != null) {
                tids.put(jugador.getPid(), tid_jugador);
                bots.put(jugador.getPid(), es_bot(navegador, tid_jugador));
            }
        }

        // El trabajo anterior puede tardar. Volvemos a leer el fichero para no reintroducir jugadores
        // eliminados o versiones antiguas si hubo otra actualización de la misma selección mientras tanto.
        List<Jugador> jugadores_actuales = AsistenteBO.leer_jugadores(tid, null, false, null);
        for (Jugador jugador : jugadores_actuales) {
            Integer tid_jugador = tids.get(jugador.getPid());
            Boolean bot = bots.get(jugador.getPid());
            if (tid_jugador != null && bot != null) {
                jugador.setTid(tid_jugador);
                jugador.setBot(bot.booleanValue());
            }
        }

        AsistenteBO.grabar_jugadores(jugadores_actuales, tid, jornada_actual, false);
    }

    static Integer obtener_tid(WebClient navegador, Integer pid)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        Object json = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/player/" + pid);
        return JsonPath.read(json, "$.info.team.id");
    }

    static boolean es_bot(WebClient navegador, Integer tid)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        try {
            JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/team/" + tid + "/owner");
            return false;
        } catch (FailingHttpStatusCodeException e) {
            return true;
        }
    }
}
