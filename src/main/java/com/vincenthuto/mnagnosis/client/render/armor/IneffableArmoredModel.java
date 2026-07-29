package com.vincenthuto.mnagnosis.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class IneffableArmoredModel<T extends LivingEntity>
        extends EntityModel<T> {

    public static final ModelLayerLocation INEFFABLE_ARMORED_ROBES_LAYER =
            new ModelLayerLocation(
                    MnAGnosis.rloc("ineffable_armored_robes"),
                    "main"
            );

    private final ModelPart whole;
    private final ModelPart body;
    private final ModelPart clothBackR1;
    private final ModelPart clothBackL1;
    private final ModelPart clothBackR2;
    private final ModelPart clothBackR3;
    private final ModelPart clothBackL2;
    private final ModelPart clothBackL3;
    private final ModelPart sideclothR1;
    private final ModelPart sideclothR2;
    private final ModelPart sideclothR3;
    private final ModelPart sideclothR4;
    private final ModelPart sideclothR5;
    private final ModelPart sideclothR6;
    private final ModelPart cloak;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public IneffableArmoredModel(ModelPart root) {
        this.whole = root.getChild("whole");
        this.body = this.whole.getChild("body3");

        ModelPart clothBack = this.body.getChild("ClothBack");
        ModelPart clothBack1 = clothBack.getChild("ClothBack1");
        ModelPart clothBack2 = clothBack1.getChild("ClothBack2");
        this.clothBackR1 = clothBack1.getChild("ClothBackR1");
        this.clothBackL1 = clothBack1.getChild("ClothBackL1");
        this.clothBackR2 = clothBack2.getChild("ClothBackR2");
        this.clothBackR3 = clothBack2.getChild("ClothBackR3");
        this.clothBackL2 = clothBack2.getChild("ClothBackL2");
        this.clothBackL3 = clothBack2.getChild("ClothBackL3");

        ModelPart sideclothRight = this.body.getChild("SideclothR");
        this.sideclothR4 = sideclothRight.getChild("SideclothR4");
        this.sideclothR5 = this.sideclothR4.getChild("SideclothR5");
        this.sideclothR6 = this.sideclothR5.getChild("SideclothR6");
        ModelPart sideclothLeft = this.body.getChild("SideclothL");
        this.sideclothR1 = sideclothLeft.getChild("SideclothR1");
        this.sideclothR2 = this.sideclothR1.getChild("SideclothR2");
        this.sideclothR3 = this.sideclothR2.getChild("SideclothR13");

        this.cloak = this.body.getChild("cloak");
        this.cloak1 = this.cloak.getChild("Cloak1");
        this.cloak2 = this.cloak1.getChild("Cloak2");
        this.cloak3 = this.cloak2.getChild("Cloak3");
        this.rightArm = this.whole.getChild("rightArm");
        this.leftArm = this.whole.getChild("leftArm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition whole = root.addOrReplaceChild(
                "whole",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 6.0F, 0.0F)
        );
        PartDefinition body = whole.addOrReplaceChild(
                "body3",
                CubeListBuilder.create()
                        .texOffs(0, 18)
                        .addBox(
                                4.4372F, -0.6082F, -3.125F,
                                1.0F, 16.0F, 6.0F,
                                CubeDeformation.NONE
                        )
                        .texOffs(0, 18)
                        .addBox(
                                -5.1247F, -0.6082F, -3.125F,
                                1.0F, 16.0F, 6.0F,
                                CubeDeformation.NONE
                        )
                        .texOffs(17, 36)
                        .addBox(
                                -5.625F, -0.625F, -4.0625F,
                                3.0F, 10.0F, 1.0F,
                                CubeDeformation.NONE
                        )
                        .texOffs(37, 68)
                        .addBox(
                                2.125F, -0.625F, -4.0625F,
                                3.0F, 10.0F, 1.0F,
                                CubeDeformation.NONE
                        )
                        .texOffs(4, 93)
                        .addBox(
                                -4.875F, -0.625F, 2.3125F,
                                10.0F, 10.0F, 1.0F,
                                CubeDeformation.NONE
                        )
                        .texOffs(38, 85)
                        .addBox(
                                -5.125F, -0.375F, 3.25F,
                                10.0F, 16.0F, 1.0F,
                                CubeDeformation.NONE
                        ),
                PartPose.offset(0.0F, -7.0F, 0.0F)
        );

        PartDefinition clothBack = body.addOrReplaceChild(
                "ClothBack",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 15.375F, 5.5F)
        );
        PartDefinition clothBack1 = clothBack.addOrReplaceChild(
                "ClothBack1",
                CubeListBuilder.create(),
                PartPose.offset(5.0F, -4.0F, -0.25F)
        );
        clothBack1.addOrReplaceChild(
                "ClothBackR1",
                CubeListBuilder.create().texOffs(26, 60)
                        .addBox(
                                -5.125F, 4.0177F, -2.4333F,
                                5.0F, 6.0F, 1.0F,
                                CubeDeformation.NONE
                        ),
                PartPose.offsetAndRotation(
                        0.0F, 0.0F, 0.0F,
                        0.1047F, 0.0F, 0.0F
                )
        );
        clothBack1.addOrReplaceChild(
                "ClothBackL1",
                CubeListBuilder.create().texOffs(63, 42)
                        .addBox(
                                -5.125F, 4.0177F, -2.4333F,
                                5.0F, 6.0F, 1.0F,
                                CubeDeformation.NONE
                        ),
                PartPose.offsetAndRotation(
                        -5.0F, 0.0F, 0.0F,
                        0.1047F, 0.0F, 0.0F
                )
        );
        PartDefinition clothBack2 = clothBack1.addOrReplaceChild(
                "ClothBack2",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(
                        -2.5F, 10.0F, 0.0F,
                        0.3054F, 0.0F, 0.0F
                )
        );
        clothBack2.addOrReplaceChild(
                "ClothBackR2",
                CubeListBuilder.create().texOffs(58, 68)
                        .addBox(
                                -1.125F, 9.234F, -3.5426F,
                                1.0F, 2.0F, 1.0F,
                                CubeDeformation.NONE
                        ),
                PartPose.offsetAndRotation(
                        2.5F, -10.0F, 0.0F,
                        0.2269F, 0.0F, 0.0F
                )
        );
        clothBack2.addOrReplaceChild(
                "ClothBackR3",
                CubeListBuilder.create().texOffs(37, 13)
                        .addBox(
                                -5.125F, 9.234F, -3.5426F,
                                4.0F, 3.0F, 1.0F,
                                CubeDeformation.NONE
                        ),
                PartPose.offsetAndRotation(
                        2.5F, -10.0F, 0.0F,
                        0.2269F, 0.0F, 0.0F
                )
        );
        clothBack2.addOrReplaceChild(
                "ClothBackL2",
                CubeListBuilder.create().texOffs(63, 68)
                        .addBox(
                                -5.125F, 9.234F, -3.5426F,
                                1.0F, 2.0F, 1.0F,
                                CubeDeformation.NONE
                        ),
                PartPose.offsetAndRotation(
                        -2.5F, -10.0F, 0.0F,
                        0.2269F, 0.0F, 0.0F
                )
        );
        clothBack2.addOrReplaceChild(
                "ClothBackL3",
                CubeListBuilder.create().texOffs(63, 52)
                        .addBox(
                                -4.125F, 9.234F, -3.5426F,
                                4.0F, 3.0F, 1.0F,
                                CubeDeformation.NONE
                        ),
                PartPose.offsetAndRotation(
                        -2.5F, -10.0F, 0.0F,
                        0.2269F, 0.0F, 0.0F
                )
        );

        PartDefinition sideclothRight = body.addOrReplaceChild(
                "SideclothR",
                CubeListBuilder.create(),
                PartPose.offset(-4.75F, 15.3125F, 0.0F)
        );
        PartDefinition sideclothR4 = sideclothRight.addOrReplaceChild(
                "SideclothR4",
                CubeListBuilder.create().texOffs(57, 57).mirror()
                        .addBox(
                                -9.1879F, 1.0053F, 0.0F,
                                1.0F, 4.0F, 6.0F,
                                CubeDeformation.NONE
                        )
                        .mirror(false),
                PartPose.offsetAndRotation(
                        8.8747F, 0.0793F, -3.125F,
                        0.0F, 0.0F, 0.1222F
                )
        );
        PartDefinition sideclothR5 = sideclothR4.addOrReplaceChild(
                "SideclothR5",
                CubeListBuilder.create().texOffs(63, 24).mirror()
                        .addBox(
                                -0.7145F, -2.9804F, -3.125F,
                                1.0F, 4.0F, 6.0F,
                                CubeDeformation.NONE
                        )
                        .mirror(false),
                PartPose.offsetAndRotation(
                        -9.3323F, 7.772F, 3.125F,
                        0.0F, 0.0F, 0.2967F
                )
        );
        sideclothR5.addOrReplaceChild(
                "SideclothR6",
                CubeListBuilder.create().texOffs(63, 33).mirror()
                        .addBox(
                                -2.3163F, -1.536F, -3.125F,
                                1.0F, 3.0F, 6.0F,
                                CubeDeformation.NONE
                        )
                        .mirror(false),
                PartPose.offsetAndRotation(
                        0.6575F, 3.008F, 0.0F,
                        0.0F, 0.0F, 0.5236F
                )
        );

        PartDefinition sideclothLeft = body.addOrReplaceChild(
                "SideclothL",
                CubeListBuilder.create(),
                PartPose.offset(4.75F, 15.3125F, 0.0F)
        );
        PartDefinition sideclothR1 = sideclothLeft.addOrReplaceChild(
                "SideclothR1",
                CubeListBuilder.create().texOffs(57, 57)
                        .addBox(
                                0.3002F, 0.1168F, -3.125F,
                                1.0F, 4.0F, 6.0F,
                                CubeDeformation.NONE
                        ),
                PartPose.offsetAndRotation(
                        -0.625F, 0.0F, 0.0F,
                        0.0F, 0.0F, -0.1222F
                )
        );
        PartDefinition sideclothR2 = sideclothR1.addOrReplaceChild(
                "SideclothR2",
                CubeListBuilder.create().texOffs(63, 24)
                        .addBox(
                                -0.0001F, -2.8533F, -3.125F,
                                1.0F, 4.0F, 6.0F,
                                CubeDeformation.NONE
                        ),
                PartPose.offsetAndRotation(
                        1.1345F, 6.8454F, 0.0F,
                        0.0F, 0.0F, -0.2967F
                )
        );
        sideclothR2.addOrReplaceChild(
                "SideclothR13",
                CubeListBuilder.create().texOffs(63, 33)
                        .addBox(
                                1.5F, -1.2832F, -3.125F,
                                1.0F, 3.0F, 6.0F,
                                CubeDeformation.NONE
                        ),
                PartPose.offsetAndRotation(
                        -0.6575F, 3.008F, 0.0F,
                        0.0F, 0.0F, -0.5236F
                )
        );

        PartDefinition cloak = body.addOrReplaceChild(
                "cloak",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(
                        0.0F, -1.5F, 1.75F,
                        -0.0436F, 0.0F, 0.0F
                )
        );
        cloak.addOrReplaceChild(
                "CloakTL",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(
                        0.0F, -1.25F, 0.0F,
                        0.1396F, 0.0F, 0.0F
                )
        );
        cloak.addOrReplaceChild(
                "CloakTR",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(
                        0.0F, -1.25F, 0.0F,
                        0.1396F, 0.0F, 0.0F
                )
        );
        PartDefinition cloak1 = cloak.addOrReplaceChild(
                "Cloak1",
                CubeListBuilder.create().texOffs(107, 54)
                        .addBox(
                                -5.35F, 2.525F, 1.225F,
                                11.0F, 15.0F, 1.0F,
                                new CubeDeformation(0.1F)
                        ),
                PartPose.offsetAndRotation(
                        0.0F, -1.25F, 0.0F,
                        0.1396F, 0.0F, 0.0F
                )
        );
        PartDefinition cloak2 = cloak1.addOrReplaceChild(
                "Cloak2",
                CubeListBuilder.create().texOffs(104, 83)
                        .addBox(
                                -5.35F, -0.1626F, -0.4785F,
                                11.0F, 5.0F, 1.0F,
                                new CubeDeformation(0.1F)
                        ),
                PartPose.offsetAndRotation(
                        0.0F, 17.5418F, 1.739F,
                        0.3069F, 0.0F, 0.0F
                )
        );
        cloak2.addOrReplaceChild(
                "Cloak3",
                CubeListBuilder.create().texOffs(103, 98)
                        .addBox(
                                -5.35F, -0.5356F, -0.2639F,
                                11.0F, 5.0F, 1.0F,
                                new CubeDeformation(0.1F)
                        ),
                PartPose.offsetAndRotation(
                        0.0F, 5.2148F, 0.004F,
                        0.4466F, 0.0F, 0.0F
                )
        );

        PartDefinition rightArm = whole.addOrReplaceChild(
                "rightArm",
                CubeListBuilder.create(),
                PartPose.offset(-6.25F, -4.5F, 0.0F)
        );
        rightArm.addOrReplaceChild(
                "ShoulderR_16_45_0eedefc6_r1",
                CubeListBuilder.create().texOffs(100, 20).mirror()
                        .addBox(
                                -3.06F, -3.31F, -2.9869F,
                                5.0F, 4.0F, 7.0F,
                                new CubeDeformation(0.26F)
                        )
                        .mirror(false),
                PartPose.offsetAndRotation(
                        -2.076F, 2.0608F, -0.4656F,
                        0.0F, 0.0F, 0.4363F
                )
        );
        rightArm.addOrReplaceChild(
                "ShoulderR_16_45_0eedefc6_r2",
                CubeListBuilder.create().texOffs(79, 19).mirror()
                        .addBox(
                                -4.0575F, -3.3075F, -2.8175F,
                                6.0F, 4.0F, 7.0F,
                                new CubeDeformation(0.27F)
                        )
                        .mirror(false),
                PartPose.offsetAndRotation(
                        -0.875F, 0.9375F, -0.875F,
                        0.0F, 0.0F, 1.1345F
                )
        );

        PartDefinition leftArm = whole.addOrReplaceChild(
                "leftArm",
                CubeListBuilder.create(),
                PartPose.offset(6.25F, -4.5F, 0.0F)
        );
        leftArm.addOrReplaceChild(
                "ShoulderR_17_45_0eedefc7_r1",
                CubeListBuilder.create().texOffs(100, 20)
                        .addBox(
                                -1.94F, -3.31F, -2.9869F,
                                5.0F, 4.0F, 7.0F,
                                new CubeDeformation(0.26F)
                        ),
                PartPose.offsetAndRotation(
                        2.076F, 2.0608F, -0.4656F,
                        0.0F, 0.0F, -0.4363F
                )
        );
        leftArm.addOrReplaceChild(
                "ShoulderR_17_45_0eedefc7_r2",
                CubeListBuilder.create().texOffs(79, 19)
                        .addBox(
                                -1.9425F, -3.3075F, -2.8175F,
                                6.0F, 4.0F, 7.0F,
                                new CubeDeformation(0.27F)
                        ),
                PartPose.offsetAndRotation(
                        0.875F, 0.9375F, -0.875F,
                        0.0F, 0.0F, -1.1345F
                )
        );

        whole.addOrReplaceChild(
                "leftLeg",
                CubeListBuilder.create(),
                PartPose.offset(2.375F, 8.0F, 0.0F)
        );
        whole.addOrReplaceChild(
                "rightLeg",
                CubeListBuilder.create(),
                PartPose.offset(-2.375F, 8.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 128, 128);
    }

    public void copyPoseFrom(HumanoidModel<T> parent) {
        IneffableArmoredPose.copyPart(
                parent.body,
                this.body,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                -7.0F,
                0.0F
        );
        IneffableArmoredPose.copyPart(
                parent.rightArm,
                this.rightArm,
                -5.0F,
                2.0F,
                0.0F,
                -6.25F,
                -4.5F,
                0.0F
        );
        IneffableArmoredPose.copyPart(
                parent.leftArm,
                this.leftArm,
                5.0F,
                2.0F,
                0.0F,
                6.25F,
                -4.5F,
                0.0F
        );
        this.whole.y = 6.0F;
    }

    @Override
    public void setupAnim(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        animateCloth(limbSwing, limbSwingAmount, ageInTicks);
    }

    public void animateCloth(
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks
    ) {
        float idle = Mth.sin(ageInTicks * 0.067F);
        float walk = Mth.sin(limbSwing * 0.6662F) * limbSwingAmount;

        this.cloak.xRot =
                -0.0436F + idle * 0.025F + Math.abs(walk) * 0.05F;
        this.cloak1.xRot =
                0.1396F + Mth.sin(ageInTicks * 0.067F + 0.5F) * 0.035F;
        this.cloak2.xRot = 0.3069F
                + Mth.sin(ageInTicks * 0.067F + 1.0F) * 0.04F
                + walk * 0.04F;
        this.cloak3.xRot = 0.4466F
                + Mth.sin(ageInTicks * 0.067F + 1.5F) * 0.035F
                + walk * 0.05F;

        this.clothBackR1.xRot = 0.1047F + idle * 0.03F + walk * 0.07F;
        this.clothBackL1.xRot = 0.1047F + idle * 0.03F - walk * 0.07F;
        this.clothBackR2.xRot = 0.2269F
                + Mth.sin(ageInTicks * 0.067F + 0.6F) * 0.035F
                + walk * 0.08F;
        this.clothBackR3.xRot = 0.2269F
                + Mth.sin(ageInTicks * 0.067F + 1.0F) * 0.04F
                + walk * 0.10F;
        this.clothBackL2.xRot = 0.2269F
                + Mth.sin(ageInTicks * 0.067F + 0.8F) * 0.035F
                - walk * 0.08F;
        this.clothBackL3.xRot = 0.2269F
                + Mth.sin(ageInTicks * 0.067F + 1.2F) * 0.04F
                - walk * 0.10F;

        this.sideclothR1.zRot = -0.1222F - idle * 0.02F - walk * 0.05F;
        this.sideclothR2.zRot = -0.2967F
                - Mth.sin(ageInTicks * 0.067F + 0.7F) * 0.025F
                - walk * 0.06F;
        this.sideclothR3.zRot = -0.5236F
                - Mth.sin(ageInTicks * 0.067F + 1.2F) * 0.03F
                - walk * 0.07F;
        this.sideclothR4.zRot = 0.1222F + idle * 0.02F + walk * 0.05F;
        this.sideclothR5.zRot = 0.2967F
                + Mth.sin(ageInTicks * 0.067F + 0.7F) * 0.025F
                + walk * 0.06F;
        this.sideclothR6.zRot = 0.5236F
                + Mth.sin(ageInTicks * 0.067F + 1.2F) * 0.03F
                + walk * 0.07F;
    }

    @Override
    public void renderToBuffer(
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        this.whole.render(
                poseStack,
                buffer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );
    }
}
