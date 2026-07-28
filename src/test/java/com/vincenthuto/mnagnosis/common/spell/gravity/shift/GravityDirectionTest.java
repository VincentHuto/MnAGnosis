package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GravityDirectionTest {

    private static final double EPSILON = 1.0E-6D;

    @Test
    void controlsUseCanonicalPlayerToWorldFrames() {
        assertFrame(GravityDirection.DOWN,
                new Vec3(1, 0, 0), new Vec3(0, 1, 0), new Vec3(0, 0, 1));
        assertFrame(GravityDirection.UP,
                new Vec3(-1, 0, 0), new Vec3(0, -1, 0), new Vec3(0, 0, 1));
        assertFrame(GravityDirection.NORTH,
                new Vec3(1, 0, 0), new Vec3(0, 0, 1), new Vec3(0, -1, 0));
        assertFrame(GravityDirection.SOUTH,
                new Vec3(-1, 0, 0), new Vec3(0, 0, -1), new Vec3(0, -1, 0));
        assertFrame(GravityDirection.WEST,
                new Vec3(0, 0, -1), new Vec3(1, 0, 0), new Vec3(0, -1, 0));
        assertFrame(GravityDirection.EAST,
                new Vec3(0, 0, 1), new Vec3(-1, 0, 0), new Vec3(0, -1, 0));
    }

    @Test
    void cameraQuaternionAndControlFrameTransformAxesIdentically() {
        for (GravityDirection gravity : GravityDirection.values()) {
            assertQuaternionAxis(gravity, new Vec3(1, 0, 0));
            assertQuaternionAxis(gravity, new Vec3(0, 1, 0));
            assertQuaternionAxis(gravity, new Vec3(0, 0, 1));
        }
    }

    private static void assertFrame(
            GravityDirection gravity,
            Vec3 right,
            Vec3 up,
            Vec3 forward
    ) {
        assertVec(right, gravity.toWorld(new Vec3(1, 0, 0)));
        assertVec(up, gravity.toWorld(new Vec3(0, 1, 0)));
        assertVec(forward, gravity.toWorld(new Vec3(0, 0, 1)));
    }

    private static void assertQuaternionAxis(
            GravityDirection gravity,
            Vec3 local
    ) {
        Vector3f transformed = gravity.rotation().transform(local.toVector3f());
        assertVec(
                gravity.toWorld(local),
                new Vec3(transformed.x(), transformed.y(), transformed.z()),
                gravity + " local=" + local
        );
    }

    private static void assertVec(Vec3 expected, Vec3 actual) {
        assertVec(expected, actual, "");
    }

    private static void assertVec(Vec3 expected, Vec3 actual, String message) {
        assertEquals(expected.x, actual.x, EPSILON, message);
        assertEquals(expected.y, actual.y, EPSILON, message);
        assertEquals(expected.z, actual.z, EPSILON, message);
    }
}
