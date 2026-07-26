package com.vincenthuto.mnagnosis.common.entity;

import com.vincenthuto.mnagnosis.common.spell.gravity.GravityFieldMath;
import com.vincenthuto.mnagnosis.common.spell.gravity.GravityPolarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class GravityFieldEntity extends Entity {

    public enum GravityAnchorMode {
        FIXED,
        CASTER,
        TARGET
    }

    public static final int MAX_FIELDS_PER_OWNER = 3;

    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(GravityFieldEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> POLARITY =
            SynchedEntityData.defineId(GravityFieldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REMAINING_TICKS =
            SynchedEntityData.defineId(GravityFieldEntity.class, EntityDataSerializers.INT);

    private UUID ownerId;
    private UUID targetId;
    private GravityAnchorMode anchorMode = GravityAnchorMode.FIXED;
    private float magnitude = 1.0F;
    private float response = 1.0F;
    private long createdAt;

    public GravityFieldEntity(EntityType<? extends GravityFieldEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(RADIUS, 5.0F);
        entityData.define(POLARITY, GravityPolarity.ATTRACT.ordinal());
        entityData.define(REMAINING_TICKS, 160);
    }

    public void configure(
            Entity owner,
            GravityAnchorMode mode,
            @Nullable Entity trackedTarget,
            Vec3 fixedPosition,
            GravityPolarity polarity,
            float radius,
            int durationTicks,
            float magnitude,
            float response
    ) {
        ownerId = owner.getUUID();
        anchorMode = mode;
        targetId = trackedTarget == null ? null : trackedTarget.getUUID();
        setPos(fixedPosition);
        entityData.set(RADIUS, clamp(radius, 3.0F, 12.0F));
        entityData.set(POLARITY, polarity.ordinal());
        entityData.set(REMAINING_TICKS, Math.max(1, Math.min(durationTicks, 600)));
        this.magnitude = clamp(magnitude, 0.5F, 3.0F);
        this.response = clamp(response, 0.5F, 3.0F);
        createdAt = level().getGameTime();
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (level().isClientSide) {
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }
        if (!updateAnchor(serverLevel)) {
            discard();
            return;
        }

        int remaining = getRemainingTicks() - 1;
        entityData.set(REMAINING_TICKS, remaining);
        if (remaining <= 0) {
            discard();
            return;
        }
        applyForces(serverLevel);
    }

    private boolean updateAnchor(ServerLevel level) {
        if (anchorMode == GravityAnchorMode.FIXED) {
            return true;
        }
        UUID anchorId = anchorMode == GravityAnchorMode.CASTER ? ownerId : targetId;
        if (anchorId == null) {
            return false;
        }
        Entity anchor = level.getEntity(anchorId);
        if (anchor == null || anchor.isRemoved()) {
            return false;
        }
        setPos(anchor.position());
        return true;
    }

    private void applyForces(ServerLevel level) {
        float radius = getRadius();
        AABB bounds = getBoundingBox().inflate(radius);
        Entity owner = ownerId == null ? null : level.getEntity(ownerId);
        List<Entity> targets = level.getEntities(
                this,
                bounds,
                candidate -> canAffect(owner, candidate)
                        && candidate.position().distanceToSqr(position()) < radius * radius
        );
        for (Entity target : targets) {
            Vec3 acceleration = GravityFieldMath.acceleration(
                    target.position().subtract(position()),
                    radius,
                    magnitude,
                    response,
                    getPolarity(),
                    target.getDeltaMovement()
            );
            if (acceleration.lengthSqr() <= 1.0E-12D) {
                continue;
            }
            target.setDeltaMovement(GravityFieldMath.clampVelocity(
                    target.getDeltaMovement().add(acceleration)
            ));
            target.hasImpulse = true;
            if (target instanceof LivingEntity living) {
                living.hurtMarked = true;
            }
        }
    }

    private boolean canAffect(@Nullable Entity owner, Entity candidate) {
        if (candidate.isRemoved()
                || candidate.noPhysics
                || candidate.getUUID().equals(ownerId)
                || !(candidate instanceof LivingEntity
                || candidate instanceof ItemEntity
                || candidate instanceof Projectile)) {
            return false;
        }
        if (candidate instanceof Player player && player.isSpectator()) {
            return false;
        }
        if (owner != null
                && (owner.isAlliedTo(candidate) || candidate.isAlliedTo(owner))) {
            return false;
        }
        if (candidate instanceof TamableAnimal tameable
                && ownerId != null
                && ownerId.equals(tameable.getOwnerUUID())) {
            return false;
        }
        return !(candidate instanceof Projectile projectile
                && projectile.getOwner() != null
                && projectile.getOwner().getUUID().equals(ownerId));
    }

    public static void makeRoomFor(ServerLevel level, UUID ownerId) {
        List<GravityFieldEntity> owned = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof GravityFieldEntity field
                    && ownerId.equals(field.ownerId)
                    && !field.isRemoved()) {
                owned.add(field);
            }
        }
        if (owned.size() < MAX_FIELDS_PER_OWNER) {
            return;
        }
        owned.stream()
                .min(Comparator.comparingLong(GravityFieldEntity::getCreatedAt)
                        .thenComparingInt(Entity::getId))
                .ifPresent(Entity::discard);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        targetId = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
        int mode = tag.getInt("AnchorMode");
        anchorMode = mode >= 0 && mode < GravityAnchorMode.values().length
                ? GravityAnchorMode.values()[mode] : GravityAnchorMode.FIXED;
        entityData.set(RADIUS, clamp(tag.getFloat("Radius"), 3.0F, 12.0F));
        int polarity = tag.getInt("Polarity");
        entityData.set(POLARITY,
                polarity == GravityPolarity.REPEL.ordinal()
                        ? GravityPolarity.REPEL.ordinal()
                        : GravityPolarity.ATTRACT.ordinal());
        entityData.set(REMAINING_TICKS,
                Math.max(1, Math.min(tag.getInt("RemainingTicks"), 600)));
        magnitude = clamp(tag.getFloat("Magnitude"), 0.5F, 3.0F);
        response = clamp(tag.getFloat("Response"), 0.5F, 3.0F);
        createdAt = tag.getLong("CreatedAt");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        if (targetId != null) {
            tag.putUUID("Target", targetId);
        }
        tag.putInt("AnchorMode", anchorMode.ordinal());
        tag.putFloat("Radius", getRadius());
        tag.putInt("Polarity", getPolarity().ordinal());
        tag.putInt("RemainingTicks", getRemainingTicks());
        tag.putFloat("Magnitude", magnitude);
        tag.putFloat("Response", response);
        tag.putLong("CreatedAt", createdAt);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public GravityAnchorMode getAnchorMode() {
        return anchorMode;
    }

    public GravityPolarity getPolarity() {
        int ordinal = entityData.get(POLARITY);
        return ordinal == GravityPolarity.REPEL.ordinal()
                ? GravityPolarity.REPEL : GravityPolarity.ATTRACT;
    }

    public float getRadius() {
        return entityData.get(RADIUS);
    }

    public int getRemainingTicks() {
        return entityData.get(REMAINING_TICKS);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    private static float clamp(float value, float minimum, float maximum) {
        if (!Float.isFinite(value)) {
            return minimum;
        }
        return Math.max(minimum, Math.min(value, maximum));
    }
}
