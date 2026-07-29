package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravityTransitionFrameTest {

    @Test
    void sixTickAnchorPathStartsAndEndsExactly() {
        Vec3 origin = new Vec3(1.0D, 2.0D, 3.0D);
        Vec3 target = new Vec3(5.0D, 4.0D, -1.0D);

        assertEquals(origin, GravityTransitionFrame.anchor(
                origin, target, 6, 0.0F
        ));
        assertEquals(target, GravityTransitionFrame.anchor(
                origin, target, 0, 0.0F
        ));
        assertEquals(new Vec3(3.0D, 3.0D, 1.0D),
                GravityTransitionFrame.anchor(
                        origin, target, 3, 0.0F
                ));
    }

    @Test
    void floorToWallRotationHasContinuousMidpoint() {
        Quaternionf midpoint = GravityTransitionFrame.rotation(
                GravityDirection.DOWN.rotation(),
                GravityDirection.EAST,
                3,
                0.0F
        );
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F)
                .rotate(midpoint);

        assertEquals(-2.0F / 3.0F, up.x(), 1.0E-5F);
        assertEquals(2.0F / 3.0F, up.y(), 1.0E-5F);
        assertEquals(1.0F / 3.0F, up.z(), 1.0E-5F);
    }

    @Test
    void blendedControlNeverDrivesIntoAuthoritativeWall() {
        Quaternionf visual = GravityTransitionFrame.rotation(
                GravityDirection.DOWN.rotation(),
                GravityDirection.EAST,
                3,
                0.0F
        );
        Vec3 movement = GravityTransitionFrame.control(
                new Vec3(1.0D, 0.0D, 0.0D),
                visual,
                GravityDirection.EAST,
                true
        );

        assertEquals(0.0D, movement.dot(
                GravityDirection.EAST.downVector()
        ), 1.0E-9D);
        assertTrue(movement.lengthSqr() > 0.0D);
        assertTrue(movement.lengthSqr() < 1.0D);
    }

    @Test
    void completedControlMatchesDiscreteGravityFrame() {
        Vec3 input = new Vec3(0.3D, 0.0D, 0.8D);
        Vec3 expected = GravityDirection.NORTH.toWorld(input);
        Vec3 actual = GravityTransitionFrame.control(
                input,
                GravityDirection.NORTH.rotation(),
                GravityDirection.NORTH,
                false
        );

        assertEquals(expected.x, actual.x, 1.0E-9D);
        assertEquals(expected.y, actual.y, 1.0E-9D);
        assertEquals(expected.z, actual.z, 1.0E-9D);
    }

    @Test
    void floorToWallControlsRemainTangentAndDoNotReverse() {
        Vec3 input = new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 previous = null;
        Vec3 current = Vec3.ZERO;

        for (int remaining = 6; remaining >= 0; remaining--) {
            current = GravityTransitionFrame.control(
                    input,
                    GravityTransitionFrame.rotation(
                            GravityDirection.DOWN.rotation(),
                            GravityDirection.EAST,
                            remaining,
                            0.0F
                    ),
                    GravityDirection.EAST,
                    remaining > 0
            );
            assertEquals(0.0D, current.dot(
                    GravityDirection.EAST.downVector()
            ), 1.0E-9D);
            if (previous != null) {
                assertTrue(previous.dot(current) >= 0.0D);
            }
            previous = current;
        }

        Vec3 expected = GravityDirection.EAST.toWorld(input);
        assertEquals(expected.x, current.x, 1.0E-9D);
        assertEquals(expected.y, current.y, 1.0E-9D);
        assertEquals(expected.z, current.z, 1.0E-9D);
    }
}
