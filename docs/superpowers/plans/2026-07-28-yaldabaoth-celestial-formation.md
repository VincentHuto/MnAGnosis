# Yaldabaoth Celestial Formation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Spawn an owned Counterfeit Sun and Moon with every Yaldabaoth, keep them facing-locked on his right and left with opposite bobbing, and independently respawn destroyed companions after exactly 400 loaded ticks.

**Architecture:** Put deterministic side, yaw, bob, and countdown calculations in pure Java components. Yaldabaoth owns companion UUIDs and respawn state; each celestial owns a synchronized Yaldabaoth UUID, follows the calculated formation on the server, and notifies its owner when killed.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1, GeckoLib 4, JUnit 5, Forge GameTest, Gradle

## Global Constraints

- Sun remains six blocks to Yaldabaoth's local right; Moon remains six blocks to his local left.
- Both use a five-block base height and a 0.75-block sinusoidal bob with an 80-tick period.
- Moon is exactly 40 ticks out of phase with Sun.
- Each destroyed companion respawns independently after exactly 400 loaded ticks.
- Yaldabaoth owns at most one Sun and one Moon.
- Owner UUIDs, companion UUIDs, and respawn countdowns survive NBT.
- Ownerless Sun and Moon summons remain stationary and never adopt a nearby boss.
- Removing Yaldabaoth permanently removes his owned celestials.
- Preserve all entity registrations, GeckoLib assets and identifiers, attributes, allegiance, and combat animation behavior.
- Keep the user's unrelated gravity-shift work unstaged and unmodified.

## File Map

- Create `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialRole.java`: immutable Sun/Moon side and phase identity.
- Create `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialFormation.java`: pure offset and respawn-countdown math.
- Create `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialFormationTest.java`: unit contracts for cardinal facing, bobbing, and countdown boundaries.
- Modify `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractCelestialEntity.java`: owner synchronization, persistence, following, and death notification.
- Modify `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothSunEntity.java`: identify the Sun role.
- Modify `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothMoonEntity.java`: identify the Moon role.
- Modify `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothEntity.java`: spawn, recover, count down, respawn, persist, and clean up companions.
- Modify `src/main/java/com/vincenthuto/mnagnosis/gametest/YaldabaothEntityGameTests.java`: integrated ownership, formation, respawn, cleanup, and ownerless behavior.

---

