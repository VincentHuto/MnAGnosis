package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Resolves collision in gravity-local axis order while keeping boxes and the
 * public motion vector in world coordinates.
 */
public final class GravityCollisionSolver {

    private static final double COLLISION_EPSILON = 1.0E-7D;

    private GravityCollisionSolver() {
    }

    public static GravityMoveResult solve(
            AABB box,
            Vec3 requestedWorld,
            GravityDirection gravity,
            double stepHeight,
            boolean wasGrounded,
            List<VoxelShape> collisions
    ) {
        Vec3 requestedLocal = gravity.toLocal(requestedWorld);
        Vec3 actualLocal = clip(box, requestedLocal, gravity, collisions);
        boolean vertical = clipped(requestedLocal.y, actualLocal.y);
        boolean horizontal = clipped(requestedLocal.x, actualLocal.x)
                || clipped(requestedLocal.z, actualLocal.z);
        boolean grounded = requestedLocal.y < 0.0D && vertical;
        boolean stepped = false;

        if (stepHeight > 0.0D && (wasGrounded || grounded) && horizontal) {
            Vec3 step = step(box, requestedLocal, gravity, stepHeight, collisions);
            if (horizontalDistanceSquared(step)
                    > horizontalDistanceSquared(actualLocal) + COLLISION_EPSILON) {
                actualLocal = step;
                vertical = clipped(requestedLocal.y, actualLocal.y);
                horizontal = clipped(requestedLocal.x, actualLocal.x)
                        || clipped(requestedLocal.z, actualLocal.z);
                grounded = requestedLocal.y < 0.0D && vertical;
                stepped = true;
            }
        }

        return new GravityMoveResult(
                requestedWorld,
                gravity.toWorld(actualLocal),
                requestedLocal,
                actualLocal,
                grounded,
                vertical,
                horizontal,
                stepped
        );
    }

    private static Vec3 step(
            AABB box,
            Vec3 requested,
            GravityDirection gravity,
            double stepHeight,
            List<VoxelShape> collisions
    ) {
        Vec3 raised = clip(box, new Vec3(0.0D, stepHeight, 0.0D),
                gravity, collisions);
        AABB raisedBox = box.move(gravity.toWorld(raised));
        Vec3 across = clip(raisedBox,
                new Vec3(requested.x, 0.0D, requested.z),
                gravity, collisions);
        AABB acrossBox = raisedBox.move(gravity.toWorld(across));
        Vec3 settled = clip(acrossBox,
                new Vec3(0.0D, requested.y - raised.y, 0.0D),
                gravity, collisions);
        return raised.add(across).add(settled);
    }

    private static Vec3 clip(
            AABB box,
            Vec3 local,
            GravityDirection gravity,
            List<VoxelShape> collisions
    ) {
        double y = clipAxis(box, local.y, gravity, Direction.UP, collisions);
        box = box.move(gravity.toWorld(new Vec3(0.0D, y, 0.0D)));

        double x = local.x;
        double z = local.z;
        boolean zFirst = Math.abs(x) < Math.abs(z);
        if (zFirst) {
            z = clipAxis(box, z, gravity, Direction.SOUTH, collisions);
            box = box.move(gravity.toWorld(new Vec3(0.0D, 0.0D, z)));
        }

        x = clipAxis(box, x, gravity, Direction.EAST, collisions);
        if (!zFirst) {
            box = box.move(gravity.toWorld(new Vec3(x, 0.0D, 0.0D)));
            z = clipAxis(box, z, gravity, Direction.SOUTH, collisions);
        }
        return new Vec3(x, y, z);
    }

    private static double clipAxis(
            AABB box,
            double localAmount,
            GravityDirection gravity,
            Direction localPositive,
            List<VoxelShape> collisions
    ) {
        if (localAmount == 0.0D || collisions.isEmpty()) {
            return localAmount;
        }
        Vec3 worldBasis = gravity.toWorld(Vec3.atLowerCornerOf(
                localPositive.getNormal()
        ));
        Direction worldDirection = Direction.getNearest(
                worldBasis.x, worldBasis.y, worldBasis.z
        );
        double basisSign = worldDirection.getAxisDirection().getStep();
        double worldAmount = localAmount * basisSign;
        double clippedWorld = Shapes.collide(
                worldDirection.getAxis(), box, collisions, worldAmount
        );
        return clippedWorld * basisSign;
    }

    private static double component(Vec3 vector, Direction.Axis axis) {
        return switch (axis) {
            case X -> vector.x;
            case Y -> vector.y;
            case Z -> vector.z;
        };
    }

    private static boolean clipped(double requested, double actual) {
        return Math.abs(requested - actual) > COLLISION_EPSILON;
    }

    private static double horizontalDistanceSquared(Vec3 local) {
        return local.x * local.x + local.z * local.z;
    }
}
