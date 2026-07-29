package com.vincenthuto.mnagnosis.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.mnagnosis.client.render.MengerianTopologyRenderCore;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class MengerianTopologyItemRenderer
        extends BlockEntityWithoutLevelRenderer {

    public MengerianTopologyItemRenderer(
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
            MultiBufferSource buffers,
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
        poseStack.mulPose(Axis.YP.rotationDegrees(
                (time % 18_000L) / 50.0F
        ));
        poseStack.mulPose(Axis.XP.rotationDegrees(
                18.0F + (time % 24_000L) / 400.0F
        ));
        float elapsedSeconds = (time % 3_600_000L) / 1_000.0F;
        MengerianTopologyRenderCore.render(
                poseStack,
                buffers,
                1.0F,
                elapsedSeconds
        );
        poseStack.popPose();
    }
}