### Task 1: Pure Formation and Respawn Math

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialRole.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialFormation.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialFormationTest.java`

**Interfaces:**
- Consumes: yaw in Minecraft degrees, owner tick count, and fixed `CelestialRole`.
- Produces: `CelestialRole.SUN`, `CelestialRole.MOON`, `CelestialFormation.Offset`, `offset(float,long,CelestialRole)`, `tickRespawn(int)`, and `isRespawnReady(int)`.

- [ ] **Step 1: Write the failing unit tests**

Create `CelestialFormationTest.java`:

```java
package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CelestialFormationTest {

    private static final double EPSILON = 1.0E-6D;

    @Test
    void sunAndMoonRemainOnOppositeFacingRelativeSides() {
        assertOffset(0.0F, CelestialRole.SUN, -6.0D, 0.0D);
        assertOffset(0.0F, CelestialRole.MOON, 6.0D, 0.0D);
        assertOffset(90.0F, CelestialRole.SUN, 0.0D, -6.0D);
        assertOffset(90.0F, CelestialRole.MOON, 0.0D, 6.0D);
        assertOffset(180.0F, CelestialRole.SUN, 6.0D, 0.0D);
        assertOffset(180.0F, CelestialRole.MOON, -6.0D, 0.0D);
        assertOffset(270.0F, CelestialRole.SUN, 0.0D, 6.0D);
        assertOffset(270.0F, CelestialRole.MOON, 0.0D, -6.0D);
    }

    @Test
    void bobbingHasExactAmplitudePeriodAndOppositePhase() {
        CelestialFormation.Offset sunStart =
                CelestialFormation.offset(0.0F, 0L, CelestialRole.SUN);
        CelestialFormation.Offset moonStart =
                CelestialFormation.offset(0.0F, 0L, CelestialRole.MOON);
        CelestialFormation.Offset sunPeak =
                CelestialFormation.offset(0.0F, 20L, CelestialRole.SUN);
        CelestialFormation.Offset moonTrough =
                CelestialFormation.offset(0.0F, 20L, CelestialRole.MOON);
        CelestialFormation.Offset sunClosed =
                CelestialFormation.offset(0.0F, 80L, CelestialRole.SUN);

        assertEquals(5.0D, sunStart.y(), EPSILON);
        assertEquals(5.0D, moonStart.y(), EPSILON);
        assertEquals(5.75D, sunPeak.y(), EPSILON);
        assertEquals(4.25D, moonTrough.y(), EPSILON);
        assertEquals(sunStart.y(), sunClosed.y(), EPSILON);
    }

    @Test
    void respawnCountdownWaitsAllFourHundredTicks() {
        int remaining = CelestialFormation.RESPAWN_TICKS;
        for (int tick = 0; tick < 399; tick++) {
            remaining = CelestialFormation.tickRespawn(remaining);
        }
        assertEquals(1, remaining);
        assertFalse(CelestialFormation.isRespawnReady(remaining));

        remaining = CelestialFormation.tickRespawn(remaining);
        assertEquals(0, remaining);
        assertTrue(CelestialFormation.isRespawnReady(remaining));
        assertEquals(0, CelestialFormation.tickRespawn(0));
    }

    private static void assertOffset(
            float yaw,
            CelestialRole role,
            double expectedX,
            double expectedZ
    ) {
        CelestialFormation.Offset offset =
                CelestialFormation.offset(yaw, 0L, role);
        assertEquals(expectedX, offset.x(), EPSILON);
        assertEquals(expectedZ, offset.z(), EPSILON);
    }
}
```

The mutation caught by the first test is using world-space sides instead of Yaldabaoth's local yaw; the second catches synchronized or wrong-amplitude bobbing; the third catches an early 399-tick respawn.

- [ ] **Step 2: Run the unit test and confirm the intended compile failure**

Run:

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.entity.yaldabaoth.CelestialFormationTest"
```

Expected: test compilation fails because `CelestialRole` and `CelestialFormation` do not exist.

- [ ] **Step 3: Add fixed celestial roles**

Create `CelestialRole.java`:

```java
package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

public enum CelestialRole {
    SUN(1.0D, 0.0D),
    MOON(-1.0D, Math.PI);

    private final double side;
    private final double phase;

    CelestialRole(double side, double phase) {
        this.side = side;
        this.phase = phase;
    }

    double side() {
        return this.side;
    }

    double phase() {
        return this.phase;
    }
}
```

- [ ] **Step 4: Implement deterministic formation math**

Create `CelestialFormation.java`:

```java
package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

public final class CelestialFormation {

    public static final int RESPAWN_TICKS = 400;
    private static final double LATERAL_DISTANCE = 6.0D;
    private static final double BASE_HEIGHT = 5.0D;
    private static final double BOB_AMPLITUDE = 0.75D;
    private static final double BOB_PERIOD_TICKS = 80.0D;

    private CelestialFormation() {
    }

    public static Offset offset(
            float ownerYaw,
            long ownerTick,
            CelestialRole role
    ) {
        double sideAngle = Math.toRadians(ownerYaw + 90.0D);
        double lateral = LATERAL_DISTANCE * role.side();
        double x = -Math.sin(sideAngle) * lateral;
        double z = Math.cos(sideAngle) * lateral;
        double bobAngle =
                (Math.PI * 2.0D * ownerTick / BOB_PERIOD_TICKS) + role.phase();
        double y = BASE_HEIGHT + Math.sin(bobAngle) * BOB_AMPLITUDE;
        return new Offset(x, y, z);
    }

    public static int tickRespawn(int remaining) {
        return Math.max(0, remaining - 1);
    }

    public static boolean isRespawnReady(int remaining) {
        return remaining <= 0;
    }

    public record Offset(double x, double y, double z) {
    }
}
```

- [ ] **Step 5: Run the focused unit test**

Run:

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.entity.yaldabaoth.CelestialFormationTest"
```

Expected: PASS with all cardinal offsets, bob phases, and countdown boundaries satisfied.

- [ ] **Step 6: Commit the pure formation component**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialRole.java src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialFormation.java src/test/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/CelestialFormationTest.java
git commit -m "feat: define Yaldabaoth celestial formation"
```

