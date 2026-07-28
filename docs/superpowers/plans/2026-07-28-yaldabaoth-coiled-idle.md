# Yaldabaoth Coiled Idle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make stationary Yaldabaoth hold a compact, reared coil and blend over 10 ticks into a low traveling-wave slither whenever GeckoLib reports locomotion.

**Architecture:** Keep the existing sibling-bone geometry and author the two silhouettes entirely through coordinated GeckoLib position and rotation tracks. Extend the shared encounter controller with overridable locomotion defaults so Sun and Moon keep their current behavior, while the main Yaldabaoth supplies a distinct movement loop and 10-tick transition.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1, GeckoLib 4, Gson, JUnit 5, Gradle

## Global Constraints

- Change the main Yaldabaoth entity only; Counterfeit Sun and Counterfeit Moon retain their existing animation identifiers and visible behavior.
- Preserve all existing bone names, geometry identifier, texture, entity registration, combat timer, and `animation.yaldabaoth.combat.roar_sweep`.
- Use `animation.yaldabaoth.idle` for the compact reared coil and `animation.yaldabaoth.move` for the low slither.
- Use exactly 10 controller transition ticks in the main Yaldabaoth.
- Do not change AI, navigation, attributes, movement speed, scale, hitbox, or textures.
- Keep the user's unrelated gravity-shift work unstaged and unmodified.

## File Map

- Modify `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothAssetContractTest.java`: describe the required coil, slither, and visible bounds as executable JSON contracts.
- Modify `src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth.animation.json`: author the compact idle and low movement loops while preserving the roar/sweep.
- Modify `src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth.geo.json`: enlarge the visible bounds for the raised head.
- Create `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothAnimationSelectionTest.java`: verify stationary and moving base-animation selection.
- Modify `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractYaldabaothEncounterEntity.java`: make the base controller movement-aware with backward-compatible defaults.
- Modify `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothEntity.java`: provide the movement loop and 10-tick blend.

---

### Task 1: Coiled and Slithering Asset Contracts

**Files:**
- Modify: `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothAssetContractTest.java`
- Modify: `src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth.animation.json`
- Modify: `src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth.geo.json`

**Interfaces:**
- Consumes: existing `json(String path)` asset loader.
- Produces: `animation.yaldabaoth.move`; idle and move position/rotation tracks; geometry bounds containing the raised pose.

- [ ] **Step 1: Add failing animation and bounds tests**

Add `assertNotEquals` to the static imports, then add these tests and helper to `YaldabaothAssetContractTest`:

```java
@Test
void yaldabaothIdleIsTallAndCompactWhileMovementIsLowAndExtended()
        throws IOException {
    JsonObject root = json("animations/entity/yaldabaoth.animation.json");
    JsonObject animations = root.getAsJsonObject("animations");
    assertTrue(animations.has("animation.yaldabaoth.move"));

    JsonObject idle = animations.getAsJsonObject("animation.yaldabaoth.idle");
    JsonObject move = animations.getAsJsonObject("animation.yaldabaoth.move");

    assertTrue(
            component(idle, "neck", "position", "0.0", 1) >= 24.0D,
            "Idle neck must rear above the coil"
    );
    assertTrue(
            component(idle, "terminal_sweep", "position", "0.0", 2) <= -180.0D,
            "Idle tail must return beneath the body to form a compact coil"
    );
    assertTrue(
            Math.abs(component(move, "neck", "position", "0.0", 1)) <= 2.0D,
            "Moving neck must return to the low stance"
    );
    assertTrue(
            Math.abs(component(move, "terminal_sweep", "position", "0.0", 2))
                    <= 2.0D,
            "Moving tail must extend behind the body"
    );

    double earlyWave =
            component(move, "segment_02", "rotation", "0.0", 1);
    double lateWave =
            component(move, "segment_07", "rotation", "0.0", 1);
    assertTrue(
            earlyWave * lateWave < 0.0D,
            "Movement loop must phase-shift its lateral wave down the body"
    );
    assertNotEquals(
            earlyWave,
            component(move, "segment_02", "rotation", "0.6", 1),
            0.0001D,
            "Movement loop must animate rather than hold a flat pose"
    );
}

@Test
void yaldabaothVisibleBoundsContainRaisedIdlePose() throws IOException {
    JsonObject description = json("geo/entity/yaldabaoth.geo.json")
            .getAsJsonArray("minecraft:geometry")
            .get(0).getAsJsonObject()
            .getAsJsonObject("description");

    assertTrue(description.get("visible_bounds_height").getAsDouble() >= 16.0D);
    assertTrue(
            description.getAsJsonArray("visible_bounds_offset")
                    .get(1).getAsDouble() >= 7.0D
    );
}

private static double component(
        JsonObject animation,
        String bone,
        String transform,
        String time,
        int component
) {
    return animation.getAsJsonObject("bones")
            .getAsJsonObject(bone)
            .getAsJsonObject(transform)
            .getAsJsonObject(time)
            .getAsJsonArray("vector")
            .get(component)
            .getAsDouble();
}
```

Also extend the main Yaldabaoth animation identifier expectation:

