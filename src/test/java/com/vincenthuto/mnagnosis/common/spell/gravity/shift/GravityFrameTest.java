package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GravityFrameTest {

    @Test
    void anchoredWallBoxKeepsItsFeetOnTheRequestedPlane() {
        AABB previous = new AABB(4.7D, 2.0D, 6.7D,
                5.3D, 3.8D, 7.3D);

        Vec3 anchor = GravityFrame.anchor(previous, GravityDirection.EAST);
        AABB wall = GravityFrame.anchoredBox(
                anchor, 0.6F, 1.8F, GravityDirection.EAST
        );

        assertEquals(previous.maxX, anchor.x, 1.0E-9D);
        assertEquals(previous.maxX, wall.maxX, 1.0E-6D);
        assertEquals(1.8D, wall.getXsize(), 1.0E-6D);
    }
}