---

### Task 2: Owned Celestial Following and Persistence

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractCelestialEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothSunEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothMoonEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/YaldabaothEntityGameTests.java`

**Interfaces:**
- Consumes: `CelestialFormation.offset(...)` and fixed `CelestialRole`.
- Produces: synchronized/persistent owner UUID, `setOwner(YaldabaothEntity)`, `getOwnerId()`, `getCelestialRole()`, and server-side following.

- [ ] **Step 1: Add a failing ownerless and ownership-NBT GameTest**

Add these imports to `YaldabaothEntityGameTests`:

```java
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.CelestialRole;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothMoonEntity;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothSunEntity;
import java.util.Optional;
```

Add:

```java
@GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
public static void celestialOwnershipPersistsWithoutAdoptingOwnerlessSummons(
        GameTestHelper helper
) {
    YaldabaothEntity owner = (YaldabaothEntity) requireType(
            helper,
            "yaldabaoth"
    ).create(helper.getLevel());
    YaldabaothSunEntity owned = (YaldabaothSunEntity) requireType(
            helper,
            "yaldabaoth_sun"
    ).create(helper.getLevel());
    YaldabaothMoonEntity ownerless = (YaldabaothMoonEntity) requireType(
            helper,
            "yaldabaoth_moon"
    ).create(helper.getLevel());
    helper.assertTrue(owner != null && owned != null && ownerless != null,
            "Celestial ownership fixtures could not be created");

    owned.setOwner(owner);
    CompoundTag saved = new CompoundTag();
    owned.saveWithoutId(saved);
    YaldabaothSunEntity loaded = (YaldabaothSunEntity) requireType(
            helper,
            "yaldabaoth_sun"
    ).create(helper.getLevel());
    helper.assertTrue(loaded != null, "Owned Sun could not be reloaded");
    loaded.load(saved);

    helper.assertTrue(
            loaded.getOwnerId().equals(Optional.of(owner.getUUID())),
            "Owned Sun did not preserve Yaldabaoth UUID"
    );
    helper.assertTrue(
            loaded.getCelestialRole() == CelestialRole.SUN,
            "Sun reported the wrong formation role"
    );
    helper.assertTrue(
            ownerless.getOwnerId().isEmpty(),
            "Ownerless Moon adopted an owner"
    );
    helper.assertTrue(
            ownerless.getCelestialRole() == CelestialRole.MOON,
            "Moon reported the wrong formation role"
    );
    helper.succeed();
}
```

- [ ] **Step 2: Run GameTest compilation and confirm failure**

Run:

```powershell
.\gradlew.bat compileJava compileTestJava
```

Expected: compilation fails because `setOwner`, `getOwnerId`, and `getCelestialRole` do not exist.

- [ ] **Step 3: Add synchronized owner state and NBT**

In `AbstractCelestialEntity`, add:

```java
private static final String OWNER_TAG = "YaldabaothOwner";
private static final int OWNER_MISSING_GRACE_TICKS = 20;
private static final EntityDataAccessor<Optional<UUID>> OWNER =
        SynchedEntityData.defineId(
                AbstractCelestialEntity.class,
                EntityDataSerializers.OPTIONAL_UUID
        );

private int ownerMissingTicks;
```

Import `ServerLevel`, `Entity`, `Vec3`, `Optional`, and `UUID`.

Extend `defineSynchedData()`:

```java
this.entityData.define(OWNER, Optional.empty());
```

Add:

```java
public final Optional<UUID> getOwnerId() {
    return this.entityData.get(OWNER);
}

public final void setOwner(YaldabaothEntity owner) {
    this.entityData.set(
            OWNER,
            owner == null ? Optional.empty() : Optional.of(owner.getUUID())
    );
}

