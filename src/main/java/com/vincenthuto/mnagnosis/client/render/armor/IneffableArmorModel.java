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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class IneffableArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

    /*
     * The Blockbench export placed its humanoid under-shell directly on the
     * player mesh. Give only those base cubes enough clearance to fully cover
     * the skin without altering the authored outer cloth and accessories.
     */
    private static final CubeDeformation BASE_HEAD_DEFORMATION = new CubeDeformation(0.55F);
    private static final CubeDeformation BASE_BODY_DEFORMATION = new CubeDeformation(0.25F);
    private static final CubeDeformation BASE_ARM_DEFORMATION = new CubeDeformation(0.55F);
    private static final CubeDeformation BASE_LEG_DEFORMATION = new CubeDeformation(0.25F);

    public static final ModelLayerLocation INEFFABLE_HOOD_LAYER =
            new ModelLayerLocation(MnAGnosis.rloc("ineffable_hood"), "main");
    public static final ModelLayerLocation INEFFABLE_ROBES_LAYER =
            new ModelLayerLocation(MnAGnosis.rloc("ineffable_robes"), "main");
    public static final ModelLayerLocation INEFFABLE_LEGGINGS_LAYER =
            new ModelLayerLocation(MnAGnosis.rloc("ineffable_leggings"), "main");
    public static final ModelLayerLocation INEFFABLE_BOOTS_LAYER =
            new ModelLayerLocation(MnAGnosis.rloc("ineffable_boots"), "main");

    private final EquipmentSlot renderSlot;
    private final ModelPart upperLeftLeg;
    private final ModelPart lowerLeftLeg;
    private final ModelPart upperRightLeg;
    private final ModelPart lowerRightLeg;
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

    public IneffableArmorModel(ModelPart root, EquipmentSlot renderSlot) {
        super(root);
        this.renderSlot = renderSlot;
        this.upperLeftLeg = this.leftLeg.getChild("upper_leg");
        this.lowerLeftLeg = this.leftLeg.getChild("lower_leg");
        this.upperRightLeg = this.rightLeg.getChild("upper_leg");
        this.lowerRightLeg = this.rightLeg.getChild("lower_leg");

        ModelPart clothBack = this.body.getChild("ClothBack");
        ModelPart clothBack1 = clothBack.getChild("ClothBack1");
        ModelPart clothBack2 = clothBack1.getChild("ClothBack2");
        this.clothBackR1 = clothBack1.getChild("ClothBackR1");
        this.clothBackL1 = clothBack1.getChild("ClothBackL1");
        this.clothBackR2 = clothBack2.getChild("ClothBackR2");
        this.clothBackR3 = clothBack2.getChild("ClothBackR3");
        this.clothBackL2 = clothBack2.getChild("ClothBackL2");
        this.clothBackL3 = clothBack2.getChild("ClothBackL3");

        ModelPart sideclothL = this.body.getChild("SideclothL");
        this.sideclothR1 = sideclothL.getChild("SideclothR1");
        this.sideclothR2 = this.sideclothR1.getChild("SideclothR2");
        this.sideclothR3 = this.sideclothR2.getChild("SideclothR3");
        ModelPart sideclothR = this.body.getChild("SideclothR");
        this.sideclothR4 = sideclothR.getChild("SideclothR4");
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

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(78, 2)
                        .addBox(-4.0F, -8.1F, -3.5F, 8.0F, 8.0F, 8.0F, BASE_HEAD_DEFORMATION),
                PartPose.ZERO);
        PartDefinition hood1 = head.addOrReplaceChild("Hood1",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -9.0F, -4.6F, 1.0F, 9.0F, 9.0F, CubeDeformation.NONE)
                        .texOffs(8, 0).addBox(3.5F, -9.0F, -4.6F, 1.0F, 9.0F, 9.0F, CubeDeformation.NONE)
                        .texOffs(24, 106).addBox(-3.5F, -9.0F, -4.6F, 7.0F, 1.0F, 9.0F, CubeDeformation.NONE),
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

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 73).addBox(-4.0F, -1.0F, -2.0F, 8.0F, 13.0F, 4.0F,
                                BASE_BODY_DEFORMATION)
                        .texOffs(0, 18).addBox(3.3497F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, CubeDeformation.NONE)
                        .texOffs(0, 18).addBox(-4.2998F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, CubeDeformation.NONE)
                        .texOffs(17, 36).addBox(-4.1F, -0.5F, -3.25F, 2.0F, 8.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(37, 68).addBox(2.1F, -0.5F, -3.25F, 2.0F, 8.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(4, 93).addBox(-3.9F, -0.5F, 1.85F, 8.0F, 8.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(38, 85).addBox(-4.1F, -0.5F, 1.4F, 8.0F, 13.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        addRearCloth(body);
        addSideCloth(body);
        addCloak(body);
        addGourds(body);
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
                        .addBox(0.0417F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));
        PartDefinition sideclothR2 = sideclothR1.addOrReplaceChild("SideclothR2",
                CubeListBuilder.create().texOffs(63, 24)
                        .addBox(-0.709F, -0.6426F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, -0.2967F));
        sideclothR2.addOrReplaceChild("SideclothR3",
                CubeListBuilder.create().texOffs(63, 33)
                        .addBox(-0.134F, -0.134F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition sideclothR = body.addOrReplaceChild("SideclothR", CubeListBuilder.create(),
                PartPose.offset(-3.8F, 12.25F, 0.0F));
        PartDefinition sideclothR4 = sideclothR.addOrReplaceChild("SideclothR4",
                CubeListBuilder.create().texOffs(57, 57).mirror()
                        .addBox(-1.0416F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, CubeDeformation.NONE)
                        .mirror(false),
                PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));
        PartDefinition sideclothR5 = sideclothR4.addOrReplaceChild("SideclothR5",
                CubeListBuilder.create().texOffs(63, 24).mirror()
                        .addBox(-0.291F, -0.6426F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE)
                        .mirror(false),
                PartPose.offsetAndRotation(-0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, 0.2967F));
        sideclothR5.addOrReplaceChild("SideclothR6",
                CubeListBuilder.create().texOffs(63, 33).mirror()
                        .addBox(-0.866F, -0.134F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE)
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
                        .addBox(-4.5F, 2.0F, 1.0F, 9.0F, 12.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.1396F, 0.0F, 0.0F));
        PartDefinition cloak2 = cloak1.addOrReplaceChild("Cloak2",
                CubeListBuilder.create().texOffs(104, 83)
                        .addBox(-4.5F, -0.1501F, -0.3628F, 9.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 14.0335F, 1.3912F, 0.3069F, 0.0F, 0.0F));
        cloak2.addOrReplaceChild("Cloak3",
                CubeListBuilder.create().texOffs(103, 98)
                        .addBox(-4.5F, -0.4485F, -0.1911F, 9.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 4.1718F, 0.0032F, 0.4466F, 0.0F, 0.0F));
    }

    private static void addGourds(PartDefinition body) {
        PartDefinition leftGourd = body.addOrReplaceChild("left_gourd",
                CubeListBuilder.create()
                        .texOffs(79, 76).addBox(-1.5F, -2.7222F, -1.5F, 3.0F, 1.0F, 3.0F)
                        .texOffs(67, 71).addBox(-1.5F, -0.7222F, -1.5F, 3.0F, 4.0F, 3.0F)
                        .texOffs(79, 80).addBox(-2.15F, -0.6722F, -1.5F, 1.0F, 4.0F, 3.0F,
                                new CubeDeformation(-0.375F))
                        .texOffs(79, 87).addBox(1.1F, -0.6722F, -1.5F, 1.0F, 4.0F, 3.0F,
                                new CubeDeformation(-0.375F))
                        .texOffs(67, 88).addBox(-1.5F, -0.6722F, 1.1F, 3.0F, 4.0F, 1.0F,
                                new CubeDeformation(-0.375F))
                        .texOffs(87, 80).addBox(-1.5F, -0.6722F, -2.1F, 3.0F, 4.0F, 1.0F,
                                new CubeDeformation(-0.375F))
                        .texOffs(67, 78).addBox(-1.5F, -3.9722F, -1.5F, 3.0F, 2.0F, 3.0F,
                                new CubeDeformation(-0.5F))
                        .texOffs(67, 83).addBox(-1.5F, 2.0278F, -1.5F, 3.0F, 2.0F, 3.0F,
                                new CubeDeformation(-0.5F))
                        .texOffs(79, 71).addBox(-1.5F, -2.2222F, -1.5F, 3.0F, 2.0F, 3.0F,
                                new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-5.25F, 12.5722F, 0.65F, 0.7418F, 0.0F, 0.0F));
        PartDefinition leftRope = leftGourd.addOrReplaceChild("rope", CubeListBuilder.create(),
                PartPose.offset(0.1F, -0.8389F, 0.0333F));
        leftRope.addOrReplaceChild("cord",
                CubeListBuilder.create().texOffs(75, 88)
                        .addBox(-1.4F, -0.5333F, -1.0333F, 0.0F, 1.0F, 2.0F,
                                new CubeDeformation(0.05F)),
                PartPose.ZERO);
        leftRope.addOrReplaceChild("front_tie",
                CubeListBuilder.create().texOffs(87, 85)
                        .addBox(0.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F,
                                new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(-1.3F, -0.0333F, 1.0667F, 0.0F, 0.3927F, 0.0F));
        leftRope.addOrReplaceChild("back_tie",
                CubeListBuilder.create().texOffs(87, 86)
                        .addBox(0.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F,
                                new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(-1.3F, -0.0333F, -1.0333F, -0.0319F, 0.0064F, -0.0646F));

        PartDefinition rightGourd = body.addOrReplaceChild("right_gourd",
                CubeListBuilder.create()
                        .texOffs(78, 95).addBox(-1.5F, -2.7222F, -1.5F, 3.0F, 1.0F, 3.0F)
                        .texOffs(66, 95).addBox(-1.5F, -0.7222F, -1.5F, 3.0F, 3.0F, 3.0F)
                        .texOffs(66, 101).addBox(-1.5F, -3.7222F, -1.5F, 3.0F, 2.0F, 3.0F,
                                new CubeDeformation(-0.5F))
                        .texOffs(66, 106).addBox(-1.5F, -2.2222F, -1.5F, 3.0F, 2.0F, 3.0F,
                                new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(5.75F, 12.5722F, 0.75F, 0.7418F, 0.0F, 0.0F));
        PartDefinition rightRope = rightGourd.addOrReplaceChild("rope", CubeListBuilder.create(),
                PartPose.offset(-0.1F, -0.8389F, 0.0333F));
        rightRope.addOrReplaceChild("cords",
                CubeListBuilder.create()
                        .texOffs(78, 103).addBox(1.4F, -0.5333F, -1.0333F, 0.0F, 1.0F, 2.0F,
                                new CubeDeformation(0.05F))
                        .texOffs(78, 106).addBox(1.4F, -0.5333F, -1.0333F, 0.0F, 1.0F, 2.0F,
                                new CubeDeformation(0.05F)),
                PartPose.ZERO);
        rightRope.addOrReplaceChild("front_tie",
                CubeListBuilder.create()
                        .texOffs(78, 99).addBox(-4.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F,
                                new CubeDeformation(0.05F))
                        .texOffs(78, 101).addBox(-4.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F,
                                new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(1.3F, -0.0333F, 1.0667F, 0.0F, -0.3927F, 0.0F));
        rightRope.addOrReplaceChild("back_tie",
                CubeListBuilder.create()
                        .texOffs(78, 100).addBox(-4.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F,
                                new CubeDeformation(0.05F))
                        .texOffs(78, 102).addBox(-4.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F,
                                new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(1.3F, -0.0333F, -1.0333F, -0.0319F, -0.0064F, 0.0646F));
    }

    private static void addArms(PartDefinition root) {
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(38, 0).addBox(-0.5F, 3.5F, -2.5F, 4.0F, 7.0F, 5.0F,
                                BASE_ARM_DEFORMATION)
                        .texOffs(48, 24).addBox(-0.5F, 7.5F, 2.5F, 4.0F, 3.0F, 2.0F, CubeDeformation.NONE)
                        .texOffs(58, 9).addBox(0.0F, 5.5F, 2.5F, 3.0F, 2.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(102, 36).addBox(-0.9841F, 1.1021F, -2.5125F, 5.0F, 4.0F, 5.0F,
                                new CubeDeformation(0.125F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("ShoulderL1",
                CubeListBuilder.create().texOffs(79, 36)
                        .addBox(-2.4063F, -2.0774F, -2.5F, 5.0F, 3.0F, 5.0F,
                                new CubeDeformation(0.25F)),
                PartPose.rotation(0.0F, 0.0F, -1.1345F));
        leftArm.addOrReplaceChild("ShoulderL2",
                CubeListBuilder.create().texOffs(100, 28)
                        .addBox(-1.9226F, -1.5937F, -2.45F, 4.0F, 3.0F, 5.0F,
                                new CubeDeformation(0.1875F)),
                PartPose.offsetAndRotation(1.4146F, 0.4898F, -0.0625F, 0.0F, 0.0F, -0.4363F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(42, 32).addBox(-3.5F, 3.5F, -2.5F, 4.0F, 7.0F, 5.0F,
                                BASE_ARM_DEFORMATION)
                        .texOffs(0, 64).addBox(-3.5F, 7.5F, 2.5F, 4.0F, 3.0F, 2.0F, CubeDeformation.NONE)
                        .texOffs(67, 9).addBox(-3.0F, 5.5F, 2.5F, 3.0F, 2.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(79, 27).mirror()
                        .addBox(-4.0159F, 1.1021F, -2.5125F, 5.0F, 4.0F, 5.0F,
                                new CubeDeformation(0.125F)).mirror(false),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild("ShoulderR1",
                CubeListBuilder.create().texOffs(100, 20).mirror()
                        .addBox(-2.5F, -2.5F, -2.45F, 4.0F, 3.0F, 5.0F,
                                new CubeDeformation(0.1875F)).mirror(false),
                PartPose.offsetAndRotation(-1.4146F, 1.4898F, -0.0625F, 0.0F, 0.0F, 0.4363F));
        rightArm.addOrReplaceChild("ShoulderR2",
                CubeListBuilder.create().texOffs(79, 19).mirror()
                        .addBox(-3.5F, -2.5F, -2.5F, 5.0F, 3.0F, 5.0F,
                                new CubeDeformation(0.25F)).mirror(false),
                PartPose.offsetAndRotation(-0.45F, 1.0F, 0.0F, 0.0F, 0.0F, 1.1345F));
    }

    private static void addLegs(PartDefinition root) {
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        leftLeg.addOrReplaceChild("upper_leg",
                CubeListBuilder.create().texOffs(25, 32)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, BASE_LEG_DEFORMATION),
                PartPose.ZERO);
        leftLeg.addOrReplaceChild("lower_leg",
                CubeListBuilder.create().texOffs(25, 40)
                        .addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, BASE_LEG_DEFORMATION),
                PartPose.ZERO);

        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        rightLeg.addOrReplaceChild("upper_leg",
                CubeListBuilder.create().texOffs(0, 36)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, BASE_LEG_DEFORMATION),
                PartPose.ZERO);
        rightLeg.addOrReplaceChild("lower_leg",
                CubeListBuilder.create().texOffs(0, 44)
                        .addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, BASE_LEG_DEFORMATION),
                PartPose.ZERO);
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
        this.cloak3.xRot = 0.4466F + Mth.sin(ageInTicks * 0.067F + 1.5F) * 0.035F + walk * 0.05F;

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
        switch (this.renderSlot) {
            case HEAD -> this.head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            case CHEST -> {
                this.body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                this.rightArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                this.leftArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            }
            case LEGS -> {
                renderLegPart(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                        this.rightLeg, this.upperRightLeg);
                renderLegPart(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                        this.leftLeg, this.upperLeftLeg);
            }
            case FEET -> {
                renderLegPart(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                        this.rightLeg, this.lowerRightLeg);
                renderLegPart(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha,
                        this.leftLeg, this.lowerLeftLeg);
            }
            default -> {
            }
        }
    }

    private static void renderLegPart(
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha,
            ModelPart leg,
            ModelPart piece
    ) {
        poseStack.pushPose();
        leg.translateAndRotate(poseStack);
        piece.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.popPose();
    }
}
