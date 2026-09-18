import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.formulamanager.sokker.auxiliares.SokkerXmlCompat;

public class TrainingMatchMappingHarness {
    public static void main(String[] args) throws Exception {
        rejectsMissingTiming();
        rejectsMissingFormation();
        rejectsMissingPlayerId();
        rejectsMissingLeagueId();
        rejectsMissingFinishedState();
        rejectsPageMatchMissingId();
        rejectsUnresolvableWeekDay();
    }

    private static void rejectsMissingTiming() {
        Map<String, Object> player = playerWithId();
        player.put("bench", Boolean.FALSE);
        Map<String, Object> formation = new LinkedHashMap<String, Object>();
        formation.put("code", Integer.valueOf(1));
        player.put("formation", formation);

        String xml = SokkerXmlCompat.buildMatchXml(detail(), lineup(player), null);
        if (xml != null) {
            throw new AssertionError("Incomplete player timing must reject the JSON compatibility mapping instead of silently omitting the player");
        }
    }

    private static void rejectsMissingFormation() {
        Map<String, Object> player = playerWithId();
        player.put("bench", Boolean.FALSE);
        player.put("minutes", Integer.valueOf(90));

        String xml = SokkerXmlCompat.buildMatchXml(detail(), lineup(player), null);
        if (xml != null) {
            throw new AssertionError("Missing player formation must reject the JSON compatibility mapping instead of silently omitting the player");
        }
    }

    private static void rejectsMissingPlayerId() {
        Map<String, Object> player = new LinkedHashMap<String, Object>();
        player.put("bench", Boolean.FALSE);
        player.put("minutes", Integer.valueOf(90));
        Map<String, Object> formation = new LinkedHashMap<String, Object>();
        formation.put("code", Integer.valueOf(1));
        player.put("formation", formation);

        String xml = SokkerXmlCompat.buildMatchXml(detail(), lineup(player), null);
        if (xml != null) {
            throw new AssertionError("Missing player id must reject the JSON compatibility mapping instead of silently omitting the player");
        }
    }

    private static void rejectsMissingLeagueId() {
        Map<String, Object> player = completePlayer();
        String xml = SokkerXmlCompat.buildMatchXml(new LinkedHashMap<String, Object>(), lineup(player), null);
        if (xml != null) {
            throw new AssertionError("Missing league id must reject the JSON compatibility mapping instead of inventing leagueID=0");
        }
    }

    private static void rejectsMissingFinishedState() {
        Map<String, Object> match = new LinkedHashMap<String, Object>();
        match.put("id", Integer.valueOf(987));
        match.put("week", Integer.valueOf(12));
        match.put("day", Integer.valueOf(3));
        List<Object> matches = new ArrayList<Object>();
        matches.add(match);

        String xml = SokkerXmlCompat.buildMatchesXml(new LinkedHashMap<String, Object>(), matches);
        if (xml != null) {
            throw new AssertionError("Missing match finished state must reject the JSON compatibility mapping instead of assuming isFinished=0");
        }
    }

    private static void rejectsPageMatchMissingId() throws Exception {
        Map<String, Object> match = new LinkedHashMap<String, Object>();
        match.put("week", Integer.valueOf(12));
        match.put("day", Integer.valueOf(3));
        match.put("wasPlayed", Boolean.TRUE);

        List<Object> pageMatches = new ArrayList<Object>();
        pageMatches.add(match);
        Map<String, Object> page = new LinkedHashMap<String, Object>();
        page.put("matches", pageMatches);

        List<Object> collected = new ArrayList<Object>();
        Set<Integer> ids = new HashSet<Integer>();
        Method agregarPartidos = SokkerXmlCompat.class.getDeclaredMethod("agregarPartidos", Object.class, List.class, Set.class);
        agregarPartidos.setAccessible(true);
        agregarPartidos.invoke(null, page, collected, ids);

        String xml = SokkerXmlCompat.buildMatchesXml(new LinkedHashMap<String, Object>(), collected);
        if (xml != null) {
            throw new AssertionError("A page match without id must reject the JSON compatibility mapping instead of disappearing from the match list");
        }
    }

    private static void rejectsUnresolvableWeekDay() {
        Map<String, Object> match = new LinkedHashMap<String, Object>();
        match.put("id", Integer.valueOf(988));
        match.put("wasPlayed", Boolean.TRUE);
        List<Object> matches = new ArrayList<Object>();
        matches.add(match);

        String xml = SokkerXmlCompat.buildMatchesXml(new LinkedHashMap<String, Object>(), matches);
        if (xml != null) {
            throw new AssertionError("A match without resolvable week/day must reject the JSON compatibility mapping instead of disappearing from the match list");
        }
    }

    private static Map<String, Object> completePlayer() {
        Map<String, Object> player = playerWithId();
        player.put("bench", Boolean.FALSE);
        player.put("minutes", Integer.valueOf(90));
        Map<String, Object> formation = new LinkedHashMap<String, Object>();
        formation.put("code", Integer.valueOf(1));
        player.put("formation", formation);
        return player;
    }

    private static Map<String, Object> playerWithId() {
        Map<String, Object> player = new LinkedHashMap<String, Object>();
        player.put("id", Integer.valueOf(456));
        return player;
    }

    private static Map<String, Object> detail() {
        Map<String, Object> detail = new LinkedHashMap<String, Object>();
        Map<String, Object> league = new LinkedHashMap<String, Object>();
        league.put("id", Integer.valueOf(123));
        detail.put("league", league);
        return detail;
    }

    private static Map<String, Object> lineup(Map<String, Object> player) {
        List<Object> homePlayers = new ArrayList<Object>();
        homePlayers.add(player);
        Map<String, Object> lineup = new LinkedHashMap<String, Object>();
        lineup.put("homePlayers", homePlayers);
        lineup.put("awayPlayers", new ArrayList<Object>());
        return lineup;
    }
}
