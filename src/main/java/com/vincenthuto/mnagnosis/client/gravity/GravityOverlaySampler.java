package com.vincenthuto.mnagnosis.client.gravity;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class GravityOverlaySampler {

    private static final int SAMPLE_COUNT = 8;

    private GravityOverlaySampler() {
    }

    public static boolean shouldUseGravitySampling(
            GravityDirection gravity
    ) {
        return gravity != GravityDirection.DOWN;
    }

    public static List<Vec3> samplePositions(
            Vec3 eye,
            double playerWidth,
            GravityDirection gravity
    ) {
        List<Vec3> samples = new ArrayList<>(SAMPLE_COUNT);
        for (int index = 0; index < SAMPLE_COUNT; index++) {
            double localX = (((index >> 0) % 2) - 0.5D)
                    * playerWidth * 0.8D;
            double localY = (((index >> 1) % 2) - 0.5D) * 0.1D;
            double localZ = (((index >> 2) % 2) - 0.5D)
                    * playerWidth * 0.8D;
            samples.add(eye.add(gravity.toWorld(
                    new Vec3(localX, localY, localZ)
            )));
        }
        return List.copyOf(samples);
    }
}
