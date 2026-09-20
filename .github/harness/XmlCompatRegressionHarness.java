import java.util.List;

import com.formulamanager.sokker.auxiliares.SokkerTrainerXmlCompat;
import com.formulamanager.sokker.auxiliares.SokkerXmlPageFactory;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public final class XmlCompatRegressionHarness {
    private XmlCompatRegressionHarness() {}

    public static void main(String[] args) throws Exception {
        syntheticXmlPageSupportsXPath();
        unassignedTrainerJobIsMappedToIgnoredLegacyRole();
    }

    private static void syntheticXmlPageSupportsXPath() throws Exception {
        WebClient navegador = new WebClient();
        navegador.getOptions().setJavaScriptEnabled(false);
        try {
            XmlPage page = SokkerXmlPageFactory.create(
                    navegador,
                    "<juniors><junior><ID>31194458</ID><name>Bülent</name>"
                    + "<surname>Dalmaz</surname><age>20</age><skill>11</skill>"
                    + "<weeks>5</weeks><formation>true</formation></junior></juniors>",
                    "https://sokker.org/xml/juniors.xml");

            List<?> juniors = page.getByXPath("//junior");
            require(juniors.size() == 1,
                    "Synthetic XML pages must be attached to the current window before XPath is used");
        } finally {
            navegador.close();
        }
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
