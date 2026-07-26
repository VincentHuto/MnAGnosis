package com.vincenthuto.mnagnosis.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TesseractItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final TesseractRenderCore.LineColor INNER =
            new TesseractRenderCore.LineColor(0.1F, 0.5F, 1.0F, 1.0F);
    private static final TesseractRenderCore.LineColor OUTER =
            new TesseractRenderCore.LineColor(0.5F, 0.9F, 1.0F, 1.0F);

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
        poseStack.scale(0.25F, 0.25F, 0.25F);

        long time = System.currentTimeMillis();
        float angleXw = (time % 10_000L) / 10_000.0F
                * (float) Math.PI * 2.0F;
        float angleYz = (time % 7_000L) / 7_000.0F
                * (float) Math.PI * 2.0F;
        TesseractRenderCore.renderEdges(
                poseStack,
                buffer,
                TesseractRenderCore.project(angleXw, angleYz),
                INNER,
                OUTER
        );
        poseStack.popPose();
    }
}
