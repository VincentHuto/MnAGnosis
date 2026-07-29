package com.vincenthuto.mnagnosis.client.render.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TesseractShaderContractTest {

    private static final Path MAIN = Path.of("src/main");
    private static final Path JAVA = MAIN.resolve(
            "java/com/vincenthuto/mnagnosis"
    );
    private static final Path SHADERS = MAIN.resolve(
            "resources/assets/mnagnosis/shaders/core"
    );

    @Test
    void blockAndItemShareTheLitRayMarchedFourDimensionalRenderer()
            throws Exception {
        JsonObject program = JsonParser.parseString(
                Files.readString(SHADERS.resolve("tesseract.json"))
        ).getAsJsonObject();
        String fragment = Files.readString(
                SHADERS.resolve("tesseract.fsh")
        );
        String core = java("client/render/item/TesseractRenderCore.java");
        String item = java("client/render/item/TesseractItemRenderer.java");
        String block = java(
                "client/render/block/TesseractBlockEntityRenderer.java"
        );
        String coreShaders = java("client/shader/core/CoreShaders.java");
        String renderHelper = java("client/shader/core/RenderHelper.java");

        assertEquals(
                "mnagnosis:tesseract",
                program.get("vertex").getAsString()
        );
        assertEquals(
                "mnagnosis:tesseract",
                program.get("fragment").getAsString()
        );

        Set<String> uniforms = new HashSet<>();
        program.getAsJsonArray("uniforms").forEach(element -> {
            JsonObject uniform = element.getAsJsonObject();
            uniforms.add(uniform.get("name").getAsString());
            assertEquals(
                    uniform.get("count").getAsInt(),
                    uniform.getAsJsonArray("values").size(),
                    uniform.get("name").getAsString()
                            + " must provide its declared value count"
            );
        });
        assertTrue(uniforms.containsAll(Set.of(
                "InversePose",
                "ModelPoseMat",
                "CameraOrigin",
                "RayDirection",
                "Perspective",
                "TesseractTime",
                "TesseractAngleXw",
                "TesseractAngleYz",
                "TesseractPulse",
                "PaletteVoid",
                "PaletteCyan",
                "PaletteAzure",
                "PaletteViolet",
                "PalettePearl",
                "PaletteGold",
                "PaletteBrightness",
                "PaletteGlowStrength",
                "TesseractTubeRadius"
        )));

        assertTrue(fragment.contains("projectTesseractVertex"));
        assertTrue(fragment.contains("segmentDistance"));
        assertTrue(fragment.contains("tesseractDistance"));
        assertTrue(fragment.contains("estimateNormal"));
        assertTrue(fragment.contains("gl_FragDepth"));
        assertTrue(core.contains("CoreShaders.tesseract()"));
        assertTrue(core.contains("RenderHelper.getTesseractLayer()"));
        assertTrue(core.contains("renderShader"));
        assertTrue(core.contains("renderProxyCube"));
        assertTrue(item.contains("TesseractRenderCore.renderShader("));
        assertTrue(block.contains("TesseractRenderCore.renderShader("));
        assertTrue(!item.contains("renderEdges("));
        assertTrue(!block.contains("RenderType.lines()"));
        assertTrue(coreShaders.contains("rloc(\"tesseract\")"));
        assertTrue(renderHelper.contains("getTesseractLayer"));
    }

    @Test
    void paletteAndMaterialTuningCanBeChangedLiveWithValidation() {
        Class<?> palette = classNamed(
                "com.vincenthuto.mnagnosis.client.render.item."
                        + "TesseractPalette"
        );
        Class<?> slot = nestedClass(palette, "Slot");
        Object cyan = enumConstant(slot, "CYAN");
        Object originalColor = invokeStatic(palette, "color", slot, cyan);
        Object originalTuning = invokeStatic(
                palette,
                "tuning",
                new Class<?>[0]
        );

        try {
            invokeStatic(
                    palette,
                    "setColor",
                    new Class<?>[]{
                            slot, float.class, float.class, float.class
                    },
                    cyan, 0.12F, 0.62F, 0.94F
            );
            Object changedColor = invokeStatic(
                    palette,
                    "color",
                    slot,
                    cyan
            );
            assertEquals(0.12F, number(changedColor, "red"));
            assertEquals(0.62F, number(changedColor, "green"));
            assertEquals(0.94F, number(changedColor, "blue"));

            invokeStatic(
                    palette,
                    "setTuning",
                    new Class<?>[]{float.class, float.class, float.class},
                    1.2F, 0.8F, 0.052F
            );
            Object tuning = invokeStatic(
                    palette,
                    "tuning",
                    new Class<?>[0]
            );
            assertEquals(1.2F, number(tuning, "brightness"));
            assertEquals(0.8F, number(tuning, "glowStrength"));
            assertEquals(0.052F, number(tuning, "tubeRadius"));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> invokeStatic(
                            palette,
                            "setTuning",
                            new Class<?>[]{
                                    float.class, float.class, float.class
                            },
                            1.0F, 0.8F, 0.0F
                    )
            );
        } finally {
            invokeStatic(
                    palette,
                    "setColor",
                    new Class<?>[]{
                            slot, float.class, float.class, float.class
                    },
                    cyan,
                    number(originalColor, "red"),
                    number(originalColor, "green"),
                    number(originalColor, "blue")
            );
            invokeStatic(
                    palette,
                    "setTuning",
                    new Class<?>[]{float.class, float.class, float.class},
                    number(originalTuning, "brightness"),
                    number(originalTuning, "glowStrength"),
                    number(originalTuning, "tubeRadius")
            );
        }
    }

    private static String java(String relativePath) throws Exception {
        return Files.readString(JAVA.resolve(relativePath));
    }

    private static Class<?> classNamed(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            return fail("Missing production class " + name, exception);
        }
    }

    private static Class<?> nestedClass(Class<?> owner, String simpleName) {
        for (Class<?> nested : owner.getDeclaredClasses()) {
            if (nested.getSimpleName().equals(simpleName)) {
                return nested;
            }
        }
        return fail("Missing nested class " + simpleName);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumConstant(Class<?> enumType, String name) {
        return Enum.valueOf((Class<? extends Enum>) enumType, name);
    }

    private static Object invokeStatic(
            Class<?> owner,
            String method,
            Class<?> parameter,
            Object argument
    ) {
        return invokeStatic(
                owner,
                method,
                new Class<?>[]{parameter},
                argument
        );
    }

    private static Object invokeStatic(
            Class<?> owner,
            String method,
            Class<?>[] parameters,
            Object... arguments
    ) {
        try {
            return owner.getMethod(method, parameters)
                    .invoke(null, arguments);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            return fail("Invocation failed", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            return fail("Missing method " + owner.getName() + "." + method,
                    exception);
        }
    }

    private static float number(Object target, String accessor) {
        try {
            return ((Number) target.getClass().getMethod(accessor)
                    .invoke(target)).floatValue();
        } catch (ReflectiveOperationException exception) {
            return fail("Missing accessor " + accessor, exception);
        }
    }
}
