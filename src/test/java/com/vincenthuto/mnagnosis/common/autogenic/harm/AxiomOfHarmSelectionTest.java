package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.spells.components.ComponentFireDamage;
import com.mna.spells.components.ComponentPoison;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AxiomOfHarmSelectionTest {

    @Test
    void selectsFirstCompatibleHarmInRecipeOrder() {
        AxiomOfHarmSelection selector = selector();

        HarmSelectionDecision decision = selector.select(List.of(
                candidate(0, id("components/true_damage"), TestEffect.class),
                candidate(1, PoisonHarmAdapter.COMPONENT_ID, ComponentPoison.class),
                candidate(2, FireDamageHarmAdapter.COMPONENT_ID, ComponentFireDamage.class)
        ));

        assertEquals(HarmSelectionDecision.Failure.NONE, decision.failure());
        HarmSelection selected = decision.selection().orElseThrow();
        assertEquals(1, selected.componentIndex());
        assertEquals(PoisonHarmAdapter.COMPONENT_ID, selected.componentId());
        assertEquals(HarmGate.UNDEAD_POISON_IMMUNITY, selected.gate());
    }

    @Test
    void ignoresFriendlyAndNeutralCandidatesAndSelectsFirstDuplicateIndex() {
        AxiomOfHarmSelection selector = selector();

        HarmSelectionDecision decision = selector.select(List.of(
                candidate(
                        0,
                        FireDamageHarmAdapter.COMPONENT_ID,
                        ComponentFireDamage.class,
                        SpellPartTags.FRIENDLY
                ),
                candidate(
                        1,
                        FireDamageHarmAdapter.COMPONENT_ID,
                        ComponentFireDamage.class,
                        SpellPartTags.NEUTRAL
                ),
                candidate(2, FireDamageHarmAdapter.COMPONENT_ID, ComponentFireDamage.class),
                candidate(3, FireDamageHarmAdapter.COMPONENT_ID, ComponentFireDamage.class)
        ));

        assertEquals(2, decision.selection().orElseThrow().componentIndex());
    }

    @Test
    void trueDamageAloneHasNoCompatibleHarm() {
        HarmSelectionDecision decision = selector().select(List.of(
                candidate(0, id("components/true_damage"), TestEffect.class)
        ));

        assertTrue(decision.selection().isEmpty());
        assertEquals(
                HarmSelectionDecision.Failure.NO_COMPATIBLE_HARM,
                decision.failure()
        );
    }

    @Test
    void malformedCandidatesAndUnfrozenRegistryFailClosed() {
        AxiomOfHarmSelection selector = selector();

        assertEquals(
                HarmSelectionDecision.Failure.ORDER_UNAVAILABLE,
                selector.select((List<HarmCandidate>) null).failure()
        );
        assertEquals(
                HarmSelectionDecision.Failure.ORDER_UNAVAILABLE,
                selector.select(Arrays.asList((HarmCandidate) null)).failure()
        );
        assertEquals(
                HarmSelectionDecision.Failure.ORDER_UNAVAILABLE,
                selector.select(List.of(new HarmCandidate(
                        0,
                        null,
                        ComponentFireDamage.class,
                        SpellPartTags.HARMFUL
                ))).failure()
        );

        HarmAdapterRegistry unfrozen = new HarmAdapterRegistry();
        unfrozen.register(new FireDamageHarmAdapter());
        AxiomOfHarmSelection unfrozenSelector =
                new AxiomOfHarmSelection(unfrozen, SpellEffect::getRegistryName);
        assertEquals(
                HarmSelectionDecision.Failure.ORDER_UNAVAILABLE,
                unfrozenSelector.select(List.of(candidate(
                        0,
                        FireDamageHarmAdapter.COMPONENT_ID,
                        ComponentFireDamage.class
                ))).failure()
        );
    }

    @Test
    void knownIdWithWrongRuntimeClassIsNotCompatible() {
        HarmSelectionDecision decision = selector().select(List.of(candidate(
                0,
                FireDamageHarmAdapter.COMPONENT_ID,
                ComponentPoison.class
        )));

        assertEquals(
                HarmSelectionDecision.Failure.NO_COMPATIBLE_HARM,
                decision.failure()
        );
    }

    private static AxiomOfHarmSelection selector() {
        HarmAdapterRegistry registry = new HarmAdapterRegistry();
        registry.register(new FireDamageHarmAdapter());
        registry.register(new PoisonHarmAdapter());
        registry.freeze();
        return new AxiomOfHarmSelection(registry, SpellEffect::getRegistryName);
    }

    private static HarmCandidate candidate(
            int index,
            ResourceLocation id,
            Class<? extends SpellEffect> type
    ) {
        return candidate(index, id, type, SpellPartTags.HARMFUL);
    }

    private static HarmCandidate candidate(
            int index,
            ResourceLocation id,
            Class<? extends SpellEffect> type,
            SpellPartTags useTag
    ) {
        return new HarmCandidate(index, id, type, useTag);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("mnagnosis", path);
    }

    private abstract static class TestEffect extends SpellEffect {
        private TestEffect() {
            super(id("test.png"));
        }
    }
}
