package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractCelestialEntity
        extends AbstractYaldabaothEncounterEntity {

    public static final int COMBAT_ANIMATION_DURATION = 24;
    private static final String ALLEGIANCE_TAG = "Allegiance";
    private static final String OWNER_TAG = "YaldabaothOwner";
    private static final EntityDataAccessor<Integer> ALLEGIANCE =
            SynchedEntityData.defineId(
                    AbstractCelestialEntity.class,
                    EntityDataSerializers.INT
            );
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(
                    AbstractCelestialEntity.class,
                    EntityDataSerializers.OPTIONAL_UUID
            );

    private boolean ownerRemovalReported;

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
        this.entityData.define(OWNER, Optional.empty());
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

    public final Optional<UUID> getOwnerId() {
        return this.entityData.get(OWNER);
    }

    public final void setOwner(YaldabaothEntity owner) {
        this.entityData.set(
                OWNER,
                owner == null ? Optional.empty() : Optional.of(owner.getUUID())
        );
    }

    public abstract CelestialRole getCelestialRole();

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Optional<UUID> ownerId = this.getOwnerId();
        if (ownerId.isEmpty()) {
            return;
        }
        Entity rawOwner = serverLevel.getEntity(ownerId.get());
        if (rawOwner instanceof YaldabaothEntity owner && owner.isAlive()) {
            CelestialFormation.Offset offset = CelestialFormation.offset(
                    owner.getYRot(),
                    owner.tickCount,
                    this.getCelestialRole()
            );
            Vec3 target = owner.position().add(
                    offset.x(),
                    offset.y(),
                    offset.z()
            );
            this.setDeltaMovement(Vec3.ZERO);
            this.setPos(target);
            this.setYRot(owner.getYRot());
            this.setYHeadRot(owner.getYRot());
            this.yBodyRot = owner.getYRot();
            return;
        }
        if (rawOwner instanceof YaldabaothEntity) {
            this.discard();
        }
    }

    @Override
    public void die(DamageSource source) {
        this.reportDestructionToOwner();
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && reason.shouldDestroy()) {
            this.reportDestructionToOwner();
        }
        super.remove(reason);
    }

    private void reportDestructionToOwner() {
        if (this.ownerRemovalReported) {
            return;
        }
        this.ownerRemovalReported = true;
        if (this.level() instanceof ServerLevel serverLevel) {
            this.getOwnerId()
                    .map(serverLevel::getEntity)
                    .filter(YaldabaothEntity.class::isInstance)
                    .map(YaldabaothEntity.class::cast)
                    .filter(YaldabaothEntity::isAlive)
                    .ifPresent(owner -> owner.onOwnedCelestialKilled(
                            this.getCelestialRole(),
                            this.getUUID()
                    ));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(ALLEGIANCE_TAG, this.getAllegiance().serializedName());
        this.getOwnerId().ifPresent(owner -> tag.putUUID(OWNER_TAG, owner));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAllegiance(
                CelestialAllegiance.fromSerializedName(tag.getString(ALLEGIANCE_TAG))
        );
        if (tag.hasUUID(OWNER_TAG)) {
            this.entityData.set(OWNER, Optional.of(tag.getUUID(OWNER_TAG)));
        } else {
            this.entityData.set(OWNER, Optional.empty());
        }
    }

    @Override
    protected final int combatAnimationDuration() {
        return COMBAT_ANIMATION_DURATION;
    }
}