public abstract CelestialRole getCelestialRole();
```

Extend NBT methods:

```java
if (tag.hasUUID(OWNER_TAG)) {
    this.entityData.set(OWNER, Optional.of(tag.getUUID(OWNER_TAG)));
} else {
    this.entityData.set(OWNER, Optional.empty());
}
```

```java
this.getOwnerId().ifPresent(owner -> tag.putUUID(OWNER_TAG, owner));
```

- [ ] **Step 4: Follow the owner**

Override `tick()` in `AbstractCelestialEntity`:

```java
@Override
public void tick() {
    super.tick();
    if (!(this.level() instanceof ServerLevel serverLevel)) {
        return;
    }
    Optional<UUID> ownerId = this.getOwnerId();
    if (ownerId.isEmpty()) {
        this.ownerMissingTicks = 0;
        return;
    }
    Entity rawOwner = serverLevel.getEntity(ownerId.get());
    if (rawOwner instanceof YaldabaothEntity owner && owner.isAlive()) {
        this.ownerMissingTicks = 0;
        CelestialFormation.Offset offset = CelestialFormation.offset(
                owner.getYRot(),
                owner.tickCount,
                this.getCelestialRole()
        );
        Vec3 target = owner.position().add(offset.x(), offset.y(), offset.z());
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(target);
        this.setYRot(owner.getYRot());
        this.setYHeadRot(owner.getYRot());
        this.yBodyRot = owner.getYRot();
        return;
    }
    if (rawOwner instanceof YaldabaothEntity || ++this.ownerMissingTicks >=
            OWNER_MISSING_GRACE_TICKS) {
        this.discard();
    }
}
```

- [ ] **Step 5: Give each concrete celestial a fixed role**

Add to `YaldabaothSunEntity`:

```java
@Override
public CelestialRole getCelestialRole() {
    return CelestialRole.SUN;
}
```

Add to `YaldabaothMoonEntity`:

```java
@Override
public CelestialRole getCelestialRole() {
    return CelestialRole.MOON;
}
```

- [ ] **Step 6: Run the focused unit suite and compile GameTests**

Run:

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.entity.yaldabaoth.CelestialFormationTest"
.\gradlew.bat compileJava compileTestJava
```

Expected: both commands pass; owner NBT and role APIs compile while pure formation behavior remains green.

- [ ] **Step 7: Commit owned celestial behavior**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractCelestialEntity.java src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothSunEntity.java src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothMoonEntity.java src/main/java/com/vincenthuto/mnagnosis/gametest/YaldabaothEntityGameTests.java
git commit -m "feat: bind celestial entities to Yaldabaoth"
```

---

### Task 3: Companion Spawn, Recovery, Respawn, and Cleanup

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractCelestialEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothEntity.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/YaldabaothEntityGameTests.java`

**Interfaces:**
- Consumes: owned celestial APIs and pure formation/countdown functions from Tasks 1–2.
- Produces: death notification, immediate initial pair, UUID recovery, one-per-role invariant, independent 400-tick return, NBT state, and owner cleanup.

- [ ] **Step 1: Add failing initial-spawn and formation GameTest**

Add these imports:

```java
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.AbstractCelestialEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;
import java.util.UUID;
```

Add:

```java
@GameTest(
        templateNamespace = MnAGnosis.MODID,
        template = "empty",
        timeoutTicks = 40
)
public static void yaldabaothCreatesOneFacingLockedCelestialPair(
        GameTestHelper helper
) {
    YaldabaothEntity boss = spawnBoss(helper, 0.0F);

    helper.runAfterDelay(2, () -> {
        List<AbstractCelestialEntity> companions = ownedCompanions(helper, boss);
        helper.assertTrue(companions.size() == 2,
                "Yaldabaoth did not create exactly two companions");
        YaldabaothSunEntity sun = requireOwned(
                helper,
                companions,
                CelestialRole.SUN
        );
        YaldabaothMoonEntity moon = requireOwned(
                helper,
                companions,
                CelestialRole.MOON
        );
        helper.assertTrue(sun.getX() < boss.getX(),
                "Sun was not on Yaldabaoth's right at yaw zero");
        helper.assertTrue(moon.getX() > boss.getX(),
                "Moon was not on Yaldabaoth's left at yaw zero");

        boss.setYRot(90.0F);
        boss.setPos(boss.getX() + 2.0D, boss.getY(), boss.getZ() + 3.0D);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(sun.getZ() < boss.getZ(),
                    "Sun did not rotate with Yaldabaoth");
            helper.assertTrue(moon.getZ() > boss.getZ(),
                    "Moon did not rotate with Yaldabaoth");
            helper.assertTrue(ownedCompanions(helper, boss).size() == 2,
                    "Repeated maintenance duplicated a companion");
            helper.succeed();
        });
    });
}
```

