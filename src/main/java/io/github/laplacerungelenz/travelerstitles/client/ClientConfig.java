package io.github.laplacerungelenz.travelerstitles.client;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import com.google.gson.JsonObject;

import io.github.laplacerungelenz.travelerstitles.core.TitleStyle;

public final class ClientConfig {

    public static Configuration config;
    public static File directory;
    public static boolean enabled, dimensions, biomes, showOnJoin, showOnRespawn, resetOnDimension, hideInGui,
        hideInDebug, onlySurface, orbitalChanges, sounds;
    public static int sampleTicks, stableTicks, cooldownTicks, recentSize;
    public static float volume;
    private static final Map<String, JsonObject> appearances = new HashMap<>();

    private ClientConfig() {}

    public static void init(File parent) {
        directory = new File(parent, "travelerstitlesgtnh");
        directory.mkdirs();
        config = new Configuration(new File(directory, "general.cfg"));
        sync();
    }

    public static void sync() {
        enabled = flag("enabled", true, "Enable location titles.");
        dimensions = flag("dimensions", true, "Show dimension / celestial body titles.");
        biomes = flag("biomes", true, "Show biome titles and biome subtitles.");
        showOnJoin = flag("showOnJoin", true, "Show a title when joining a world.");
        showOnRespawn = flag("showOnRespawn", false, "Show a title after respawning in the same dimension.");
        resetOnDimension = flag("resetOnDimension", true, "Reset the recent-biome cache on dimension change.");
        hideInGui = flag("hideInGui", true, "Hide in inventory, chat and other screens.");
        hideInDebug = flag("hideInDebug", true, "Hide while the F3 debug overlay is visible.");
        onlySurface = flag(
            "onlySurface",
            false,
            "Limit ordinary surface-world biome titles to sky-visible positions; space is exempt.");
        orbitalChanges = flag("orbitalChanges", true, "Show a new title when a mothership's orbital context changes.");
        sounds = flag("sounds", true, "Play resource-configured title sounds.");
        sampleTicks = number("sampleTicks", 5, 1, 100, "Location sample interval in game ticks.");
        stableTicks = number("stableTicks", 15, 0, 1200, "Ticks a biome must remain stable before displaying.");
        cooldownTicks = number("cooldownTicks", 80, 0, 12000, "Minimum time between biome titles.");
        recentSize = number("recentSize", 5, 0, 100, "Recently displayed biomes to suppress.");
        volume = config.getFloat("volume", "general", 1, 0, 1, "Master title volume.", "ttgtnh.config.volume");
        for (String kind : new String[] { "dimension", "biome" }) syncAppearance(kind);
        if (config.hasChanged()) config.save();
    }

    public static JsonObject appearance(String kind) {
        JsonObject result = appearances.get(kind);
        return result == null ? new JsonObject() : result;
    }

    private static void syncAppearance(String kind) {
        JsonObject defaults = TitleStyle.defaults(kind);
        JsonObject values = new JsonObject();
        boolean override = config.getBoolean(
            "overrideAppearance",
            kind,
            false,
            "Use these appearance settings instead of resource packs and overrides.json.",
            "ttgtnh.config.overrideAppearance");
        for (String key : new String[] { "scale", "subtitleScale", "anchorX", "anchorY", "x", "y", "maxWidth",
            "lineSpacing" }) {
            double min = 0, max = 1;
            if (key.equals("scale") || key.equals("subtitleScale")) {
                min = 0.1;
                max = 10;
            } else if (key.equals("x") || key.equals("y")) {
                min = -4096;
                max = 4096;
            } else if (key.equals("maxWidth")) min = 0.05;
            else if (key.equals("lineSpacing")) max = 100;
            double fallback = defaults.get(key)
                .getAsDouble();
            Property property = config.get(kind, key, fallback, "", min, max);
            property.setLanguageKey("ttgtnh.config." + key);
            double value = property.getDouble(fallback);
            if (Double.isNaN(value) || Double.isInfinite(value)) value = fallback;
            value = Math.max(min, Math.min(max, value));
            property.set(value);
            values.addProperty(key, value);
        }
        for (String key : new String[] { "shadow", "showSubtitle", "background", "decoration" }) {
            values.addProperty(
                key,
                config.getBoolean(
                    key,
                    kind,
                    defaults.get(key)
                        .getAsBoolean(),
                    "",
                    "ttgtnh.config." + key));
        }
        for (String key : new String[] { "color", "subtitleColor" }) {
            Property property = config.get(
                kind,
                key,
                defaults.get(key)
                    .getAsString(),
                "Six hexadecimal digits (RRGGBB).");
            property.setLanguageKey("ttgtnh.config." + key)
                .setValidationPattern(Pattern.compile("[0-9a-fA-F]{6}"));
            if (!property.getString()
                .matches("[0-9a-fA-F]{6}")) property.setToDefault();
            values.addProperty(key, property.getString());
        }
        appearances.put(kind, override ? values : new JsonObject());
        config.getCategory(kind)
            .setPropertyOrder(
                Arrays.asList(
                    "overrideAppearance",
                    "scale",
                    "subtitleScale",
                    "color",
                    "subtitleColor",
                    "shadow",
                    "anchorX",
                    "anchorY",
                    "x",
                    "y",
                    "maxWidth",
                    "lineSpacing",
                    "showSubtitle",
                    "background",
                    "decoration"));
    }

    private static boolean flag(String name, boolean value, String comment) {
        return config.getBoolean(name, "general", value, comment, "ttgtnh.config." + name);
    }

    private static int number(String name, int value, int min, int max, String comment) {
        return config.getInt(name, "general", value, min, max, comment, "ttgtnh.config." + name);
    }
}
