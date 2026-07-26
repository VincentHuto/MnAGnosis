package com.vincenthuto.mnagnosis.common.entity;

import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandConservation;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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

    public static final int MAX_ACTIVE_PER_OWNER = 8;
    private static final EntityDataAccessor<Integer> MODE =
            SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CARRIED_STATE =
            SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT);
    private UUID ownerId;
    private UUID targetId;
    private LivingLandConservation.Reservation reservation;
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
        entityData.define(CARRIED_STATE, Block.getId(Blocks.STONE.defaultBlockState()));
    }

    public void configure(ServerPlayer owner, LivingEntity target, LivingLandMode mode,
                          LivingLandConservation.Reservation reservation,
                          float damage, float speed) {
        ownerId = owner.getUUID();
        targetId = target.getUUID();
        this.reservation = reservation;
        this.damage = clamp(damage, 1.0F, 40.0F);
        this.speed = clamp(speed, 0.25F, 2.0F);
        entityData.set(MODE, mode.ordinal());
        entityData.set(CARRIED_STATE, Block.getId(reservation.state()));
        setPos(Vec3.atCenterOf(reservation.source()));
        Vec3 direction = target.getBoundingBox().getCenter().subtract(position()).normalize();
        setDeltaMovement(direction.scale(this.speed));
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
        AABB swept = getBoundingBox().minmax(new AABB(oldPosition, position())).inflate(0.35D);
        List<LivingEntity> hits = serverLevel.getEntitiesOfClass(
                LivingEntity.class, swept,
                candidate -> candidate == target && candidate.isAlive()
        );
        if (!hits.isEmpty()) {
            target.hurt(serverLevel.damageSources().indirectMagic(this, owner), damage);
            applyKnockback(target);
            finish(serverLevel, owner, target.blockPosition());
        }
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
        LivingLandConservation.SettlementResult result =
                LivingLandConservation.SettlementResult.FAILED;
        if (reservation != null && !reservation.settled() && owner != null) {
            result = LivingLandConservation.settle(level, owner, reservation, preferred);
        }
        if (reservation != null && !reservation.settled()
                && result == LivingLandConservation.SettlementResult.FAILED) {
            result = LivingLandConservation.emergencySettle(level, reservation);
        }
        if (reservation == null || reservation.settled()
                || result != LivingLandConservation.SettlementResult.FAILED) {
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
        BlockState state = (tickCount & 2) == 0
                ? Blocks.BLACK_CONCRETE.defaultBlockState()
                : Blocks.WHITE_CONCRETE.defaultBlockState();
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
        if (tag.contains("CarriedState") && tag.contains("Source")) {
            BlockState state = NbtUtils.readBlockState(
                    level().holderLookup(Registries.BLOCK), tag.getCompound("CarriedState")
            );
            BlockPos source = NbtUtils.readBlockPos(tag.getCompound("Source"));
            reservation = new LivingLandConservation.Reservation(source, state);
            entityData.set(CARRIED_STATE, Block.getId(state));
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
        if (reservation != null) {
            tag.put("Source", NbtUtils.writeBlockPos(reservation.source()));
            tag.put("CarriedState", NbtUtils.writeBlockState(reservation.state()));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public LivingLandMode getMode() {
        return LivingLandMode.values()[entityData.get(MODE)];
    }

    public BlockState getCarriedState() {
        return Block.stateById(entityData.get(CARRIED_STATE));
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
