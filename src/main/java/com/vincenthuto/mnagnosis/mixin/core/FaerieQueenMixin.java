package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.entities.boss.FaerieQueen;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FaerieQueen.class, remap = false, priority = 1000)
public abstract class FaerieQueenMixin {

    @ModifyConstant(
            method = "lambda$interactAt$10",
            constant = @Constant(intValue = 5),
            require = 1
    )
    private static int mnagnosis$allowFeyTierSix(int originalMaximum) {
        return Tier6Progression.MAX_TIER;
    }

    @Redirect(
            method = "lambda$interactAt$10",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mna/api/capabilities/IPlayerProgression;setTier(ILnet/minecraft/world/entity/player/Player;)V"
            ),
            require = 1
    )
    private static void mnagnosis$requireFeyTierProgress(
            IPlayerProgression progression,
            int requestedTier,
            Player player
    ) {
        Tier6Progression.advanceIfReady(progression, requestedTier, player);
    }

    @Redirect(
            method = "lambda$interactAt$10",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V",
                    ordinal = 1
            ),
            require = 1
    )
    private static void mnagnosis$sendFeyAdvancementMessage(
            Player player,
            Component originalMessage
    ) {
        Tier6Progression.sendAdvancementMessage(player, originalMessage);
    }
}
