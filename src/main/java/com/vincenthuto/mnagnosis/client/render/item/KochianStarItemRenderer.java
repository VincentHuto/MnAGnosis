package com.vincenthuto.mnagnosis.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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

public final class KochianStarItemRenderer
        extends BlockEntityWithoutLevelRenderer {

    private static final float PROXY_BOUND = 1.32F;
    private static final float REFERENCE_FACE_TILT_DEGREES = 90.0F;
    private static final float REFERENCE_FACE_SPIN_DEGREES = 150.0F;
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

    public KochianStarItemRenderer(
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
            case GUI -> 0.34F;
            case GROUND -> 0.28F;
            case FIXED -> 0.32F;
            default -> 0.30F;
        };
        poseStack.scale(scale, scale, scale);

        float seconds = (System.currentTimeMillis() % 240_000L) / 1_000.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                REFERENCE_FACE_SPIN_DEGREES + seconds * 2.0F
        ));
        poseStack.mulPose(Axis.XP.rotationDegrees(
                REFERENCE_FACE_TILT_DEGREES
        ));

        configureShader(poseStack, seconds);
        RenderType layer = RenderHelper.getKochianStarLayer();
        renderProxyCube(buffer.getBuffer(layer), poseStack.last().pose());
        if (buffer instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(layer);
        }
        poseStack.popPose();
    }

    private static void configureShader(PoseStack poseStack, float seconds) {
        ShaderInstance shader = CoreShaders.kochianStar();
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
        KochianStarAnimation.Frame frame =
                KochianStarAnimation.sample(seconds);

        shader.safeGetUniform("InversePose").set(inversePose);
        shader.safeGetUniform("ModelPoseMat").set(pose);
        shader.safeGetUniform("CameraOrigin").set(
                cameraOrigin.x(), cameraOrigin.y(), cameraOrigin.z()
        );
        shader.safeGetUniform("RayDirection").set(
                rayDirection.x(), rayDirection.y(), rayDirection.z()
        );
        shader.safeGetUniform("Perspective").set(perspective);
        shader.safeGetUniform("KochianTime").set(seconds);
        shader.safeGetUniform("KochianAngle").set(frame.angleDegrees());
        shader.safeGetUniform("KochianRecursion").set(frame.recursion());
        setPaletteColor(shader, "PaletteVoid", KochianStarPalette.Slot.VOID);
        setPaletteColor(
                shader,
                "PaletteAmethyst",
                KochianStarPalette.Slot.AMETHYST
        );
        setPaletteColor(
                shader,
                "PaletteFuchsia",
                KochianStarPalette.Slot.FUCHSIA
        );
        setPaletteColor(
                shader,
                "PalettePearl",
                KochianStarPalette.Slot.PEARL
        );
        setPaletteColor(shader, "PaletteIce", KochianStarPalette.Slot.ICE);
        setPaletteColor(shader, "PaletteGold", KochianStarPalette.Slot.GOLD);
    }

    private static void setPaletteColor(
            ShaderInstance shader,
            String uniform,
            KochianStarPalette.Slot slot
    ) {
        KochianStarPalette.Color color = KochianStarPalette.color(slot);
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
