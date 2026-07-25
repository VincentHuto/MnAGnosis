package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.effects.neutral.EffectColdDark;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EffectColdDark.class, remap = false, priority = 1000)
public abstract class EffectColdDarkMixin {

    @Inject(
            method = "lambda$removeAttributeModifiers$0",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private static void mnagnosis$stopUndeadProgressionAtTierSix(
            LivingEntity entity,
            IPlayerProgression progression,
            CallbackInfo callback
    ) {
        if (progression.getTier() >= Tier6Progression.MAX_TIER
                || !Tier6Progression.canAdvance(progression, entity.level())) {
            callback.cancel();
        }
    }

    @Redirect(
            method = "lambda$removeAttributeModifiers$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mna/api/capabilities/IPlayerProgression;setTier(ILnet/minecraft/world/entity/player/Player;)V"
            ),
            require = 1
    )
    private static void mnagnosis$replaceUndeadTierSixWithTruth(
            IPlayerProgression progression,
            int requestedTier,
            Player player
    ) {
        Tier6Progression.advanceOrSummonTruthNearPlayer(progression, requestedTier, player);
    }

    @Redirect(
            method = "lambda$removeAttributeModifiers$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V",
                    ordinal = 1
            ),
            require = 1
    )
    private static void mnagnosis$sendUndeadAdvancementMessage(
            Player player,
            Component originalMessage
    ) {
        Tier6Progression.sendAdvancementMessage(player, originalMessage);
    }
}
