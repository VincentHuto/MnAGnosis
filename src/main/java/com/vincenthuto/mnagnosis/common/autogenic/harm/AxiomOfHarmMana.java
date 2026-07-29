package com.vincenthuto.mnagnosis.common.autogenic.harm;

public final class AxiomOfHarmMana {
    public static final float MULTIPLIER = 1.35F;

    private AxiomOfHarmMana() {
    }

    public static float adjustedCost(float incomingCost) {
        if (!Float.isFinite(incomingCost) || incomingCost < 0.0F) {
            return Float.MAX_VALUE;
        }
        double adjusted = (double) incomingCost * MULTIPLIER;
        return adjusted >= Float.MAX_VALUE
                ? Float.MAX_VALUE
                : (float) adjusted;
    }
}
