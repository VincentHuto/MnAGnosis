package com.vincenthuto.mnagnosis.common.spell.gravity;

public final class GravityRuptureMath {

    public static final int WAVE_COUNT = 3;
    public static final int WAVE_INTERVAL_TICKS = 6;
    public static final int WAVE_TRAVEL_TICKS = 24;
    public static final int TOTAL_DURATION_TICKS =
            (WAVE_COUNT - 1) * WAVE_INTERVAL_TICKS + WAVE_TRAVEL_TICKS;

    private static final float[] BASE_DAMAGE = {10.0F, 6.0F, 3.5F};
    private static final float[] BASE_KNOCKBACK = {1.6F, 1.1F, 0.7F};

    private GravityRuptureMath() {
    }

    public static float collisionDistance(float firstRadius, float secondRadius) {
        float first = finiteAtLeast(firstRadius, 0.0F);
        float second = finiteAtLeast(secondRadius, 0.0F);
        return Math.max(1.5F, (first + second) * 0.18F);
    }

    public static float maximumRadius(int fieldCount) {
        int safeCount = Math.max(2, fieldCount);
        return Math.min(18.0F, 10.0F + (safeCount - 2) * 2.0F);
    }

    public static float waveRadius(int ruptureAge, int waveIndex, float maximumRadius) {
        if (waveIndex < 0 || waveIndex >= WAVE_COUNT) {
            return -1.0F;
        }
        int elapsed = ruptureAge - waveIndex * WAVE_INTERVAL_TICKS;
        if (elapsed < 0 || elapsed > WAVE_TRAVEL_TICKS) {
            return -1.0F;
        }
        float safeMaximum = finiteAtLeast(maximumRadius, 0.0F);
        return safeMaximum * elapsed / WAVE_TRAVEL_TICKS;
    }

    public static float waveDamage(
            int waveIndex,
            float distance,
            float maximumRadius
    ) {
        if (waveIndex < 0 || waveIndex >= WAVE_COUNT) {
            return 0.0F;
        }
        return BASE_DAMAGE[waveIndex] * distanceFalloff(distance, maximumRadius);
    }

    public static float waveKnockback(
            int waveIndex,
            float distance,
            float maximumRadius
    ) {
        if (waveIndex < 0 || waveIndex >= WAVE_COUNT) {
            return 0.0F;
        }
        return BASE_KNOCKBACK[waveIndex]
                * distanceFalloff(distance, maximumRadius);
    }

    private static float distanceFalloff(float distance, float maximumRadius) {
        float safeMaximum = finiteAtLeast(maximumRadius, 0.0F);
        float safeDistance = finiteAtLeast(distance, 0.0F);
        if (safeMaximum <= 0.0F || safeDistance > safeMaximum) {
            return 0.0F;
        }
        float normalized = safeDistance / safeMaximum;
        return 1.0F - normalized * 0.65F;
    }

    private static float finiteAtLeast(float value, float minimum) {
        if (!Float.isFinite(value)) {
            return minimum;
        }
        return Math.max(minimum, value);
    }
}
