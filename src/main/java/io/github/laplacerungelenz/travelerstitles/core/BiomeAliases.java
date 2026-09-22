package io.github.laplacerungelenz.travelerstitles.core;

import java.util.Locale;

/** Human-readable selectors; numeric IDs and class selectors remain available for collisions. */
public final class BiomeAliases {

    private BiomeAliases() {}

    public static String alias(String className, String name) {
        String owner = className.startsWith("net.minecraft.")
            || className.startsWith("biomesoplenty.common.biome.overridden.")
            || className.equals("chylex.hee.world.biome.BiomeGenHardcoreEnd")
                ? "minecraft"
                : className.startsWith("biomesoplenty.") ? "biomesoplenty"
                    : className.startsWith("rwg.") ? "rwg"
                        : className.startsWith("twilightforest.") ? "twilightforest"
                            : className.startsWith("thaumcraft.") ? "thaumcraft"
                                : className.startsWith("galaxyspace.") ? "galaxyspace"
                                    : className.startsWith("micdoodle8.") ? "galacticraft"
                                        : className.startsWith("de.katzenpapst.amunra.") ? "amunra"
                                            : "legacy." + className;
        return owner + ":" + slug(name);
    }

    public static String slug(String name) {
        if (name == null || name.trim()
            .isEmpty()) return "unknown";
        String result = name.trim()
            .toLowerCase(Locale.ROOT)
            .replace("+", "_plus")
            .replaceAll("[^\\p{L}\\p{N}]+", "_")
            .replaceAll("^_|_$", "");
        return result.isEmpty() ? "unknown_" + Integer.toHexString(name.hashCode()) : result;
    }
}
