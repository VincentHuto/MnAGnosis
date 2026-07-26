package com.vincenthuto.mnagnosis.common.authorship.law.exchange;

import com.mna.api.spells.targeting.SpellTarget;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredCastContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class PositionExchange implements ExchangeProperty {

    @Override
    public ResourceLocation id() {
        return ExchangeLawHandler.POSITION;
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
        LivingEntity first = subjects.first();
        LivingEntity second = subjects.second();
        Vec3 firstPosition = first.position();
        Vec3 secondPosition = second.position();
        AABB firstDestination = first.getBoundingBox().move(
                secondPosition.subtract(firstPosition)
        );
        AABB secondDestination = second.getBoundingBox().move(
                firstPosition.subtract(secondPosition)
        );
        if (hasCollision(subjects, first, second, firstDestination)
                || hasCollision(subjects, second, first, secondDestination)) {
            return Optional.empty();
        }

        CompoundTag before = snapshot(first, second);
        float firstYaw = first.getYRot();
        float firstPitch = first.getXRot();
        float secondYaw = second.getYRot();
        float secondPitch = second.getXRot();
        first.moveTo(secondPosition.x, secondPosition.y, secondPosition.z,
                secondYaw, secondPitch);
        second.moveTo(firstPosition.x, firstPosition.y, firstPosition.z,
                firstYaw, firstPitch);
        CompoundTag after = snapshot(first, second);
        float magnitude = Math.max(
                1.0F, (float) Math.min(4.0D, firstPosition.distanceTo(secondPosition) / 16.0D)
        );
        return Optional.of(new ExchangePayload(
                ExchangePayload.VERSION,
                first.getUUID(),
                second.getUUID(),
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
                restore(first, before.getCompound("first"));
            }
            if (second != null) {
                restore(second, before.getCompound("second"));
            }
        });
    }

    private static boolean hasCollision(
            ExchangeSubjects.Pair subjects,
            Entity moving,
            Entity counterpart,
            AABB destination
    ) {
        if (subjects.level().getBlockCollisions(moving, destination).iterator().hasNext()) {
            return true;
        }
        return !subjects.level().getEntities(
                moving,
                destination,
                entity -> entity != counterpart
                        && entity instanceof LivingEntity
                        && entity.isAlive()
        ).isEmpty();
    }

    static CompoundTag snapshot(Entity first, Entity second) {
        CompoundTag tag = new CompoundTag();
        tag.put("first", snapshot(first));
        tag.put("second", snapshot(second));
        return tag;
    }

    private static CompoundTag snapshot(Entity entity) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("x", entity.getX());
        tag.putDouble("y", entity.getY());
        tag.putDouble("z", entity.getZ());
        tag.putFloat("yaw", entity.getYRot());
        tag.putFloat("pitch", entity.getXRot());
        return tag;
    }

    private static void restore(Entity entity, CompoundTag tag) {
        entity.moveTo(
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z"),
                tag.getFloat("yaw"),
                tag.getFloat("pitch")
        );
    }
}
