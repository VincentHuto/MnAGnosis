package com.vincenthuto.mnagnosis.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.client.registry.RenderTypeRegistry;
import com.vincenthuto.mnagnosis.client.shader.core.CoreShaders;
import com.vincenthuto.mnagnosis.client.shader.core.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.Lazy;

public class PrimalCrownModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            MnAGnosis.rloc("primal_crown_old"), "main");

    public static final Lazy<PrimalCrownModel<LivingEntity>> helmet = Lazy
            .of(() -> new PrimalCrownModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER_LOCATION)));

    private final ModelPart head;

    public PrimalCrownModel(ModelPart root) {
        super(root, RenderHelper::getNoiseLayer);
        this.head = root.getChild("head");
    }


    @SuppressWarnings("unused")
    public static LayerDefinition createHeadLayer(EquipmentSlot slot) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition partdefinition = meshdefinition.getRoot();
        if (slot.equals(EquipmentSlot.HEAD)) {
            PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, 0.0F));

            PartDefinition bone = head.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(90, 159).addBox(-6.5F, 1.75F, -6.5F, 13.0F, 0.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.9F, 0.15F, 0.1745F, 0.0436F, -0.1745F));

            PartDefinition hat3 = head.addOrReplaceChild("hat3", CubeListBuilder.create().texOffs(105, 142).addBox(-5.0161F, 0.1785F, -4.4878F, 0.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.9F, 0.15F, 0.1745F, 0.0436F, -0.1745F));

            PartDefinition hat4 = head.addOrReplaceChild("hat4", CubeListBuilder.create().texOffs(103, 153).addBox(-4.5436F, -0.0765F, -5.0161F, 9.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.9F, 0.15F, 0.1745F, 0.0436F, -0.1745F));

            PartDefinition hat2 = head.addOrReplaceChild("hat2", CubeListBuilder.create().texOffs(106, 145).addBox(5.5161F, -0.1785F, -4.5122F, 0.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -8.9F, 0.15F, 0.1745F, 0.0436F, -0.1745F));

            PartDefinition hat5 = head.addOrReplaceChild("hat5", CubeListBuilder.create().texOffs(102, 146).addBox(-4.4564F, -0.1735F, 5.0161F, 9.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.9F, 0.15F, 0.1745F, 0.0436F, -0.1745F));

            PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(16, 159).addBox(-5.5161F, 1.1785F, -4.4878F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                    .texOffs(16, 159).addBox(4.5161F, 0.8215F, -4.5122F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                    .texOffs(15, 167).addBox(-4.5436F, 1.1735F, -5.5161F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(15, 167).addBox(-4.4564F, 0.8265F, 4.5161F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.9F, 0.15F, 0.1745F, 0.0436F, -0.1745F));


        }
        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
      head.render(poseStack, buffer, packedLight, packedOverlay);

    }

    public ModelPart getHeadModel(){
        return head;
    }


    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                          float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}