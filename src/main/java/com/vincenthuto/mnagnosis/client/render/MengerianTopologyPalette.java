package com.vincenthuto.mnagnosis.client.render;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class MengerianTopologyPalette {

    private static final Map<Slot, Color> COLORS = new EnumMap<>(Slot.class);
    private static volatile Tuning tuning =
            new Tuning(1.0F, 0.16F, 0.28F);

    static {
        COLORS.put(Slot.CRIMSON, new Color(0.96F, 0.20F, 0.34F));
        COLORS.put(Slot.GOLD, new Color(1.00F, 0.68F, 0.12F));
        COLORS.put(Slot.VERDANT, new Color(0.20F, 0.92F, 0.52F));
        COLORS.put(Slot.VIOLET, new Color(0.62F, 0.28F, 0.96F));
        COLORS.put(Slot.AZURE, new Color(0.18F, 0.62F, 1.00F));
        COLORS.put(Slot.PEARL, new Color(0.92F, 0.96F, 1.00F));
    }

    private MengerianTopologyPalette() {
    }

    /**
     * Changes one face color immediately; both renderers read it next frame.
     */
    public static synchronized void setColor(
            Slot slot,
            float red,
            float green,
            float blue
    ) {
        COLORS.put(
                Objects.requireNonNull(slot, "slot"),
                new Color(red, green, blue)
        );
    }

    public static synchronized Color color(Slot slot) {
        return COLORS.get(Objects.requireNonNull(slot, "slot"));
    }

    /**
     * Changes non-destructive color grading immediately for both renderers.
     */
    public static void setTuning(
            float brightness,
            float shadeStrength,
            float depthColorMix
    ) {
        tuning = new Tuning(brightness, shadeStrength, depthColorMix);
    }

    public static Tuning tuning() {
        return tuning;
    }

    public enum Slot {
        CRIMSON,
        GOLD,
        VERDANT,
        VIOLET,
        AZURE,
        PEARL
    }

    public record Color(float red, float green, float blue) {

        public Color {
            validate(red, "red");
            validate(green, "green");
            validate(blue, "blue");
        }

        private static void validate(float value, String component) {
            if (!Float.isFinite(value) || value < 0.0F || value > 1.0F) {
                throw new IllegalArgumentException(
                        component + " must be finite and between 0 and 1"
                );
            }
        }
    }

    public record Tuning(
            float brightness,
            float shadeStrength,
            float depthColorMix
    ) {

        public Tuning {
            if (!Float.isFinite(brightness)
                    || brightness < 0.0F
                    || brightness > 2.0F) {
                throw new IllegalArgumentException(
                        "brightness must be finite and between 0 and 2"
                );
            }
            validateUnit(shadeStrength, "shadeStrength");
            validateUnit(depthColorMix, "depthColorMix");
        }

        private static void validateUnit(float value, String name) {
            if (!Float.isFinite(value) || value < 0.0F || value > 1.0F) {
                throw new IllegalArgumentException(
                        name + " must be finite and between 0 and 1"
                );
            }
        }
    }
}
