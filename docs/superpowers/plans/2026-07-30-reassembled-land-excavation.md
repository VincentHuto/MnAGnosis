# Reassembled Land Excavation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make downward Reassembled Land casts excavate every lattice pattern, conserve removed terrain in a compact spoil mound beside the mouth, and reverse the move at Duration expiry.

**Architecture:** `ReassembledPlanner` will produce downward excavation cells separately from construction targets. `ReassembledTransactionService.excavate` will select supported spoil positions and journal one-to-one moves from excavation cells to the mound through the existing receipt system. `ComponentReassembledLand` will choose excavation only for a downward look hitting solid terrain; ordinary construction remains unchanged.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.0, Mana and Artifice 3.1.11, JUnit 5, Forge GameTest.

## Global Constraints

- Work inline on the existing `master` checkout as explicitly requested.
- Preserve all unrelated dirty and untracked user work.
- Excavation and spoil endpoints are loaded-only, inside the world border, block-entity-free, fluid-free, and capped at 384 moves.
- The pile must not intersect the player, excavation volume, mouth, or entrance approach.
- Forward mutation is journaled and atomic; expiry uses the existing forced idempotent restoration.
- No excavated block may become an item drop.

---

### Task 1: Downward Excavation Geometry

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/reassembled/ReassembledPlanner.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/reassembled/ReassembledPlannerTest.java`

**Interfaces:**
- Consumes: `BlockPos mouth`, `Vec3 look`, `Direction casterFacing`, `ReassembledParameters`, and `ReassembledPattern`.
- Produces: `PlanResult planExcavation(BlockPos, Vec3, Direction, ReassembledParameters, ReassembledPattern)`.
- Produces: a `ReassembledPlan` whose `targets()` are cells to remove, not construction destinations.

- [ ] **Step 1: Write failing hand-checked geometry tests**

Add tests proving Wall descends from the mouth, Bridge follows downward look with two-cell clearance, Stair retains a descending floor profile while returning only its two headroom cells, and Pillar forms a downward shaft. Assert literal `BlockPos` lists for width/radius one fixtures.

- [ ] **Step 2: Run the focused planner test**

Run:
`.\gradlew.bat test --tests "*ReassembledPlannerTest" --console=plain`

Expected: compilation failure because `planExcavation` does not exist.

- [ ] **Step 3: Implement the minimal excavation planner**

Add:

```java
public PlanResult planExcavation(
        BlockPos mouth,
        Vec3 look,
        Direction casterFacing,
        ReassembledParameters parameters,
        ReassembledPattern pattern)
```

Use deterministic `LinkedHashSet` normalization and the existing `MAX_CELLS`.
Wall uses width by height below the mouth. Bridge voxelizes a pitched centerline
for `depth` and adds two clearance cells per width row. Stair derives total drop
from `height`, distributes it across `depth`, and adds `floor.above(1)` and
`floor.above(2)`. Pillar advances along normalized look for `height` and adds the
horizontal radius disk at each center.

- [ ] **Step 4: Run the focused planner test**

Expected: all `ReassembledPlannerTest` methods pass.

---

### Task 2: Supported Spoil Mound and Excavation Transaction

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/reassembled/ReassembledSpoilPlanner.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/reassembled/ReassembledTransactionService.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/reassembled/ReassembledSpoilPlannerTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/ReassembledLandGameTests.java`

**Interfaces:**
- Produces: `Optional<List<BlockPos>> ReassembledSpoilPlanner.select(ServerLevel, ServerPlayer, ReassembledPlan, List<BlockPos> sources, int range)`.
- Produces: `AssemblyResult ReassembledTransactionService.excavate(ServerLevel, ServerPlayer, ReassembledPlan, int range, long dueAt)`.
- Consumes: the exact-state `ReassembledMove`, receipt ledger, journal, rollback, protection, and expiry machinery already used by `assemble`.

- [ ] **Step 1: Write failing spoil and transaction tests**

Add a pure ordering/footprint test for a package-private mound-coordinate
generator and a GameTest that fills a controlled underground volume, excavates
it, and asserts: source cells are air, the same count of exact blocks appears in
the mound, the mouth/approach/player cells remain clear, no `ItemEntity` is
created, and a receipt exists.

- [ ] **Step 2: Run focused unit tests and GameTests**

