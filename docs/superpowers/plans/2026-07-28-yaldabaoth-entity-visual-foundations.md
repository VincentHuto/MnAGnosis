# Yaldabaoth Entity Visual Foundations Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add spawnable, damageable GeckoLib foundations for Yaldabaoth, the white Sun, and the black crescent Moon, with finished models, pixel textures, synchronized presentation state, idle animations, and one non-damaging combat sample each.

**Architecture:** A common no-AI GeckoLib living-entity base owns damage-triggered animation timing and persistence. Yaldabaoth has its own concrete entity, while the Sun and Moon share an abstract celestial layer that adds synchronized allegiance. Client models, renderers, and registration remain isolated under client packages; all encounter mechanics beyond presentation are excluded.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.0, GeckoLib 4, JUnit 5.10.2, Forge GameTest, Bedrock geometry/animation JSON, PNG pixel textures.

## Global Constraints

- Implement exactly three new entity types: `mnagnosis:yaldabaoth`, `mnagnosis:yaldabaoth_sun`, and `mnagnosis:yaldabaoth_moon`.
- Yaldabaoth targets a 14–18 block full-model length and a 4–5 block lion head.
- Texture sizes are exactly 128×128 for Yaldabaoth and 64×64 for each celestial.
- Yaldabaoth uses muted gold, aged bone, ember, and storm-dark colors.
- The Sun is a white modeled disc with a black modeled rim; the Moon is a black modeled crescent with a white modeled rim.
- Every entity is damageable, persistent, gravity-free, and has no goals, navigation behavior, target selection, loot, or autonomous attacks.
- Every entity has one looping idle animation and one non-looping sample combat animation.
- A valid damage event triggers the sample combat animation; later server code can call the same public trigger.
- Celestial allegiance is exactly `HOSTILE`, `DORMANT`, or `WITNESS`, with invalid saved values falling back to `HOSTILE`.
- Do not add spawn eggs, summon items, boss bars, sounds, subtitles, multipart server hitboxes, controller ownership, encounter UUIDs, stagger, anti-focus, beams, projectile erasure, floor omission, projection shaders, Claims, authored actions, or encounter AI.
- Preserve all unrelated uncommitted work. Existing modified files receive only narrow additive edits.

---

## File Structure

### Common entity state

- `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CombatAnimationTimer.java` — pure timer rules used by entity synchronization and unit tests.
- `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialAllegiance.java` — stable serialized celestial state.
- `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractYaldabaothEncounterEntity.java` — shared living-entity, GeckoLib, persistence, no-AI, and combat-trigger behavior.
- `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothEntity.java` — boss-specific health, dimensions, and animations.
- `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractCelestialEntity.java` — synced allegiance and shared celestial attributes.
- `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothSunEntity.java` — Sun animation identity.
- `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothMoonEntity.java` — Moon animation identity.
- `src/main/java/com/vincenthuto/mnagnosis/common/event/YaldabaothEntityEvents.java` — common-side attribute registration.
- `src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java` — three narrow deferred-register entries.

### Client presentation

- `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/yaldabaoth/YaldabaothModel.java`
- `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/yaldabaoth/YaldabaothSunModel.java`
- `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/yaldabaoth/YaldabaothMoonModel.java`
- `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/yaldabaoth/YaldabaothRenderer.java`
- `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/yaldabaoth/YaldabaothSunRenderer.java`
- `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/yaldabaoth/YaldabaothMoonRenderer.java`
- `src/main/java/com/vincenthuto/mnagnosis/client/event/YaldabaothClientEvents.java` — isolated client renderer registration.

### Assets

- `src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth.geo.json`
- `src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth_sun.geo.json`
- `src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth_moon.geo.json`
- `src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth.animation.json`
- `src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth_sun.animation.json`
- `src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth_moon.animation.json`
- `src/main/resources/assets/mnagnosis/textures/entity/yaldabaoth/yaldabaoth.png`
- `src/main/resources/assets/mnagnosis/textures/entity/yaldabaoth/yaldabaoth_sun.png`
- `src/main/resources/assets/mnagnosis/textures/entity/yaldabaoth/yaldabaoth_moon.png`
- `src/main/resources/assets/mnagnosis/lang/en_us.json` — three entity display names.

