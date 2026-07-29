package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.api.spells.targeting.SpellSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class HarmTargetPolicy {
    private HarmTargetPolicy() {
    }

    public static HarmTargetDecision evaluate(HarmTargetFacts facts) {
        if (facts == null
                || !facts.present()
                || !facts.alive()
                || facts.removed()
                || !facts.loaded()
                || !facts.sameDimension()) {
            return HarmTargetDecision.INVALID_TARGET;
        }
        if (facts.invulnerable() || facts.creativeOrSpectator()) {
            return HarmTargetDecision.ABSOLUTE_PROTECTION;
        }
        if (facts.allied()) {
            return HarmTargetDecision.ALLIED;
        }
        return facts.pvpAllowed()
                ? HarmTargetDecision.ALLOW
                : HarmTargetDecision.PVP_DENIED;
    }

    public static HarmTargetDecision evaluate(
            SpellSource source,
            LivingEntity target
    ) {
        LivingEntity caster = source == null ? null : source.getCaster();
        boolean present = caster != null && target != null;
        boolean sameDimension = present && caster.level() == target.level();
        boolean allied = present
                && (caster.isAlliedTo(target) || target.isAlliedTo(caster));
        boolean creativeOrSpectator = target instanceof Player player
                && (player.isCreative() || player.isSpectator());
        boolean pvpAllowed = !(caster instanceof Player attacking)
                || !(target instanceof Player defending)
                || attacking.canHarmPlayer(defending);
        return evaluate(new HarmTargetFacts(
                present,
                present && target.isAlive(),
                present && target.isRemoved(),
                present && target.level().hasChunkAt(target.blockPosition()),
                sameDimension,
                present && target.isInvulnerable(),
                creativeOrSpectator,
                allied,
                pvpAllowed
        ));
    }
}
