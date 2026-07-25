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

public class PrimalBootModel<T extends LivingEntity> extends HumanoidModel<T> {


    public static final ModelLayerLocation PRIMAL_BOOTS_LAYER = new ModelLayerLocation(
            MnAGnosis.rloc("primal_boots"), "main");

    public static final Lazy<PrimalBootModel<LivingEntity>> boots = Lazy
            .of(() -> new PrimalBootModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(PRIMAL_BOOTS_LAYER)));

    public PrimalBootModel(ModelPart root) {
        super(root, RenderHelper::entityTranslucent);
    }

    public static LayerDefinition createBootLayer(EquipmentSlot slot) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition partdefinition = meshdefinition.getRoot();
        if (slot.equals(EquipmentSlot.FEET)) {

            PartDefinition left_boot = partdefinition.addOrReplaceChild("left_boot", CubeListBuilder.create().texOffs(21, 52).addBox(-0.575F, -3.14F, -2.58F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(50, 25).addBox(-0.425F, -1.05F, -3.09F, 4.7F, 1.375F, 5.35F, new CubeDeformation(0.0F))
                    .texOffs(34, 59).addBox(-0.425F, -2.15F, -2.34F, 4.7F, 1.125F, 4.6F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

            PartDefinition right_boot = partdefinition.addOrReplaceChild("right_boot", CubeListBuilder.create().texOffs(42, 52).addBox(-4.425F, -3.14F, -2.58F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(50, 32).addBox(-4.275F, -1.05F, -3.09F, 4.7F, 1.375F, 5.35F, new CubeDeformation(0.0F))
                    .texOffs(55, 59).addBox(-4.275F, -2.15F, -2.34F, 4.7F, 1.125F, 4.6F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 12.0F, 0.0F));
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

