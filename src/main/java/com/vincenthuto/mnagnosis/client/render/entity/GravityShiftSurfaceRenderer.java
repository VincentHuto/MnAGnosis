package com.vincenthuto.mnagnosis.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.mnagnosis.common.entity.GravityShiftSurfaceEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public final class GravityShiftSurfaceRenderer
        extends EntityRenderer<GravityShiftSurfaceEntity> {

    public GravityShiftSurfaceRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(
            GravityShiftSurfaceEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight
    ) {
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GravityShiftSurfaceEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
