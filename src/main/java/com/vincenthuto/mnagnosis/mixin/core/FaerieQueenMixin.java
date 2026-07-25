package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.entities.boss.FaerieQueen;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import com.vincenthuto.mnagnosis.common.progression.TruthEncounterService;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(value = FaerieQueen.class, remap = false, priority = 1000)
public abstract class FaerieQueenMixin {

    @Inject(method = "interactAt", at = @At("HEAD"), require = 1)
    private void mnagnosis$captureFeyTruthSource(
            Player player,
            Vec3 hitLocation,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> callback
    ) {
        player.getCapability(com.mna.capabilities.playerdata.progression.PlayerProgressionProvider.PROGRESSION)
                .filter(progression -> Tier6Progression.isEligibleForTruth(progression, player.level()))
                .ifPresent(progression -> {
                    net.minecraft.world.entity.Entity queen = (net.minecraft.world.entity.Entity) (Object) this;
                    TruthEncounterService.rememberFeySource(
                            player, queen.position(), queen.getYRot(), player.level().getGameTime()
                    );
                });
    }

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
        TruthEncounterService.Source source = TruthEncounterService.consumeFeySource(
                player, player.level().getGameTime()
        );
        Tier6Progression.advanceOrSummonTruth(
                progression,
                requestedTier,
                player,
                source != null ? source.position() : player.position(),
                source != null ? source.yaw() : player.getYRot()
        );
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
