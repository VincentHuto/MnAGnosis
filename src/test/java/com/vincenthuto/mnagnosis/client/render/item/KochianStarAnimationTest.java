package com.vincenthuto.mnagnosis.client.render.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KochianStarAnimationTest {

    private static final float EPSILON = 0.000_1F;

    @Test
    void cycleGrowsFromCoarseToFineAndReturnsContinuously() {
        KochianStarAnimation.Frame start = KochianStarAnimation.sample(0.0F);
        KochianStarAnimation.Frame middle =
                KochianStarAnimation.sample(10.0F);
        KochianStarAnimation.Frame end = KochianStarAnimation.sample(20.0F);

        assertEquals(78.0F, start.angleDegrees(), EPSILON);
        assertEquals(3.0F, start.recursion(), EPSILON);
        assertEquals(88.0F, middle.angleDegrees(), EPSILON);
        assertEquals(8.0F, middle.recursion(), EPSILON);
        assertEquals(start, end);
    }

    @Test
    void quarterCycleUsesSmoothedMidpointAndNeverLeavesSafeRanges() {
        KochianStarAnimation.Frame quarter =
                KochianStarAnimation.sample(5.0F);

        assertEquals(83.0F, quarter.angleDegrees(), EPSILON);
        assertEquals(5.5F, quarter.recursion(), EPSILON);
        for (int sample = -200; sample <= 400; sample++) {
            KochianStarAnimation.Frame frame =
                    KochianStarAnimation.sample(sample * 0.1F);
            assertTrue(frame.angleDegrees() >= 78.0F);
            assertTrue(frame.angleDegrees() <= 88.0F);
            assertTrue(frame.recursion() >= 3.0F);
            assertTrue(frame.recursion() <= 8.0F);
        }
    }
}
