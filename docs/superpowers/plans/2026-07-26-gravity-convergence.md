# Gravity Convergence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a persistent, shape-dependent Ineffable gravity field whose attractive behavior is reversed by a dedicated Polarity modifier.

**Architecture:** `ComponentGravityConvergence` translates M&A spell targets and attributes into a persistent `GravityFieldEntity`. The entity owns all server-side targeting and bounded force math, while a lightweight client renderer and particles provide the monochrome lattice feedback.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1, Mana and Artifice spell API, Forge registries, vanilla synced entity data/NBT, Forge GameTests.

## Global Constraints

- Implement Gravity Convergence only; Living Land, Reassembled Land, and True Self are separate follow-up projects.
- The component and Polarity modifier require Tier 6 Ineffable progression.
- Maximum three active fields per caster per dimension; a fourth discards the oldest.
- Maximum acceleration is `0.12` blocks/tick² and maximum velocity is `1.50` blocks/tick.
- The field does not move blocks, force-load chunks, or deal direct damage.
- Preserve the existing user-owned staged deletion of `src/main/resources/assets/mnagnosis/textures/models/armor/marrow_crown_layer_1.png`.

---

### Task 1: Pure gravity rules and force math

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/gravity/GravityPolarity.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/gravity/GravityFieldMath.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `GravityPolarity { ATTRACT, REPEL }`
- Produces: `GravityFieldMath.acceleration(Vec3 offsetFromCenter, double radius, double magnitude, double response, GravityPolarity polarity, Vec3 currentVelocity): Vec3`
- Produces: `GravityFieldMath.clampVelocity(Vec3 velocity): Vec3`

- [ ] **Step 1: Write failing GameTests**

Add tests that assert attraction points toward the center, repulsion points
away, attraction damps inside the `0.85` capture shell, repulsion fades in the
outer 20%, exact-center input remains finite, acceleration never exceeds
`0.12`, and velocity never exceeds `1.50`.

- [ ] **Step 2: Run the focused GameTest server and verify RED**

Run: `./gradlew.bat runGameTestServer --tests '*gravity*'`

Expected: compilation fails because `GravityFieldMath` and `GravityPolarity`
do not exist.

- [ ] **Step 3: Implement the minimum pure math**

Normalize only nonzero vectors, calculate a distance-sensitive response,
special-case the attractive capture shell with damping, apply outer-band
repulsion falloff, then independently clamp acceleration and final velocity.

- [ ] **Step 4: Run the focused tests and verify GREEN**

Run: `./gradlew.bat runGameTestServer`

Expected: all existing tests and new force-math tests pass.

- [ ] **Step 5: Commit**

Commit only the two gravity math files and the exact GameTest hunks with:
`feat: define bounded gravity field physics`.

### Task 2: Persistent field entity

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/GravityFieldEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `GravityFieldEntity.configure(Entity owner, GravityAnchorMode mode, Entity trackedTarget, Vec3 fixedPosition, GravityPolarity polarity, float radius, int durationTicks, float magnitude, float response): void`
- Produces: `GravityFieldEntity.GravityAnchorMode { FIXED, CASTER, TARGET }`
- Consumes: `GravityFieldMath.acceleration(...)`

- [ ] **Step 1: Write failing entity GameTests**

Add tests for NBT round-trip, fixed/caster/target anchors, invalid moving-anchor
expiry, duration expiry, hostile/item/projectile inclusion, protection
exclusions, no health loss, and the three-field ownership cap helper.

- [ ] **Step 2: Run GameTests and verify RED**

Run: `./gradlew.bat runGameTestServer`

Expected: compilation fails because `GravityFieldEntity` and
`EntityRegistry.GRAVITY_FIELD` do not exist.

- [ ] **Step 3: Register and implement the field entity**

Use synced entity data for visual state, NBT for persistence, a spherical AABB
query for candidate entities, centralized ownership/alliance filtering, and
server-only force application. Set `hasImpulse` after velocity changes. Clamp
all loaded and configured numeric values to spec bounds.

- [ ] **Step 4: Run GameTests and verify GREEN**

Run: `./gradlew.bat runGameTestServer`

Expected: every field lifecycle and physics integration test passes.

- [ ] **Step 5: Commit**

Commit the entity, registry change, and exact GameTest hunks with:
`feat: add persistent gravity fields`.

### Task 3: Spell component and Polarity modifier

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentGravityConvergence.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/PolarityModifier.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/SpellComponentRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `SpellComponentRegistry.GRAVITY_CONVERGENCE_ID`
- Produces: `SpellComponentRegistry.GRAVITY_CONVERGENCE`
- Produces: `SpellComponentRegistry.POLARITY_ID`
- Produces: `SpellComponentRegistry.POLARITY`
- Consumes: `GravityFieldEntity.configure(...)`

