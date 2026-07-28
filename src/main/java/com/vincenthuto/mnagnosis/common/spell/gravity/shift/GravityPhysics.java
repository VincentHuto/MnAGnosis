package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.world.phys.Vec3;

public final class GravityPhysics {

    private static final double SURFACE_STATIC_GRIP_SPEED = 0.2D;

    private GravityPhysics() {
    }

    public static Vec3 applyGravity(
            Vec3 velocity,
            GravityDirection gravity,
            double acceleration
    ) {
        return velocity.add(gravity.downVector().scale(acceleration));
    }

    public static Vec3 jump(
            Vec3 velocity,
            GravityDirection gravity,
            double jumpPower
    ) {
        return velocity.add(gravity.upVector().scale(jumpPower));
    }

    public static Vec3 eyePosition(
            Vec3 feetPosition,
            double eyeHeight,
            GravityDirection gravity
    ) {
        return feetPosition.add(gravity.upVector().scale(eyeHeight));
    }

    public static Vec3 transitionVelocity(
            Vec3 velocity,
            GravityDirection gravity
    ) {
        return transitionVelocity(
                velocity,
                GravityDirection.DOWN,
                gravity,
                false
        );
    }

    public static Vec3 transitionVelocity(
            Vec3 velocity,
            GravityDirection previous,
            GravityDirection current,
            boolean discardPreviousGravityAxis
    ) {
        if (current == GravityDirection.DOWN) {
            return velocity;
        }
        Vec3 transitioned = withoutAxis(velocity, current.downVector());
        if (discardPreviousGravityAxis) {
            transitioned = withoutAxis(
                    transitioned, previous.downVector()
            );
        }
        return transitioned;
    }

    private static Vec3 withoutAxis(Vec3 velocity, Vec3 axis) {
        return velocity.subtract(axis.scale(velocity.dot(axis)));
    }

    public static Vec3 remapVanillaGravity(
            Vec3 velocityAfterVanilla,
            GravityDirection gravity,
            double gravityAfterDrag
    ) {
        return velocityAfterVanilla
                .add(0.0D, gravityAfterDrag, 0.0D)
                .add(gravity.downVector().scale(gravityAfterDrag));
    }

    public static Vec3 remapVanillaTravel(
            Vec3 velocityAfterVanilla,
            GravityDirection gravity,
            double gravityAcceleration,
            double horizontalDrag,
            double verticalDrag
    ) {
        if (!validDrag(horizontalDrag) || !validDrag(verticalDrag)
                || !Double.isFinite(gravityAcceleration)) {
            return velocityAfterVanilla;
        }
        Vec3 beforeVanillaForces = new Vec3(
                velocityAfterVanilla.x / horizontalDrag,
                velocityAfterVanilla.y / verticalDrag
                        + gravityAcceleration,
                velocityAfterVanilla.z / horizontalDrag
        );
        Vec3 local = gravity.toLocal(beforeVanillaForces);
        return gravity.toWorld(new Vec3(
                local.x * horizontalDrag,
                (local.y - gravityAcceleration) * verticalDrag,
                local.z * horizontalDrag
        ));
    }

    public static boolean shouldRemapVanillaTravel(
            GravityDirection gravity,
            boolean ordinaryDryTravel,
            boolean vanillaGravityApplied
    ) {
        return gravity != GravityDirection.DOWN
                && ordinaryDryTravel
                && vanillaGravityApplied;
    }

    public static double vanillaHorizontalDrag(
            boolean discardFriction,
            boolean onGround,
            float blockFriction
    ) {
        if (discardFriction) {
            return 1.0D;
        }
        return (double) (onGround
                ? blockFriction * 0.91F
                : 0.91F);
    }

    public static Vec3 applySurfaceStaticGrip(
            Vec3 velocity,
            GravityDirection gravity,
            boolean idle
    ) {
        if (!idle) {
            return velocity;
        }
        Vec3 local = gravity.toLocal(velocity);
        double tangentSpeedSquared =
                local.x * local.x + local.z * local.z;
        if (tangentSpeedSquared
                > SURFACE_STATIC_GRIP_SPEED * SURFACE_STATIC_GRIP_SPEED) {
            return velocity;
        }
        return gravity.toWorld(new Vec3(
                0.0D, local.y, 0.0D
        ));
    }

    public static Vec3 applySurfaceControlGrip(
            Vec3 velocity,
            GravityDirection gravity,
            Vec3 intendedLocalMovement
    ) {
        Vec3 local = gravity.toLocal(velocity);
        double tangentSpeedSquared =
                local.x * local.x + local.z * local.z;
        if (tangentSpeedSquared
                > SURFACE_STATIC_GRIP_SPEED * SURFACE_STATIC_GRIP_SPEED) {
            return velocity;
        }

        double intendedLengthSquared =
                intendedLocalMovement.x * intendedLocalMovement.x
                        + intendedLocalMovement.z
                        * intendedLocalMovement.z;
        if (intendedLengthSquared < 1.0E-7D) {
            return gravity.toWorld(new Vec3(
                    0.0D, local.y, 0.0D
            ));
        }

        double inverseLength = 1.0D / Math.sqrt(intendedLengthSquared);
        double intendedX = intendedLocalMovement.x * inverseLength;
        double intendedZ = intendedLocalMovement.z * inverseLength;
        double alignedSpeed = Math.max(
                0.0D,
                local.x * intendedX + local.z * intendedZ
        );
        return gravity.toWorld(new Vec3(
                intendedX * alignedSpeed,
                local.y,
                intendedZ * alignedSpeed
        ));
    }

    public static Vec3 applyMissingTangentialDrag(
            Vec3 velocity,
            GravityDirection gravity,
            double correctionFactor
    ) {
        Vec3 local = gravity.toLocal(velocity);
        double x = local.x;
        double z = local.z;
        if (Math.abs(gravity.toWorld(new Vec3(1.0D, 0.0D, 0.0D)).y)
                > 0.5D) {
            x = snap(x * correctionFactor);
        }
        if (Math.abs(gravity.toWorld(new Vec3(0.0D, 0.0D, 1.0D)).y)
                > 0.5D) {
            z = snap(z * correctionFactor);
        }
        return gravity.toWorld(new Vec3(x, local.y, z));
    }

    private static double snap(double value) {
        return Math.abs(value) < 1.0E-4D ? 0.0D : value;
    }

    private static boolean validDrag(double drag) {
        return Double.isFinite(drag) && drag > 1.0E-9D;
    }
}
