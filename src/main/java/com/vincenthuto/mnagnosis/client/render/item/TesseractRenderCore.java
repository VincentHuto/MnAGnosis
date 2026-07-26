package com.vincenthuto.mnagnosis.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

public final class TesseractRenderCore {

    private static final float[][] VERTICES = {
            {-1, -1, -1, -1}, {1, -1, -1, -1},
            {1, 1, -1, -1}, {-1, 1, -1, -1},
            {-1, -1, 1, -1}, {1, -1, 1, -1},
            {1, 1, 1, -1}, {-1, 1, 1, -1},
            {-1, -1, -1, 1}, {1, -1, -1, 1},
            {1, 1, -1, 1}, {-1, 1, -1, 1},
            {-1, -1, 1, 1}, {1, -1, 1, 1},
            {1, 1, 1, 1}, {-1, 1, 1, 1}
    };

    private static final int[][] EDGES = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7},
            {8, 9}, {9, 10}, {10, 11}, {11, 8},
            {12, 13}, {13, 14}, {14, 15}, {15, 12},
            {8, 12}, {9, 13}, {10, 14}, {11, 15},
            {0, 8}, {1, 9}, {2, 10}, {3, 11},
            {4, 12}, {5, 13}, {6, 14}, {7, 15}
    };

    private static final float PROJECTION_DISTANCE = 2.5F;

    private TesseractRenderCore() {
    }

    public static float[][] project(float angleXw, float angleYz) {
        float[][] projected = new float[VERTICES.length][3];
        float cosXw = (float) Math.cos(angleXw);
        float sinXw = (float) Math.sin(angleXw);
        float cosYz = (float) Math.cos(angleYz);
        float sinYz = (float) Math.sin(angleYz);
        for (int index = 0; index < VERTICES.length; index++) {
            float[] vertex = VERTICES[index];
            float x = vertex[0] * cosXw - vertex[3] * sinXw;
            float w = vertex[0] * sinXw + vertex[3] * cosXw;
            float y = vertex[1] * cosYz - vertex[2] * sinYz;
            float z = vertex[1] * sinYz + vertex[2] * cosYz;
            float factor = PROJECTION_DISTANCE / (PROJECTION_DISTANCE - w);
            projected[index][0] = x * factor;
            projected[index][1] = y * factor;
            projected[index][2] = z * factor;
        }
        return projected;
    }

    public static void renderEdges(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float[][] projected,
            LineColor inner,
            LineColor outer
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();
        for (int[] edge : EDGES) {
            line(
                    consumer,
                    matrix,
                    projected[edge[0]],
                    projected[edge[1]],
                    edge[0] < 8 ? inner : outer,
                    edge[1] < 8 ? inner : outer
            );
        }
    }

    public static void line(
            VertexConsumer consumer,
            Matrix4f matrix,
            float[] from,
            float[] to,
            LineColor fromColor,
            LineColor toColor
    ) {
        vertex(consumer, matrix, from, fromColor);
        vertex(consumer, matrix, to, toColor);
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            float[] point,
            LineColor color
    ) {
        consumer.vertex(matrix, point[0], point[1], point[2])
                .color(color.red(), color.green(), color.blue(), color.alpha())
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    public record LineColor(float red, float green, float blue, float alpha) {
    }
}
