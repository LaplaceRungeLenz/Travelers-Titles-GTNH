package io.github.laplacerungelenz.travelerstitles.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class AnimationTest {

    @Test
    public void fadesAtBothEndsAndHoldsInMiddle() {
        assertEquals(0, Animation.alpha(0, 10, 20, 10), 0.001);
        assertEquals(0.5, Animation.alpha(5, 10, 20, 10), 0.001);
        assertEquals(1, Animation.alpha(20, 10, 20, 10), 0.001);
        assertEquals(0.5, Animation.alpha(35, 10, 20, 10), 0.001);
        assertEquals(0, Animation.alpha(40, 10, 20, 10), 0.001);
    }

    @Test
    public void zeroDurationsAreFiniteAndInstant() {
        assertEquals(0, Animation.alpha(0, 0, 0, 0), 0);
        assertEquals(1, Animation.alpha(0, 0, 5, 0), 0);
        assertEquals(0, Animation.alpha(5, 0, 5, 0), 0);
    }
}
