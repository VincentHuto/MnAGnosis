package com.vincenthuto.mnagnosis.common.entity;

import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandTerrain;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandPillarPayload;
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
import java.util.UUID;

public final class LivingLandControllerEntity extends Entity {

    public static final int MAX_CONTROLLERS_PER_OWNER = 2;
    private UUID ownerId;
    private UUID targetId;
    private float radius = 6.0F;
    private int remainingTicks = 160;
    private float magnitude = 1.0F;
    private float speed = 1.0F;
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
                          int durationTicks, float magnitude, float speed) {
        ownerId = owner.getUUID();
        targetId = target.getUUID();
        this.radius = clamp(radius, 4.0F, 12.0F);
        remainingTicks = Math.max(20, Math.min(durationTicks, 600));
        this.magnitude = clamp(magnitude, 0.5F, 3.0F);
        this.speed = clamp(speed, 0.5F, 3.0F);
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
        Entity rawTarget = targetId == null ? null : serverLevel.getEntity(targetId);
        if (owner == null || !(rawTarget instanceof LivingEntity target)
                || !target.isAlive() || owner.isAlliedTo(target)
                || !serverLevel.hasChunkAt(target.blockPosition()) || --remainingTicks <= 0) {
            discard();
            return;
        }
        setPos(target.position());
        if (tickCount % 16 == 1) {
            launchWave(serverLevel, owner, target);
        }
    }

    private void launchWave(ServerLevel level, ServerPlayer owner, LivingEntity target) {
        int capacity = LivingLandStrikeEntity.MAX_ACTIVE_PER_OWNER
                - LivingLandStrikeEntity.activeCount(level, ownerId);
        if (capacity <= 0) return;
        LivingLandTerrain.scan(level, owner, target, Math.round(radius)).ifPresent(scan -> {
            int count = Math.min(pillarsPerWave(magnitude), capacity);
            int length = pillarLength(magnitude);
            for (LivingLandTerrain.SourceCandidate source
                    : scan.sources().subList(0, Math.min(count, scan.sources().size()))) {
                List<BlockPos> sources = new ArrayList<>();
                for (int index = 0; index < length; index++) {
                    sources.add(source.source().relative(
                            source.approach().getOpposite(), index));
                }
                LivingLandPillarPayload.acquire(level, owner, sources, false).ifPresent(payload -> {
                    LivingLandStrikeEntity strike = new LivingLandStrikeEntity(
                            EntityRegistry.LIVING_LAND_STRIKE.get(), level
                    );
                    strike.configure(owner, target, scan.mode(), payload,
                            4.0F + magnitude * 2.0F, 0.35F + speed * 0.15F);
                    if (!level.addFreshEntity(strike)) {
                        if (!payload.settle(
                                level, owner, source.source(), Vec3.atLowerCornerOf(
                                        source.approach().getNormal()))) {
                            payload.emergencySettle(level);
                        }
                    }
                });
            }
        });
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
        targetId = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
        radius = clamp(tag.getFloat("Radius"), 4.0F, 12.0F);
        remainingTicks = Math.max(1, Math.min(tag.getInt("RemainingTicks"), 600));
        magnitude = clamp(tag.getFloat("Magnitude"), 0.5F, 3.0F);
        speed = clamp(tag.getFloat("Speed"), 0.5F, 3.0F);
        createdAt = tag.getLong("CreatedAt");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        if (targetId != null) tag.putUUID("Target", targetId);
        tag.putFloat("Radius", radius);
        tag.putInt("RemainingTicks", remainingTicks);
        tag.putFloat("Magnitude", magnitude);
        tag.putFloat("Speed", speed);
        tag.putLong("CreatedAt", createdAt);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public UUID getOwnerId() { return ownerId; }
    public long getCreatedAt() { return createdAt; }

    private static float clamp(float value, float minimum, float maximum) {
        return Float.isFinite(value) ? Math.max(minimum, Math.min(value, maximum)) : minimum;
    }
}
