package io.github.laplacerungelenz.travelerstitles.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SpaceCompatTest {
    public static class Body {
        public String getUnlocalizedName() { return "planet.MixedCase"; }
        public String getName() { return "MixedCase"; }
        public String getLocalizedName() { return "Planet title"; }
    }
    public static class Provider {
        public Object getCelestialBody() { return new Body(); }
    }
    public static class BrokenProvider {
        public Object getCelestialBody() { throw new IllegalStateException("Not synchronized yet"); }
    }
    @Test public void readsBodyIdentityWithoutChangingCase() {
        assertEquals("planet.MixedCase", SpaceCompat.describe(new Provider()).get("body"));
    }
    @Test public void absentOptionalApiHasNoHardLinkage() {
        assertTrue(SpaceCompat.describe(new Object()).isEmpty());
        assertTrue(SpaceCompat.registry().isEmpty());
    }
    @Test public void unsynchronizedProviderFallsBackAndCanRecover() {
        assertTrue(SpaceCompat.describe(new BrokenProvider()).isEmpty());
        assertEquals("Planet title", SpaceCompat.describe(new Provider()).get("bodyTitle"));
    }
}
