package io.github.laplacerungelenz.travelerstitles.client;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.common.BiomeDictionary;

import io.github.laplacerungelenz.travelerstitles.core.BiomeAliases;
import io.github.laplacerungelenz.travelerstitles.core.Location;

public final class LocationResolver {

    public Location sample(Minecraft mc) {
        if (mc.theWorld == null || mc.thePlayer == null) return null;
        int x = MathHelper.floor_double(mc.thePlayer.posX), z = MathHelper.floor_double(mc.thePlayer.posZ);
        Chunk chunk = mc.theWorld.getChunkProvider()
            .provideChunk(x >> 4, z >> 4);
        if (chunk == null || chunk instanceof EmptyChunk || !chunk.isChunkLoaded) return null;
        BiomeGenBase biome = mc.theWorld.getBiomeGenForCoords(x, z);
        if (biome == null) return null;
        WorldProvider provider = mc.theWorld.provider;
        Map<String, String> a = biomeAttributes(biome);
        a.put(
            "provider",
            provider.getClass()
                .getName());
        a.put("dimensionId", Integer.toString(provider.dimensionId));
        a.put("dimensionName", provider.getDimensionName());
        a.putAll(SpaceCompat.describe(provider));
        String dimension = dimensionAlias(provider);
        if (a.containsKey("body")) dimension = "space:" + a.get("body");
        if ("mothership".equals(a.get("spaceKind"))) dimension = "amunra:mothership";
        a.put("dimension", dimension);
        boolean surface = !a.containsKey("body") && provider.isSurfaceWorld();
        a.put(
            "surfaceVisible",
            Boolean
                .toString(!surface || mc.theWorld.canBlockSeeTheSky(x, MathHelper.floor_double(mc.thePlayer.posY), z)));
        String dimensionKey = provider.dimensionId + "|" + a.get("provider") + "|" + dimension;
        if (ClientConfig.orbitalChanges && "mothership".equals(a.get("spaceKind"))) {
            dimensionKey += "|" + a.get("orbit") + "|" + a.get("transit");
        }
        return new Location(dimensionKey, biome.biomeID + "|" + a.get("biomeClass") + "|" + a.get("biomeName"), a);
    }

    public static Map<String, String> biomeAttributes(BiomeGenBase biome) {
        Map<String, String> a = new LinkedHashMap<>();
        String className = biome.getClass()
            .getName();
        a.put("biome", BiomeAliases.alias(className, biome.biomeName));
        a.put("biomeId", Integer.toString(biome.biomeID));
        a.put("biomeClass", className);
        a.put("biomeName", biome.biomeName == null ? "" : biome.biomeName);
        StringBuilder types = new StringBuilder();
        for (BiomeDictionary.Type type : BiomeDictionary.getTypesForBiome(biome)) {
            if (types.length() > 0) types.append(',');
            types.append(type.name());
        }
        a.put("types", types.toString());
        return a;
    }

    public static String dimensionAlias(WorldProvider provider) {
        if (provider instanceof WorldProviderHell) return "minecraft:the_nether";
        if (provider instanceof WorldProviderEnd) return "minecraft:the_end";
        String c = provider.getClass()
            .getName();
        if (c.startsWith("twilightforest.")) return "twilightforest:twilight_forest";
        if (c.startsWith("me.eigenraven.personalspace.")) return "personalspace:personal_space";
        if (c.contains("WorldProviderUnderdark")) return "extrautilities:deep_dark";
        if (c.contains("WorldProviderEndOfTime")) return "extrautilities:last_millennium";
        if (c.startsWith("thaumcraft.")) return "thaumcraft:outer_lands";
        if (c.contains("WorldProviderDream")) return "witchery:dream_world";
        if (c.contains("WorldProviderTorment")) return "witchery:torment";
        if (c.contains("WorldProviderMirror")) return "witchery:mirror";
        if (c.startsWith("toxiceverglades.")) return "gtnh:toxic_everglades";
        if (provider instanceof WorldProviderSurface) return "minecraft:overworld";
        return "legacy:" + c;
    }

    public static String slug(String s) {
        return BiomeAliases.slug(s);
    }
}
