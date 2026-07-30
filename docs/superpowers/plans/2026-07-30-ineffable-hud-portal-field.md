# Ineffable HUD Portal Field Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Render a subtle procedural black, white, and cyan portal/star field across the complete Ineffable mana channel beneath all live resource layers.

**Architecture:** Register one textureless `POSITION_TEX` core shader through the existing `CoreShaders` lifecycle. A stateless HUD renderer draws one immediate channel quad with time and opacity uniforms while the frame's high-resolution, angled pose is active; `IneffableHudRenderer` inserts that pass directly after the base texture.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1 core shaders, GLSL 150, Mojang rendering APIs, Gson, JUnit 5, Gradle.

## Global Constraints

- Channel rectangle is source-space `(80, 52, 790, 54)`.
- Palette is only near-black, white, and cyan.
- `PortalOpacity` is `0.88`.
- The field spans the full channel regardless of mana amount.
- Draw order is base, portal, disruption, mana, cap, paradox, XP.
- The shader samples no textures and uses no framebuffer or post-processing pass.
- Missing shader state leaves the existing black channel unchanged.
- The active HUD perspective and frame scale must apply to the shader quad.

---

### Task 1: Register and define the portal core shader

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/shader/core/CoreShaders.java`
- Create: `src/main/resources/assets/mnagnosis/shaders/core/ineffable_hud_portal.json`
- Create: `src/main/resources/assets/mnagnosis/shaders/core/ineffable_hud_portal.vsh`
- Create: `src/main/resources/assets/mnagnosis/shaders/core/ineffable_hud_portal.fsh`
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPortalShaderContractTest.java`

**Interfaces:**
- Produces: nullable `CoreShaders.ineffableHudPortal()` using `DefaultVertexFormat.POSITION_TEX`.
- Produces shader uniforms: `ModelViewMat`, `ProjMat`, `PortalTime`, `PortalOpacity`.

- [ ] **Step 1: Write the failing shader contract test**

Parse the shader JSON and assert:

```java
assertEquals("mnagnosis:ineffable_hud_portal",
        program.get("vertex").getAsString());
assertEquals("mnagnosis:ineffable_hud_portal",
        program.get("fragment").getAsString());
assertEquals(List.of("Position", "UV0"), attributes(program));
assertEquals(Set.of(
        "ModelViewMat", "ProjMat", "PortalTime", "PortalOpacity"
), uniformNames(program));
```

Read the vertex and fragment files and assert that the vertex shader passes
`UV0`, while the fragment shader contains `PortalTime`, `PortalOpacity`,
`hash21`, three `starLayer` calls, aspect ratio `790.0 / 54.0`, and cyan
`vec3(0.0, 0.72, 0.83)`. Read `CoreShaders.java` and assert registration uses
`rloc("ineffable_hud_portal")` and `DefaultVertexFormat.POSITION_TEX`.

- [ ] **Step 2: Run the contract test and verify RED**

```powershell
.\gradlew.bat test --tests "*IneffableHudPortalShaderContractTest"
```

Expected: FAIL because the shader resources and registration do not exist.

- [ ] **Step 3: Add the shader registration**

Add a nullable `ShaderInstance ineffableHudPortal` field, register
`mnagnosis:ineffable_hud_portal` with `POSITION_TEX`, and expose:

```java
public static ShaderInstance ineffableHudPortal() {
    return ineffableHudPortal;
}
```

- [ ] **Step 4: Add the GLSL program**

The vertex shader must transform `Position`, pass `UV0`, and use
`ModelViewMat`/`ProjMat`. The fragment shader must:

```glsl
vec2 fieldUv = vec2(texCoord.x * (790.0 / 54.0), texCoord.y);
vec3 color = vec3(0.003, 0.005, 0.008);
color += starLayer(fieldUv, PortalTime, 23.0, 0.10, 1.00);
color += starLayer(fieldUv, PortalTime, 37.0, -0.06, 0.66);
color += starLayer(fieldUv, PortalTime, 61.0, 0.035, 0.42);
float band = warpedBand(fieldUv, PortalTime);
color += vec3(0.0, 0.72, 0.83) * band * 0.12;
fragColor = vec4(color, PortalOpacity);
```

