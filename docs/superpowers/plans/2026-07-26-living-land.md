# Living Land Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Tier 6 Ineffable component that classifies nearby terrain and conservatively relocates real blocks through dodgeable ceiling, wall, or floor attacks against a selected creature.

**Architecture:** `LivingLandTerrain` performs deterministic loaded-chunk scans, and `LivingLandConservation` owns all protected source removal and idempotent settlement. A persistent `LivingLandControllerEntity` schedules waves while lightweight `LivingLandStrikeEntity` instances carry exact block states through collision and recovery.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1, Mana and Artifice 3.1.11 spell API, vanilla entity/NBT/block-state APIs, Forge block events, Forge GameTests.

## Global Constraints

- Implement Living Land only; Reassembled Land and True Self remain separate projects.
- Living Land requires a hostile living target and Tier 6 Ineffable progression.
- Terrain edits operate only in already loaded chunks and never force-load.
- Every removed source block is deposited, restored, or converted to at most one corresponding block item.
- Block entities, fluids, unbreakable blocks, immune-tagged blocks, protected infrastructure, denied interactions, and cancelled Forge block events are never consumed.
- Damage is ordinary sourced damage and respects normal defensive rules.
- Maximum two conductors and eight active strikes per caster per dimension.
- Preserve `src/main/resources/assets/mnagnosis/textures/models/armor/marrow_crown_layer_1.png` exactly in its existing staged/deleted state.

---

