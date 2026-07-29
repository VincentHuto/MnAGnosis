package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YaldabaothAssetContractTest {

    private static final String ASSET_ROOT = "assets/mnagnosis/";

    @Test
    void geometryShipsStableIdentifiersAndIntegrationBones() throws IOException {
        assertGeometry(
                "yaldabaoth",
                "geometry.mnagnosis.yaldabaoth",
                Set.of(
                        "root", "body", "neck", "head", "jaw", "mane", "crown",
                        "segment_01", "segment_02", "segment_03", "segment_04",
                        "segment_05", "segment_06", "segment_07", "segment_08",
                        "segment_09", "segment_10", "tail", "terminal_sweep"
                )
        );
        assertGeometry(
                "yaldabaoth_sun",
                "geometry.mnagnosis.yaldabaoth_sun",
                Set.of("root", "disc", "outline", "flare")
        );
        assertGeometry(
                "yaldabaoth_moon",
                "geometry.mnagnosis.yaldabaoth_moon",
                Set.of("root", "crescent", "outline", "cut")
        );
    }

    @Test
    void animationFilesExposeEveryJavaAnimationIdentifier() throws IOException {
        assertAnimations(
                "yaldabaoth",
                Set.of(
                        "animation.yaldabaoth.idle",
                        "animation.yaldabaoth.move",
                        "animation.yaldabaoth.combat.roar_sweep"
                )
        );
        assertAnimations(
                "yaldabaoth_sun",
                Set.of(
                        "animation.yaldabaoth_sun.idle",
                        "animation.yaldabaoth_sun.combat.judgment"
                )
        );
        assertAnimations(
                "yaldabaoth_moon",
                Set.of(
                        "animation.yaldabaoth_moon.idle",
                        "animation.yaldabaoth_moon.combat.omission_slash"
                )
        );
    }

    @Test
    void texturesUseApprovedDimensionsAndReadablePalettes() throws IOException {
        BufferedImage boss = image("yaldabaoth", 128, 128);
        assertTrue(containsNear(boss, 0x9A7638, 8), "Missing muted gold");
        assertTrue(containsNear(boss, 0xD7C8A1, 8), "Missing aged bone");
        assertTrue(containsNear(boss, 0xD95C2B, 8), "Missing ember");
        assertTrue(containsNear(boss, 0x171A24, 8), "Missing storm-dark");

        BufferedImage sun = image("yaldabaoth_sun", 64, 64);
        assertTrue(containsNear(sun, 0xF8F8F2, 8), "Sun has no white face");
        assertTrue(containsNear(sun, 0x090A0C, 8), "Sun has no black outline");

        BufferedImage moon = image("yaldabaoth_moon", 64, 64);
        assertTrue(containsNear(moon, 0x08090B, 8), "Moon has no black body");
        assertTrue(containsNear(moon, 0xF5F5EF, 8), "Moon has no white outline");
    }

    @Test
    void localizationNamesAllThreeEncounterEntities() throws IOException {
        JsonObject language = json("lang/en_us.json");
        assertEquals(
                "Yaldabaoth",
                language.get("entity.mnagnosis.yaldabaoth").getAsString()
        );
        assertEquals(
                "The Counterfeit Sun",
                language.get("entity.mnagnosis.yaldabaoth_sun").getAsString()
        );
        assertEquals(
                "The Counterfeit Moon",
                language.get("entity.mnagnosis.yaldabaoth_moon").getAsString()
        );
    }

    @Test
    void sunIdleRotatesInItsVisiblePlaneInsteadOfTurningEdgeOn()
            throws IOException {
        JsonObject root = json(
                "animations/entity/yaldabaoth_sun.animation.json"
        );
        JsonObject rotations = root.getAsJsonObject("animations")
                .getAsJsonObject("animation.yaldabaoth_sun.idle")
                .getAsJsonObject("bones")
                .getAsJsonObject("root")
                .getAsJsonObject("rotation");

        boolean rotatesAroundVisibleAxis = false;
        for (var keyframe : rotations.entrySet()) {
            var vector = keyframe.getValue().getAsJsonObject()
                    .getAsJsonArray("vector");
            assertEquals(
                    0.0D,
                    vector.get(1).getAsDouble(),
                    0.0001D,
                    "Sun idle turned the disc edge-on at " + keyframe.getKey()
            );
            rotatesAroundVisibleAxis |= Math.abs(vector.get(2).getAsDouble()) > 1.0D;
        }
        assertTrue(rotatesAroundVisibleAxis, "Sun idle did not rotate in its plane");
    }

    @Test
    void lionHeadFrontUsesTheCoherentFacialTextureTile() throws IOException {
        JsonObject root = json("geo/entity/yaldabaoth.geo.json");
        var bones = root.getAsJsonArray("minecraft:geometry")
                .get(0).getAsJsonObject()
                .getAsJsonArray("bones");
        JsonObject head = null;
        for (var element : bones) {
            JsonObject bone = element.getAsJsonObject();
            if ("head".equals(bone.get("name").getAsString())) {
                head = bone;
                break;
            }
        }
        assertNotNull(head, "Yaldabaoth head bone was missing");
        JsonObject headUv = head.getAsJsonArray("cubes")
                .get(0).getAsJsonObject()
                .getAsJsonObject("uv");
        for (String face : new String[]{"north", "south"}) {
            var faceUv = headUv.getAsJsonObject(face).getAsJsonArray("uv");
            var faceSize =
                    headUv.getAsJsonObject(face).getAsJsonArray("uv_size");
            assertEquals(0, faceUv.get(0).getAsInt(), face + " facial tile U");
            assertEquals(0, faceUv.get(1).getAsInt(), face + " facial tile V");
            assertEquals(64, faceSize.get(0).getAsInt(), face + " facial width");
            assertEquals(64, faceSize.get(1).getAsInt(), face + " facial height");
        }
    }

    @Test
    void yaldabaothIdleIsTallAndCompactWhileMovementIsLowAndExtended()
            throws IOException {
        JsonObject root = json("animations/entity/yaldabaoth.animation.json");
        JsonObject animations = root.getAsJsonObject("animations");
        assertTrue(animations.has("animation.yaldabaoth.move"));

        JsonObject idle = animations.getAsJsonObject("animation.yaldabaoth.idle");
        JsonObject move = animations.getAsJsonObject("animation.yaldabaoth.move");

        assertTrue(
                component(idle, "neck", "position", "0.0", 1) >= 24.0D,
                "Idle neck must rear above the coil"
        );
        assertTrue(
                component(idle, "terminal_sweep", "position", "0.0", 2)
                        <= -180.0D,
                "Idle tail must return beneath the body to form a compact coil"
        );
        assertTrue(
                Math.abs(component(move, "neck", "position", "0.0", 1))
                        <= 2.0D,
                "Moving neck must return to the low stance"
        );
        assertTrue(
                Math.abs(component(
                        move,
                        "terminal_sweep",
                        "position",
                        "0.0",
                        2
                )) <= 2.0D,
                "Moving tail must extend behind the body"
        );

        double earlyWave =
                component(move, "segment_02", "rotation", "0.0", 1);
        double lateWave =
                component(move, "segment_07", "rotation", "0.0", 1);
        assertTrue(
                earlyWave * lateWave < 0.0D,
                "Movement loop must phase-shift its lateral wave down the body"
        );
        assertNotEquals(
                earlyWave,
                component(move, "segment_02", "rotation", "0.6", 1),
                0.0001D,
                "Movement loop must animate rather than hold a flat pose"
        );
    }

    @Test
    void yaldabaothVisibleBoundsContainRaisedIdlePose() throws IOException {
        JsonObject description = json("geo/entity/yaldabaoth.geo.json")
                .getAsJsonArray("minecraft:geometry")
                .get(0).getAsJsonObject()
                .getAsJsonObject("description");

        assertTrue(description.get("visible_bounds_height").getAsDouble() >= 16.0D);
        assertTrue(
                description.getAsJsonArray("visible_bounds_offset")
                        .get(1).getAsDouble() >= 7.0D
        );
    }

    private static void assertGeometry(
            String assetName,
            String expectedIdentifier,
            Set<String> requiredBones
    ) throws IOException {
        JsonObject root = json("geo/entity/" + assetName + ".geo.json");
        JsonObject geometry = root.getAsJsonArray("minecraft:geometry")
                .get(0).getAsJsonObject();
        assertEquals(
                expectedIdentifier,
                geometry.getAsJsonObject("description").get("identifier").getAsString()
        );

        Set<String> actualBones = new HashSet<>();
        for (var element : geometry.getAsJsonArray("bones")) {
            actualBones.add(element.getAsJsonObject().get("name").getAsString());
        }
        assertTrue(
                actualBones.containsAll(requiredBones),
                "Missing bones " + difference(requiredBones, actualBones)
        );
    }

    private static void assertAnimations(
            String assetName,
            Set<String> requiredAnimations
    ) throws IOException {
        JsonObject root = json(
                "animations/entity/" + assetName + ".animation.json"
        );
        Set<String> actual = root.getAsJsonObject("animations").keySet();
        assertTrue(
                actual.containsAll(requiredAnimations),
                "Missing animations " + difference(requiredAnimations, actual)
        );
    }

    private static JsonObject json(String path) throws IOException {
        try (InputStream stream = resource(ASSET_ROOT + path);
             InputStreamReader reader =
                     new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static BufferedImage image(
            String assetName,
            int expectedWidth,
            int expectedHeight
    ) throws IOException {
        try (InputStream stream = resource(
                ASSET_ROOT + "textures/entity/yaldabaoth/" + assetName + ".png"
        )) {
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, assetName + " was not a readable PNG");
            assertEquals(expectedWidth, image.getWidth(), assetName + " width");
            assertEquals(expectedHeight, image.getHeight(), assetName + " height");
            return image;
        }
    }

    private static InputStream resource(String path) {
        InputStream stream =
                YaldabaothAssetContractTest.class.getClassLoader()
                        .getResourceAsStream(path);
        assertNotNull(stream, "Missing resource " + path);
        return stream;
    }

    private static double component(
            JsonObject animation,
            String bone,
            String transform,
            String time,
            int component
    ) {
        JsonElement keyframe = animation.getAsJsonObject("bones")
                .getAsJsonObject(bone)
                .getAsJsonObject(transform)
                .get(time);
        return (keyframe.isJsonArray()
                ? keyframe.getAsJsonArray()
                : keyframe.getAsJsonObject().getAsJsonArray("vector"))
                .get(component)
                .getAsDouble();
    }

    private static boolean containsNear(
            BufferedImage image,
            int expectedRgb,
            int tolerance
    ) {
        int expectedRed = (expectedRgb >> 16) & 0xFF;
        int expectedGreen = (expectedRgb >> 8) & 0xFF;
        int expectedBlue = expectedRgb & 0xFF;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if (((argb >>> 24) & 0xFF) == 0) {
                    continue;
                }
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;
                if (Math.abs(red - expectedRed) <= tolerance
                        && Math.abs(green - expectedGreen) <= tolerance
                        && Math.abs(blue - expectedBlue) <= tolerance) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Set<String> difference(Set<String> expected, Set<String> actual) {
        Set<String> missing = new HashSet<>(expected);
        missing.removeAll(actual);
        return missing;
    }
}
