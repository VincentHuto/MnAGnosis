package com.vincenthuto.mnagnosis.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.mnagnosis.client.ClientConfig;
import com.vincenthuto.mnagnosis.client.render.armor.IneffableArmorClearance;
import com.vincenthuto.mnagnosis.client.render.armor.IneffableArmorModel;
import com.vincenthuto.mnagnosis.client.render.armor.IneffableRobesCurioLookup;
import com.vincenthuto.mnagnosis.client.render.armor.IneffableArmorShaderMode;
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

    private static final float FRACTAL_PHASE = 200.0F;
    private static final float FRACTAL_FLASH = 10.0F;
    private static final int FRACTAL_ITERATIONS = 6;
    private static final float FRACTAL_MODULUS = 2.1F;
    private static final float FRACTAL_COLOR_SPEED = 12.0F;
    private static final float FRACTAL_SCALE_BASE = 2F;
    private static final float FRACTAL_SCALE_AMPLITUDE = 0.50F;
    private static final float FRACTAL_SCALE_SPEED =10F;
    private static final float FRACTAL_ROTATION_SPEED = 5.60F;
    private static final float FRACTAL_ROTATION_OFFSET = 0.0F;
    private static final float FRACTAL_ORBIT_X = 0.06545465634F;
    private static final float FRACTAL_ORBIT_Y = -0.05346356485F;
    private static final float FRACTAL_ORBIT_X_SPEED = 25.20F;
    private static final float FRACTAL_ORBIT_Y_SPEED = 10.68F;
    private static final float FRACTAL_DRIFT_SPEED = 1.20F;
    private static final float FRACTAL_RADIUS_SMOOTH_MAX = 0.60F;
    private static final float FRACTAL_LENGTH_OFFSET = 0.05F;
    private static final float FRACTAL_EDGE_WIDTH = 0.02F;
    private static final float FRACTAL_DENSITY_FADE = 0.20F;
    private static final float FRACTAL_PIXEL_SIZE = 1F;
    private static final int FRACTAL_BLUR_SAMPLES = 16;
    private static final float FRACTAL_MOTION_BLUR_SCALE = 0.0F;
    private static final float FRACTAL_AA_BLUR_SCALE = 4.0F;
    private static final float FRACTAL_BRIGHTNESS = 10.0F;


    private static final float DISFIGURATION = 0F;

    private final IneffableArmorModel<T> robes;

    public IneffableArmorLayer(LivingEntityRenderer<T, M> owner) {
        super(owner);
        this.robes = bake(IneffableArmorModel.INEFFABLE_ROBES_LAYER);
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

        IneffableArmorClearance clearance = IneffableArmorClearance.from(
                !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty(),
                !entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),
                !entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty(),
                !entity.getItemBySlot(EquipmentSlot.FEET).isEmpty()
        );
        this.getParentModel().copyPropertiesTo(this.robes);
        this.robes.animateCloth(limbSwing, limbSwingAmount, ageInTicks);
        this.robes.applyClearance(clearance);
        configureShader();
        VertexConsumer shaderBuffer =
                bufferSource.getBuffer(RenderHelper.getDopplegangerLayer());
        this.robes.renderToBuffer(
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

    @SuppressWarnings("unchecked")
    private IneffableArmorModel<T> bake(
            net.minecraft.client.model.geom.ModelLayerLocation layer
    ) {
        return new IneffableArmorModel<>(
                Minecraft.getInstance().getEntityModels().bakeLayer(layer)
        );
    }

    private static void configureShader() {
        ShaderInstance shader = CoreShaders.doppleganger();
        if (shader != null) {
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
            shader.safeGetUniform("FractalPhase").set(FRACTAL_PHASE);
            shader.safeGetUniform("FractalFlash").set(FRACTAL_FLASH);
            shader.safeGetUniform("FractalIterations").set(FRACTAL_ITERATIONS);
            shader.safeGetUniform("FractalModulus").set(FRACTAL_MODULUS);
            shader.safeGetUniform("FractalColorSpeed").set(FRACTAL_COLOR_SPEED);
            shader.safeGetUniform("FractalScaleBase").set(FRACTAL_SCALE_BASE);
            shader.safeGetUniform("FractalScaleAmplitude").set(FRACTAL_SCALE_AMPLITUDE);
            shader.safeGetUniform("FractalScaleSpeed").set(FRACTAL_SCALE_SPEED);
            shader.safeGetUniform("FractalRotationSpeed").set(FRACTAL_ROTATION_SPEED);
            shader.safeGetUniform("FractalRotationOffset").set(FRACTAL_ROTATION_OFFSET);
            shader.safeGetUniform("FractalOrbitX").set(FRACTAL_ORBIT_X);
            shader.safeGetUniform("FractalOrbitY").set(FRACTAL_ORBIT_Y);
            shader.safeGetUniform("FractalOrbitXSpeed").set(FRACTAL_ORBIT_X_SPEED);
            shader.safeGetUniform("FractalOrbitYSpeed").set(FRACTAL_ORBIT_Y_SPEED);
            shader.safeGetUniform("FractalDriftSpeed").set(FRACTAL_DRIFT_SPEED);
            shader.safeGetUniform("FractalRadiusSmoothMax").set(FRACTAL_RADIUS_SMOOTH_MAX);
            shader.safeGetUniform("FractalLengthOffset").set(FRACTAL_LENGTH_OFFSET);
            shader.safeGetUniform("FractalEdgeWidth").set(FRACTAL_EDGE_WIDTH);
            shader.safeGetUniform("FractalDensityFade").set(FRACTAL_DENSITY_FADE);
            shader.safeGetUniform("FractalPixelSize").set(FRACTAL_PIXEL_SIZE);
            shader.safeGetUniform("FractalBlurSamples").set(FRACTAL_BLUR_SAMPLES);
            shader.safeGetUniform("FractalMotionBlurScale").set(FRACTAL_MOTION_BLUR_SCALE);
            shader.safeGetUniform("FractalAaBlurScale").set(FRACTAL_AA_BLUR_SCALE);
            shader.safeGetUniform("FractalBrightness").set(FRACTAL_BRIGHTNESS);
            shader.safeGetUniform("BotaniaDisfiguration").set(DISFIGURATION);
        }
    }
}
