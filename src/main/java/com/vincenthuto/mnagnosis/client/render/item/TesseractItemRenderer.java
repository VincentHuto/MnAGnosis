package com.vincenthuto.mnagnosis.client.render.item;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class TesseractItemRenderer extends BlockEntityWithoutLevelRenderer {

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

    public TesseractItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {

        poseStack.pushPose();
        // Center and scale the tesseract
        poseStack.translate(0.5, 0.5, 0.5);
        float scale = 0.25f;
        poseStack.scale(scale, scale, scale);

        // Get time for rotation
        long time = System.currentTimeMillis();
        float angle1 = (time % 10000) / 10000.0f * (float) Math.PI * 2;
        float angle2 = (time % 7000) / 7000.0f * (float) Math.PI * 2;

        // Project and render the tesseract
        float[][] projectedVertices = new float[16][3];

        for (int i = 0; i < 16; i++) {
            float[] rotated = rotate4D(TESSERACT_VERTICES[i], angle1, angle2);
            projectedVertices[i] = project4Dto3D(rotated);
        }

        // Render edges
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        for (int[] edge : TESSERACT_EDGES) {
            float[] v1 = projectedVertices[edge[0]];
            float[] v2 = projectedVertices[edge[1]];

            // Calculate color based on depth (w coordinate)
            float depth1 = TESSERACT_VERTICES[edge[0]][3];
            float depth2 = TESSERACT_VERTICES[edge[1]][3];

            // Cyan to blue gradient based on 4D depth
            float r1 = 0.3f + depth1 * 0.2f;
            float g1 = 0.7f + depth1 * 0.2f;
            float b1 = 1.0f;

            float r2 = 0.3f + depth2 * 0.2f;
            float g2 = 0.7f + depth2 * 0.2f;
            float b2 = 1.0f;

            consumer.vertex(matrix, v1[0], v1[1], v1[2])
                    .color(r1, g1, b1, 1.0f)
                    .normal(0, 1, 0)
                    .endVertex();

            consumer.vertex(matrix, v2[0], v2[1], v2[2])
                    .color(r2, g2, b2, 1.0f)
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
}
