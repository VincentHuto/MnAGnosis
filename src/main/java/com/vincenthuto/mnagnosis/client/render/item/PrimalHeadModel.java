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

public class PrimalHeadModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation PRIMAL_CROWN_LAYER = new ModelLayerLocation(
            MnAGnosis.rloc("primal_crown"), "main");


    public static final Lazy<PrimalHeadModel<LivingEntity>> helmet = Lazy
            .of(() -> new PrimalHeadModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(PRIMAL_CROWN_LAYER)));

    public PrimalHeadModel(ModelPart root) {
        super(root, RenderHelper::entityTranslucent);
    }

    public static LayerDefinition createHeadLayer(EquipmentSlot slot) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition partdefinition = meshdefinition.getRoot();
        if (slot.equals(EquipmentSlot.HEAD)) {
            PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(21, 34).addBox(-1.0F, -10.0F, 6.0F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition runes_r1 = head.addOrReplaceChild("runes_r1", CubeListBuilder.create().texOffs(76, 59).addBox(-1.0F, -34.0F, 6.0F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

            PartDefinition runes_r2 = head.addOrReplaceChild("runes_r2", CubeListBuilder.create().texOffs(75, 10).addBox(-1.0F, -34.0F, 6.0F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 1.0472F, 0.0F));

            PartDefinition runes_r3 = head.addOrReplaceChild("runes_r3", CubeListBuilder.create().texOffs(75, 5).addBox(-1.0F, -34.0F, 6.0F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

            PartDefinition runes_r4 = head.addOrReplaceChild("runes_r4", CubeListBuilder.create().texOffs(75, 0).addBox(-1.0F, -34.0F, 6.0F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

            PartDefinition runes_r5 = head.addOrReplaceChild("runes_r5", CubeListBuilder.create().texOffs(74, 66).addBox(-1.0F, -34.0F, 6.0F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        }

        return LayerDefinition.create(meshdefinition, 128, 128);

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
//        head.render(poseStack, buffer, packedLight, packedOverlay);
//        body.render(poseStack, buffer, packedLight, packedOverlay);
//        leftArm.render(poseStack, buffer, packedLight, packedOverlay);
//        rightLeg.render(poseStack, buffer, packedLight, packedOverlay);
//        leftLeg.render(poseStack, buffer, packedLight, packedOverlay);
//        rightArm.render(poseStack, buffer, packedLight, packedOverlay);

    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                          float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}