### Task 1: Terrain modes, scanning, and source eligibility

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/livingland/LivingLandMode.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/livingland/LivingLandTerrain.java`
- Create: `src/main/resources/data/mnagnosis/tags/blocks/living_land_immune.json`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `LivingLandMode { CEILING_CRUSH, WALL_LANCES, FLOOR_TEETH }`
- Produces: `LivingLandTerrain.SourceCandidate(BlockPos source, Direction approach)`
- Produces: `LivingLandTerrain.ScanResult(LivingLandMode mode, List<SourceCandidate> sources)`
- Produces: `LivingLandTerrain.scan(ServerLevel level, ServerPlayer caster, LivingEntity target, int radius): Optional<ScanResult>`
- Produces: `LivingLandTerrain.isEligibleSource(ServerLevel level, ServerPlayer caster, BlockPos pos): boolean`

- [ ] **Step 1: Write failing terrain GameTests**

Create isolated fixtures by setting stone floor, ceiling, and wall arrangements
around spawned zombies. Assert ceiling beats walls, two distinct walls beat the
floor fallback, open ground chooses floor teeth, a single source yields no mode,
and air/fluid/block-entity/bedrock/barrier/immune-tag candidates are rejected.

- [ ] **Step 2: Run the compiler and verify RED**

Run: `./gradlew.bat compileJava`

Expected: compilation fails because `LivingLandMode` and `LivingLandTerrain`
do not exist.

- [ ] **Step 3: Implement bounded deterministic scans**

Check `level.hasChunkAt(pos)` before every state read. Search ceiling offsets
`2..5`, floor offsets `1..3`, and cardinal rays `2..radius`; retain candidates
only when the path from source to target is air/replaceable and
`isEligibleSource` passes. Sort candidates by distance, then coordinates, and
apply the exact ceiling → wall → floor priority.

- [ ] **Step 4: Run all GameTests and verify GREEN**

Run: `./gradlew.bat runGameTestServer`

Expected: all existing tests plus terrain classification tests pass.

- [ ] **Step 5: Commit**

Commit exact task files with:
`feat: classify terrain for Living Land`.

### Task 2: Conservation-safe block reservation and settlement

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/livingland/LivingLandConservation.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `LivingLandConservation.Reservation(BlockPos source, BlockState state)`
- Produces: `LivingLandConservation.reserve(ServerLevel level, ServerPlayer caster, BlockPos source): Optional<Reservation>`
- Produces: `LivingLandConservation.settle(ServerLevel level, ServerPlayer caster, Reservation reservation, BlockPos preferred): SettlementResult`
- Produces: `LivingLandConservation.SettlementResult { DEPOSITED, RESTORED, DROPPED, FAILED }`

- [ ] **Step 1: Write failing conservation GameTests**

Assert reservation captures the exact state and replaces its source with air
without drops; denied or ineligible sources remain unchanged; settlement uses
the preferred replaceable position; blocked preferred positions search a
deterministic 3×3×3 shell; completely blocked shells restore the source; and a
second settlement call cannot place or drop a duplicate.

- [ ] **Step 2: Run the compiler and verify RED**

Run: `./gradlew.bat compileJava`

Expected: compilation fails because `LivingLandConservation` does not exist.

- [ ] **Step 3: Implement event-aware conservation**

Require `level.mayInteract`, call
`ForgeHooks.onBlockBreakEvent(level, caster.gameMode.getGameModeForPlayer(), caster, source)`,
and set air only after a non-negative result. For placement, snapshot the
destination, set the carried state, post `BlockEvent.EntityPlaceEvent`, and
restore the previous destination if cancelled. Keep settlement idempotence in
the reservation object and never overwrite non-replaceable states.

- [ ] **Step 4: Run all GameTests and verify GREEN**

Run: `./gradlew.bat runGameTestServer`

Expected: conservation and existing tests pass with no duplicated block items.

- [ ] **Step 5: Commit**

Commit exact task files with:
`feat: conserve blocks moved by Living Land`.

### Task 3: Moving land-strike entity

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/LivingLandStrikeEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `LivingLandStrikeEntity.configure(ServerPlayer owner, LivingEntity target, LivingLandMode mode, LivingLandConservation.Reservation reservation, float damage, float speed): void`
- Produces: `LivingLandStrikeEntity.activeCount(ServerLevel level, UUID ownerId): int`
- Consumes: `LivingLandConservation.settle(...)`

- [ ] **Step 1: Write failing strike GameTests**

Assert exact block-state NBT round-trip, bounded steering toward a moving target,
one-time collision damage, armor-respecting damage, mode-specific knockback,
timeout settlement, invalid-target settlement, unloaded-destination refusal,
and no duplicate settlement after repeated removal calls.

- [ ] **Step 2: Run the compiler and verify RED**

Run: `./gradlew.bat compileJava`

Expected: compilation fails because `LivingLandStrikeEntity` and
`EntityRegistry.LIVING_LAND_STRIKE` do not exist.

- [ ] **Step 3: Implement strike movement and recovery**

Sync mode and carried block state ID for clients; persist the complete state
with `NbtUtils.writeBlockState`. On each server tick, reject unloaded movement,
apply a capped homing correction toward the target body, move with vanilla
collision, detect living intersections in the swept AABB, apply
`level.damageSources().indirectMagic(this, owner)`, then settle through the
single idempotent recovery path.

- [ ] **Step 4: Run all GameTests and verify GREEN**

Run: `./gradlew.bat runGameTestServer`

Expected: all strike lifecycle, collision, and conservation tests pass.

- [ ] **Step 5: Commit**

Commit exact task files with:
`feat: animate conserved Living Land strikes`.

### Task 4: Terrain conductor scheduling

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/LivingLandControllerEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `LivingLandControllerEntity.configure(ServerPlayer owner, LivingEntity target, float radius, int durationTicks, float magnitude, float speed): void`
- Produces: `LivingLandControllerEntity.makeRoomFor(ServerLevel level, UUID ownerId): void`
- Consumes: `LivingLandTerrain.scan(...)`
- Consumes: `LivingLandConservation.reserve(...)`
- Consumes: `LivingLandStrikeEntity.configure(...)`

- [ ] **Step 1: Write failing controller GameTests**

Assert target-following position, 16-tick wave cadence, terrain-mode selection,
`2 + floor(magnitude)` strike count capped at five, eight-active-strike limit,
skipped unsupported waves, dead/allied/unloaded target expiry, duration expiry,
NBT clamping, and third-cast oldest-controller replacement.

- [ ] **Step 2: Run the compiler and verify RED**

Run: `./gradlew.bat compileJava`

Expected: compilation fails because `LivingLandControllerEntity` and its entity
registration do not exist.

- [ ] **Step 3: Implement server-authoritative waves**

Follow the target each tick, decrement duration, and every 16 ticks rescan.
Choose distinct sources in scan order, stop at the per-wave or per-owner active
strike cap, reserve each source immediately before spawning, and restore the
reservation if entity addition fails. Clamp loaded values to the spec ranges.

- [ ] **Step 4: Run all GameTests and verify GREEN**

Run: `./gradlew.bat runGameTestServer`

Expected: conductor scheduling and the complete existing suite pass.

- [ ] **Step 5: Commit**

Commit exact task files with:
`feat: conduct repeated Living Land attacks`.

### Task 5: M&A component, recipes, icon, and renderers

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentLivingLand.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/LivingLandControllerRenderer.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/LivingLandStrikeRenderer.java`
- Create: `src/main/resources/data/mnagnosis/recipes/components/living_land.json`
- Create: `src/main/resources/assets/mnagnosis/textures/spell/component/living_land.png`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/SpellComponentRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java`
- Modify: `src/main/resources/assets/mnagnosis/lang/en_us.json`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `SpellComponentRegistry.LIVING_LAND_ID`
- Produces: `SpellComponentRegistry.LIVING_LAND`
- Consumes: `LivingLandControllerEntity.configure(...)`
- Consumes: synced strike mode and carried block state accessors.

- [ ] **Step 1: Write failing spell and packaging GameTests**

Assert component registry identity, Tier 6 Ineffable requirement, Radius /
Duration / Magnitude / Speed plus built-in Delay attributes, living-target-only
application, owner/ally/dead rejection, controller creation and cap, recipe
resolution, icon, localization, both entity types, and both renderer class
resources.

- [ ] **Step 2: Run GameTests and verify RED**

Run: `./gradlew.bat runGameTestServer`

Expected: registration and packaging tests fail for the absent component,
recipe, icon, and renderer classes.

- [ ] **Step 3: Implement spell integration and presentation**

In `ApplyEffect`, require a `ServerPlayer`, validate the living target and team,
evict the oldest third controller, configure the new controller from component
attributes, and return success only when it is added. Register textureless
controller rendering and a strike renderer that renders the carried block with
the block dispatcher. Emit sparse deterministic black/white concrete particles
from strike client ticks. Add the exact Tier 6 recipe and language descriptions.

- [ ] **Step 4: Run all GameTests and verify GREEN**

Run: `./gradlew.bat runGameTestServer`

Expected: spell, packaging, and all previous tests pass.

- [ ] **Step 5: Commit**

Commit exact task files with:
`feat: add the Living Land spell component`.

### Task 6: Full verification

**Files:**
- Modify only files required by a failure reproduced in a new or existing test.

**Interfaces:**
- Consumes: complete Living Land behavior.
- Produces: verified distributable feature.

- [ ] **Step 1: Run clean build**

Run: `./gradlew.bat clean build`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run complete GameTest suite**

Run: `./gradlew.bat runGameTestServer`

Expected: every required test passes with zero failures.

- [ ] **Step 3: Run client initialization smoke**

Run: `./gradlew.bat runClient`

Expected: Living Land entity registrations, recipes, block atlas, particles,
and both renderers initialize without Living Land errors. Stop after successful
atlas/audio initialization.

- [ ] **Step 4: Inspect packaged jar**

Run:
`jar tf build/libs/mnagnosis-1.0.0.jar | Select-String 'living_land|LivingLand'`

Expected: component, terrain, conservation, controller, strike, renderers,
recipe, tag, icon, and classes are present.

- [ ] **Step 5: Audit status**

Run: `git diff --check; git status --short`

Expected: no Living Land changes remain uncommitted; only the preserved
user-owned armor texture state and `.codex-remote-attachments/` remain.

## Self-Review

- Spec coverage: Tasks 1–6 cover player contract, classification, protection,
  conservation, strike lifecycle, controller scheduling, balance, modes,
  persistence, visuals, registration, and verification.
- Placeholder scan: no TBD, TODO, “implement later,” or vague test steps remain.
- Type consistency: mode, scan result, reservation, settlement, entity
  configuration, and registry names match across all tasks.
