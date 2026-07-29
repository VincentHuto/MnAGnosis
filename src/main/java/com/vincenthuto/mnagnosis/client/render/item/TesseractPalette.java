package com.vincenthuto.mnagnosis.client.render.item;

import java.util.Objects;

public final class TesseractPalette {

    private static volatile Color voidColor =
            new Color(0.025F, 0.015F, 0.080F);
    private static volatile Color cyan =
            new Color(0.12F, 0.92F, 1.00F);
    private static volatile Color azure =
            new Color(0.16F, 0.44F, 1.00F);
    private static volatile Color violet =
            new Color(0.58F, 0.22F, 1.00F);
    private static volatile Color pearl =
            new Color(0.88F, 0.96F, 1.00F);
    private static volatile Color gold =
            new Color(1.00F, 0.68F, 0.18F);
    private static volatile Tuning tuning =
            new Tuning(1.0F, 0.72F, 0.045F);

    private TesseractPalette() {
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
            case CYAN -> cyan = color;
            case AZURE -> azure = color;
            case VIOLET -> violet = color;
            case PEARL -> pearl = color;
            case GOLD -> gold = color;
        }
    }

    public static Color color(Slot slot) {
        Objects.requireNonNull(slot, "slot");
        return switch (slot) {
            case VOID -> voidColor;
            case CYAN -> cyan;
            case AZURE -> azure;
            case VIOLET -> violet;
            case PEARL -> pearl;
            case GOLD -> gold;
        };
    }

    public static void setTuning(
            float brightness,
            float glowStrength,
            float tubeRadius
    ) {
        tuning = new Tuning(brightness, glowStrength, tubeRadius);
    }

    public static Tuning tuning() {
        return tuning;
    }

    public enum Slot {
        VOID,
        CYAN,
        AZURE,
        VIOLET,
        PEARL,
        GOLD
    }

    public record Color(float red, float green, float blue) {

        public Color {
            validateUnit(red, "red");
            validateUnit(green, "green");
            validateUnit(blue, "blue");
        }
    }

    public record Tuning(
            float brightness,
            float glowStrength,
            float tubeRadius
    ) {

        public Tuning {
            if (!Float.isFinite(brightness)
                    || brightness < 0.0F
                    || brightness > 2.0F) {
                throw new IllegalArgumentException(
                        "brightness must be finite and between 0 and 2"
                );
            }
            if (!Float.isFinite(glowStrength)
                    || glowStrength < 0.0F
                    || glowStrength > 2.0F) {
                throw new IllegalArgumentException(
                        "glowStrength must be finite and between 0 and 2"
                );
            }
            if (!Float.isFinite(tubeRadius)
                    || tubeRadius < 0.015F
                    || tubeRadius > 0.10F) {
                throw new IllegalArgumentException(
                        "tubeRadius must be finite and between 0.015 and 0.10"
                );
            }
        }
    }

    private static void validateUnit(float value, String name) {
        if (!Float.isFinite(value) || value < 0.0F || value > 1.0F) {
            throw new IllegalArgumentException(
                    name + " must be finite and between 0 and 1"
            );
        }
    }
}
