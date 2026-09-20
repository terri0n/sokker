import java.util.LinkedHashMap;
import java.util.Map;

import com.formulamanager.sokker.auxiliares.SokkerXmlCompat;

public final class LeagueTrainingCompatHarness {
    private LeagueTrainingCompatHarness() {}

    public static void main(String[] args) {
        unknownNonOfficialLeagueFallsBackToFriendlyLegacyType();
        knownJuniorLeagueKeepsNoTrainingType();
        knownOfficialLeagueTypeIsPreserved();
        incompleteLeaguePayloadKeepsLegacyMissingTypeFallback();
    }

    private static void unknownNonOfficialLeagueFallsBackToFriendlyLegacyType() {
        String xml = SokkerXmlCompat.buildLeagueXml(league(3, false));
        require(xml.contains("<type>0</type><isOfficial>0</isOfficial>"),
                "An unknown non-official JSON league must map to legacy friendly type 0");
    }

    private static void knownJuniorLeagueKeepsNoTrainingType() {
        String xml = SokkerXmlCompat.buildLeagueXml(league(7, false));
        require(xml.contains("<type>7</type><isOfficial>0</isOfficial>"),
                "Junior league type 7 must remain no-training type 7");
    }

    private static void knownOfficialLeagueTypeIsPreserved() {
        String xml = SokkerXmlCompat.buildLeagueXml(league(2, true));
        require(xml.contains("<type>2</type><isOfficial>1</isOfficial>"),
                "Known official competition types must remain unchanged");
    }

    private static void incompleteLeaguePayloadKeepsLegacyMissingTypeFallback() {
        Map<String, Object> league = new LinkedHashMap<String, Object>();
        league.put("type", Integer.valueOf(3));
        String xml = SokkerXmlCompat.buildLeagueXml(league);
        require(!xml.contains("<type>"),
                "Without the official flag the adapter must not invent league semantics");
    }

    private static Map<String, Object> league(int type, boolean official) {
        Map<String, Object> league = new LinkedHashMap<String, Object>();
        league.put("type", Integer.valueOf(type));
        league.put("isOfficial", Boolean.valueOf(official));
        return league;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
