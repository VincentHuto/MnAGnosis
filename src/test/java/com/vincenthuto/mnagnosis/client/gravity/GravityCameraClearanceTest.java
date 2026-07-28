package com.vincenthuto.mnagnosis.client.gravity;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravityCameraClearanceTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void lowWallCameraMovesOnlyEnoughToClearGround() {
        Vec3 camera = new Vec3(1.8D, 1.30D, 2.5D);
        AABB floor = new AABB(
                0.0D, 0.0D, 0.0D,
                4.0D, 1.0D, 4.0D
        );

        Vec3 resolved = GravityCameraClearance.resolve(
                camera, GravityDirection.WEST, List.of(floor)
        );

        assertEquals(camera.x, resolved.x, EPSILON);
        assertTrue(
                resolved.y - GravityCameraClearance.SAFETY_RADIUS > 1.0D
        );
        assertTrue(resolved.y < 1.351D);
        assertEquals(camera.z, resolved.z, EPSILON);
    }

    @Test
    void highWallCameraRemainsAtAuthoritativeEye() {
        Vec3 camera = new Vec3(1.8D, 2.0D, 2.5D);
        AABB floor = new AABB(
                0.0D, 0.0D, 0.0D,
                4.0D, 1.0D, 4.0D
        );

        assertEquals(camera, GravityCameraClearance.resolve(
                camera, GravityDirection.WEST, List.of(floor)
        ));
    }

    @Test
    void partialCollisionHeightDeterminesClearance() {
        Vec3 camera = new Vec3(1.8D, 0.80D, 2.5D);
        AABB slab = new AABB(
                0.0D, 0.0D, 0.0D,
                4.0D, 0.5D, 4.0D
        );

        Vec3 resolved = GravityCameraClearance.resolve(
                camera, GravityDirection.WEST, List.of(slab)
        );

        assertTrue(
                resolved.y - GravityCameraClearance.SAFETY_RADIUS > 0.5D
        );
        assertTrue(resolved.y < 0.851D);
    }

    @Test
    void resolverNeverMovesAlongActiveGravityAxis() {
        Vec3 camera = Vec3.ZERO;
        AABB obstacle = new AABB(
                0.30D, -1.0D, -1.0D,
                0.40D, 1.0D, 1.0D
        );

        assertEquals(camera, GravityCameraClearance.resolve(
                camera, GravityDirection.WEST, List.of(obstacle)
        ));
    }

    @Test
    void cameraOnSupportPlaneEscapesOnlyAwayFromWall() {
        Vec3 camera = Vec3.ZERO;
        AABB wall = new AABB(
                0.0D, -2.0D, -2.0D,
                1.0D, 2.0D, 2.0D
        );

        Vec3 resolved = GravityCameraClearance.resolve(
                camera, GravityDirection.EAST, List.of(wall)
        );

        assertTrue(
                resolved.x + GravityCameraClearance.SAFETY_RADIUS < 0.0D
        );
        assertEquals(0.0D, resolved.y, EPSILON);
        assertEquals(0.0D, resolved.z, EPSILON);
        assertTrue(
                resolved.subtract(camera).dot(
                        GravityDirection.EAST.downVector()
                ) < 0.0D
        );
    }

    @Test
    void excessiveCorrectionPreservesAuthoritativeEye() {
        Vec3 camera = Vec3.ZERO;
        AABB enclosingBlock = new AABB(
                -1.0D, -1.0D, -1.0D,
                1.0D, 1.0D, 1.0D
        );

        assertEquals(camera, GravityCameraClearance.resolve(
                camera, GravityDirection.NORTH, List.of(enclosingBlock)
        ));
    }
}
