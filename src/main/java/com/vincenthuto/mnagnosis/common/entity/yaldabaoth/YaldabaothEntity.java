package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class YaldabaothEntity extends AbstractYaldabaothEncounterEntity {

    public static final int COMBAT_ANIMATION_DURATION = 36;
    private static final String SUN_ID_TAG = "SunCompanion";
    private static final String MOON_ID_TAG = "MoonCompanion";
    private static final String SUN_RESPAWN_TAG = "SunRespawnTicks";
    private static final String MOON_RESPAWN_TAG = "MoonRespawnTicks";
    private static final double RECOVERY_RADIUS = 16.0D;
    private static final int BASE_ANIMATION_TRANSITION_TICKS = 10;
    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("animation.yaldabaoth.idle");
    private static final RawAnimation MOVEMENT =
            RawAnimation.begin().thenLoop("animation.yaldabaoth.move");
    private static final RawAnimation ROAR_SWEEP =
            RawAnimation.begin().thenPlay("animation.yaldabaoth.combat.roar_sweep");

    private UUID sunId;
    private UUID moonId;
    private int sunRespawnTicks;
    private int moonRespawnTicks;
    private boolean cleaningUpCompanions;

    public YaldabaothEntity(
            EntityType<? extends YaldabaothEntity> type,
            Level level
    ) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel && this.isAlive()) {
            this.maintainCompanion(serverLevel, CelestialRole.SUN);
            this.maintainCompanion(serverLevel, CelestialRole.MOON);
        }
    }

    public Optional<UUID> getCompanionId(CelestialRole role) {
        return Optional.ofNullable(role == CelestialRole.SUN
                ? this.sunId
                : this.moonId);
    }

    public int getCompanionRespawnTicks(CelestialRole role) {
        return role == CelestialRole.SUN
                ? this.sunRespawnTicks
                : this.moonRespawnTicks;
    }

    void onOwnedCelestialKilled(CelestialRole role, UUID celestialId) {
        if (!this.isAlive()
                || this.getCompanionId(role)
                        .filter(celestialId::equals)
                        .isEmpty()) {
            return;
        }
        this.setCompanionId(role, null);
        this.setCompanionRespawnTicks(
                role,
                CelestialFormation.RESPAWN_TICKS
        );
    }

    private void maintainCompanion(ServerLevel level, CelestialRole role) {
        CompanionResolution resolution = this.resolveCompanion(level, role);
        AbstractCelestialEntity companion = resolution.companion();
        if (companion != null) {
            this.setCompanionId(role, companion.getUUID());
            this.setCompanionRespawnTicks(role, 0);
            return;
        }

        if (resolution.awaitingStoredCompanion()) {
            return;
        }

        if (this.getCompanionId(role).isPresent()) {
            this.setCompanionId(role, null);
            this.setCompanionRespawnTicks(
                    role,
                    CelestialFormation.RESPAWN_TICKS
            );
            return;
        }

        int remaining = this.getCompanionRespawnTicks(role);
        if (remaining > 0) {
            remaining = CelestialFormation.tickRespawn(remaining);
            this.setCompanionRespawnTicks(role, remaining);
            if (!CelestialFormation.isRespawnReady(remaining)) {
                return;
            }
        }
        this.spawnCompanion(level, role);
    }

    private CompanionResolution resolveCompanion(
            ServerLevel level,
            CelestialRole role
    ) {
        Optional<UUID> storedId = this.getCompanionId(role);
        Entity byId = storedId
                .map(level::getEntity)
                .orElse(null);
        AbstractCelestialEntity storedCompanion =
                byId instanceof AbstractCelestialEntity celestial
                && celestial.getCelestialRole() == role
                && celestial.getOwnerId()
                        .filter(this.getUUID()::equals)
                        .isPresent()
                && celestial.isAlive()
                        ? celestial
                        : null;
        List<AbstractCelestialEntity> matches = new ArrayList<>(
                level.getEntitiesOfClass(
                AbstractCelestialEntity.class,
                this.getBoundingBox().inflate(RECOVERY_RADIUS),
                celestial -> celestial.isAlive()
                        && celestial.getCelestialRole() == role
                        && celestial.getOwnerId()
                                .filter(this.getUUID()::equals)
                                .isPresent()
                )
        );
        if (storedCompanion != null
                && matches.stream().noneMatch(entity ->
                        entity.getUUID().equals(storedCompanion.getUUID()))) {
            matches.add(storedCompanion);
        }

        if (storedId.isPresent() && byId == null) {
            matches.forEach(Entity::discard);
            return new CompanionResolution(null, true);
        }

        AbstractCelestialEntity canonical = storedCompanion != null
                ? storedCompanion
                : matches.stream()
                        .min(Comparator.comparing(entity ->
                                entity.getUUID().toString()))
                        .orElse(null);
        if (canonical != null) {
            matches.stream()
                    .filter(entity -> entity != canonical)
                    .forEach(Entity::discard);
        }
        return new CompanionResolution(canonical, false);
    }

    private void spawnCompanion(ServerLevel level, CelestialRole role) {
        AbstractCelestialEntity celestial = role == CelestialRole.SUN
                ? EntityRegistry.YALDABAOTH_SUN.get().create(level)
                : EntityRegistry.YALDABAOTH_MOON.get().create(level);
        if (celestial == null) {
            return;
        }
        celestial.setOwner(this);
        CelestialFormation.Offset offset = CelestialFormation.offset(
                this.getYRot(),
                this.tickCount,
                role
        );
        celestial.moveTo(
                this.getX() + offset.x(),
                this.getY() + offset.y(),
                this.getZ() + offset.z(),
                this.getYRot(),
                0.0F
        );
        if (level.addFreshEntity(celestial)) {
            this.setCompanionId(role, celestial.getUUID());
            this.setCompanionRespawnTicks(role, 0);
        }
    }

    private void setCompanionId(CelestialRole role, UUID id) {
        if (role == CelestialRole.SUN) {
            this.sunId = id;
        } else {
            this.moonId = id;
        }
    }

    private void setCompanionRespawnTicks(CelestialRole role, int ticks) {
        int clamped = Math.max(0, Math.min(
                CelestialFormation.RESPAWN_TICKS,
                ticks
        ));
        if (role == CelestialRole.SUN) {
            this.sunRespawnTicks = clamped;
        } else {
            this.moonRespawnTicks = clamped;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.sunId != null) {
            tag.putUUID(SUN_ID_TAG, this.sunId);
        }
        if (this.moonId != null) {
            tag.putUUID(MOON_ID_TAG, this.moonId);
        }
        tag.putInt(SUN_RESPAWN_TAG, this.sunRespawnTicks);
        tag.putInt(MOON_RESPAWN_TAG, this.moonRespawnTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.sunId = tag.hasUUID(SUN_ID_TAG) ? tag.getUUID(SUN_ID_TAG) : null;
        this.moonId =
                tag.hasUUID(MOON_ID_TAG) ? tag.getUUID(MOON_ID_TAG) : null;
        this.setCompanionRespawnTicks(
                CelestialRole.SUN,
                tag.getInt(SUN_RESPAWN_TAG)
        );
        this.setCompanionRespawnTicks(
                CelestialRole.MOON,
                tag.getInt(MOON_RESPAWN_TAG)
        );
    }

    @Override
    public void die(DamageSource source) {
        this.removeOwnedCompanions();
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && reason.shouldDestroy()) {
            this.removeOwnedCompanions();
        }
        super.remove(reason);
    }

    private void removeOwnedCompanions() {
        if (this.cleaningUpCompanions
                || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.cleaningUpCompanions = true;
        serverLevel.getEntitiesOfClass(
                AbstractCelestialEntity.class,
                this.getBoundingBox().inflate(RECOVERY_RADIUS),
                celestial -> celestial.getOwnerId()
                        .filter(this.getUUID()::equals)
                        .isPresent()
        ).forEach(Entity::discard);
        for (CelestialRole role : CelestialRole.values()) {
            this.setCompanionId(role, null);
            this.setCompanionRespawnTicks(role, 0);
        }
    }

    @Override
    protected int combatAnimationDuration() {
        return COMBAT_ANIMATION_DURATION;
    }

    @Override
    protected RawAnimation idleAnimation() {
        return IDLE;
    }

    @Override
    protected RawAnimation movementAnimation() {
        return MOVEMENT;
    }

    @Override
    protected int baseAnimationTransitionTicks() {
        return BASE_ANIMATION_TRANSITION_TICKS;
    }

    @Override
    protected RawAnimation combatAnimation() {
        return ROAR_SWEEP;
    }

    private record CompanionResolution(
            AbstractCelestialEntity companion,
            boolean awaitingStoredCompanion
    ) {
    }
}
