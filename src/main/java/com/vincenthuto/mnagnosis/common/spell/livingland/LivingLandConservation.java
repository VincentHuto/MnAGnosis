package com.vincenthuto.mnagnosis.common.spell.livingland;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LivingLandConservation {

    public enum SettlementResult {
        DEPOSITED,
        RESTORED,
        DROPPED,
        FAILED
    }

    public static final class Reservation {
        private final BlockPos source;
        private final BlockState state;
        private boolean settled;

        public Reservation(BlockPos source, BlockState state) {
            this.source = source.immutable();
            this.state = state;
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

        private void markSettled() {
            settled = true;
        }
    }

    private LivingLandConservation() {
    }

    public static Optional<Reservation> reserve(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos source
    ) {
        if (!LivingLandTerrain.isEligibleSource(level, caster, source)) {
            return Optional.empty();
        }
        int experience = ForgeHooks.onBlockBreakEvent(
                level,
                caster.gameMode.getGameModeForPlayer(),
                caster,
                source
        );
        if (experience < 0) {
            return Optional.empty();
        }
        BlockState state = level.getBlockState(source);
        if (!level.setBlock(source, net.minecraft.world.level.block.Blocks.AIR
                .defaultBlockState(), 3)) {
            return Optional.empty();
        }
        return Optional.of(new Reservation(source, state));
    }

    public static SettlementResult settle(
            ServerLevel level,
            ServerPlayer caster,
            Reservation reservation,
            BlockPos preferred
    ) {
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

    public static SettlementResult emergencySettle(
            ServerLevel level,
            Reservation reservation
    ) {
        if (reservation.settled) {
            return SettlementResult.FAILED;
        }
        if (level.hasChunkAt(reservation.source)
                && level.getBlockState(reservation.source).canBeReplaced()
                && level.setBlock(reservation.source, reservation.state, 3)) {
            reservation.markSettled();
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
                    new net.minecraft.world.item.ItemStack(item)
            ));
            reservation.markSettled();
            return SettlementResult.DROPPED;
        }
        return SettlementResult.FAILED;
    }

    private static boolean place(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos pos,
            BlockState state
    ) {
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
