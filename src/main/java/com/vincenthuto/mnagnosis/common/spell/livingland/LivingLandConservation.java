package com.vincenthuto.mnagnosis.common.spell.livingland;

import java.util.Optional;

import com.vincenthuto.mnagnosis.common.conservation.ConservedBlockService;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public final class LivingLandConservation {
    public enum SettlementResult {
        DEPOSITED,
        RESTORED,
        DROPPED,
        FAILED
    }

    public static final class Reservation extends ConservedBlockService.Reservation {
        public Reservation(BlockPos source, BlockState state) {
            super(source, state);
        }

        public Reservation(BlockPos source, BlockState state, boolean settled) {
            super(source, state, settled);
        }
    }

    private LivingLandConservation() {
    }

    public static Optional<Reservation> reserve(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos source) {
        return ConservedBlockService.reserve(
                level,
                caster,
                source,
                LivingLandTerrain::isEligibleSource,
                Reservation::new);
    }

    public static SettlementResult settle(
            ServerLevel level,
            ServerPlayer caster,
            Reservation reservation,
            BlockPos preferred) {
        return convert(ConservedBlockService.settle(level, caster, reservation, preferred));
    }

    public static SettlementResult emergencySettle(
            ServerLevel level,
            Reservation reservation) {
        return convert(ConservedBlockService.emergencySettle(level, reservation));
    }

    private static SettlementResult convert(ConservedBlockService.SettlementResult result) {
        return SettlementResult.valueOf(result.name());
    }
}
