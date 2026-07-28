package com.vincenthuto.mnagnosis.common.spell.livingland;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public final class LivingLandTarget {

    public enum Mode {
        ENTITY,
        FIXED
    }

    private final Mode mode;
    private final UUID entityId;
    private final Vec3 position;

    private LivingLandTarget(Mode mode, UUID entityId, Vec3 position) {
        this.mode = mode;
        this.entityId = entityId;
        this.position = finite(position) ? position : Vec3.ZERO;
    }

    public static LivingLandTarget entity(LivingEntity entity) {
        return new LivingLandTarget(
                Mode.ENTITY, entity.getUUID(), entity.getBoundingBox().getCenter());
    }

    public static LivingLandTarget fixed(Vec3 position) {
        return new LivingLandTarget(Mode.FIXED, null, position);
    }

    public Mode mode() {
        return mode;
    }

    public Optional<UUID> entityId() {
        return Optional.ofNullable(entityId);
    }

    public Vec3 position() {
        return position;
    }

    public Optional<LivingEntity> resolveEntity(ServerLevel level) {
        if (mode != Mode.ENTITY || entityId == null) {
            return Optional.empty();
        }
        return level.getEntity(entityId) instanceof LivingEntity living
                ? Optional.of(living) : Optional.empty();
    }

    public LivingLandTarget track(LivingEntity entity) {
        return mode == Mode.ENTITY && entityId != null
                && entityId.equals(entity.getUUID())
                ? entity(entity) : this;
    }

    public Optional<LivingLandTarget> retarget(
            ServerLevel level,
            ServerPlayer owner,
            double radius
    ) {
        double boundedRadius = Double.isFinite(radius)
                ? Math.max(0.0D, Math.min(radius, 12.0D)) : 0.0D;
        AABB search = new AABB(position, position).inflate(boundedRadius);
        return level.getEntitiesOfClass(
                        LivingEntity.class,
                        search,
                        candidate -> candidate.isAlive()
                                && candidate != owner
                                && !owner.isAlliedTo(candidate)
                                && !candidate.isAlliedTo(owner)
                                && candidate.getBoundingBox().getCenter()
                                .distanceToSqr(position)
                                <= boundedRadius * boundedRadius)
                .stream()
                .min(Comparator
                        .comparingDouble((LivingEntity candidate) ->
                                candidate.getBoundingBox().getCenter()
                                        .distanceToSqr(position))
                        .thenComparing(LivingEntity::getUUID))
                .map(LivingLandTarget::entity);
    }

    public CompoundTag writeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Mode", mode.name());
        if (entityId != null) {
            tag.putUUID("Entity", entityId);
        }
        tag.putDouble("X", position.x);
        tag.putDouble("Y", position.y);
        tag.putDouble("Z", position.z);
        return tag;
    }

    public static LivingLandTarget readNbt(CompoundTag tag) {
        Mode mode;
        try {
            mode = Mode.valueOf(tag.getString("Mode"));
        } catch (IllegalArgumentException ignored) {
            mode = tag.hasUUID("Entity") ? Mode.ENTITY : Mode.FIXED;
        }
        UUID entityId = tag.hasUUID("Entity") ? tag.getUUID("Entity") : null;
        Vec3 position = new Vec3(
                tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
        return new LivingLandTarget(
                mode == Mode.ENTITY && entityId == null ? Mode.FIXED : mode,
                entityId,
                position);
    }

    private static boolean finite(Vec3 position) {
        return position != null
                && Double.isFinite(position.x)
                && Double.isFinite(position.y)
                && Double.isFinite(position.z);
    }
}