Run:
`.\gradlew.bat test --tests "*ReassembledSpoilPlannerTest" --console=plain`

Then run:
`.\gradlew.bat runGameTestServer --no-daemon --console=plain`

Expected: RED because spoil selection and `excavate` do not exist.

- [ ] **Step 3: Implement deterministic spoil selection**

Generate a smallest-fitting stepped square mound. Try its center on the right
of the mouth, then left, then rear. Fill bottom-up. Resolve each base column to
a replaceable cell above full support; upper cells may use a previously selected
cell as support. Reject cells intersecting `caster.getBoundingBox()`, the plan,
the mouth approach, protected endpoints, unloaded chunks, block entities,
fluids, or the world border. Require every exact source state to survive at its
paired spoil cell.

- [ ] **Step 4: Implement the excavation transaction**

Preflight all excavation sources before opening a journal. Skip air/replaceable
cells; reject block entities, fluids, unbreakable states, falling blocks, and
non-full collision terrain. Build one-to-one moves from each source to its spoil
target, journal and flush, clear sources, place exact states in the mound, verify
all endpoints, then commit the receipt. On any failure call the existing
idempotent rollback and leave zero partial mutation.

- [ ] **Step 5: Run focused tests**

Expected: spoil unit tests and excavation GameTests pass.

---

### Task 3: Live Downward-Cast Routing

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentReassembledLand.java`
- Modify: `src/test/java/com/vincenthuto/mnagnosis/common/spell/ComponentReassembledLandTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/ReassembledLandGameTests.java`

**Interfaces:**
- Produces: package-private `static boolean excavationMode(Vec3 look, BlockState impactState)`.
- Uses: downward threshold `look.y < -0.05D`.
- Routes excavation to `planExcavation` and `excavate`; routes everything else through the existing `plan` and `assemble`.

- [ ] **Step 1: Write failing routing tests**

Assert downward plus solid is excavation; horizontal/upward or replaceable
impact is construction. Add GameTests invoking the real component with each
lattice pattern and a downward fake-player pitch, proving every pattern removes
at least one controlled underground block.

- [ ] **Step 2: Run focused tests**

Run:
`.\gradlew.bat test --tests "*ComponentReassembledLandTest" --console=plain`

Expected: RED because `excavationMode` does not exist.

- [ ] **Step 3: Implement server-authoritative routing**

Read `player.getLookAngle()` and the impacted `BlockState`. In excavation mode
use `target.getBlock()` as the mouth, plan excavation, and call `excavate`.
Otherwise retain the current exact construction code path and messages.

- [ ] **Step 4: Run focused tests and GameTests**

Expected: routing unit tests and all Reassembled Land GameTests pass.

---

### Task 4: Restoration, Atomic Failure, and Full Verification

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/ReassembledLandGameTests.java`
- Modify only if a regression requires it:
  `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/reassembled/ReassembledTransactionService.java`

**Interfaces:**
- Consumes: existing receipt expiry tick and `restoreExact`.
- Proves: excavation receipt reversal clears the mound, restores every source, and releases receipt capacity.

- [ ] **Step 1: Write failing end-to-end tests**

Add GameTests for Duration expiry and insufficient spoil space. The expiry test
must compare literal original source states and original empty spoil states
after return. The insufficient-space test must snapshot all controlled cells
and assert zero mutation and zero receipt creation.

- [ ] **Step 2: Run GameTests**

Expected: RED for any missing restoration or preflight behavior.

- [ ] **Step 3: Make the minimum correction required by the tests**

Keep restoration in the shared receipt path; do not add a second timer or a
second receipt format. Ensure all source and spoil writes use exact state tags
and idempotent verification.

- [ ] **Step 4: Run complete verification**

Run:

```powershell
.\gradlew.bat test --console=plain
.\gradlew.bat runGameTestServer --no-daemon --console=plain
.\gradlew.bat clean build --no-daemon --console=plain
git diff --check
```

Expected: unit tests and clean build pass; every Reassembled Land GameTest
passes. Any unrelated pre-existing GameTest failure must be named exactly.

- [ ] **Step 5: Review the focused implementation**

Review geometry, conservation, atomicity, receipt recovery, player/mouth
clearance, and ordinary construction non-regression. Fix all Critical and
Important findings, rerun the relevant tests, and produce the updated jar at
`build/libs/mnagnosis-1.2.0.jar`.
