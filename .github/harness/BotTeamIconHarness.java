import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class BotTeamIconHarness {
    private BotTeamIconHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path tag = Paths.get("sokker/WebContent/WEB-INF/tags/jugadores.tag");
        if (!Files.isRegularFile(tag)) {
            throw new AssertionError("Missing players tag: " + tag);
        }

        String text = new String(Files.readAllBytes(tag), StandardCharsets.UTF_8).replace("\r\n", "\n");
        String block = "<c:if test=\"${j.bot}\">\n"
                + "\t\t\t\t\t\t<span class=\"borde\" title=\"Bot\">&#x1F4BB;</span>\n"
                + "\t\t\t\t\t</c:if>";

        require(text.contains(block), "Server bot-team icon block is missing or has changed");

        int star = text.indexOf("<c:if test=\"${j.nt > 0}\">");
        int bot = text.indexOf("<c:if test=\"${j.bot}\">");
        int playerLink = text.indexOf("href=\"https://sokker.org/player/PID/${j.pid}\"");
        require(star >= 0 && bot > star && playerLink > bot,
                "Bot icon must remain between the NT star and the player link, as deployed on the server");

        System.out.println("Bot team icon harness OK");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
