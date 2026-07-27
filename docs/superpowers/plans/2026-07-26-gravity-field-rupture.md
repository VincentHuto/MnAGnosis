# Gravity Field Rupture Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make any two or more colliding Gravity Convergence cores collapse into one large, damaging, monochrome rippling explosion.

**Architecture:** Pure `GravityRuptureMath` owns collision and wave formulas. `GravityFieldEntity` discovers transitive collision clusters and spawns one synchronized `GravityRuptureEntity`; the rupture entity advances three wavefronts, applies server-authoritative shell damage/knockback, and emits client particle rings.

**Tech Stack:** Java 17, Forge 47.4.0, Minecraft 1.20.1 entity/synced-data/particle APIs, Forge GameTests, Gradle.

## Global Constraints

- Work inline in the current MnAGnosis repository.
- Preserve the user-modified `GravityFieldRenderer`, deleted armor texture, and `.codex-remote-attachments/`.
- Fixed, caster-bound, and target-bound fields use identical collision logic.
- Core collision uses `max(1.5, (firstRadius + secondRadius) * 0.18)`.
- One transitive collision cluster creates exactly one rupture and consumes every field in the cluster.
- Ruptures damage and knock back owners but never destroy blocks or create fire.
- Use test-driven development and commit only task-owned files.

---

### Task 1: Collision and Wave Math

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/gravity/GravityRuptureMath.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `collisionDistance(float, float)`, `maximumRadius(int)`,
  `waveRadius(int, int, float)`, `waveDamage(int, float, float)`, and
  `waveKnockback(int, float, float)`.
- Consumes: finite field radii, zero-based rupture age, zero-based wave index,
  maximum radius, and target distance.

- [ ] **Step 1: Write failing math tests**

Add a GameTest asserting:

```java
collisionDistance(5.0F, 5.0F) == 1.8F
collisionDistance(3.0F, 3.0F) == 1.5F
maximumRadius(2) == 10.0F
maximumRadius(6) == 18.0F
waveRadius(0, 0, 10.0F) == 0.0F
waveRadius(24, 0, 10.0F) == 10.0F
waveRadius(5, 1, 10.0F) < 0.0F
```

Also assert that first-wave damage and knockback exceed later waves, fall with
distance, and remain finite.

- [ ] **Step 2: Verify RED**

Run: `.\gradlew.bat compileJava --no-daemon --console=plain`

Expected: compilation fails because `GravityRuptureMath` is missing.

- [ ] **Step 3: Implement the formulas**

Use a 1.5-block minimum collision distance, `10 + 2 * (fieldCount - 2)` radius
capped at 18, six-tick wave spacing, twenty-four-tick travel time, and
distance-falloff multipliers. Return `-1` for a wave that has not started or has
finished.

- [ ] **Step 4: Verify GREEN**

Run: `.\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: every GameTest passes.

- [ ] **Step 5: Commit**

Commit only `GravityRuptureMath` and its GameTest changes.

### Task 2: Deterministic Field Collision Clusters

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/GravityFieldEntity.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/GravityRuptureEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Consumes: `GravityRuptureMath.collisionDistance` and
  `GravityRuptureMath.maximumRadius`.
- Produces: `GravityFieldEntity.tryCollapseCluster(ServerLevel)` and
  `GravityRuptureEntity.configure(Vec3, int)`.

- [ ] **Step 1: Write failing collision GameTests**

Construct configured fields for these cases:

```text
fixed + fixed at touching centers
target-bound + fixed after target movement
target-bound + caster-bound at touching entities
three-field transitive chain A-B-C
```

Tick the lowest-ID field after anchors update. Assert the pair cases consume two
fields and create one rupture. Assert the chain consumes all three and creates
one rupture. Include a separated pair that creates no rupture.

- [ ] **Step 2: Verify RED**

Run: `.\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: collision tests fail because fields do not collapse and the rupture
entity type is absent.

- [ ] **Step 3: Register and synchronize the rupture entity**

Register `gravity_rupture` as a `0.1 x 0.1` miscellaneous entity with tracking
range 24 and update interval 1. Synchronize maximum radius, field count, and
rupture age; implement NBT persistence and Forge spawn packets.

- [ ] **Step 4: Implement transitive collision resolution**

After anchor update, breadth-first search active `GravityFieldEntity` instances
using `collisionDistance`. Only the smallest ID in the connected cluster may
resolve it. Calculate the radius-weighted center, discard every member, add one
configured rupture, and return before normal gravity forces run.

- [ ] **Step 5: Verify GREEN**

Run: `.\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: all pair, cluster, and prior gravity tests pass.

- [ ] **Step 6: Commit**

Commit only the field entity, rupture entity, registry, and collision tests.

### Task 3: Expanding Damage Shells and Monochrome Ripple Visuals

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/GravityRuptureEntity.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/GravityRuptureRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Consumes: all `GravityRuptureMath` wave methods.
- Produces: three server-authoritative expanding shells, particle-ring client
  feedback, and registered no-op entity rendering.

- [ ] **Step 1: Write failing wave behavior and resource tests**

Spawn a rupture beside a zombie, item, projectile, and stone marker block. Tick
through a wave crossing. Assert the zombie loses health, every physical entity
gains outward velocity, and the marker block remains stone. Require
`GravityRuptureRenderer.class` and its registration in `ClientEvents.class`.

- [ ] **Step 2: Verify RED**

Run: `.\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: behavior and renderer contract tests fail.

- [ ] **Step 3: Implement server wave effects**

For three waves starting at ages 0, 6, and 12, find entities crossed between the
previous and current radius. Track a per-wave UUID hit set, apply explosion
damage to living entities, add normalized outward/upward knockback to living
entities/items/projectiles, and discard the controller after age 36.

- [ ] **Step 4: Implement client particles and sound**

At collapse play `GENERIC_EXPLODE` and `WARDEN_SONIC_BOOM`. On the client,
sample three orthogonal circles for each live wave using alternating
black/white concrete block particles; add reverse-portal center particles and
one sonic-boom/explosion burst at each wave start.

- [ ] **Step 5: Register the no-op renderer**

Register `EntityRegistry.GRAVITY_RUPTURE` with
`GravityRuptureRenderer::new` in `ClientEvents`.

- [ ] **Step 6: Verify GREEN and build**

Run:

```powershell
.\gradlew.bat runGameTestServer --no-daemon --console=plain
.\gradlew.bat build --no-daemon --console=plain
```

Expected: every required GameTest passes and the build reports
`BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit and audit**

Commit only rupture behavior, renderer, event registration, and tests. Confirm
`git status --short` still shows the user's pre-existing renderer/armor/
attachment changes and no other uncommitted feature files.

