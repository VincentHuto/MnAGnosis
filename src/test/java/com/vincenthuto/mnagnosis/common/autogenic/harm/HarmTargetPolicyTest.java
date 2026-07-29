package com.vincenthuto.mnagnosis.common.autogenic.harm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HarmTargetPolicyTest {

    @Test
    void rejectsInvalidLifecycleAndDimensionFactsFirst() {
        assertEquals(
                HarmTargetDecision.INVALID_TARGET,
                HarmTargetPolicy.evaluate(facts(false, true, false, true, true))
        );
        assertEquals(
                HarmTargetDecision.INVALID_TARGET,
                HarmTargetPolicy.evaluate(facts(true, false, false, true, true))
        );
        assertEquals(
                HarmTargetDecision.INVALID_TARGET,
                HarmTargetPolicy.evaluate(facts(true, true, true, true, true))
        );
        assertEquals(
                HarmTargetDecision.INVALID_TARGET,
                HarmTargetPolicy.evaluate(facts(true, true, false, false, true))
        );
        assertEquals(
                HarmTargetDecision.INVALID_TARGET,
                HarmTargetPolicy.evaluate(facts(true, true, false, true, false))
        );
    }

    @Test
    void appliesAbsoluteAllianceAndPvpProtectionsInOrder() {
        assertEquals(
                HarmTargetDecision.ABSOLUTE_PROTECTION,
                HarmTargetPolicy.evaluate(valid(true, false, false, true))
        );
        assertEquals(
                HarmTargetDecision.ABSOLUTE_PROTECTION,
                HarmTargetPolicy.evaluate(valid(false, true, false, true))
        );
        assertEquals(
                HarmTargetDecision.ALLIED,
                HarmTargetPolicy.evaluate(valid(false, false, true, true))
        );
        assertEquals(
                HarmTargetDecision.PVP_DENIED,
                HarmTargetPolicy.evaluate(valid(false, false, false, false))
        );
        assertEquals(
                HarmTargetDecision.ALLOW,
                HarmTargetPolicy.evaluate(valid(false, false, false, true))
        );
    }

    private static HarmTargetFacts facts(
            boolean present,
            boolean alive,
            boolean removed,
            boolean loaded,
            boolean sameDimension
    ) {
        return new HarmTargetFacts(
                present,
                alive,
                removed,
                loaded,
                sameDimension,
                false,
                false,
                false,
                true
        );
    }

    private static HarmTargetFacts valid(
            boolean invulnerable,
            boolean creativeOrSpectator,
            boolean allied,
            boolean pvpAllowed
    ) {
        return new HarmTargetFacts(
                true,
                true,
                false,
                true,
                true,
                invulnerable,
                creativeOrSpectator,
                allied,
                pvpAllowed
        );
    }
}
