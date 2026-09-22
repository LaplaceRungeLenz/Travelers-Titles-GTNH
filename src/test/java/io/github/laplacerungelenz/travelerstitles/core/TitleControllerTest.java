package io.github.laplacerungelenz.travelerstitles.core;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

public class TitleControllerTest {

    private Location at(String dimension, String biome) {
        return new Location(dimension, biome, Collections.<String, String>emptyMap());
    }

    private TitleController controller() {
        return new TitleController(3, 10, 2, true, true);
    }

    @Test
    public void waitsForStableBiomeAndIgnoresBoundaryFlicker() {
        TitleController c = controller();
        assertEquals(TitleController.Kind.DIMENSION, c.update(at("earth", "a"), 0));
        assertNull(c.update(at("earth", "b"), 1));
        assertNull(c.update(at("earth", "a"), 2));
        assertNull(c.update(at("earth", "b"), 3));
        assertNull(c.update(at("earth", "b"), 5));
        assertEquals(TitleController.Kind.BIOME, c.update(at("earth", "b"), 6));
    }

    @Test
    public void emitsOnlyLatestCandidateAfterCooldown() {
        TitleController c = controller();
        c.update(at("earth", "a"), 0);
        c.update(at("earth", "b"), 1);
        assertEquals(TitleController.Kind.BIOME, c.update(at("earth", "b"), 4));
        c.update(at("earth", "c"), 5);
        c.update(at("earth", "d"), 6);
        assertNull(c.update(at("earth", "d"), 13));
        assertEquals(TitleController.Kind.BIOME, c.update(at("earth", "d"), 14));
        assertNull(c.update(at("earth", "d"), 30));
    }

    @Test
    public void sharedSpaceBiomeDoesNotHideNewPlanet() {
        TitleController c = controller();
        c.update(at("moon", "space"), 0);
        assertEquals(TitleController.Kind.DIMENSION, c.update(at("mars", "space"), 1));
    }

    @Test
    public void resetRemovesPendingAndRecentWorldState() {
        TitleController c = controller();
        c.update(at("earth", "a"), 0);
        c.update(at("earth", "b"), 1);
        c.reset();
        assertEquals(TitleController.Kind.DIMENSION, c.update(at("earth", "a"), 2));
    }

    @Test
    public void suppressesInitialJoinWhenConfigured() {
        TitleController c = new TitleController(3, 10, 2, false, true);
        assertNull(c.update(at("earth", "a"), 0));
        assertNull(c.update(at("earth", "a"), 10));
        assertEquals(TitleController.Kind.DIMENSION, c.update(at("moon", "a"), 11));
    }

    @Test
    public void dimensionTitleBlocksBiomeUntilFinished() {
        TitleController c = controller();
        c.update(at("earth", "a"), 0);
        c.blockUntil(20);
        c.update(at("earth", "b"), 1);
        assertNull(c.update(at("earth", "b"), 10));
        assertEquals(TitleController.Kind.BIOME, c.update(at("earth", "b"), 20));
    }

    @Test
    public void hiddenHudDoesNotQueueStaleLocations() {
        TitleController c = controller();
        c.update(at("earth", "a"), 0);
        c.suspend(at("moon", "b"));
        assertNull(c.update(at("moon", "b"), 20));
        assertEquals(TitleController.Kind.DIMENSION, c.update(at("mars", "b"), 21));
    }

    @Test
    public void zeroRecentCacheStillDoesNotRepeatWhileStationary() {
        TitleController c = new TitleController(0, 0, 0, true, true);
        c.update(at("earth", "a"), 0);
        assertNull(c.update(at("earth", "a"), 1));
        assertEquals(TitleController.Kind.BIOME, c.update(at("earth", "b"), 2));
        assertNull(c.update(at("earth", "b"), 3));
    }
}
