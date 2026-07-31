package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import com.vincenthuto.mnagnosis.common.architectonics.ArchitectonicProgression;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ReassembledTransactionService {
    private ReassembledTransactionService() {
    }

    public static AssemblyResult assemble(
            ServerLevel level,
            ServerPlayer caster,
            ReassembledPlan plan,
            int range,
            long dueAt
    ) {
        if (level == null
                || caster == null
                || plan == null
                || plan.targets().isEmpty()
                || plan.targets().size() > ReassembledPlanner.MAX_CELLS
                || range < 4
                || range > 24
                || dueAt <= level.getGameTime()) {
            return AssemblyResult.rejected(
                    AssemblyResult.Failure.INVALID_PLAN);
        }
        ReassembledSavedData data = ReassembledSavedData.get(level);
        AssemblyResult.Failure targetFailure =
                preflightTargets(level, caster, plan.targets());
        if (targetFailure != AssemblyResult.Failure.NONE) {
            return AssemblyResult.rejected(targetFailure);
        }
        List<BlockPos> placementTargets = plan.targets().stream()
                .filter(target ->
                        level.getBlockState(target).canBeReplaced()
                                && level.getBlockEntity(target) == null)
                .toList();
        if (placementTargets.isEmpty()) {
            return AssemblyResult.alreadySatisfied();
        }
        if (!data.receipts().hasCapacity(caster.getUUID())) {
            return AssemblyResult.rejected(
                    AssemblyResult.Failure.RECEIPT_CAP);
        }
        List<BlockPos> sources = sources(
                level,
                caster,
                placementTargets,
                plan.targets(),
                range);
        if (sources.size() < placementTargets.size()) {
            return AssemblyResult.rejected(
                    AssemblyResult.Failure.INSUFFICIENT_MATTER);
        }

        UUID receiptId = UUID.randomUUID();
        ArrayList<ReassembledMove> moves = new ArrayList<>();
        for (int index = 0; index < placementTargets.size(); index++) {
            BlockPos source = sources.get(index);
            BlockPos target = placementTargets.get(index);
            moves.add(new ReassembledMove(
                    source,
                    target,
                    NbtUtils.writeBlockState(level.getBlockState(source)),
                    NbtUtils.writeBlockState(level.getBlockState(target))));
        }
        ReassembledReceipt receipt = new ReassembledReceipt(
                receiptId,
                caster.getUUID(),
                level.dimension().location(),
                level.getGameTime(),
                dueAt,
                ReceiptStatus.RETURNING,
                moves);
        data.beginJournal(receipt);
        data.flush(level);

        boolean complete = true;
        for (ReassembledMove move : moves) {
            BlockState expectedSource =
                    readState(level, move.sourceState());
            if (!level.getBlockState(move.source())
                    .equals(expectedSource)
                    || !eligible(level, move.source())
                    || !level.getBlockState(move.target())
                    .canBeReplaced()
                    || level.getBlockEntity(move.target()) != null
                    || !expectedSource.canSurvive(
                            level, move.target())) {
                complete = false;
                break;
            }
            if (!level.setBlock(
                    move.source(),
                    net.minecraft.world.level.block.Blocks.AIR
                            .defaultBlockState(),
                    2)
                    || !level.setBlock(
                    move.target(), expectedSource, 2)) {
                complete = false;
                break;
            }
        }
        if (complete) {
            complete = moves.stream().allMatch(move ->
                    level.getBlockState(move.source()).isAir()
                            && level.getBlockState(move.target()).equals(
                            readState(level, move.sourceState())));
        }
        if (!complete) {
            if (rollbackToOriginal(level, moves)) {
                flushWorld(level);
                data.clearJournal(receiptId);
            }
            data.flush(level);
            return AssemblyResult.rejected(
                    AssemblyResult.Failure.ROLLED_BACK);
        }
        flushWorld(level);
        data.commitForward(receipt);
        data.flush(level);
        return AssemblyResult.placed(receiptId);
    }

    public static AssemblyResult excavate(
            ServerLevel level,
            ServerPlayer caster,
            ReassembledPlan plan,
            int range,
            long dueAt
    ) {
        if (level == null
                || caster == null
                || plan == null
                || plan.targets().isEmpty()
                || plan.targets().size() > ReassembledPlanner.MAX_CELLS
                || range < 4
                || range > 24
                || dueAt <= level.getGameTime()) {
            return AssemblyResult.rejected(
                    AssemblyResult.Failure.INVALID_PLAN);
        }
        ReassembledSavedData data = ReassembledSavedData.get(level);
        List<BlockPos> clippedTargets = plan.targets().stream()
                .filter(target -> withinRange(
                        caster.blockPosition(), target, range))
                .toList();
        if (clippedTargets.isEmpty()) {
            return AssemblyResult.alreadySatisfied();
        }
        BlockPos clippedAnchor = withinRange(
                caster.blockPosition(), plan.anchor(), range)
                ? plan.anchor()
                : clippedTargets.get(0);
        ReassembledPlan clippedPlan = new ReassembledPlan(
                plan.pattern(),
                clippedAnchor,
                plan.face(),
                clippedTargets);
        ArrayList<BlockPos> sources = new ArrayList<>();
        for (BlockPos source : clippedTargets) {
            if (!level.hasChunkAt(source)) {
                return AssemblyResult.rejected(
                        AssemblyResult.Failure.UNLOADED);
            }
            if (!level.getWorldBorder().isWithinBounds(source)) {
                return AssemblyResult.rejected(
                        AssemblyResult.Failure.DENIED);
            }
            BlockState sourceState = level.getBlockState(source);
            if (!sourceState.getFluidState().isEmpty()
                    || level.getBlockEntity(source) != null) {
                return AssemblyResult.rejected(
                        AssemblyResult.Failure.BLOCKED_TARGET);
            }
            if (sourceState.canBeReplaced()) {
                continue;
            }
            if (!eligibleExcavation(level, source)
                    || isProtected(level, source)) {
                return AssemblyResult.rejected(
                        AssemblyResult.Failure.BLOCKED_TARGET);
            }
            sources.add(source.immutable());
        }
        if (sources.isEmpty()) {
            return AssemblyResult.alreadySatisfied();
        }
        if (!data.receipts().hasCapacity(caster.getUUID())) {
            return AssemblyResult.rejected(
                    AssemblyResult.Failure.RECEIPT_CAP);
        }
        List<BlockPos> spoil =
                ReassembledSpoilPlanner.select(
                                level,
                                caster,
                                clippedPlan,
                                sources,
                                range)
                        .orElse(null);
        if (spoil == null || spoil.size() != sources.size()) {
            return AssemblyResult.rejected(
                    AssemblyResult.Failure.INSUFFICIENT_MATTER);
        }

        UUID receiptId = UUID.randomUUID();
        ArrayList<ReassembledMove> moves = new ArrayList<>();
        for (int index = 0; index < sources.size(); index++) {
            BlockPos source = sources.get(index);
            BlockPos target = spoil.get(index);
            moves.add(new ReassembledMove(
                    source,
                    target,
                    NbtUtils.writeBlockState(
                            level.getBlockState(source)),
                    NbtUtils.writeBlockState(
                            level.getBlockState(target))));
        }
        ReassembledReceipt receipt = new ReassembledReceipt(
                receiptId,
                caster.getUUID(),
                level.dimension().location(),
                level.getGameTime(),
                dueAt,
                ReceiptStatus.RETURNING,
                moves);
        data.beginJournal(receipt);
        data.flush(level);

        boolean complete = true;
        for (ReassembledMove move : moves) {
            BlockState expected =
                    readState(level, move.sourceState());
            if (!level.getBlockState(move.source()).equals(expected)
                    || !eligibleExcavation(level, move.source())
                    || !level.getBlockState(move.target())
                    .canBeReplaced()
                    || level.getBlockEntity(move.target()) != null
                    || !expected.canSurvive(level, move.target())) {
                complete = false;
                break;
            }
            if (!level.setBlock(
                    move.source(),
                    net.minecraft.world.level.block.Blocks.AIR
                            .defaultBlockState(),
                    2)
                    || !level.setBlock(
                    move.target(), expected, 2)) {
                complete = false;
                break;
            }
        }
        if (complete) {
            complete = moves.stream().allMatch(move ->
                    level.getBlockState(move.source()).isAir()
                            && level.getBlockState(move.target())
                            .equals(readState(
                                    level, move.sourceState())));
        }
        if (!complete) {
            if (rollbackToOriginal(level, moves)) {
                flushWorld(level);
                data.clearJournal(receiptId);
            }
            data.flush(level);
            return AssemblyResult.rejected(
                    AssemblyResult.Failure.ROLLED_BACK);
        }
        flushWorld(level);
        data.commitForward(receipt);
        data.flush(level);
        return AssemblyResult.placed(receiptId);
    }

    public static AssemblyResult.Returned returnReceipt(
            ServerLevel level,
            ServerPlayer actor,
            UUID receiptId,
            boolean manual
    ) {
        ReassembledSavedData data = ReassembledSavedData.get(level);
        ReassembledReceipt receipt =
                data.receipt(receiptId).orElse(null);
        if (receipt == null) {
            return AssemblyResult.Returned.NOT_FOUND;
        }
        if (manual && (actor == null
                || !receipt.ownerId().equals(actor.getUUID()))) {
            return AssemblyResult.Returned.DENIED;
        }
        if (!receipt.dimension().equals(level.dimension().location())) {
            return AssemblyResult.Returned.DENIED;
        }
        if (receipt.moves().stream().anyMatch(move ->
                !level.hasChunkAt(move.source())
                        || !level.hasChunkAt(move.target()))) {
            return AssemblyResult.Returned.UNLOADED;
        }
        data.receipts().update(receiptId, ReceiptStatus.RETURNING);
        data.changed();
        data.flush(level);
        if (!rollbackToOriginal(level, receipt.moves())) {
            restoreAssembled(level, receipt.moves());
            data.receipts().update(
                    receiptId, ReceiptStatus.CONFLICTED);
            data.changed();
            data.flush(level);
            return AssemblyResult.Returned.ROLLED_BACK;
        }
        flushWorld(level);
        data.receipts().close(receiptId);
        data.changed();
        data.flush(level);
        if (manual && actor != null) {
            ArchitectonicProgression.grantReturnedLand(actor, receiptId);
            return AssemblyResult.Returned.MANUAL;
        }
        return AssemblyResult.Returned.AUTOMATIC;
    }

    public static void tick(ServerLevel level) {
        if (level != level.getServer().overworld()) {
            return;
        }
        ReassembledSavedData data = ReassembledSavedData.get(level);
        if (data.recoveryPending()) {
            data.recoveryPending(!recoverInterrupted(level, data));
        }
        boolean newlyDue = data.receipts().all().stream().anyMatch(receipt ->
                receipt.status() == ReceiptStatus.ACTIVE
                        && receipt.dueAt() <= level.getGameTime());
        for (ReassembledReceipt receipt
                : data.receipts().markDue(level.getGameTime())) {
            ServerLevel receiptLevel = level(level, receipt);
            if (receiptLevel != null) {
                returnReceipt(
                        receiptLevel, null, receipt.id(), false);
            }
        }
        if (newlyDue) {
            data.changed();
        }
    }

    public static boolean isProtected(
            ServerLevel level,
            BlockPos pos
    ) {
        return ReassembledSavedData.get(level)
                .receipts().isProtected(
                        level.dimension().location(), pos)
                || ReassembledSavedData.get(level).journals().values()
                .stream()
                .filter(receipt -> receipt.dimension().equals(
                        level.dimension().location()))
                .flatMap(receipt -> receipt.moves().stream())
                .anyMatch(move -> move.source().equals(pos)
                        || move.target().equals(pos));
    }

    private static AssemblyResult.Failure preflightTargets(
            ServerLevel level,
            ServerPlayer caster,
            List<BlockPos> targets
    ) {
        for (BlockPos target : targets) {
            if (!level.hasChunkAt(target)) {
                return AssemblyResult.Failure.UNLOADED;
            }
            boolean needsPlacement =
                    level.getBlockState(target).canBeReplaced()
                            && level.getBlockEntity(target) == null;
            if (needsPlacement && isProtected(level, target)) {
                return AssemblyResult.Failure.BLOCKED_TARGET;
            }
            if (!level.getWorldBorder().isWithinBounds(target)
                ) {
                return AssemblyResult.Failure.DENIED;
            }
        }
        return AssemblyResult.Failure.NONE;
    }

    private static boolean recoverInterrupted(
            ServerLevel canonical,
            ReassembledSavedData data
    ) {
        boolean complete = true;
        for (ReassembledReceipt journal : data.journals().values()) {
            ServerLevel world = level(canonical, journal);
            if (world == null || !allLoaded(world, journal.moves())) {
                complete = false;
                continue;
            }
            Layout layout = layout(world, journal.moves());
            if (layout == Layout.CONFLICTED) {
                if (data.conflictJournal(journal)) {
                    data.flush(world);
                } else {
                    complete = false;
                }
            } else if (layout == Layout.ORIGINAL) {
                data.clearJournal(journal.id());
                data.flush(world);
            } else if (rollbackToOriginal(world, journal.moves())) {
                flushWorld(world);
                data.clearJournal(journal.id());
                data.flush(world);
            } else {
                complete = false;
            }
        }
        for (ReassembledReceipt receipt : data.receipts().all()) {
            if (receipt.status() != ReceiptStatus.ACTIVE
                    && receipt.status() != ReceiptStatus.RETURNING) {
                continue;
            }
            ServerLevel world = level(canonical, receipt);
            if (world == null || !allLoaded(world, receipt.moves())) {
                complete = false;
                continue;
            }
            Layout layout = layout(world, receipt.moves());
            if (layout == Layout.CONFLICTED) {
                data.receipts().update(
                        receipt.id(), ReceiptStatus.CONFLICTED);
                data.changed();
                data.flush(world);
                continue;
            }
            if (receipt.status() == ReceiptStatus.ACTIVE
                    && layout == Layout.ASSEMBLED) {
                continue;
            }
            if (layout == Layout.ORIGINAL) {
                data.receipts().close(receipt.id());
                data.changed();
                data.flush(world);
            } else if (rollbackToOriginal(world, receipt.moves())) {
                flushWorld(world);
                data.receipts().close(receipt.id());
                data.changed();
                data.flush(world);
            } else {
                complete = false;
            }
        }
        return complete;
    }

    private static ServerLevel level(
            ServerLevel canonical,
            ReassembledReceipt receipt
    ) {
        return canonical.getServer().getLevel(ResourceKey.create(
                Registries.DIMENSION, receipt.dimension()));
    }

    private static Layout layout(
            ServerLevel level,
            List<ReassembledMove> moves
    ) {
        boolean allOriginal = true;
        boolean allAssembled = true;
        BlockState air =
                net.minecraft.world.level.block.Blocks.AIR
                        .defaultBlockState();
        for (ReassembledMove move : moves) {
            BlockState source = level.getBlockState(move.source());
            BlockState target = level.getBlockState(move.target());
            BlockState originalSource =
                    readState(level, move.sourceState());
            BlockState originalTarget =
                    readState(level, move.targetState());
            boolean original = source.equals(originalSource)
                    && target.equals(originalTarget);
            boolean assembled = source.equals(air)
                    && target.equals(originalSource);
            boolean transitional =
                    (source.equals(originalSource) || source.equals(air))
                            && (target.equals(originalTarget)
                            || target.equals(originalSource));
            if (!transitional) {
                return Layout.CONFLICTED;
            }
            allOriginal &= original;
            allAssembled &= assembled;
        }
        if (allOriginal) {
            return Layout.ORIGINAL;
        }
        if (allAssembled) {
            return Layout.ASSEMBLED;
        }
        return Layout.TRANSITIONAL;
    }

    private static boolean allLoaded(
            ServerLevel level,
            List<ReassembledMove> moves
    ) {
        return moves.stream().allMatch(move ->
                level.hasChunkAt(move.source())
                        && level.hasChunkAt(move.target()));
    }

    private static void flushWorld(ServerLevel level) {
        level.getChunkSource().save(true);
    }

    private static List<BlockPos> sources(
            ServerLevel level,
            ServerPlayer caster,
            List<BlockPos> targets,
            List<BlockPos> excludedTargets,
            int range
    ) {
        Set<BlockPos> targetSet = new HashSet<>(excludedTargets);
        BlockPos origin = caster.blockPosition();
        ArrayList<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-range, -range, -range),
                origin.offset(range, range, range))) {
            BlockPos immutable = pos.immutable();
            if (!targetSet.contains(immutable)
                    && eligible(level, immutable)
                    && !isProtected(level, immutable)) {
                candidates.add(immutable);
            }
        }
        candidates.sort(Comparator
                .comparingDouble((BlockPos source) -> targets.stream()
                        .mapToDouble(target -> target.distSqr(source))
                        .min().orElse(Double.MAX_VALUE))
                .thenComparingInt(BlockPos::getY)
                .thenComparingInt(BlockPos::getX)
                .thenComparingInt(BlockPos::getZ));
        return List.copyOf(candidates);
    }

    private static boolean eligible(
            ServerLevel level,
            BlockPos source
    ) {
        if (!level.hasChunkAt(source)) {
            return false;
        }
        BlockState state = level.getBlockState(source);
        if (state.isAir()
                || !state.getFluidState().isEmpty()
                || level.getBlockEntity(source) != null
                || state.getDestroySpeed(level, source) < 0.0F
                || state.getBlock() instanceof FallingBlock
                || !state.isCollisionShapeFullBlock(level, source)) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = source.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (!neighbor.getFluidState().isEmpty()
                    || neighbor.getBlock() instanceof FallingBlock
                    || !neighbor.isAir()
                    && !neighbor.isCollisionShapeFullBlock(
                    level, neighborPos)) {
                return false;
            }
        }
        return true;
    }

    private static boolean eligibleExcavation(
            ServerLevel level,
            BlockPos source
    ) {
        BlockState state = level.getBlockState(source);
        return level.hasChunkAt(source)
                && !state.isAir()
                && !state.canBeReplaced()
                && state.getFluidState().isEmpty()
                && level.getBlockEntity(source) == null
                && state.getDestroySpeed(level, source) >= 0.0F
                && !(state.getBlock() instanceof FallingBlock)
                && state.isCollisionShapeFullBlock(level, source);
    }

    private static boolean rollbackToOriginal(
            ServerLevel level,
            List<ReassembledMove> moves
    ) {
        boolean complete = true;
        for (ReassembledMove move : moves) {
            complete &= restoreExact(
                    level,
                    move.target(),
                    readState(level, move.targetState()));
        }
        for (ReassembledMove move : moves) {
            complete &= restoreExact(
                    level,
                    move.source(),
                    readState(level, move.sourceState()));
        }
        return complete;
    }

    private static boolean restoreExact(
            ServerLevel level,
            BlockPos pos,
            BlockState desired
    ) {
        if (level.getBlockState(pos).equals(desired)) {
            return true;
        }
        level.setBlock(pos, desired, 2);
        return level.getBlockState(pos).equals(desired);
    }

    private static void restoreAssembled(
            ServerLevel level,
            List<ReassembledMove> moves
    ) {
        for (ReassembledMove move : moves) {
            level.setBlock(
                    move.source(),
                    net.minecraft.world.level.block.Blocks.AIR
                            .defaultBlockState(),
                    2);
            level.setBlock(
                    move.target(),
                    readState(level, move.sourceState()),
                    2);
        }
    }

    private static BlockState readState(
            ServerLevel level,
            net.minecraft.nbt.CompoundTag tag
    ) {
        return NbtUtils.readBlockState(
                level.holderLookup(Registries.BLOCK),
                tag);
    }

    private static boolean withinRange(
            BlockPos origin,
            BlockPos candidate,
            int range
    ) {
        return Math.abs(candidate.getX() - origin.getX()) <= range
                && Math.abs(candidate.getY() - origin.getY()) <= range
                && Math.abs(candidate.getZ() - origin.getZ()) <= range;
    }

    private enum Layout {
        ORIGINAL,
        ASSEMBLED,
        TRANSITIONAL,
        CONFLICTED
    }
}
