package com.vincenthuto.mnagnosis.client.render.armor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableRobesCurioLookupTest {

    @Test
    void findsOnlyVisibleMatchingStacks() {
        assertTrue(IneffableRobesCurioLookup.containsVisibleMatch(
                2,
                index -> index == 1,
                List.of(true, true)
        ));
        assertFalse(IneffableRobesCurioLookup.containsVisibleMatch(
                2,
                index -> index == 1,
                List.of(true, false)
        ));
    }

    @Test
    void missingSlotsAndRenderFlagsFailClosed() {
        assertFalse(IneffableRobesCurioLookup.containsVisibleMatch(
                0,
                index -> true,
                List.of()
        ));
        assertFalse(IneffableRobesCurioLookup.containsVisibleMatch(
                1,
                index -> true,
                List.of()
        ));
    }
}
