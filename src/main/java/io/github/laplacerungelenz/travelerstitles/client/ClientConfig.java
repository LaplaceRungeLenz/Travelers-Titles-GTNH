package io.github.laplacerungelenz.travelerstitles.client;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public final class ClientConfig {

    public static Configuration config;
    public static File directory;
    public static boolean enabled, dimensions, biomes, showOnJoin, showOnRespawn, resetOnDimension, hideInGui,
        hideInDebug, onlySurface, orbitalChanges, sounds;
    public static int sampleTicks, stableTicks, cooldownTicks, recentSize;
    public static float volume;

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
        if (config.hasChanged()) config.save();
    }

    private static boolean flag(String name, boolean value, String comment) {
        return config.getBoolean(name, "general", value, comment, "ttgtnh.config." + name);
    }

    private static int number(String name, int value, int min, int max, String comment) {
        return config.getInt(name, "general", value, min, max, comment, "ttgtnh.config." + name);
    }
}
