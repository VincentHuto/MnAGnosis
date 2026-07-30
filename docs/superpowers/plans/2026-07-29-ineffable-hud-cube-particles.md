# Ineffable HUD Cube Particles Implementation Plan

> **For Codex:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Render four deterministic, miniature rotating 3D cubes over the Ineffable HUD's existing square nodes using the black and white world-particle textures.

**Architecture:** Add a pure animation/layout model that converts the concept texture's source-space anchors into compact HUD coordinates. Add a stateless GUI renderer that samples that model and emits six textured faces per cube after the rest of the HUD has rendered. Forward partial tick from the existing HUD mixin so rotation remains smooth.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1 rendering APIs, JOML, JUnit 5, Gradle.

---

### Task 1: Add the deterministic cube layout model

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudCubeLayout.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudCubeLayoutTest.java`

**Step 1: Write the failing test**

Cover:

- exactly four anchors;
- the four approved 976×158 source coordinates map into the 153×25 display space;
- white/black texture variants alternate;
- identical inputs produce identical samples;
- each emitter has distinct XYZ rotation;
- drift, bob, half-size, and alpha remain within the design bounds;
- `animationTime(gameTime, partialTick)` preserves the partial tick.

**Step 2: Run the focused test to verify it fails**

Run: `.\gradlew.bat test --tests "*IneffableHudCubeLayoutTest"`

Expected: FAIL because `IneffableHudCubeLayout` does not exist.

**Step 3: Write the minimal implementation**

Create an immutable utility with:

- four source-space anchors;
- `TextureVariant` enum;
- `Sample` record;
- source-to-display conversion based on `IneffableHudConcept` dimensions;
- deterministic sine/cosine motion using fixed per-emitter phases and rates;
- no Minecraft client or mutable particle state.

**Step 4: Run the focused test to verify it passes**

Run: `.\gradlew.bat test --tests "*IneffableHudCubeLayoutTest"`

Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudCubeLayout.java src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudCubeLayoutTest.java
git commit -m "test: define ineffable HUD cube animation"
```

### Task 2: Render the cubes over the HUD

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudCubeRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/mixin/client/MixinHUDOverlayRenderer.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudCubeAssetsTest.java`

**Step 1: Write the failing asset contract test**

Assert that both existing particle textures are present and remain 16×16:

- `textures/particle/ineffable_white_cube.png`;
- `textures/particle/ineffable_black_cube.png`.

**Step 2: Run the focused tests**

Run: `.\gradlew.bat test --tests "*IneffableHudCube*Test"`

Expected: PASS for the existing assets. This is a characterization test that protects the renderer contract.

**Step 3: Implement the renderer**

Create a stateless renderer that:

- reuses the six-face topology and UV winding from `OutlinedCubeParticle`;
- rotates all eight cube corners with JOML quaternions;
- draws with the approved white/black textures, translucency, and full-bright lighting;
- places cubes relative to `FRAME_X` and `FRAME_Y`;
- disables depth and culling only around this overlay and restores render state in `finally`;
- flushes only the render types it uses.

**Step 4: Wire smooth time into the HUD**

- Add `partialTick` to `IneffableHudRenderer.render`.
- Forward it from `MixinHUDOverlayRenderer`.
- After frame resources and contradiction marks, render cubes using `level.getGameTime() + partialTick`.
- Skip only the cube overlay when no client level exists.

**Step 5: Compile and run focused tests**

Run: `.\gradlew.bat test --tests "*IneffableHudCube*Test"`

Expected: PASS and production sources compile.

**Step 6: Commit**

```bash
git add src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudCubeRenderer.java src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java src/main/java/com/vincenthuto/mnagnosis/mixin/client/MixinHUDOverlayRenderer.java src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudCubeAssetsTest.java
git commit -m "feat: animate 3D cubes over ineffable HUD"
```

### Task 3: Verify the integrated HUD change

**Files:**
- Verify all changed files.

**Step 1: Run the full verification**

Run: `.\gradlew.bat test processResources`

Expected: BUILD SUCCESSFUL.

**Step 2: Check patch hygiene**

Run: `git diff --check`

Expected: no output.

**Step 3: Confirm repository state**

Run: `git status --short`

Expected: clean working tree.

