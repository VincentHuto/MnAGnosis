package com.vincenthuto.mnagnosis.client.render.armor;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableFractalShaderContractTest {

    private static final Path SHADER_ROOT = Path.of(
            "src/main/resources/assets/mnagnosis/shaders/core"
    );
    private static final Path ARMOR_LAYER = Path.of(
            "src/main/java/com/vincenthuto/mnagnosis/client/event/"
                    + "IneffableArmorLayer.java"
    );

    @Test
    void fractalModeUsesCameraIndependentModelSpacePlanes()
            throws IOException {
        String vertex = read("doppleganger.vsh");
        String fragment = read("doppleganger.fsh");
        String json = read("doppleganger.json");
        String java = Files.readString(ARMOR_LAYER);
        String compactFragment = fragment.replaceAll("\\s+", "");
        String compactVertex = vertex.replaceAll("\\s+", "");

        assertTrue(vertex.contains("out vec3 robeModelPosition;"));
        assertTrue(vertex.contains("out vec3 robeModelNormal;"));
        assertTrue(vertex.contains("uniform mat4 FractalInversePose;"));
        assertTrue(vertex.contains("uniform mat4 FractalInverseHeadPose;"));
        assertTrue(compactVertex.contains(
                "robeModelPosition="
                        + "(FractalInversePose*vec4(Position,1.0)).xyz;"
        ));
        assertTrue(compactVertex.contains(
                "robeModelNormal=normalize("
                        + "mat3(FractalInversePose)*Normal);"
        ));
        assertTrue(compactVertex.contains(
                "FractalInverseHeadPose*vec4(robeModelPosition,1.0)"
        ));
        assertTrue(json.contains("\"FractalInversePose\""));
        assertTrue(json.contains("\"FractalInverseHeadPose\""));
        assertTrue(java.contains("configureShader(poseStack);"));
        assertTrue(java.contains(
                "new Matrix4f(poseStack.last().pose()).invert()"
        ));
        assertTrue(java.contains(
                "this.originalRobes.head.translateAndRotate(headPose)"
        ));
        assertTrue(java.contains(
                "safeGetUniform(\"FractalInversePose\")"
        ));
        assertTrue(java.contains(
                "safeGetUniform(\"FractalInverseHeadPose\")"
        ));
        assertTrue(fragment.contains("in vec3 robeModelPosition;"));
        assertTrue(fragment.contains("in vec3 robeModelNormal;"));
        assertTrue(fragment.contains(
                "planarRobePosition(modelPosition, modelNormal)"
        ));
        assertTrue(compactFragment.contains(
                "fractalNurseryPattern("
                        + "robeModelPosition,robeModelNormal)"
        ));
        assertFalse(fragment.contains("atan("));
    }

    @Test
    void nurseryRevealsIterationsWithoutWideDerivativePatches()
            throws IOException {
        String fragment = read("doppleganger.fsh")
                .replaceAll("\\s+", " ");

        assertTrue(fragment.contains("vec2 complexSquare(vec2 value)"));
        assertTrue(fragment.contains(
                "z = complexSquare(z) + constantValue;"
        ));
        assertTrue(fragment.contains("float mandelbrotDistance("));
        assertTrue(fragment.contains("float lifecycle = fract("));
        assertTrue(fragment.contains("float activeIterations = mix("));
        assertTrue(fragment.contains(
                "int activeIterationLimit = int(floor(activeIterations));"
        ));
        assertTrue(fragment.contains("escapeIteration"));
        assertTrue(fragment.contains(
                "mandelbrotDistance( constantValue,"
                        + " activeIterationLimit, escapeIteration"
        ));
        assertTrue(fragment.contains(
                "min(fwidth(distance), width * 0.75)"
        ));
        assertTrue(fragment.contains("float filaments = max("));
        assertTrue(fragment.contains(
                "secondary * FractalSecondaryBrightness"
        ));
        assertFalse(fragment.contains("FractalBlurSamples"));
    }

    @Test
    void nurseryUniformsStaySynchronizedAcrossShaderJsonAndJava()
            throws IOException {
        String fragment = read("doppleganger.fsh");
        String json = read("doppleganger.json");
        String java = Files.readString(ARMOR_LAYER);
        String[] uniforms = {
                "FractalFieldScale",
                "FractalFlowX",
                "FractalFlowY",
                "FractalPrimaryCellSize",
                "FractalSecondaryCellSize",
                "FractalIterations",
                "FractalContourWidth",
                "FractalBrightness",
                "FractalSecondaryBrightness",
                "FractalGrowthMin",
                "FractalGrowthMax",
                "FractalLifecycleSpeed",
                "FractalRotationRange"
        };

        for (String uniform : uniforms) {
            assertTrue(
                    fragment.contains(uniform),
                    uniform + " missing in GLSL"
            );
            assertTrue(
                    json.contains("\"" + uniform + "\""),
                    uniform + " missing in JSON"
            );
            assertTrue(
                    java.contains("\"" + uniform + "\""),
                    uniform + " missing in Java"
            );
        }

        assertFalse(json.contains("\"FractalBlurSamples\""));
        assertFalse(java.contains("\"FractalBlurSamples\""));
        assertTrue(fragment.contains(
                "(seed - 0.5) * FractalRotationRange"
        ));
    }

    private static String read(String fileName) throws IOException {
        return Files.readString(SHADER_ROOT.resolve(fileName));
    }
}
