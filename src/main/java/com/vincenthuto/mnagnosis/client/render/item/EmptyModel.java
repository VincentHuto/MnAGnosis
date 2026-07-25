package com.vincenthuto.mnagnosis.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.client.shader.core.CoreShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.Lazy;

public class EmptyModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            MnAGnosis.rloc("empty"), "main");

    public static final Lazy<EmptyModel<LivingEntity>> lazyModel = Lazy
            .of(() -> new EmptyModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER_LOCATION)));


    public EmptyModel(ModelPart root) {
        super(root, RenderType::entityTranslucent);
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createHeadLayer(EquipmentSlot slot) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition partdefinition = meshdefinition.getRoot();

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

}