# Ineffable Fractal Nursery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the broken Ineffable Robes fractal-flash mode with a charcoal-dominant, model-space field of small Mandelbrot buds that continuously crawls and reseeds.

**Architecture:** Pass the robe's submitted model position from the existing doppleganger vertex shader to its fragment shader, then cylindrically project that shared position into a continuous two-dimensional field. Evaluate two staggered, hashed cell layers with the quadratic Mandelbrot recurrence and distance-estimated contour rendering; configure the compact replacement uniform set from the armor layer.

**Tech Stack:** Java 17, JUnit 5, Minecraft Forge 1.20.1 core shaders, GLSL 1.50, Gradle

## Global Constraints

- Preserve shader modes `0`, `1`, and `2` unchanged.
- Preserve `FRACTAL_FLASH` as shader-mode uniform value `3`.
- Render only fine silver-white boundary filaments; Mandelbrot interiors remain charcoal.
- Keep the field continuous across robe model parts and put the cylindrical seam at the back.
- Use two Mandelbrot evaluations per fragment and no motion-blur sampling loop.
- Preserve existing lighting, overlay, alpha, and fog behavior.
- Do not stage or rewrite unrelated existing worktree changes.

---

## File Map

- `src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableFractalShaderContractTest.java`: reads the actual shader resources and enforces the model-space, Mandelbrot, contour, lifecycle, and synchronized-uniform contracts.
- `src/main/resources/assets/mnagnosis/shaders/core/doppleganger.vsh`: exports the submitted model position while preserving existing vertex processing.
- `src/main/resources/assets/mnagnosis/shaders/core/doppleganger.fsh`: replaces only fractal mode with the crawling two-layer Mandelbrot nursery.
- `src/main/resources/assets/mnagnosis/shaders/core/doppleganger.json`: replaces obsolete fractal uniforms with the nursery controls.
- `src/main/java/com/vincenthuto/mnagnosis/client/event/IneffableArmorLayer.java`: owns the matching default tuning and uploads it to the shader.

### Task 1: Lock the fractal shader contract

