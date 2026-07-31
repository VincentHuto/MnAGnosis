package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

public record ReassembledParameters(
        int range,
        int width,
        int height,
        int depth,
        int radius,
        int durationTicks,
        boolean precision
) {
    public boolean valid() {
        return range >= 4 && range <= 24
                && width >= 1 && width <= 15
                && height >= 1 && height <= 12
                && depth >= 1 && depth <= 15
                && radius >= 1 && radius <= 6
                && durationTicks >= 40 && durationTicks <= 600;
    }
}
