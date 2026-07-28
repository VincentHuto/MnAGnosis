package com.vincenthuto.mnagnosis.client.render.entity.yaldabaoth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothSunEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class YaldabaothSunRenderer
        extends GeoEntityRenderer<YaldabaothSunEntity> {

    public YaldabaothSunRenderer(EntityRendererProvider.Context context) {
        super(context, new YaldabaothSunModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            YaldabaothSunEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        super.render(
                entity,
                entityYaw,
                partialTick,
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT
        );
    }

    @Override
    public RenderType getRenderType(
            YaldabaothSunEntity entity,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