- [ ] **Step 1: Write failing spell integration GameTests**

Assert registry identity, four attribute ranges, faction requirement, Tier 6
craftability, default attraction, Polarity repulsion, self/entity/block anchor
selection, unsupported-target failure, and fourth-cast oldest replacement.

- [ ] **Step 2: Run GameTests and verify RED**

Run: `./gradlew.bat runGameTestServer`

Expected: compilation fails because the component, modifier, and registry
constants do not exist.

- [ ] **Step 3: Implement the component and modifier**

Create four `AttributeValuePair` declarations for Radius, Duration, Magnitude,
and Speed. In `ApplyEffect`, validate the caster/target, derive polarity from
`context.getSpell().getModifiers()`, map the final target to an anchor, evict
the owner's oldest fourth field, configure the new entity, and add it to the
server level. Give Polarity the same strict Tier 6 Ineffable craftability rule
as the component.

- [ ] **Step 4: Run GameTests and verify GREEN**

Run: `./gradlew.bat runGameTestServer`

Expected: spell integration tests and the existing suite pass.

- [ ] **Step 5: Commit**

Commit the component, modifier, registry changes, and exact GameTest hunks with:
`feat: add Gravity Convergence spell parts`.

### Task 4: Recipes, localization, icons, and client field feedback

**Files:**
- Create: `src/main/resources/data/mnagnosis/recipes/components/gravity_convergence.json`
- Create: `src/main/resources/data/mnagnosis/recipes/polarity.json`
- Create: `src/main/resources/assets/mnagnosis/textures/spell/component/gravity_convergence.png`
- Create: `src/main/resources/assets/mnagnosis/textures/spell/modifier/polarity.png`
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/GravityFieldRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java`
- Modify: `src/main/resources/assets/mnagnosis/lang/en_us.json`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: an entity renderer registration for `EntityRegistry.GRAVITY_FIELD`
- Consumes: synced radius and polarity accessors on `GravityFieldEntity`

- [ ] **Step 1: Write failing packaging GameTests**

Assert both recipe JSON files exist, resolve to the registered parts at Tier 6,
both icons exist, localization keys exist, and the gravity entity has a client
renderer registration smoke path.

- [ ] **Step 2: Run GameTests and verify RED**

Run: `./gradlew.bat runGameTestServer`

Expected: packaging assertions fail for missing recipes, icons, and language
entries.

- [ ] **Step 3: Add data and visual feedback**

Add exact Tier 6 recipes from the spec. Add 16×16 transparent monochrome
lattice icons. Register a textureless renderer which emits sparse alternating
black/white block particles along deterministic lattice sample points, with
motion reversed for Polarity and density bounded by particle settings.

- [ ] **Step 4: Run GameTests and verify GREEN**

Run: `./gradlew.bat runGameTestServer`

Expected: packaging tests and the full suite pass.

- [ ] **Step 5: Commit**

Commit exact task files with:
`feat: present Gravity Convergence in game`.

### Task 5: Full verification

**Files:**
- Modify only files required to correct failures proven by a new or existing test.

**Interfaces:**
- Consumes: the complete Gravity Convergence feature.
- Produces: verified distributable behavior.

- [ ] **Step 1: Run a clean build**

Run: `./gradlew.bat clean build`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run the complete GameTest server**

Run: `./gradlew.bat runGameTestServer`

Expected: all tests pass and the server exits successfully.

- [ ] **Step 3: Run client startup smoke verification**

Run: `./gradlew.bat runClient`

Expected: registries, recipes, atlases, and entity renderers initialize without
MnAGnosis errors. Stop after the title screen or equivalent successful client
initialization marker.

- [ ] **Step 4: Inspect the packaged jar**

Run: `jar tf build/libs/mnagnosis-1.0.0.jar | Select-String 'gravity_convergence|polarity|GravityField'`

Expected: the component, modifier, entity, recipes, icons, and classes are
present.

- [ ] **Step 5: Commit only test-proven corrections if needed**

If verification required a correction, commit it with a focused message.
Otherwise do not create an empty commit.

## Self-Review

- Spec coverage: every player contract, architecture, balance, protection,
  visual, registration, lifecycle, and verification requirement maps to Tasks
  1–5.
- Placeholder scan: no TBD/TODO/“implement later” steps remain.
- Type consistency: the gravity polarity, force math, entity configuration,
  registry IDs, and renderer dependencies use the same names across tasks.
