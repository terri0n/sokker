package com.formulamanager.sokker.auxiliares;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.formulamanager.sokker.bo.AsistenteBO;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Adaptador conservador de /api/trainer al XML minimo que consume
 * AsistenteBO.leer_entrenadores(). Si no puede demostrar la equivalencia,
 * conserva el XML legado, normalizando únicamente el rol sin asignar que el
 * consumidor histórico no puede representar directamente.
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
            // El XML actual usa job=0 para entrenadores sin asignación. El
            // consumidor histórico usa un enum 1-based y su valor OTRO (4)
            // se ignora, que es exactamente el comportamiento que necesitamos.
            WebRequest request = new WebRequest(new URL(url), HttpMethod.GET);
            Page legacyPage = navegador.getPage(request);
            xml = normalizeLegacyXml(legacyPage.getWebResponse().getContentAsString());
        }

        return SokkerXmlPageFactory.create(navegador, xml, url);
    }

    public static String normalizeLegacyXml(String xml) {
        if (xml == null) {
            return null;
        }
        return xml.replaceAll("<job>\\s*0\\s*</job>", "<job>4</job>");
    }
}
