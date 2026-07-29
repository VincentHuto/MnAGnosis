package com.vincenthuto.mnagnosis.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.client.ClientConfig;
import com.vincenthuto.mnagnosis.client.render.armor.IneffableArmoredModel;
import com.vincenthuto.mnagnosis.client.render.armor.IneffableArmorModel;
import com.vincenthuto.mnagnosis.client.render.armor.IneffableArmorShaderMode;
import com.vincenthuto.mnagnosis.client.render.armor.IneffableRobePresentation;
import com.vincenthuto.mnagnosis.client.render.armor.IneffableRobesCurioLookup;
import com.vincenthuto.mnagnosis.client.shader.core.CoreShaders;
import com.vincenthuto.mnagnosis.client.shader.core.RenderHelper;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

public final class IneffableArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>>
        extends RenderLayer<T, M> {

    private static final float TRIANGLE_SCALE = 150.0F;
    private static final float TRIANGLE_LINE_WIDTH = 0.055F;
    private static final float TRIANGLE_BRIGHTNESS = 0.82F;
    private static final float TRIANGLE_SPEED = 3600.0F;
    private static final float CIRCLE_GRID_SCALE = 100.0F;
    private static final float CIRCLE_MIN_RADIUS = 0.50F;
    private static final float CIRCLE_MAX_RADIUS = 0.71F;
    private static final float CIRCLE_SPEED = 540.0F;
    private static final float CIRCLE_EDGE_SOFTNESS = 0.50F;

    private static final int FBM_OCTAVES = 2;
    private static final float FBM_SCALE = 30.0F;
    private static final float FBM_SPEED = 2006.0F;
    private static final float FBM_WARP_OFFSET = 2.16F;
    private static final float FBM_INITIAL_AMPLITUDE = .70F;
    private static final float FBM_GAIN = 0.50F;
    private static final float FBM_LACUNARITY = 2.0F;
    private static final float FBM_ROTATION = 0.50F;
    private static final float FBM_SHIFT = 10.0F;
    private static final float FBM_INTENSITY =0.9F;
    private static final float FBM_SMOOTH_MIN = 0.10F;
    private static final float FBM_SMOOTH_MAX = 0.70F;
    private static final float FBM_POWER = 8.0F;
    private static final float FBM_SAMPLE_OFFSET = 125F;
    private static final float FBM_GRADIENT_DIVISOR = 200.0F;
    private static final float FBM_OUTPUT_EXPONENT = 0.1F;
    private static final float FBM_OUTPUT_BIAS = 0.30F;

    private static final float FRACTAL_FIELD_SCALE = 5.5F;
    private static final float FRACTAL_FLOW_X = 18.0F;
    private static final float FRACTAL_FLOW_Y = 72.0F;
    private static final float FRACTAL_PRIMARY_CELL_SIZE = 0.82F;
    private static final float FRACTAL_SECONDARY_CELL_SIZE = 0.48F;
    private static final int FRACTAL_ITERATIONS = 40;
    private static final float FRACTAL_CONTOUR_WIDTH = 0.0022F;
    private static final float FRACTAL_BRIGHTNESS = 1.78F;
    private static final float FRACTAL_SECONDARY_BRIGHTNESS = 0.35F;
    private static final float FRACTAL_GROWTH_MIN = .14F;
    private static final float FRACTAL_GROWTH_MAX = 2.0F;
    private static final float FRACTAL_LIFECYCLE_SPEED = 20.0F;
    private static final float FRACTAL_ROTATION_RANGE = 6.2831855F;


    private static final float DISFIGURATION = 0F;

    private final IneffableArmorModel<T> originalRobes;
    private final IneffableArmoredModel<T> armoredRobes;

    public IneffableArmorLayer(LivingEntityRenderer<T, M> owner) {
        super(owner);
        this.originalRobes = new IneffableArmorModel<>(
                Minecraft.getInstance().getEntityModels().bakeLayer(
                        IneffableArmorModel.INEFFABLE_ROBES_LAYER
                )
        );
        this.armoredRobes = new IneffableArmoredModel<>(
                Minecraft.getInstance().getEntityModels().bakeLayer(
                        IneffableArmoredModel.INEFFABLE_ARMORED_ROBES_LAYER
                )
        );
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!IneffableRobesCurioLookup.isEquipped(
                entity,
                ItemRegistry.INEFFABLE_ROBES.get()
        )) {
            return;
        }

        IneffableRobePresentation presentation = IneffableRobePresentation.from(
                !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty(),
                !entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),
                !entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty(),
                !entity.getItemBySlot(EquipmentSlot.FEET).isEmpty()
        );
        this.getParentModel().copyPropertiesTo(this.originalRobes);
        this.originalRobes.animateCloth(
                limbSwing,
                limbSwingAmount,
                ageInTicks
        );
        this.armoredRobes.copyPoseFrom(this.getParentModel());
        this.armoredRobes.animateCloth(
                limbSwing,
                limbSwingAmount,
                ageInTicks
        );
        configureShader(poseStack);
        VertexConsumer shaderBuffer =
                bufferSource.getBuffer(RenderHelper.getDopplegangerLayer());
        if (presentation.armoredBody()) {
            this.armoredRobes.renderToBuffer(
                    poseStack,
                    shaderBuffer,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );
        } else {
            this.originalRobes.renderBodyToBuffer(
                    poseStack,
                    shaderBuffer,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );
        }
        if (presentation.hoodVisible()) {
            this.originalRobes.renderHoodToBuffer(
                    poseStack,
                    shaderBuffer,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );
        }
    }

    private void configureShader(PoseStack poseStack) {
        ShaderInstance shader = CoreShaders.doppleganger();
        if (shader != null) {
            Matrix4f inversePose =
                    new Matrix4f(poseStack.last().pose()).invert();
            PoseStack headPose = new PoseStack();
            this.originalRobes.head.translateAndRotate(headPose);
            Matrix4f inverseHeadPose =
                    new Matrix4f(headPose.last().pose()).invert();
            shader.safeGetUniform("FractalInversePose").set(inversePose);
            shader.safeGetUniform("FractalInverseHeadPose")
                    .set(inverseHeadPose);
            int mode = IneffableArmorShaderMode.fromConfigValue(
                    ClientConfig.INEFFABLE_ARMOR_SHADER.get()
            ).uniformValue();
            shader.safeGetUniform("ShaderMode").set(mode);
            shader.safeGetUniform("TriangleScale").set(TRIANGLE_SCALE);
            shader.safeGetUniform("TriangleLineWidth").set(TRIANGLE_LINE_WIDTH);
            shader.safeGetUniform("TriangleBrightness").set(TRIANGLE_BRIGHTNESS);
            shader.safeGetUniform("TriangleSpeed").set(TRIANGLE_SPEED);
            shader.safeGetUniform("CircleGridScale").set(CIRCLE_GRID_SCALE);
            shader.safeGetUniform("CircleMinRadius").set(CIRCLE_MIN_RADIUS);
            shader.safeGetUniform("CircleMaxRadius").set(CIRCLE_MAX_RADIUS);
            shader.safeGetUniform("CircleSpeed").set(CIRCLE_SPEED);
            shader.safeGetUniform("CircleEdgeSoftness").set(CIRCLE_EDGE_SOFTNESS);
            shader.safeGetUniform("FbmOctaves").set(FBM_OCTAVES);
            shader.safeGetUniform("FbmScale").set(FBM_SCALE);
            shader.safeGetUniform("FbmSpeed").set(FBM_SPEED);
            shader.safeGetUniform("FbmWarpOffset").set(FBM_WARP_OFFSET);
            shader.safeGetUniform("FbmInitialAmplitude").set(FBM_INITIAL_AMPLITUDE);
            shader.safeGetUniform("FbmGain").set(FBM_GAIN);
            shader.safeGetUniform("FbmLacunarity").set(FBM_LACUNARITY);
            shader.safeGetUniform("FbmRotation").set(FBM_ROTATION);
            shader.safeGetUniform("FbmShift").set(FBM_SHIFT);
            shader.safeGetUniform("FbmIntensity").set(FBM_INTENSITY);
            shader.safeGetUniform("FbmSmoothMin").set(FBM_SMOOTH_MIN);
            shader.safeGetUniform("FbmSmoothMax").set(FBM_SMOOTH_MAX);
            shader.safeGetUniform("FbmPower").set(FBM_POWER);
            shader.safeGetUniform("FbmSampleOffset").set(FBM_SAMPLE_OFFSET);
            shader.safeGetUniform("FbmGradientDivisor").set(FBM_GRADIENT_DIVISOR);
            shader.safeGetUniform("FbmOutputExponent").set(FBM_OUTPUT_EXPONENT);
            shader.safeGetUniform("FbmOutputBias").set(FBM_OUTPUT_BIAS);
            shader.safeGetUniform("FractalFieldScale")
                    .set(FRACTAL_FIELD_SCALE);
            shader.safeGetUniform("FractalFlowX").set(FRACTAL_FLOW_X);
            shader.safeGetUniform("FractalFlowY").set(FRACTAL_FLOW_Y);
            shader.safeGetUniform("FractalPrimaryCellSize")
                    .set(FRACTAL_PRIMARY_CELL_SIZE);
            shader.safeGetUniform("FractalSecondaryCellSize")
                    .set(FRACTAL_SECONDARY_CELL_SIZE);
            shader.safeGetUniform("FractalIterations")
                    .set(FRACTAL_ITERATIONS);
            shader.safeGetUniform("FractalContourWidth")
                    .set(FRACTAL_CONTOUR_WIDTH);
            shader.safeGetUniform("FractalBrightness")
                    .set(FRACTAL_BRIGHTNESS);
            shader.safeGetUniform("FractalSecondaryBrightness")
                    .set(FRACTAL_SECONDARY_BRIGHTNESS);
            shader.safeGetUniform("FractalGrowthMin")
                    .set(FRACTAL_GROWTH_MIN);
            shader.safeGetUniform("FractalGrowthMax")
                    .set(FRACTAL_GROWTH_MAX);
            shader.safeGetUniform("FractalLifecycleSpeed")
                    .set(FRACTAL_LIFECYCLE_SPEED);
            shader.safeGetUniform("FractalRotationRange")
                    .set(FRACTAL_ROTATION_RANGE);
            shader.safeGetUniform("BotaniaDisfiguration").set(DISFIGURATION);
        }
    }
}
