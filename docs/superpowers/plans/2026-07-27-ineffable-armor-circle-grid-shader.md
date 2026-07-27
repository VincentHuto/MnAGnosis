# Ineffable Armor Circle-Grid Shader Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a client-configurable animated checkerboard/circle style to Ineffable armor while retaining the existing triangle style.

**Architecture:** Keep one `doppleganger` core shader and select between focused triangle and circle-grid functions with an integer uniform set by `IneffableArmorLayer`. Generalize the existing client config class so it owns both HUD and armor presentation settings, and isolate the boolean-to-shader-mode decision in a pure tested enum.

**Tech Stack:** Java 17, Forge 1.20.1 config/render APIs, Minecraft GLSL 1.50 core shaders, Gradle, JUnit Jupiter 5.

## Global Constraints

- `useCircleGridArmorShader = true` selects the new circle-grid style.
- `useCircleGridArmorShader = false` selects the existing triangle style.
- The default is `true`.
- Preserve current working-tree values `TriangleScale = 150.0` and `BotaniaDisfiguration = 0.005`.
- Preserve mask alpha, Minecraft lighting, overlays, fog, and armor cloth animation.
- Retain Nicole Vella/Shadertoy/CC BY 4.0 attribution in source and project credits.
- Do not stage or overwrite unrelated working-tree changes.

---

### Task 1: Test the Boolean-to-Mode Contract

**Files:**
- Modify: `build.gradle`
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorShaderModeTest.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorShaderMode.java`

**Interfaces:**
- Produces: `IneffableArmorShaderMode.fromCircleGridEnabled(boolean)` and `uniformValue()`.

- [ ] **Step 1: Add JUnit Jupiter test support**

Add:

```groovy
testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'

tasks.named('test', Test).configure {
    useJUnitPlatform()
}
```

- [ ] **Step 2: Write the failing selector tests**

```java
class IneffableArmorShaderModeTest {
    @Test
    void trueSelectsCircleGrid() {
        assertEquals(
                IneffableArmorShaderMode.CIRCLE_GRID,
                IneffableArmorShaderMode.fromCircleGridEnabled(true)
        );
        assertEquals(1, IneffableArmorShaderMode.CIRCLE_GRID.uniformValue());
    }

    @Test
    void falseSelectsTriangles() {
        assertEquals(
                IneffableArmorShaderMode.TRIANGLES,
                IneffableArmorShaderMode.fromCircleGridEnabled(false)
        );
        assertEquals(0, IneffableArmorShaderMode.TRIANGLES.uniformValue());
    }
}
```

- [ ] **Step 3: Run the test and confirm it fails because the enum is missing**

Run: `.\gradlew.bat test --tests "*IneffableArmorShaderModeTest"`

Expected: compilation failure naming missing `IneffableArmorShaderMode`.

- [ ] **Step 4: Implement the minimal enum**

```java
public enum IneffableArmorShaderMode {
    TRIANGLES(0),
    CIRCLE_GRID(1);

    private final int uniformValue;

    IneffableArmorShaderMode(int uniformValue) {
        this.uniformValue = uniformValue;
    }

    public int uniformValue() {
        return this.uniformValue;
    }

    public static IneffableArmorShaderMode fromCircleGridEnabled(boolean enabled) {
        return enabled ? CIRCLE_GRID : TRIANGLES;
    }
}
```

- [ ] **Step 5: Run the focused tests and confirm both pass**

Run: `.\gradlew.bat test --tests "*IneffableArmorShaderModeTest"`

Expected: two tests pass.

### Task 2: Generalize the Client Config and Wire the Mode Uniform

**Files:**
- Move: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/ClientAuthorshipConfig.java`
  to `src/main/java/com/vincenthuto/mnagnosis/client/ClientConfig.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/CounterlawHudRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/IneffableArmorLayer.java`

**Interfaces:**
- Consumes: `IneffableArmorShaderMode.fromCircleGridEnabled(boolean)`.
- Produces: `ClientConfig.USE_CIRCLE_GRID_ARMOR_SHADER`, default `true`.

