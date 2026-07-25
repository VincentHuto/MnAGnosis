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

public class PrimalRobeModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation PRIMAL_ROBE_LAYER = new ModelLayerLocation(
            MnAGnosis.rloc("primal_robes"), "main");

    public static final Lazy<PrimalRobeModel<LivingEntity>> chest = Lazy
            .of(() -> new PrimalRobeModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(PRIMAL_ROBE_LAYER)));

    public PrimalRobeModel(ModelPart root) {
        super(root, RenderHelper::entityTranslucent);
    }


    public static LayerDefinition createBodyLayer(EquipmentSlot slot) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition partdefinition = meshdefinition.getRoot();
        if (slot.equals(EquipmentSlot.CHEST)) {
            PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 18).addBox(-4.35F, -0.35F, -2.5F, 8.7F, 3.35F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 27).addBox(-4.35F, 7.25F, -2.5F, 8.7F, 1.25F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(18, 70).addBox(3.5F, 11.0F, -2.49F, 1.0F, 2.0F, 4.98F, new CubeDeformation(0.0F))
                    .texOffs(71, 16).addBox(-4.5F, 11.0F, -2.49F, 1.0F, 2.0F, 4.98F, new CubeDeformation(0.0F))
                    .texOffs(50, 39).addBox(-3.5F, 11.0F, 1.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(67, 45).addBox(-3.5F, 11.0F, -2.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(40, 73).addBox(-1.5F, 10.5F, -2.7F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 0).addBox(-4.325F, 0.0F, -2.375F, 8.65F, 12.0F, 4.7F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition armorBody_r1 = body.addOrReplaceChild("armorBody_r1", CubeListBuilder.create().texOffs(73, 72).addBox(-1.5F, -1.375F, -0.2875F, 2.75F, 2.75F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, 2.0F, -2.8125F, 0.0F, 0.0F, 0.7854F));

            PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 49).addBox(-3.95F, 7.35F, -2.5F, 4.5F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 58).addBox(-2.65F, -4.25F, -2.499F, 3.0F, 5.0F, 4.998F, new CubeDeformation(0.0F))
                    .texOffs(28, 0).addBox(-4.35F, -1.25F, -2.51F, 5.0F, 8.0F, 4.995F, new CubeDeformation(0.0F))
                    .texOffs(51, 0).addBox(-4.45F, 6.75F, -2.75F, 5.5F, 1.25F, 5.5F, new CubeDeformation(0.0F)), PartPose.offset(-3.7F, 2.0F, 0.0F));

            PartDefinition armorRightArm_r1 = right_arm.addOrReplaceChild("armorRightArm_r1", CubeListBuilder.create().texOffs(21, 43).addBox(-3.625F, -2.5F, -2.573F, 6.0F, 3.0F, 4.995F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.725F, -0.8F, 0.075F, 0.0F, 0.0F, -0.7418F));

            PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(50, 8).addBox(-0.75F, 6.75F, -2.75F, 5.5F, 1.25F, 5.5F, new CubeDeformation(0.0F))
                    .texOffs(49, 16).addBox(-0.75F, 7.35F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(17, 59).addBox(-0.35F, -4.25F, -2.499F, 3.0F, 5.0F, 4.998F, new CubeDeformation(0.0F))
                    .texOffs(28, 14).addBox(-0.65F, -1.25F, -2.51F, 5.0F, 8.0F, 4.995F, new CubeDeformation(0.0F)), PartPose.offset(3.7F, 2.0F, 0.0F));

            PartDefinition armorLeftArm_r1 = left_arm.addOrReplaceChild("armorLeftArm_r1", CubeListBuilder.create().texOffs(44, 43).addBox(-2.375F, -2.5F, -2.573F, 6.0F, 3.0F, 4.995F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.725F, -0.8F, 0.075F, 0.0F, 0.0F, 0.7418F));

        }
        return LayerDefinition.create(meshdefinition, 128, 128);

    }


    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
//        head.render(poseStack, buffer, packedLight, packedOverlay);
        body.render(poseStack, buffer, packedLight, packedOverlay);
       leftArm.render(poseStack, buffer, packedLight, packedOverlay);
//        rightLeg.render(poseStack, buffer, packedLight, packedOverlay);
//        leftLeg.render(poseStack, buffer, packedLight, packedOverlay);
       rightArm.render(poseStack, buffer, packedLight, packedOverlay);

    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                          float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}

