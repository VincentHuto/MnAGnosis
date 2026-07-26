package com.vincenthuto.mnagnosis.common.authorship.law.suspension;

import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import net.minecraft.server.level.ServerPlayer;

public final class ManaCostSuspension {

    private ManaCostSuspension() {
    }

    public static void release(
            ServerPlayer player,
            SuspensionPayload payload,
            SuspensionScheduler.ReleaseReason reason
    ) {
        float deferred = Math.max(0.0F, payload.consequence().getFloat("deferred"));
        IneffableMana mana = player.getCapability(PlayerMagicProvider.MAGIC)
                .map(value -> value.getCastingResource())
                .filter(IneffableMana.class::isInstance)
                .map(IneffableMana.class::cast)
                .orElse(null);
        if (mana == null || deferred <= 0.0F) {
            return;
        }
        float paid = Math.min(mana.getAmount(), deferred);
        mana.setAmount(mana.getAmount() - paid);
        float shortfall = deferred - paid;
        if (shortfall > 0.0F && reason == SuspensionScheduler.ReleaseReason.VENT) {
            float damage = Math.min(player.getMaxHealth() * 0.40F, shortfall / 20.0F);
            if (damage > 0.0F) {
                player.hurt(player.damageSources().magic(), damage);
            }
        }
    }
}
