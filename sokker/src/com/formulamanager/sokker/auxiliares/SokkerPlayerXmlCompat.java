package com.formulamanager.sokker.auxiliares;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.formulamanager.sokker.bo.AsistenteBO;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Adaptador mínimo para la única lectura activa de /xml/player-{pid}.xml:
 * AsistenteBO.existe_jugador(). Solo reconstruye name y teamID.
 */
public final class SokkerPlayerXmlCompat {
    private SokkerPlayerXmlCompat() {}

    public static XmlPage getXmlPage(WebClient navegador, String url)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        String prefix = AsistenteBO.SOKKER_URL + "/xml/player-";
        if (!url.startsWith(prefix) || !url.endsWith(".xml")) {
            return null;
        }

        Integer pid;
        try {
            pid = Integer.valueOf(url.substring(prefix.length(), url.length() - 4));
        } catch (NumberFormatException e) {
            return null;
        }

        final Object player;
        try {
            player = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/player/" + pid);
        } catch (FailingHttpStatusCodeException e) {
            if (e.getStatusCode() == 404) {
                return xmlPage(navegador, url, "<player></player>");
            }
            throw e;
        }

        String name = JSONUtil.getString(player, "info.name.full");
        Integer teamId = JSONUtil.getInteger(player, "info.team.id");
        if (name == null || teamId == null) {
            return null;
        }

        String xml = "<player><name>" + escape(name) + "</name><teamID>"
                + teamId + "</teamID></player>";
        return xmlPage(navegador, url, xml);
    }

    private static XmlPage xmlPage(WebClient navegador, String url, String xml)
            throws MalformedURLException, IOException {
        StringWebResponse response = new StringWebResponse(xml, new URL(url));
        return new XmlPage(response, navegador.getCurrentWindow());
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
