package com.formulamanager.sokker.acciones.sete;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;

@WebServlet("/sete/save-tactic")
public class SaveTactic extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        if (request.getSession(false) == null
                || request.getSession(false).getAttribute("login") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "SETE session expired");
            return;
        }

        Object cookieManager = request.getSession(false).getAttribute("seteCookieManager");
        if (!(cookieManager instanceof com.gargoylesoftware.htmlunit.CookieManager)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sokker session expired; log in to SETE again");
            return;
        }

        String tacticId = request.getParameter("id");
        String tacticName = request.getParameter("name");
        String config = request.getParameter("config");
        if (tacticId == null || !tacticId.matches("[0-9]+")
                || tacticName == null || tacticName.trim().isEmpty()
                || config == null || config.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tactic data");
            return;
        }

        WebClient client = Navegador.createSokkerWebClient();
        try {
            client.setCookieManager((com.gargoylesoftware.htmlunit.CookieManager) cookieManager);

            WebRequest apiRequest = new WebRequest(
                    new URL(AsistenteBO.SOKKER_URL + "/api/tactic/" + tacticId),
                    HttpMethod.PUT);
            apiRequest.setAdditionalHeader("Content-Type", "application/json; charset=utf-8");
            apiRequest.setRequestBody("{\"name\":\"" + jsonEscape(tacticName.trim())
                    + "\",\"config\":\"" + jsonEscape(config.trim()) + "\"}");

            Page apiResponse = client.getPage(apiRequest);
            int status = apiResponse.getWebResponse().getStatusCode();
            if (status < 200 || status >= 300) {
                response.setStatus(status);
                response.getWriter().write("{\"error\":\"Sokker rejected the tactic\"}");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"saved\":true}");
        } catch (FailingHttpStatusCodeException e) {
            int status = e.getStatusCode();
            response.setStatus(status);
            response.getWriter().write("{\"error\":\"Sokker returned HTTP " + status + "\"}");
        } finally {
            client.close();
        }
    }

    private static String jsonEscape(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.toString();
    }
}
