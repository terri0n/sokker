import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.formulamanager.sokker.auxiliares.SokkerTrainerXmlCompat;

public final class XmlCompatRegressionHarness {
    private XmlCompatRegressionHarness() {}

    public static void main(String[] args) throws Exception {
        legacyTrainerXmlIsParsedWithoutHtmlUnitXPath();
        legacyJuniorXmlIsParsedWithoutHtmlUnitXPath();
        unassignedTrainerJobIsMappedToIgnoredLegacyRole();
    }

    @SuppressWarnings("unchecked")
    private static void legacyTrainerXmlIsParsedWithoutHtmlUnitXPath() throws Exception {
        Class<?> parser = Class.forName("com.formulamanager.sokker.auxiliares.SokkerLegacyXmlParser");
        Method parse = parser.getMethod("parseElements", String.class, String.class);
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<trainers teamID=\"143649\"><trainer><ID>10666010</ID>"
                + "<name>Fedor</name><surname>Filipčík</surname><job>3</job>"
                + "<skillStamina>12</skillStamina><skillCoach>14</skillCoach>"
                + "</trainer></trainers>";

        List<Map<String, String>> trainers = (List<Map<String, String>>) parse.invoke(null, xml, "trainer");
        require(trainers.size() == 1, "Real trainers XML must be parsed without HtmlUnit XPath");
        require("3".equals(trainers.get(0).get("job")), "Trainer job must be preserved");
        require("Filipčík".equals(trainers.get(0).get("surname")), "Trainer XML must preserve UTF-8 text");
    }

    @SuppressWarnings("unchecked")
    private static void legacyJuniorXmlIsParsedWithoutHtmlUnitXPath() throws Exception {
        Class<?> parser = Class.forName("com.formulamanager.sokker.auxiliares.SokkerLegacyXmlParser");
        Method parse = parser.getMethod("parseElements", String.class, String.class);
        String xml = "<juniors><junior><ID>31194458</ID><name>Bülent</name>"
                + "<surname>Dalmaz</surname><age>20</age><skill>11</skill>"
                + "<weeks>5</weeks><formation>true</formation></junior></juniors>";

        List<Map<String, String>> juniors = (List<Map<String, String>>) parse.invoke(null, xml, "junior");
        require(juniors.size() == 1, "Real juniors XML must be parsed without HtmlUnit XPath");
        require("31194458".equals(juniors.get(0).get("ID")), "Junior ID must be preserved");
        require("Bülent".equals(juniors.get(0).get("name")), "Junior XML must preserve UTF-8 text");
    }

    private static void unassignedTrainerJobIsMappedToIgnoredLegacyRole() {
        String original = "<trainers><trainer><job>0</job></trainer>"
                + "<trainer><job>1</job></trainer></trainers>";
        String normalized = SokkerTrainerXmlCompat.normalizeLegacyXml(original);

        require(normalized.contains("<trainer><job>4</job></trainer>"),
                "Legacy job=0 must map to OTRO (4), which the historical consumer ignores");
        require(normalized.contains("<trainer><job>1</job></trainer>"),
                "Known trainer jobs must remain unchanged");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
