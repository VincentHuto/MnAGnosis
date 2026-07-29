package com.vincenthuto.mnagnosis.client.render.item;

import java.util.Objects;

public final class ApollonianTrapPalette {

    private static volatile Color orbitCyan =
            new Color(0.0F, 1.0F, 1.0F);
    private static volatile Color surfaceWhite =
            new Color(1.0F, 1.0F, 1.0F);
    private static volatile Color trapRed =
            new Color(0.4F, 0.0F, 0.0F);
    private static volatile Color keyLight =
            new Color(1.0F, 1.0F, 0.6F);
    private static volatile Color backLight =
            new Color(1.0F, 0.8F, 0.3F);
    private static volatile Color specular =
            new Color(1.0F, 1.0F, 1.0F);

    private ApollonianTrapPalette() {
    }

    /**
     * Changes one color immediately; the renderer uploads it next frame.
     */
    public static void setColor(
            Slot slot,
            float red,
            float green,
            float blue
    ) {
        Objects.requireNonNull(slot, "slot");
        Color color = new Color(red, green, blue);
        switch (slot) {
            case ORBIT_CYAN -> orbitCyan = color;
            case SURFACE_WHITE -> surfaceWhite = color;
            case TRAP_RED -> trapRed = color;
            case KEY_LIGHT -> keyLight = color;
            case BACK_LIGHT -> backLight = color;
            case SPECULAR -> specular = color;
        }
    }

    public static Color color(Slot slot) {
        Objects.requireNonNull(slot, "slot");
        return switch (slot) {
            case ORBIT_CYAN -> orbitCyan;
            case SURFACE_WHITE -> surfaceWhite;
            case TRAP_RED -> trapRed;
            case KEY_LIGHT -> keyLight;
            case BACK_LIGHT -> backLight;
            case SPECULAR -> specular;
        };
    }

    public enum Slot {
        ORBIT_CYAN,
        SURFACE_WHITE,
        TRAP_RED,
        KEY_LIGHT,
        BACK_LIGHT,
        SPECULAR
    }

    public record Color(float red, float green, float blue) {

        public Color {
            validateComponent(red, "red");
            validateComponent(green, "green");
            validateComponent(blue, "blue");
        }

        private static void validateComponent(float value, String name) {
            if (!Float.isFinite(value) || value < 0.0F || value > 1.0F) {
                throw new IllegalArgumentException(
                        name + " must be finite and between 0 and 1"
                );
            }
        }
    }

}