- [ ] **Step 1: Replace the authorship-only config class with `ClientConfig`**

Keep `ANIMATE_COUNTERLAW_HUD` under `ineffable_authorship`, then add:

```java
builder.push("ineffable_armor");
USE_CIRCLE_GRID_ARMOR_SHADER = builder
        .comment("Use the animated circle-grid Ineffable armor shader. Disable for triangles.")
        .define("useCircleGridArmorShader", true);
builder.pop();
```

- [ ] **Step 2: Update config consumers and registration**

Replace all `ClientAuthorshipConfig` imports/usages with `ClientConfig` while
retaining `"mnagnosis-client.toml"`.

- [ ] **Step 3: Set the selector uniform from the live client config**

In `IneffableArmorLayer.configureShader`, compute:

```java
int mode = IneffableArmorShaderMode
        .fromCircleGridEnabled(ClientConfig.USE_CIRCLE_GRID_ARMOR_SHADER.get())
        .uniformValue();
shader.safeGetUniform("UseCircleGrid").set(mode);
```

Keep all current triangle and disfiguration uniform assignments.

- [ ] **Step 4: Compile Java**

Run: `.\gradlew.bat compileJava`

Expected: `BUILD SUCCESSFUL`.

### Task 3: Add the Attributed Circle-Grid Shader Path

**Files:**
- Modify: `src/main/resources/assets/mnagnosis/shaders/core/doppleganger.fsh`
- Modify: `src/main/resources/assets/mnagnosis/shaders/core/doppleganger.json`
- Modify: `CREDITS.txt`

**Interfaces:**
- Consumes: integer uniform `UseCircleGrid` (`0` triangles, `1` circle grid).

- [ ] **Step 1: Add the mode uniform to shader JSON**

```json
{ "name": "UseCircleGrid", "type": "int", "count": 1, "values": [ 1 ] }
```

- [ ] **Step 2: Refactor the current triangle math into a helper**

Create `float trianglePattern(vec2 uv)` returning the current monochrome
pattern after applying the existing scale, seams, time band, and brightness.

- [ ] **Step 3: Adapt the Shadertoy circle-grid pattern**

Add the Nicole Vella/Shadertoy/CC BY 4.0 notice and implement helpers for:

```glsl
float remapValue(float value, float min1, float max1, float min2, float max2)
float circleMask(vec2 st, vec2 pos, float radius)
float circleGridPattern(vec2 uv)
```

Use a `10.0` square grid, alternating parity, four moving edge circles, a
`0.45` animation delay, and derivative-based anti-aliasing.

- [ ] **Step 4: Share the final lighting/fog path**

Select with:

```glsl
float pattern = UseCircleGrid != 0
        ? circleGridPattern(texCoord0)
        : trianglePattern(texCoord0);
```

Tint the pattern between lighting-aware charcoal and off-white, preserve
`litColor.a`, then call `linear_fog`.

- [ ] **Step 5: Add project credit**

Add a concise credit naming Nicole Vella, the Shadertoy URL, and CC BY 4.0.

- [ ] **Step 6: Validate resources**

Run: `.\gradlew.bat processResources`

Expected: `BUILD SUCCESSFUL`; parse `doppleganger.json` with PowerShell
`ConvertFrom-Json` without error.

### Task 4: Full Verification

**Files:**
- Review all files above; do not modify unrelated work.

- [ ] **Step 1: Run focused selector tests**

Run: `.\gradlew.bat test --tests "*IneffableArmorShaderModeTest"`

Expected: two tests pass.

- [ ] **Step 2: Run a clean full build**

Run: `.\gradlew.bat clean build`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Inspect the final diff and worktree**

Run:

```powershell
git diff --check
git status --short
git diff -- build.gradle CREDITS.txt src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java src/main/java/com/vincenthuto/mnagnosis/client src/main/resources/assets/mnagnosis/shaders/core/doppleganger.fsh src/main/resources/assets/mnagnosis/shaders/core/doppleganger.json
```

Confirm the shader feature is isolated and pre-existing unrelated edits remain
unstaged and intact.

