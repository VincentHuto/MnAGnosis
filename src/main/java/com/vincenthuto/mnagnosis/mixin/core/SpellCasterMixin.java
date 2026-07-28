package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import com.vincenthuto.mnagnosis.common.spell.IneffableAffinityErosion;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpellCaster.class, remap = false, priority = 1000)
public abstract class SpellCasterMixin {

    @Inject(
            method = "lambda$AddAffinityAndMagicXP$16",
            at = @At("HEAD"),
            require = 1
    )
    private static void mnagnosis$erodeCoreAffinities(
            SpellRecipe spell,
            int channelTicks,
            IPlayerMagic magic,
            Player player,
            IPlayerProgression progression,
            CallbackInfo callback
    ) {
        if (!player.level().isClientSide
                && IneffableAffinityErosion.isIneffable(spell)) {
            IneffableAffinityErosion.erode(magic);
        }
    }

    @Redirect(
            method = "lambda$AddAffinityAndMagicXP$16",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mna/api/capabilities/IPlayerMagic;"
                            + "shiftAffinity(Lnet/minecraft/world/entity/player/Player;"
                            + "Lcom/mna/api/affinity/Affinity;F)V"
            ),
            require = 1
    )
    private static void mnagnosis$replaceOrdinaryAffinity(
            IPlayerMagic magic,
            Player player,
            Affinity affinity,
            float amount,
            SpellRecipe spell,
            int channelTicks,
            IPlayerMagic lambdaMagic,
            Player lambdaPlayer,
            IPlayerProgression progression
    ) {
        if (IneffableAffinityErosion.shouldApplyOrdinaryAffinity(spell)) {
            magic.shiftAffinity(player, affinity, amount);
        }
    }
}
