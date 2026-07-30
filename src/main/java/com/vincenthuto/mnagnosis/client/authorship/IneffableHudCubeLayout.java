package com.vincenthuto.mnagnosis.client.authorship;

import java.util.List;

public final class IneffableHudCubeLayout {

    private static final float[] PHASES = {
            0.35F, 1.9F, 3.65F, 5.2F
    };
    private static final float[] X_RATES = {
            0.071F, -0.057F, 0.083F, -0.067F
    };
    private static final float[] Y_RATES = {
            0.093F, 0.078F, -0.061F, -0.088F
    };
    private static final float[] Z_RATES = {
            -0.049F, 0.064F, 0.052F, -0.073F
    };

    private static final List<Anchor> ANCHORS = List.of(
            anchor(171.5F, 13.5F, TextureVariant.WHITE),
            anchor(206.5F, 20.5F, TextureVariant.BLACK),
            anchor(782.5F, 144.5F, TextureVariant.WHITE),
            anchor(821.5F, 134.5F, TextureVariant.BLACK)
    );

    private IneffableHudCubeLayout() {
    }

    public static List<Anchor> anchors() {
        return ANCHORS;
    }

    public static float animationTime(long gameTime, float partialTick) {
        return gameTime + partialTick;
    }

    public static Sample sample(int index, float time) {
        Anchor anchor = ANCHORS.get(index);
        float phase = PHASES[index];
        float driftX = sin(time * 0.041F + phase) * 0.72F;
        float driftY = cos(time * 0.052F + phase * 1.37F) * 0.78F;
        float halfSize = 1.88F
                + sin(time * 0.047F + phase * 0.83F) * 0.32F;
        float alpha = 0.83F
                + sin(time * 0.039F + phase * 1.71F) * 0.16F;

        return new Sample(
                anchor.displayX() + driftX,
                anchor.displayY() + driftY,
                halfSize,
                alpha,
                time * X_RATES[index] + phase * 0.71F,
                time * Y_RATES[index] + phase,
                time * Z_RATES[index] + phase * 1.29F,
                anchor.texture()
        );
    }

    private static Anchor anchor(
            float sourceX,
            float sourceY,
            TextureVariant texture
    ) {
        return new Anchor(
                sourceX,
                sourceY,
                sourceX * IneffableHudConcept.DISPLAY_WIDTH
                        / IneffableHudConcept.SOURCE_WIDTH,
                sourceY * IneffableHudConcept.DISPLAY_HEIGHT
                        / IneffableHudConcept.SOURCE_HEIGHT,
                texture
        );
    }

    private static float sin(float value) {
        return (float) Math.sin(value);
    }

    private static float cos(float value) {
        return (float) Math.cos(value);
    }

    public enum TextureVariant {
        WHITE,
        BLACK
    }

    public record Anchor(
            float sourceX,
            float sourceY,
            float displayX,
            float displayY,
            TextureVariant texture
    ) {
    }

    public record Sample(
            float x,
            float y,
            float halfSize,
            float alpha,
            float rotationX,
            float rotationY,
            float rotationZ,
            TextureVariant texture
    ) {
    }
}
