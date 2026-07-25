package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.entities.boss.DemonLord;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DemonLord.class, remap = false, priority = 1000)
public abstract class DemonLordMixin {

    @ModifyConstant(
            method = "lambda$interactAt$14",
            constant = @Constant(intValue = 5),
            require = 1
    )
    private int mnagnosis$allowDemonTierSix(int originalMaximum) {
        return Tier6Progression.MAX_TIER;
    }

    @Redirect(
            method = "lambda$interactAt$14",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mna/api/capabilities/IPlayerProgression;setTier(ILnet/minecraft/world/entity/player/Player;)V"
            ),
            require = 1
    )
    private void mnagnosis$requireDemonTierProgress(
            IPlayerProgression progression,
            int requestedTier,
            Player player
    ) {
        Entity demonLord = (Entity) (Object) this;
        Tier6Progression.advanceOrSummonTruth(
                progression, requestedTier, player, demonLord.position(), demonLord.getYRot()
        );
    }

    @Redirect(
            method = "lambda$interactAt$14",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V",
                    ordinal = 1
            ),
            require = 1
    )
    private void mnagnosis$sendDemonAdvancementMessage(
            Player player,
            Component originalMessage
    ) {
        Tier6Progression.sendAdvancementMessage(player, originalMessage);
    }
}
