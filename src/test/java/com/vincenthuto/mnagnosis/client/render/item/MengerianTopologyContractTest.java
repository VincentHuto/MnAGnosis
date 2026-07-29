package com.vincenthuto.mnagnosis.client.render.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class MengerianTopologyContractTest {

    private static final Path MAIN = Path.of("src/main");
    private static final Path JAVA = MAIN.resolve(
            "java/com/vincenthuto/mnagnosis"
    );
    private static final Path ASSETS = MAIN.resolve(
            "resources/assets/mnagnosis"
    );

    @Test
    void geometryCachesEveryMengerLevelThroughAdjustableDepthThree() {
        Object geometry = invokeStatic(
                "com.vincenthuto.mnagnosis.client.render."
                        + "MengerianTopologyGeometry",
                "cells"
        );
        assertTrue(geometry instanceof List<?>);
        List<?> cells = (List<?>) geometry;
        assertEquals(400, cells.size());

        for (Object cell : cells) {
            int x = ((Number) invoke(cell, "x")).intValue();
            int y = ((Number) invoke(cell, "y")).intValue();
            int z = ((Number) invoke(cell, "z")).intValue();
            assertTrue(isRetainedAtEveryLevel(x, y, z, 2));
        }

        Object depthThree = invokeStatic(
                classNamed(
                        "com.vincenthuto.mnagnosis.client.render."
                                + "MengerianTopologyGeometry"
                ),
                "cells",
                new Class<?>[]{int.class},
                3
        );
        assertTrue(depthThree instanceof List<?>);
        List<?> depthThreeCells = (List<?>) depthThree;
        assertEquals(8_000, depthThreeCells.size());
        for (Object cell : depthThreeCells) {
            int x = ((Number) invoke(cell, "x")).intValue();
            int y = ((Number) invoke(cell, "y")).intValue();
            int z = ((Number) invoke(cell, "z")).intValue();
            assertTrue(isRetainedAtEveryLevel(x, y, z, 3));
        }
    }

    @Test
    void rendererUsesFrustumCullingWithAPerformanceSafeDefault()
            throws IOException {
        Class<?> animation = animationClass();
        Object settings = invokeStatic(
                animation,
                "settings",
                new Class<?>[0]
        );
        assertEquals(
                2,
                ((Number) invoke(settings, "depth")).intValue(),
                "Depth three is optional because it contains 8,000 cells"
        );

        String blockRenderer = java(
                "client/render/block/MengerianTopologyBlockEntityRenderer.java"
        );
        assertTrue(blockRenderer.contains("return false;"));
    }

    @Test
    void blockAndItemUseTheSharedLitRayMarchedMengerianShader()
            throws IOException {
        JsonObject program = json(
                "shaders/core/mengerian_topology.json"
        );
        String fragment = Files.readString(ASSETS.resolve(
                "shaders/core/mengerian_topology.fsh"
        ));
        String renderCore = java(
                "client/render/MengerianTopologyRenderCore.java"
        );
        String coreShaders = java("client/shader/core/CoreShaders.java");
        String renderHelper = java("client/shader/core/RenderHelper.java");

        assertEquals(
                "mnagnosis:mengerian_topology",
                program.get("vertex").getAsString()
        );
        assertEquals(
                "mnagnosis:mengerian_topology",
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
                "MengerianTime",
                "MengerianDepth",
                "MengerianSeparation",
                "PaletteCrimson",
                "PaletteGold",
                "PaletteVerdant",
                "PaletteViolet",
                "PaletteAzure",
                "PalettePearl",
                "PaletteBrightness",
                "PaletteShadeStrength",
                "PaletteDepthColorMix"
        )));

        assertTrue(fragment.contains("mengerDistance"));
        assertTrue(fragment.contains("estimateNormal"));
        assertTrue(fragment.contains("gl_FragDepth"));
        assertTrue(fragment.contains("discard;"));
        assertTrue(renderCore.contains("CoreShaders.mengerianTopology()"));
        assertTrue(renderCore.contains(
                "RenderHelper.getMengerianTopologyLayer()"
        ));
        assertTrue(renderCore.contains("\"MengerianDepth\""));
        assertTrue(renderCore.contains("renderProxyCube"));
        assertTrue(!renderCore.contains("RenderType.debugQuads()"));
        assertTrue(coreShaders.contains("rloc(\"mengerian_topology\")"));
        assertTrue(renderHelper.contains("getMengerianTopologyLayer"));
    }

    @Test
    void animationContinuouslyPingPongsAcrossEveryConfiguredDivision() {
        Class<?> animation = animationClass();
        Object original = invokeStatic(
                animation,
                "settings",
                new Class<?>[0]
        );
        try {
            invokeStatic(
                    animation,
                    "setDepth",
                    new Class<?>[]{int.class},
                    3
            );
            invokeStatic(
                    animation,
                    "setSecondsPerDivision",
                    new Class<?>[]{float.class},
                    1.0F
            );
            invokeStatic(
                    animation,
                    "setSeparation",
                    new Class<?>[]{float.class},
                    0.12F
            );

            Object solid = animationFrame(0.0F);
            Object formingOne = animationFrame(0.5F);
            Object formingTwo = animationFrame(1.5F);
            Object formingThree = animationFrame(2.5F);
            Object collapsingThree = animationFrame(3.5F);
            Object collapsingTwo = animationFrame(4.5F);
            Object collapsingOne = animationFrame(5.5F);
            Object wrappedSolid = animationFrame(6.0F);

            assertFrame(solid, 0, 1);
            assertFrame(formingOne, 1, 20);
            assertFrame(formingTwo, 2, 400);
            assertFrame(formingThree, 3, 8_000);
            assertFrame(collapsingThree, 3, 8_000);
            assertFrame(collapsingTwo, 2, 400);
            assertFrame(collapsingOne, 1, 20);
            assertFrame(wrappedSolid, 0, 1);
            assertEquals(
                    number(formingOne, "cellSize"),
                    number(collapsingOne, "cellSize"),
                    0.000_01F
            );

            invokeStatic(
                    animation,
                    "setDepth",
                    new Class<?>[]{int.class},
                    2
            );
            assertTrue(
                    ((Number) invoke(animationFrame(2.5F), "depth"))
                            .intValue() <= 2
            );
        } finally {
            restoreAnimationSettings(animation, original);
        }
    }

    @Test
    void depthTimingSeparationAndColorTuningUpdateLiveWithValidation() {
        Class<?> animation = animationClass();
        Class<?> palette = classNamed(
                "com.vincenthuto.mnagnosis.client.render."
                        + "MengerianTopologyPalette"
        );
        Object originalSettings = invokeStatic(
                animation,
                "settings",
                new Class<?>[0]
        );
        Object originalTuning = invokeStatic(
                palette,
                "tuning",
                new Class<?>[0]
        );
        try {
            invokeStatic(
                    animation,
                    "setDepth",
                    new Class<?>[]{int.class},
                    3
            );
            invokeStatic(
                    animation,
                    "setSecondsPerDivision",
                    new Class<?>[]{float.class},
                    0.75F
            );
            invokeStatic(
                    animation,
                    "setSeparation",
                    new Class<?>[]{float.class},
                    0.20F
            );
            Object changedSettings = invokeStatic(
                    animation,
                    "settings",
                    new Class<?>[0]
            );
            assertEquals(3, ((Number) invoke(changedSettings, "depth")).intValue());
            assertEquals(
                    0.75F,
                    number(changedSettings, "secondsPerDivision")
            );
            assertEquals(0.20F, number(changedSettings, "separation"));

            invokeStatic(
                    palette,
                    "setTuning",
                    new Class<?>[]{float.class, float.class, float.class},
                    1.15F, 0.35F, 0.60F
            );
            Object changedTuning = invokeStatic(
                    palette,
                    "tuning",
                    new Class<?>[0]
            );
            assertEquals(1.15F, number(changedTuning, "brightness"));
            assertEquals(0.35F, number(changedTuning, "shadeStrength"));
            assertEquals(0.60F, number(changedTuning, "depthColorMix"));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> invokeStatic(
                            animation,
                            "setDepth",
                            new Class<?>[]{int.class},
                            4
                    )
            );
            assertThrows(
                    IllegalArgumentException.class,
                    () -> invokeStatic(
                            animation,
                            "setSecondsPerDivision",
                            new Class<?>[]{float.class},
                            0.0F
                    )
            );
            assertThrows(
                    IllegalArgumentException.class,
                    () -> invokeStatic(
                            palette,
                            "setTuning",
                            new Class<?>[]{
                                    float.class, float.class, float.class
                            },
                            -0.1F, 0.35F, 0.60F
                    )
            );
        } finally {
            restoreAnimationSettings(animation, originalSettings);
            invokeStatic(
                    palette,
                    "setTuning",
                    new Class<?>[]{float.class, float.class, float.class},
                    number(originalTuning, "brightness"),
                    number(originalTuning, "shadeStrength"),
                    number(originalTuning, "depthColorMix")
            );
        }
    }

    @Test
    void paletteChangesImmediatelyAndRejectsInvalidComponents() {
        Class<?> palette = classNamed(
                "com.vincenthuto.mnagnosis.client.render."
                        + "MengerianTopologyPalette"
        );
        Class<?> slot = nestedClass(palette, "Slot");
        Object azure = enumConstant(slot, "AZURE");
        Object original = invokeStatic(palette, "color", slot, azure);

        try {
            invokeStatic(
                    palette,
                    "setColor",
                    new Class<?>[]{slot, float.class, float.class, float.class},
                    azure, 0.20F, 0.40F, 0.80F
            );
            Object changed = invokeStatic(palette, "color", slot, azure);
            assertEquals(0.20F, ((Number) invoke(changed, "red")).floatValue());
            assertEquals(0.40F, ((Number) invoke(changed, "green")).floatValue());
            assertEquals(0.80F, ((Number) invoke(changed, "blue")).floatValue());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> invokeStatic(
                            palette,
                            "setColor",
                            new Class<?>[]{
                                    slot, float.class, float.class, float.class
                            },
                            azure, -0.01F, 0.40F, 0.80F
                    )
            );
        } finally {
            invokeStatic(
                    palette,
                    "setColor",
                    new Class<?>[]{slot, float.class, float.class, float.class},
                    azure,
                    invoke(original, "red"),
                    invoke(original, "green"),
                    invoke(original, "blue")
            );
        }
    }

    @Test
    void blockAndInventoryItemUseTheSharedThreeDimensionalRenderer()
            throws IOException {
        JsonObject blockstate = json("blockstates/mengerian_topology.json");
        JsonObject blockModel = json(
                "models/block/mengerian_topology.json"
        );
        JsonObject itemModel = json("models/item/mengerian_topology.json");
        JsonObject language = json("lang/en_us.json");
        String block = java("common/block/MengerianTopologyBlock.java");
        String item = java("common/item/MengerianTopologyItem.java");
        String blockRenderer = java(
                "client/render/block/MengerianTopologyBlockEntityRenderer.java"
        );
        String itemRenderer = java(
                "client/render/item/MengerianTopologyItemRenderer.java"
        );
        String renderCore = java(
                "client/render/MengerianTopologyRenderCore.java"
        );
        String blockRegistry = java("common/registry/BlockRegistry.java");
        String itemRegistry = java("common/registry/ItemRegistry.java");
        String blockEntityRegistry = java(
                "common/registry/BlockEntityRegistry.java"
        );
        String mod = java("MnAGnosis.java");

        assertEquals(
                "mnagnosis:block/mengerian_topology",
                blockstate.getAsJsonObject("variants")
                        .getAsJsonObject("")
                        .get("model")
                        .getAsString()
        );
        assertEquals(
                "minecraft:block/block",
                blockModel.get("parent").getAsString()
        );
        assertEquals("builtin/entity", itemModel.get("parent").getAsString());
        assertEquals(
                "Mengerian Topology",
                language.get("block.mnagnosis.mengerian_topology").getAsString()
        );
        assertTrue(block.contains("RenderShape.INVISIBLE"));
        assertTrue(item.contains("getCustomRenderer"));
        assertTrue(item.contains("MengerianTopologyItemRenderer"));
        assertTrue(blockRenderer.contains("MengerianTopologyRenderCore.render"));
        assertTrue(itemRenderer.contains("MengerianTopologyRenderCore.render"));
        assertTrue(renderCore.contains("MengerianTopologyPalette.color("));
        assertTrue(renderCore.contains("MengerianTopologyAnimation.frame("));
        assertTrue(blockRegistry.contains("\"mengerian_topology\""));
        assertTrue(itemRegistry.contains("\"mengerian_topology\""));
        assertTrue(blockEntityRegistry.contains("\"mengerian_topology\""));
        assertTrue(mod.contains("MENGERIAN_TOPOLOGY_BE"));
    }

    private static boolean isRetainedAtEveryLevel(
            int x,
            int y,
            int z,
            int depth
    ) {
        int scale = 1;
        for (int level = 0; level < depth; level++) {
            int middleCoordinates = 0;
            middleCoordinates += (x / scale) % 3 == 1 ? 1 : 0;
            middleCoordinates += (y / scale) % 3 == 1 ? 1 : 0;
            middleCoordinates += (z / scale) % 3 == 1 ? 1 : 0;
            if (middleCoordinates >= 2) {
                return false;
            }
            scale *= 3;
        }
        return true;
    }

    private static void assertFrame(Object frame, int depth, int cells) {
        assertEquals(depth, ((Number) invoke(frame, "depth")).intValue());
        assertEquals(cells, animationCells(frame).size());
    }

    private static Class<?> animationClass() {
        return classNamed(
                "com.vincenthuto.mnagnosis.client.render."
                        + "MengerianTopologyAnimation"
        );
    }

    private static Object animationFrame(float cycle) {
        return invokeStatic(
                animationClass(),
                "frame",
                new Class<?>[]{float.class},
                cycle
        );
    }

    private static List<?> animationCells(Object frame) {
        Object cells = invoke(frame, "cells");
        assertTrue(cells instanceof List<?>);
        return (List<?>) cells;
    }

    private static float animationCenter(Object frame, int coordinate) {
        try {
            Method center = frame.getClass().getMethod(
                    "center",
                    int.class
            );
            return ((Number) center.invoke(frame, coordinate)).floatValue();
        } catch (ReflectiveOperationException exception) {
            return fail("Missing animation center calculation", exception);
        }
    }

    private static float number(Object target, String accessor) {
        return ((Number) invoke(target, accessor)).floatValue();
    }

    private static void restoreAnimationSettings(
            Class<?> animation,
            Object settings
    ) {
        invokeStatic(
                animation,
                "setDepth",
                new Class<?>[]{int.class},
                ((Number) invoke(settings, "depth")).intValue()
        );
        invokeStatic(
                animation,
                "setSecondsPerDivision",
                new Class<?>[]{float.class},
                number(settings, "secondsPerDivision")
        );
        invokeStatic(
                animation,
                "setSeparation",
                new Class<?>[]{float.class},
                number(settings, "separation")
        );
    }

    private static JsonObject json(String relativePath) throws IOException {
        return JsonParser.parseString(
                Files.readString(ASSETS.resolve(relativePath))
        ).getAsJsonObject();
    }

    private static String java(String relativePath) throws IOException {
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

    private static Object invokeStatic(String owner, String method) {
        return invokeStatic(classNamed(owner), method, new Class<?>[0]);
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
            return owner.getMethod(method, parameters).invoke(null, arguments);
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

    private static Object invoke(Object target, String method) {
        try {
            Method accessor = target.getClass().getMethod(method);
            return accessor.invoke(target);
        } catch (ReflectiveOperationException exception) {
            return fail("Missing accessor " + method, exception);
        }
    }
}
