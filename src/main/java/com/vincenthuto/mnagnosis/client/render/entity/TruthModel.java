package com.vincenthuto.mnagnosis.client.render.entity;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.TruthEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/** The animation model keeps finale-only details hidden until the synchronized server state begins. */
public final class TruthModel extends GeoModel<TruthEntity> {
    private static final ResourceLocation MODEL = MnAGnosis.rloc("geo/entity/truth.geo.json");
    private static final ResourceLocation ANIMATIONS = MnAGnosis.rloc("animations/entity/truth.animation.json");
    private static final ResourceLocation WHITE_CONCRETE =
            ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");

    @Override
    public ResourceLocation getModelResource(TruthEntity truth) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TruthEntity truth) {
        return WHITE_CONCRETE;
    }

    @Override
    public ResourceLocation getAnimationResource(TruthEntity truth) {
        return ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(TruthEntity truth, long instanceId, AnimationState<TruthEntity> state) {
        super.setCustomAnimations(truth, instanceId, state);
        setHidden("grin", !truth.shouldShowGrin());
        setHidden("flame_core", !truth.shouldShowFinaleFlames());
        setHidden("flame_left", !truth.shouldShowFinaleFlames());
        setHidden("flame_right", !truth.shouldShowFinaleFlames());
        setHidden("glitch_slice_0", !truth.shouldShowGlitchSlices());
        setHidden("glitch_slice_1", !truth.shouldShowGlitchSlices());
        setHidden("glitch_slice_2", !truth.shouldShowGlitchSlices());
        setHidden("glitch_slice_3", !truth.shouldShowGlitchSlices());
    }

    private void setHidden(String boneName, boolean hidden) {
        CoreGeoBone bone = this.getAnimationProcessor().getBone(boneName);
        if (bone != null) {
            bone.setHidden(hidden);
        }
    }
}
