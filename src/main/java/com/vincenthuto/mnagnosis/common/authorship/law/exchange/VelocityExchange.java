package com.vincenthuto.mnagnosis.common.authorship.law.exchange;

import com.mna.api.spells.targeting.SpellTarget;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredCastContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class VelocityExchange implements ExchangeProperty {

    @Override
    public ResourceLocation id() {
        return ExchangeLawHandler.VELOCITY;
    }

    @Override
    public boolean supports(AuthoredCastContext context) {
        return true;
    }

    @Override
    public Optional<ExchangePayload> exchange(
            AuthoredCastContext context,
            SpellTarget target
    ) {
        Optional<ExchangeSubjects.Pair> resolved = ExchangeSubjects.resolve(context, target);
        if (resolved.isEmpty()) {
            return Optional.empty();
        }
        ExchangeSubjects.Pair subjects = resolved.orElseThrow();
        Vec3 first = subjects.first().getDeltaMovement();
        Vec3 second = subjects.second().getDeltaMovement();
        CompoundTag before = snapshot(first, second);
        subjects.first().setDeltaMovement(second);
        subjects.second().setDeltaMovement(first);
        subjects.first().hurtMarked = true;
        subjects.second().hurtMarked = true;
        CompoundTag after = snapshot(second, first);
        float magnitude = Math.max(1.0F,
                (float) Math.min(4.0D, first.subtract(second).length()));
        return Optional.of(new ExchangePayload(
                ExchangePayload.VERSION,
                subjects.first().getUUID(),
                subjects.second().getUUID(),
                subjects.level().dimension().location(),
                id(),
                before,
                after,
                magnitude
        ));
    }

    @Override
    public boolean isBalancedClosure(ExchangePayload debt, ExchangePayload current) {
        return ExchangeSubjects.isBalanced(debt, current);
    }

    @Override
    public void vent(ServerPlayer owner, ExchangePayload debt) {
        ExchangeSubjects.loadedLevel(owner, debt.dimension()).ifPresent(level -> {
            Entity first = ExchangeSubjects.loadedEntity(level, debt.firstSubject())
                    .orElse(owner.getUUID().equals(debt.firstSubject()) ? owner : null);
            Entity second = ExchangeSubjects.loadedEntity(level, debt.secondSubject())
                    .orElse(null);
            CompoundTag before = debt.before();
            if (first != null) {
                first.setDeltaMovement(load(before.getCompound("first")));
                first.hurtMarked = true;
            }
            if (second != null) {
                second.setDeltaMovement(load(before.getCompound("second")));
                second.hurtMarked = true;
            }
        });
    }

    private static CompoundTag snapshot(Vec3 first, Vec3 second) {
        CompoundTag tag = new CompoundTag();
        tag.put("first", save(first));
        tag.put("second", save(second));
        return tag;
    }

    private static CompoundTag save(Vec3 value) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("x", value.x);
        tag.putDouble("y", value.y);
        tag.putDouble("z", value.z);
        return tag;
    }

    private static Vec3 load(CompoundTag tag) {
        return new Vec3(
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z")
        );
    }
}
