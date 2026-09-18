package com.formulamanager.sokker.auxiliares;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.formulamanager.sokker.bo.AsistenteBO;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Adaptador de /api/junior al XML mínimo que todavía consume
 * AsistenteBO.obtener_juveniles(). Conserva la posición guardada de los
 * juveniles ya conocidos y solo consulta las noticias para juveniles nuevos.
 */
public final class SokkerJuniorsXmlCompat {
    private SokkerJuniorsXmlCompat() {}

    public static XmlPage getXmlPage(WebClient navegador, String url)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        if (!(AsistenteBO.SOKKER_URL + "/xml/juniors.xml").equals(url)) {
            return null;
        }

        Object current = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/current");
        Integer tid = integer(current, "team.id");
        if (tid == null) {
            throw new IllegalStateException("/api/current no contiene team.id");
        }

        Object response = JSONUtil.getJson(navegador, AsistenteBO.SOKKER_URL + "/api/junior");
        List<?> juniors = list(response, "juniors");
        Map<Integer, Boolean> savedPositions = loadSavedPositions(tid);

        Set<Integer> pending = new HashSet<Integer>();
        for (Object junior : juniors) {
            Integer id = integer(junior, "id");
            if (id != null && !savedPositions.containsKey(id)) {
                pending.add(id);
            }
        }

        Map<Integer, String> newsPositions = pending.isEmpty()
                ? Collections.<Integer, String>emptyMap()
                : loadNewsPositions(navegador, pending);

        String xml = buildJuniorsXml(juniors, savedPositions, newsPositions);
        StringWebResponse webResponse = new StringWebResponse(xml, new URL(url));
        return new XmlPage(webResponse, navegador.getCurrentWindow());
    }

    private static Map<Integer, Boolean> loadSavedPositions(int tid) throws IOException {
        Properties properties = new Properties();
        File file = new File(SystemUtil.getVar(SystemUtil.PATH) + tid + "_juveniles.properties");
        if (!file.exists()) {
            return Collections.emptyMap();
        }

        InputStream input = null;
        try {
            input = new FileInputStream(file);
            properties.load(input);
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return readSavedPositions(properties);
    }

    private static Map<Integer, String> loadNewsPositions(WebClient navegador, Set<Integer> pending)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        Map<Integer, String> result = new HashMap<Integer, String>();
        Set<Integer> remaining = new HashSet<Integer>(pending);
        Object response = JSONUtil.getJson(navegador,
                AsistenteBO.SOKKER_URL + "/api/news?filter[limit]=200");

        for (Object item : list(response, "news")) {
            if (remaining.isEmpty()) {
                break;
            }
            if (!"youth_school".equals(string(item, "kind"))
                    || !Integer.valueOf(2).equals(integer(item, "type"))) {
                continue;
            }

            Integer newsId = integer(item, "id");
            if (newsId == null) {
                continue;
            }

            try {
                Object detail = JSONUtil.getJson(navegador,
                        AsistenteBO.SOKKER_URL + "/api/news/" + newsId);
                addNewsPositions(detail, remaining, result);
            } catch (FailingHttpStatusCodeException e) {
                if (e.getStatusCode() != 403 && e.getStatusCode() != 404) {
                    throw e;
                }
            }
        }
        return result;
    }

    public static Map<Integer, Boolean> readSavedPositions(Properties properties) {
        Map<Integer, Boolean> result = new HashMap<Integer, Boolean>();
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (value == null) {
                continue;
            }
            String[] fields = value.split(",", 3);
            if (fields.length < 2) {
                continue;
            }
            try {
                result.put(Integer.valueOf(key), Boolean.valueOf(fields[1]));
            } catch (NumberFormatException ignored) {
                // La lectura histórica de juveniles solo admite PID numérico.
            }
        }
        return result;
    }

    public static void addNewsPositions(Object detail, Set<Integer> pending,
            Map<Integer, String> result) {
        for (Object block : list(detail, "blocks")) {
            if (!"juniors".equals(string(block, "type"))) {
                continue;
            }
            for (Object junior : list(block, "data.juniors")) {
                Integer id = integer(junior, "id");
                if (id != null && pending.contains(id)) {
                    String position = string(junior, "position");
                    if (position != null) {
                        result.put(id, position);
                        pending.remove(id);
                    }
                }
            }
        }
    }

    public static String buildJuniorsXml(List<?> juniors,
            Map<Integer, Boolean> savedPositions, Map<Integer, String> newsPositions) {
        StringBuilder xml = new StringBuilder("<juniors>");
        for (Object junior : juniors) {
            Integer id = integer(junior, "id");
            String name = string(junior, "fullName.name");
            String surname = string(junior, "fullName.surname");
            Integer age = integer(junior, "age");
            Integer skill = integer(junior, "skill");
            Integer weeks = integer(junior, "weeksLeft");
            if (id == null || name == null || surname == null || age == null
                    || skill == null || weeks == null) {
                throw new IllegalStateException("Datos incompletos del juvenil: " + id);
            }

            Boolean outfield = savedPositions.get(id);
            if (outfield == null) {
                String position = newsPositions.get(id);
                if ("outfield".equals(position)) {
                    outfield = Boolean.TRUE;
                } else if ("goalkeeper".equals(position)) {
                    outfield = Boolean.FALSE;
                } else {
                    throw new IllegalStateException(
                            "Posición desconocida del juvenil " + id + ": " + position);
                }
            }

            xml.append("<junior><ID>").append(id).append("</ID>")
               .append("<name>").append(escape(name)).append("</name>")
               .append("<surname>").append(escape(surname)).append("</surname>")
               .append("<age>").append(age).append("</age>")
               .append("<skill>").append(skill).append("</skill>")
               .append("<weeks>").append(weeks).append("</weeks>")
               .append("<formation>").append(outfield.booleanValue())
               .append("</formation></junior>");
        }
        return xml.append("</juniors>").toString();
    }

    private static List<?> list(Object root, String path) {
        Object value = value(root, path);
        return value instanceof List<?> ? (List<?>) value : Collections.emptyList();
    }

    private static Object value(Object root, String path) {
        Object current = root;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?>)) {
                return null;
            }
            current = ((Map<?, ?>) current).get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private static Integer integer(Object root, String path) {
        Object value = value(root, path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static String string(Object root, String path) {
        Object value = value(root, path);
        return value == null ? null : String.valueOf(value);
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