### Tests

- `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CombatAnimationTimerTest.java`
- `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialAllegianceTest.java`
- `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothAssetContractTest.java`
- `src/main/java/com/vincenthuto/mnagnosis/gametest/YaldabaothEntityGameTests.java`

---

### Task 1: Stable Presentation-State Primitives

**Files:**
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CombatAnimationTimerTest.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialAllegianceTest.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CombatAnimationTimer.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialAllegiance.java`

**Interfaces:**
- Produces: `CombatAnimationTimer.trigger(int duration)`, `tick(int remaining)`, `clampLoaded(int saved, int duration)`, and `isActive(int remaining)`.
- Produces: `CelestialAllegiance.serializedName()` and `CelestialAllegiance.fromSerializedName(String)`.

- [ ] **Step 1: Write failing timer tests**

```java
package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatAnimationTimerTest {
    @Test
    void triggerStartsAtTheRequestedDuration() {
        assertEquals(36, CombatAnimationTimer.trigger(36));
    }

    @Test
    void tickCountsDownWithoutBecomingNegative() {
        assertEquals(2, CombatAnimationTimer.tick(3));
        assertEquals(0, CombatAnimationTimer.tick(0));
        assertEquals(0, CombatAnimationTimer.tick(-4));
    }

    @Test
    void loadedValuesAreClampedToTheEntitiesDuration() {
        assertEquals(0, CombatAnimationTimer.clampLoaded(-1, 36));
        assertEquals(18, CombatAnimationTimer.clampLoaded(18, 36));
        assertEquals(36, CombatAnimationTimer.clampLoaded(200, 36));
    }

    @Test
    void onlyPositiveTimeIsActive() {
        assertTrue(CombatAnimationTimer.isActive(1));
        assertFalse(CombatAnimationTimer.isActive(0));
    }
}
```

- [ ] **Step 2: Run the focused timer test and verify RED**

Run: `./gradlew.bat test --tests "*CombatAnimationTimerTest"`

Expected: FAIL because `CombatAnimationTimer` does not exist.

- [ ] **Step 3: Implement the minimal timer**

```java
package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import net.minecraft.util.Mth;

public final class CombatAnimationTimer {
    private CombatAnimationTimer() {
    }

    public static int trigger(int duration) {
        return Math.max(1, duration);
    }

    public static int tick(int remaining) {
        return Math.max(0, remaining - 1);
    }

    public static int clampLoaded(int saved, int duration) {
        return Mth.clamp(saved, 0, Math.max(1, duration));
    }

    public static boolean isActive(int remaining) {
        return remaining > 0;
    }
}
```

- [ ] **Step 4: Run the focused timer test and verify GREEN**

Run: `./gradlew.bat test --tests "*CombatAnimationTimerTest"`

Expected: PASS.

- [ ] **Step 5: Write failing allegiance tests**

```java
package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CelestialAllegianceTest {
    @Test
    void everyAllegianceRoundTripsByStableName() {
        for (CelestialAllegiance allegiance : CelestialAllegiance.values()) {
            assertEquals(
                    allegiance,
                    CelestialAllegiance.fromSerializedName(allegiance.serializedName())
            );
        }
    }

    @Test
    void unknownOrMissingNamesFallBackToHostile() {
        assertEquals(CelestialAllegiance.HOSTILE,
                CelestialAllegiance.fromSerializedName("future_state"));
        assertEquals(CelestialAllegiance.HOSTILE,
                CelestialAllegiance.fromSerializedName(""));
        assertEquals(CelestialAllegiance.HOSTILE,
                CelestialAllegiance.fromSerializedName(null));
    }
}
```

- [ ] **Step 6: Run the focused allegiance test and verify RED**

Run: `./gradlew.bat test --tests "*CelestialAllegianceTest"`

Expected: FAIL because `CelestialAllegiance` does not exist.

- [ ] **Step 7: Implement the allegiance enum**

```java
package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import java.util.Locale;

public enum CelestialAllegiance {
    HOSTILE,
    DORMANT,
    WITNESS;

