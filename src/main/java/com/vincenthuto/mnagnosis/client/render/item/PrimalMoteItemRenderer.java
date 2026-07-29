package com.vincenthuto.mnagnosis.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.client.shader.core.CoreShaders;
import com.vincenthuto.mnagnosis.client.shader.core.RenderHelper;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class PrimalMoteItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final float PROXY_BOUND = 1.24F;
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

    public PrimalMoteItemRenderer(
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
            case GUI -> 0.31F;
            case GROUND -> 0.26F;
            case FIXED -> 0.29F;
            default -> 0.27F;
        };
        poseStack.scale(scale, scale, scale);

        configureShader(poseStack);
        RenderType layer = RenderHelper.getMandelbulbLayer();
        renderProxyCube(
                buffer.getBuffer(layer),
                poseStack.last().pose()
        );
        if (buffer instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(layer);
        }
        poseStack.popPose();
    }

    private static void configureShader(PoseStack poseStack) {
        ShaderInstance shader = CoreShaders.mandelbulb();
        if (shader == null) {
            return;
        }
        Matrix4f pose = new Matrix4f(poseStack.last().pose());
        Matrix4f inversePose = new Matrix4f(pose).invert();
        Matrix4f inverseModelViewPose =
                new Matrix4f(RenderSystem.getModelViewMatrix())
                        .mul(pose)
                        .invert();
        Vector4f cameraOrigin = inverseModelViewPose.transform(
                new Vector4f(0.0F, 0.0F, 0.0F, 1.0F)
        );
        if (Math.abs(cameraOrigin.w()) > 0.00001F) {
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
        if (directionLength > 0.00001F) {
            rayDirection.div(directionLength);
        }
        int perspective =
                Math.abs(RenderSystem.getProjectionMatrix().m33()) < 0.5F
                        ? 1
                        : 0;

        shader.safeGetUniform("InversePose").set(inversePose);
        shader.safeGetUniform("ModelPoseMat").set(pose);
        shader.safeGetUniform("CameraOrigin").set(
                cameraOrigin.x(), cameraOrigin.y(), cameraOrigin.z()
        );
        shader.safeGetUniform("RayDirection").set(
                rayDirection.x(), rayDirection.y(), rayDirection.z()
        );
        shader.safeGetUniform("Perspective").set(perspective);
        shader.safeGetUniform("MandelbulbTime").set(
                (System.currentTimeMillis() % 120_000L) / 1_000.0F
        );
        shader.safeGetUniform("MorphAmount").set(2.8F);
        setPaletteColor(shader, "PaletteBlush", MandelbulbPalette.Slot.BLUSH);
        setPaletteColor(shader, "PalettePeach", MandelbulbPalette.Slot.PEACH);
        setPaletteColor(shader, "PaletteButter", MandelbulbPalette.Slot.BUTTER);
        setPaletteColor(shader, "PaletteMint", MandelbulbPalette.Slot.MINT);
        setPaletteColor(shader, "PaletteSky", MandelbulbPalette.Slot.SKY);
        setPaletteColor(
                shader,
                "PaletteLavender",
                MandelbulbPalette.Slot.LAVENDER
        );
        MandelbulbPalette.Stops stops = MandelbulbPalette.stops();
        shader.safeGetUniform("PaletteStopPeach").set(stops.peach());
        shader.safeGetUniform("PaletteStopButter").set(stops.butter());
        shader.safeGetUniform("PaletteStopMint").set(stops.mint());
        shader.safeGetUniform("PaletteStopSky").set(stops.sky());
        shader.safeGetUniform("PaletteStopLavender").set(stops.lavender());
    }

    private static void setPaletteColor(
            ShaderInstance shader,
            String uniform,
            MandelbulbPalette.Slot slot
    ) {
        MandelbulbPalette.Color color = MandelbulbPalette.color(slot);
        shader.safeGetUniform(uniform).set(
                color.red(),
                color.green(),
                color.blue()
        );
    }

    private static void renderProxyCube(
            VertexConsumer consumer,
            Matrix4f matrix
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
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .endVertex();
            }
        }
    }
}
