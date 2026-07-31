package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ReassembledSpoilPlanner {
    private ReassembledSpoilPlanner() {
    }

    static List<BlockPos> moundOffsets(int count) {
        if (count <= 0) {
            return List.of();
        }
        int radius = 0;
        while (moundCapacity(radius) < count) {
            radius++;
        }
        ArrayList<BlockPos> offsets = new ArrayList<>(count);
        for (int layer = 0;
             layer <= radius && offsets.size() < count;
             layer++) {
            int layerRadius = radius - layer;
            for (int x = -layerRadius;
                 x <= layerRadius && offsets.size() < count;
                 x++) {
                for (int z = -layerRadius;
                     z <= layerRadius && offsets.size() < count;
                     z++) {
                    offsets.add(new BlockPos(x, layer + 1, z));
                }
            }
        }
        return List.copyOf(offsets);
    }

    public static Optional<List<BlockPos>> select(
            ServerLevel level,
            ServerPlayer caster,
            ReassembledPlan plan,
            List<BlockPos> sources,
            int range
    ) {
        if (level == null
                || caster == null
                || plan == null
                || sources == null
                || sources.isEmpty()) {
            return Optional.empty();
        }
        List<BlockPos> offsets = moundOffsets(sources.size());
        int radius = offsets.stream()
                .mapToInt(pos -> Math.max(
                        Math.abs(pos.getX()),
                        Math.abs(pos.getZ())))
                .max().orElse(0);
        Direction forward = horizontal(caster.getDirection());
        Direction right = forward.getClockWise();
        List<BlockPos> centers = List.of(
                plan.anchor().relative(
                        right,
                        radius + 2 + extent(
                                plan, right)),
                plan.anchor().relative(
                        right.getOpposite(),
                        radius + 2 + extent(
                                plan, right.getOpposite())),
                plan.anchor().relative(
                        forward.getOpposite(),
                        radius + 2 + extent(
                                plan, forward.getOpposite())));
        for (BlockPos center : centers) {
            for (int shiftRadius = 0;
                 shiftRadius <= range;
                 shiftRadius++) {
                for (int x = -shiftRadius;
                     x <= shiftRadius;
                     x++) {
                    for (int z = -shiftRadius;
                         z <= shiftRadius;
                         z++) {
                        if (Math.max(Math.abs(x), Math.abs(z))
                                != shiftRadius) {
                            continue;
                        }
                        BlockPos shifted = center
                                .relative(right, x)
                                .relative(forward, z);
                        Optional<List<BlockPos>> selected = atCenter(
                                level,
                                caster,
                                plan,
                                sources,
                                offsets,
                                shifted,
                                forward,
                                right,
                                range);
                        if (selected.isPresent()) {
                            return selected;
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<List<BlockPos>> atCenter(
            ServerLevel level,
            ServerPlayer caster,
            ReassembledPlan plan,
            List<BlockPos> sources,
            List<BlockPos> offsets,
            BlockPos center,
            Direction forward,
            Direction right,
            int range
    ) {
        Set<BlockPos> excavation = new HashSet<>(plan.targets());
        Set<BlockPos> selected = new HashSet<>();
        Map<BlockPos, BlockPos> columnBases = new HashMap<>();
        ArrayList<BlockPos> result = new ArrayList<>(sources.size());
        for (int index = 0; index < offsets.size(); index++) {
            BlockPos offset = offsets.get(index);
            BlockPos column = new BlockPos(
                    offset.getX(), 0, offset.getZ());
            BlockPos horizontal = center
                    .relative(right, offset.getX())
                    .relative(forward, offset.getZ());
            BlockState sourceState =
                    level.getBlockState(sources.get(index));
            BlockPos candidate;
            if (offset.getY() == 1) {
                candidate = findSupportedBase(
                        level,
                        caster,
                        plan.anchor(),
                        excavation,
                        selected,
                        horizontal.above(),
                        sourceState,
                        range);
                if (candidate == null) {
                    return Optional.empty();
                }
                columnBases.put(column, candidate);
            } else {
                BlockPos base = columnBases.get(column);
                if (base == null) {
                    return Optional.empty();
                }
                candidate = base.above(offset.getY() - 1);
            }
            if (!valid(
                    level,
                    caster,
                    plan.anchor(),
                        excavation,
                        selected,
                        candidate,
                        offset.getY(),
                        sourceState,
                        range)) {
                return Optional.empty();
            }
            selected.add(candidate);
            result.add(candidate);
        }
        return Optional.of(List.copyOf(result));
    }

    private static BlockPos findSupportedBase(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos mouth,
            Set<BlockPos> excavation,
            Set<BlockPos> selected,
            BlockPos expected,
            BlockState sourceState,
            int range
    ) {
        for (int distance = 0; distance <= range; distance++) {
            int[] deltas = distance == 0
                    ? new int[]{0}
                    : new int[]{distance, -distance};
            for (int delta : deltas) {
                BlockPos candidate = expected.above(delta).immutable();
                BlockPos support = candidate.below();
                if (excavation.contains(support)
                        || !level.hasChunkAt(support)
                        || !level.getWorldBorder().isWithinBounds(support)
                        || !level.getBlockState(support)
                        .isCollisionShapeFullBlock(level, support)) {
                    continue;
                }
                if (valid(
                        level,
                        caster,
                        mouth,
                        excavation,
                        selected,
                        candidate,
                        1,
                        sourceState,
                        range)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean valid(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos mouth,
            Set<BlockPos> excavation,
            Set<BlockPos> selected,
            BlockPos candidate,
            int layer,
            BlockState sourceState,
            int range
    ) {
        if (!level.hasChunkAt(candidate)
                || !level.getWorldBorder().isWithinBounds(candidate)
                || !withinRange(
                caster.blockPosition(), candidate, range)
                || excavation.contains(candidate)
                || selected.contains(candidate)
                || candidate.equals(mouth.above())
                || candidate.equals(mouth.above(2))
                || caster.getBoundingBox().intersects(
                new AABB(candidate))
                || !level.getBlockState(candidate).canBeReplaced()
                || !level.getFluidState(candidate).isEmpty()
                || level.getBlockEntity(candidate) != null
                || ReassembledTransactionService.isProtected(
                level, candidate)
                || !sourceState.canSurvive(level, candidate)) {
            return false;
        }
        BlockPos support = candidate.below();
        return layer > 1
                ? selected.contains(support)
                : level.getBlockState(support)
                .isCollisionShapeFullBlock(level, support);
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

    private static int extent(
            ReassembledPlan plan,
            Direction direction
    ) {
        BlockPos mouth = plan.anchor();
        return plan.targets().stream()
                .mapToInt(pos -> Math.max(
                        0,
                        (pos.getX() - mouth.getX())
                                * direction.getStepX()
                                + (pos.getZ() - mouth.getZ())
                                * direction.getStepZ()))
                .max().orElse(0);
    }

    private static int moundCapacity(int radius) {
        int capacity = 0;
        for (int layer = 0; layer <= radius; layer++) {
            int width = 2 * (radius - layer) + 1;
            capacity += width * width;
        }
        return capacity;
    }

    private static Direction horizontal(Direction direction) {
        return direction.getAxis().isHorizontal()
                ? direction
                : Direction.NORTH;
    }
}
