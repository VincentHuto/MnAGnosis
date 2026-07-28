package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public abstract class AbstractCelestialEntity
        extends AbstractYaldabaothEncounterEntity {

    public static final int COMBAT_ANIMATION_DURATION = 24;
    private static final String ALLEGIANCE_TAG = "Allegiance";
    private static final EntityDataAccessor<Integer> ALLEGIANCE =
            SynchedEntityData.defineId(
                    AbstractCelestialEntity.class,
                    EntityDataSerializers.INT
            );

    protected AbstractCelestialEntity(
            EntityType<? extends AbstractCelestialEntity> type,
            Level level
    ) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ALLEGIANCE, CelestialAllegiance.HOSTILE.ordinal());
    }

    public final CelestialAllegiance getAllegiance() {
        int ordinal = this.entityData.get(ALLEGIANCE);
        CelestialAllegiance[] values = CelestialAllegiance.values();
        return ordinal >= 0 && ordinal < values.length
                ? values[ordinal]
                : CelestialAllegiance.HOSTILE;
    }

    public final void setAllegiance(CelestialAllegiance allegiance) {
        this.entityData.set(
                ALLEGIANCE,
                (allegiance == null ? CelestialAllegiance.HOSTILE : allegiance).ordinal()
        );
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(ALLEGIANCE_TAG, this.getAllegiance().serializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAllegiance(
                CelestialAllegiance.fromSerializedName(tag.getString(ALLEGIANCE_TAG))
        );
    }

    @Override
    protected final int combatAnimationDuration() {
        return COMBAT_ANIMATION_DURATION;
    }
}
