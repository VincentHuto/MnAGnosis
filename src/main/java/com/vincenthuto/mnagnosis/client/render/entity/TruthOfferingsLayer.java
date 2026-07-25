package com.vincenthuto.mnagnosis.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.mnagnosis.common.entity.TruthEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

/** Renders the exact NBT-preserving stacks stored by the server over Truth's receiving palm. */
public final class TruthOfferingsLayer extends BlockAndItemGeoLayer<TruthEntity> {
    public TruthOfferingsLayer(GeoRenderer<TruthEntity> renderer) {
        super(renderer);
    }

    @Override
    protected ItemStack getStackForBone(GeoBone bone, TruthEntity truth) {
        if (truth.getFinaleProgress(0.0F) >= 0.72F) {
            return ItemStack.EMPTY;
        }
        return switch (bone.getName()) {
            case "codex_item" -> truth.getCodexOffering();
            case "wand_item" -> truth.getWandOffering();
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(
            GeoBone bone,
            ItemStack stack,
            TruthEntity truth
    ) {
        return ItemDisplayContext.FIXED;
    }

    @Override
    protected void renderStackForBone(
            PoseStack poseStack,
            GeoBone bone,
            ItemStack stack,
            TruthEntity truth,
            MultiBufferSource bufferSource,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        if ("codex_item".equals(bone.getName())) {
            poseStack.scale(0.45F, 0.45F, 0.45F);
            poseStack.translate(0, .25F, 0);
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.translate(.2f, .13F, 0.12f);

        super.renderStackForBone(
                poseStack, bone, stack, truth, bufferSource, partialTick, packedLight, packedOverlay
        );
        poseStack.popPose();
    }
}
