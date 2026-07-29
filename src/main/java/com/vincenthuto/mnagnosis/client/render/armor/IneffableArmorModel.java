package com.vincenthuto.mnagnosis.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.MnAGnosis;
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

public final class IneffableArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation INEFFABLE_ROBES_LAYER =
            new ModelLayerLocation(MnAGnosis.rloc("ineffable_robes"), "main");
    private static final float REAR_OFFSET_MULTIPLIER = 0.5F;

    private final ModelPart hoodLeft;
    private final ModelPart hoodRight;
    private final ModelPart hoodTop;
    private final ModelPart hoodBack;
    private final ModelPart bodyShell;
    private final ModelPart bodyPositiveX;
    private final ModelPart bodyNegativeX;
    private final ModelPart bodyFront;
    private final ModelPart bodyBack;
    private final ModelPart clothBack;
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
    private final ModelPart sideclothL;
    private final ModelPart sideclothR;
    private final ModelPart cloak;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;

    public IneffableArmorModel(ModelPart root) {
        super(root);

        ModelPart hood = this.head.getChild("Hood1");
        this.hoodLeft = hood.getChild("hood_left");
        this.hoodRight = hood.getChild("hood_right");
        this.hoodTop = hood.getChild("hood_top");
        this.hoodBack = hood.getChild("Hood2");
        this.bodyShell = this.body.getChild("body_shell");
        this.bodyPositiveX = this.bodyShell.getChild("positive_x");
        this.bodyNegativeX = this.bodyShell.getChild("negative_x");
        this.bodyFront = this.bodyShell.getChild("front");
        this.bodyBack = this.bodyShell.getChild("back");
        this.clothBack = this.body.getChild("ClothBack");
        ModelPart clothBack1 = this.clothBack.getChild("ClothBack1");
        ModelPart clothBack2 = clothBack1.getChild("ClothBack2");
        this.clothBackR1 = clothBack1.getChild("ClothBackR1");
        this.clothBackL1 = clothBack1.getChild("ClothBackL1");
        this.clothBackR2 = clothBack2.getChild("ClothBackR2");
        this.clothBackR3 = clothBack2.getChild("ClothBackR3");
        this.clothBackL2 = clothBack2.getChild("ClothBackL2");
        this.clothBackL3 = clothBack2.getChild("ClothBackL3");

        this.sideclothL = this.body.getChild("SideclothL");
        this.sideclothR1 = this.sideclothL.getChild("SideclothR1");
        this.sideclothR2 = this.sideclothR1.getChild("SideclothR2");
        this.sideclothR3 = this.sideclothR2.getChild("SideclothR3");
        this.sideclothR = this.body.getChild("SideclothR");
        this.sideclothR4 = this.sideclothR.getChild("SideclothR4");
        this.sideclothR5 = this.sideclothR4.getChild("SideclothR5");
        this.sideclothR6 = this.sideclothR5.getChild("SideclothR6");

        this.cloak = this.body.getChild("cloak");
        this.cloak1 = this.cloak.getChild("Cloak1");
        this.cloak2 = this.cloak1.getChild("Cloak2");
        this.cloak3 = this.cloak2.getChild("Cloak3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create(),
                PartPose.ZERO
        );
        PartDefinition hood1 = head.addOrReplaceChild("Hood1",
                CubeListBuilder.create(),
                PartPose.ZERO);
        hood1.addOrReplaceChild("hood_left",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.5F, -9.0F, -4.6F, 1.0F, 9.0F, 9.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        hood1.addOrReplaceChild("hood_right",
                CubeListBuilder.create().texOffs(8, 0)
                        .addBox(3.5F, -9.0F, -4.6F, 1.0F, 9.0F, 9.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        hood1.addOrReplaceChild("hood_top",
                CubeListBuilder.create().texOffs(24, 106)
                        .addBox(-3.5F, -9.0F, -4.6F, 7.0F, 1.0F, 9.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        PartDefinition hood2 = hood1.addOrReplaceChild("Hood2",
                CubeListBuilder.create().texOffs(25, 19)
                        .addBox(-4.0F, -9.7F, 2.0F, 8.0F, 9.0F, 3.0F, CubeDeformation.NONE),
                PartPose.rotation(-0.2269F, 0.0F, 0.0F));
        PartDefinition hood3 = hood2.addOrReplaceChild("Hood3", CubeListBuilder.create(),
                PartPose.rotation(-0.3491F, 0.0F, 0.0F));
        hood3.addOrReplaceChild("Hood3_r1",
                CubeListBuilder.create().texOffs(42, 45)
                        .addBox(-3.5F, -10.0F, 3.5F, 7.0F, 8.0F, 3.0F, CubeDeformation.NONE),
                PartPose.rotation(0.2182F, 0.0F, 0.0F));
        PartDefinition hood4 = hood3.addOrReplaceChild("Hood4", CubeListBuilder.create(),
                PartPose.rotation(-0.576F, 0.0F, 0.0F));
        hood4.addOrReplaceChild("Hood4_r1",
                CubeListBuilder.create().texOffs(38, 57)
                        .addBox(-3.0F, -10.7F, 3.5F, 6.0F, 7.0F, 3.0F, CubeDeformation.NONE),
                PartPose.rotation(0.6109F, 0.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.ZERO
        );
        PartDefinition bodyShell = body.addOrReplaceChild(
                "body_shell",
                CubeListBuilder.create(),
                PartPose.ZERO
        );
        bodyShell.addOrReplaceChild("positive_x",
                CubeListBuilder.create().texOffs(0, 18)
                        .addBox(3.3497F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        bodyShell.addOrReplaceChild("negative_x",
                CubeListBuilder.create().texOffs(0, 18)
                        .addBox(-4.2998F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        bodyShell.addOrReplaceChild("front",
                CubeListBuilder.create()
                        .texOffs(17, 36).addBox(-4.1F, -0.5F, -3.25F, 2.0F, 8.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(37, 68).addBox(2.1F, -0.5F, -3.25F, 2.0F, 8.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        bodyShell.addOrReplaceChild("back",
                CubeListBuilder.create()
                        .texOffs(4, 93).addBox(-3.9F, -0.5F, 1.85F, 8.0F, 8.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(38, 85).addBox(-4.1F, -0.5F, 1.4F, 8.0F, 13.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        addRearCloth(body);
        addSideCloth(body);
        addCloak(body);
        addArms(root);
        addLegs(root);
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void addRearCloth(PartDefinition body) {
        PartDefinition clothBack = body.addOrReplaceChild("ClothBack", CubeListBuilder.create(),
                PartPose.offset(0.0F, 12.3F, 4.4F));
        PartDefinition clothBack1 = clothBack.addOrReplaceChild("ClothBack1", CubeListBuilder.create(),
                PartPose.offset(4.0F, 0.0F, -1.0F));
        clothBack1.addOrReplaceChild("ClothBackR1",
                CubeListBuilder.create().texOffs(26, 60)
                        .addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F, CubeDeformation.NONE),
                PartPose.rotation(0.1047F, 0.0F, 0.0F));
        clothBack1.addOrReplaceChild("ClothBackL1",
                CubeListBuilder.create().texOffs(63, 42)
                        .addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));
        PartDefinition clothBack2 = clothBack1.addOrReplaceChild("ClothBack2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-2.0F, 8.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
        clothBack2.addOrReplaceChild("ClothBackR2",
                CubeListBuilder.create().texOffs(58, 68)
                        .addBox(-1.0F, 7.3522F, -2.8768F, 1.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));
        clothBack2.addOrReplaceChild("ClothBackR3",
                CubeListBuilder.create().texOffs(37, 13)
                        .addBox(-4.0F, 7.3522F, -2.8768F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));
        clothBack2.addOrReplaceChild("ClothBackL2",
                CubeListBuilder.create().texOffs(63, 68)
                        .addBox(-4.0F, 7.3522F, -2.8768F, 1.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));
        clothBack2.addOrReplaceChild("ClothBackL3",
                CubeListBuilder.create().texOffs(63, 52)
                        .addBox(-3.0F, 7.3522F, -2.8768F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));
    }

    private static void addSideCloth(PartDefinition body) {
        PartDefinition sideclothL = body.addOrReplaceChild("SideclothL", CubeListBuilder.create(),
                PartPose.offset(3.8F, 12.25F, 0.0F));
        PartDefinition sideclothR1 = sideclothL.addOrReplaceChild("SideclothR1",
                CubeListBuilder.create().texOffs(57, 57)
                        .addBox(-0.008F, 0.063F, -2.5F, 1.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));
        PartDefinition sideclothR2 = sideclothR1.addOrReplaceChild("SideclothR2",
                CubeListBuilder.create().texOffs(63, 24)
                        .addBox(-0.7547F, -0.663F, -2.51F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, -0.2967F));
        sideclothR2.addOrReplaceChild("SideclothR3",
                CubeListBuilder.create().texOffs(63, 33)
                        .addBox(-0.1634F, -0.1744F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition sideclothR = body.addOrReplaceChild("SideclothR", CubeListBuilder.create(),
                PartPose.offset(-3.8F, 12.25F, 0.0F));
        PartDefinition sideclothR4 = sideclothR.addOrReplaceChild("SideclothR4",
                CubeListBuilder.create().texOffs(57, 57).mirror()
                        .addBox(-0.992F, 0.063F, -2.5F, 1.0F, 5.0F, 5.0F, CubeDeformation.NONE)
                        .mirror(false),
                PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));
        PartDefinition sideclothR5 = sideclothR4.addOrReplaceChild("SideclothR5",
                CubeListBuilder.create().texOffs(63, 24).mirror()
                        .addBox(-0.2453F, -0.663F, -2.51F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE)
                        .mirror(false),
                PartPose.offsetAndRotation(-0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, 0.2967F));
        sideclothR5.addOrReplaceChild("SideclothR6",
                CubeListBuilder.create().texOffs(63, 33).mirror()
                        .addBox(-0.8366F, -0.1744F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE)
                        .mirror(false),
                PartPose.offsetAndRotation(0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, 0.5236F));
    }

    private static void addCloak(PartDefinition body) {
        PartDefinition cloak = body.addOrReplaceChild("cloak", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 0.0F, 1.2F, -0.0436F, 0.0F, 0.0F));
        cloak.addOrReplaceChild("CloakTL",
                CubeListBuilder.create().texOffs(98, 72)
                        .addBox(-4.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.1396F, 0.0F, 0.0F));
        cloak.addOrReplaceChild("CloakTR",
                CubeListBuilder.create().texOffs(94, 62)
                        .addBox(2.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.1396F, 0.0F, 0.0F));
        PartDefinition cloak1 = cloak.addOrReplaceChild("Cloak1",
                CubeListBuilder.create().texOffs(107, 54)
                        .addBox(-4.5F, 2.0F, 1.0F, 9.0F, 12.0F, 1.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.1396F, 0.0F, 0.0F));
        PartDefinition cloak2 = cloak1.addOrReplaceChild("Cloak2",
                CubeListBuilder.create().texOffs(104, 83)
                        .addBox(-4.5F, 0.015F, -0.3884F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(0.0F, 14.0335F, 1.3912F, 0.3069F, 0.0F, 0.0F));
        cloak2.addOrReplaceChild("Cloak3",
                CubeListBuilder.create().texOffs(103, 98)
                        .addBox(-4.5F, -0.0868F, -0.3582F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(0.0F, 4.1718F, 0.0032F, 0.272F, 0.0F, 0.0F));
    }

    private static void addArms(PartDefinition root) {
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(42, 32).mirror()
                        .addBox(-1.0F, 1.0F, -2.6F, 4.0F, 7.0F, 5.0F,
                                new CubeDeformation(0.25F)).mirror(false)
                        .texOffs(68, 9).mirror()
                        .addBox(0.5F, 3.0F, 2.4F, 2.0F, 2.0F, 1.0F,
                                new CubeDeformation(0.25F)).mirror(false)
                        .texOffs(2, 65).mirror()
                        .addBox(0.0F, 5.0F, 2.9F, 3.0F, 3.0F, 1.0F,
                                new CubeDeformation(0.25F)).mirror(false)
                        .texOffs(79, 27)
                        .addBox(-0.7628F, 1.0122F, -2.66F, 5.0F, 4.0F, 5.0F,
                                new CubeDeformation(0.25F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("ShoulderL1",
                CubeListBuilder.create().texOffs(100, 20)
                        .addBox(-1.5F, -2.5F, -2.3375F, 4.0F, 3.0F, 5.0F,
                                new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(1.6779F, 1.2446F, -0.3225F,
                        0.0F, 0.0F, -0.4363F));
        leftArm.addOrReplaceChild("ShoulderL2",
                CubeListBuilder.create().texOffs(79, 19)
                        .addBox(-1.5F, -2.5F, -2.2F, 5.0F, 3.0F, 5.0F,
                                new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(0.7302F, 0.6669F, -0.45F,
                        0.0F, 0.0F, -1.1345F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(42, 32)
                        .addBox(-3.0F, 1.0F, -2.6F, 4.0F, 7.0F, 5.0F,
                                new CubeDeformation(0.25F))
                        .texOffs(1, 65)
                        .addBox(-3.0F, 5.0F, 2.9F, 3.0F, 3.0F, 1.0F,
                                new CubeDeformation(0.25F))
                        .texOffs(67, 9)
                        .addBox(-2.5F, 3.0F, 2.4F, 2.0F, 2.0F, 1.0F,
                                new CubeDeformation(0.25F))
                        .texOffs(79, 27).mirror()
                        .addBox(-4.2372F, 1.0122F, -2.66F, 5.0F, 4.0F, 5.0F,
                                new CubeDeformation(0.25F)).mirror(false),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild("ShoulderR1",
                CubeListBuilder.create().texOffs(100, 20).mirror()
                        .addBox(-2.5F, -2.5F, -2.3375F, 4.0F, 3.0F, 5.0F,
                                new CubeDeformation(0.25F)).mirror(false),
                PartPose.offsetAndRotation(-1.6779F, 1.2446F, -0.3225F,
                        0.0F, 0.0F, 0.4363F));
        rightArm.addOrReplaceChild("ShoulderR2",
                CubeListBuilder.create().texOffs(79, 19).mirror()
                        .addBox(-3.5F, -2.5F, -2.2F, 5.0F, 3.0F, 5.0F,
                                new CubeDeformation(0.25F)).mirror(false),
                PartPose.offsetAndRotation(-0.7302F, 0.6669F, -0.45F,
                        0.0F, 0.0F, 1.1345F));
    }

    private static void addLegs(PartDefinition root) {
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create(),
                PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create(),
                PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
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
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        animateCloth(limbSwing, limbSwingAmount, ageInTicks);
    }

    public void animateCloth(float limbSwing, float limbSwingAmount, float ageInTicks) {
        float idle = Mth.sin(ageInTicks * 0.067F);
        float walk = Mth.sin(limbSwing * 0.6662F) * limbSwingAmount;

        this.cloak.xRot = -0.0436F + idle * 0.025F + Math.abs(walk) * 0.05F;
        this.cloak1.xRot = 0.1396F + Mth.sin(ageInTicks * 0.067F + 0.5F) * 0.035F;
        this.cloak2.xRot = 0.3069F + Mth.sin(ageInTicks * 0.067F + 1.0F) * 0.04F + walk * 0.04F;
        this.cloak3.xRot = 0.272F + Mth.sin(ageInTicks * 0.067F + 1.5F) * 0.035F + walk * 0.05F;

        this.clothBackR1.xRot = 0.1047F + idle * 0.03F + walk * 0.07F;
        this.clothBackL1.xRot = 0.1047F + idle * 0.03F - walk * 0.07F;
        this.clothBackR2.xRot = 0.2269F + Mth.sin(ageInTicks * 0.067F + 0.6F) * 0.035F + walk * 0.08F;
        this.clothBackR3.xRot = 0.2269F + Mth.sin(ageInTicks * 0.067F + 1.0F) * 0.04F + walk * 0.10F;
        this.clothBackL2.xRot = 0.2269F + Mth.sin(ageInTicks * 0.067F + 0.8F) * 0.035F - walk * 0.08F;
        this.clothBackL3.xRot = 0.2269F + Mth.sin(ageInTicks * 0.067F + 1.2F) * 0.04F - walk * 0.10F;

        this.sideclothR1.zRot = -0.1222F - idle * 0.02F - walk * 0.05F;
        this.sideclothR2.zRot = -0.2967F - Mth.sin(ageInTicks * 0.067F + 0.7F) * 0.025F - walk * 0.06F;
        this.sideclothR3.zRot = -0.5236F - Mth.sin(ageInTicks * 0.067F + 1.2F) * 0.03F - walk * 0.07F;
        this.sideclothR4.zRot = 0.1222F + idle * 0.02F + walk * 0.05F;
        this.sideclothR5.zRot = 0.2967F + Mth.sin(ageInTicks * 0.067F + 0.7F) * 0.025F + walk * 0.06F;
        this.sideclothR6.zRot = 0.5236F + Mth.sin(ageInTicks * 0.067F + 1.2F) * 0.03F + walk * 0.07F;
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
        renderHoodToBuffer(
                poseStack,
                buffer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );
        renderBodyToBuffer(
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

    public void renderHoodToBuffer(
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        this.head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void renderBodyToBuffer(
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
