package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GravityFlightContactPolicyTest {

    @Test
    void activeMobileGravityNeverForciblyDisablesFlight() {
        assertEquals(
                GravityFlightContactPolicy.Action.SUSPEND_GRAVITY,
                GravityFlightContactPolicy.decide(true, true, false, false)
        );
        assertEquals(
                GravityFlightContactPolicy.Action.SUSPEND_GRAVITY,
                GravityFlightContactPolicy.decide(true, true, false, true)
        );
    }

    @Test
    void gravitySurfaceContactSuspendsWhileFlightRemainsEnabled() {
        assertEquals(
                GravityFlightContactPolicy.Action.SUSPEND_GRAVITY,
                GravityFlightContactPolicy.decide(true, false, true, false)
        );
    }

    @Test
    void ordinaryCreativeFlightIsUnaffected() {
        assertEquals(
                GravityFlightContactPolicy.Action.NONE,
                GravityFlightContactPolicy.decide(true, false, false, true)
        );
    }
}
