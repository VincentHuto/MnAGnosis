package com.vincenthuto.mnagnosis.client.render.item;

import java.util.Objects;

public final class MandelbulbPalette {

    private static volatile Color blush = new Color(1.00F, 0.62F, 0.77F);
    private static volatile Color peach = new Color(1.00F, 0.74F, 0.61F);
    private static volatile Color butter = new Color(1.00F, 0.91F, 0.62F);
    private static volatile Color mint = new Color(0.62F, 0.94F, 0.77F);
    private static volatile Color sky = new Color(0.57F, 0.84F, 0.98F);
    private static volatile Color lavender = new Color(0.79F, 0.66F, 0.97F);
    private static volatile Stops stops =
            new Stops(0.16F, 0.34F, 0.52F, 0.70F, 0.86F);

    private MandelbulbPalette() {
    }

    /**
     * Changes one color immediately; the renderer uploads it next frame.
     */
    public static void setColor(Slot slot, float red, float green, float blue) {
        Objects.requireNonNull(slot, "slot");
        Color color = new Color(red, green, blue);
        switch (slot) {
            case BLUSH -> blush = color;
            case PEACH -> peach = color;
            case BUTTER -> butter = color;
            case MINT -> mint = color;
            case SKY -> sky = color;
            case LAVENDER -> lavender = color;
        }
    }

    public static Color color(Slot slot) {
        Objects.requireNonNull(slot, "slot");
        return switch (slot) {
            case BLUSH -> blush;
            case PEACH -> peach;
            case BUTTER -> butter;
            case MINT -> mint;
            case SKY -> sky;
            case LAVENDER -> lavender;
        };
    }

    /**
     * Repositions all five blend boundaries immediately.
     */
    public static void setStops(
            float peach,
            float butter,
            float mint,
            float sky,
            float lavender
    ) {
        stops = new Stops(peach, butter, mint, sky, lavender);
    }

    public static Stops stops() {
        return stops;
    }

    public enum Slot {
        BLUSH,
        PEACH,
        BUTTER,
        MINT,
        SKY,
        LAVENDER
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

    public record Stops(
            float peach,
            float butter,
            float mint,
            float sky,
            float lavender
    ) {

        public Stops {
            if (!(0.0F < peach
                    && peach < butter
                    && butter < mint
                    && mint < sky
                    && sky < lavender
                    && lavender < 1.0F)) {
                throw new IllegalArgumentException(
                        "Palette stops must increase strictly between 0 and 1"
                );
            }
        }
    }
}
