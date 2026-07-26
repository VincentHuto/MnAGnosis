package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.api.config.GeneralConfigValues;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerProgression.class, remap = false, priority = 1000)
public abstract class PlayerProgressionMixin {

    @Shadow
    private int tier;

    @ModifyArg(
            method = "setTier",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mna/tools/math/MathUtils;clamp(III)I"
            ),
            index = 2,
            require = 1
    )
    private int mnagnosis$allowTierSix(int originalMaximum) {
        return Tier6Progression.MAX_TIER;
    }

    @Inject(
            method = "setTier",
            at = @At("TAIL"),
            require = 1
    )
    private void mnagnosis$joinIneffableAtTierSix(
            int requestedTier,
            Player player,
            boolean notifyTierRote,
            CallbackInfo callback
    ) {
        Tier6Progression.enforceIneffable((PlayerProgression) (Object) this, player);
    }

    @ModifyVariable(
            method = "setAlliedFaction",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 1
    )
    private IFaction mnagnosis$lockTierSixFaction(IFaction requestedFaction) {
        return this.tier == Tier6Progression.MAX_TIER
                ? IneffableFactionRegistry.INEFFABLE_FACTION
                : requestedFaction;
    }

    @ModifyVariable(
            method = "setFactionStanding",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 1
    )
    private int mnagnosis$lockTierSixFactionStanding(int requestedStanding) {
        return this.tier == Tier6Progression.MAX_TIER ? 0 : requestedStanding;
    }

    @Inject(
            method = "canBeRaided(Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void mnagnosis$ignoreOrdinaryTierSixRaids(
            Player player,
            CallbackInfoReturnable<Boolean> callback
    ) {
        PlayerProgression progression = (PlayerProgression) (Object) this;
        if (this.tier == Tier6Progression.MAX_TIER
                && progression.getAlliedFaction() == IneffableFactionRegistry.INEFFABLE_FACTION
                && !progression.hasForceRaid()) {
            callback.setReturnValue(false);
        }
    }

    @Inject(
            method = "canBeRaided(Lcom/mna/api/faction/IFaction;Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void mnagnosis$ignoreOrdinaryTierSixRaidFromFaction(
            IFaction faction,
            Player player,
            CallbackInfoReturnable<Boolean> callback
    ) {
        PlayerProgression progression = (PlayerProgression) (Object) this;
        if (this.tier == Tier6Progression.MAX_TIER
                && progression.getAlliedFaction() == IneffableFactionRegistry.INEFFABLE_FACTION
                && !progression.hasForceRaid()) {
            callback.setReturnValue(false);
        }
    }

    @Inject(
            method = "getTierMaxComplexity",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void mnagnosis$useTierFiveComplexityAtTierSix(
            CallbackInfoReturnable<Integer> callback
    ) {
        if (this.tier == Tier6Progression.MAX_TIER) {
            callback.setReturnValue(GeneralConfigValues.Tier5ComplexityLimit);
        }
    }
}
