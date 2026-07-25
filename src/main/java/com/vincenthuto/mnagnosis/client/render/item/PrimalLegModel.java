package com.vincenthuto.mnagnosis.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.client.shader.core.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.Lazy;

public class PrimalLegModel<T extends LivingEntity> extends HumanoidModel<T> {


    public static final ModelLayerLocation PRIMAL_LEG_LAYER = new ModelLayerLocation(
            MnAGnosis.rloc("primal_leg_wraps"), "main");

    public static final Lazy<PrimalLegModel<LivingEntity>> legs = Lazy
            .of(() -> new PrimalLegModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(PRIMAL_LEG_LAYER)));

    public PrimalLegModel(ModelPart root) {
        super(root, RenderHelper::entityTranslucent);
    }


    public static LayerDefinition createLeggingLayers(EquipmentSlot slot) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition partdefinition = meshdefinition.getRoot();
        if (slot.equals(EquipmentSlot.LEGS)) {
            PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(55, 66).addBox(-2.26F, -0.05F, -2.026F, 4.5F, 0.55F, 4.175F, new CubeDeformation(0.0F))
                    .texOffs(29, 28).addBox(-2.425F, 0.4F, -2.34F, 4.7F, 8.6F, 4.6F, new CubeDeformation(0.0F))
                    .texOffs(63, 52).addBox(-2.425F, -0.2F, -2.34F, 4.7F, 0.6F, 4.6F, new CubeDeformation(0.0F))
                    .texOffs(67, 49).addBox(-2.0433F, -0.0077F, 1.985F, 4.05F, 1.55F, 0.125F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 12.0F, 0.0F));

            PartDefinition armorLeftLeg_r1 = left_leg.addOrReplaceChild("armorLeftLeg_r1", CubeListBuilder.create().texOffs(71, 24).addBox(-2.125F, -5.05F, 0.05F, 4.25F, 10.1F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.9127F, 5.0491F, 0.5225F, 2.5303F, 1.4993F, 2.1821F));

            PartDefinition armorLeftLeg_r2 = left_leg.addOrReplaceChild("armorLeftLeg_r2", CubeListBuilder.create().texOffs(76, 49).addBox(-1.0375F, -5.075F, -0.0468F, 3.075F, 2.0F, 0.1F, new CubeDeformation(0.0F))
                    .texOffs(64, 72).addBox(-2.0375F, -3.075F, -0.0468F, 4.075F, 8.15F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2031F, 5.5417F, 2.6168F, 0.041F, 0.0149F, -0.3488F));

            PartDefinition armorLeftLeg_r3 = left_leg.addOrReplaceChild("armorLeftLeg_r3", CubeListBuilder.create().texOffs(0, 69).addBox(-4.0F, -3.9F, 0.0F, 4.05F, 10.7F, 0.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9817F, 4.2173F, 2.51F, 0.0436F, 0.0F, 0.0F));

            PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(67, 39).addBox(-2.24F, -0.05F, -2.026F, 4.5F, 0.55F, 4.175F, new CubeDeformation(0.0F))
                    .texOffs(0, 34).addBox(-2.275F, 0.4F, -2.34F, 4.7F, 8.6F, 4.6F, new CubeDeformation(0.0F))
                    .texOffs(34, 66).addBox(-2.275F, -0.2F, -2.34F, 4.7F, 0.6F, 4.6F, new CubeDeformation(0.0F))
                    .texOffs(71, 35).addBox(-2.0067F, -0.0077F, 1.985F, 4.05F, 1.55F, 0.125F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

            PartDefinition armorRightLeg_r1 = right_leg.addOrReplaceChild("armorRightLeg_r1", CubeListBuilder.create().texOffs(55, 72).addBox(-2.125F, -5.05F, 0.05F, 4.25F, 10.1F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9127F, 5.0491F, 0.5225F, 2.5303F, -1.4993F, -2.1821F));

            PartDefinition armorRightLeg_r2 = right_leg.addOrReplaceChild("armorRightLeg_r2", CubeListBuilder.create().texOffs(21, 39).addBox(-2.0375F, -5.075F, -0.0468F, 3.075F, 2.0F, 0.1F, new CubeDeformation(0.0F))
                    .texOffs(31, 73).addBox(-2.0375F, -3.075F, -0.0468F, 4.075F, 8.15F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2031F, 5.5417F, 2.6168F, 0.041F, -0.0149F, 0.3488F));

            PartDefinition armorRightLeg_r3 = right_leg.addOrReplaceChild("armorRightLeg_r3", CubeListBuilder.create().texOffs(9, 70).addBox(-0.05F, -3.9F, 0.0F, 4.05F, 10.7F, 0.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9817F, 4.2173F, 2.51F, 0.0436F, 0.0F, 0.0F));
        }
        return LayerDefinition.create(meshdefinition, 128, 128);
    }


    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
//        head.render(poseStack, buffer, packedLight, packedOverlay);
//        body.render(poseStack, buffer, packedLight, packedOverlay);
//        leftArm.render(poseStack, buffer, packedLight, packedOverlay);
       rightLeg.render(poseStack, buffer, packedLight, packedOverlay);
        leftLeg.render(poseStack, buffer, packedLight, packedOverlay);
//        rightArm.render(poseStack, buffer, packedLight, packedOverlay);

    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                          float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}

