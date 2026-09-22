package io.github.laplacerungelenz.travelerstitles.core;

import java.util.LinkedHashSet;
import java.util.Set;

/** Tick-driven debounce/cooldown; retains only the current candidate, never a travel queue. */
public final class TitleController {

    public enum Kind {
        DIMENSION,
        BIOME
    }

    private final int stableTicks;
    private final int cooldownTicks;
    private final int recentSize;
    private final boolean showOnJoin;
    private final boolean resetOnDimension;
    private final Set<String> recent = new LinkedHashSet<>();
    private String dimension;
    private String biome;
    private String candidate;
    private long candidateSince;
    private long nextBiome;
    private long blockedUntil;

    public TitleController(int stableTicks, int cooldownTicks, int recentSize, boolean showOnJoin,
        boolean resetOnDimension) {
        this.stableTicks = Math.max(0, stableTicks);
        this.cooldownTicks = Math.max(0, cooldownTicks);
        this.recentSize = Math.max(0, recentSize);
        this.showOnJoin = showOnJoin;
        this.resetOnDimension = resetOnDimension;
    }

    public Kind update(Location location, long tick) {
        if (!location.dimensionKey.equals(dimension)) {
            boolean initial = dimension == null;
            if (resetOnDimension) recent.clear();
            dimension = location.dimensionKey;
            biome = location.biomeKey;
            candidate = null;
            nextBiome = tick;
            blockedUntil = tick;
            remember(biome);
            return initial && !showOnJoin ? null : Kind.DIMENSION;
        }
        if (location.biomeKey.equals(biome)) {
            candidate = null;
            return null;
        }
        if (!location.biomeKey.equals(candidate)) {
            candidate = location.biomeKey;
            candidateSince = tick;
        }
        if (tick - candidateSince < stableTicks || tick < nextBiome || tick < blockedUntil) return null;
        biome = candidate;
        candidate = null;
        if (recent.contains(biome)) return null;
        remember(biome);
        nextBiome = tick + cooldownTicks;
        return Kind.BIOME;
    }

    public void blockUntil(long tick) {
        blockedUntil = tick;
    }

    public void suspend(Location location) {
        if (resetOnDimension && !location.dimensionKey.equals(dimension)) recent.clear();
        dimension = location.dimensionKey;
        biome = location.biomeKey;
        candidate = null;
        remember(biome);
    }

    public void reset() {
        dimension = null;
        biome = null;
        candidate = null;
        candidateSince = 0;
        nextBiome = 0;
        blockedUntil = 0;
        recent.clear();
    }

    private void remember(String key) {
        if (recentSize == 0) return;
        recent.remove(key);
        recent.add(key);
        while (recent.size() > recentSize) recent.remove(
            recent.iterator()
                .next());
    }
}
