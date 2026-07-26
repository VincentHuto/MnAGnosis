package com.vincenthuto.mnagnosis.common.entity;

import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandMode;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandPillarPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.UUID;

public final class LivingLandStrikeEntity extends Entity {

    public static final int MAX_ACTIVE_PER_OWNER = 4;
    private static final EntityDataAccessor<Integer> MODE =
            SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LENGTH =
            SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PROJECTED =
            SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STATE_0 =
            SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE_1 =
            SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE_2 =
            SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE_3 =
            SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE_4 =
            SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT);
    private static final List<EntityDataAccessor<Integer>> STATES =
            List.of(STATE_0, STATE_1, STATE_2, STATE_3, STATE_4);

    private UUID ownerId;
    private UUID targetId;
    private LivingLandPillarPayload payload;
    private float damage = 6.0F;
    private float speed = 0.8F;
    private int remainingTicks = 80;

    public LivingLandStrikeEntity(EntityType<? extends LivingLandStrikeEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(MODE, LivingLandMode.FLOOR_TEETH.ordinal());
        entityData.define(LENGTH, 1);
        entityData.define(PROJECTED, false);
        int stone = Block.getId(Blocks.STONE.defaultBlockState());
        for (EntityDataAccessor<Integer> accessor : STATES) {
            entityData.define(accessor, stone);
        }
    }

    public void configure(ServerPlayer owner, LivingEntity target, LivingLandMode mode,
                          LivingLandPillarPayload payload, float damage, float speed) {
        ownerId = owner.getUUID();
        targetId = target.getUUID();
        this.payload = payload;
        this.damage = clamp(damage, 1.0F, 40.0F);
        this.speed = clamp(speed, 0.25F, 2.0F);
        entityData.set(MODE, mode.ordinal());
        syncPayload();
        setPos(Vec3.atCenterOf(payload.entries().get(0).source()));
        Vec3 direction = target.getBoundingBox().getCenter().subtract(position()).normalize();
        setDeltaMovement(direction.scale(this.speed));
    }

    private void syncPayload() {
        if (payload == null || payload.entries().isEmpty()) {
            return;
        }
        int length = Math.min(payload.entries().size(), 5);
        entityData.set(LENGTH, length);
        entityData.set(PROJECTED, payload.projected());
        for (int index = 0; index < length; index++) {
            entityData.set(STATES.get(index),
                    Block.getId(payload.entries().get(index).state()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnParticles();
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }
        Entity rawOwner = ownerId == null ? null : serverLevel.getPlayerByUUID(ownerId);
        ServerPlayer owner = rawOwner instanceof ServerPlayer player ? player : null;
        Entity rawTarget = targetId == null ? null : serverLevel.getEntity(targetId);
        if (owner == null || !(rawTarget instanceof LivingEntity target)
                || !target.isAlive() || owner.isAlliedTo(target)) {
            finish(serverLevel, owner, blockPosition());
            return;
        }
        if (--remainingTicks <= 0) {
            finish(serverLevel, owner, blockPosition());
            return;
        }
        Vec3 oldPosition = position();
        Vec3 desired = target.getBoundingBox().getCenter().subtract(oldPosition);
        if (desired.lengthSqr() > 1.0E-6D) {
            Vec3 velocity = getDeltaMovement().scale(0.72D)
                    .add(desired.normalize().scale(speed * 0.28D));
            if (velocity.length() > speed) {
                velocity = velocity.normalize().scale(speed);
            }
            setDeltaMovement(velocity);
        }
        Vec3 next = oldPosition.add(getDeltaMovement());
        if (!serverLevel.hasChunkAt(BlockPos.containing(next))) {
            finish(serverLevel, owner, blockPosition());
            return;
        }
        move(MoverType.SELF, getDeltaMovement());
        if (intersectsTarget(oldPosition, position(), target)) {
            target.hurt(serverLevel.damageSources().indirectMagic(this, owner), damage);
            applyKnockback(target);
            finish(serverLevel, owner, target.blockPosition());
        }
    }

    private boolean intersectsTarget(Vec3 oldPosition, Vec3 newPosition, LivingEntity target) {
        Vec3 axis = flightAxis();
        double center = (getPayloadLength() - 1) * 0.5D;
        for (int index = 0; index < getPayloadLength(); index++) {
            Vec3 offset = axis.scale(index - center);
            AABB swept = new AABB(oldPosition.add(offset), newPosition.add(offset))
                    .inflate(0.4D);
            if (swept.intersects(target.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    public Vec3 flightAxis() {
        return getDeltaMovement().lengthSqr() < 1.0E-8D
                ? new Vec3(0.0D, 1.0D, 0.0D)
                : getDeltaMovement().normalize();
    }

    private void applyKnockback(LivingEntity target) {
        Vec3 horizontal = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
        if (horizontal.lengthSqr() < 1.0E-6D) {
            horizontal = new Vec3(1.0D, 0.0D, 0.0D);
        }
        Vec3 impulse = switch (getMode()) {
            case CEILING_CRUSH -> new Vec3(0.0D, -0.45D, 0.0D);
            case WALL_LANCES -> horizontal.normalize().scale(0.65D).add(0.0D, 0.15D, 0.0D);
            case FLOOR_TEETH -> new Vec3(0.0D, 0.75D, 0.0D);
        };
        target.setDeltaMovement(target.getDeltaMovement().add(impulse));
        target.hurtMarked = true;
    }

    private void finish(ServerLevel level, ServerPlayer owner, BlockPos preferred) {
        boolean complete = payload == null || payload.settled();
        if (!complete && owner != null) {
            complete = payload.settle(level, owner, preferred, flightAxis());
        }
        if (!complete) {
            complete = payload.emergencySettle(level);
        }
        if (complete) {
            discard();
        } else {
            setDeltaMovement(Vec3.ZERO);
            remainingTicks = 1;
        }
    }

    private void spawnParticles() {
        if ((tickCount & 1) != 0) {
            return;
        }
        BlockState state = isProjected()
                ? ((tickCount & 2) == 0
                    ? Blocks.BLACK_CONCRETE.defaultBlockState()
                    : Blocks.WHITE_CONCRETE.defaultBlockState())
                : getCarriedState(Math.floorMod(tickCount / 2, getPayloadLength()));
        level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                getX(), getY(), getZ(), 0.0D, 0.02D, 0.0D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        targetId = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
        int mode = tag.getInt("Mode");
        entityData.set(MODE, Math.max(0, Math.min(mode, LivingLandMode.values().length - 1)));
        damage = clamp(tag.getFloat("Damage"), 1.0F, 40.0F);
        speed = clamp(tag.getFloat("Speed"), 0.25F, 2.0F);
        remainingTicks = Math.max(1, Math.min(tag.getInt("RemainingTicks"), 160));
        if (level() instanceof ServerLevel serverLevel && tag.contains("Payload")) {
            payload = LivingLandPillarPayload.readNbt(
                    serverLevel, tag.getCompound("Payload"));
            syncPayload();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        if (targetId != null) tag.putUUID("Target", targetId);
        tag.putInt("Mode", getMode().ordinal());
        tag.putFloat("Damage", damage);
        tag.putFloat("Speed", speed);
        tag.putInt("RemainingTicks", remainingTicks);
        if (payload != null) {
            tag.put("Payload", payload.writeNbt());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public LivingLandMode getMode() {
        return LivingLandMode.values()[entityData.get(MODE)];
    }

    public int getPayloadLength() {
        return Math.max(1, Math.min(entityData.get(LENGTH), 5));
    }

    public boolean isProjected() {
        return entityData.get(PROJECTED);
    }

    public BlockState getCarriedState(int index) {
        int safeIndex = Math.max(0, Math.min(index, getPayloadLength() - 1));
        return Block.stateById(entityData.get(STATES.get(safeIndex)));
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public static int activeCount(ServerLevel level, UUID ownerId) {
        int count = 0;
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingLandStrikeEntity strike
                    && !strike.isRemoved() && ownerId.equals(strike.ownerId)) {
                count++;
            }
        }
        return count;
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Float.isFinite(value) ? Math.max(minimum, Math.min(value, maximum)) : minimum;
    }
}
