package com.vincenthuto.mnagnosis.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FractalItemEntityRendererContractTest {

    @Test
    void rendererAcceptsVanillaCopiesUsedByPickupParticles()
            throws Exception {
        assertNotNull(
                FractalItemEntityRenderer.class.getDeclaredMethod(
                        "render",
                        ItemEntity.class,
                        float.class,
                        float.class,
                        PoseStack.class,
                        MultiBufferSource.class,
                        int.class
                )
        );
    }
}
