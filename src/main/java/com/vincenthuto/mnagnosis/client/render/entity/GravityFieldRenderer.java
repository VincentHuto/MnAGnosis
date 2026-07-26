package com.vincenthuto.mnagnosis.client.render.entity;

import com.vincenthuto.mnagnosis.common.entity.GravityFieldEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public final class GravityFieldRenderer extends EntityRenderer<GravityFieldEntity> {

    public GravityFieldRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(GravityFieldEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