```java
Set.of(
        "animation.yaldabaoth.idle",
        "animation.yaldabaoth.move",
        "animation.yaldabaoth.combat.roar_sweep"
)
```

- [ ] **Step 2: Run the focused test and confirm the intended failure**

Run:

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothAssetContractTest"
```

Expected: FAIL because `animation.yaldabaoth.move` is absent and the existing visible bounds height is `9.0`.

- [ ] **Step 3: Author the compact idle pose**

Replace the current idle segment tracks with coordinated position and rotation tracks. At `0.0` and `4.0`, use the following base transforms; at `2.0`, retain the same large offsets while adding no more than 1.5 units of vertical breathing and 4 degrees of head/body drift.

| Bone | Position at 0.0 `[x,y,z]` | Rotation at 0.0 `[x,y,z]` |
|---|---:|---:|
| `neck` | `[0, 28, 8]` | `[-24, 0, 0]` |
| `head` | `[0, 1, -2]` | `[9, -2, 0]` |
| `segment_01` | `[0, 27, 4]` | `[-48, -4, 0]` |
| `segment_02` | `[-2, 20, -2]` | `[-38, -18, 2]` |
| `segment_03` | `[-7, 10, -8]` | `[-22, -42, 3]` |
| `segment_04` | `[3, -3, -19]` | `[0, -68, 2]` |
| `segment_05` | `[8, -4, -52]` | `[0, -112, 1]` |
| `segment_06` | `[11, -5, -96]` | `[0, -158, 0]` |
| `segment_07` | `[9, -5, -138]` | `[0, 166, -1]` |
| `segment_08` | `[-2, -5, -170]` | `[0, 124, -2]` |
| `segment_09` | `[-17, -5, -187]` | `[0, 80, -2]` |
| `segment_10` | `[-27, -5, -192]` | `[0, 36, -1]` |
| `tail` | `[-21, -5, -193]` | `[0, -4, 0]` |
| `terminal_sweep` | `[-3, -5, -201]` | `[0, -38, 0]` |

Preserve the current four-second restrained jaw and mane motion. Ensure every listed `position` and `rotation` track has matching `0.0` and `4.0` endpoints so the loop has no seam.

- [ ] **Step 4: Author the low traveling-wave movement loop**

Add `animation.yaldabaoth.move` with `"loop": true` and `"animation_length": 1.2`. Give `neck`, `head`, `segment_01` through `segment_10`, `tail`, and `terminal_sweep` explicit zero-or-near-zero position tracks at `0.0`, `0.6`, and `1.2`, returning all large idle translations to the extended modeled layout.

Use a lateral Y-rotation wave at the three keyframes:

| Bone | Y rotation at 0.0 | Y rotation at 0.6 | Y rotation at 1.2 |
|---|---:|---:|---:|
| `segment_01` | `14` | `-14` | `14` |
| `segment_02` | `22` | `-22` | `22` |
| `segment_03` | `18` | `-18` | `18` |
| `segment_04` | `6` | `-6` | `6` |
| `segment_05` | `-10` | `10` | `-10` |
| `segment_06` | `-22` | `22` | `-22` |
| `segment_07` | `-18` | `18` | `-18` |
| `segment_08` | `-4` | `4` | `-4` |
| `segment_09` | `12` | `-12` | `12` |
| `segment_10` | `24` | `-24` | `24` |
| `tail` | `18` | `-18` | `18` |
| `terminal_sweep` | `6` | `-6` | `6` |

Keep the neck between `-7` and `-3` degrees on X, the head between `1` and `4` degrees on X, and add subtle opposite Y counter-motion to both. Add a restrained mane sway that closes at `1.2`.

- [ ] **Step 5: Expand the geometry bounds**

Change the geometry description to:

```json
"visible_bounds_width": 22.0,
"visible_bounds_height": 18.0,
"visible_bounds_offset": [0, 8.0, 4.0]
```

Width remains unchanged because the coil stays inside the existing broad boss footprint; the higher vertical center contains the raised head.

- [ ] **Step 6: Run the focused test and confirm it passes**

Run:

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothAssetContractTest"
```

Expected: PASS with all Yaldabaoth geometry, animation, texture, and localization contracts intact.

- [ ] **Step 7: Commit the asset deliverable**

```powershell
git add -- src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothAssetContractTest.java src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth.animation.json src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth.geo.json
git commit -m "feat: add Yaldabaoth coiled and slithering poses"
```

---

### Task 2: Movement-Aware GeckoLib Controller

