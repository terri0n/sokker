package com.formulamanager.sokker.auxiliares;

import java.io.IOException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Crea páginas XML sintéticas del mismo modo que el PageCreator de HtmlUnit:
 * además de construir la página, la asocia a la ventana actual. Sin esta
 * asociación, operaciones como getByXPath() pueden fallar con
 * "No script object associated with the Page".
 */
public final class SokkerXmlPageFactory {
    private SokkerXmlPageFactory() {}

    public static XmlPage create(WebClient navegador, String xml, String url) throws IOException {
        StringWebResponse response = new StringWebResponse(xml, new URL(url));
        XmlPage page = new XmlPage(response, navegador.getCurrentWindow());
        navegador.getCurrentWindow().setEnclosedPage(page);
        return page;
    }
}
