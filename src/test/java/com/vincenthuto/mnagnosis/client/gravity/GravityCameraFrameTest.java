package com.vincenthuto.mnagnosis.client.gravity;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityMirageMath;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GravityCameraFrameTest {

    private static final float EPSILON = 1.0E-6F;

    @Test
    void neutralFirstPersonCameraUsesTheActiveGravityFrame() {
        assertNeutralFrame(GravityDirection.DOWN,
                0.0F, 1.0F, 0.0F,
                0.0F, 0.0F, 1.0F,
                1.0F, 0.0F, 0.0F);
        assertNeutralFrame(GravityDirection.UP,
                0.0F, -1.0F, 0.0F,
                0.0F, 0.0F, 1.0F,
                -1.0F, 0.0F, 0.0F);
        assertNeutralFrame(GravityDirection.NORTH,
                0.0F, 0.0F, 1.0F,
                0.0F, -1.0F, 0.0F,
                1.0F, 0.0F, 0.0F);
        assertNeutralFrame(GravityDirection.SOUTH,
                0.0F, 0.0F, -1.0F,
                0.0F, -1.0F, 0.0F,
                -1.0F, 0.0F, 0.0F);
        assertNeutralFrame(GravityDirection.WEST,
                1.0F, 0.0F, 0.0F,
                0.0F, -1.0F, 0.0F,
                0.0F, 0.0F, -1.0F);
        assertNeutralFrame(GravityDirection.EAST,
                -1.0F, 0.0F, 0.0F,
                0.0F, -1.0F, 0.0F,
                0.0F, 0.0F, 1.0F);
    }

    @Test
    void resolvedCameraBasisKeepsSurfaceProjectionOnscreen() {
        assertProjection(GravityDirection.DOWN, new Vec3(0.0D, 0.0D, 1.0D));
        assertProjection(GravityDirection.UP, new Vec3(0.0D, 0.0D, 1.0D));
        assertProjection(GravityDirection.NORTH, new Vec3(0.0D, -1.0D, 0.0D));
        assertProjection(GravityDirection.SOUTH, new Vec3(0.0D, -1.0D, 0.0D));
        assertProjection(GravityDirection.WEST, new Vec3(0.0D, -1.0D, 0.0D));
        assertProjection(GravityDirection.EAST, new Vec3(0.0D, -1.0D, 0.0D));
    }

    @Test
    void worldViewRotationIsInverseOfActiveGravityRotation() {
        for (GravityDirection gravity : GravityDirection.values()) {
            org.joml.Quaternionf identity = new org.joml.Quaternionf(
                    gravity.rotation()
            ).mul(GravityCameraFrame.worldViewRotation(gravity.rotation()));
            Vector3f transformed = new Vector3f(0.31F, -0.47F, 0.73F)
                    .rotate(identity);
            assertEquals(0.31F, transformed.x(), EPSILON, gravity.name());
            assertEquals(-0.47F, transformed.y(), EPSILON, gravity.name());
            assertEquals(0.73F, transformed.z(), EPSILON, gravity.name());
        }
    }

    @Test
    void spatialEyeOffsetAlwaysPointsAwayFromTheSupportPlane() {
        assertOffset(GravityDirection.DOWN, 0.0D, 1.62D, 0.0D);
        assertOffset(GravityDirection.UP, 0.0D, -1.62D, 0.0D);
        assertOffset(GravityDirection.NORTH, 0.0D, 0.0D, 1.62D);
        assertOffset(GravityDirection.SOUTH, 0.0D, 0.0D, -1.62D);
        assertOffset(GravityDirection.WEST, 1.62D, 0.0D, 0.0D);
        assertOffset(GravityDirection.EAST, -1.62D, 0.0D, 0.0D);
    }

    @Test
    void thirdPersonOffsetUsesTheFinalRotatedCameraDirection() {
        for (GravityDirection gravity : GravityDirection.values()) {
            GravityCameraFrame.Basis basis = GravityCameraFrame.basis(
                    GravityCameraFrame.cameraRotation(
                            gravity.rotation(),
                            new org.joml.Quaternionf()
                    )
            );

            Vec3 offset = GravityCameraFrame.thirdPersonOffset(basis, 4.0D);
            Vector3f forward = basis.forward();

            assertEquals(-4.0D, offset.dot(new Vec3(
                    forward.x(), forward.y(), forward.z()
            )), EPSILON, gravity.name());
            assertEquals(16.0D, offset.lengthSqr(), EPSILON, gravity.name());
        }
    }

    private static void assertNeutralFrame(
            GravityDirection gravity,
            float upX,
            float upY,
            float upZ,
            float forwardX,
            float forwardY,
            float forwardZ,
            float leftX,
            float leftY,
            float leftZ
    ) {
        GravityCameraFrame.Basis basis = GravityCameraFrame.basis(
                GravityCameraFrame.cameraRotation(
                        gravity.rotation(),
                        new org.joml.Quaternionf()
                )
        );
        Vector3f up = basis.up();
        assertEquals(upX, up.x(), EPSILON, gravity.name());
        assertEquals(upY, up.y(), EPSILON, gravity.name());
        assertEquals(upZ, up.z(), EPSILON, gravity.name());
        Vector3f forward = basis.forward();
        assertEquals(forwardX, forward.x(), EPSILON, gravity.name());
        assertEquals(forwardY, forward.y(), EPSILON, gravity.name());
        assertEquals(forwardZ, forward.z(), EPSILON, gravity.name());
        Vector3f left = basis.left();
        assertEquals(leftX, left.x(), EPSILON, gravity.name());
        assertEquals(leftY, left.y(), EPSILON, gravity.name());
        assertEquals(leftZ, left.z(), EPSILON, gravity.name());
        assertEquals(
                0.0F,
                forward.x() * (float) gravity.downVector().x
                        + forward.y() * (float) gravity.downVector().y
                        + forward.z() * (float) gravity.downVector().z,
                EPSILON,
                gravity.name()
        );
    }

    private static void assertProjection(
            GravityDirection gravity,
            Vec3 expectedForward
    ) {
        GravityCameraFrame.Basis basis = GravityCameraFrame.basis(
                GravityCameraFrame.cameraRotation(
                        gravity.rotation(),
                        new org.joml.Quaternionf()
                )
        );
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(70.0D),
                16.0F / 9.0F,
                0.05F,
                100.0F
        );
        Vector3f visible = GravityMirageMath.projectToNdc(
                expectedForward.scale(4.0D),
                Vec3.ZERO,
                basis.forward(),
                basis.up(),
                basis.left(),
                projection
        );
        assertNotNull(visible, gravity.name());
        assertEquals(0.0F, visible.x(), EPSILON, gravity.name());
        assertEquals(0.0F, visible.y(), EPSILON, gravity.name());
        assertNull(GravityMirageMath.projectToNdc(
                expectedForward.scale(-4.0D),
                Vec3.ZERO,
                basis.forward(),
                basis.up(),
                basis.left(),
                projection
        ), gravity.name());
    }

    private static void assertOffset(
            GravityDirection gravity,
            double x,
            double y,
            double z
    ) {
        Vec3 offset = GravityCameraFrame.spatialOffset(
                new Vec3(0.0D, 1.62D, 0.0D), gravity
        );
        assertEquals(x, offset.x, EPSILON);
        assertEquals(y, offset.y, EPSILON);
        assertEquals(z, offset.z, EPSILON);
        assertEquals(
                -1.62D,
                offset.dot(gravity.downVector()),
                EPSILON
        );
    }
}
