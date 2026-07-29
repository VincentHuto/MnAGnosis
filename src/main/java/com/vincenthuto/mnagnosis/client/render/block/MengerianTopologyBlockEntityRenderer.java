package com.vincenthuto.mnagnosis.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.mnagnosis.client.render.MengerianTopologyRenderCore;
import com.vincenthuto.mnagnosis.common.block.entity.MengerianTopologyBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class MengerianTopologyBlockEntityRenderer
        implements BlockEntityRenderer<MengerianTopologyBlockEntity> {

    public MengerianTopologyBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            MengerianTopologyBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        long gameTime = blockEntity.getLevel() == null
                ? 0L
                : blockEntity.getLevel().getGameTime();
//        poseStack.mulPose(Axis.YP.rotationDegrees(
//                (gameTime + partialTick) * 0.35F
//        ));
    //    poseStack.mulPose(Axis.XP.rotationDegrees(8.0F));
        float elapsedSeconds = (gameTime + partialTick) / 20.0F;
        MengerianTopologyRenderCore.render(
                poseStack,
                buffers,
                1.0F,
                elapsedSeconds
        );
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(
            MengerianTopologyBlockEntity blockEntity
    ) {
        return false;
    }
}
