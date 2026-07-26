# Rooted Precision Tendril Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Precision Living Land visibly emerge from intact terrain and stretch continuously from a fixed terrain root to its moving head.

**Architecture:** Carry the selected source face into each projected strike and synchronize its immutable emergence/root position. Projected strikes use a deterministic anchored curve while the renderer elongates terrain sections between curve points; non-projected strikes retain the existing follower-chain path.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1, Mana and Artifice spell API, Forge GameTests.

## Global Constraints

- Precision must not remove or replace its source terrain.
- The final projected segment remains fixed at the selected source face.
- The projected tendril remains visually continuous from source to head.
- Non-Precision Living Land behavior remains unchanged.
- Preserve the existing four-strike and two-controller owner limits.

---

### Task 1: Reproduce projected launch and anchoring

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Consumes: `LivingLandControllerEntity.configure(...)`, `LivingLandStrikeEntity`
- Produces: regression coverage for projected launch, visible emergence, and fixed roots

- [ ] **Step 1: Write the failing launch test**

Create valid layered floor terrain around a hostile target, configure a
projected controller, tick it through its first wave, and assert that a
projected `LivingLandStrikeEntity` exists while every source block remains.

- [ ] **Step 2: Write the failing root test**

Configure a projected strike with an explicit `Direction.UP` emergence face,
move its head through a tick, and assert that `getRootPosition()` is outside
the source block and equals the last segment position.

- [ ] **Step 3: Run the GameTests to verify RED**

Run: `.\gradlew.bat runGameTestServer --no-daemon`

Expected: the new launch/root assertions fail against the current
center-of-block, unanchored implementation.

### Task 2: Preserve the projected terrain root

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/LivingLandControllerEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/LivingLandStrikeEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/livingland/LivingLandTendrilMath.java`

**Interfaces:**
- Consumes: `LivingLandTerrain.SourceCandidate.approach()`
- Produces: `LivingLandStrikeEntity.configure(..., Direction emergence, ...)`,
  `Vec3 getRootPosition()`, and anchored projected segment positions

- [ ] **Step 1: Pass the emergence direction**

Have the controller pass `source.approach()` into strike configuration.
Compute the root as the source block center plus `0.501` blocks along that
direction so it is outside intact terrain.

- [ ] **Step 2: Synchronize and persist the root**

Add synchronized root coordinates to the strike, serialize them to NBT, and
restore them on load so server and client use the same anchor.

- [ ] **Step 3: Add anchored curve math**

For projected strikes, place the head at index `0`, the exact root at the last
index, and distribute intermediate points by linear interpolation plus a
deterministic perpendicular sine bend whose envelope is zero at both ends.
Keep the current follower-chain method for non-projected strikes.

- [ ] **Step 4: Run GameTests to verify GREEN**

Run: `.\gradlew.bat runGameTestServer --no-daemon`

Expected: projected launch and anchored-motion tests pass without regressions.

### Task 3: Render continuous stretched terrain

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/LivingLandStrikeRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Consumes: projected curve positions from `getSegmentPosition(int, float)`
- Produces: continuous cuboid spans from every curve point to its neighbor

- [ ] **Step 1: Add renderer-facing span math coverage**

Assert that each projected edge has a positive finite length and that the sum
of its edge lengths is at least the direct root-to-head distance.

- [ ] **Step 2: Render projected edges**

For every adjacent projected point pair, render the carried terrain block at
the midpoint, rotate it along the edge tangent, and scale its local Z axis to
the edge length plus a small overlap. Retain alternating thin black/white
projection shells and render root/head caps. Leave the non-projected branch
unchanged.

- [ ] **Step 3: Run full verification**

Run: `.\gradlew.bat runGameTestServer --no-daemon`

Expected: all required GameTests pass.

Run: `.\gradlew.bat build --no-daemon`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit scoped files**

Commit only the controller, strike, tendril math, renderer, and GameTest
changes. Do not include unrelated staged or untracked files.
