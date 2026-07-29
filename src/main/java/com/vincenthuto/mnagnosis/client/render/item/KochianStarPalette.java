package com.vincenthuto.mnagnosis.client.render.item;

import java.util.Objects;

/**
 * Live item palette. Calls to {@link #setColor} are uploaded by the renderer
 * on its next frame, matching the Primal Mote palette workflow.
 */
public final class KochianStarPalette {

    private static volatile Color voidColor =
            new Color(0.035F, 0.008F, 0.060F);
    private static volatile Color amethyst =
            new Color(0.34F, 0.08F, 0.48F);
    private static volatile Color fuchsia =
            new Color(0.86F, 0.16F, 0.62F);
    private static volatile Color pearl =
            new Color(0.96F, 0.80F, 0.94F);
    private static volatile Color ice =
            new Color(0.49F, 0.90F, 1.00F);
    private static volatile Color gold =
            new Color(1.00F, 0.72F, 0.24F);

    private KochianStarPalette() {
    }

    public static void setColor(
            Slot slot,
            float red,
            float green,
            float blue
    ) {
        Objects.requireNonNull(slot, "slot");
        Color color = new Color(red, green, blue);
        switch (slot) {
            case VOID -> voidColor = color;
            case AMETHYST -> amethyst = color;
            case FUCHSIA -> fuchsia = color;
            case PEARL -> pearl = color;
            case ICE -> ice = color;
            case GOLD -> gold = color;
        }
    }

    public static Color color(Slot slot) {
        Objects.requireNonNull(slot, "slot");
        return switch (slot) {
            case VOID -> voidColor;
            case AMETHYST -> amethyst;
            case FUCHSIA -> fuchsia;
            case PEARL -> pearl;
            case ICE -> ice;
            case GOLD -> gold;
        };
    }

    public enum Slot {
        VOID,
        AMETHYST,
        FUCHSIA,
        PEARL,
        ICE,
        GOLD
    }

    public record Color(float red, float green, float blue) {

        public Color {
            validate(red, "red");
            validate(green, "green");
            validate(blue, "blue");
        }

        private static void validate(float component, String name) {
            if (!Float.isFinite(component)
                    || component < 0.0F
                    || component > 1.0F) {
                throw new IllegalArgumentException(
                        name + " must be finite and between 0 and 1"
                );
            }
        }
    }
}
