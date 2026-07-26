package com.vincenthuto.mnagnosis.common.spell;

import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.parts.Modifier;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class PrecisionModifier extends Modifier {

    public PrecisionModifier(ResourceLocation icon) {
        super(icon, 5000);
    }

    @Override
    public boolean isCraftable(SpellCraftingContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }
        return player.getCapability(PlayerProgressionProvider.PROGRESSION)
                .map(progression -> progression.getTier() == 6
                        && progression.getAlliedFaction()
                        == IneffableFactionRegistry.INEFFABLE_FACTION)
                .orElse(false);
    }
}
