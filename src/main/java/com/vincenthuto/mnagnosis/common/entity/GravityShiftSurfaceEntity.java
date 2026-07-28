package com.vincenthuto.mnagnosis.common.entity;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravitySurfaceMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class GravityShiftSurfaceEntity extends Entity {

    public static final int MAX_SURFACES_PER_OWNER = 3;
    public static final double CONTACT_DEPTH = 0.25D;

    private static final EntityDataAccessor<BlockPos> ANCHOR =
            SynchedEntityData.defineId(GravityShiftSurfaceEntity.class,
                    EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> FACE =
            SynchedEntityData.defineId(GravityShiftSurfaceEntity.class,
                    EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(GravityShiftSurfaceEntity.class,
                    EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> REMAINING =
            SynchedEntityData.defineId(GravityShiftSurfaceEntity.class,
                    EntityDataSerializers.INT);

    private UUID ownerId;
    private long createdAt;

    public GravityShiftSurfaceEntity(
            EntityType<? extends GravityShiftSurfaceEntity> type,
            Level level
    ) {
        super(type, level);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ANCHOR, BlockPos.ZERO);
        entityData.define(FACE, Direction.UP.ordinal());
        entityData.define(RADIUS, 5.0F);
        entityData.define(REMAINING, 160);
    }

    public void configure(
            UUID ownerId,
            BlockPos anchor,
            Direction face,
            float radius,
            int durationTicks
    ) {
        this.ownerId = ownerId;
        entityData.set(ANCHOR, anchor.immutable());
        entityData.set(FACE, face.ordinal());
        entityData.set(RADIUS, Math.max(3.0F, Math.min(radius, 12.0F)));
        entityData.set(REMAINING, Math.max(1, Math.min(durationTicks, 600)));
        createdAt = level().getGameTime();
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal()).scale(0.501D);
        setPos(Vec3.atCenterOf(anchor).add(normal));
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (level().isClientSide) {
            return;
        }
        if (!anchorIsValid()) {
            discard();
            return;
        }
        int remaining = getRemainingTicks() - 1;
        entityData.set(REMAINING, remaining);
        if (remaining <= 0) {
            discard();
        }
    }

    public Set<BlockPos> activeFaces() {
        return GravitySurfaceMath.collectPlanar(
                getAnchor(), getFace(), getRadius(), this::faceIsValid
        );
    }

    public boolean touches(LivingEntity entity) {
        AABB entityBounds = entity.getBoundingBox();
        for (BlockPos position : activeFaces()) {
            if (faceContactBounds(position, getFace()).intersects(entityBounds)) {
                return true;
            }
        }
        return false;
    }

    public GravityDirection gravityDirection() {
        return GravitySurfaceMath.gravityForFace(getFace());
    }

    private boolean anchorIsValid() {
        return faceIsValid(getAnchor());
    }

    private boolean faceIsValid(BlockPos position) {
        if (level().getBlockState(position).getCollisionShape(level(), position)
                .isEmpty()) {
            return false;
        }
        BlockPos outside = position.relative(getFace());
        return !level().getBlockState(outside)
                .isFaceSturdy(level(), outside, getFace().getOpposite());
    }

    private static AABB faceContactBounds(BlockPos position, Direction face) {
        double minX = position.getX();
        double minY = position.getY();
        double minZ = position.getZ();
        double maxX = minX + 1.0D;
        double maxY = minY + 1.0D;
        double maxZ = minZ + 1.0D;
        return switch (face) {
            case DOWN -> new AABB(minX, minY - CONTACT_DEPTH, minZ,
                    maxX, minY + CONTACT_DEPTH, maxZ);
            case UP -> new AABB(minX, maxY - CONTACT_DEPTH, minZ,
                    maxX, maxY + CONTACT_DEPTH, maxZ);
            case NORTH -> new AABB(minX, minY, minZ - CONTACT_DEPTH,
                    maxX, maxY, minZ + CONTACT_DEPTH);
            case SOUTH -> new AABB(minX, minY, maxZ - CONTACT_DEPTH,
                    maxX, maxY, maxZ + CONTACT_DEPTH);
            case WEST -> new AABB(minX - CONTACT_DEPTH, minY, minZ,
                    minX + CONTACT_DEPTH, maxY, maxZ);
            case EAST -> new AABB(maxX - CONTACT_DEPTH, minY, minZ,
                    maxX + CONTACT_DEPTH, maxY, maxZ);
        };
    }

    public static void makeRoomFor(ServerLevel level, UUID ownerId) {
        List<GravityShiftSurfaceEntity> owned = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof GravityShiftSurfaceEntity surface
                    && ownerId.equals(surface.ownerId) && !surface.isRemoved()) {
                owned.add(surface);
            }
        }
        if (owned.size() < MAX_SURFACES_PER_OWNER) {
            return;
        }
        owned.stream().min(Comparator
                        .comparingLong(GravityShiftSurfaceEntity::getCreatedAt)
                        .thenComparingInt(Entity::getId))
                .ifPresent(Entity::discard);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        entityData.set(ANCHOR, BlockPos.of(tag.getLong("Anchor")));
        entityData.set(FACE, validFace(tag.getInt("Face")).ordinal());
        entityData.set(RADIUS, Math.max(3.0F,
                Math.min(tag.getFloat("Radius"), 12.0F)));
        entityData.set(REMAINING, Math.max(1,
                Math.min(tag.getInt("Remaining"), 600)));
        createdAt = tag.getLong("CreatedAt");
        Vec3 normal = Vec3.atLowerCornerOf(getFace().getNormal()).scale(0.501D);
        setPos(Vec3.atCenterOf(getAnchor()).add(normal));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        tag.putLong("Anchor", getAnchor().asLong());
        tag.putInt("Face", getFace().ordinal());
        tag.putFloat("Radius", getRadius());
        tag.putInt("Remaining", getRemainingTicks());
        tag.putLong("CreatedAt", createdAt);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private static Direction validFace(int ordinal) {
        Direction[] values = Direction.values();
        return ordinal >= 0 && ordinal < values.length
                ? values[ordinal] : Direction.UP;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public BlockPos getAnchor() {
        return entityData.get(ANCHOR);
    }

    public Direction getFace() {
        return validFace(entityData.get(FACE));
    }

    public float getRadius() {
        return entityData.get(RADIUS);
    }

    public int getRemainingTicks() {
        return entityData.get(REMAINING);
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
