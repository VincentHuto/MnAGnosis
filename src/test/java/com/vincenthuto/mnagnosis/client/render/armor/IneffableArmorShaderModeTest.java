package com.vincenthuto.mnagnosis.client.render.armor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IneffableArmorShaderModeTest {

    @Test
    void zeroSelectsTriangles() {
        assertEquals(
                IneffableArmorShaderMode.TRIANGLES,
                IneffableArmorShaderMode.fromConfigValue(0)
        );
        assertEquals(0, IneffableArmorShaderMode.TRIANGLES.uniformValue());
    }

    @Test
    void oneSelectsCircleGrid() {
        assertEquals(
                IneffableArmorShaderMode.CIRCLE_GRID,
                IneffableArmorShaderMode.fromConfigValue(1)
        );
        assertEquals(1, IneffableArmorShaderMode.CIRCLE_GRID.uniformValue());
    }

    @Test
    void twoSelectsFbm() {
        assertEquals(
                IneffableArmorShaderMode.FBM,
                IneffableArmorShaderMode.fromConfigValue(2)
        );
        assertEquals(2, IneffableArmorShaderMode.FBM.uniformValue());
    }

    @Test
    void threeSelectsFractalFlash() {
        assertEquals(
                IneffableArmorShaderMode.FRACTAL_FLASH,
                IneffableArmorShaderMode.fromConfigValue(3)
        );
        assertEquals(3, IneffableArmorShaderMode.FRACTAL_FLASH.uniformValue());
    }
}
