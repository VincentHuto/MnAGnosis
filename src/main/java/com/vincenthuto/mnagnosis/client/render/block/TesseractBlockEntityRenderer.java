package com.vincenthuto.mnagnosis.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.mnagnosis.client.render.item.TesseractRenderCore;
import com.vincenthuto.mnagnosis.common.block.entity.TesseractBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class TesseractBlockEntityRenderer
        implements BlockEntityRenderer<TesseractBlockEntity> {

    public TesseractBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            TesseractBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        long gameTime = blockEntity.getLevel() == null
                ? 0L
                : blockEntity.getLevel().getGameTime();
        float elapsedSeconds = (gameTime + partialTick) / 20.0F;
        TesseractRenderCore.renderShader(
                poseStack,
                buffer,
                elapsedSeconds,
                blockEntity.getRotation1(),
                blockEntity.getRotation2(),
                blockEntity.getPulse(),
                1.0F
        );
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TesseractBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
