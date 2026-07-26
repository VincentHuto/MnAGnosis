# Living Land Articulated Tendrils Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace rigid Living Land pillar motion with deterministic articulated stone tendrils while preserving bundled payload conservation and Precision projection.

**Architecture:** A focused `LivingLandTendrilMath` unit calculates emergence spacing, lateral bend, follower constraints, tangents, and swept bounds. `LivingLandStrikeEntity` owns transient segment histories and uses the math on server and client; the renderer places and rotates each block independently.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1, Mana and Artifice 3.1.11, vanilla `Vec3`/`AABB`, Forge GameTests.

## Global Constraints

- Preserve all existing physical payload conservation guarantees.
- Precision never edits, deposits, restores, or drops terrain.
- Tendrils contain the existing three-to-five exact carried states.
- Server collision is authoritative and follows every curved segment.
- Client simulation must be deterministic and require no custom per-tick packet.
- Never force-load chunks.
- Preserve the staged/deleted armor texture and `.codex-remote-attachments/`.

---

### Task 1: Tendril motion math

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/livingland/LivingLandTendrilMath.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `emergenceSpacing(int age): double`
- Produces: `lateralAcceleration(Vec3 forward, LivingLandMode mode, int age, int seed): Vec3`
- Produces: `constrainFollower(Vec3 leader, Vec3 follower, double spacing, Vec3 bend): Vec3`
- Produces: `localTangent(List<Vec3> segments): Vec3`
- Produces: `sweptBounds(Vec3 previous, Vec3 current): AABB`

- [ ] Write failing GameTests asserting spacing is zero at age zero and 0.78
  at age six, followers remain exactly one spacing from leaders, lateral
  acceleration is deterministic and nonzero, and each mode uses a distinct
  bend plane.
- [ ] Run `.\gradlew.bat compileJava` and verify failure for the absent math
  class.
- [ ] Implement finite-value guards, smoothstep emergence, deterministic
  sinusoidal lateral vectors, follower constraints, tangent fallback, and
  0.4-radius swept bounds.
- [ ] Run the full GameTest suite and require every test to pass.
- [ ] Commit exact task files with `feat: model articulated Living Land motion`.

### Task 2: Articulated strike simulation and collision

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/LivingLandStrikeEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `getSegmentPosition(int index, float partialTick): Vec3`
- Produces: `getSegmentTangent(int index, float partialTick): Vec3`
- Consumes all `LivingLandTendrilMath` functions.

- [ ] Write failing GameTests asserting a configured five-state strike begins
  collapsed, becomes curved after six ticks, keeps neighboring segments within
  0.78 blocks, and exposes a non-central swept collision.
- [ ] Run `.\gradlew.bat compileJava` and verify failure for missing segment
  accessors.
- [ ] Store five current/previous segment positions, initialize at the source,
  update the head with homing and lateral acceleration, constrain followers,
  and use every segment's swept bounds for target collision.
- [ ] Use the leading local tangent for payload settlement and reset a loaded
  chain to its persisted head position.
- [ ] Run all GameTests and require every test to pass.
- [ ] Commit exact task files with `feat: animate Living Land as stone tendrils`.

### Task 3: Per-segment rendering and verification

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/LivingLandStrikeRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Consumes `getSegmentPosition` and `getSegmentTangent`.

- [ ] Write a packaging GameTest that requires the articulated renderer class
  and preserves the projected Precision resources.
- [ ] Render each block at its interpolated relative segment position, rotate
  it along its own tangent, add deterministic alternating roll, and retain the
  per-segment Precision shell.
- [ ] Run `.\gradlew.bat clean build` and require `BUILD SUCCESSFUL`.
- [ ] Run the complete GameTest suite and confirm its success marker.
- [ ] Start the client hidden, confirm sound and block-atlas initialization,
  then terminate only smoke-test processes.
- [ ] Audit the JAR for tendril math, strike, renderer, payload, and Precision
  resources; run `git diff --check` and inspect status.
- [ ] Commit exact implementation files with
  `feat: render curved Living Land tendrils`.

## Self-Review

- Spec coverage: emergence, curvature, mode behavior, collision, settlement,
  rendering, Precision, persistence, and verification all map to tasks.
- Placeholder scan: no deferred decisions or incomplete steps remain.
- Type consistency: math and entity accessor signatures match all consumers.
