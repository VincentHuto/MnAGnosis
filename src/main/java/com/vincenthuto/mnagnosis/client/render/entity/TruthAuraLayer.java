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

/** A torso-centered, enlarged noise pass creates Truth's black static silhouette. */
public final class TruthAuraLayer extends GeoRenderLayer<TruthEntity> {
    private static final float MODEL_CENTER_Y = 1.0F;

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
        float scale = 1.08F + progress * 0.42F;
        boolean dissolving = truth.shouldDissolveAura();
        float preDissolveAlpha = 0.30F + progress * 0.33F;
        float dissolve = dissolving ? Math.min(1.0F, (progress - 0.70F) / 0.30F) : 0.0F;
        if (dissolve >= 0.96F) {
            return;
        }
        float jitter = (float) Math.sin((truth.tickCount + partialTick) * 4.73F)
                * (0.018F + progress * (dissolving ? 0.22F : 0.07F));
        RenderType auraLayer = dissolving
                ? RenderHelper.getTruthGlitchLayer(this.getTextureResource(truth))
                : RenderHelper.getNoiseLayer(this.getTextureResource(truth));
        // truth_glitch reads alpha as this render's progress; its scanline discard owns the late fade.
        float alpha = dissolving ? progress : preDissolveAlpha;

        poseStack.pushPose();
        poseStack.translate(jitter, MODEL_CENTER_Y, -jitter * 0.6F);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, -MODEL_CENTER_Y, 0.0F);
        this.getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                truth,
                auraLayer,
                bufferSource.getBuffer(auraLayer),
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
