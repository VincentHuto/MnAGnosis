package com.vincenthuto.mnagnosis.common.conservation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;

public final class ConservedBlockService {
    public enum SettlementResult {
        DEPOSITED,
        RESTORED,
        DROPPED,
        FAILED
    }

    @FunctionalInterface
    public interface SourcePolicy {
        boolean canReserve(ServerLevel level, ServerPlayer player, BlockPos source);
    }

    @FunctionalInterface
    public interface ReservationFactory<R extends Reservation> {
        R create(BlockPos source, BlockState state);
    }

    public static class Reservation {
        private final BlockPos source;
        private final BlockState state;
        private boolean settled;

        public Reservation(BlockPos source, BlockState state) {
            this(source, state, false);
        }

        public Reservation(BlockPos source, BlockState state, boolean settled) {
            this.source = source.immutable();
            this.state = state;
            this.settled = settled;
        }

        public BlockPos source() {
            return source;
        }

        public BlockState state() {
            return state;
        }

        public boolean settled() {
            return settled;
        }
    }

    private ConservedBlockService() {
    }

    public static <R extends Reservation> Optional<R> reserve(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos source,
            SourcePolicy policy,
            ReservationFactory<R> factory) {
        if (!policy.canReserve(level, caster, source)) {
            return Optional.empty();
        }
        int experience = ForgeHooks.onBlockBreakEvent(
                level, caster.gameMode.getGameModeForPlayer(), caster, source);
        if (experience < 0) {
            return Optional.empty();
        }
        BlockState state = level.getBlockState(source);
        if (!level.setBlock(source, net.minecraft.world.level.block.Blocks.AIR
                .defaultBlockState(), 3)) {
            return Optional.empty();
        }
        return Optional.of(factory.create(source, state));
    }

    public static SettlementResult settle(
            ServerLevel level,
            ServerPlayer caster,
            Reservation reservation,
            BlockPos preferred) {
        if (reservation.settled) {
            return SettlementResult.FAILED;
        }
        List<BlockPos> candidates = new ArrayList<>();
        candidates.add(preferred.immutable());
        for (int radius = 1; radius <= 1; radius++) {
            for (int y = -radius; y <= radius; y++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        candidates.add(preferred.offset(x, y, z));
                    }
                }
            }
        }
        for (BlockPos candidate : candidates) {
            if (place(level, caster, candidate, reservation.state)) {
                reservation.settled = true;
                return candidate.equals(reservation.source)
                        ? SettlementResult.RESTORED
                        : SettlementResult.DEPOSITED;
            }
        }
        if (place(level, caster, reservation.source, reservation.state)) {
            reservation.settled = true;
            return SettlementResult.RESTORED;
        }
        return SettlementResult.FAILED;
    }

    /**
     * Deposits reserved matter at one exact position. Unlike {@link #settle},
     * this never substitutes a neighboring cell or restores the source.
     */
    public static SettlementResult settleExact(
            ServerLevel level,
            ServerPlayer caster,
            Reservation reservation,
            BlockPos target) {
        if (reservation.settled
                || !place(level, caster, target, reservation.state)) {
            return SettlementResult.FAILED;
        }
        reservation.settled = true;
        return target.equals(reservation.source)
                ? SettlementResult.RESTORED
                : SettlementResult.DEPOSITED;
    }

    public static SettlementResult emergencySettle(
            ServerLevel level,
            Reservation reservation) {
        if (reservation.settled) {
            return SettlementResult.FAILED;
        }
        if (level.hasChunkAt(reservation.source)
                && level.getBlockState(reservation.source).canBeReplaced()
                && level.setBlock(reservation.source, reservation.state, 3)) {
            reservation.settled = true;
            return SettlementResult.RESTORED;
        }
        net.minecraft.world.item.Item item = reservation.state.getBlock().asItem();
        if (item != net.minecraft.world.item.Items.AIR
                && level.hasChunkAt(reservation.source)) {
            level.addFreshEntity(new ItemEntity(
                    level,
                    reservation.source.getX() + 0.5D,
                    reservation.source.getY() + 0.5D,
                    reservation.source.getZ() + 0.5D,
                    new net.minecraft.world.item.ItemStack(item)));
            reservation.settled = true;
            return SettlementResult.DROPPED;
        }
        return SettlementResult.FAILED;
    }

    private static boolean place(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos pos,
            BlockState state) {
        if (!level.hasChunkAt(pos)
                || !level.getWorldBorder().isWithinBounds(pos)
                || !level.mayInteract(caster, pos)
                || !level.getBlockState(pos).canBeReplaced()
                || level.getBlockEntity(pos) != null) {
            return false;
        }
        BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
        BlockState replaced = level.getBlockState(pos);
        if (!level.setBlock(pos, state, 3)) {
            return false;
        }
        BlockEvent.EntityPlaceEvent event =
                new BlockEvent.EntityPlaceEvent(snapshot, replaced, caster);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            snapshot.restore(true, false);
            return false;
        }
        return true;
    }
}
