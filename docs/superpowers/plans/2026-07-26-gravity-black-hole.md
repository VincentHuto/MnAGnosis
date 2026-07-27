# Gravity Convergence Black Hole Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Render Gravity Convergence fields as spherical black holes whose bounded post shader visibly bends the world around their event horizons.

**Architecture:** A pure common-side `GravityLensMath` defines stable falloff and normalized lens parameters. Client rendering is split between `GravityFieldRenderer` for depth-respecting sphere/ring geometry and `GravityLensController` for projecting up to three visible fields into a standalone post chain processed at `AFTER_LEVEL`.

**Tech Stack:** Java 17, Forge 47.4.0, Minecraft 1.20.1 rendering APIs, Mojang `PostChain`/GLSL 1.50, Forge GameTests, Gradle.

## Global Constraints

- Work inline in the current MnAGnosis repository, as explicitly requested.
- Preserve unrelated worktree changes and `.codex-remote-attachments/`.
- Keep the visual monochrome: opaque black horizon and white/gray photon ring.
- Support at most `GravityFieldEntity.MAX_FIELDS_PER_OWNER` simultaneous lenses.
- Shader failure must never affect gameplay or crash the client.
- Use test-driven development and verify client shader compilation before completion.

---

### Task 1: Stable Lens Parameter Math

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/gravity/GravityLensMath.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `GravityLensMath.distortion(float normalizedDistance, boolean repelling)` and `GravityLensMath.clampScreenRadius(float screenRadius)`.
- Consumes: no client classes, allowing dedicated-server GameTests.

- [ ] **Step 1: Write failing GameTests**

Add tests asserting that lens distortion is finite, strongest near the horizon,
zero outside the halo, and opposite in sign for repel. Assert screen radius is
clamped to a safe nonzero range.

- [ ] **Step 2: Verify RED**

Run: `.\gradlew.bat compileJava --no-daemon`

Expected: compilation fails because `GravityLensMath` does not exist.

- [ ] **Step 3: Implement the minimal pure math class**

Implement a smooth bounded falloff over normalized radii `1.0..4.0`, a polarity
sign, and screen-radius clamping between `2.0` and `640.0` pixels.

- [ ] **Step 4: Verify GREEN**

Run: `.\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: all existing and new GameTests pass.

- [ ] **Step 5: Commit**

Commit only the math class and its GameTests.

### Task 2: Sphere and Photon-Ring Entity Renderer

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/GravityFieldRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Consumes: `GravityFieldEntity.getRadius()`, `getPolarity()`, and interpolated tick time.
- Produces: opaque UV-sphere geometry and crossed monochrome annuli centered on the entity.

- [ ] **Step 1: Add a failing renderer contract test**

Require the compiled renderer to expose named horizon tessellation constants and
verify that the renderer resource contract no longer consists of an empty class.

- [ ] **Step 2: Verify RED**

Run: `.\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: the renderer contract test fails because sphere geometry is absent.

- [ ] **Step 3: Implement sphere and ring rendering**

Render a small UV sphere with opaque-black vertices, then two thin segmented
annuli with opposite rotations. Scale the horizon modestly from gameplay radius
so the visual remains readable without implying the whole force volume is solid.

- [ ] **Step 4: Verify GREEN**

Run: `.\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: the renderer contract and all prior tests pass.

- [ ] **Step 5: Commit**

Commit only the renderer and contract-test changes.

### Task 3: Multi-Field Post-Process Lensing

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/render/gravity/GravityLensController.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java`
- Create: `src/main/resources/assets/mnagnosis/shaders/post/gravity_lens.json`
- Create: `src/main/resources/assets/mnagnosis/shaders/program/gravity_lens.json`
- Create: `src/main/resources/assets/mnagnosis/shaders/program/gravity_lens.vsh`
- Create: `src/main/resources/assets/mnagnosis/shaders/program/gravity_lens.fsh`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Consumes: `RenderLevelStageEvent` matrices/camera, visible
  `GravityFieldEntity` instances, and `GravityLensMath.clampScreenRadius`.
- Produces: `GravityLensController.render(RenderLevelStageEvent)` and
  `GravityLensController.reset()` lifecycle entry points.

- [ ] **Step 1: Write failing resource and controller contract tests**

Require all four shader resources, the controller class, three lens uniform
slots, and Forge event wiring.

- [ ] **Step 2: Verify RED**

Run: `.\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: tests fail because the controller and shader resources are absent.

- [ ] **Step 3: Implement projection and lifecycle**

Collect the nearest three in-frustum, line-of-sight fields; project their center
and camera-up offset; populate three vec4 uniforms; lazily create/resize/process
the post chain; reset it on logout.

- [ ] **Step 4: Implement the GLSL pass**

Copy the completed world image while applying bounded radial refraction,
achromatic fringe, and photon-ring luminance around each populated lens.
Preserve alpha and return the original sample outside every lens halo.

- [ ] **Step 5: Verify GREEN**

Run: `.\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: every GameTest passes.

- [ ] **Step 6: Commit**

Commit only the controller, event wiring, shader resources, and tests.

### Task 4: Full Verification and Shader Smoke Test

**Files:**
- Modify only files above if verification reveals a tested defect.

**Interfaces:**
- Consumes: the complete black-hole renderer and post-process feature.
- Produces: fresh evidence that server tests, compilation, packaging, and client shader loading succeed.

- [ ] **Step 1: Run the full GameTest suite**

Run: `.\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: all required GameTests pass with zero failures.

- [ ] **Step 2: Run a clean build**

Run: `.\gradlew.bat build --no-daemon --console=plain`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run a bounded client smoke launch**

Launch `runClient`, wait until resource loading reaches the title screen, stop
the launched client process, and inspect `run/logs/latest.log`. Require no shader
compile/link error, post-chain load failure, or GravityLensController exception.

- [ ] **Step 4: Audit scope**

Run `git status --short` and `git diff --check`. Confirm the armor texture deletion
and attachment directory remain untouched and uncommitted.

