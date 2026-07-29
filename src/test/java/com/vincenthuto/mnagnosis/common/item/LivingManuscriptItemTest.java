package com.vincenthuto.mnagnosis.common.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LivingManuscriptItemTest {
    @Test
    void requiresTierSixIneffableProgressionAndManuscriptState() {
        assertTrue(LivingManuscriptAccess.canOpen(6, true, true));
        assertFalse(LivingManuscriptAccess.canOpen(5, true, true));
        assertFalse(LivingManuscriptAccess.canOpen(6, false, true));
        assertFalse(LivingManuscriptAccess.canOpen(6, true, false));
    }
}
