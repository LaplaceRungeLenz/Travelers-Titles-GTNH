package io.github.laplacerungelenz.travelerstitles.core;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BundledTitlesTest {

    @Test
    public void acceptsAtlasCoordinatesWithoutWarnings() {
        RuleEngine engine = new RuleEngine();
        engine.load(
            "{\"schemaVersion\":1,\"defaults\":{\"dimension\":{\"textureU0\":0.5,\"textureV0\":0.2,\"textureU1\":0.9,\"textureV1\":0.4}}}",
            "atlas",
            0);
        assertTrue(
            engine.warnings()
                .toString(),
            engine.warnings()
                .isEmpty());
        TitleStyle style = engine.resolve("dimension", new Location("", "", new HashMap<>()));
        assertEquals(0.5, style.textureU0, 0.00001);
        assertEquals(0.4, style.textureV1, 0.00001);
    }

    @Test
    public void everyBundledRuleSelectsDistinctNonemptyTransparentArtwork() throws Exception {
        RuleEngine engine = bundled();
        JsonObject json;
        try (InputStream stream = getClass().getResourceAsStream("/assets/travelerstitlesgtnh/titles/index.json")) {
            json = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject();
        }
        assertEquals(
            54,
            json.getAsJsonArray("rules")
                .size());
        Set<String> titles = new HashSet<>(), regions = new HashSet<>();
        Map<String, BufferedImage> images = new HashMap<>();
        for (JsonElement element : json.getAsJsonArray("rules")) {
            JsonObject rule = element.getAsJsonObject();
            Map<String, String> attributes = new HashMap<>();
            for (Map.Entry<String, JsonElement> match : rule.getAsJsonObject("match")
                .entrySet()) {
                attributes.put(
                    match.getKey(),
                    match.getValue()
                        .getAsString());
            }
            attributes.put("dimensionId", "123456");
            attributes.put("bodyTitle", "中文名称");
            TitleStyle style = engine.resolve("dimension", new Location("arbitrary", "space", attributes));
            assertEquals(
                rule.getAsJsonObject("style")
                    .get("title")
                    .getAsString(),
                style.title);
            assertTrue(titles.add(style.title));
            assertTrue(style.title.matches("[A-Z0-9 ]+"));
            assertTrue(regions.add(style.texture + ":" + style.textureU0 + ":" + style.textureV0));
            BufferedImage image = images.get(style.texture);
            if (image == null) {
                try (
                    InputStream stream = getClass().getResourceAsStream("/assets/" + style.texture.replace(':', '/'))) {
                    assertNotNull(style.texture, stream);
                    image = ImageIO.read(stream);
                }
                assertNotNull(image);
                assertTrue(
                    image.getColorModel()
                        .hasAlpha());
                int clear = 0;
                for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) >>> 24) == 0) clear++;
                }
                assertTrue("Atlas background must be transparent", clear > image.getWidth() * image.getHeight() / 2);
                images.put(style.texture, image);
            }
            int x0 = Math.round(style.textureU0 * image.getWidth()),
                x1 = Math.round(style.textureU1 * image.getWidth());
            int y0 = Math.round(style.textureV0 * image.getHeight()),
                y1 = Math.round(style.textureV1 * image.getHeight());
            assertTrue(x0 >= 0 && y0 >= 0 && x1 <= image.getWidth() && y1 <= image.getHeight());
            assertTrue(x1 > x0 && y1 > y0);
            // A crop must have transparent padding on all sides; otherwise it can include
            // a thin fragment of the next title or cut through a letter/decoration.
            for (int x = x0; x < x1; x++) {
                assertTrue("Top edge cuts artwork: " + style.title, (image.getRGB(x, y0) >>> 24) <= 8);
                assertTrue("Bottom edge cuts artwork: " + style.title, (image.getRGB(x, y1 - 1) >>> 24) <= 8);
            }
            for (int y = y0; y < y1; y++) {
                assertTrue("Left edge cuts artwork: " + style.title, (image.getRGB(x0, y) >>> 24) <= 8);
                assertTrue("Right edge cuts artwork: " + style.title, (image.getRGB(x1 - 1, y) >>> 24) <= 8);
            }
            int pixels = 0;
            for (int y = y0; y < y1; y++) for (int x = x0; x < x1; x++) {
                if ((image.getRGB(x, y) >>> 24) > 8) pixels++;
            }
            assertTrue("Empty title: " + style.title, pixels > 1000);
            assertEquals((x1 - x0) / (double) (y1 - y0), style.imageWidth / (double) style.imageHeight, 0.2);
            assertTrue(style.showSubtitle);
            assertTrue(style.biomeSubtitleColor);
            assertEquals("", engine.resolve("biome", new Location("arbitrary", "space", attributes)).texture);
        }
        assertEquals(9, images.size());
        assertTrue(
            engine.warnings()
                .toString(),
            engine.warnings()
                .isEmpty());
    }

    @Test
    public void dynamicStationsAndMothershipsOverrideTheirOrbitalBody() throws Exception {
        RuleEngine engine = bundled();
        Map<String, String> attributes = new HashMap<>();
        attributes.put("body", "planet.mars");
        attributes.put("spaceKind", "station");
        assertEquals("SPACE STATION", engine.resolve("dimension", new Location("99", "", attributes)).title);
        attributes.put("spaceKind", "mothership");
        assertEquals("MOTHERSHIP", engine.resolve("dimension", new Location("100", "", attributes)).title);
        attributes.put("spaceKind", "body");
        assertEquals("MARS", engine.resolve("dimension", new Location("101", "", attributes)).title);
        attributes.put("body", "planet.unknown");
        assertEquals("", engine.resolve("dimension", new Location("102", "", attributes)).texture);
    }

    @Test
    public void replacementTextureResetsAtlasCropButColorOverrideRetainsIt() {
        RuleEngine engine = new RuleEngine();
        engine.load(
            "{\"schemaVersion\":1,\"rules\":[{\"id\":\"x\",\"style\":{\"texture\":\"test:atlas.png\",\"textureU0\":0.5,\"textureV1\":0.4}}]}",
            "base",
            0);
        engine.load("{\"schemaVersion\":1,\"rules\":[{\"id\":\"x\",\"style\":{\"color\":\"ff0000\"}}]}", "color", 1);
        Location location = new Location("", "", new HashMap<>());
        assertEquals(0.5, engine.resolve("dimension", location).textureU0, 0.00001);
        engine.load(
            "{\"schemaVersion\":1,\"rules\":[{\"id\":\"x\",\"style\":{\"texture\":\"test:replacement.png\"}}]}",
            "replacement",
            2);
        TitleStyle style = engine.resolve("dimension", location);
        assertEquals(0, style.textureU0, 0.00001);
        assertEquals(1, style.textureV1, 0.00001);
        engine.load(
            "{\"schemaVersion\":1,\"defaults\":{\"dimension\":{\"texture\":\"\",\"title\":\"My title\"}}}",
            "local",
            RuleEngine.LOCAL);
        assertEquals("", engine.resolve("dimension", location).texture);
        assertEquals("My title", engine.resolve("dimension", location).title);
    }

    @Test
    public void invalidOrReversedAtlasBoundsFallBackToFullImage() {
        RuleEngine engine = new RuleEngine();
        engine.load(
            "{\"schemaVersion\":1,\"defaults\":{\"dimension\":{\"textureU0\":0.8,\"textureU1\":0.2,\"textureV0\":-1}}}",
            "bad",
            0);
        TitleStyle style = engine.resolve("dimension", new Location("", "", new HashMap<>()));
        assertEquals(0, style.textureU0, 0.00001);
        assertEquals(0, style.textureV0, 0.00001);
        assertEquals(1, style.textureU1, 0.00001);
        assertEquals(1, style.textureV1, 0.00001);
        assertFalse(
            engine.warnings()
                .isEmpty());
    }

    @Test
    public void repeatedTextureInNewRulePreservesDefaultAtlasRegion() {
        RuleEngine engine = new RuleEngine();
        engine.load(
            "{\"schemaVersion\":1,\"defaults\":{\"dimension\":{\"texture\":\"test:atlas.png\",\"textureU0\":0.5}},\"rules\":[{\"id\":\"color\",\"style\":{\"texture\":\"test:atlas.png\",\"color\":\"ff0000\"}}]}",
            "pack",
            0);
        assertEquals(0.5, engine.resolve("dimension", new Location("", "", new HashMap<>())).textureU0, 0.00001);
    }

    @Test
    public void bundledOverworldHasImageAndEnglishFallbackRegardlessOfLocalizedName() throws Exception {
        RuleEngine engine = bundled();
        Map<String, String> values = new HashMap<>();
        values.put("dimension", "minecraft:overworld");
        values.put("dimensionName", "主世界");
        TitleStyle style = engine.resolve("dimension", new Location("0", "plains", values));
        assertEquals("OVERWORLD", style.title);
        assertFalse(style.texture.isEmpty());
        assertFalse(style.decoration);
        assertEquals("", engine.resolve("biome", new Location("0", "plains", values)).texture);
    }

    static RuleEngine bundled() throws Exception {
        RuleEngine engine = new RuleEngine();
        try (InputStream stream = BundledTitlesTest.class
            .getResourceAsStream("/assets/travelerstitlesgtnh/titles/index.json")) {
            assertNotNull(stream);
            JsonObject index = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject();
            if (index.has("files")) for (JsonElement file : index.getAsJsonArray("files")) {
                try (InputStream included = BundledTitlesTest.class
                    .getResourceAsStream("/assets/travelerstitlesgtnh/titles/" + file.getAsString())) {
                    assertNotNull(included);
                    engine.load(
                        new JsonParser().parse(new InputStreamReader(included, StandardCharsets.UTF_8))
                            .toString(),
                        file.getAsString(),
                        0);
                }
            }
            engine.load(index.toString(), "bundled", 0);
        }
        return engine;
    }
}
