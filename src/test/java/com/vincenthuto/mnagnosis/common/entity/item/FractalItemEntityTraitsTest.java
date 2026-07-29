package com.vincenthuto.mnagnosis.common.entity.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FractalItemEntityTraitsTest {

    @Test
    void staticDefaultsNeverBobOrRotate() {
        assertEquals(
                0.0F,
                FractalItemEntityTraits.STATIC.bobOffset(0.0F),
                0.00001F
        );
        assertEquals(
                0.0F,
                FractalItemEntityTraits.STATIC.bobOffset(500.0F),
                0.00001F
        );
        assertEquals(
                0.0F,
                FractalItemEntityTraits.STATIC.rotation(500.0F),
                0.00001F
        );
    }

    @Test
    void animatedTraitsDeriveOffsetsFromAge() {
        FractalItemEntityTraits traits = new FractalItemEntityTraits(
                0.25F,
                0.5F,
                0.02F,
                0.3F,
                1.0F,
                0.0F,
                false
        );

        assertEquals(
                (float) Math.sin(1.0F) * 0.25F,
                traits.bobOffset(2.0F),
                0.00001F
        );
        assertEquals(0.5F, traits.rotation(10.0F), 0.00001F);
    }

    @Test
    void rejectsInvalidProfiles() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FractalItemEntityTraits(
                        0, 0, 0, 0, 0, 0, false
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new FractalItemEntityTraits(
                        Float.NaN, 0, 0, 0, 1, 0, false
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new FractalItemEntityTraits(
                        0, 0, Float.POSITIVE_INFINITY,
                        0, 1, 0, false
                )
        );
    }
}
