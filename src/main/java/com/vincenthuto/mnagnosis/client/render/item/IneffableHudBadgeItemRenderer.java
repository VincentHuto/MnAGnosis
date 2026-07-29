package com.vincenthuto.mnagnosis.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public final class IneffableHudBadgeItemRenderer
        extends BlockEntityWithoutLevelRenderer {

    private static final TesseractRenderCore.LineColor BLACK =
            new TesseractRenderCore.LineColor(0.015F, 0.015F, 0.015F, 1.0F);
    private static final TesseractRenderCore.LineColor WHITE =
            new TesseractRenderCore.LineColor(1.0F, 1.0F, 1.0F, 1.0F);
    private static final TesseractRenderCore.LineColor PALE =
            new TesseractRenderCore.LineColor(0.72F, 0.72F, 0.72F, 0.9F);
    private static final int[] FACE_ANCHORS = {10, 12, 15};
    private static final long FACE_PERIOD_MILLIS = 5_400L;

    public IneffableHudBadgeItemRenderer(
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
            case GUI -> 0.15F;
            case GROUND -> 0.18F;
            case FIXED -> 0.21F;
            default -> 0.20F;
        };
        poseStack.scale(scale, scale, scale);

        long time = System.currentTimeMillis();
        float angleXw = cycle(time, 15_000L);
        float angleYz = cycle(time, 11_000L);
        float[][] projected = TesseractRenderCore.project(angleXw, angleYz);
        TesseractRenderCore.renderEdges(
                poseStack, buffer, projected, BLACK, WHITE
        );
        renderFaces(poseStack, buffer, projected, time);
        poseStack.popPose();
    }

    private static void renderFaces(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float[][] projected,
            long time
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();
        for (int face = 0; face < FACE_ANCHORS.length; face++) {
            float phase = ((time + face * FACE_PERIOD_MILLIS / 3L)
                    % FACE_PERIOD_MILLIS) / (float) FACE_PERIOD_MILLIS;
            if (phase >= 0.76F) {
                continue;
            }
            float life = Mth.sin(phase / 0.76F * Mth.PI);
            float emergence = life * life;
            float[] anchor = projected[FACE_ANCHORS[face]];
            float length = Mth.sqrt(
                    anchor[0] * anchor[0]
                            + anchor[1] * anchor[1]
                            + anchor[2] * anchor[2]
            );
            float inverseLength = length < 0.001F ? 0.0F : 1.0F / length;
            float outward = 0.18F + emergence * 0.72F;
            float[] center = {
                    anchor[0] + anchor[0] * inverseLength * outward,
                    anchor[1] + anchor[1] * inverseLength * outward,
                    anchor[2] + anchor[2] * inverseLength * outward
            };
            float width = 0.10F + emergence * 0.24F;
            float height = 0.12F + emergence * 0.34F;
            float scream = 0.08F + emergence * 0.22F;

            diamond(consumer, matrix, center,
                    width * 0.92F, height * 0.92F, WHITE);
            diamond(consumer, matrix,
                    point(center, -width * 0.45F, height * 0.30F),
                    width * 0.17F, height * 0.12F, BLACK);
            diamond(consumer, matrix,
                    point(center, width * 0.45F, height * 0.30F),
                    width * 0.17F, height * 0.12F, BLACK);
            diamond(consumer, matrix,
                    point(center, 0.0F, -height * 0.24F),
                    width * 0.22F, scream, BLACK);
            TesseractRenderCore.line(
                    consumer, matrix,
                    point(center, -width * 0.72F, height * 0.58F),
                    point(center, width * 0.72F, height * 0.58F),
                    WHITE, WHITE
            );
            if (phase > 0.57F) {
                renderFragments(
                        consumer, matrix, center, width, height,
                        (phase - 0.57F) / 0.19F
                );
            }
        }
    }

    private static void diamond(
            VertexConsumer consumer,
            Matrix4f matrix,
            float[] center,
            float halfWidth,
            float halfHeight,
            TesseractRenderCore.LineColor color
    ) {
        float[] top = point(center, 0.0F, halfHeight);
        float[] right = point(center, halfWidth, 0.0F);
        float[] bottom = point(center, 0.0F, -halfHeight);
        float[] left = point(center, -halfWidth, 0.0F);
        TesseractRenderCore.line(consumer, matrix, top, right, color, color);
        TesseractRenderCore.line(consumer, matrix, right, bottom, color, color);
        TesseractRenderCore.line(consumer, matrix, bottom, left, color, color);
        TesseractRenderCore.line(consumer, matrix, left, top, color, color);
    }

    private static void renderFragments(
            VertexConsumer consumer,
            Matrix4f matrix,
            float[] center,
            float width,
            float height,
            float collapse
    ) {
        for (int fragment = 0; fragment < 5; fragment++) {
            float angle = fragment * Mth.TWO_PI / 5.0F + collapse;
            float radius = (1.0F - collapse)
                    * width * (1.1F + fragment * 0.12F);
            float x = Mth.cos(angle) * radius;
            float y = Mth.sin(angle) * radius + height * 0.05F;
            float size = 0.025F * (1.0F - collapse);
            TesseractRenderCore.line(
                    consumer, matrix,
                    point(center, x - size, y - size),
                    point(center, x + size, y + size),
                    PALE, WHITE
            );
        }
    }

    private static float[] point(float[] center, float x, float y) {
        return new float[]{center[0] + x, center[1] + y, center[2]};
    }

    private static float cycle(long time, long period) {
        return (time % period) / (float) period * Mth.TWO_PI;
    }
}
