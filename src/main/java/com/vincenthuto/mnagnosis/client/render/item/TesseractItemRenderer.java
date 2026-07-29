package com.vincenthuto.mnagnosis.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TesseractItemRenderer extends BlockEntityWithoutLevelRenderer {

    public TesseractItemRenderer(
            BlockEntityRenderDispatcher dispatcher,
            EntityModelSet models
    ) {
        super(dispatcher, models);
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        float scale = switch (displayContext) {
            case GUI -> 0.82F;
            case GROUND -> 0.66F;
            case FIXED -> 0.78F;
            default -> 0.72F;
        };
        poseStack.scale(scale, scale, scale);

        long time = System.currentTimeMillis();
        float elapsedSeconds = (time % 240_000L) / 1_000.0F;
        float angleXw = (time % 10_000L) / 10_000.0F
                * (float) Math.PI * 2.0F;
        float angleYz = (time % 7_000L) / 7_000.0F
                * (float) Math.PI * 2.0F;
        float pulse = (float) (
                Math.sin(elapsedSeconds * 2.4F) * 0.5F + 0.5F
        );
        TesseractRenderCore.renderShader(
                poseStack,
                buffer,
                elapsedSeconds,
                angleXw,
                angleYz,
                pulse,
                1.0F
        );
        poseStack.popPose();
    }
}
