package io.github.laplacerungelenz.travelerstitles.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class BiomeAliasesTest {

    @Test
    public void bopReplacementRetainsVanillaAlias() {
        assertEquals(
            "minecraft:river",
            BiomeAliases.alias("biomesoplenty.common.biome.overridden.BiomeGenBOPRiver", "River"));
    }

    @Test
    public void plusVariantDoesNotCollideWithOrdinaryHills() {
        assertEquals(
            "minecraft:extreme_hills_plus",
            BiomeAliases.alias("net.minecraft.world.biome.BiomeGenHills", "Extreme Hills+"));
    }

    @Test
    public void unicodeNamesRetainDistinctAliases() {
        assertNotEquals(BiomeAliases.slug("甲"), BiomeAliases.slug("乙"));
        assertFalse(
            BiomeAliases.slug("甲")
                .isEmpty());
    }

    @Test
    public void endReplacementRetainsEndAlias() {
        assertEquals("minecraft:sky", BiomeAliases.alias("chylex.hee.world.biome.BiomeGenHardcoreEnd", "Sky"));
    }
}
