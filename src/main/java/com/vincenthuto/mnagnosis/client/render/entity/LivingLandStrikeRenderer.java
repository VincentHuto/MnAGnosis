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
import net.minecraft.util.Mth;
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
        Vec3 renderedHead = new Vec3(
                Mth.lerp(partialTick, entity.xOld, entity.getX()),
                Mth.lerp(partialTick, entity.yOld, entity.getY()),
                Mth.lerp(partialTick, entity.zOld, entity.getZ()));
        for (int index = 0; index < entity.getPayloadLength(); index++) {
            Vec3 segment = entity.getSegmentPosition(index, partialTick);
            Vec3 tangent = entity.getSegmentTangent(index, partialTick);
            Vec3 offset = segment.subtract(renderedHead);
            float segmentYaw = (float) Math.toDegrees(Math.atan2(tangent.x, tangent.z));
            float segmentPitch = (float) -Math.toDegrees(Math.asin(tangent.y));
            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y, offset.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(segmentYaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(segmentPitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(
                    (index & 1) == 0 ? 7.0F : -7.0F));
            if (entity.isProjected()) {
                poseStack.pushPose();
                poseStack.scale(1.06F, 1.06F, 1.06F);
                poseStack.translate(-0.5D, -0.5D, -0.5D);
                context.getBlockRenderDispatcher().renderSingleBlock(
                        (index & 1) == 0
                                ? Blocks.BLACK_CONCRETE.defaultBlockState()
                                : Blocks.WHITE_CONCRETE.defaultBlockState(),
                        poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
                poseStack.scale(0.86F, 0.86F, 0.86F);
            } else {
                poseStack.scale(0.94F, 0.94F, 0.94F);
            }
            poseStack.translate(-0.5D, -0.5D, -0.5D);
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
