package com.vincenthuto.mnagnosis.common.entity;

import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandTerrain;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandPillarPayload;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class LivingLandControllerEntity extends Entity {

    public static final int MAX_CONTROLLERS_PER_OWNER = 2;
    private UUID ownerId;
    private LivingLandTarget target;
    private float radius = 6.0F;
    private int remainingTicks = 160;
    private float magnitude = 1.0F;
    private float speed = 1.0F;
    private boolean projected;
    private int waveCooldown;
    private long createdAt;

    public LivingLandControllerEntity(EntityType<? extends LivingLandControllerEntity> type,
                                      Level level) {
        super(type, level);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
    }

    public void configure(ServerPlayer owner, LivingEntity target, float radius,
                          int durationTicks, float magnitude, float speed,
                          boolean projected) {
        configure(owner, LivingLandTarget.entity(target), radius, durationTicks,
                magnitude, speed, projected);
    }

    public void configure(ServerPlayer owner, LivingLandTarget target, float radius,
                          int durationTicks, float magnitude, float speed,
                          boolean projected) {
        ownerId = owner.getUUID();
        this.target = target;
        this.radius = clamp(radius, 4.0F, 12.0F);
        remainingTicks = Math.max(20, Math.min(durationTicks, 600));
        this.magnitude = clamp(magnitude, 0.5F, 3.0F);
        this.speed = clamp(speed, 0.5F, 3.0F);
        this.projected = projected;
        createdAt = level().getGameTime();
        setPos(target.position());
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }
        Entity rawOwner = ownerId == null ? null : serverLevel.getPlayerByUUID(ownerId);
        ServerPlayer owner = rawOwner instanceof ServerPlayer player ? player : null;
        if (owner == null || target == null || --remainingTicks <= 0) {
            discard();
            return;
        }
        LivingEntity trackedEntity = null;
        if (target.mode() == LivingLandTarget.Mode.ENTITY) {
            trackedEntity = target.resolveEntity(serverLevel).orElse(null);
            if (trackedEntity == null || !trackedEntity.isAlive()
                    || owner.isAlliedTo(trackedEntity)
                    || trackedEntity.isAlliedTo(owner)) {
                target = target.retarget(serverLevel, owner, radius).orElse(null);
                if (target == null) {
                    discard();
                    return;
                }
                trackedEntity = target.resolveEntity(serverLevel).orElse(null);
            }
            if (trackedEntity != null) {
                target = target.track(trackedEntity);
            }
        }
        Vec3 targetPosition = target.position();
        if (!serverLevel.hasChunkAt(BlockPos.containing(targetPosition))) {
            discard();
            return;
        }
        setPos(targetPosition);
        if (waveCooldown-- <= 0) {
            launchWave(serverLevel, owner, target);
            waveCooldown = 15;
        }
    }

    private void launchWave(
            ServerLevel level,
            ServerPlayer owner,
            LivingLandTarget target
    ) {
        int capacity = LivingLandStrikeEntity.MAX_ACTIVE_PER_OWNER
                - LivingLandStrikeEntity.activeCount(level, ownerId);
        if (capacity <= 0) return;
        LivingLandTerrain.scan(
                level, owner, BlockPos.containing(target.position()),
                Math.round(radius)).ifPresent(scan -> {
            int count = Math.min(pillarsPerWave(magnitude), capacity);
            int desiredLength = pillarLength(magnitude);
            int launched = 0;
            for (LivingLandTerrain.SourceCandidate source : scan.sources()) {
                if (launched >= count) {
                    break;
                }
                Optional<LivingLandPillarPayload> acquired = acquirePillar(
                        level, owner, source, desiredLength, projected);
                if (acquired.isEmpty()) {
                    continue;
                }
                LivingLandPillarPayload payload = acquired.get();
                LivingLandStrikeEntity strike = new LivingLandStrikeEntity(
                        EntityRegistry.LIVING_LAND_STRIKE.get(), level
                );
                strike.configure(owner, target, scan.mode(), source.approach(), payload,
                        4.0F + magnitude * 2.0F, 0.35F + speed * 0.15F,
                        remainingTicks, radius);
                if (level.addFreshEntity(strike)) {
                    launched++;
                } else if (!payload.settle(
                        level, owner, source.source(), Vec3.atLowerCornerOf(
                                source.approach().getNormal()))) {
                    payload.emergencySettle(level);
                }
            }
        });
    }

    private static Optional<LivingLandPillarPayload> acquirePillar(
            ServerLevel level,
            ServerPlayer owner,
            LivingLandTerrain.SourceCandidate source,
            int desiredLength,
            boolean projected
    ) {
        for (int length = Math.max(3, Math.min(desiredLength, 5));
             length >= 3; length--) {
            List<BlockPos> sources = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                sources.add(source.source().relative(
                        source.approach().getOpposite(), index));
            }
            Optional<LivingLandPillarPayload> payload =
                    LivingLandPillarPayload.acquire(level, owner, sources, projected);
            if (payload.isPresent()) {
                return payload;
            }
        }
        return Optional.empty();
    }

    public static int pillarsPerWave(float magnitude) {
        return magnitude >= 2.0F ? 2 : 1;
    }

    public static int pillarLength(float magnitude) {
        return Math.max(3, Math.min(5, 3 + (int) Math.floor(magnitude - 1.0F)));
    }

    public static void makeRoomFor(ServerLevel level, UUID ownerId) {
        List<LivingLandControllerEntity> owned = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingLandControllerEntity controller
                    && !controller.isRemoved() && ownerId.equals(controller.ownerId)) {
                owned.add(controller);
            }
        }
        if (owned.size() >= MAX_CONTROLLERS_PER_OWNER) {
            owned.stream().min(Comparator
                    .comparingLong(LivingLandControllerEntity::getCreatedAt)
                    .thenComparingInt(Entity::getId)).ifPresent(Entity::discard);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        if (tag.contains("LivingLandTarget")) {
            target = LivingLandTarget.readNbt(tag.getCompound("LivingLandTarget"));
        } else if (tag.hasUUID("Target")) {
            CompoundTag legacyTarget = new CompoundTag();
            legacyTarget.putString("Mode", LivingLandTarget.Mode.ENTITY.name());
            legacyTarget.putUUID("Entity", tag.getUUID("Target"));
            legacyTarget.putDouble("X", getX());
            legacyTarget.putDouble("Y", getY());
            legacyTarget.putDouble("Z", getZ());
            target = LivingLandTarget.readNbt(legacyTarget);
        }
        radius = clamp(tag.getFloat("Radius"), 4.0F, 12.0F);
        remainingTicks = Math.max(1, Math.min(tag.getInt("RemainingTicks"), 600));
        magnitude = clamp(tag.getFloat("Magnitude"), 0.5F, 3.0F);
        speed = clamp(tag.getFloat("Speed"), 0.5F, 3.0F);
        projected = tag.getBoolean("Projected");
        waveCooldown = Math.max(0, Math.min(tag.getInt("WaveCooldown"), 15));
        createdAt = tag.getLong("CreatedAt");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        if (target != null) {
            tag.put("LivingLandTarget", target.writeNbt());
            target.entityId().ifPresent(id -> tag.putUUID("Target", id));
        }
        tag.putFloat("Radius", radius);
        tag.putInt("RemainingTicks", remainingTicks);
        tag.putFloat("Magnitude", magnitude);
        tag.putFloat("Speed", speed);
        tag.putBoolean("Projected", projected);
        tag.putInt("WaveCooldown", waveCooldown);
        tag.putLong("CreatedAt", createdAt);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public UUID getOwnerId() { return ownerId; }
    public long getCreatedAt() { return createdAt; }
    public boolean isProjected() { return projected; }

    private static float clamp(float value, float minimum, float maximum) {
        return Float.isFinite(value) ? Math.max(minimum, Math.min(value, maximum)) : minimum;
    }
}
