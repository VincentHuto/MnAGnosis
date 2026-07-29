package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.parts.Modifier;
import com.vincenthuto.mnagnosis.common.autogenic.AutogenicAccess;
import net.minecraft.resources.ResourceLocation;

public final class AxiomOfHarmModifier extends Modifier {
    public AxiomOfHarmModifier(ResourceLocation icon) {
        super(icon, 5000);
    }

    @Override
    public boolean isCraftable(SpellCraftingContext context) {
        return context != null && AutogenicAccess.canUse(context.getPlayer());
    }
}