**Files:**
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothAnimationSelectionTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractYaldabaothEncounterEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothEntity.java`

**Interfaces:**
- Consumes: `animation.yaldabaoth.move` from Task 1 and GeckoLib `AnimationState.isMoving()`.
- Produces: package-private `selectBaseAnimation(boolean, RawAnimation, RawAnimation)`; protected `movementAnimation()` and `baseAnimationTransitionTicks()` defaults; main-entity `MOVEMENT` animation and 10-tick override.

- [ ] **Step 1: Write the failing selection test**

Create `YaldabaothAnimationSelectionTest.java`:

```java
package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import org.junit.jupiter.api.Test;
import software.bernie.geckolib.core.animation.RawAnimation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class YaldabaothAnimationSelectionTest {

    @Test
    void stationaryUsesIdleAndMovementUsesSlither() {
        RawAnimation idle =
                RawAnimation.begin().thenLoop("animation.yaldabaoth.idle");
        RawAnimation movement =
                RawAnimation.begin().thenLoop("animation.yaldabaoth.move");

        assertSame(
                idle,
                AbstractYaldabaothEncounterEntity.selectBaseAnimation(
                        false,
                        idle,
                        movement
                )
        );
        assertSame(
                movement,
                AbstractYaldabaothEncounterEntity.selectBaseAnimation(
                        true,
                        idle,
                        movement
                )
        );
    }

    @Test
    void mainYaldabaothUsesTenTickPoseBlend() {
        assertEquals(10, YaldabaothEntity.BASE_ANIMATION_TRANSITION_TICKS);
    }
}
```

- [ ] **Step 2: Run the focused test and confirm the intended compile failure**

Run:

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothAnimationSelectionTest"
```

Expected: FAIL compilation because `selectBaseAnimation` and `BASE_ANIMATION_TRANSITION_TICKS` do not exist.

- [ ] **Step 3: Add backward-compatible controller hooks**

In `AbstractYaldabaothEncounterEntity`, add:

```java
static RawAnimation selectBaseAnimation(
        boolean moving,
        RawAnimation idle,
        RawAnimation movement
) {
    return moving ? movement : idle;
}

protected RawAnimation movementAnimation() {
    return this.idleAnimation();
}

protected int baseAnimationTransitionTicks() {
    return 2;
}
```

Replace the existing `idle_controller` registration with:

```java
controllers.add(new AnimationController<>(
        this,
        "base_controller",
        this.baseAnimationTransitionTicks(),
        state -> {
            state.setAnimation(selectBaseAnimation(
                    state.isMoving(),
                    this.idleAnimation(),
                    this.movementAnimation()
            ));
            return PlayState.CONTINUE;
        }
));
```

Leave `combat_controller` unchanged.

- [ ] **Step 4: Supply the main movement animation and transition**

In `YaldabaothEntity`, add:

```java
public static final int BASE_ANIMATION_TRANSITION_TICKS = 10;

private static final RawAnimation MOVEMENT =
        RawAnimation.begin().thenLoop("animation.yaldabaoth.move");
```

Add these overrides without changing the existing idle or combat methods:

```java
@Override
protected RawAnimation movementAnimation() {
    return MOVEMENT;
}

@Override
protected int baseAnimationTransitionTicks() {
    return BASE_ANIMATION_TRANSITION_TICKS;
}
```

- [ ] **Step 5: Run both focused tests**

Run:

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothAnimationSelectionTest" --tests "com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothAssetContractTest"
```

Expected: PASS. The selector returns the exact requested loop, the main transition is 10 ticks, and the asset identifiers match Java.

- [ ] **Step 6: Commit the controller deliverable**

```powershell
git add -- src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothAnimationSelectionTest.java src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractYaldabaothEncounterEntity.java src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothEntity.java
git commit -m "feat: switch Yaldabaoth pose while moving"
```

---

### Task 3: Integrated Verification and Visual Acceptance

**Files:**
- Verify only; correct Task 1 or Task 2 files if a check exposes a defect.

**Interfaces:**
- Consumes: completed asset and controller tasks.
- Produces: a buildable mod whose automated contracts pass and a documented visual-QA status.

- [ ] **Step 1: Check JSON and patch hygiene**

Run:

```powershell
Get-Content -Raw src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth.animation.json | ConvertFrom-Json | Out-Null
Get-Content -Raw src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth.geo.json | ConvertFrom-Json | Out-Null
git diff --check HEAD~2..HEAD
```

Expected: all commands exit successfully with no JSON parse or whitespace errors.

- [ ] **Step 2: Run all unit tests**

Run:

```powershell
.\gradlew.bat test
```

Expected: BUILD SUCCESSFUL with zero failed tests.

- [ ] **Step 3: Compile the complete mod**

Run:

```powershell
.\gradlew.bat build
```

Expected: BUILD SUCCESSFUL, including Java compilation, resource processing, tests, jar creation, and reobfuscation.

- [ ] **Step 4: Inspect the final scoped diff**

Run:

```powershell
git status --short
git diff HEAD~2..HEAD -- src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth.animation.json src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth.geo.json src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth
```

Expected: only the planned Yaldabaoth files appear in the two implementation commits; pre-existing gravity files remain outside the scoped diff.

- [ ] **Step 5: Perform visual acceptance when a render-capable client is available**

Spawn Yaldabaoth in the development client and verify all four observations:

1. Stationary silhouette is a compact ground coil with the front third and lion head raised.
2. Movement silhouette is low and extended with a traveling lateral wave.
3. Starting and stopping blend over 10 ticks without snapping or visible gaps between adjacent segments.
4. The head remains visible without frustum-culling artifacts at the top of the idle pose.

If the environment cannot provide an interactive Minecraft client, report this item explicitly as pending manual visual acceptance rather than claiming it passed.