    public String serializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static CelestialAllegiance fromSerializedName(String name) {
        if (name == null) {
            return HOSTILE;
        }
        for (CelestialAllegiance value : values()) {
            if (value.serializedName().equals(name)) {
                return value;
            }
        }
        return HOSTILE;
    }
}
```

- [ ] **Step 8: Run both focused tests and commit**

Run: `./gradlew.bat test --tests "*CombatAnimationTimerTest" --tests "*CelestialAllegianceTest"`

Expected: PASS.

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth
git commit -m "feat: add Yaldabaoth presentation state primitives"
```

---

### Task 2: Damageable No-AI Entity Foundations

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractYaldabaothEncounterEntity.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothEntity.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractCelestialEntity.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothSunEntity.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothMoonEntity.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/event/YaldabaothEntityEvents.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/gametest/YaldabaothEntityGameTests.java`

**Interfaces:**
- Consumes: `CombatAnimationTimer` and `CelestialAllegiance`.
- Produces: three `RegistryObject<EntityType<...>>` fields named `YALDABAOTH`, `YALDABAOTH_SUN`, and `YALDABAOTH_MOON`.
- Produces: `triggerCombatAnimation()`, `isCombatAnimationActive()`, and `getCombatAnimationTicks()`.
- Produces: `AbstractCelestialEntity#getAllegiance()` and `setAllegiance(CelestialAllegiance)`.

- [ ] **Step 1: Add registry-first GameTests without referencing missing concrete classes**

Create tests that look up the three IDs through `ForgeRegistries.ENTITY_TYPES`,
fail when an ID is absent, instantiate each generic type, add it to the test
level, and assert that it is a `LivingEntity`, has gravity disabled, has no
target, and accepts ordinary player damage. A second test saves Sun and Moon to
`CompoundTag`, reloads them through `EntityType.loadEntityRecursive`, and
asserts that the saved allegiance and combat timer survive.

Use registry-string lookup in the initial RED version so the test source
compiles before the concrete entity classes exist:

```java
private static EntityType<?> requireType(GameTestHelper helper, String path) {
    EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(MnAGnosis.rloc(path));
    helper.assertTrue(type != null, "Missing entity type mnagnosis:" + path);
    return type;
}
```

- [ ] **Step 2: Run the GameTest server and verify RED**

Run: `./gradlew.bat runGameTestServer`

Expected: FAIL in `YaldabaothEntityGameTests` because all three registry IDs
are absent.

- [ ] **Step 3: Implement the shared entity base**

`AbstractYaldabaothEncounterEntity` extends `PathfinderMob` and implements
`GeoEntity`. It must:

- Define a synchronized integer `COMBAT_ANIMATION_TICKS`.
- Set `setNoGravity(true)` and `setPersistenceRequired()` in its constructor.
- Register no goals by leaving `registerGoals()` empty.
- Tick the synchronized timer on the logical server.
- Trigger the timer and GeckoLib triggerable animation after a successful
  `hurt` call.
- Save/load `CombatAnimationTicks` through `CombatAnimationTimer.clampLoaded`.
- Return `false` from `removeWhenFarAway`.
- Return an empty default loot table.
- Register an idle controller plus a `"combat_controller"` containing
  triggerable animation key `"combat"`.
- Expose:

```java
public final void triggerCombatAnimation()
public final boolean isCombatAnimationActive()
public final int getCombatAnimationTicks()
protected abstract int combatAnimationDuration()
protected abstract RawAnimation idleAnimation()
protected abstract RawAnimation combatAnimation()
```

Use `triggerAnim("combat_controller", "combat")` only on the logical server.

- [ ] **Step 4: Implement the three concrete entities**

`YaldabaothEntity` uses 600 maximum health, 12 armor, 1.0 knockback resistance,
a 4.5×4.5 registration hitbox, a 36-tick sample duration, and animation names
`animation.yaldabaoth.idle` and `animation.yaldabaoth.combat.roar_sweep`.

`AbstractCelestialEntity` defines synchronized allegiance as an integer,
converts invalid ordinals to `HOSTILE`, and saves the stable string under
`Allegiance`.

Both celestials use 120 maximum health, 4 armor, 1.0 knockback resistance, a
3.5×3.5 registration hitbox, and a 24-tick sample duration. Their animation
names are:

```text
animation.yaldabaoth_sun.idle
animation.yaldabaoth_sun.combat.judgment
animation.yaldabaoth_moon.idle
animation.yaldabaoth_moon.combat.omission_slash
```

