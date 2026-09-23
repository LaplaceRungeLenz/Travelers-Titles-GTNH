package io.github.laplacerungelenz.travelerstitles.core;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonObject;

import io.github.laplacerungelenz.travelerstitles.client.TitleResources;

public class BiomePaletteTest {

    @Test
    public void knownBiomesHaveDistinctReadableThemeColors() throws Exception {
        RuleEngine engine = BundledTitlesTest.bundled();
        Map<String, String> values = new HashMap<>();
        values.put("biome", "minecraft:forest");
        int forest = engine.resolve("biome", new Location("0", "1", values)).color;
        values.put("biome", "minecraft:desert");
        int desert = engine.resolve("biome", new Location("0", "2", values)).color;
        values.put("biome", "minecraft:ice_plains");
        int ice = engine.resolve("biome", new Location("0", "3", values)).color;
        assertNotEquals(0xffffff, forest);
        assertNotEquals(forest, desert);
        assertNotEquals(desert, ice);
    }

    @Test
    public void subtitleUsesSameBiomeColorAndExplicitOverridesWin() throws Exception {
        RuleEngine engine = BundledTitlesTest.bundled();
        Map<String, String> values = new HashMap<>();
        values.put("dimension", "minecraft:overworld");
        values.put("biome", "minecraft:forest");
        Location location = new Location("0", "4", values);
        TitleStyle dimension = engine.resolve("dimension", location), biome = engine.resolve("biome", location);
        assertEquals(biome.color, TitleResources.subtitleColor("dimension", location, dimension, biome));
        assertTrue(biome.scale < dimension.scale);
        JsonObject appearance = new JsonObject();
        appearance.addProperty("subtitleColor", "123456");
        dimension = engine.resolve("dimension", location, appearance);
        assertEquals(0x123456, TitleResources.subtitleColor("dimension", location, dimension, biome));
        engine.load("{\"schemaVersion\":1,\"defaults\":{\"biome\":{\"color\":\"abcdef\"}}}", "local", RuleEngine.LOCAL);
        biome = engine.resolve("biome", location);
        dimension = engine.resolve("dimension", location);
        assertEquals(0xabcdef, TitleResources.subtitleColor("dimension", location, dimension, biome));
    }

    @Test
    public void sharedSpaceBiomeUsesBodyThemeAndUnknownForestsUseTypeTheme() throws Exception {
        RuleEngine engine = BundledTitlesTest.bundled();
        Map<String, String> values = new HashMap<>();
        values.put("biome", "galaxyspace:space");
        values.put("types", "DRY,PLAINS");
        values.put("body", "planet.mars");
        int mars = engine.resolve("biome", new Location("1", "1", values)).color;
        values.put("body", "planet.venus");
        int venus = engine.resolve("biome", new Location("2", "1", values)).color;
        assertNotEquals(mars, venus);
        values.put("biome", "other:unlisted_forest");
        values.put("types", "FOREST");
        int forest = engine.resolve("biome", new Location("3", "2", values)).color;
        assertEquals(0x9ccc83, forest);
        assertTrue(
            engine.warnings()
                .toString(),
            engine.warnings()
                .isEmpty());
    }

    @Test
    public void manualAndOrbitalSubtitlesKeepTheirOwnColor() throws Exception {
        RuleEngine engine = BundledTitlesTest.bundled();
        Map<String, String> values = new HashMap<>();
        values.put("dimension", "minecraft:overworld");
        values.put("biome", "minecraft:forest");
        Location location = new Location("0", "4", values);
        engine.load(
            "{\"schemaVersion\":1,\"defaults\":{\"dimension\":{\"subtitle\":\"Custom\"}}}",
            "local",
            RuleEngine.LOCAL);
        TitleStyle dimension = engine.resolve("dimension", location), biome = engine.resolve("biome", location);
        assertEquals(dimension.subtitleColor, TitleResources.subtitleColor("dimension", location, dimension, biome));
        values.put("spaceKind", "mothership");
        location = new Location("9", "4", values);
        dimension = engine.resolve("dimension", location);
        assertEquals(dimension.subtitleColor, TitleResources.subtitleColor("dimension", location, dimension, biome));
    }
}
