package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.vincenthuto.mnagnosis.common.autogenic.AutogenicSpellClassifier;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AxiomOfHarmManaTest {

    @Test
    void appliesThirtyFivePercentExactlyOnce() {
        assertEquals(135.0F, AxiomOfHarmMana.adjustedCost(100.0F));
        assertEquals(13.5F, AxiomOfHarmMana.adjustedCost(10.0F));
    }

    @Test
    void invalidIncomingCostFailsClosed() {
        assertEquals(Float.MAX_VALUE, AxiomOfHarmMana.adjustedCost(-1.0F));
        assertEquals(
                Float.MAX_VALUE,
                AxiomOfHarmMana.adjustedCost(Float.POSITIVE_INFINITY)
        );
        assertEquals(
                Float.MAX_VALUE,
                AxiomOfHarmMana.adjustedCost(Float.NaN)
        );
        assertEquals(
                Float.MAX_VALUE,
                AxiomOfHarmMana.adjustedCost(Float.MAX_VALUE)
        );
    }

    @Test
    void classifierTreatsDuplicateAxiomModifiersAsOnePresence() {
        ResourceLocation other = id("other");
        assertFalse(AutogenicSpellClassifier.hasAxiom(List.of()));
        assertFalse(AutogenicSpellClassifier.hasAxiom(List.of(other)));
        assertTrue(AutogenicSpellClassifier.hasAxiom(List.of(
                other,
                AutogenicSpellClassifier.AXIOM_OF_HARM_ID,
                AutogenicSpellClassifier.AXIOM_OF_HARM_ID
        )));
        assertEquals(
                135.0F,
                AxiomOfHarmMana.adjustedCost(
                        AutogenicSpellClassifier.hasAxiom(List.of(
                                AutogenicSpellClassifier.AXIOM_OF_HARM_ID,
                                AutogenicSpellClassifier.AXIOM_OF_HARM_ID
                        ))
                                ? 100.0F
                                : Float.MAX_VALUE
                )
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("mnagnosis", path);
    }
}
