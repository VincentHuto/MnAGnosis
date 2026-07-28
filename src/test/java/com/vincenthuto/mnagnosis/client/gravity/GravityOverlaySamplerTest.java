package com.vincenthuto.mnagnosis.client.gravity;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravityOverlaySamplerTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void onlyShiftedGravityReplacesVanillaOverlaySampling() {
        assertFalse(GravityOverlaySampler.shouldUseGravitySampling(
                GravityDirection.DOWN
        ));
        assertTrue(GravityOverlaySampler.shouldUseGravitySampling(
                GravityDirection.WEST
        ));
        assertTrue(GravityOverlaySampler.shouldUseGravitySampling(
                GravityDirection.UP
        ));
    }

    @Test
    void wallSamplesRotateTheVanillaEyeVolumeOutOfWorldUp() {
        List<Vec3> samples = GravityOverlaySampler.samplePositions(
                new Vec3(10.0D, 20.0D, 30.0D),
                0.6D,
                GravityDirection.WEST
        );

        assertEquals(8, samples.size());
        assertVecEquals(
                new Vec3(9.95D, 20.24D, 30.24D),
                samples.get(0)
        );
        assertVecEquals(
                new Vec3(10.05D, 19.76D, 29.76D),
                samples.get(7)
        );
        assertTrue(samples.stream().allMatch(sample ->
                Math.abs(sample.y - 20.0D) <= 0.2400001D));
    }

    @Test
    void worldDownSamplesMatchVanillaCoordinateOffsets() {
        List<Vec3> samples = GravityOverlaySampler.samplePositions(
                Vec3.ZERO,
                0.6D,
                GravityDirection.DOWN
        );

        assertVecEquals(
                new Vec3(-0.24D, -0.05D, -0.24D),
                samples.get(0)
        );
        assertVecEquals(
                new Vec3(0.24D, 0.05D, 0.24D),
                samples.get(7)
        );
    }

    private static void assertVecEquals(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }
}
