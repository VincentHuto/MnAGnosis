package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;

public final class AxiomOfHarmSelection {
    private final HarmAdapterRegistry adapters;
    private final Function<SpellEffect, ResourceLocation> identity;

    public AxiomOfHarmSelection(
            HarmAdapterRegistry adapters,
            Function<SpellEffect, ResourceLocation> identity
    ) {
        this.adapters = Objects.requireNonNull(adapters, "adapters");
        this.identity = Objects.requireNonNull(identity, "identity");
    }

    public HarmSelectionDecision select(ISpellDefinition spell) {
        if (spell == null || !adapters.isFrozen()) {
            return HarmSelectionDecision.failed(
                    HarmSelectionDecision.Failure.ORDER_UNAVAILABLE
            );
        }
        List<IModifiedSpellPart<SpellEffect>> components = spell.getComponents();
        if (components == null) {
            return HarmSelectionDecision.failed(
                    HarmSelectionDecision.Failure.ORDER_UNAVAILABLE
            );
        }
        ArrayList<HarmCandidate> candidates = new ArrayList<>();
        List<IModifiedSpellPart<SpellEffect>> ordered =
                new ArrayList<>(components);
        for (int index = 0; index < ordered.size(); index++) {
            IModifiedSpellPart<SpellEffect> part = ordered.get(index);
            if (part == null || part.getPart() == null) {
                return HarmSelectionDecision.failed(
                        HarmSelectionDecision.Failure.ORDER_UNAVAILABLE
                );
            }
            SpellEffect effect = part.getPart();
            ResourceLocation componentId = identity.apply(effect);
            if (componentId == null) {
                return HarmSelectionDecision.failed(
                        HarmSelectionDecision.Failure.ORDER_UNAVAILABLE
                );
            }
            candidates.add(new HarmCandidate(
                    index,
                    componentId,
                    effect.getClass(),
                    effect.getUseTag()
            ));
        }
        return select(candidates);
    }

    public HarmSelectionDecision select(List<HarmCandidate> candidates) {
        if (candidates == null || !adapters.isFrozen()) {
            return HarmSelectionDecision.failed(
                    HarmSelectionDecision.Failure.ORDER_UNAVAILABLE
            );
        }
        for (HarmCandidate candidate : candidates) {
            if (candidate == null
                    || candidate.componentIndex() < 0
                    || candidate.componentId() == null
                    || candidate.componentType() == null
                    || candidate.useTag() == null) {
                return HarmSelectionDecision.failed(
                        HarmSelectionDecision.Failure.ORDER_UNAVAILABLE
                );
            }
            if (candidate.useTag() != SpellPartTags.HARMFUL) {
                continue;
            }
            var adapter = adapters.resolve(
                    candidate.componentId(),
                    candidate.componentType()
            );
            if (adapter.isPresent()) {
                HarmAdapter<?> resolved = adapter.orElseThrow();
                return HarmSelectionDecision.selected(new HarmSelection(
                        candidate.componentIndex(),
                        candidate.componentId(),
                        resolved.id(),
                        resolved.gate()
                ));
            }
        }
        return HarmSelectionDecision.failed(
                HarmSelectionDecision.Failure.NO_COMPATIBLE_HARM
        );
    }
}
