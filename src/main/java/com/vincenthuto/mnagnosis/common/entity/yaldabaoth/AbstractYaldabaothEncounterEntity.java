package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class AbstractYaldabaothEncounterEntity
        extends PathfinderMob implements GeoEntity {

    private static final String COMBAT_ANIMATION_TAG = "CombatAnimationTicks";
    private static final EntityDataAccessor<Integer> COMBAT_ANIMATION_TICKS =
            SynchedEntityData.defineId(
                    AbstractYaldabaothEncounterEntity.class,
                    EntityDataSerializers.INT
            );

    private final AnimatableInstanceCache animationCache =
            GeckoLibUtil.createInstanceCache(this);

    protected AbstractYaldabaothEncounterEntity(
            EntityType<? extends PathfinderMob> type,
            Level level
    ) {
        super(type, level);
        this.setNoGravity(true);
        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        // Encounter controllers will own movement and targeting in a later stage.
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(COMBAT_ANIMATION_TICKS, 0);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (!this.level().isClientSide && this.isCombatAnimationActive()) {
            this.entityData.set(
                    COMBAT_ANIMATION_TICKS,
                    CombatAnimationTimer.tick(this.getCombatAnimationTicks())
            );
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean accepted = super.hurt(source, amount);
        if (accepted && !this.level().isClientSide) {
            this.triggerCombatAnimation();
        }
        return accepted;
    }

    public final void triggerCombatAnimation() {
        if (this.level().isClientSide) {
            return;
        }
        this.entityData.set(
                COMBAT_ANIMATION_TICKS,
                CombatAnimationTimer.trigger(this.combatAnimationDuration())
        );
        this.triggerAnim("combat_controller", "combat");
    }

    public final boolean isCombatAnimationActive() {
        return CombatAnimationTimer.isActive(this.getCombatAnimationTicks());
    }

    public final int getCombatAnimationTicks() {
        return this.entityData.get(COMBAT_ANIMATION_TICKS);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(COMBAT_ANIMATION_TAG, this.getCombatAnimationTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(
                COMBAT_ANIMATION_TICKS,
                CombatAnimationTimer.clampLoaded(
                        tag.getInt(COMBAT_ANIMATION_TAG),
                        this.combatAnimationDuration()
                )
        );
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "base_controller",
                this.baseAnimationTransitionTicks(),
                state -> {
                    state.setAnimation(YaldabaothBaseAnimationSelector.select(
                            state.isMoving(),
                            this.idleAnimation(),
                            this.movementAnimation()
                    ));
                    return PlayState.CONTINUE;
                }
        ));
        controllers.add(new AnimationController<>(
                this,
                "combat_controller",
                0,
                state -> PlayState.STOP
        ).triggerableAnim("combat", this.combatAnimation()));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    protected abstract int combatAnimationDuration();

    protected abstract RawAnimation idleAnimation();

    protected RawAnimation movementAnimation() {
        return this.idleAnimation();
    }

    protected int baseAnimationTransitionTicks() {
        return 2;
    }

    protected abstract RawAnimation combatAnimation();
}
