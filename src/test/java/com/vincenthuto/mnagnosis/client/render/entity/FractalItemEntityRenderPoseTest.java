package com.vincenthuto.mnagnosis.client.render.entity;

import com.vincenthuto.mnagnosis.common.entity.item.FractalItemEntityTraits;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FractalItemEntityRenderPoseTest {

    @Test
    void staticTraitsUseGroundHeightWithoutMotion() {
        FractalItemEntityRenderPose pose =
                FractalItemEntityRenderPose.from(
                        FractalItemEntityTraits.STATIC,
                        400.0F,
                        0.5F
                );

        assertEquals(0.125F, pose.verticalTranslation(), 0.00001F);
        assertEquals(0.0F, pose.yawRadians(), 0.00001F);
        assertEquals(1.0F, pose.scale(), 0.00001F);
        assertFalse(pose.fullBright());
    }

    @Test
    void customTraitsDriveTheEntityTransform() {
        FractalItemEntityTraits traits =
                new FractalItemEntityTraits(
                        0.0F,
                        0.0F,
                        0.02F,
                        0.1F,
                        1.5F,
                        0.2F,
                        true
                );

        FractalItemEntityRenderPose pose =
                FractalItemEntityRenderPose.from(
                        traits,
                        20.0F,
                        1.0F
                );

        assertEquals(0.45F, pose.verticalTranslation(), 0.00001F);
        assertEquals(0.5F, pose.yawRadians(), 0.00001F);
        assertEquals(1.5F, pose.scale(), 0.00001F);
        assertTrue(pose.fullBright());
    }
}
