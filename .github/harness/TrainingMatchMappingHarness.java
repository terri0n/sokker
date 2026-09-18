import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.formulamanager.sokker.auxiliares.SokkerXmlCompat;

public class TrainingMatchMappingHarness {
    public static void main(String[] args) {
        Map<String, Object> detail = new LinkedHashMap<String, Object>();
        Map<String, Object> league = new LinkedHashMap<String, Object>();
        league.put("id", Integer.valueOf(123));
        detail.put("league", league);

        Map<String, Object> player = new LinkedHashMap<String, Object>();
        player.put("id", Integer.valueOf(456));
        player.put("bench", Boolean.FALSE);
        Map<String, Object> formation = new LinkedHashMap<String, Object>();
        formation.put("code", Integer.valueOf(1));
        player.put("formation", formation);

        List<Object> homePlayers = new ArrayList<Object>();
        homePlayers.add(player);
        Map<String, Object> lineup = new LinkedHashMap<String, Object>();
        lineup.put("homePlayers", homePlayers);
        lineup.put("awayPlayers", new ArrayList<Object>());

        String xml = SokkerXmlCompat.buildMatchXml(detail, lineup, null);
        if (xml != null) {
            throw new AssertionError("Incomplete player timing must reject the JSON compatibility mapping instead of silently omitting the player");
        }
    }
}
