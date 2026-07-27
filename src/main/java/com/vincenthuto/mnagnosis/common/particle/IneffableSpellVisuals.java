package com.vincenthuto.mnagnosis.common.particle;

import com.mna.spells.crafting.SpellRecipe;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipRegistry;
import com.vincenthuto.mnagnosis.common.spell.SpellComponentRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class IneffableSpellVisuals {

    private static final Set<ResourceLocation> COMPONENT_IDS = Set.of(
            SpellComponentRegistry.TRUE_DAMAGE_ID,
            SpellComponentRegistry.GRAVITY_CONVERGENCE_ID,
            SpellComponentRegistry.LIVING_LAND_ID,
            AuthorshipRegistry.BANISH_ID
    );

    private IneffableSpellVisuals() {
    }

    public static boolean containsIneffableComponent(SpellRecipe spell) {
        return spell != null && (
                spell.getComponents().stream()
                        .map(part -> part.getPart().getRegistryName())
                        .anyMatch(COMPONENT_IDS::contains)
                        || spell.getModifiers().stream()
                        .anyMatch(AuthorshipRegistry::isLawInscription)
        );
    }
}