- [ ] **Step 2: Add failing independent delayed-respawn GameTest**

Add:

```java
@GameTest(
        templateNamespace = MnAGnosis.MODID,
        template = "empty",
        timeoutTicks = 430
)
public static void destroyedSunReturnsAfterIndependentFourHundredTickDelay(
        GameTestHelper helper
) {
    YaldabaothEntity boss = spawnBoss(helper, 0.0F);

    helper.runAfterDelay(2, () -> {
        List<AbstractCelestialEntity> initial = ownedCompanions(helper, boss);
        YaldabaothSunEntity sun =
                requireOwned(helper, initial, CelestialRole.SUN);
        YaldabaothMoonEntity moon =
                requireOwned(helper, initial, CelestialRole.MOON);
        UUID originalSun = sun.getUUID();
        UUID originalMoon = moon.getUUID();

        sun.kill();
        helper.assertTrue(
                boss.getCompanionRespawnTicks(CelestialRole.SUN)
                        == CelestialFormation.RESPAWN_TICKS,
                "Sun respawn did not start at 400 ticks"
        );
        helper.assertTrue(
                boss.getCompanionRespawnTicks(CelestialRole.MOON) == 0,
                "Sun destruction changed Moon respawn state"
        );

        helper.runAfterDelay(398, () -> {
            helper.assertTrue(
                    ownedCompanions(helper, boss).stream()
                            .noneMatch(entity ->
                                    entity.getCelestialRole() == CelestialRole.SUN),
                    "Sun returned before the 400-tick delay elapsed"
            );
            helper.assertTrue(
                    boss.getCompanionId(CelestialRole.MOON)
                            .filter(originalMoon::equals)
                            .isPresent(),
                    "Surviving Moon was replaced"
            );
        });

        helper.runAfterDelay(402, () -> {
            YaldabaothSunEntity returned = requireOwned(
                    helper,
                    ownedCompanions(helper, boss),
                    CelestialRole.SUN
            );
            helper.assertTrue(!returned.getUUID().equals(originalSun),
                    "Destroyed Sun did not receive a new entity identity");
            helper.assertTrue(
                    boss.getCompanionRespawnTicks(CelestialRole.SUN) == 0,
                    "Sun respawn timer did not clear"
            );
            helper.succeed();
        });
    });
}
```

- [ ] **Step 3: Add failing NBT and cleanup GameTest**

Add:

```java
@GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
public static void companionStatePersistsAndOwnerCleanupIsScoped(
        GameTestHelper helper
) {
    YaldabaothEntity boss = spawnBoss(helper, 0.0F);
    YaldabaothMoonEntity ownerless = (YaldabaothMoonEntity) requireType(
            helper,
            "yaldabaoth_moon"
    ).create(helper.getLevel());
    helper.assertTrue(ownerless != null, "Ownerless Moon could not be created");
    ownerless.setPos(boss.position().add(0.0D, 1.0D, 0.0D));
    helper.getLevel().addFreshEntity(ownerless);

    helper.runAfterDelay(2, () -> {
        YaldabaothSunEntity sun = requireOwned(
                helper,
                ownedCompanions(helper, boss),
                CelestialRole.SUN
        );
        sun.kill();

        CompoundTag saved = new CompoundTag();
        boss.saveWithoutId(saved);
        YaldabaothEntity loaded = (YaldabaothEntity) requireType(
                helper,
                "yaldabaoth"
        ).create(helper.getLevel());
        helper.assertTrue(loaded != null, "Yaldabaoth could not be reloaded");
        loaded.load(saved);
        helper.assertTrue(
                loaded.getCompanionRespawnTicks(CelestialRole.SUN) > 0,
                "Sun respawn timer did not survive NBT"
        );
        helper.assertTrue(
                loaded.getCompanionId(CelestialRole.MOON).isPresent(),
                "Moon UUID did not survive NBT"
        );

        boss.discard();
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(ownedCompanions(helper, boss).isEmpty(),
                    "Owned companions survived Yaldabaoth removal");
            helper.assertTrue(ownerless.isAlive(),
                    "Yaldabaoth cleanup removed an ownerless celestial");
            helper.succeed();
        });
    });
}
```

