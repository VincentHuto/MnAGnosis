package com.vincenthuto.mnagnosis.common.autogenic;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Modifier;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class AutogenicSpellClassifier {
    public static final ResourceLocation AXIOM_OF_HARM_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "mnagnosis",
                    "axiom_of_harm"
            );

    private AutogenicSpellClassifier() {
    }

    public static boolean hasAxiom(List<ResourceLocation> modifierIds) {
        return modifierIds != null && modifierIds.stream()
                .anyMatch(AXIOM_OF_HARM_ID::equals);
    }

    public static boolean hasAxiom(ISpellDefinition spell) {
        if (spell == null || spell.getModifiers() == null) {
            return false;
        }
        return spell.getModifiers().stream()
                .filter(java.util.Objects::nonNull)
                .map(Modifier::getRegistryName)
                .anyMatch(AXIOM_OF_HARM_ID::equals);
    }
}
