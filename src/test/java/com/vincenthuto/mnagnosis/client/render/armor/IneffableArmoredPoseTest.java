package com.vincenthuto.mnagnosis.client.render.armor;

import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class IneffableArmoredPoseTest {

    @Test
    void copiesPoseWhileTranslatingParentPivotDeltasToAuthoredPivot() {
        ModelPart parent = part();
        parent.setPos(-4.25F, 5.2F, 1.5F);
        parent.setRotation(0.4F, 0.5F, 0.6F);
        parent.xScale = 1.15F;
        parent.yScale = 0.9F;
        parent.zScale = 1.25F;
        parent.visible = false;
        ModelPart armored = part();

        IneffableArmoredPose.copyPart(
                parent,
                armored,
                -5.0F,
                2.0F,
                0.0F,
                -6.25F,
                -4.5F,
                0.0F
        );

        assertEquals(-5.5F, armored.x, 0.0001F);
        assertEquals(-1.3F, armored.y, 0.0001F);
        assertEquals(1.5F, armored.z, 0.0001F);
        assertEquals(0.4F, armored.xRot, 0.0001F);
        assertEquals(0.5F, armored.yRot, 0.0001F);
        assertEquals(0.6F, armored.zRot, 0.0001F);
        assertEquals(1.15F, armored.xScale, 0.0001F);
        assertEquals(0.9F, armored.yScale, 0.0001F);
        assertEquals(1.25F, armored.zScale, 0.0001F);
        assertFalse(armored.visible);
    }

    private static ModelPart part() {
        return new ModelPart(List.of(), Map.of());
    }
}
