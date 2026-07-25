package com.vincenthuto.mnagnosis.client.render.block;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.common.block.entity.TesseractBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

public class TesseractBlockEntityRenderer implements BlockEntityRenderer<TesseractBlockEntity> {

    // Tesseract vertices in 4D (16 vertices total)
    private static final float[][] TESSERACT_VERTICES = {
            // Inner cube
            {-1, -1, -1, -1}, {1, -1, -1, -1}, {1, 1, -1, -1}, {-1, 1, -1, -1},
            {-1, -1, 1, -1}, {1, -1, 1, -1}, {1, 1, 1, -1}, {-1, 1, 1, -1},
            // Outer cube
            {-1, -1, -1, 1}, {1, -1, -1, 1}, {1, 1, -1, 1}, {-1, 1, -1, 1},
            {-1, -1, 1, 1}, {1, -1, 1, 1}, {1, 1, 1, 1}, {-1, 1, 1, 1}
    };

    // Edges connecting vertices (32 edges total)
    private static final int[][] TESSERACT_EDGES = {
            // Inner cube edges
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7},
            // Outer cube edges
            {8, 9}, {9, 10}, {10, 11}, {11, 8},
            {12, 13}, {13, 14}, {14, 15}, {15, 12},
            {8, 12}, {9, 13}, {10, 14}, {11, 15},
            // Connecting inner to outer
            {0, 8}, {1, 9}, {2, 10}, {3, 11},
            {4, 12}, {5, 13}, {6, 14}, {7, 15}
    };

    public TesseractBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TesseractBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        System.out.println("displayContext: " + blockEntity);



        poseStack.pushPose();

        // Center on the block
        poseStack.translate(0.5, 0.5, 0.5);

        // Scale the tesseract
        float baseScale = 0.3f;
        float pulse = blockEntity.getPulse();
        float scale = baseScale * (0.9f + pulse * 0.1f); // Slight pulsing
        poseStack.scale(scale, scale, scale);

        // Get rotation angles with interpolation for smooth animation
        float angle1 = blockEntity.getRotation1();
        float angle2 = blockEntity.getRotation2();

        // Project and render the tesseract
        float[][] projectedVertices = new float[16][3];

        for (int i = 0; i < 16; i++) {
            float[] rotated = rotate4D(TESSERACT_VERTICES[i], angle1, angle2);
            projectedVertices[i] = project4Dto3D(rotated);
        }

        // Render edges with glowing effect
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        for (int[] edge : TESSERACT_EDGES) {
            float[] v1 = projectedVertices[edge[0]];
            float[] v2 = projectedVertices[edge[1]];

            // Calculate color based on depth (w coordinate) and pulse
            float depth1 = TESSERACT_VERTICES[edge[0]][3];
            float depth2 = TESSERACT_VERTICES[edge[1]][3];

            // Enhanced cyan to blue gradient with pulsing glow
            float glowBoost = 0.2f + pulse * 0.3f;

            float r1 = (0.2f + depth1 * 0.2f) * glowBoost;
            float g1 = (0.6f + depth1 * 0.3f) + glowBoost;
            float b1 = 1.0f;

            float r2 = (0.2f + depth2 * 0.2f) * glowBoost;
            float g2 = (0.6f + depth2 * 0.3f) + glowBoost;
            float b2 = 1.0f;

            // Alpha for extra glow effect
            float alpha = 0.8f + pulse * 0.2f;

            consumer.vertex(matrix, v1[0], v1[1], v1[2])
                    .color(r1, g1, b1, alpha)
                    .normal(0, 1, 0)
                    .endVertex();

            consumer.vertex(matrix, v2[0], v2[1], v2[2])
                    .color(r2, g2, b2, alpha)
                    .normal(0, 1, 0)
                    .endVertex();
        }

        // Optional: Render a second layer with slight offset for glow effect
        VertexConsumer glowConsumer = buffer.getBuffer(RenderType.lines());
        float glowOffset = 1.05f;

        for (int[] edge : TESSERACT_EDGES) {
            float[] v1 = projectedVertices[edge[0]];
            float[] v2 = projectedVertices[edge[1]];

            float depth1 = TESSERACT_VERTICES[edge[0]][3];
            float depth2 = TESSERACT_VERTICES[edge[1]][3];

            float r1 = (0.4f + depth1 * 0.3f) * pulse;
            float g1 = (0.8f + depth1 * 0.2f) * pulse;
            float b1 = 1.0f;

            float r2 = (0.4f + depth2 * 0.3f) * pulse;
            float g2 = (0.8f + depth2 * 0.2f) * pulse;
            float b2 = 1.0f;

            float glowAlpha = 0.3f * pulse;

            glowConsumer.vertex(matrix, v1[0] * glowOffset, v1[1] * glowOffset, v1[2] * glowOffset)
                    .color(r1, g1, b1, glowAlpha)
                    .normal(0, 1, 0)
                    .endVertex();

            glowConsumer.vertex(matrix, v2[0] * glowOffset, v2[1] * glowOffset, v2[2] * glowOffset)
                    .color(r2, g2, b2, glowAlpha)
                    .normal(0, 1, 0)
                    .endVertex();
        }

        poseStack.popPose();
    }

    // Rotate in 4D space using two rotation planes
    private float[] rotate4D(float[] vertex, float angle1, float angle2) {
        float x = vertex[0];
        float y = vertex[1];
        float z = vertex[2];
        float w = vertex[3];

        // Rotation in XW plane
        float cosA1 = (float) Math.cos(angle1);
        float sinA1 = (float) Math.sin(angle1);
        float newX = x * cosA1 - w * sinA1;
        float newW = x * sinA1 + w * cosA1;

        // Rotation in YZ plane
        float cosA2 = (float) Math.cos(angle2);
        float sinA2 = (float) Math.sin(angle2);
        float newY = y * cosA2 - z * sinA2;
        float newZ = y * sinA2 + z * cosA2;

        return new float[]{newX, newY, newZ, newW};
    }

    // Project from 4D to 3D using perspective projection
    private float[] project4Dto3D(float[] vertex) {
        float distance = 2.5f; // Distance for 4D perspective
        float factor = distance / (distance - vertex[3]);

        return new float[]{
                vertex[0] * factor,
                vertex[1] * factor,
                vertex[2] * factor
        };
    }

    @Override
    public boolean shouldRenderOffScreen(TesseractBlockEntity blockEntity) {
        return true; // Render even when off-screen for dramatic effect
    }

    @Override
    public int getViewDistance() {
        return 256; // Render from far away
    }
}
