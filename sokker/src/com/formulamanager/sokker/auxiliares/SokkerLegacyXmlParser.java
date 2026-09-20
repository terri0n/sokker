package com.formulamanager.sokker.auxiliares;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parser minimo para los XML legacy que aun se usan como fallback.
 * Evita XPath de HtmlUnit, que depende de objetos JavaScript y falla con
 * HtmlUnit 2.24 en runtimes Java modernos.
 */
public final class SokkerLegacyXmlParser {
    private SokkerLegacyXmlParser() {}

    public static List<Map<String, String>> parseElements(String xml, String elementName) throws IOException {
        if (xml == null) {
            throw new IOException("Missing XML response");
        }
        if (elementName == null || elementName.length() == 0) {
            throw new IOException("Missing XML element name");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            NodeList nodes = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)))
                    .getElementsByTagName(elementName);

            List<Map<String, String>> result = new ArrayList<Map<String, String>>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                Map<String, String> fields = new LinkedHashMap<String, String>();
                NodeList children = node.getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);
                    if (child instanceof Element) {
                        fields.put(child.getNodeName(), child.getTextContent());
                    }
                }
                result.add(fields);
            }
            return result;
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Invalid Sokker XML", e);
        }
    }

    public static String required(Map<String, String> fields, String name) throws IOException {
        String value = fields.get(name);
        if (value == null) {
            throw new IOException("Missing XML field: " + name);
        }
        return value;
    }
}