- [ ] **Step 4: Add reusable GameTest helpers**

Add to `YaldabaothEntityGameTests`:

```java
private static YaldabaothEntity spawnBoss(
        GameTestHelper helper,
        float yaw
) {
    YaldabaothEntity boss = (YaldabaothEntity) requireType(
            helper,
            "yaldabaoth"
    ).create(helper.getLevel());
    helper.assertTrue(boss != null, "Yaldabaoth could not be created");
    net.minecraft.world.phys.Vec3 spawn =
            helper.absoluteVec(new net.minecraft.world.phys.Vec3(
            5.0D,
            2.0D,
            5.0D
    ));
    boss.moveTo(spawn.x(), spawn.y(), spawn.z(), yaw, 0.0F);
    helper.getLevel().addFreshEntity(boss);
    return boss;
}

private static List<AbstractCelestialEntity> ownedCompanions(
        GameTestHelper helper,
        YaldabaothEntity boss
) {
    return helper.getLevel().getEntitiesOfClass(
            AbstractCelestialEntity.class,
            new AABB(boss.blockPosition()).inflate(16.0D),
            entity -> entity.getOwnerId()
                    .filter(boss.getUUID()::equals)
                    .isPresent()
    );
}

@SuppressWarnings("unchecked")
private static <T extends AbstractCelestialEntity> T requireOwned(
        GameTestHelper helper,
        List<AbstractCelestialEntity> companions,
        CelestialRole role
) {
    AbstractCelestialEntity match = companions.stream()
            .filter(entity -> entity.getCelestialRole() == role)
            .findFirst()
            .orElse(null);
    helper.assertTrue(match != null, "Missing owned " + role);
    return (T) match;
}
```

- [ ] **Step 5: Compile and confirm the companion-management API failures**

Run:

```powershell
.\gradlew.bat compileJava
```

Expected: compilation fails because `onOwnedCelestialKilled`, `getCompanionId`, and `getCompanionRespawnTicks` do not exist on `YaldabaothEntity`.

- [ ] **Step 6: Add companion state and read-only APIs**

In `YaldabaothEntity`, add imports for `CompoundTag`, `DamageSource`, `Entity`, `ServerLevel`, `EntityRegistry`, `AABB`, `UUID`, and `Optional`.

Add constants and state:

```java
private static final String SUN_ID_TAG = "SunCompanion";
private static final String MOON_ID_TAG = "MoonCompanion";
private static final String SUN_RESPAWN_TAG = "SunRespawnTicks";
private static final String MOON_RESPAWN_TAG = "MoonRespawnTicks";
private static final double RECOVERY_RADIUS = 16.0D;

private UUID sunId;
private UUID moonId;
private int sunRespawnTicks;
private int moonRespawnTicks;
private boolean cleaningUpCompanions;
```

Add:

```java
public Optional<UUID> getCompanionId(CelestialRole role) {
    return Optional.ofNullable(role == CelestialRole.SUN
            ? this.sunId
            : this.moonId);
}

public int getCompanionRespawnTicks(CelestialRole role) {
    return role == CelestialRole.SUN
            ? this.sunRespawnTicks
            : this.moonRespawnTicks;
}

void onOwnedCelestialKilled(CelestialRole role, UUID celestialId) {
    if (!this.isAlive()
            || this.getCompanionId(role)
                    .filter(celestialId::equals)
                    .isEmpty()) {
        return;
    }
    this.setCompanionId(role, null);
    this.setCompanionRespawnTicks(
            role,
            CelestialFormation.RESPAWN_TICKS
    );
}

private void setCompanionId(CelestialRole role, UUID id) {
    if (role == CelestialRole.SUN) {
        this.sunId = id;
    } else {
        this.moonId = id;
    }
}

private void setCompanionRespawnTicks(CelestialRole role, int ticks) {
    int clamped = Math.max(0, Math.min(
            CelestialFormation.RESPAWN_TICKS,
            ticks
    ));
    if (role == CelestialRole.SUN) {
        this.sunRespawnTicks = clamped;
    } else {
        this.moonRespawnTicks = clamped;
    }
}
```

- [ ] **Step 7: Notify Yaldabaoth when an owned celestial dies**

Import `DamageSource` in `AbstractCelestialEntity` and add:

