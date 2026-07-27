package com.vincenthuto.mnagnosis.common.spell.gravity;

/**
 * Shared, client-independent bounds for the Gravity Convergence lens.
 */
public final class GravityLensMath {

    public static final float HALO_RADIUS = 4.0F;
    public static final float MIN_SCREEN_RADIUS = 2.0F;
    public static final float MAX_SCREEN_RADIUS = 640.0F;
    private static final float MAX_DISTORTION = 0.035F;

    private GravityLensMath() {
    }

    public static float distortion(float normalizedDistance, boolean repelling) {
        if (!Float.isFinite(normalizedDistance) || normalizedDistance >= HALO_RADIUS) {
            return 0.0F;
        }
        float distance = Math.max(1.0F, normalizedDistance);
        float progress = (HALO_RADIUS - distance) / (HALO_RADIUS - 1.0F);
        float smooth = progress * progress * (3.0F - 2.0F * progress);
        float distortion = MAX_DISTORTION * smooth;
        return repelling ? -distortion : distortion;
    }

    public static float clampScreenRadius(float screenRadius) {
        if (!Float.isFinite(screenRadius)) {
            return MIN_SCREEN_RADIUS;
        }
        return Math.max(MIN_SCREEN_RADIUS, Math.min(screenRadius, MAX_SCREEN_RADIUS));
    }
}
