package com.vincenthuto.mnagnosis.common.spell.livingland;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LivingLandPillarPayload {

    public record Entry(BlockPos source, BlockState state) {
        public Entry {
            source = source.immutable();
        }
    }

    private final List<LivingLandConservation.Reservation> reservations;
    private final boolean projected;
    private boolean settled;

    private LivingLandPillarPayload(
            List<LivingLandConservation.Reservation> reservations,
            boolean projected,
            boolean settled
    ) {
        this.reservations = new ArrayList<>(reservations);
        this.projected = projected;
        this.settled = settled;
    }

    public static Optional<LivingLandPillarPayload> acquire(
            ServerLevel level,
            ServerPlayer caster,
            List<BlockPos> sources,
            boolean projected
    ) {
        if (sources.size() < 3 || sources.size() > 5
                || sources.stream().distinct().count() != sources.size()) {
            return Optional.empty();
        }
        for (BlockPos source : sources) {
            if (!LivingLandTerrain.isEligibleSource(level, caster, source)) {
                return Optional.empty();
            }
        }
        if (projected) {
            List<LivingLandConservation.Reservation> snapshots = sources.stream()
                    .map(source -> new LivingLandConservation.Reservation(
                            source, level.getBlockState(source)))
                    .toList();
            return Optional.of(new LivingLandPillarPayload(snapshots, true, false));
        }

        List<LivingLandConservation.Reservation> acquired = new ArrayList<>();
        for (BlockPos source : sources) {
            Optional<LivingLandConservation.Reservation> reservation =
                    LivingLandConservation.reserve(level, caster, source);
            if (reservation.isEmpty()) {
                restoreAcquired(level, caster, acquired);
                return Optional.empty();
            }
            acquired.add(reservation.get());
        }
        return Optional.of(new LivingLandPillarPayload(acquired, false, false));
    }

    private static void restoreAcquired(
            ServerLevel level,
            ServerPlayer caster,
            List<LivingLandConservation.Reservation> acquired
    ) {
        for (LivingLandConservation.Reservation reservation : acquired) {
            if (LivingLandConservation.settle(
                    level, caster, reservation, reservation.source())
                    == LivingLandConservation.SettlementResult.FAILED) {
                LivingLandConservation.emergencySettle(level, reservation);
            }
        }
    }

    public boolean settle(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos preferred,
            Vec3 axis
    ) {
        if (settled) {
            return false;
        }
        if (projected) {
            settled = true;
            return true;
        }
        Vec3 direction = axis.lengthSqr() < 1.0E-8D
                ? new Vec3(0.0D, 1.0D, 0.0D) : axis.normalize();
        double center = (reservations.size() - 1) * 0.5D;
        boolean complete = true;
        for (int index = 0; index < reservations.size(); index++) {
            LivingLandConservation.Reservation reservation = reservations.get(index);
            BlockPos offset = BlockPos.containing(direction.scale(index - center));
            LivingLandConservation.SettlementResult result =
                    LivingLandConservation.settle(
                            level, caster, reservation, preferred.offset(offset));
            if (result == LivingLandConservation.SettlementResult.FAILED) {
                result = LivingLandConservation.emergencySettle(level, reservation);
            }
            complete &= result != LivingLandConservation.SettlementResult.FAILED;
        }
        settled = reservations.stream().allMatch(
                LivingLandConservation.Reservation::settled);
        return complete && settled;
    }

    public boolean emergencySettle(ServerLevel level) {
        if (settled) {
            return false;
        }
        if (projected) {
            settled = true;
            return true;
        }
        for (LivingLandConservation.Reservation reservation : reservations) {
            if (!reservation.settled()) {
                LivingLandConservation.emergencySettle(level, reservation);
            }
        }
        settled = reservations.stream().allMatch(
                LivingLandConservation.Reservation::settled);
        return settled;
    }

    public CompoundTag writeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Projected", projected);
        tag.putBoolean("Settled", settled);
        ListTag entriesTag = new ListTag();
        for (LivingLandConservation.Reservation reservation : reservations) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("Source", NbtUtils.writeBlockPos(reservation.source()));
            entryTag.put("State", NbtUtils.writeBlockState(reservation.state()));
            entryTag.putBoolean("Settled", reservation.settled());
            entriesTag.add(entryTag);
        }
        tag.put("Entries", entriesTag);
        return tag;
    }

    public static LivingLandPillarPayload readNbt(ServerLevel level, CompoundTag tag) {
        List<LivingLandConservation.Reservation> reservations = new ArrayList<>();
        ListTag entriesTag = tag.getList("Entries", Tag.TAG_COMPOUND);
        int count = Math.min(entriesTag.size(), 5);
        for (int index = 0; index < count; index++) {
            CompoundTag entryTag = entriesTag.getCompound(index);
            BlockPos source = NbtUtils.readBlockPos(entryTag.getCompound("Source"));
            BlockState state = NbtUtils.readBlockState(
                    level.holderLookup(Registries.BLOCK),
                    entryTag.getCompound("State"));
            reservations.add(new LivingLandConservation.Reservation(
                    source, state, entryTag.getBoolean("Settled")));
        }
        return new LivingLandPillarPayload(
                reservations,
                tag.getBoolean("Projected"),
                tag.getBoolean("Settled"));
    }

    public List<Entry> entries() {
        return reservations.stream()
                .map(reservation -> new Entry(
                        reservation.source(), reservation.state()))
                .toList();
    }

    public boolean projected() {
        return projected;
    }

    public boolean settled() {
        return settled;
    }
}
