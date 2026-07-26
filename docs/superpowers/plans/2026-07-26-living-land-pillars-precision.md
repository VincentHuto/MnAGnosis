# Living Land Pillars and Precision Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace one-block Living Land strikes with conserved rigid pillars and add a Tier 6 Precision modifier that projects those pillars without changing terrain.

**Architecture:** `LivingLandPillarPayload` owns ordered physical or projected block entries and their atomic lifecycle. The controller assembles contiguous payloads, while the existing strike entity synchronizes, persists, renders, moves, and collides as a single multi-block body.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1, Mana and Artifice 3.1.11 spell API, vanilla entity/NBT/block rendering APIs, Forge GameTests.

## Global Constraints

- Preserve the existing Living Land entity and component registry IDs.
- Physical pillars contain three to five contiguous exact block states.
- Physical acquisition is atomic and every removed state settles exactly once.
- Precision projection never changes blocks, posts edit events, deposits states, or drops items.
- One pillar launches below Magnitude 2.0 and two at or above Magnitude 2.0.
- Maximum four active pillars per caster and dimension.
- Never force-load chunks.
- Preserve `src/main/resources/assets/mnagnosis/textures/models/armor/marrow_crown_layer_1.png` and `.codex-remote-attachments/`.

---

### Task 1: Bundled payload lifecycle

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/livingland/LivingLandPillarPayload.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/livingland/LivingLandConservation.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `LivingLandPillarPayload.Entry(BlockPos source, BlockState state)`
- Produces: `acquire(ServerLevel, ServerPlayer, List<BlockPos>, boolean): Optional<LivingLandPillarPayload>`
- Produces: `settle(ServerLevel, ServerPlayer, BlockPos, Vec3): boolean`
- Produces: `writeNbt(): CompoundTag`
- Produces: `readNbt(ServerLevel, CompoundTag): LivingLandPillarPayload`

- [ ] Write GameTests that request three adjacent states, verify complete
  physical removal, force the middle source invalid and verify rollback, and
  acquire projection while asserting all sources remain unchanged.
- [ ] Run `.\gradlew.bat compileJava` and verify compilation fails because
  `LivingLandPillarPayload` is absent.
- [ ] Implement ordered entries, physical rollback through restoration,
  projected snapshots, exact state NBT, idempotent grouped settlement, and
  emergency per-entry recovery.
- [ ] Run `.\gradlew.bat --stop` followed by
  `.\gradlew.bat runGameTestServer`; require every test to pass.
- [ ] Commit the exact task files with
  `feat: bundle conserved Living Land pillars`.

### Task 2: Multi-block strike and controller

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/LivingLandStrikeEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/LivingLandControllerEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/livingland/LivingLandTerrain.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Strike `configure` consumes one `LivingLandPillarPayload`.
- Strike exposes `getPayloadLength()`, `isProjected()`, and
  `getCarriedState(int index)`.
- Terrain produces contiguous source positions extending opposite the approach.

- [ ] Write GameTests for three and five synced states, complete NBT round
  trips, one/two-pillar Magnitude scaling, four-pillar cap, and collision
  against a non-central pillar segment.
- [ ] Run `.\gradlew.bat compileJava` and verify the tests fail against the
  one-block strike interface.
- [ ] Add five state accessors plus length/projection synced data, persist the
  complete payload, render/move around the center segment, and use swept AABBs
  per segment for one-time impact.
- [ ] Change controller waves to one or two pillars, select contiguous eligible
  sources, acquire atomically, and recover payloads when entity addition fails.
- [ ] Run the complete GameTest suite and require every test to pass.
- [ ] Commit the exact task files with
  `feat: launch rigid Living Land pillars`.

### Task 3: Precision modifier and projected presentation

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/PrecisionModifier.java`
- Create: `src/main/resources/data/mnagnosis/recipes/precision.json`
- Create: `src/main/resources/assets/mnagnosis/textures/spell/modifier/precision.png`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/SpellComponentRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentLivingLand.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/LivingLandControllerEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/LivingLandStrikeRenderer.java`
- Modify: `src/main/resources/assets/mnagnosis/lang/en_us.json`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `SpellComponentRegistry.PRECISION_ID`
- Produces: `SpellComponentRegistry.PRECISION`
- Produces: `SpellComponentRegistry.isPrecision(Modifier): boolean`
- Controller configuration consumes `boolean projected`.

- [ ] Write GameTests for registry identity, Tier 6 Ineffable crafting,
  recipe/icon resources, spell-context detection, projection retaining every
  source, and projected impact producing no deposit or item.
- [ ] Run GameTests and verify the new registration/resource assertions fail.
- [ ] Implement `PrecisionModifier` using the same progression gate as
  Polarity, register it, add its recipe/icon/text, and pass its presence from
  the component to the controller and payload acquisition.
- [ ] Render all payload blocks along the velocity axis and add a sparse
  monochrome lattice treatment only for projected payloads.
- [ ] Run all GameTests and require every test to pass.
- [ ] Commit the exact task files with
  `feat: project terrain with Precision`.

### Task 4: Full verification

**Files:**
- Modify only files required by a reproduced verification failure.

**Interfaces:**
- Consumes the complete bundled-pillar and Precision behavior.

- [ ] Run `.\gradlew.bat clean build` and require `BUILD SUCCESSFUL`.
- [ ] Run `.\gradlew.bat --stop` and
  `.\gradlew.bat runGameTestServer`; confirm the success marker in
  `run/logs/latest.log`.
- [ ] Start `runClient` hidden, confirm block atlas and sound initialization,
  then stop only the processes launched by the smoke test.
- [ ] Run
  `jar tf build/libs/mnagnosis-1.0.0.jar | Select-String 'Precision|LivingLand|precision|living_land'`
  and confirm all classes and data assets are packaged.
- [ ] Run `git diff --check` and `git status --short`; confirm only preserved
  user-owned paths remain outside committed work.

## Self-Review

- Spec coverage: payload conservation, projection, rendering, collision,
  balance, registration, persistence, packaging, and recovery each have a task.
- Placeholder scan: no deferred or ambiguous implementation steps remain.
- Type consistency: payload, strike accessors, controller projection flag, and
  modifier registry names are consistent across tasks.
