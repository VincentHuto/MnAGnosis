package com.vincenthuto.mnagnosis.common.entity;

import com.vincenthuto.mnagnosis.common.spell.gravity.GravityRuptureMath;
import net.minecraft.core.particles.ParticleTypes;
import com.vincenthuto.mnagnosis.common.particle.IneffableParticleEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private static final int PARTICLES_PER_RING = 12;

    private final List<Set<UUID>> waveHits = new ArrayList<>();

    public GravityRuptureEntity(
            EntityType<? extends GravityRuptureEntity> type,
            Level level
    ) {
        super(type, level);
        noPhysics = true;
        for (int wave = 0; wave < GravityRuptureMath.WAVE_COUNT; wave++) {
            waveHits.add(new HashSet<>());
        }
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
        int age = getRuptureAge();
        if (level().isClientSide) {
            spawnClientRipples(age);
            return;
        }
        if (age == 0) {
            playCollapseSounds();
        }
        applyWavefronts(age);
        int nextAge = age + 1;
        entityData.set(RUPTURE_AGE, nextAge);
        if (nextAge > GravityRuptureMath.TOTAL_DURATION_TICKS) {
            discard();
        }
    }

    private void applyWavefronts(int age) {
        float maximumRadius = getMaximumRadius();
        AABB bounds = getBoundingBox().inflate(maximumRadius + 1.0F);
        List<Entity> candidates = level().getEntities(
                this,
                bounds,
                candidate -> canAffect(candidate)
                        && candidate.position().distanceToSqr(position())
                        <= (maximumRadius + 1.0F) * (maximumRadius + 1.0F)
        );
        for (int wave = 0; wave < GravityRuptureMath.WAVE_COUNT; wave++) {
            float currentRadius = GravityRuptureMath.waveRadius(
                    age, wave, maximumRadius
            );
            if (currentRadius < 0.0F) {
                continue;
            }
            float previousRadius = GravityRuptureMath.waveRadius(
                    age - 1, wave, maximumRadius
            );
            if (previousRadius < 0.0F) {
                previousRadius = 0.0F;
            }
            for (Entity candidate : candidates) {
                if (waveHits.get(wave).contains(candidate.getUUID())) {
                    continue;
                }
                double distance = candidate.position().distanceTo(position());
                double bodyAllowance = Math.max(
                        0.35D,
                        Math.max(candidate.getBbWidth(), candidate.getBbHeight())
                                * 0.5D
                );
                if (distance - bodyAllowance > currentRadius
                        || distance + bodyAllowance < previousRadius) {
                    continue;
                }
                waveHits.get(wave).add(candidate.getUUID());
                affect(candidate, wave, (float) distance, maximumRadius);
            }
        }
    }

    private void affect(
            Entity target,
            int wave,
            float distance,
            float maximumRadius
    ) {
        if (target instanceof LivingEntity living) {
            living.hurt(
                    level().damageSources().explosion(this, null),
                    GravityRuptureMath.waveDamage(wave, distance, maximumRadius)
            );
        }
        float strength = GravityRuptureMath.waveKnockback(
                wave, distance, maximumRadius
        );
        Vec3 outward = target.position().subtract(position());
        if (outward.lengthSqr() < 1.0E-8D) {
            double angle = target.getId() * 2.399963229728653D;
            outward = new Vec3(Math.cos(angle), 0.15D, Math.sin(angle));
        }
        outward = outward.normalize();
        Vec3 impulse = outward.scale(strength)
                .add(0.0D, strength * 0.28D, 0.0D);
        target.setDeltaMovement(target.getDeltaMovement().add(impulse));
        target.hasImpulse = true;
        if (target instanceof LivingEntity living) {
            living.hurtMarked = true;
        }
    }

    private static boolean canAffect(Entity candidate) {
        if (candidate.isRemoved()
                || candidate instanceof GravityRuptureEntity
                || candidate instanceof GravityFieldEntity) {
            return false;
        }
        if (candidate instanceof Player player && player.isSpectator()) {
            return false;
        }
        return candidate instanceof LivingEntity
                || candidate instanceof ItemEntity
                || candidate instanceof Projectile;
    }

    private void playCollapseSounds() {
        level().playSound(
                null, getX(), getY(), getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                2.8F, 0.55F
        );
        level().playSound(
                null, getX(), getY(), getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS,
                2.2F, 0.72F
        );
    }

    private void spawnClientRipples(int age) {
        for (int centerParticle = 0; centerParticle < 3; centerParticle++) {
            double angle = age * 0.73D + centerParticle * Math.PI * 2.0D / 3.0D;
            double radius = 0.8D + centerParticle * 0.18D;
            level().addParticle(
                    ParticleTypes.REVERSE_PORTAL,
                    getX() + Math.cos(angle) * radius,
                    getY() + Math.sin(angle * 1.7D) * 0.55D,
                    getZ() + Math.sin(angle) * radius,
                    -Math.cos(angle) * 0.08D,
                    0.0D,
                    -Math.sin(angle) * 0.08D
            );
        }

        for (int wave = 0; wave < GravityRuptureMath.WAVE_COUNT; wave++) {
            int waveStart = wave * GravityRuptureMath.WAVE_INTERVAL_TICKS;
            if (age == waveStart) {
                level().addParticle(
                        ParticleTypes.EXPLOSION_EMITTER,
                        getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D
                );
                level().addParticle(
                        ParticleTypes.SONIC_BOOM,
                        getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D
                );
            }
            float radius = GravityRuptureMath.waveRadius(
                    age, wave, getMaximumRadius()
            );
            if (radius <= 0.0F) {
                continue;
            }
            spawnWaveRings(age, wave, radius);
        }
    }

    private void spawnWaveRings(int age, int wave, float radius) {
        for (int plane = 0; plane < 3; plane++) {
            for (int sample = 0; sample < PARTICLES_PER_RING; sample++) {
                double angle = Math.PI * 2.0D * sample / PARTICLES_PER_RING
                        + age * 0.055D * (wave % 2 == 0 ? 1.0D : -1.0D);
                double cosine = Math.cos(angle);
                double sine = Math.sin(angle);
                Vec3 direction = switch (plane) {
                    case 0 -> new Vec3(cosine, sine, 0.0D);
                    case 1 -> new Vec3(cosine, 0.0D, sine);
                    default -> new Vec3(0.0D, cosine, sine);
                };
                Vec3 point = position().add(direction.scale(radius));
                Vec3 velocity = direction.scale(0.025D);
                IneffableParticleEffects.add(
                        level(),
                        sample + plane + wave,
                        point,
                        velocity
                );
            }
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
