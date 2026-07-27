package com.vincenthuto.mnagnosis.common.entity;

import com.vincenthuto.mnagnosis.common.spell.gravity.GravityRuptureMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class GravityRuptureEntity extends Entity {

    private static final EntityDataAccessor<Float> MAXIMUM_RADIUS =
            SynchedEntityData.defineId(
                    GravityRuptureEntity.class, EntityDataSerializers.FLOAT
            );
    private static final EntityDataAccessor<Integer> FIELD_COUNT =
            SynchedEntityData.defineId(
                    GravityRuptureEntity.class, EntityDataSerializers.INT
            );
    private static final EntityDataAccessor<Integer> RUPTURE_AGE =
            SynchedEntityData.defineId(
                    GravityRuptureEntity.class, EntityDataSerializers.INT
            );

    public GravityRuptureEntity(
            EntityType<? extends GravityRuptureEntity> type,
            Level level
    ) {
        super(type, level);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(MAXIMUM_RADIUS, 10.0F);
        entityData.define(FIELD_COUNT, 2);
        entityData.define(RUPTURE_AGE, 0);
    }

    public void configure(Vec3 position, int fieldCount) {
        setPos(position);
        int safeCount = Math.max(2, fieldCount);
        entityData.set(FIELD_COUNT, safeCount);
        entityData.set(
                MAXIMUM_RADIUS, GravityRuptureMath.maximumRadius(safeCount)
        );
        entityData.set(RUPTURE_AGE, 0);
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        int age = getRuptureAge() + 1;
        entityData.set(RUPTURE_AGE, age);
        if (!level().isClientSide
                && age > GravityRuptureMath.TOTAL_DURATION_TICKS) {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(
                FIELD_COUNT, Math.max(2, tag.getInt("FieldCount"))
        );
        entityData.set(
                MAXIMUM_RADIUS,
                Math.max(1.0F, Math.min(18.0F, tag.getFloat("MaximumRadius")))
        );
        entityData.set(
                RUPTURE_AGE,
                Math.max(0, Math.min(
                        GravityRuptureMath.TOTAL_DURATION_TICKS,
                        tag.getInt("RuptureAge")
                ))
        );
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("FieldCount", getFieldCount());
        tag.putFloat("MaximumRadius", getMaximumRadius());
        tag.putInt("RuptureAge", getRuptureAge());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public int getFieldCount() {
        return entityData.get(FIELD_COUNT);
    }

    public float getMaximumRadius() {
        return entityData.get(MAXIMUM_RADIUS);
    }

    public int getRuptureAge() {
        return entityData.get(RUPTURE_AGE);
    }
}
