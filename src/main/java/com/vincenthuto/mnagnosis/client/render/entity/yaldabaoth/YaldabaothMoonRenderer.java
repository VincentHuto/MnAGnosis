package com.vincenthuto.mnagnosis.client.render.entity.yaldabaoth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothMoonEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class YaldabaothMoonRenderer
        extends GeoEntityRenderer<YaldabaothMoonEntity> {

    public YaldabaothMoonRenderer(EntityRendererProvider.Context context) {
        super(context, new YaldabaothMoonModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            YaldabaothMoonEntity entity,
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
            YaldabaothMoonEntity entity,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
