package com.vincenthuto.mnagnosis.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.client.shader.core.CoreShaders;
import com.vincenthuto.mnagnosis.client.shader.core.RenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.joml.Vector4f;

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
    private static final float PROXY_BOUND = 0.78F;
    private static final float[][][] PROXY_FACES = {
            {
                    {-1, -1, 1}, {1, -1, 1},
                    {1, 1, 1}, {-1, 1, 1}
            },
            {
                    {-1, -1, -1}, {-1, 1, -1},
                    {1, 1, -1}, {1, -1, -1}
            },
            {
                    {-1, -1, -1}, {-1, -1, 1},
                    {-1, 1, 1}, {-1, 1, -1}
            },
            {
                    {1, -1, -1}, {1, 1, -1},
                    {1, 1, 1}, {1, -1, 1}
            },
            {
                    {-1, 1, -1}, {-1, 1, 1},
                    {1, 1, 1}, {1, 1, -1}
            },
            {
                    {-1, -1, -1}, {1, -1, -1},
                    {1, -1, 1}, {-1, -1, 1}
            }
    };
    private static final int[] TRIANGLE_ORDER = {0, 1, 2, 0, 2, 3};

    private TesseractRenderCore() {
    }

    public static void renderShader(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float elapsedSeconds,
            float angleXw,
            float angleYz,
            float pulse,
            float alpha
    ) {
        ShaderInstance shader = CoreShaders.tesseract();
        if (shader == null) {
            return;
        }

        configureShader(
                shader,
                poseStack,
                elapsedSeconds,
                angleXw,
                angleYz,
                pulse
        );
        RenderType layer = RenderHelper.getTesseractLayer();
        renderProxyCube(
                buffer.getBuffer(layer),
                poseStack.last().pose(),
                alpha
        );
        if (buffer instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(layer);
        }
    }

    private static void configureShader(
            ShaderInstance shader,
            PoseStack poseStack,
            float elapsedSeconds,
            float angleXw,
            float angleYz,
            float pulse
    ) {
        Matrix4f pose = new Matrix4f(poseStack.last().pose());
        Matrix4f inversePose = new Matrix4f(pose).invert();
        Matrix4f inverseModelViewPose =
                new Matrix4f(RenderSystem.getModelViewMatrix())
                        .mul(pose)
                        .invert();
        Vector4f cameraOrigin = inverseModelViewPose.transform(
                new Vector4f(0.0F, 0.0F, 0.0F, 1.0F)
        );
        if (Math.abs(cameraOrigin.w()) > 0.000_01F) {
            cameraOrigin.div(cameraOrigin.w());
        }
        Vector4f rayDirection = inverseModelViewPose.transform(
                new Vector4f(0.0F, 0.0F, -1.0F, 0.0F)
        );
        float directionLength = (float) Math.sqrt(
                rayDirection.x() * rayDirection.x()
                        + rayDirection.y() * rayDirection.y()
                        + rayDirection.z() * rayDirection.z()
        );
        if (directionLength > 0.000_01F) {
            rayDirection.div(directionLength);
        }
        int perspective =
                Math.abs(RenderSystem.getProjectionMatrix().m33()) < 0.5F
                        ? 1
                        : 0;

        shader.safeGetUniform("InversePose").set(inversePose);
        shader.safeGetUniform("ModelPoseMat").set(pose);
        shader.safeGetUniform("CameraOrigin").set(
                cameraOrigin.x(),
                cameraOrigin.y(),
                cameraOrigin.z()
        );
        shader.safeGetUniform("RayDirection").set(
                rayDirection.x(),
                rayDirection.y(),
                rayDirection.z()
        );
        shader.safeGetUniform("Perspective").set(perspective);
        shader.safeGetUniform("TesseractTime").set(elapsedSeconds);
        shader.safeGetUniform("TesseractAngleXw").set(angleXw);
        shader.safeGetUniform("TesseractAngleYz").set(angleYz);
        shader.safeGetUniform("TesseractPulse").set(pulse);

        setPaletteColor(shader, "PaletteVoid", TesseractPalette.Slot.VOID);
        setPaletteColor(shader, "PaletteCyan", TesseractPalette.Slot.CYAN);
        setPaletteColor(shader, "PaletteAzure", TesseractPalette.Slot.AZURE);
        setPaletteColor(
                shader,
                "PaletteViolet",
                TesseractPalette.Slot.VIOLET
        );
        setPaletteColor(shader, "PalettePearl", TesseractPalette.Slot.PEARL);
        setPaletteColor(shader, "PaletteGold", TesseractPalette.Slot.GOLD);

        TesseractPalette.Tuning tuning = TesseractPalette.tuning();
        shader.safeGetUniform("PaletteBrightness").set(tuning.brightness());
        shader.safeGetUniform("PaletteGlowStrength").set(
                tuning.glowStrength()
        );
        shader.safeGetUniform("TesseractTubeRadius").set(
                tuning.tubeRadius()
        );
    }

    private static void setPaletteColor(
            ShaderInstance shader,
            String uniform,
            TesseractPalette.Slot slot
    ) {
        TesseractPalette.Color color = TesseractPalette.color(slot);
        shader.safeGetUniform(uniform).set(
                color.red(),
                color.green(),
                color.blue()
        );
    }

    private static void renderProxyCube(
            VertexConsumer consumer,
            Matrix4f matrix,
            float alpha
    ) {
        for (float[][] face : PROXY_FACES) {
            for (int index : TRIANGLE_ORDER) {
                float[] point = face[index];
                consumer.vertex(
                                matrix,
                                point[0] * PROXY_BOUND,
                                point[1] * PROXY_BOUND,
                                point[2] * PROXY_BOUND
                        )
                        .color(1.0F, 1.0F, 1.0F, alpha)
                        .endVertex();
            }
        }
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
