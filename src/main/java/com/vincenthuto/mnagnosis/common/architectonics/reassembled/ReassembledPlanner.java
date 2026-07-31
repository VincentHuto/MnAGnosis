package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class ReassembledPlanner {
    public static final int MAX_CELLS = 384;

    public PlanResult planExcavation(
            BlockPos mouth,
            Vec3 look,
            Direction casterFacing,
            ReassembledParameters parameters,
            ReassembledPattern pattern
    ) {
        if (mouth == null
                || look == null
                || look.lengthSqr() < 1.0E-6D
                || casterFacing == null
                || parameters == null
                || !parameters.valid()
                || pattern == null) {
            return new PlanResult.Rejected(
                    PlanResult.Failure.INVALID_PARAMETERS);
        }
        Direction forward = horizontal(casterFacing);
        Direction right = forward.getClockWise();
        Vec3 voxelDirection = voxelDirection(look);
        List<BlockPos> generated = switch (pattern) {
            case WALL -> excavationWall(
                    mouth, right, parameters);
            case BRIDGE -> excavationBridge(
                    mouth, voxelDirection, right, parameters);
            case STAIR -> excavationStair(
                    mouth, look, forward, right, parameters);
            case PILLAR -> excavationPillar(
                    mouth, look, voxelDirection, parameters);
        };
        return result(
                pattern,
                mouth,
                Direction.DOWN,
                generated);
    }

    public PlanResult plan(
            BlockPos anchor,
            Direction face,
            BlockPos casterPosition,
            Direction casterFacing,
            ReassembledParameters parameters,
            ReassembledPattern pattern
    ) {
        return planInternal(
                anchor,
                face,
                casterPosition,
                casterFacing,
                parameters,
                pattern);
    }

    public PlanResult plan(
            BlockPos anchor,
            Direction face,
            Direction casterFacing,
            ReassembledParameters parameters,
            ReassembledPattern pattern
    ) {
        return planInternal(
                anchor,
                face,
                null,
                casterFacing,
                parameters,
                pattern);
    }

    private PlanResult planInternal(
            BlockPos anchor,
            Direction face,
            BlockPos casterPosition,
            Direction casterFacing,
            ReassembledParameters parameters,
            ReassembledPattern pattern
    ) {
        if (anchor == null
                || face == null
                || casterFacing == null
                || pattern == null
                || parameters == null
                || !parameters.valid()) {
            return new PlanResult.Rejected(
                    PlanResult.Failure.INVALID_PARAMETERS);
        }
        Direction forward = face.getAxis().isHorizontal()
                ? face
                : horizontal(casterFacing);
        Direction right = forward.getClockWise();
        List<BlockPos> generated = switch (pattern) {
            case WALL -> wall(anchor, right, parameters);
            case BRIDGE -> bridge(anchor, forward, right, parameters);
            case STAIR -> casterPosition == null
                    ? stair(anchor, forward, right, parameters)
                    : aimedStair(
                            casterPosition,
                            anchor,
                            horizontal(casterFacing),
                            parameters);
            case PILLAR -> pillar(anchor, parameters);
        };
        return result(pattern, anchor, face, generated);
    }

    private static PlanResult result(
            ReassembledPattern pattern,
            BlockPos anchor,
            Direction face,
            List<BlockPos> generated
    ) {
        List<BlockPos> normalized = List.copyOf(
                new LinkedHashSet<>(generated.stream()
                        .map(BlockPos::immutable).toList()));
        if (normalized.isEmpty() || normalized.size() > MAX_CELLS) {
            return new PlanResult.Rejected(
                    PlanResult.Failure.TOO_MANY_CELLS);
        }
        return new PlanResult.Success(new ReassembledPlan(
                pattern,
                anchor,
                face,
                normalized));
    }

    private static List<BlockPos> excavationWall(
            BlockPos mouth,
            Direction right,
            ReassembledParameters parameters
    ) {
        List<BlockPos> cells = new ArrayList<>();
        for (int y = 0; y < parameters.height(); y++) {
            for (int x = centeredStart(parameters.width());
                 x <= centeredEnd(parameters.width());
                 x++) {
                cells.add(mouth.relative(right, x).below(y));
            }
        }
        return cells;
    }

    private static List<BlockPos> excavationBridge(
            BlockPos mouth,
            Vec3 direction,
            Direction right,
            ReassembledParameters parameters
    ) {
        List<BlockPos> cells = new ArrayList<>();
        for (int step = 0; step < parameters.depth(); step++) {
            BlockPos center = along(mouth, direction, step);
            for (int x = centeredStart(parameters.width());
                 x <= centeredEnd(parameters.width());
                 x++) {
                BlockPos row = center.relative(right, x);
                cells.add(row);
                cells.add(row.above());
            }
        }
        return cells;
    }

    private static List<BlockPos> excavationStair(
            BlockPos mouth,
            Vec3 look,
            Direction fallbackForward,
            Direction right,
            ReassembledParameters parameters
    ) {
        List<BlockPos> cells = new ArrayList<>();
        Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
        Vec3 forward = horizontalLook.lengthSqr() < 1.0E-6D
                ? new Vec3(
                fallbackForward.getStepX(),
                0.0D,
                fallbackForward.getStepZ())
                : voxelDirection(horizontalLook);
        double horizontalMagnitude = Math.max(
                Math.abs(look.x), Math.abs(look.z));
        double descentPerStep = horizontalMagnitude < 1.0E-6D
                ? 1.0D
                : Math.min(
                1.0D,
                Math.max(0.0D, -look.y / horizontalMagnitude));
        int maximumDrop = Math.max(0, parameters.height() - 1);
        for (int step = 0; step < parameters.depth(); step++) {
            int drop = Math.min(
                    maximumDrop,
                    (int) Math.round(step * descentPerStep));
            BlockPos floor = along(
                    mouth.below(), forward, step).below(drop);
            for (int x = centeredStart(parameters.width());
                 x <= centeredEnd(parameters.width());
                 x++) {
                BlockPos row = floor.relative(right, x);
                cells.add(row.above());
                cells.add(row.above(2));
            }
        }
        return cells;
    }

    private static List<BlockPos> excavationPillar(
            BlockPos mouth,
            Vec3 look,
            Vec3 voxelDirection,
            ReassembledParameters parameters
    ) {
        List<BlockPos> cells = new ArrayList<>();
        Vec3 axis = look.normalize();
        Vec3 right = new Vec3(0.0D, 1.0D, 0.0D).cross(axis);
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 planeUp = axis.cross(right).normalize();
        int radius = parameters.radius();
        for (int step = 0; step < parameters.height(); step++) {
            BlockPos center = along(mouth, voxelDirection, step);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius) {
                        Vec3 offset = right.scale(x)
                                .add(planeUp.scale(z));
                        cells.add(center.offset(
                                (int) Math.round(offset.x),
                                (int) Math.round(offset.y),
                                (int) Math.round(offset.z)));
                    }
                }
            }
        }
        return cells;
    }

    private static BlockPos along(
            BlockPos start,
            Vec3 direction,
            int distance
    ) {
        return start.offset(
                (int) Math.round(direction.x * distance),
                (int) Math.round(direction.y * distance),
                (int) Math.round(direction.z * distance));
    }

    private static Vec3 voxelDirection(Vec3 look) {
        double maximum = Math.max(
                Math.max(Math.abs(look.x), Math.abs(look.y)),
                Math.abs(look.z));
        return maximum < 1.0E-6D
                ? Vec3.ZERO
                : look.scale(1.0D / maximum);
    }

    private static List<BlockPos> wall(
            BlockPos anchor,
            Direction right,
            ReassembledParameters parameters
    ) {
        List<BlockPos> cells = new ArrayList<>();
        for (int y = 0; y < parameters.height(); y++) {
            for (int x = centeredStart(parameters.width());
                 x <= centeredEnd(parameters.width());
                 x++) {
                cells.add(anchor.relative(right, x).above(y));
            }
        }
        return cells;
    }

    private static List<BlockPos> aimedStair(
            BlockPos casterPosition,
            BlockPos impact,
            Direction casterFacing,
            ReassembledParameters parameters
    ) {
        BlockPos start = casterPosition.relative(casterFacing);
        if (impact.getY() < casterPosition.getY()) {
            start = start.below();
        }
        Direction right = casterFacing.getClockWise();
        int segments = Math.max(
                Math.abs(impact.getX() - start.getX()),
                Math.abs(impact.getZ() - start.getZ()));
        int verticalDelta = Math.max(
                -segments,
                Math.min(
                        segments,
                        impact.getY() - start.getY()));
        int reachableY = start.getY() + verticalDelta;
        List<BlockPos> cells = new ArrayList<>();
        for (int step = 0; step <= segments; step++) {
            double progress = segments == 0
                    ? 1.0D
                    : (double) step / segments;
            BlockPos center = new BlockPos(
                    interpolate(start.getX(), impact.getX(), progress),
                    interpolate(start.getY(), reachableY, progress),
                    interpolate(start.getZ(), impact.getZ(), progress));
            for (int x = centeredStart(parameters.width());
                 x <= centeredEnd(parameters.width());
                 x++) {
                cells.add(center.relative(right, x));
            }
        }
        return cells;
    }

    private static List<BlockPos> bridge(
            BlockPos anchor,
            Direction forward,
            Direction right,
            ReassembledParameters parameters
    ) {
        List<BlockPos> cells = new ArrayList<>();
        for (int z = 0; z < parameters.depth(); z++) {
            for (int x = centeredStart(parameters.width());
                 x <= centeredEnd(parameters.width());
                 x++) {
                cells.add(anchor.relative(forward, z).relative(right, x));
            }
        }
        return cells;
    }

    private static List<BlockPos> stair(
            BlockPos anchor,
            Direction forward,
            Direction right,
            ReassembledParameters parameters
    ) {
        List<BlockPos> cells = new ArrayList<>();
        int denominator = Math.max(1, parameters.depth() - 1);
        for (int z = 0; z < parameters.depth(); z++) {
            int y = Math.min(
                    parameters.height() - 1,
                    z * parameters.height() / denominator);
            for (int x = centeredStart(parameters.width());
                 x <= centeredEnd(parameters.width());
                 x++) {
                cells.add(anchor.relative(forward, z)
                        .relative(right, x)
                        .above(y));
            }
        }
        return cells;
    }

    private static List<BlockPos> pillar(
            BlockPos anchor,
            ReassembledParameters parameters
    ) {
        List<BlockPos> cells = new ArrayList<>();
        int radius = parameters.radius();
        for (int y = 0; y < parameters.height(); y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius) {
                        cells.add(anchor.offset(x, y, z));
                    }
                }
            }
        }
        return cells;
    }

    private static int centeredStart(int width) {
        return -(width / 2);
    }

    private static int centeredEnd(int width) {
        return centeredStart(width) + width - 1;
    }

    private static int interpolate(int start, int end, double progress) {
        return start + (int) Math.round((end - start) * progress);
    }

    private static Direction horizontal(Direction direction) {
        return direction.getAxis().isHorizontal()
                ? direction
                : Direction.NORTH;
    }
}