- [ ] **Step 5: Register entity types and attributes**

Add the three registry objects to `EntityRegistry` using `MobCategory.MISC`,
tracking range 32, update interval 1, and the dimensions above. Add
`YaldabaothEntityEvents` as a MOD-bus subscriber and handle
`EntityAttributeCreationEvent`:

```java
event.put(EntityRegistry.YALDABAOTH.get(), YaldabaothEntity.createAttributes().build());
event.put(EntityRegistry.YALDABAOTH_SUN.get(), AbstractCelestialEntity.createAttributes().build());
event.put(EntityRegistry.YALDABAOTH_MOON.get(), AbstractCelestialEntity.createAttributes().build());
```

- [ ] **Step 6: Run GameTests and verify GREEN**

Run: `./gradlew.bat runGameTestServer`

Expected: all Yaldabaoth entity foundation tests PASS, including registry,
spawn, damage, no-gravity/no-target, and NBT round trips.

- [ ] **Step 7: Run focused unit tests and commit**

Run: `./gradlew.bat test --tests "*yaldabaoth*"`

Expected: PASS.

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth src/main/java/com/vincenthuto/mnagnosis/common/event/YaldabaothEntityEvents.java src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java src/main/java/com/vincenthuto/mnagnosis/gametest/YaldabaothEntityGameTests.java
git commit -m "feat: add Yaldabaoth encounter entity foundations"
```

---

### Task 3: GeckoLib Geometry, Animation, and Texture Assets

**Files:**
- Create: all nine asset files listed in the File Structure section.
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothAssetContractTest.java`

**Interfaces:**
- Consumes: exact animation identifiers from Task 2.
- Produces: stable Yaldabaoth bones `root`, `body`, `neck`, `head`, `jaw`,
  `mane`, `crown`, `segment_01` through `segment_10`, `tail`, and
  `terminal_sweep`.
- Produces: stable Sun bones `root`, `disc`, `outline`, and `flare`.
- Produces: stable Moon bones `root`, `crescent`, `outline`, and `cut`.

- [ ] **Step 1: Write the failing asset contract test**

The JUnit test must:

- Load all six JSON resources with Gson and fail clearly if any is missing.
- Assert Bedrock geometry identifiers:
  `geometry.mnagnosis.yaldabaoth`,
  `geometry.mnagnosis.yaldabaoth_sun`, and
  `geometry.mnagnosis.yaldabaoth_moon`.
- Collect bone names and assert every required stable bone listed above.
- Assert all six animation identifiers from Task 2.
- Load all three PNGs through `ImageIO.read`.
- Assert exact dimensions 128×128, 64×64, and 64×64.
- Sample known outline/body atlas regions and assert the Sun contains both
  near-white and near-black pixels, the Moon contains both near-white and
  near-black pixels, and Yaldabaoth contains opaque gold, bone, ember, and
  storm-dark swatches.

- [ ] **Step 2: Run the asset test and verify RED**

Run: `./gradlew.bat test --tests "*YaldabaothAssetContractTest"`

Expected: FAIL with the first missing geometry resource.

- [ ] **Step 3: Create the Yaldabaoth geometry**

Author `yaldabaoth.geo.json` as an articulated 16-block S-curve. Use overlapping
rotated cuboids so adjacent segments read as one serpent. The head must be
approximately 64–80 model units wide, with separate jaw, cheek, muzzle, brow,
eye, corona-mane, and crown geometry. Segment pivots must form a parented chain
from neck through `terminal_sweep`, allowing one animation wave to travel the
entire body. Set visible bounds large enough that the tail is not culled when
the parent hitbox is off-screen.

- [ ] **Step 4: Create the celestial geometry**

The Sun uses shallow front/back disc layers plus a separately modeled black
rim and recessed flare core. The Moon uses a block-built crescent silhouette
with a separately modeled white outer edge and narrower interior cut.
Give both non-zero thickness and symmetrical front/back faces so neither
vanishes at oblique angles.

- [ ] **Step 5: Create the pixel textures using the imagegen skill**

Read and follow the `imagegen` skill. Generate texture-atlas source art from
the approved historical/canonical palette, then constrain it to the exact UV
layouts and dimensions required by the geometry. Preserve crisp nearest-neighbor
edges and full transparency only in intentionally unused atlas regions.

