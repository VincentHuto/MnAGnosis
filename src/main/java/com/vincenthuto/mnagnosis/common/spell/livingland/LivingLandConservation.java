package com.vincenthuto.mnagnosis.common.spell.livingland;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;

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

    private static boolean place(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos pos,
            BlockState state
    ) {
        return level.hasChunkAt(pos)
                && level.getWorldBorder().isWithinBounds(pos)
                && level.mayInteract(caster, pos)
                && level.getBlockState(pos).canBeReplaced()
                && level.getBlockEntity(pos) == null
                && level.setBlock(pos, state, 3);
    }
}
