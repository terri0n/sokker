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
 * Adaptador conservador de /api/trainer al XML minimo que consume
 * AsistenteBO.leer_entrenadores(). Si no puede demostrar la equivalencia,
 * devuelve null y el llamador conserva el XML legado.
 */
public final class SokkerTrainerXmlCompat {
    private SokkerTrainerXmlCompat() {}

    public static XmlPage getXmlPage(WebClient navegador, String url)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        if (!(AsistenteBO.SOKKER_URL + "/xml/trainers.xml").equals(url)) {
            return null;
        }

        Object response = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/trainer");
        String xml = SokkerTrainerMapping.buildXml(response);
        if (xml == null) {
            return null;
        }

        StringWebResponse webResponse = new StringWebResponse(xml, new URL(url));
        return new XmlPage(webResponse, navegador.getCurrentWindow());
    }
}
