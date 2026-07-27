package com.vincenthuto.mnagnosis.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.mnagnosis.common.entity.GravityFieldEntity;
import com.vincenthuto.mnagnosis.common.spell.gravity.GravityPolarity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class GravityFieldRenderer extends EntityRenderer<GravityFieldEntity> {

    public static final int HORIZON_LATITUDE_SEGMENTS = 16;
    public static final int HORIZON_LONGITUDE_SEGMENTS = 24;
    public static final int PHOTON_RING_SEGMENTS = 40;

    public GravityFieldRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(
            GravityFieldEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight
    ) {
        poseStack.pushPose();
        poseStack.translate(0,1.5,0);
        float horizonRadius = horizonRadius(entity.getRadius());
        renderEventHorizon(poseStack, buffers, horizonRadius);
        renderPhotonRings(entity, partialTick, poseStack, buffers, horizonRadius);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    public static float horizonRadius(float fieldRadius) {
        return 0.65F + fieldRadius * 0.035F;
    }

    private static void renderEventHorizon(
            PoseStack poseStack,
            MultiBufferSource buffers,
            float radius
    ) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.debugQuads());
        Matrix4f matrix = poseStack.last().pose();
        for (int latitude = 0; latitude < HORIZON_LATITUDE_SEGMENTS; latitude++) {
            double lowerPhi = -Math.PI * 0.5D
                    + Math.PI * latitude / HORIZON_LATITUDE_SEGMENTS;
            double upperPhi = -Math.PI * 0.5D
                    + Math.PI * (latitude + 1) / HORIZON_LATITUDE_SEGMENTS;
            for (int longitude = 0;
                 longitude < HORIZON_LONGITUDE_SEGMENTS;
                 longitude++) {
                double leftTheta = Math.PI * 2.0D * longitude
                        / HORIZON_LONGITUDE_SEGMENTS;
                double rightTheta = Math.PI * 2.0D * (longitude + 1)
                        / HORIZON_LONGITUDE_SEGMENTS;
                horizonVertex(consumer, matrix, radius, lowerPhi, leftTheta);
                horizonVertex(consumer, matrix, radius, lowerPhi, rightTheta);
                horizonVertex(consumer, matrix, radius, upperPhi, rightTheta);
                horizonVertex(consumer, matrix, radius, upperPhi, leftTheta);
            }
        }
    }

    private static void horizonVertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            float radius,
            double phi,
            double theta
    ) {
        float horizontal = radius * (float) Math.cos(phi);
        consumer.vertex(
                        matrix,
                        horizontal * (float) Math.cos(theta),
                        radius * (float) Math.sin(phi),
                        horizontal * (float) Math.sin(theta)
                )
                .color(0, 0, 0, 255)
                .endVertex();
    }

    private void renderPhotonRings(
            GravityFieldEntity entity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            float horizonRadius
    ) {
        poseStack.pushPose();
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        float time = entity.tickCount + partialTick;
        float direction =
                entity.getPolarity() == GravityPolarity.REPEL ? -1.0F : 1.0F;

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(time * 2.7F * direction));
        renderSegmentedAnnulus(
                poseStack, buffers,
                horizonRadius * 1.12F, horizonRadius * 1.25F,
                240, 240, 240, 205, 5
        );
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(-time * 1.6F * direction + 17.0F));
        renderSegmentedAnnulus(
                poseStack, buffers,
                horizonRadius * 1.30F, horizonRadius * 1.37F,
                150, 150, 150, 125, 7
        );
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderSegmentedAnnulus(
            PoseStack poseStack,
            MultiBufferSource buffers,
            float innerRadius,
            float outerRadius,
            int red,
            int green,
            int blue,
            int alpha,
            int gapPeriod
    ) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.debugQuads());
        Matrix4f matrix = poseStack.last().pose();
        for (int segment = 0; segment < PHOTON_RING_SEGMENTS; segment++) {
            if (segment % gapPeriod == gapPeriod - 1) {
                continue;
            }
            double start = Math.PI * 2.0D * segment / PHOTON_RING_SEGMENTS;
            double end = Math.PI * 2.0D * (segment + 0.72D)
                    / PHOTON_RING_SEGMENTS;
            ringVertex(consumer, matrix, innerRadius, start, red, green, blue, alpha);
            ringVertex(consumer, matrix, outerRadius, start, red, green, blue, alpha);
            ringVertex(consumer, matrix, outerRadius, end, red, green, blue, alpha);
            ringVertex(consumer, matrix, innerRadius, end, red, green, blue, alpha);
        }
    }

    private static void ringVertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            float radius,
            double angle,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        consumer.vertex(
                        matrix,
                        radius * (float) Math.cos(angle),
                        radius * (float) Math.sin(angle),
                        -0.015F
                )
                .color(red, green, blue, alpha)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(GravityFieldEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
