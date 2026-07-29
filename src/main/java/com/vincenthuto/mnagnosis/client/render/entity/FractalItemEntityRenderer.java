package com.vincenthuto.mnagnosis.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.mnagnosis.common.entity.item.FractalItemEntityTraits;
import com.vincenthuto.mnagnosis.common.item.FractalEntityItem;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;

public final class FractalItemEntityRenderer
        extends EntityRenderer<ItemEntity> {

    private final ItemRenderer itemRenderer;

    public FractalItemEntityRenderer(
            EntityRendererProvider.Context context
    ) {
        super(context);
        itemRenderer = context.getItemRenderer();
        shadowRadius = 0.0F;
    }

    @Override
    public void render(
            ItemEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight
    ) {
        ItemStack stack = entity.getItem();
        if (!stack.isEmpty()) {
            BakedModel model = itemRenderer.getModel(
                    stack,
                    entity.level(),
                    null,
                    entity.getId()
            );
            float groundScaleY = model.getTransforms()
                    .getTransform(ItemDisplayContext.GROUND)
                    .scale
                    .y();
            FractalItemEntityRenderPose renderPose =
                    FractalItemEntityRenderPose.from(
                            traits(stack),
                            entity.getAge() + partialTick,
                            groundScaleY
                    );

            poseStack.pushPose();
            poseStack.translate(
                    0.0F,
                    renderPose.verticalTranslation(),
                    0.0F
            );
            poseStack.mulPose(
                    Axis.YP.rotation(renderPose.yawRadians())
            );
            poseStack.scale(
                    renderPose.scale(),
                    renderPose.scale(),
                    renderPose.scale()
            );
            itemRenderer.renderStatic(
                    stack,
                    ItemDisplayContext.GROUND,
                    renderPose.fullBright()
                            ? LightTexture.FULL_BRIGHT
                            : packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffers,
                    entity.level(),
                    entity.getId()
            );
            poseStack.popPose();
        }
        super.render(
                entity,
                entityYaw,
                partialTick,
                poseStack,
                buffers,
                packedLight
        );
    }

    @Override
    public ResourceLocation getTextureLocation(
            ItemEntity entity
    ) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private static FractalItemEntityTraits traits(ItemStack stack) {
        if (stack.getItem() instanceof FractalEntityItem provider) {
            return provider.fractalEntityTraits(stack);
        }
        return FractalItemEntityTraits.STATIC;
    }
}
