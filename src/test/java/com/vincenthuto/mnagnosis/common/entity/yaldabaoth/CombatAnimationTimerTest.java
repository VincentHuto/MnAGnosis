package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatAnimationTimerTest {

    @Test
    void triggerStartsAtTheRequestedDuration() {
        assertEquals(36, CombatAnimationTimer.trigger(36));
    }

    @Test
    void tickCountsDownWithoutBecomingNegative() {
        assertEquals(2, CombatAnimationTimer.tick(3));
        assertEquals(0, CombatAnimationTimer.tick(0));
        assertEquals(0, CombatAnimationTimer.tick(-4));
    }

    @Test
    void loadedValuesAreClampedToTheEntitiesDuration() {
        assertEquals(0, CombatAnimationTimer.clampLoaded(-1, 36));
        assertEquals(18, CombatAnimationTimer.clampLoaded(18, 36));
        assertEquals(36, CombatAnimationTimer.clampLoaded(200, 36));
    }

    @Test
    void onlyPositiveTimeIsActive() {
        assertTrue(CombatAnimationTimer.isActive(1));
        assertFalse(CombatAnimationTimer.isActive(0));
    }
}
