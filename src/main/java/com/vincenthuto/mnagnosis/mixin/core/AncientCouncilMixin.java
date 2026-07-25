package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.entities.rituals.AncientCouncil;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AncientCouncil.class, remap = false, priority = 1000)
public abstract class AncientCouncilMixin {

    @ModifyConstant(
            method = "lambda$new$5",
            constant = @Constant(intValue = 5),
            require = 1
    )
    private int mnagnosis$allowCouncilTierSix(int originalMaximum) {
        return Tier6Progression.MAX_TIER;
    }

    @Redirect(
            method = "lambda$new$5",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mna/api/capabilities/IPlayerProgression;setTier(ILnet/minecraft/world/entity/player/Player;)V"
            ),
            require = 1
    )
    private void mnagnosis$requireCouncilTierProgress(
            IPlayerProgression progression,
            int requestedTier,
            Player player
    ) {
        Entity council = (Entity) (Object) this;
        Tier6Progression.advanceOrSummonTruth(
                progression, requestedTier, player, council.position(), council.getYRot()
        );
    }

    @Redirect(
            method = "lambda$new$5",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V",
                    ordinal = 1
            ),
            require = 1
    )
    private void mnagnosis$sendCouncilAdvancementMessage(
            Player player,
            Component originalMessage
    ) {
        Tier6Progression.sendAdvancementMessage(player, originalMessage);
    }
}
