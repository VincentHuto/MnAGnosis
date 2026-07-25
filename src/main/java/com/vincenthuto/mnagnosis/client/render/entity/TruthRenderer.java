package com.vincenthuto.mnagnosis.client.render.entity;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.client.shader.core.RenderHelper;
import com.vincenthuto.mnagnosis.common.entity.TruthEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Fullbright white base, noise aura, and actual stored offerings for the final Tier 6 encounter. */
public final class TruthRenderer extends GeoEntityRenderer<TruthEntity> {
    public TruthRenderer(EntityRendererProvider.Context context) {
        super(context, new TruthModel());
        this.shadowRadius = 0.0F;
        this.addRenderLayer(new TruthAuraLayer(this));
        this.addRenderLayer(new TruthOfferingsLayer(this));
    }

    @Override
    public void render(
            TruthEntity truth,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        super.render(truth, entityYaw, partialTick, poseStack, bufferSource, LightTexture.FULL_BRIGHT);
    }

    @Override
    protected void applyRotations(
            TruthEntity truth,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTick
    ) {
        float interpolatedYaw = Mth.rotLerp(partialTick, truth.yRotO, truth.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(TruthEntity.calculateModelYRotation(interpolatedYaw)));
    }

    @Override
    public Color getRenderColor(TruthEntity truth, float partialTick, int packedLight) {
        float progress = truth.getFinaleProgress(partialTick);
        // The late shader reads alpha as progress. Before that, Truth stays a solid fullbright white.
        return truth.shouldShowGlitchSlices()
                ? Color.ofRGBA(1.0F, 1.0F, 1.0F, progress)
                : Color.WHITE;
    }

    @Override
    public RenderType getRenderType(
            TruthEntity truth,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return truth.shouldShowGlitchSlices()
                ? RenderHelper.getTruthGlitchLayer(texture)
                : RenderType.entityTranslucent(texture);
    }

    @Override
    public void renderRecursively(
            PoseStack poseStack,
            TruthEntity truth,
            GeoBone bone,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        boolean tracksFlamePosition = "flame_core".equals(bone.getName());
        if (tracksFlamePosition) {
            bone.setTrackingMatrices(true);
        }

        if ("mouth_backing".equals(bone.getName())) {
            super.renderRecursively(poseStack, truth, bone, renderType, bufferSource, buffer, isReRender,
                    partialTick, packedLight, packedOverlay, 0.0F, 0.0F, 0.0F, alpha);
            return;
        }
        if ("grin_teeth".equals(bone.getName())) {
            super.renderRecursively(poseStack, truth, bone, renderType, bufferSource, buffer, isReRender,
                    partialTick, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
            return;
        }
        super.renderRecursively(poseStack, truth, bone, renderType, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if (tracksFlamePosition && !isReRender) {
            Vector3d worldPosition = bone.getWorldPosition();
            truth.setClientFlamePosition(new Vec3(worldPosition.x, worldPosition.y, worldPosition.z));
        }
    }
}
