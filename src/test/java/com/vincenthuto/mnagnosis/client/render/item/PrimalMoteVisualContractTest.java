package com.vincenthuto.mnagnosis.client.render.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimalMoteVisualContractTest {

    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/mnagnosis"
    );
    private static final Path HUD_RENDERER = Path.of(
            "src/main/java/com/vincenthuto/mnagnosis/client/authorship/"
                    + "IneffableHudRenderer.java"
    );
    private static final Path MOTE_RENDERER = Path.of(
            "src/main/java/com/vincenthuto/mnagnosis/client/render/item/"
                    + "PrimalMoteItemRenderer.java"
    );

    @Test
    void hudBadgeUsesTheCustomEntityRendererPipeline() throws IOException {
        JsonObject model = json("models/item/ineffable_hud_badge.json");
        String hudRenderer = Files.readString(HUD_RENDERER);

        assertEquals("builtin/entity", model.get("parent").getAsString());
        assertTrue(hudRenderer.contains("graphics.renderItem("));
        assertTrue(hudRenderer.contains("ItemRegistry.INEFFABLE_HUD_BADGE"));
    }

    @Test
    void primalMoteRayMarchesTheImplicitSurfaceInAThreeDimensionalVolume()
            throws IOException {
        String vertex = read("shaders/core/mandelbulb.vsh");
        String fragment = read("shaders/core/mandelbulb.fsh");
        JsonObject program = json("shaders/core/mandelbulb.json");
        String renderer = Files.readString(MOTE_RENDERER);

        assertTrue(vertex.contains("InversePose"));
        assertTrue(vertex.contains("localPosition"));
        assertTrue(fragment.contains("mandelbulbDistance"));
        assertTrue(fragment.contains("sceneDistance"));
        assertTrue(fragment.contains("CameraOrigin"));
        assertTrue(fragment.contains("RayDirection"));
        assertTrue(fragment.contains("Perspective"));
        assertTrue(fragment.contains("gl_FragDepth"));
        assertTrue(fragment.contains("mandelbulbOrbitValue"));
        assertTrue(fragment.contains("pastelStepPalette"));
        assertTrue(fragment.contains("PaletteStopPeach"));
        assertTrue(fragment.contains("PaletteStopButter"));
        assertTrue(fragment.contains("PaletteStopMint"));
        assertTrue(fragment.contains("PaletteStopSky"));
        assertTrue(fragment.contains("PaletteStopLavender"));
        assertTrue(fragment.contains("smoothstep"));
        assertFalse(fragment.contains("samplePoint.y * 0.15"));
        assertTrue(fragment.contains("discard"));
        assertTrue(renderer.contains("renderProxyCube"));
        assertTrue(renderer.contains("configureShader"));
        assertFalse(renderer.contains("MandelbulbMesh"));
        assertTrue(program.getAsJsonArray("uniforms").size() >= 10);
    }

    @Test
    void pastelColorsCanBeChangedAtRuntimeFromJava() throws IOException {
        MandelbulbPalette.Color original =
                MandelbulbPalette.color(MandelbulbPalette.Slot.SKY);
        try {
            MandelbulbPalette.setColor(
                    MandelbulbPalette.Slot.SKY,
                    0.25F,
                    0.50F,
                    0.75F
            );

            assertEquals(
                    new MandelbulbPalette.Color(0.25F, 0.50F, 0.75F),
                    MandelbulbPalette.color(MandelbulbPalette.Slot.SKY)
            );
            assertThrows(
                    IllegalArgumentException.class,
                    () -> MandelbulbPalette.setColor(
                            MandelbulbPalette.Slot.SKY,
                            -0.1F,
                            0.5F,
                            0.5F
                    )
            );
            String renderer = Files.readString(MOTE_RENDERER);
            assertTrue(renderer.contains("MandelbulbPalette.color("));
            assertTrue(renderer.contains("PaletteSky"));
        } finally {
            MandelbulbPalette.setColor(
                    MandelbulbPalette.Slot.SKY,
                    original.red(),
                    original.green(),
                    original.blue()
            );
        }
    }

    private static JsonObject json(String relativePath) throws IOException {
        return JsonParser.parseString(read(relativePath)).getAsJsonObject();
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ASSETS.resolve(relativePath));
    }
}
