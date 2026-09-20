import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.formulamanager.sokker.auxiliares.SokkerXmlPageFactory;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/** Regression for HtmlUnit XmlPage XPath failures seen on production Java 26. */
public final class XmlPageAssociationHarness {
    private XmlPageAssociationHarness() {}

    public static void main(String[] args) throws Exception {
        syntheticPageSupportsXPath();
        allSyntheticXmlAdaptersUseTheFactory();
    }

    private static void syntheticPageSupportsXPath() throws Exception {
        WebClient client = new WebClient();
        try {
            XmlPage page = SokkerXmlPageFactory.create(client,
                    "<matches><match><matchID>123</matchID></match></matches>",
                    "https://sokker.org/xml/matches-team-92.xml");
            List<?> matches = page.getByXPath("//match");
            require(matches.size() == 1,
                    "Synthetic XML pages must be attached to the current window before XPath is used");
        } finally {
            client.close();
        }
    }

    private static void allSyntheticXmlAdaptersUseTheFactory() throws Exception {
        assertUsesFactory("sokker/src/com/formulamanager/sokker/auxiliares/SokkerXmlCompat.java");
        assertUsesFactory("sokker/src/com/formulamanager/sokker/auxiliares/SokkerPlayerXmlCompat.java");
    }

    private static void assertUsesFactory(String path) throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        require(source.contains("SokkerXmlPageFactory.create(navegador, xml, url)"),
                path + " must create synthetic XmlPage instances through SokkerXmlPageFactory");
        require(!source.contains("return new XmlPage(response, navegador.getCurrentWindow())"),
                path + " must not return an unattached synthetic XmlPage");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
