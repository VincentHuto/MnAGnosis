package com.vincenthuto.mnagnosis.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.mnagnosis.common.entity.LivingLandStrikeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

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
        Vec3 axis = entity.flightAxis();
        float axisYaw = (float) Math.toDegrees(Math.atan2(axis.x, axis.z));
        float pitch = (float) -Math.toDegrees(Math.asin(axis.y));
        poseStack.mulPose(Axis.YP.rotationDegrees(axisYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        double center = (entity.getPayloadLength() - 1) * 0.5D;
        for (int index = 0; index < entity.getPayloadLength(); index++) {
            poseStack.pushPose();
            poseStack.translate(-0.5D, -0.5D, index - center - 0.5D);
            if (entity.isProjected()) {
                poseStack.pushPose();
                poseStack.translate(0.5D, 0.5D, 0.5D);
                poseStack.scale(1.06F, 1.06F, 1.06F);
                poseStack.translate(-0.5D, -0.5D, -0.5D);
                context.getBlockRenderDispatcher().renderSingleBlock(
                        (index & 1) == 0
                                ? Blocks.BLACK_CONCRETE.defaultBlockState()
                                : Blocks.WHITE_CONCRETE.defaultBlockState(),
                        poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
                poseStack.translate(0.07D, 0.07D, 0.07D);
                poseStack.scale(0.86F, 0.86F, 0.86F);
            }
            context.getBlockRenderDispatcher().renderSingleBlock(
                    entity.getCarriedState(index), poseStack, buffers,
                    packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingLandStrikeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