Implement deterministic `hash21`, compact circular stars with a rare cyan
selection, and a low-amplitude sinusoidal `warpedBand`. Clamp the final RGB to
`0.0..1.0`.

- [ ] **Step 5: Run the contract test and verify GREEN**

```powershell
.\gradlew.bat test --tests "*IneffableHudPortalShaderContractTest"
```

Expected: PASS.

- [ ] **Step 6: Commit shader resources**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/client/shader/core/CoreShaders.java src/main/resources/assets/mnagnosis/shaders/core/ineffable_hud_portal.json src/main/resources/assets/mnagnosis/shaders/core/ineffable_hud_portal.vsh src/main/resources/assets/mnagnosis/shaders/core/ineffable_hud_portal.fsh src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPortalShaderContractTest.java
git commit -m "feat: add Ineffable HUD portal shader"
```

### Task 2: Draw the shader beneath the live mana layers

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPortalRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPortalRendererTest.java`

**Interfaces:**
- Consumes: `CoreShaders.ineffableHudPortal()`.
- Produces: `IneffableHudPortalRenderer.render(GuiGraphics, float animationTicks)`.
- Produces: `IneffableHudPortalRenderer.animationSeconds(long, float)`.

- [ ] **Step 1: Write the failing renderer behavior tests**

Assert:

```java
assertEquals(80, IneffableHudPortalRenderer.X);
assertEquals(52, IneffableHudPortalRenderer.Y);
assertEquals(790, IneffableHudPortalRenderer.WIDTH);
assertEquals(54, IneffableHudPortalRenderer.HEIGHT);
assertEquals(4.03125F,
        IneffableHudPortalRenderer.animationSeconds(80L, 0.625F),
        0.0001F);
```

Also read `IneffableHudRenderer.java` and assert the first occurrences satisfy:

```java
assertTrue(baseIndex < portalIndex);
assertTrue(portalIndex < disruptionIndex);
assertTrue(portalIndex < manaIndex);
```

- [ ] **Step 2: Run the renderer test and verify RED**

```powershell
.\gradlew.bat test --tests "*IneffableHudPortalRendererTest"
```

Expected: FAIL because `IneffableHudPortalRenderer` and its HUD call do not
exist.

- [ ] **Step 3: Implement the stateless renderer**

`render` must:

```java
ShaderInstance shader = CoreShaders.ineffableHudPortal();
if (shader == null) {
    return;
}
graphics.flush();
shader.safeGetUniform("PortalTime").set(animationTicks / 20.0F);
shader.safeGetUniform("PortalOpacity").set(0.88F);
RenderSystem.setShader(() -> shader);
```

Enable standard blending, disable depth writes and culling, draw one
`POSITION_TEX` quad using the active pose matrix and UVs `(0,0)` through
`(1,1)`, then restore shader color, culling, depth writes, and blending in a
`finally` block.

- [ ] **Step 4: Insert the portal pass into frame composition**

Pass `gameTime + partialTick` into `drawConceptFrame`. Immediately after
`blitFull(graphics, IneffableHudConcept.baseTexture())`, call:

```java
IneffableHudPortalRenderer.render(graphics, animationTicks);
```

Use `0.0F` when no client level exists. Keep disruption and every resource
blit after the portal call.

- [ ] **Step 5: Run focused HUD tests**

```powershell
.\gradlew.bat test --tests "*IneffableHudPortal*Test" --tests "*IneffableHudConceptTest"
```

Expected: PASS.

- [ ] **Step 6: Run complete verification**

```powershell
.\gradlew.bat test processResources
git diff --check
```

Expected: `BUILD SUCCESSFUL` and no patch-hygiene output.

- [ ] **Step 7: Commit renderer integration**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPortalRenderer.java src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPortalRendererTest.java
git commit -m "feat: render portal field behind Ineffable mana"
```
