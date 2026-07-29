package com.vincenthuto.mnagnosis.client.render.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KochianStarVisualContractTest {

    private static final Path ROOT = Path.of("src/main");
    private static final Path ASSETS = ROOT.resolve(
            "resources/assets/mnagnosis"
    );

    @Test
    void itemUsesAThreeDimensionalCustomShaderRenderer() throws IOException {
        JsonObject model = json("models/item/kochian_star.json");
        JsonObject shader = json("shaders/core/kochian_star.json");
        String vertex = read("shaders/core/kochian_star.vsh");
        String fragment = read("shaders/core/kochian_star.fsh");
        String item = java("common/item/KochianStarItem.java");
        String renderer = java(
                "client/render/item/KochianStarItemRenderer.java"
        );
        String coreShaders = java("client/shader/core/CoreShaders.java");
        String renderHelper = java("client/shader/core/RenderHelper.java");

        assertEquals("builtin/entity", model.get("parent").getAsString());
        assertTrue(item.contains("KochianStarItemRenderer"));
        assertTrue(item.contains("getCustomRenderer"));
        assertEquals(
                "mnagnosis:kochian_star",
                shader.get("vertex").getAsString()
        );
        assertEquals(
                "mnagnosis:kochian_star",
                shader.get("fragment").getAsString()
        );
        shader.getAsJsonArray("uniforms").forEach(uniformElement -> {
            JsonObject uniform = uniformElement.getAsJsonObject();
            if ("matrix4x4".equals(uniform.get("type").getAsString())) {
                assertEquals(
                        16,
                        uniform.getAsJsonArray("values").size(),
                        uniform.get("name").getAsString()
                                + " must contain a complete 4x4 matrix"
                );
            }
        });
        assertTrue(vertex.contains("InversePose"));
        assertTrue(vertex.contains("localPosition"));
        assertTrue(fragment.contains("PaletteVoid"));
        assertTrue(fragment.contains("PaletteAmethyst"));
        assertTrue(fragment.contains("PaletteFuchsia"));
        assertTrue(fragment.contains("PalettePearl"));
        assertTrue(fragment.contains("PaletteIce"));
        assertTrue(fragment.contains("PaletteGold"));
        assertTrue(fragment.contains("KochianTime"));
        assertTrue(fragment.contains("KochianAngle"));
        assertTrue(fragment.contains("KochianRecursion"));
        assertTrue(fragment.contains("kochV3Distance"));
        assertTrue(fragment.contains("kochFold"));
        assertTrue(fragment.contains("MAX_MARCH_STEPS"));
        assertTrue(fragment.contains("CameraOrigin"));
        assertTrue(fragment.contains("RayDirection"));
        assertTrue(fragment.contains("Perspective"));
        assertTrue(fragment.contains("gl_FragDepth"));
        assertTrue(fragment.contains("discard"));
        assertTrue(renderer.contains("KochianStarAnimation.sample("));
        assertTrue(renderer.contains("renderProxyCube"));
        assertTrue(renderer.contains("\"KochianAngle\""));
        assertTrue(renderer.contains("\"KochianRecursion\""));
        assertTrue(!renderer.contains("renderTube"));
        assertTrue(!renderer.contains("KochianStarGeometry"));
        assertTrue(renderer.contains("KochianStarPalette.color("));
        assertTrue(coreShaders.contains("rloc(\"kochian_star\")"));
        assertTrue(renderHelper.contains("getKochianStarLayer"));
    }

    @Test
    void itemIsRegisteredAndNamedExactlyKochianStar() throws IOException {
        String registry = java("common/registry/ItemRegistry.java");
        JsonObject language = json("lang/en_us.json");

        assertTrue(registry.contains("KOCHIAN_STAR"));
        assertTrue(registry.contains("\"kochian_star\""));
        assertEquals(
                "Kochian Star",
                language.get("item.mnagnosis.kochian_star").getAsString()
        );
    }

    @Test
    void preservesTheThreePointBairdDeltaSilhouette() throws IOException {
        String fragment = read("shaders/core/kochian_star.fsh");
        String renderer = java(
                "client/render/item/KochianStarItemRenderer.java"
        );

        assertTrue(fragment.contains("vec3 folded = z;"));
        assertFalse(
                fragment.contains("vec3 folded = abs(z);"),
                "Optional XYZ absolute folds collapse the three-point "
                        + "Baird Delta into a two-point rhombus"
        );
        assertTrue(fragment.contains("THREE_ARM_SECTOR"));
        assertTrue(fragment.contains("foldThreeArms"));
        assertTrue(fragment.contains("kochArmDistance"));
        assertTrue(fragment.contains(
                "kochArmDistance(foldThreeArms(point))"
        ));
        assertTrue(renderer.contains("REFERENCE_FACE_TILT_DEGREES"));
        assertTrue(renderer.contains("Axis.ZP.rotationDegrees"));
    }

    private static JsonObject json(String relativePath) throws IOException {
        return JsonParser.parseString(read(relativePath)).getAsJsonObject();
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ASSETS.resolve(relativePath));
    }

    private static String java(String relativePath) throws IOException {
        return Files.readString(
                ROOT.resolve(
                        "java/com/vincenthuto/mnagnosis"
                ).resolve(relativePath)
        );
    }
}