**Files:**
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableFractalShaderContractTest.java`
- Read: `src/main/resources/assets/mnagnosis/shaders/core/doppleganger.vsh`
- Read: `src/main/resources/assets/mnagnosis/shaders/core/doppleganger.fsh`
- Read: `src/main/resources/assets/mnagnosis/shaders/core/doppleganger.json`
- Read: `src/main/java/com/vincenthuto/mnagnosis/client/event/IneffableArmorLayer.java`

**Interfaces:**
- Consumes: repository-root-relative shader and Java source paths.
- Produces: regression checks for `robeModelPosition`, `mandelbrotDistance`, `fractalNurseryPattern`, the compact uniform names, and removal of the old blur implementation.

- [ ] **Step 1: Write the failing resource-contract test**

```java
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
    void fractalModeUsesSharedModelSpaceInsteadOfUvIslands()
            throws IOException {
        String vertex = read("doppleganger.vsh");
        String fragment = read("doppleganger.fsh");

        assertTrue(vertex.contains("out vec3 robeModelPosition;"));
        assertTrue(vertex.contains("robeModelPosition = Position;"));
        assertTrue(fragment.contains("in vec3 robeModelPosition;"));
        assertTrue(fragment.contains("cylindricalRobePosition("
                + "robeModelPosition)"));
        assertTrue(fragment.contains(
                "fractalNurseryPattern(robeModelPosition)"
        ));
        assertFalse(fragment.contains(
                "fractalFlashPattern(texCoord0)"
        ));
    }

    @Test
    void nurseryUsesTrueMandelbrotDistanceContoursAndStaggeredBirths()
            throws IOException {
        String fragment = read("doppleganger.fsh");

        assertTrue(fragment.contains("vec2 complexSquare(vec2 value)"));
        assertTrue(fragment.contains(
                "z = complexSquare(z) + constantValue;"
        ));
        assertTrue(fragment.contains("float mandelbrotDistance("));
        assertTrue(fragment.contains("float lifecycle = fract("));
        assertTrue(fragment.contains("fwidth(distance)"));
        assertTrue(fragment.contains(
                "max(primary, secondary * FractalSecondaryBrightness)"
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
                "FractalLifecycleSpeed"
        };

        for (String uniform : uniforms) {
            assertTrue(fragment.contains(uniform), uniform + " missing in GLSL");
            assertTrue(json.contains("\"" + uniform + "\""),
                    uniform + " missing in JSON");
            assertTrue(java.contains("\"" + uniform + "\""),
                    uniform + " missing in Java");
        }

        assertFalse(json.contains("\"FractalBlurSamples\""));
        assertFalse(java.contains("\"FractalBlurSamples\""));
    }

    private static String read(String fileName) throws IOException {
        return Files.readString(SHADER_ROOT.resolve(fileName));
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
.\gradlew.bat test --tests "*IneffableFractalShaderContractTest"
```

Expected: FAIL because the vertex shader does not export
`robeModelPosition`, the fragment shader lacks the nursery functions, and the
new uniforms do not yet exist.

- [ ] **Step 3: Commit the failing contract test**

```powershell
git add -- src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableFractalShaderContractTest.java
git commit -m "test: define ineffable fractal nursery contract"
```

### Task 2: Implement the model-space Mandelbrot nursery

**Files:**
- Modify: `src/main/resources/assets/mnagnosis/shaders/core/doppleganger.vsh`
- Modify: `src/main/resources/assets/mnagnosis/shaders/core/doppleganger.fsh`
- Test: `src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableFractalShaderContractTest.java`

**Interfaces:**
- Consumes: `Position`, `GameTime`, and the twelve `Fractal*` uniforms declared by Task 1.
- Produces: `robeModelPosition`, `cylindricalRobePosition(vec3)`, `mandelbrotDistance(vec2, int)`, `nurseryLayer(vec2, float, float)`, and `fractalNurseryPattern(vec3)`.

- [ ] **Step 1: Export the robe model position from the vertex shader**

Add beside the existing vertex outputs:

```glsl
out vec3 robeModelPosition;
```

Assign it before the end of `main()`:

```glsl
robeModelPosition = Position;
```

- [ ] **Step 2: Replace the old fractal uniforms and implementation**

In `doppleganger.fsh`, replace the old `FractalPhase` through
`FractalBrightness` declarations with:

```glsl
uniform float FractalFieldScale;
uniform float FractalFlowX;
uniform float FractalFlowY;
uniform float FractalPrimaryCellSize;
uniform float FractalSecondaryCellSize;
uniform int FractalIterations;
uniform float FractalContourWidth;
uniform float FractalBrightness;
uniform float FractalSecondaryBrightness;
uniform float FractalGrowthMin;
uniform float FractalGrowthMax;
uniform float FractalLifecycleSpeed;
```

Add the varying:

```glsl
in vec3 robeModelPosition;
```

Replace `rotation2d`, `fractalFormula`, and `fractalFlashPattern` with:

```glsl
const float TAU = 6.28318530718;

float hashCell(vec2 cell) {
    return fract(sin(dot(cell, vec2(127.1, 311.7))) * 43758.5453123);
}

vec2 hashCell2(vec2 cell) {
    return vec2(
        hashCell(cell + vec2(19.19, 7.73)),
        hashCell(cell + vec2(83.17, 41.53))
    );
}

mat2 rotation2d(float angle) {
    float sine = sin(angle);
    float cosine = cos(angle);
    return mat2(cosine, -sine, sine, cosine);
}

vec2 complexSquare(vec2 value) {
    return vec2(
        value.x * value.x - value.y * value.y,
        2.0 * value.x * value.y
    );
}

vec2 complexMultiply(vec2 left, vec2 right) {
    return vec2(
        left.x * right.x - left.y * right.y,
        left.x * right.y + left.y * right.x
    );
}

vec2 cylindricalRobePosition(vec3 position) {
    float wrappedAngle = atan(position.x, -position.z) / TAU + 0.5;
    return vec2(wrappedAngle, -position.y * 0.45);
}

float mandelbrotDistance(vec2 constantValue, int iterationLimit) {
    vec2 z = vec2(0.0);
    vec2 derivative = vec2(0.0);
    float escaped = 0.0;
    float squaredRadius = 0.0;

    for (int iteration = 0; iteration < 48; iteration++) {
        if (iteration >= iterationLimit) {
            break;
        }
        derivative = 2.0 * complexMultiply(z, derivative)
            + vec2(1.0, 0.0);
        z = complexSquare(z) + constantValue;
        squaredRadius = dot(z, z);
        if (squaredRadius > 256.0) {
            escaped = 1.0;
            break;
        }
    }

    if (escaped < 0.5) {
        return -1.0;
    }
    float radius = sqrt(max(squaredRadius, 1.0001));
    return 0.5 * log(max(squaredRadius, 1.0001)) * radius
        / max(length(derivative), 0.0001);
}

float nurseryLayer(vec2 fieldPosition, float cellSize, float seedOffset) {
    float safeCellSize = max(cellSize, 0.001);
    vec2 gridPosition = fieldPosition / safeCellSize;
    vec2 cell = floor(gridPosition);
    vec2 local = fract(gridPosition) - 0.5;
    float seed = hashCell(cell + vec2(seedOffset));
    vec2 centerJitter = (hashCell2(cell + vec2(seedOffset)) - 0.5)
        * 0.08;
    float lifecycle = fract(
        GameTime * max(FractalLifecycleSpeed, 0.001)
        + seed
        + seedOffset * 0.137
    );
    float birth = smoothstep(0.02, 0.20, lifecycle);
    float death = 1.0 - smoothstep(0.70, 0.98, lifecycle);
    float lifeOpacity = birth * death;
    float growthPhase = smoothstep(0.04, 0.72, lifecycle);
    float growth = mix(
        max(FractalGrowthMin, 0.05),
        max(FractalGrowthMax, FractalGrowthMin + 0.01),
        growthPhase
    );
    float angle = (seed - 0.5) * 0.36;
    vec2 budPosition = rotation2d(angle)
        * (local - centerJitter)
        / growth;
    vec2 constantValue = vec2(
        budPosition.x * 3.0 - 0.5,
        budPosition.y * 2.4
    );
    float distance = mandelbrotDistance(
        constantValue,
        clamp(FractalIterations, 8, 48)
    );
    if (distance < 0.0) {
        return 0.0;
    }

    float width = max(FractalContourWidth, 0.0001) / growth;
    float antialias = max(fwidth(distance), 0.00005);
    float contour = 1.0 - smoothstep(
        width - antialias,
        width + antialias,
        distance
    );
    float cellEdge = max(abs(local.x), abs(local.y));
    float cellFade = 1.0 - smoothstep(0.44, 0.50, cellEdge);
    return contour * lifeOpacity * cellFade;
}

float fractalNurseryPattern(vec3 modelPosition) {
    vec2 projected = cylindricalRobePosition(modelPosition);
    vec2 flow = vec2(FractalFlowX, FractalFlowY) * GameTime;
    flow.x += sin(GameTime * 7.0) * 0.035;
    vec2 field = projected * max(FractalFieldScale, 0.001) + flow;

    float primary = nurseryLayer(
        field,
        FractalPrimaryCellSize,
        0.0
    );
    float secondary = nurseryLayer(
        field + vec2(0.37, 0.61),
        FractalSecondaryCellSize,
        17.0
    );
    float filaments = max(
        primary,
        secondary * FractalSecondaryBrightness
    );
    return clamp(filaments * FractalBrightness, 0.0, 1.0);
}
```

- [ ] **Step 3: Route shader mode 3 through the nursery**

Change only the mode-3 branch in `main()`:

```glsl
if (ShaderMode == 3) {
    pattern = fractalNurseryPattern(robeModelPosition);
```

Leave the mode-2, mode-1, and fallback branches byte-for-byte unchanged.

- [ ] **Step 4: Run the focused test and confirm the remaining failure**

Run:

```powershell
.\gradlew.bat test --tests "*IneffableFractalShaderContractTest"
```

Expected: FAIL only in
`nurseryUniformsStaySynchronizedAcrossShaderJsonAndJava`, because JSON and
Java have not yet adopted the compact uniform set.

- [ ] **Step 5: Inspect the shader diff for accidental mode changes**

Run:

```powershell
git diff -- src/main/resources/assets/mnagnosis/shaders/core/doppleganger.vsh src/main/resources/assets/mnagnosis/shaders/core/doppleganger.fsh
```

Expected: the vertex varying plus replacement of the mode-3 fractal code;
triangle, circle-grid, FBM, lighting, fog, and alpha code are unchanged.

### Task 3: Synchronize and tune the nursery uniforms

**Files:**
- Modify: `src/main/resources/assets/mnagnosis/shaders/core/doppleganger.json`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/IneffableArmorLayer.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableFractalShaderContractTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorShaderModeTest.java`

**Interfaces:**
- Consumes: the twelve uniform names declared by Task 2.
- Produces: matching GLSL/JSON/Java defaults and upload calls.

- [ ] **Step 1: Replace the obsolete JSON uniform entries**

In `doppleganger.json`, replace `FractalPhase` through
`FractalBrightness` with:

```json
    { "name": "FractalFieldScale", "type": "float", "count": 1, "values": [ 5.5 ] },
    { "name": "FractalFlowX", "type": "float", "count": 1, "values": [ 0.12 ] },
    { "name": "FractalFlowY", "type": "float", "count": 1, "values": [ 0.42 ] },
    { "name": "FractalPrimaryCellSize", "type": "float", "count": 1, "values": [ 0.82 ] },
    { "name": "FractalSecondaryCellSize", "type": "float", "count": 1, "values": [ 0.48 ] },
    { "name": "FractalIterations", "type": "int", "count": 1, "values": [ 28 ] },
    { "name": "FractalContourWidth", "type": "float", "count": 1, "values": [ 0.012 ] },
    { "name": "FractalBrightness", "type": "float", "count": 1, "values": [ 0.86 ] },
    { "name": "FractalSecondaryBrightness", "type": "float", "count": 1, "values": [ 0.58 ] },
    { "name": "FractalGrowthMin", "type": "float", "count": 1, "values": [ 0.68 ] },
    { "name": "FractalGrowthMax", "type": "float", "count": 1, "values": [ 0.98 ] },
    { "name": "FractalLifecycleSpeed", "type": "float", "count": 1, "values": [ 1.75 ] }
```

- [ ] **Step 2: Replace the Java constants**

In `IneffableArmorLayer`, replace the obsolete fractal constants with:

```java
    private static final float FRACTAL_FIELD_SCALE = 5.5F;
    private static final float FRACTAL_FLOW_X = 0.12F;
    private static final float FRACTAL_FLOW_Y = 0.42F;
    private static final float FRACTAL_PRIMARY_CELL_SIZE = 0.82F;
    private static final float FRACTAL_SECONDARY_CELL_SIZE = 0.48F;
    private static final int FRACTAL_ITERATIONS = 28;
    private static final float FRACTAL_CONTOUR_WIDTH = 0.012F;
    private static final float FRACTAL_BRIGHTNESS = 0.86F;
    private static final float FRACTAL_SECONDARY_BRIGHTNESS = 0.58F;
    private static final float FRACTAL_GROWTH_MIN = 0.68F;
    private static final float FRACTAL_GROWTH_MAX = 0.98F;
    private static final float FRACTAL_LIFECYCLE_SPEED = 1.75F;
```

- [ ] **Step 3: Replace the Java uniform uploads**

In `configureShader()`, replace the obsolete fractal uploads with:

```java
            shader.safeGetUniform("FractalFieldScale")
                    .set(FRACTAL_FIELD_SCALE);
            shader.safeGetUniform("FractalFlowX").set(FRACTAL_FLOW_X);
            shader.safeGetUniform("FractalFlowY").set(FRACTAL_FLOW_Y);
            shader.safeGetUniform("FractalPrimaryCellSize")
                    .set(FRACTAL_PRIMARY_CELL_SIZE);
            shader.safeGetUniform("FractalSecondaryCellSize")
                    .set(FRACTAL_SECONDARY_CELL_SIZE);
            shader.safeGetUniform("FractalIterations")
                    .set(FRACTAL_ITERATIONS);
            shader.safeGetUniform("FractalContourWidth")
                    .set(FRACTAL_CONTOUR_WIDTH);
            shader.safeGetUniform("FractalBrightness")
                    .set(FRACTAL_BRIGHTNESS);
            shader.safeGetUniform("FractalSecondaryBrightness")
                    .set(FRACTAL_SECONDARY_BRIGHTNESS);
            shader.safeGetUniform("FractalGrowthMin")
                    .set(FRACTAL_GROWTH_MIN);
            shader.safeGetUniform("FractalGrowthMax")
                    .set(FRACTAL_GROWTH_MAX);
            shader.safeGetUniform("FractalLifecycleSpeed")
                    .set(FRACTAL_LIFECYCLE_SPEED);
```

- [ ] **Step 4: Run the focused tests and verify GREEN**

Run:

```powershell
.\gradlew.bat test --tests "*IneffableFractalShaderContractTest" --tests "*IneffableArmorShaderModeTest"
```

Expected: all focused tests PASS.

- [ ] **Step 5: Commit the synchronized implementation**

Stage only the test and shader-specific files. Because
`IneffableArmorLayer.java` already contains unrelated user changes, stage only
the fractal constant and uniform-upload hunks from that file and verify the
cached diff before committing.

```powershell
git add -- src/main/resources/assets/mnagnosis/shaders/core/doppleganger.vsh src/main/resources/assets/mnagnosis/shaders/core/doppleganger.fsh src/main/resources/assets/mnagnosis/shaders/core/doppleganger.json src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableFractalShaderContractTest.java
git diff --cached --check
git diff --cached
git commit -m "feat: rebuild ineffable fractal nursery shader"
```

Do not commit `IneffableArmorLayer.java` until its shader-only hunks can be
isolated without including the existing armor-model work. If hunk isolation is
unsafe, leave that file unstaged and report it explicitly rather than
committing unrelated changes.

### Task 4: Full verification and visual handoff

**Files:**
- Verify: all files changed by Tasks 1–3
- Preserve: every unrelated dirty worktree path

**Interfaces:**
- Consumes: completed shader implementation.
- Produces: fresh automated verification evidence and exact in-game acceptance steps.

- [ ] **Step 1: Run the complete unit-test suite**

Run:

```powershell
.\gradlew.bat test
```

Expected: BUILD SUCCESSFUL with zero failed tests.

- [ ] **Step 2: Process all resources**

Run:

```powershell
.\gradlew.bat processResources
```

Expected: BUILD SUCCESSFUL; `doppleganger.json`, `.vsh`, and `.fsh` copy
without resource-processing errors.

- [ ] **Step 3: Compile the Java source**

Run:

```powershell
.\gradlew.bat compileJava
```

Expected: BUILD SUCCESSFUL with no Java compilation errors.

- [ ] **Step 4: Audit the final diff and worktree scope**

Run:

```powershell
git diff --check
git status --short
git diff -- src/main/resources/assets/mnagnosis/shaders/core/doppleganger.vsh src/main/resources/assets/mnagnosis/shaders/core/doppleganger.fsh src/main/resources/assets/mnagnosis/shaders/core/doppleganger.json src/main/java/com/vincenthuto/mnagnosis/client/event/IneffableArmorLayer.java src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableFractalShaderContractTest.java
```

Expected: no whitespace errors; only the intended shader changes appear in
the scoped diff, while pre-existing unrelated changes remain present and
untouched.

- [ ] **Step 5: Perform in-game acceptance**

Launch the existing Forge client configuration, select Ineffable armor shader
mode `3`, equip the robes, and inspect front, side, and back views while
walking. Accept when all of the following hold:

- Several small Mandelbrot silhouettes are recognizable at normal third-person
  distance.
- Filaments crawl continuously over the hood, torso, sleeves, and lower robe.
- New buds appear behind the flow without synchronized full-robe flashing.
- Charcoal remains dominant and no broad gray or white patches occur.
- The cylindrical seam remains unobtrusive at the back.

If the client cannot be launched in the current environment, report the exact
automated verification completed and identify this visual pass as the only
remaining manual check.
