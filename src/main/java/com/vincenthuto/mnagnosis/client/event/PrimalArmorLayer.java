package com.vincenthuto.mnagnosis.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.client.render.item.PrimalCrownModel;
import com.vincenthuto.mnagnosis.client.render.item.PrimalRobeModel;
import com.vincenthuto.mnagnosis.client.shader.core.CoreShaders;
import com.vincenthuto.mnagnosis.client.shader.core.RenderHelper;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class PrimalArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    public static ResourceLocation texture = MnAGnosis.rloc(
            "textures/models/armor/primal_crown_layer_1.png");
    private final PrimalCrownModel<T> modelCrown;
    private final PrimalRobeModel<T> modelRobe;


    public PrimalArmorLayer(LivingEntityRenderer<T, M> owner) {
        super(owner);
        this.modelCrown = new PrimalCrownModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(PrimalCrownModel.LAYER_LOCATION));
        this.modelRobe = new PrimalRobeModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(PrimalRobeModel.PRIMAL_ROBE_LAYER));

    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int lightness, T ent, float limbSwing,
                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack crown = ent.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack armor = ent.getItemBySlot(EquipmentSlot.CHEST);

        if (armor.is(ItemRegistry.primal_robes.get())) {
            MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
                    .immediate(Tesselator.getInstance().getBuilder());

            VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(RenderType.text(MnAGnosis.rloc(
                    "textures/item/primal_armor_model.png")));
            this.getParentModel().copyPropertiesTo(modelRobe);

            float DEFAULT_GRAIN_INTENSITY = 0.85f;
            final float DEFAULT_DISFIGURATION = 0.001f;
            ShaderInstance shader = CoreShaders.doppleganger();
            if (shader != null) {
                float grainIntensity, disfiguration;

                shader.safeGetUniform("BotaniaGrainIntensity").set(DEFAULT_GRAIN_INTENSITY);
                shader.safeGetUniform("BotaniaDisfiguration").set(DEFAULT_DISFIGURATION);
            }
            VertexConsumer glitch = buffer.getBuffer(RenderHelper.getDopplegangerLayer(MnAGnosis.rloc(
                    "textures/item/primal_armor_model.png")));
            VertexConsumer vc = VertexMultiConsumer.create(glitch, ivertexbuilder);
            modelRobe.renderToBuffer(matrixStack, glitch, lightness, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            irendertypebuffer$impl.endBatch();
        } else if (crown.is(ItemRegistry.primal_crown.get())) {
            matrixStack.translate(0, 0.05f, 0);
            MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
                    .immediate(Tesselator.getInstance().getBuilder());

            VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(RenderType.text(texture));
            this.getParentModel().copyPropertiesTo(modelCrown);


            float DEFAULT_GRAIN_INTENSITY = 0.85f;
            final float DEFAULT_DISFIGURATION = 0.001f;
            ShaderInstance shader = CoreShaders.doppleganger();
            if (shader != null) {
                float grainIntensity, disfiguration;

                shader.safeGetUniform("BotaniaGrainIntensity").set(DEFAULT_GRAIN_INTENSITY);
                shader.safeGetUniform("BotaniaDisfiguration").set(DEFAULT_DISFIGURATION);
            }

            VertexConsumer glitch = buffer.getBuffer(RenderHelper.getDopplegangerLayer(texture));
            VertexConsumer vc = VertexMultiConsumer.create(glitch, ivertexbuilder);
            modelCrown.renderToBuffer(matrixStack, glitch, lightness, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

            irendertypebuffer$impl.endBatch();

        }
    }

}
