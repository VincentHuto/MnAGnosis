package com.vincenthuto.mnagnosis.client.render.armor;

public enum IneffableArmorShaderMode {
    TRIANGLES(0),
    CIRCLE_GRID(1),
    FBM(2),
    FRACTAL_FLASH(3);

    private final int uniformValue;

    IneffableArmorShaderMode(int uniformValue) {
        this.uniformValue = uniformValue;
    }

    public int uniformValue() {
        return this.uniformValue;
    }

    public static IneffableArmorShaderMode fromConfigValue(int value) {
        return switch (value) {
            case 0 -> TRIANGLES;
            case 2 -> FBM;
            case 3 -> FRACTAL_FLASH;
            default -> CIRCLE_GRID;
        };
    }

}
