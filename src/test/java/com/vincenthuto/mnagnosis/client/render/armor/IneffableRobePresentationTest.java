package com.vincenthuto.mnagnosis.client.render.armor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableRobePresentationTest {

    @Test
    void originalBodyAndHoodRenderWithoutRelevantArmor() {
        IneffableRobePresentation none =
                IneffableRobePresentation.from(false, false, false, false);
        IneffableRobePresentation boots =
                IneffableRobePresentation.from(false, false, false, true);

        assertFalse(none.armoredBody());
        assertTrue(none.hoodVisible());
        assertEquals(none, boots);
    }

    @Test
    void helmetHidesHoodWithoutChangingBody() {
        IneffableRobePresentation presentation =
                IneffableRobePresentation.from(true, false, false, false);

        assertFalse(presentation.armoredBody());
        assertFalse(presentation.hoodVisible());
    }

    @Test
    void chestOrLegsSelectArmoredBodyIndependentlyOfHood() {
        assertEquals(
                new IneffableRobePresentation(true, true),
                IneffableRobePresentation.from(false, true, false, false)
        );
        assertEquals(
                new IneffableRobePresentation(true, true),
                IneffableRobePresentation.from(false, false, true, false)
        );
        assertEquals(
                new IneffableRobePresentation(true, false),
                IneffableRobePresentation.from(true, true, true, false)
        );
    }
}
