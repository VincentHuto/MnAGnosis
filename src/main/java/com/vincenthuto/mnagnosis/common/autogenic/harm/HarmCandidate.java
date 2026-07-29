package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.parts.SpellEffect;
import net.minecraft.resources.ResourceLocation;

public record HarmCandidate(
        int componentIndex,
        ResourceLocation componentId,
        Class<? extends SpellEffect> componentType,
        SpellPartTags useTag
) {
}
