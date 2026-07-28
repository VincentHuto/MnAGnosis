package com.vincenthuto.mnagnosis.client.gravity;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class GravityCameraClearance {

    public static final double SAFETY_RADIUS = 0.35D;
    public static final double MAX_CORRECTION = 0.45D;
    private static final double SEPARATION_EPSILON = 1.0E-5D;

    private GravityCameraClearance() {
    }

    public static Vec3 resolve(
            Vec3 cameraPosition,
            GravityDirection gravity,
            Iterable<AABB> collisionBoxes
    ) {
        return resolve(
                cameraPosition,
                gravity,
                SAFETY_RADIUS,
                MAX_CORRECTION,
                collisionBoxes
        );
    }

    public static Vec3 resolve(
            CollisionGetter level,
            Entity entity,
            Vec3 cameraPosition,
            GravityDirection gravity
    ) {
        AABB search = new AABB(
                cameraPosition, cameraPosition
        ).inflate(SAFETY_RADIUS + MAX_CORRECTION);
        List<AABB> collisionBoxes = new ArrayList<>();
        for (VoxelShape shape : level.getBlockCollisions(entity, search)) {
            collisionBoxes.addAll(shape.toAabbs());
        }
        return resolve(cameraPosition, gravity, collisionBoxes);
    }

    static Vec3 resolve(
            Vec3 cameraPosition,
            GravityDirection gravity,
            double safetyRadius,
            double maximumCorrection,
            Iterable<AABB> collisionBoxes
    ) {
        AABB envelope = new AABB(
                cameraPosition, cameraPosition
        ).inflate(safetyRadius);
        AABB search = envelope.inflate(maximumCorrection);
        List<AABB> obstacles = new ArrayList<>();
        for (AABB collisionBox : collisionBoxes) {
            if (collisionBox.intersects(search)) {
                obstacles.add(collisionBox);
            }
        }
        if (obstacles.stream().noneMatch(envelope::intersects)) {
            return cameraPosition;
        }

        Direction.Axis[] tangentAxes = tangentAxes(gravity);
        Direction.Axis normalAxis = gravity.down().getAxis();
        List<Double> firstCandidates = candidates(
                envelope, obstacles, tangentAxes[0]
        );
        List<Double> secondCandidates = candidates(
                envelope, obstacles, tangentAxes[1]
        );
        List<Double> normalCandidates = candidates(
                envelope, obstacles, normalAxis
        );
        Vec3 best = null;
        double bestDistanceSquared = Double.POSITIVE_INFINITY;
        double maximumDistanceSquared =
                maximumCorrection * maximumCorrection;

        for (double first : firstCandidates) {
            for (double second : secondCandidates) {
                for (double normal : normalCandidates) {
                    Vec3 normalDisplacement = displacement(
                            normalAxis, normal
                    );
                    if (normalDisplacement.dot(gravity.downVector())
                            > SEPARATION_EPSILON) {
                        continue;
                    }
                    Vec3 displacement = displacement(
                            tangentAxes[0], first, tangentAxes[1], second
                    ).add(normalDisplacement);
                    double distanceSquared = displacement.lengthSqr();
                    if (distanceSquared > maximumDistanceSquared
                            || distanceSquared >= bestDistanceSquared) {
                        continue;
                    }
                    AABB moved = envelope.move(displacement);
                    if (obstacles.stream().noneMatch(moved::intersects)) {
                        best = displacement;
                        bestDistanceSquared = distanceSquared;
                    }
                }
            }
        }
        return best == null ? cameraPosition : cameraPosition.add(best);
    }

    private static Direction.Axis[] tangentAxes(GravityDirection gravity) {
        return switch (gravity.down().getAxis()) {
            case X -> new Direction.Axis[]{
                    Direction.Axis.Y, Direction.Axis.Z
            };
            case Y -> new Direction.Axis[]{
                    Direction.Axis.X, Direction.Axis.Z
            };
            case Z -> new Direction.Axis[]{
                    Direction.Axis.X, Direction.Axis.Y
            };
        };
    }

    private static List<Double> candidates(
            AABB envelope,
            List<AABB> obstacles,
            Direction.Axis axis
    ) {
        List<Double> candidates = new ArrayList<>();
        candidates.add(0.0D);
        for (AABB obstacle : obstacles) {
            candidates.add(min(obstacle, axis) - max(envelope, axis)
                    - SEPARATION_EPSILON);
            candidates.add(max(obstacle, axis) - min(envelope, axis)
                    + SEPARATION_EPSILON);
        }
        candidates.sort(Comparator
                .comparingDouble((Double value) -> Math.abs(value))
                .thenComparingDouble(Double::doubleValue));
        return candidates;
    }

    private static Vec3 displacement(
            Direction.Axis firstAxis,
            double first,
            Direction.Axis secondAxis,
            double second
    ) {
        double x = 0.0D;
        double y = 0.0D;
        double z = 0.0D;
        switch (firstAxis) {
            case X -> x = first;
            case Y -> y = first;
            case Z -> z = first;
        }
        switch (secondAxis) {
            case X -> x = second;
            case Y -> y = second;
            case Z -> z = second;
        }
        return new Vec3(x, y, z);
    }

    private static Vec3 displacement(
            Direction.Axis axis,
            double amount
    ) {
        return switch (axis) {
            case X -> new Vec3(amount, 0.0D, 0.0D);
            case Y -> new Vec3(0.0D, amount, 0.0D);
            case Z -> new Vec3(0.0D, 0.0D, amount);
        };
    }

    private static double min(AABB box, Direction.Axis axis) {
        return switch (axis) {
            case X -> box.minX;
            case Y -> box.minY;
            case Z -> box.minZ;
        };
    }

    private static double max(AABB box, Direction.Axis axis) {
        return switch (axis) {
            case X -> box.maxX;
            case Y -> box.maxY;
            case Z -> box.maxZ;
        };
    }
}
