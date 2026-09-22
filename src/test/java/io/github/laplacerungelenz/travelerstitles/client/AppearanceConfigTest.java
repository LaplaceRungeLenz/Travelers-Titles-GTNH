package io.github.laplacerungelenz.travelerstitles.client;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cpw.mods.fml.relauncher.FMLInjectionData;
import io.github.laplacerungelenz.travelerstitles.core.Location;
import io.github.laplacerungelenz.travelerstitles.core.RuleEngine;
import io.github.laplacerungelenz.travelerstitles.core.TitleStyle;

public class AppearanceConfigTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private Object previousHome;

    @Before
    public void setForgeHome() throws Exception {
        Field home = FMLInjectionData.class.getDeclaredField("minecraftHome");
        home.setAccessible(true);
        previousHome = home.get(null);
        home.set(null, folder.getRoot());
    }

    @After
    public void restoreForgeHome() throws Exception {
        Field home = FMLInjectionData.class.getDeclaredField("minecraftHome");
        home.setAccessible(true);
        home.set(null, previousHome);
    }

    @Test
    public void invalidAppearanceValuesAreRepairedAndDefaultsCanBeRestored() {
        ClientConfig.init(folder.getRoot());
        ClientConfig.config.getCategory("dimension")
            .get("overrideAppearance")
            .set(true);
        ClientConfig.config.getCategory("dimension")
            .get("scale")
            .set(Double.NaN);
        ClientConfig.config.getCategory("dimension")
            .get("anchorX")
            .set(50.0);
        ClientConfig.config.getCategory("dimension")
            .get("color")
            .set("broken");
        ClientConfig.sync();
        assertEquals(
            3,
            ClientConfig.appearance("dimension")
                .get("scale")
                .getAsDouble(),
            0.001);
        assertEquals(
            1,
            ClientConfig.appearance("dimension")
                .get("anchorX")
                .getAsDouble(),
            0.001);
        assertEquals(
            "ffffff",
            ClientConfig.appearance("dimension")
                .get("color")
                .getAsString());
        for (net.minecraftforge.common.config.Property property : ClientConfig.config.getCategory("dimension")
            .values()) property.setToDefault();
        ClientConfig.sync();
        assertTrue(
            ClientConfig.appearance("dimension")
                .entrySet()
                .isEmpty());
    }

    @Test
    public void appearanceIsOptInPersistentAndSeparateForEachKind() {
        ClientConfig.init(folder.getRoot());
        assertEquals(
            0,
            ClientConfig.appearance("dimension")
                .entrySet()
                .size());
        ClientConfig.config.getCategory("dimension")
            .get("overrideAppearance")
            .set(true);
        ClientConfig.config.getCategory("dimension")
            .get("scale")
            .set(4.5);
        ClientConfig.config.getCategory("dimension")
            .get("x")
            .set(-35.0);
        ClientConfig.sync();
        ClientConfig.init(folder.getRoot());
        assertEquals(
            4.5,
            ClientConfig.appearance("dimension")
                .get("scale")
                .getAsDouble(),
            0.001);
        assertEquals(
            -35,
            ClientConfig.appearance("dimension")
                .get("x")
                .getAsDouble(),
            0.001);
        assertEquals(
            0,
            ClientConfig.appearance("biome")
                .entrySet()
                .size());
        assertFalse(
            ClientConfig.config.getCategory("dimension")
                .get("scale")
                .requiresWorldRestart());
    }

    @Test
    public void appearanceWinsOverLocalRulesWithoutLosingNamesOrMutatingEngine() {
        ClientConfig.init(folder.getRoot());
        ClientConfig.config.getCategory("dimension")
            .get("overrideAppearance")
            .set(true);
        ClientConfig.config.getCategory("dimension")
            .get("scale")
            .set(4.5);
        ClientConfig.sync();
        RuleEngine engine = new RuleEngine();
        engine.load(
            "{\"schemaVersion\":1,\"rules\":[{\"id\":\"local\",\"style\":{\"title\":\"My planet\",\"scale\":2}}]}",
            "local",
            RuleEngine.LOCAL);
        Location location = new Location("world", "biome", Collections.emptyMap());
        TitleStyle style = engine.resolve("dimension", location, ClientConfig.appearance("dimension"));
        assertEquals(4.5, style.scale, 0.001);
        assertEquals("My planet", style.title);
        assertEquals(2, engine.resolve("dimension", location).scale, 0.001);
        ClientConfig.config.getCategory("dimension")
            .get("overrideAppearance")
            .set(false);
        ClientConfig.sync();
        assertEquals(2, engine.resolve("dimension", location, ClientConfig.appearance("dimension")).scale, 0.001);
    }
}
