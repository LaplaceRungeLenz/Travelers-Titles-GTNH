package io.github.laplacerungelenz.travelerstitles.core;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RuleEngineTest {

    private Location location() {
        Map<String, String> values = new HashMap<>();
        values.put("body", "planet.mars");
        values.put("biomeId", "4096");
        values.put("types", "COLD,DRY");
        values.put("provider", "some.mod.Provider");
        return new Location("mars:29", "space", values);
    }

    @Test
    public void mergesPackFieldsButExplicitLocalOverridesWin() {
        RuleEngine r = new RuleEngine();
        r.load("{\"schemaVersion\":1,\"defaults\":{\"dimension\":{\"color\":\"112233\",\"scale\":3}}}", "pack", 1);
        r.load(
            "{\"schemaVersion\":1,\"rules\":[{\"id\":\"mars\",\"priority\":99,\"match\":{\"body\":\"planet.mars\"},\"style\":{\"color\":\"ff0000\"}}]}",
            "pack",
            1);
        r.load("{\"schemaVersion\":1,\"defaults\":{\"dimension\":{\"color\":\"00ff00\"}}}", "local", RuleEngine.LOCAL);
        TitleStyle s = r.resolve("dimension", location());
        assertEquals(0x00ff00, s.color);
        assertEquals(3, s.scale, 0.001);
    }

    @Test
    public void sameRuleIdInheritsUnspecifiedFields() {
        RuleEngine r = new RuleEngine();
        r.load(
            "{\"schemaVersion\":1,\"rules\":[{\"id\":\"mars\",\"match\":{\"body\":\"planet.mars\"},\"style\":{\"title\":\"Red planet\",\"scale\":4}}]}",
            "low",
            1);
        r.load("{\"schemaVersion\":1,\"rules\":[{\"id\":\"mars\",\"style\":{\"color\":\"abcdef\"}}]}", "high", 2);
        TitleStyle s = r.resolve("dimension", location());
        assertEquals("Red planet", s.title);
        assertEquals(4, s.scale, 0.001);
        assertEquals(0xabcdef, s.color);
    }

    @Test
    public void matchesExtendedIdsAndTypeConjunction() {
        RuleEngine r = new RuleEngine();
        r.load(
            "{\"schemaVersion\":1,\"rules\":[{\"id\":\"extended\",\"match\":{\"kind\":\"biome\",\"biomeId\":4096,\"types\":[\"COLD\",\"DRY\"],\"provider\":\"some.*\"},\"style\":{\"title\":\"Extended biome\"}}]}",
            "pack",
            0);
        assertEquals("Extended biome", r.resolve("biome", location()).title);
        assertEquals("", r.resolve("dimension", location()).title);
    }

    @Test
    public void rejectsBadValuesWithoutDiscardingValidFields() {
        RuleEngine r = new RuleEngine();
        r.load(
            "{\"schemaVersion\":1,\"defaults\":{\"dimension\":{\"color\":\"not-a-color\",\"fadeIn\":-10,\"scale\":999999,\"title\":\"Valid\"}}}",
            "bad",
            1);
        TitleStyle s = r.resolve("dimension", location());
        assertEquals("Valid", s.title);
        assertEquals(0xffffff, s.color);
        assertTrue(s.fadeIn >= 0);
        assertTrue(s.scale <= 10);
        assertFalse(
            r.warnings()
                .isEmpty());
    }

    @Test
    public void invalidDocumentDoesNotClearExistingRules() {
        RuleEngine r = new RuleEngine();
        r.load("{\"schemaVersion\":1,\"defaults\":{\"dimension\":{\"title\":\"Kept\"}}}", "good", 1);
        r.load("{broken", "broken", 2);
        assertEquals("Kept", r.resolve("dimension", location()).title);
    }

    @Test
    public void unknownMatcherCannotAccidentallyMatchEverything() {
        RuleEngine r = new RuleEngine();
        r.load(
            "{\"schemaVersion\":1,\"rules\":[{\"id\":\"typo\",\"match\":{\"bodi\":\"mars\"},\"style\":{\"enabled\":false}}]}",
            "bad",
            1);
        assertTrue(r.resolve("dimension", location()).enabled);
    }
}
