package com.vincenthuto.mnagnosis.client.render.armor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableArmorClearanceTest {

    @Test
    void preservesIndependentArmorSections() {
        IneffableArmorClearance clearance =
                IneffableArmorClearance.from(true, false, true, false);

        assertEquals(new IneffableArmorClearance(true, false, true, false), clearance);
        assertTrue(clearance.helmetOffset() > 0.0F);
        assertEquals(0.0F, clearance.chestOffset());
        assertTrue(clearance.legsOffset() > 0.0F);
        assertEquals(0.0F, clearance.feetOffset());
    }

    @Test
    void emptyArmorUsesTheSharedBaselineState() {
        assertSame(
                IneffableArmorClearance.NONE,
                IneffableArmorClearance.from(false, false, false, false)
        );
    }

    @Test
    void occupiedSectionsClearArmorByTranslationInsteadOfInflation() {
        IneffableArmorClearance clearance =
                IneffableArmorClearance.from(true, true, true, true);

        assertTrue(4.5F + clearance.helmetOffset() >= 5.25F,
                "The hood does not clear an outer helmet with a visible margin");
        assertTrue(4.35F + clearance.chestOffset() >= 5.25F,
                "The robe body does not clear the sides of outer chest armor");
        assertTrue(2.8F + clearance.chestOffset() >= 3.5F,
                "The robe sleeves do not clear outer armor sleeves");
        assertTrue(clearance.legsOffset() >= 0.5F,
                "The lower robe does not visibly clear equipped leggings");
        assertTrue(clearance.feetOffset() >= 0.3F,
                "The robe hem does not visibly clear equipped boots");
    }
}