```java
@Override
public void die(DamageSource source) {
    if (this.level() instanceof ServerLevel serverLevel) {
        this.getOwnerId()
                .map(serverLevel::getEntity)
                .filter(YaldabaothEntity.class::isInstance)
                .map(YaldabaothEntity.class::cast)
                .filter(YaldabaothEntity::isAlive)
                .ifPresent(owner -> owner.onOwnedCelestialKilled(
                        this.getCelestialRole(),
                        this.getUUID()
                ));
    }
    super.die(source);
}
```

- [ ] **Step 8: Maintain and spawn both roles**

Extend `tick()` in `YaldabaothEntity`:

```java
@Override
public void tick() {
    super.tick();
    if (this.level() instanceof ServerLevel serverLevel && this.isAlive()) {
        this.maintainCompanion(serverLevel, CelestialRole.SUN);
        this.maintainCompanion(serverLevel, CelestialRole.MOON);
    }
}
```

Add:

```java
private void maintainCompanion(ServerLevel level, CelestialRole role) {
    AbstractCelestialEntity companion = this.resolveCompanion(level, role);
    if (companion != null) {
        this.setCompanionId(role, companion.getUUID());
        this.setCompanionRespawnTicks(role, 0);
        return;
    }

    if (this.getCompanionId(role).isPresent()) {
        this.setCompanionId(role, null);
        this.setCompanionRespawnTicks(
                role,
                CelestialFormation.RESPAWN_TICKS
        );
        return;
    }

    int remaining = this.getCompanionRespawnTicks(role);
    if (remaining > 0) {
        remaining = CelestialFormation.tickRespawn(remaining);
        this.setCompanionRespawnTicks(role, remaining);
        if (!CelestialFormation.isRespawnReady(remaining)) {
            return;
        }
    }
    this.spawnCompanion(level, role);
}

private AbstractCelestialEntity resolveCompanion(
        ServerLevel level,
        CelestialRole role
) {
    Entity byId = this.getCompanionId(role)
            .map(level::getEntity)
            .orElse(null);
    if (byId instanceof AbstractCelestialEntity celestial
            && celestial.getCelestialRole() == role
            && celestial.getOwnerId().filter(this.getUUID()::equals).isPresent()
            && celestial.isAlive()) {
        return celestial;
    }
    return level.getEntitiesOfClass(
            AbstractCelestialEntity.class,
            this.getBoundingBox().inflate(RECOVERY_RADIUS),
            celestial -> celestial.isAlive()
                    && celestial.getCelestialRole() == role
                    && celestial.getOwnerId()
                            .filter(this.getUUID()::equals)
                            .isPresent()
    ).stream().findFirst().orElse(null);
}

private void spawnCompanion(ServerLevel level, CelestialRole role) {
    AbstractCelestialEntity celestial = role == CelestialRole.SUN
            ? EntityRegistry.YALDABAOTH_SUN.get().create(level)
            : EntityRegistry.YALDABAOTH_MOON.get().create(level);
    if (celestial == null) {
        return;
    }
    celestial.setOwner(this);
    CelestialFormation.Offset offset = CelestialFormation.offset(
            this.getYRot(),
            this.tickCount,
            role
    );
    celestial.moveTo(
            this.getX() + offset.x(),
            this.getY() + offset.y(),
            this.getZ() + offset.z(),
            this.getYRot(),
            0.0F
    );
    if (level.addFreshEntity(celestial)) {
        this.setCompanionId(role, celestial.getUUID());
        this.setCompanionRespawnTicks(role, 0);
    }
}
```

- [ ] **Step 9: Persist companion state**

Extend `addAdditionalSaveData`:

```java
super.addAdditionalSaveData(tag);
if (this.sunId != null) {
    tag.putUUID(SUN_ID_TAG, this.sunId);
}
if (this.moonId != null) {
    tag.putUUID(MOON_ID_TAG, this.moonId);
}
tag.putInt(SUN_RESPAWN_TAG, this.sunRespawnTicks);
tag.putInt(MOON_RESPAWN_TAG, this.moonRespawnTicks);
```

Extend `readAdditionalSaveData`:

