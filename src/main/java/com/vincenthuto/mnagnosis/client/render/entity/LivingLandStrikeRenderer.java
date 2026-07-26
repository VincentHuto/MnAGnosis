package com.vincenthuto.mnagnosis.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.mnagnosis.common.entity.LivingLandStrikeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public final class LivingLandStrikeRenderer extends EntityRenderer<LivingLandStrikeEntity> {

    private final EntityRendererProvider.Context context;

    public LivingLandStrikeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.context = context;
        shadowRadius = 0.35F;
    }

    @Override
    public void render(LivingLandStrikeEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        context.getBlockRenderDispatcher().renderSingleBlock(
                entity.getCarriedState(), poseStack, buffers,
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingLandStrikeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
