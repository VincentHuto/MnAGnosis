package com.vincenthuto.mnagnosis.client.render.item;

public final class KochianStarAnimation {

    private static final float CYCLE_SECONDS = 20.0F;

    private KochianStarAnimation() {
    }

    public static Frame sample(float seconds) {
        float phase = Math.floorMod(
                (long) Math.floor(seconds * 1_000.0F),
                (long) (CYCLE_SECONDS * 1_000.0F)
        ) / (CYCLE_SECONDS * 1_000.0F);
        float wave = 0.5F - 0.5F * (float) Math.cos(
                phase * Math.PI * 2.0
        );
        float smooth = wave * wave * (3.0F - 2.0F * wave);
        return new Frame(
                78.0F + smooth * 10.0F,
                3.0F + smooth * 5.0F
        );
    }

    public record Frame(float angleDegrees, float recursion) {
    }
}
