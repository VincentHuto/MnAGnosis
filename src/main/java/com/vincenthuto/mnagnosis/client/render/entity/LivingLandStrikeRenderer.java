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
        if (entity.isProjected()) {
            renderProjectedTendril(
                    entity, partialTick, poseStack, buffers, packedLight, renderedHead);
            poseStack.popPose();
            super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
            return;
        }
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
            poseStack.scale(0.94F, 0.94F, 0.94F);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            context.getBlockRenderDispatcher().renderSingleBlock(
                    entity.getCarriedState(index), poseStack, buffers,
                    packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    private void renderProjectedTendril(
            LivingLandStrikeEntity entity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            Vec3 renderedHead
    ) {
        for (int index = 0; index < entity.getPayloadLength() - 1; index++) {
            Vec3 start = entity.getSegmentPosition(index, partialTick);
            Vec3 end = entity.getSegmentPosition(index + 1, partialTick);
            Vec3 span = end.subtract(start);
            double length = span.length();
            if (!Double.isFinite(length) || length < 1.0E-4D) {
                continue;
            }
            Vec3 midpoint = start.add(end).scale(0.5D);
            Vec3 direction = span.scale(1.0D / length);
            float spanYaw = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
            float spanPitch = (float) -Math.toDegrees(Math.asin(direction.y));

            renderSpan(
                    entity.getCarriedState(index),
                    midpoint.subtract(renderedHead), spanYaw, spanPitch,
                    0.94F, (float) length + 0.10F,
                    poseStack, buffers, packedLight);
        }
    }

    private void renderSpan(
            net.minecraft.world.level.block.state.BlockState state,
            Vec3 offset,
            float yaw,
            float pitch,
            float width,
            float length,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight
    ) {
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.scale(width, width, length);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        context.getBlockRenderDispatcher().renderSingleBlock(
                state, poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(LivingLandStrikeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
