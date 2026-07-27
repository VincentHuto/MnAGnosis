package com.vincenthuto.mnagnosis.common.particle;

public final class IneffableParticleScale {

    private static final float MINIMUM_HALF_SIZE = 0.019F;
    private static final float HALF_SIZE_VARIANCE = 0.011F;

    private IneffableParticleScale() {
    }

    public static float baseHalfSize(float randomUnit) {
        return baseHalfSize(randomUnit, 1.0F);
    }

    public static float baseHalfSize(float randomUnit, float scale) {
        float normalized = Math.max(0.0F, Math.min(1.0F, randomUnit));
        float normalizedScale =
                Float.isFinite(scale) && scale > 0.0F ? scale : 1.0F;
        return (MINIMUM_HALF_SIZE + normalized * HALF_SIZE_VARIANCE)
                * normalizedScale;
    }
}
