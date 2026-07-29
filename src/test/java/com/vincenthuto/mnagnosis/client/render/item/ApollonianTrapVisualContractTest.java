package com.vincenthuto.mnagnosis.client.render.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApollonianTrapVisualContractTest {

    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/mnagnosis"
    );

    @Test
    void paletteChangesAreVisibleImmediatelyAndInvalidColorsAreRejected() {
        ApollonianTrapPalette.Color original =
                ApollonianTrapPalette.color(
                        ApollonianTrapPalette.Slot.ORBIT_CYAN
                );
        try {
            ApollonianTrapPalette.setColor(
                    ApollonianTrapPalette.Slot.ORBIT_CYAN,
                    0.20F,
                    0.45F,
                    0.70F
            );

            assertEquals(
                    new ApollonianTrapPalette.Color(0.20F, 0.45F, 0.70F),
                    ApollonianTrapPalette.color(
                            ApollonianTrapPalette.Slot.ORBIT_CYAN
                    )
            );
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ApollonianTrapPalette.setColor(
                            ApollonianTrapPalette.Slot.ORBIT_CYAN,
                            1.01F,
                            0.45F,
                            0.70F
                    )
            );
        } finally {
            ApollonianTrapPalette.setColor(
                    ApollonianTrapPalette.Slot.ORBIT_CYAN,
                    original.red(),
                    original.green(),
                    original.blue()
            );
        }
    }

    @Test
    void itemUsesAThreeDimensionalRayMarchedApollonianProgram()
            throws IOException {
        JsonObject model = json("models/item/apollonian_trap.json");
        JsonObject program = json("shaders/core/apollonian_trap.json");
        String fragment = read("shaders/core/apollonian_trap.fsh");

        assertEquals("builtin/entity", model.get("parent").getAsString());
        assertEquals(
                "mnagnosis:apollonian_trap",
                program.get("vertex").getAsString()
        );
        assertEquals(
                "mnagnosis:apollonian_trap",
                program.get("fragment").getAsString()
        );

        Set<String> uniforms = uniformNames(program);
        program.getAsJsonArray("uniforms").forEach(element -> {
            JsonObject uniform = element.getAsJsonObject();
            assertEquals(
                    uniform.get("count").getAsInt(),
                    uniform.getAsJsonArray("values").size(),
                    uniform.get("name").getAsString()
                            + " must provide one default per component"
            );
        });
        assertTrue(uniforms.contains("CameraOrigin"));
        assertTrue(uniforms.contains("RayDirection"));
        assertTrue(uniforms.contains("Perspective"));
        assertTrue(uniforms.contains("ApollonianTime"));
        assertTrue(uniforms.contains("PaletteOrbitCyan"));
        assertTrue(uniforms.contains("PaletteSurfaceWhite"));
        assertTrue(uniforms.contains("PaletteTrapRed"));
        assertTrue(uniforms.contains("PaletteKeyLight"));
        assertTrue(uniforms.contains("PaletteBackLight"));
        assertTrue(uniforms.contains("PaletteSpecular"));
        assertFalse(uniforms.contains("PaletteBrightGold"));
        assertFalse(uniforms.contains("PaletteStopEmber"));

        assertTrue(fragment.contains("referenceApollonian"));
        assertTrue(fragment.contains("referenceMap"));
        assertTrue(fragment.contains("centeredCubeRepeat"));
        assertTrue(fragment.contains("outerInversion"));
        assertTrue(fragment.contains("crossPlaneDistance"));
        assertTrue(fragment.contains("referenceMorphRadius"));
        assertTrue(fragment.contains("0.2 * cos(0.123 * ApollonianTime)"));
        assertTrue(fragment.contains("orbitTrap"));
        assertFalse(fragment.contains("largeGapDistance"));
        assertFalse(fragment.contains("morphingSurfacePoint"));
        assertFalse(fragment.contains("morphingAxisScale"));
        assertFalse(fragment.contains("morphingOuterRadius"));
        assertTrue(fragment.contains("gl_FragDepth"));
        assertTrue(fragment.contains("discard"));
    }

    @Test
    void expandedProxyDoesNotClipAtLegacyCubeBoundary()
            throws IOException {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/mnagnosis/client/render/item/"
                        + "ApollonianTrapItemRenderer.java"
        ));
        String fragment = read("shaders/core/apollonian_trap.fsh");

        float proxyBound = floatConstant(
                renderer,
                "PROXY_BOUND",
                "F"
        );
        float maxDistance = floatConstant(
                fragment,
                "MAX_DISTANCE",
                ""
        );
        String compactFragment = fragment.replaceAll("\\s+", "");

        assertTrue(
                proxyBound >= 2.4F,
                "The raster proxy must extend well beyond the old ±1.16 cube"
        );
        assertTrue(
                maxDistance >= proxyBound * 2.0F,
                "A ray must be able to march completely across the proxy"
        );
        assertFalse(
                compactFragment.contains(
                        "greaterThan(abs(samplePoint),vec3(PROXY_BOUND"
                ),
                "The shader must not terminate at an axis-aligned cube"
        );
    }

    private static Set<String> uniformNames(JsonObject program) {
        Set<String> names = new HashSet<>();
        JsonArray uniforms = program.getAsJsonArray("uniforms");
        uniforms.forEach(element ->
                names.add(element.getAsJsonObject().get("name").getAsString())
        );
        return names;
    }

    private static JsonObject json(String relativePath) throws IOException {
        return JsonParser.parseString(read(relativePath)).getAsJsonObject();
    }

    private static float floatConstant(
            String source,
            String name,
            String suffix
    ) {
        Pattern pattern = Pattern.compile(
                "\\b" + Pattern.quote(name)
                        + "\\s*=\\s*([0-9]+(?:\\.[0-9]+)?)"
                        + Pattern.quote(suffix)
        );
        Matcher matcher = pattern.matcher(source);
        assertTrue(matcher.find(), "Missing float constant " + name);
        return Float.parseFloat(matcher.group(1));
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ASSETS.resolve(relativePath));
    }
}
