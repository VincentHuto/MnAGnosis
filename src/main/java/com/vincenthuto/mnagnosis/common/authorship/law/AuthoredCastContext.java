package com.vincenthuto.mnagnosis.common.authorship.law;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record AuthoredCastContext(
        ServerPlayer player,
        ISpellDefinition spell,
        SpellSource source,
        SpellContext spellContext,
        ItemStack stack,
        ResourceLocation interpretationId,
        float baseManaCost
) {
}
