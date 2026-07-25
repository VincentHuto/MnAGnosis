package com.vincenthuto.mnagnosis.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.client.shader.core.RenderHelper;
import com.vincenthuto.mnagnosis.common.entity.TruthEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * Slow translucent shells give Truth a smoky silhouette while a quieter noise pass
 * preserves the digital static motif. The finale deliberately returns to the
 * sharper glitch shader.
 */
public final class TruthAuraLayer extends GeoRenderLayer<TruthEntity> {
    private static final float MODEL_CENTER_Y = 1.0F;
    private static final float[][] HAZE_SHELLS = {
            {1.045F, 0.105F, 0.010F, 0.031F, 0.0F},
            {1.085F, 0.070F, 0.022F, 0.023F, 2.1F},
            {1.135F, 0.040F, 0.038F, 0.017F, 4.2F}
    };

    public TruthAuraLayer(GeoRenderer<TruthEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            TruthEntity truth,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        float progress = truth.getFinaleProgress(partialTick);
        boolean dissolving = truth.shouldDissolveAura();
        float dissolve = dissolving ? Math.min(1.0F, (progress - 0.70F) / 0.30F) : 0.0F;
        if (dissolve >= 0.96F) {
            return;
        }
        float time = truth.tickCount + partialTick;

        // These broad, low-opacity shells move independently and read as haze
        // instead of a second solid model or a cloud positioned above Truth.
        float hazeFade = 1.0F - dissolve;
        RenderType hazeLayer = RenderType.entityTranslucent(this.getTextureResource(truth));
        for (float[] shell : HAZE_SHELLS) {
            float pulse = (float) Math.sin(time * shell[3] + shell[4]);
            float scale = shell[0] + progress * 0.16F + pulse * 0.012F;
            float horizontalDrift = (float) Math.cos(time * shell[3] * 0.83F + shell[4])
                    * shell[2];
            float verticalDrift = pulse * shell[2] * 0.55F;
            renderShell(
                    poseStack,
                    truth,
                    bakedModel,
                    bufferSource,
                    hazeLayer,
                    partialTick,
                    scale,
                    horizontalDrift,
                    verticalDrift,
                    -horizontalDrift * 0.65F,
                    shell[1] * hazeFade
            );
        }

        float scale = 1.075F + progress * 0.42F;
        float jitter = (float) Math.sin(time * 1.37F)
                * (0.006F + progress * (dissolving ? 0.22F : 0.025F));
        RenderType auraLayer = dissolving
                ? RenderHelper.getTruthGlitchLayer(this.getTextureResource(truth))
                : RenderHelper.getNoiseLayer(this.getTextureResource(truth));
        // truth_glitch reads alpha as this render's progress; its scanline discard owns the late fade.
        float alpha = dissolving ? progress : 0.17F + progress * 0.22F;

        renderShell(
                poseStack,
                truth,
                bakedModel,
                bufferSource,
                auraLayer,
                partialTick,
                scale,
                jitter,
                0.0F,
                -jitter * 0.6F,
                alpha
        );
    }

    private void renderShell(
            PoseStack poseStack,
            TruthEntity truth,
            BakedGeoModel bakedModel,
            MultiBufferSource bufferSource,
            RenderType layer,
            float partialTick,
            float scale,
            float offsetX,
            float offsetY,
            float offsetZ,
            float alpha
    ) {
        poseStack.pushPose();
        poseStack.translate(offsetX, MODEL_CENTER_Y + offsetY, offsetZ);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, -MODEL_CENTER_Y, 0.0F);
        this.getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                truth,
                layer,
                bufferSource.getBuffer(layer),
                partialTick,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                0.02F,
                0.02F,
                0.02F,
                alpha
        );
                poseStack.popPose();
    }
}
