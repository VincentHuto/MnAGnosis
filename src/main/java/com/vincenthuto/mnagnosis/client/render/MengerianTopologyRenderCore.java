package com.vincenthuto.mnagnosis.client.render;

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

public final class MengerianTopologyRenderCore {

    private static final float PROXY_BOUND = 0.62F;
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

    private MengerianTopologyRenderCore() {
    }

    public static void render(
            PoseStack poseStack,
            MultiBufferSource buffers,
            float alpha,
            float elapsedSeconds
    ) {
        ShaderInstance shader = CoreShaders.mengerianTopology();
        if (shader == null) {
            return;
        }

        MengerianTopologyAnimation.Frame frame =
                MengerianTopologyAnimation.frame(elapsedSeconds);
        configureShader(
                shader,
                poseStack,
                frame,
                elapsedSeconds
        );

        RenderType layer = RenderHelper.getMengerianTopologyLayer();
        renderProxyCube(
                buffers.getBuffer(layer),
                poseStack.last().pose(),
                alpha
        );
        if (buffers instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(layer);
        }
    }

    private static void configureShader(
            ShaderInstance shader,
            PoseStack poseStack,
            MengerianTopologyAnimation.Frame frame,
            float elapsedSeconds
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
        shader.safeGetUniform("MengerianTime").set(elapsedSeconds);
        shader.safeGetUniform("MengerianDepth").set(frame.recursiveDepth());
        shader.safeGetUniform("MengerianSeparation").set(frame.separation());

        setPaletteColor(
                shader,
                "PaletteCrimson",
                MengerianTopologyPalette.Slot.CRIMSON
        );
        setPaletteColor(
                shader,
                "PaletteGold",
                MengerianTopologyPalette.Slot.GOLD
        );
        setPaletteColor(
                shader,
                "PaletteVerdant",
                MengerianTopologyPalette.Slot.VERDANT
        );
        setPaletteColor(
                shader,
                "PaletteViolet",
                MengerianTopologyPalette.Slot.VIOLET
        );
        setPaletteColor(
                shader,
                "PaletteAzure",
                MengerianTopologyPalette.Slot.AZURE
        );
        setPaletteColor(
                shader,
                "PalettePearl",
                MengerianTopologyPalette.Slot.PEARL
        );

        MengerianTopologyPalette.Tuning tuning =
                MengerianTopologyPalette.tuning();
        shader.safeGetUniform("PaletteBrightness").set(tuning.brightness());
        shader.safeGetUniform("PaletteShadeStrength").set(
                tuning.shadeStrength()
        );
        shader.safeGetUniform("PaletteDepthColorMix").set(
                tuning.depthColorMix()
        );
    }

    private static void setPaletteColor(
            ShaderInstance shader,
            String uniform,
            MengerianTopologyPalette.Slot slot
    ) {
        MengerianTopologyPalette.Color color =
                MengerianTopologyPalette.color(slot);
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
}
