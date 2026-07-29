package com.vincenthuto.mnagnosis.common.entity.item;

import net.minecraft.util.Mth;

public record FractalItemEntityTraits(
        float bobAmplitude,
        float bobRadiansPerTick,
        float rotationRadiansPerTick,
        float fixedYaw,
        float renderScale,
        float verticalOffset,
        boolean fullBright
) {

    public static final FractalItemEntityTraits STATIC =
            new FractalItemEntityTraits(
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    1.0F,
                    0.0F,
                    false
            );

    public FractalItemEntityTraits {
        requireFinite("bobAmplitude", bobAmplitude);
        requireFinite("bobRadiansPerTick", bobRadiansPerTick);
        requireFinite(
                "rotationRadiansPerTick",
                rotationRadiansPerTick
        );
        requireFinite("fixedYaw", fixedYaw);
        requireFinite("renderScale", renderScale);
        requireFinite("verticalOffset", verticalOffset);
        if (renderScale <= 0.0F) {
            throw new IllegalArgumentException(
                    "renderScale must be greater than zero"
            );
        }
    }

    public float bobOffset(float ageInTicks) {
        return bobAmplitude == 0.0F
                ? 0.0F
                : Mth.sin(ageInTicks * bobRadiansPerTick)
                        * bobAmplitude;
    }

    public float rotation(float ageInTicks) {
        return fixedYaw + ageInTicks * rotationRadiansPerTick;
    }

    private static void requireFinite(String name, float value) {
        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException(
                    name + " must be finite"
            );
        }
    }
}
