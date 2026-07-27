# Visible Gravity Lensing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Gravity Convergence visibly distort, stretch, and mirror the world behind its black-hole core while retaining the existing square lattice particles.

**Architecture:** Correct `GravityLensController` so all visibility and projection calculations use the rendered sphere's elevated visual center. Strengthen the shared bounded lens curve and implement a two-sample radial framebuffer warp in the existing GLSL post-process.

**Tech Stack:** Java 17, Forge 1.20.1 rendering events, Minecraft `PostChain`, GLSL 1.50, Forge GameTests, Gradle

## Global Constraints

- Preserve the current square lattice particle system unchanged.
- Preserve unrelated working-tree changes.
- Process no more than three visible gravity lenses per frame.
- Keep the post effect bounded and applied before HUD rendering.

---

### Task 1: Define the Strong Bounded Lens Contract

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/gravity/GravityLensMath.java`

**Interfaces:**
- Consumes: normalized pixel distance from the event-horizon radius
- Produces: `GravityLensMath.distortion(float normalizedDistance, boolean repelling)`

- [ ] **Step 1: Strengthen the failing GameTest**

Require distortion near `1.05F` radii to exceed `0.30F`, remain polarity
aware, decrease outward, and reach exactly zero at `HALO_RADIUS`.

- [ ] **Step 2: Run the focused GameTests to verify RED**

Run: `./gradlew runGameTestServer`

Expected: FAIL because the current maximum distortion is `0.035F`.

- [ ] **Step 3: Implement the bounded inverse-radius curve**

Set `HALO_RADIUS` to `4.5F` and combine a strong inverse-radius horizon term
with a smooth outer falloff, retaining a finite signed result.

- [ ] **Step 4: Run focused GameTests to verify GREEN**

Run: `./gradlew runGameTestServer`

Expected: all GameTests pass.

### Task 2: Align and Render Actual Space Bending

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/render/gravity/GravityLensController.java`
- Modify: `src/main/resources/assets/mnagnosis/shaders/program/gravity_lens.fsh`

**Interfaces:**
- Consumes: up to three `(screenX, screenY, radiusPixels, polarity)` uniforms
- Produces: the scene sampled through a radial primary warp and mirrored Einstein band

- [ ] **Step 1: Add failing artifact contracts**

Require controller bytecode to contain `VISUAL_CENTER_Y_OFFSET` and require
the fragment shader to contain `sampleBentSpace` and `mirroredUv`.

- [ ] **Step 2: Run GameTests to verify RED**

Run: `./gradlew runGameTestServer`

Expected: FAIL because the center constant and multi-sample shader do not exist.

- [ ] **Step 3: Align all controller calculations**

Use a `1.5D` visual-center offset for interpolation, line of sight, distance
sorting, and frustum bounds without modifying the renderer or particle code.

- [ ] **Step 4: Implement the multi-sample GLSL lens**

Compute a strong radial primary sample from the shared 4.5-radius curve.
Blend a mirrored scene sample across the center only in a narrow Einstein
band, retain the photon-ring light, and clamp all UVs.

- [ ] **Step 5: Verify GameTests and production build**

Run: `./gradlew runGameTestServer`

Run: `./gradlew build`

Expected: all GameTests pass and the production build exits successfully.