```java
super.readAdditionalSaveData(tag);
this.sunId = tag.hasUUID(SUN_ID_TAG) ? tag.getUUID(SUN_ID_TAG) : null;
this.moonId = tag.hasUUID(MOON_ID_TAG) ? tag.getUUID(MOON_ID_TAG) : null;
this.setCompanionRespawnTicks(
        CelestialRole.SUN,
        tag.getInt(SUN_RESPAWN_TAG)
);
this.setCompanionRespawnTicks(
        CelestialRole.MOON,
        tag.getInt(MOON_RESPAWN_TAG)
);
```

- [ ] **Step 10: Remove only owned companions with Yaldabaoth**

Add:

```java
@Override
public void die(DamageSource source) {
    this.removeOwnedCompanions();
    super.die(source);
}

@Override
public void remove(RemovalReason reason) {
    if (!this.level().isClientSide && reason.shouldDestroy()) {
        this.removeOwnedCompanions();
    }
    super.remove(reason);
}

private void removeOwnedCompanions() {
    if (this.cleaningUpCompanions
            || !(this.level() instanceof ServerLevel serverLevel)) {
        return;
    }
    this.cleaningUpCompanions = true;
    for (CelestialRole role : CelestialRole.values()) {
        AbstractCelestialEntity companion =
                this.resolveCompanion(serverLevel, role);
        if (companion != null) {
            companion.discard();
        }
        this.setCompanionId(role, null);
        this.setCompanionRespawnTicks(role, 0);
    }
}
```

Forge 1.20.1 exposes `Entity.RemovalReason.shouldDestroy()`. Keep that exact guard: it is false for chunk unload and prevents companions from being removed during `UNLOADED_TO_CHUNK`.

- [ ] **Step 11: Run unit tests and the GameTest server**

Run:

```powershell
.\gradlew.bat test
.\gradlew.bat runGameTestServer
```

Expected: unit tests pass and the GameTest server reports all MnAGnosis GameTests successful, including the new ownership, formation, respawn, persistence, and cleanup cases.

- [ ] **Step 12: Commit encounter lifecycle**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/AbstractCelestialEntity.java src/main/java/com/vincenthuto/mnagnosis/common/entity/yaldabaoth/YaldabaothEntity.java src/main/java/com/vincenthuto/mnagnosis/gametest/YaldabaothEntityGameTests.java
git commit -m "feat: maintain Yaldabaoth celestial companions"
```

---

### Task 4: Integrated Verification and Visual Acceptance

**Files:**
- Verify only; correct the scoped files from Tasks 1–3 if a check exposes a defect.

**Interfaces:**
- Consumes: complete formation and companion lifecycle.
- Produces: a buildable mod with verified unit, GameTest, and scope status plus explicit visual-QA status.

- [ ] **Step 1: Run patch and scope checks**

Run:

```powershell
git diff --check HEAD~3..HEAD
git diff --name-only HEAD~3..HEAD
git status --short
```

Expected: the three implementation commits contain only the files listed in this plan. Existing gravity changes remain uncommitted and outside the scoped diff.

- [ ] **Step 2: Force all unit tests to execute**

Run:

```powershell
.\gradlew.bat test --rerun-tasks
```

Expected: BUILD SUCCESSFUL with the `test` task executed and zero failures.

- [ ] **Step 3: Run the complete Forge build**

Run:

```powershell
.\gradlew.bat build
```

Expected: BUILD SUCCESSFUL through compilation, resources, tests, jar creation, and reobfuscation.

- [ ] **Step 4: Re-run GameTests**

Run:

```powershell
.\gradlew.bat runGameTestServer
```

Expected: the headless server exits successfully after reporting all GameTests passed.

- [ ] **Step 5: Perform development-client visual acceptance when capture is available**

Observe a spawned Yaldabaoth long enough to verify:

1. Sun stays on his apparent right and Moon on his apparent left at multiple yaws.
2. Both translate with him while he slithers.
3. Their vertical bobs are smooth, equal in amplitude, and opposite in phase.
4. Destroying one leaves the other undisturbed and the missing role reappears after 20 loaded seconds.
5. Neither companion visibly snaps, trails excessively, or turns edge-on during ordinary formation movement.

If the environment still returns `SetIsBorderRequired failed: No such interface supported (0x80004002)` for Minecraft and Blockbench capture, report visual acceptance as pending rather than claiming it passed.
