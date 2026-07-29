package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.api.spells.ComponentApplicationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AxiomOfHarmProgressionGateTest {
    @Test
    void requiresConsumedImmunityAndSuccessfulNativeApplication() {
        assertTrue(AxiomOfHarmDecorator.completedCrossing(
                ComponentApplicationResult.SUCCESS,
                new HarmInvocationScope.Outcome(
                        true,
                        true,
                        HarmGate.FIRE_TYPE_IMMUNITY
                )
        ));
        assertFalse(AxiomOfHarmDecorator.completedCrossing(
                ComponentApplicationResult.FAIL,
                new HarmInvocationScope.Outcome(
                        true,
                        true,
                        HarmGate.FIRE_TYPE_IMMUNITY
                )
        ));
        assertFalse(AxiomOfHarmDecorator.completedCrossing(
                ComponentApplicationResult.SUCCESS,
                new HarmInvocationScope.Outcome(
                        false,
                        true,
                        HarmGate.FIRE_TYPE_IMMUNITY
                )
        ));
        assertFalse(AxiomOfHarmDecorator.completedCrossing(
                ComponentApplicationResult.SUCCESS,
                new HarmInvocationScope.Outcome(
                        true,
                        false,
                        HarmGate.FIRE_TYPE_IMMUNITY
                )
        ));
    }
}
