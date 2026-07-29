package com.vincenthuto.mnagnosis.client.gravity;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftState;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravitySourceMode;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GravityVisualsTest {

    @Test
    void floorToWallEyeStartsAndEndsAtContinuousPose() {
        GravityShiftState state = new GravityShiftState();
        Vec3 floorAnchor = new Vec3(0.7D, 1.0D, 0.5D);
        Vec3 wallAnchor = new Vec3(1.0D, 1.9D, 0.5D);
        state.resolve(GravitySourceMode.MOBILE, GravityDirection.EAST);
        state.setTransitionOrigin(
                floorAnchor,
                GravityDirection.DOWN.rotation()
        );

        Vec3 first = GravityVisuals.eye(
                state, wallAnchor, 1.62D, 0.0F
        );
        for (int tick = 0; tick < 6; tick++) {
            state.tickClient();
        }
        Vec3 last = GravityVisuals.eye(
                state, wallAnchor, 1.62D, 0.0F
        );

        assertTrue(first.distanceToSqr(
                new Vec3(0.7D, 2.62D, 0.5D)
        ) < 1.0E-10D);
        assertTrue(last.distanceToSqr(
                new Vec3(-0.62D, 1.9D, 0.5D)
        ) < 1.0E-10D);
    }

    @Test
    void everyFloorToWallEyeSampleCanClearBothSurfaces() {
        GravityShiftState state = new GravityShiftState();
        Vec3 floorAnchor = new Vec3(0.7D, 1.0D, 0.5D);
        Vec3 wallAnchor = new Vec3(1.0D, 1.9D, 0.5D);
        AABB floor = new AABB(
                -2.0D, 0.0D, -2.0D,
                2.0D, 1.0D, 2.0D
        );
        AABB wall = new AABB(
                1.0D, 0.0D, -2.0D,
                2.0D, 4.0D, 2.0D
        );
        List<AABB> obstacles = List.of(floor, wall);
        state.resolve(GravitySourceMode.MOBILE, GravityDirection.EAST);
        state.setTransitionOrigin(
                floorAnchor,
                GravityDirection.DOWN.rotation()
        );

        for (int tick = 0; tick <= 6; tick++) {
            Vec3 eye = GravityVisuals.eye(
                    state, wallAnchor, 1.62D, 0.0F
            );
            Vec3 cleared = GravityCameraClearance.resolve(
                    eye, GravityDirection.EAST, obstacles
            );
            AABB envelope = new AABB(
                    cleared, cleared
            ).inflate(GravityCameraClearance.SAFETY_RADIUS);

            assertTrue(!envelope.intersects(floor), "floor at " + tick);
            assertTrue(!envelope.intersects(wall), "wall at " + tick);
            state.tickClient();
        }
    }
}
