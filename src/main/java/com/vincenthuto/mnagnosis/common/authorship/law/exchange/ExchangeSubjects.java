package com.vincenthuto.mnagnosis.common.authorship.law.exchange;

import com.mna.api.spells.targeting.SpellTarget;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredCastContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;

final class ExchangeSubjects {

    private ExchangeSubjects() {
    }

    static Optional<Pair> resolve(AuthoredCastContext context, SpellTarget target) {
        return resolve(context, target, false);
    }

    static Optional<Pair> resolve(
            AuthoredCastContext context,
            SpellTarget target,
            boolean allowProtectedSubjects
    ) {
        if (!target.isLivingEntity()) {
            return Optional.empty();
        }
        LivingEntity first = context.source().getCaster();
        LivingEntity second = target.getLivingEntity();
        if (first == second || !first.isAlive() || !second.isAlive()
                || first.level() != second.level()
                || first.level() != context.spellContext().getLevel()
                || !(first.level() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        if (!allowProtectedSubjects && first.isAlliedTo(second)) {
            return Optional.empty();
        }
        if (!allowProtectedSubjects
                && first instanceof Player actor && second instanceof Player subject
                && !actor.canHarmPlayer(subject)) {
            return Optional.empty();
        }
        return Optional.of(new Pair(level, first, second));
    }

    static Optional<ServerLevel> loadedLevel(ServerPlayer owner, ResourceLocation dimension) {
        ResourceKey<Level> key = ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION, dimension
        );
        return Optional.ofNullable(owner.server.getLevel(key));
    }

    static Optional<Entity> loadedEntity(ServerLevel level, java.util.UUID id) {
        return Optional.ofNullable(level.getEntity(id)).filter(entity -> !entity.isRemoved());
    }

    static boolean isBalanced(ExchangePayload debt, ExchangePayload current) {
        return debt.propertyId().equals(current.propertyId())
                && debt.firstSubject().equals(current.firstSubject())
                && debt.secondSubject().equals(current.secondSubject())
                && debt.after().equals(current.before())
                && debt.before().equals(current.after());
    }

    record Pair(ServerLevel level, LivingEntity first, LivingEntity second) {
    }
}