Yaldabaoth swatches:

```text
muted gold:  #9A7638 / #C09A4A
aged bone:   #D7C8A1 / #A99772
ember:       #D95C2B / #FFB14A
storm-dark:  #171A24 / #303442
```

Celestial contract:

```text
Sun face:    #F8F8F2
Sun rim:     #090A0C
Moon body:   #08090B
Moon rim:    #F5F5EF
```

- [ ] **Step 6: Create all six animations**

Yaldabaoth idle is 4.0 seconds and loops. It animates neck breathing, sparse
mane plates, small head discontinuities, and a low-amplitude phase-delayed
wave over `segment_01`–`segment_10`.

Yaldabaoth combat is 1.8 seconds and does not loop. It opens the jaw, snaps the
head forward, expands the mane, then sends an increasing rotation wave down
the segments into a broad `terminal_sweep`.

Sun idle is 3.0 seconds and loops with axial rotation and restrained
disc/outline counter-pulsing. Sun combat is 1.2 seconds and does not loop:
compress, lock, flare, recover.

Moon idle is 3.2 seconds and loops with slow rocking and a slight offset between
`crescent` and `cut`. Moon combat is 1.2 seconds and does not loop: draw back,
rotate edge-on, slash, recover.

- [ ] **Step 7: Run asset tests and inspect the texture files**

Run: `./gradlew.bat test --tests "*YaldabaothAssetContractTest"`

Expected: PASS.

Open all three PNGs at original resolution and confirm there are no filtered
edges, accidental semitransparent seams, missing outline regions, or UV bleed.

- [ ] **Step 8: Commit the validated assets**

```powershell
git add src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth*.geo.json src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth*.animation.json src/main/resources/assets/mnagnosis/textures/entity/yaldabaoth src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothAssetContractTest.java
git commit -m "feat: add Yaldabaoth GeckoLib models and animations"
```

---

### Task 4: Client Models, Renderers, and Registration

**Files:**
- Create: the seven client Java files listed in the File Structure section.
- Modify: `src/main/resources/assets/mnagnosis/lang/en_us.json`

**Interfaces:**
- Consumes: Task 2 entity classes and Task 3 asset paths.
- Produces: client-only renderer registration for all three entity types.

- [ ] **Step 1: Extend the asset contract test with localization and model paths**

Assert that `en_us.json` contains:

```json
{
  "entity.mnagnosis.yaldabaoth": "Yaldabaoth",
  "entity.mnagnosis.yaldabaoth_sun": "The Counterfeit Sun",
  "entity.mnagnosis.yaldabaoth_moon": "The Counterfeit Moon"
}
```

Also assert that each intended `geo`, `animations`, and `textures` resource
path exists before a renderer can reference it.

- [ ] **Step 2: Run the focused asset test and verify RED**

Run: `./gradlew.bat test --tests "*YaldabaothAssetContractTest"`

Expected: FAIL because the three localization keys are absent.

- [ ] **Step 3: Add the three localization entries**

Patch only the three keys into the existing modified `en_us.json`; preserve
all unrelated user edits and valid JSON ordering.

- [ ] **Step 4: Implement the three `GeoModel` classes**

Each class returns only its exact model, texture, and animation resource:

```text
geo/entity/yaldabaoth.geo.json
animations/entity/yaldabaoth.animation.json
textures/entity/yaldabaoth/yaldabaoth.png

geo/entity/yaldabaoth_sun.geo.json
animations/entity/yaldabaoth_sun.animation.json
textures/entity/yaldabaoth/yaldabaoth_sun.png

geo/entity/yaldabaoth_moon.geo.json
animations/entity/yaldabaoth_moon.animation.json
textures/entity/yaldabaoth/yaldabaoth_moon.png
```

- [ ] **Step 5: Implement renderers**

`YaldabaothRenderer` uses normal entity lighting, a 2.0 shadow radius, and no
projection or emissive shader.

Sun and Moon renderers:

- Use `LightTexture.FULL_BRIGHT`.
- Use `RenderType.entityCutoutNoCull(texture)` so both sides remain visible.
- Use zero shadow radius.
- Apply no runtime orbit or attack effects.

