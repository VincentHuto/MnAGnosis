package com.vincenthuto.mnagnosis.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.client.render.armor.IneffableArmorModel;
import com.vincenthuto.mnagnosis.client.shader.core.CoreShaders;
import com.vincenthuto.mnagnosis.client.shader.core.RenderHelper;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public final class IneffableArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>>
        extends RenderLayer<T, M> {

    private static final ResourceLocation STATIC_MASK =
            MnAGnosis.rloc("textures/models/armor/ineffable_static_mask.png");
    private static final float GRAIN_INTENSITY = 0.85F;
    private static final float DISFIGURATION = 0.001F;

    private final IneffableArmorModel<T> hood;
    private final IneffableArmorModel<T> robes;
    private final IneffableArmorModel<T> leggings;
    private final IneffableArmorModel<T> boots;

    public IneffableArmorLayer(LivingEntityRenderer<T, M> owner) {
        super(owner);
        this.hood = bake(IneffableArmorModel.INEFFABLE_HOOD_LAYER, EquipmentSlot.HEAD);
        this.robes = bake(IneffableArmorModel.INEFFABLE_ROBES_LAYER, EquipmentSlot.CHEST);
        this.leggings = bake(IneffableArmorModel.INEFFABLE_LEGGINGS_LAYER, EquipmentSlot.LEGS);
        this.boots = bake(IneffableArmorModel.INEFFABLE_BOOTS_LAYER, EquipmentSlot.FEET);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        configureShader();
        renderPiece(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, EquipmentSlot.HEAD,
                ItemRegistry.INEFFABLE_HOOD.get(), this.hood);
        renderPiece(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, EquipmentSlot.CHEST,
                ItemRegistry.INEFFABLE_ROBES.get(), this.robes);
        renderPiece(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, EquipmentSlot.LEGS,
                ItemRegistry.INEFFABLE_LEGGINGS.get(), this.leggings);
        renderPiece(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, EquipmentSlot.FEET,
                ItemRegistry.INEFFABLE_BOOTS.get(), this.boots);
    }

    private void renderPiece(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            EquipmentSlot slot,
            Item expectedItem,
            IneffableArmorModel<T> model
    ) {
        if (!entity.getItemBySlot(slot).is(expectedItem)) {
            return;
        }

        this.getParentModel().copyPropertiesTo(model);
        model.animateCloth(limbSwing, limbSwingAmount, ageInTicks);
        VertexConsumer staticBuffer =
                bufferSource.getBuffer(RenderHelper.getDopplegangerLayer(STATIC_MASK));
        model.renderToBuffer(
                poseStack,
                staticBuffer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }

    @SuppressWarnings("unchecked")
    private IneffableArmorModel<T> bake(
            net.minecraft.client.model.geom.ModelLayerLocation layer,
            EquipmentSlot slot
    ) {
        return new IneffableArmorModel<>(
                Minecraft.getInstance().getEntityModels().bakeLayer(layer),
                slot
        );
    }

    private static void configureShader() {
        ShaderInstance shader = CoreShaders.doppleganger();
        if (shader != null) {
            shader.safeGetUniform("BotaniaGrainIntensity").set(GRAIN_INTENSITY);
            shader.safeGetUniform("BotaniaDisfiguration").set(DISFIGURATION);
        }
    }
}
