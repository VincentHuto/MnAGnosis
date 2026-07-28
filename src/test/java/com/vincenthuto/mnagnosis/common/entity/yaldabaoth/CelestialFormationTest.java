package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CelestialFormationTest {

    private static final double EPSILON = 1.0E-6D;

    @Test
    void sunAndMoonRemainOnOppositeFacingRelativeSides() {
        assertOffset(0.0F, CelestialRole.SUN, -6.0D, 0.0D);
        assertOffset(0.0F, CelestialRole.MOON, 6.0D, 0.0D);
        assertOffset(90.0F, CelestialRole.SUN, 0.0D, -6.0D);
        assertOffset(90.0F, CelestialRole.MOON, 0.0D, 6.0D);
        assertOffset(180.0F, CelestialRole.SUN, 6.0D, 0.0D);
        assertOffset(180.0F, CelestialRole.MOON, -6.0D, 0.0D);
        assertOffset(270.0F, CelestialRole.SUN, 0.0D, 6.0D);
        assertOffset(270.0F, CelestialRole.MOON, 0.0D, -6.0D);
    }

    @Test
    void bobbingHasExactAmplitudePeriodAndOppositePhase() {
        CelestialFormation.Offset sunStart =
                CelestialFormation.offset(0.0F, 0L, CelestialRole.SUN);
        CelestialFormation.Offset moonStart =
                CelestialFormation.offset(0.0F, 0L, CelestialRole.MOON);
        CelestialFormation.Offset sunPeak =
                CelestialFormation.offset(0.0F, 20L, CelestialRole.SUN);
        CelestialFormation.Offset moonTrough =
                CelestialFormation.offset(0.0F, 20L, CelestialRole.MOON);
        CelestialFormation.Offset sunClosed =
                CelestialFormation.offset(0.0F, 80L, CelestialRole.SUN);

        assertEquals(5.0D, sunStart.y(), EPSILON);
        assertEquals(5.0D, moonStart.y(), EPSILON);
        assertEquals(5.75D, sunPeak.y(), EPSILON);
        assertEquals(4.25D, moonTrough.y(), EPSILON);
        assertEquals(sunStart.y(), sunClosed.y(), EPSILON);
    }

    @Test
    void respawnCountdownWaitsAllFourHundredTicks() {
        int remaining = CelestialFormation.RESPAWN_TICKS;
        for (int tick = 0; tick < 399; tick++) {
            remaining = CelestialFormation.tickRespawn(remaining);
        }
        assertEquals(1, remaining);
        assertFalse(CelestialFormation.isRespawnReady(remaining));

        remaining = CelestialFormation.tickRespawn(remaining);
        assertEquals(0, remaining);
        assertTrue(CelestialFormation.isRespawnReady(remaining));
        assertEquals(0, CelestialFormation.tickRespawn(0));
    }

    private static void assertOffset(
            float yaw,
            CelestialRole role,
            double expectedX,
            double expectedZ
    ) {
        CelestialFormation.Offset offset =
                CelestialFormation.offset(yaw, 0L, role);
        assertEquals(expectedX, offset.x(), EPSILON);
        assertEquals(expectedZ, offset.z(), EPSILON);
    }
}