- [ ] **Step 6: Register renderers in an isolated client event class**

`YaldabaothClientEvents` is annotated for `Dist.CLIENT`, MOD bus, and the
MnAGnosis mod ID. Its `EntityRenderersEvent.RegisterRenderers` handler
registers all three renderer constructors. Do not add client imports to
`EntityRegistry` or any common entity class.

- [ ] **Step 7: Verify client/common compilation and localization**

Run:

```powershell
./gradlew.bat test --tests "*YaldabaothAssetContractTest"
./gradlew.bat compileJava
./gradlew.bat processResources
```

Expected: all commands PASS with no missing resource or client/common linkage
errors.

- [ ] **Step 8: Commit client integration**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/client/render/entity/yaldabaoth src/main/java/com/vincenthuto/mnagnosis/client/event/YaldabaothClientEvents.java src/main/resources/assets/mnagnosis/lang/en_us.json
git commit -m "feat: render Yaldabaoth encounter entities"
```

---

### Task 5: Integrated Verification and Visual QA

**Files:**
- Modify only files already created in Tasks 1–4 if verification finds a defect.

**Interfaces:**
- Consumes: the complete visual foundation.
- Produces: verified spawn commands and a clean build.

- [ ] **Step 1: Run all JUnit tests**

Run: `./gradlew.bat test`

Expected: PASS with no failing tests.

- [ ] **Step 2: Run all Forge GameTests**

Run: `./gradlew.bat runGameTestServer`

Expected: PASS, including the Yaldabaoth registry, spawn, damage, no-AI, and
NBT cases.

- [ ] **Step 3: Run the complete build**

Run: `./gradlew.bat build`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Perform client visual QA**

Run: `./gradlew.bat runClient`

In a flat test area, execute:

```mcfunction
/summon mnagnosis:yaldabaoth ~ ~ ~
/summon mnagnosis:yaldabaoth_sun ~6 ~3 ~
/summon mnagnosis:yaldabaoth_moon ~-6 ~3 ~
```

Verify:

- Yaldabaoth is approximately 16 blocks long and is not culled when the camera
  looks toward its tail.
- The lion face, damaged-corona mane, lightning-fire eyes, segment chain, and
  terminal sweep read correctly.
- The Sun is white with a modeled black rim from front, back, and oblique
  angles.
- The Moon is a black crescent with a modeled white rim from front, back, and
  oblique angles.
- All three idle animations loop without a visible hitch.
- `/damage @e[type=mnagnosis:yaldabaoth,limit=1] 1 minecraft:generic`
  triggers the roar/sweep sample.
- Equivalent `/damage` commands trigger the judgment and omission-slash
  samples on the Sun and Moon.
- None moves, falls, chooses a target, attacks, or drops loot.

- [ ] **Step 5: Correct visual defects and rerun affected verification**

For any defect, first add or tighten the smallest automated asset/state test
that can reproduce it, watch that test fail, then patch the geometry,
animation, texture, or Java code. Rerun the focused test followed by
`./gradlew.bat test` and `./gradlew.bat build`.

- [ ] **Step 6: Inspect final scope and worktree**

Run:

```powershell
git status --short
git diff --check
git diff --stat HEAD~4..HEAD
```

Confirm that only the planned Yaldabaoth files and narrow additive
`EntityRegistry`/localization edits belong to this feature. Do not stage or
alter unrelated pre-existing changes.

- [ ] **Step 7: Commit any QA corrections**

If visual QA required corrections:

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth
git add src/main/java/com/vincenthuto/mnagnosis/client/render/entity/yaldabaoth
git add src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth.geo.json
git add src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth_sun.geo.json
git add src/main/resources/assets/mnagnosis/geo/entity/yaldabaoth_moon.geo.json
git add src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth.animation.json
git add src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth_sun.animation.json
git add src/main/resources/assets/mnagnosis/animations/entity/yaldabaoth_moon.animation.json
git add src/main/resources/assets/mnagnosis/textures/entity/yaldabaoth
git add src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth
git commit -m "fix: polish Yaldabaoth entity presentation"
```

- [ ] **Step 8: Record final evidence**

Report the exact successful commands, the three summon commands, animation
trigger behavior, any remaining limitations from Explicit Exclusions, and the
feature commit IDs.
