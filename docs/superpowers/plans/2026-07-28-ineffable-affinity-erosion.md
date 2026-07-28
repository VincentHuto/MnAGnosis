# Ineffable Affinity Erosion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace ordinary affinity gain from completed MnAGnosis ineffable spells with a single 0.1-point erosion of each of Mana and Artifice's six core affinities.

**Architecture:** A focused `IneffableAffinityErosion` service will classify spells by stable MnAGnosis component IDs and perform clamped core-affinity mutation. A narrow `SpellCasterMixin` will call that service once inside Mana and Artifice's existing XP/affinity award flow and suppress only that flow's ordinary affinity shifts for qualifying spells.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1, Mana and Artifice API, Sponge Mixin 0.8.5, JUnit 5, Forge GameTest

## Global Constraints

- The qualifying component IDs are exactly True Damage, Gravity Convergence, Gravity Shift, Living Land, and Banish.
- Each completed qualifying cast removes exactly `0.1F` from `ARCANE`, `EARTH`, `ENDER`, `FIRE`, `WATER`, and `WIND`.
- Core affinity depths clamp independently at `0.0F`.
- `BLOOD`, `HELLFIRE`, `ICE`, `LIGHTNING`, and `UNKNOWN` remain unchanged.
- A qualifying cast receives no ordinary affinity shift, including a mixed spell with ordinary components.
- One cast erodes once regardless of the number of qualifying components.
- The Affinity Lock belt does not prevent erosion.
- Failed or invalid casts do not erode affinity.
- Magic XP, rote XP, and unrelated casting bookkeeping remain on Mana and Artifice's original path.
- Mutations are server-authoritative and synchronize only when at least one depth changes.
- Preserve all unrelated dirty-worktree changes, especially the existing edits to `src/main/resources/mnagnosis.mixins.json`.

---

## File Structure

- Create `src/main/java/com/vincenthuto/mnagnosis/common/spell/IneffableAffinityErosion.java`
  - Own the qualifying component-ID set, spell classification, core-affinity list, erosion amount, clamped mutation, and sync decision.
- Create `src/test/java/com/vincenthuto/mnagnosis/common/spell/IneffableAffinityErosionTest.java`
  - Exercise classification and mutation through lightweight dynamic proxies for the M&A interfaces.
- Create `src/main/java/com/vincenthuto/mnagnosis/mixin/core/SpellCasterMixin.java`
  - Bridge the service into M&A's single award transaction and suppress its normal shift calls for qualifying spells.
- Create `src/main/java/com/vincenthuto/mnagnosis/gametest/IneffableAffinityErosionGameTests.java`
  - Verify the Mixin against real player capabilities, registered spell components, mixed spells, and the Affinity Lock belt.
- Modify `src/main/resources/mnagnosis.mixins.json`
  - Register `core.SpellCasterMixin` without altering any existing entries.

### Task 1: Classifier and erosion service

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/spell/IneffableAffinityErosion.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/spell/IneffableAffinityErosionTest.java`

**Interfaces:**
- Consumes:
  - `ISpellDefinition.containsPart(ResourceLocation): boolean`
  - `IPlayerMagic.getAffinityDepth(Affinity): float`
  - `IPlayerMagic.setAffinityDepth(Affinity, float): void`
  - `IPlayerMagic.forceSync(): void`
- Produces:
  - `public static boolean isIneffable(ISpellDefinition spell)`
  - `public static boolean shouldApplyOrdinaryAffinity(ISpellDefinition spell)`
  - `public static boolean erode(IPlayerMagic magic)`
  - `static float erodedDepth(float currentDepth)` for package-local unit testing

- [ ] **Step 1: Write failing classification tests**

Create the test class with a dynamic `ISpellDefinition` proxy whose
`containsPart` result is driven by a set of registry IDs:

```java
package com.vincenthuto.mnagnosis.common.spell;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.spells.base.ISpellDefinition;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipRegistry;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.EnumMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class IneffableAffinityErosionTest {

    @Test
    void recognizesEveryIneffableEffectId() {
        for (ResourceLocation id : Set.of(
                SpellComponentRegistry.TRUE_DAMAGE_ID,
                SpellComponentRegistry.GRAVITY_CONVERGENCE_ID,
                SpellComponentRegistry.GRAVITY_SHIFT_ID,
                SpellComponentRegistry.LIVING_LAND_ID,
                AuthorshipRegistry.BANISH_ID
        )) {
            assertTrue(IneffableAffinityErosion.isIneffable(spellWith(id)), id.toString());
        }
    }

    @Test
    void rejectsOrdinaryAndNullSpells() {
        assertFalse(IneffableAffinityErosion.isIneffable(
                spellWith(new ResourceLocation("mna", "components/damage"))
        ));
        assertFalse(IneffableAffinityErosion.isIneffable(null));
    }

    @Test
    void anySingleIneffableEffectMakesAMixedSpellQualify() {
        ISpellDefinition mixed = spellWith(
                new ResourceLocation("mna", "components/damage"),
                SpellComponentRegistry.TRUE_DAMAGE_ID
        );

        assertTrue(IneffableAffinityErosion.isIneffable(mixed));
        assertFalse(IneffableAffinityErosion.shouldApplyOrdinaryAffinity(mixed));
    }

    private static ISpellDefinition spellWith(ResourceLocation... ids) {
        Set<ResourceLocation> present = Set.of(ids);
        return (ISpellDefinition) Proxy.newProxyInstance(
                ISpellDefinition.class.getClassLoader(),
                new Class<?>[]{ISpellDefinition.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("containsPart")) {
                        return present.contains((ResourceLocation) args[0]);
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0F;
        if (type == double.class) return 0.0D;
        if (type == char.class) return '\0';
        throw new AssertionError("Unhandled primitive " + type);
    }
}
```

- [ ] **Step 2: Run the classification tests and verify they fail**

Run:

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.spell.IneffableAffinityErosionTest"
```

Expected: compilation fails because `IneffableAffinityErosion` does not exist.

- [ ] **Step 3: Implement the classifier minimally**

Create the service with the stable component set and null-safe classification:

```java
package com.vincenthuto.mnagnosis.common.spell;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.spells.base.ISpellDefinition;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

public final class IneffableAffinityErosion {
    public static final float EROSION_PER_CAST = 0.1F;

    private static final Set<ResourceLocation> INEFFABLE_EFFECTS = Set.of(
            SpellComponentRegistry.TRUE_DAMAGE_ID,
            SpellComponentRegistry.GRAVITY_CONVERGENCE_ID,
            SpellComponentRegistry.GRAVITY_SHIFT_ID,
            SpellComponentRegistry.LIVING_LAND_ID,
            AuthorshipRegistry.BANISH_ID
    );

    private static final List<Affinity> CORE_AFFINITIES = List.of(
            Affinity.ARCANE,
            Affinity.EARTH,
            Affinity.ENDER,
            Affinity.FIRE,
            Affinity.WATER,
            Affinity.WIND
    );

    private IneffableAffinityErosion() {
    }

    public static boolean isIneffable(ISpellDefinition spell) {
        return spell != null
                && INEFFABLE_EFFECTS.stream().anyMatch(spell::containsPart);
    }

    public static boolean shouldApplyOrdinaryAffinity(ISpellDefinition spell) {
        return !isIneffable(spell);
    }

    static float erodedDepth(float currentDepth) {
        return Math.max(0.0F, currentDepth - EROSION_PER_CAST);
    }

    public static boolean erode(IPlayerMagic magic) {
        throw new UnsupportedOperationException("implemented by the next test");
    }
}
```

- [ ] **Step 4: Run the classification tests and verify they pass**

Run:

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.spell.IneffableAffinityErosionTest"
```

Expected: all three classification tests pass.

- [ ] **Step 5: Add failing erosion and clamping tests**

Add these tests and proxy helper to the same test class:

```java
@Test
void erodesOnlyCoreAffinitiesAndSyncsOnce() {
    EnumMap<Affinity, Float> depths = depthsAt(5.0F);
    depths.put(Affinity.BLOOD, 7.0F);
    depths.put(Affinity.HELLFIRE, 8.0F);
    depths.put(Affinity.ICE, 9.0F);
    depths.put(Affinity.LIGHTNING, 10.0F);
    depths.put(Affinity.UNKNOWN, 11.0F);
    AtomicInteger syncs = new AtomicInteger();

    assertTrue(IneffableAffinityErosion.erode(magic(depths, syncs)));

    for (Affinity affinity : Affinity.CoreSix()) {
        assertEquals(4.9F, depths.get(affinity), 0.0001F);
    }
    assertEquals(7.0F, depths.get(Affinity.BLOOD), 0.0001F);
    assertEquals(8.0F, depths.get(Affinity.HELLFIRE), 0.0001F);
    assertEquals(9.0F, depths.get(Affinity.ICE), 0.0001F);
    assertEquals(10.0F, depths.get(Affinity.LIGHTNING), 0.0001F);
    assertEquals(11.0F, depths.get(Affinity.UNKNOWN), 0.0001F);
    assertEquals(1, syncs.get());
}

@Test
void clampsAtZeroAndDoesNotSyncWhenNothingChanges() {
    assertEquals(0.0F, IneffableAffinityErosion.erodedDepth(0.05F), 0.0001F);

    EnumMap<Affinity, Float> depths = depthsAt(0.0F);
    AtomicInteger syncs = new AtomicInteger();
    assertFalse(IneffableAffinityErosion.erode(magic(depths, syncs)));
    assertEquals(0, syncs.get());
}

private static EnumMap<Affinity, Float> depthsAt(float depth) {
    EnumMap<Affinity, Float> depths = new EnumMap<>(Affinity.class);
    for (Affinity affinity : Affinity.values()) depths.put(affinity, depth);
    return depths;
}

private static IPlayerMagic magic(
        EnumMap<Affinity, Float> depths,
        AtomicInteger syncs
) {
    return (IPlayerMagic) Proxy.newProxyInstance(
            IPlayerMagic.class.getClassLoader(),
            new Class<?>[]{IPlayerMagic.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getAffinityDepth" -> depths.get((Affinity) args[0]);
                case "setAffinityDepth" -> {
                    depths.put((Affinity) args[0], (Float) args[1]);
                    yield null;
                }
                case "forceSync" -> {
                    syncs.incrementAndGet();
                    yield null;
                }
                default -> defaultValue(method.getReturnType());
            }
    );
}
```

- [ ] **Step 6: Run the erosion tests and verify they fail**

Run:

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.spell.IneffableAffinityErosionTest"
```

Expected: both erosion tests fail with `UnsupportedOperationException`.

- [ ] **Step 7: Implement clamped mutation and conditional synchronization**

Replace the placeholder `erode` body:

```java
public static boolean erode(IPlayerMagic magic) {
    if (magic == null) {
        return false;
    }

    boolean changed = false;
    for (Affinity affinity : CORE_AFFINITIES) {
        float current = magic.getAffinityDepth(affinity);
        float eroded = erodedDepth(current);
        if (Float.compare(current, eroded) != 0) {
            magic.setAffinityDepth(affinity, eroded);
            changed = true;
        }
    }
    if (changed) {
        magic.forceSync();
    }
    return changed;
}
```

- [ ] **Step 8: Run the focused tests**

Run:

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.spell.IneffableAffinityErosionTest"
```

Expected: all five tests pass.

- [ ] **Step 9: Commit the service**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/spell/IneffableAffinityErosion.java src/test/java/com/vincenthuto/mnagnosis/common/spell/IneffableAffinityErosionTest.java
git commit -m "feat: define ineffable affinity erosion"
```

### Task 2: M&A award-path integration

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/mixin/core/SpellCasterMixin.java`
- Modify: `src/main/resources/mnagnosis.mixins.json`
- Test: `src/main/java/com/vincenthuto/mnagnosis/gametest/IneffableAffinityErosionGameTests.java`

**Interfaces:**
- Consumes:
  - `IneffableAffinityErosion.isIneffable(ISpellDefinition): boolean`
  - `IneffableAffinityErosion.shouldApplyOrdinaryAffinity(ISpellDefinition): boolean`
  - `IneffableAffinityErosion.erode(IPlayerMagic): boolean`
  - M&A `SpellCaster.AddAffinityAndMagicXP(SpellRecipe, Player, int): void`
- Produces:
  - One server-side erosion call per qualifying award transaction.
  - No `IPlayerMagic.shiftAffinity` calls for a qualifying transaction.
  - Unmodified upstream execution for magic XP, rote XP, and ordinary spells.

- [ ] **Step 1: Write failing GameTests for the award transaction**

Create a new `@GameTestHolder(MnAGnosis.MODID)` class. The first test uses a
mixed spell and multiple ineffable effects to establish all of these at once:
qualification, one erosion, suppression of ordinary gain, non-core
preservation, and retained magic XP.

```java
package com.vincenthuto.mnagnosis.gametest;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.spell.SpellComponentRegistry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class IneffableAffinityErosionGameTests {
    private static final float EPSILON = 0.0001F;

    private IneffableAffinityErosionGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void mixedMultiEffectSpellErodesOnceAndKeepsXp(
            GameTestHelper helper
    ) {
        Player player = helper.makeMockSurvivalPlayer();
        var magic = player.getCapability(PlayerMagicProvider.MAGIC).orElseThrow();
        for (Affinity affinity : Affinity.values()) {
            magic.setAffinityDepth(affinity, 5.0F);
        }
        int xpBefore = magic.getMagicXP();
        SpellRecipe spell = new SpellRecipe(
                Shapes.SELF, SpellComponentRegistry.TRUE_DAMAGE
        ).addComponent(SpellComponentRegistry.LIVING_LAND)
                .addComponent(Components.DAMAGE);

        SpellCaster.AddAffinityAndMagicXP(spell, player);

        for (Affinity affinity : Affinity.CoreSix()) {
            helper.assertTrue(
                    Math.abs(magic.getAffinityDepth(affinity) - 4.9F) < EPSILON,
                    affinity + " was shifted normally or eroded more than once"
            );
        }
        helper.assertTrue(
                Math.abs(magic.getAffinityDepth(Affinity.BLOOD) - 5.0F) < EPSILON
                        && Math.abs(magic.getAffinityDepth(Affinity.HELLFIRE) - 5.0F) < EPSILON
                        && Math.abs(magic.getAffinityDepth(Affinity.ICE) - 5.0F) < EPSILON
                        && Math.abs(magic.getAffinityDepth(Affinity.LIGHTNING) - 5.0F) < EPSILON
                        && Math.abs(magic.getAffinityDepth(Affinity.UNKNOWN) - 5.0F) < EPSILON,
                "An ineffable cast changed a non-core affinity"
        );
        helper.assertTrue(
                magic.getMagicXP() > xpBefore,
                "The integration skipped Mana and Artifice magic XP"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ordinarySpellRetainsOrdinaryAffinityBehavior(
            GameTestHelper helper
    ) {
        Player player = helper.makeMockSurvivalPlayer();
        var magic = player.getCapability(PlayerMagicProvider.MAGIC).orElseThrow();
        for (Affinity affinity : Affinity.values()) {
            magic.setAffinityDepth(affinity, 5.0F);
        }
        SpellRecipe spell = new SpellRecipe(Shapes.SELF, Components.DAMAGE);

        SpellCaster.AddAffinityAndMagicXP(spell, player);

        boolean allUniformlyEroded = true;
        for (Affinity affinity : Affinity.CoreSix()) {
            allUniformlyEroded &= Math.abs(
                    magic.getAffinityDepth(affinity) - 4.9F
            ) < EPSILON;
        }
        helper.assertTrue(
                !allUniformlyEroded,
                "An ordinary spell received ineffable erosion"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void invalidSpellDoesNotErode(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        var magic = player.getCapability(PlayerMagicProvider.MAGIC).orElseThrow();
        for (Affinity affinity : Affinity.CoreSix()) {
            magic.setAffinityDepth(affinity, 5.0F);
        }

        SpellCaster.AddAffinityAndMagicXP(new SpellRecipe(), player);

        for (Affinity affinity : Affinity.CoreSix()) {
            helper.assertTrue(
                    Math.abs(magic.getAffinityDepth(affinity) - 5.0F) < EPSILON,
                    "An invalid spell eroded " + affinity
            );
        }
        helper.succeed();
    }
}
```

- [ ] **Step 2: Compile the GameTests before adding the Mixin**

Run:

```powershell
.\gradlew.bat compileJava
```

Expected: compilation passes. The GameTests are intentionally red at runtime
because no integration invokes the service yet.

- [ ] **Step 3: Implement the narrow Mixin**

Create the Mixin targeting the single M&A lambda that performs magic XP and
affinity work. The `@Inject` runs once before M&A checks the Affinity Lock
belt. The `@Redirect` allows ordinary casts through and drops every normal
shift for a qualifying spell:

```java
package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import com.vincenthuto.mnagnosis.common.spell.IneffableAffinityErosion;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpellCaster.class, remap = false, priority = 1000)
public abstract class SpellCasterMixin {

    @Inject(
            method = "lambda$AddAffinityAndMagicXP$16",
            at = @At("HEAD"),
            require = 1
    )
    private static void mnagnosis$erodeCoreAffinities(
            SpellRecipe spell,
            int channelTicks,
            IPlayerMagic magic,
            Player player,
            IPlayerProgression progression,
            CallbackInfo callback
    ) {
        if (!player.level().isClientSide
                && IneffableAffinityErosion.isIneffable(spell)) {
            IneffableAffinityErosion.erode(magic);
        }
    }

    @Redirect(
            method = "lambda$AddAffinityAndMagicXP$16",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mna/api/capabilities/IPlayerMagic;"
                            + "shiftAffinity(Lnet/minecraft/world/entity/player/Player;"
                            + "Lcom/mna/api/affinity/Affinity;F)V"
            ),
            require = 1
    )
    private static void mnagnosis$replaceOrdinaryAffinity(
            IPlayerMagic magic,
            Player player,
            Affinity affinity,
            float amount,
            SpellRecipe spell,
            int channelTicks,
            IPlayerMagic lambdaMagic,
            Player lambdaPlayer,
            IPlayerProgression progression
    ) {
        if (IneffableAffinityErosion.shouldApplyOrdinaryAffinity(spell)) {
            magic.shiftAffinity(player, affinity, amount);
        }
    }
}
```

If the compiler rejects the enclosing-argument suffix on the redirect handler,
inspect the generated target descriptor with:

```powershell
$mnaJar = 'C:\Users\Vince\.gradle\caches\forge_gradle\deobf_dependencies\curse\maven\mana-and-artifice-406360\6847805_mapped_parchment_2023.09.03-1.20.1\mana-and-artifice-406360-6847805_mapped_parchment_2023.09.03-1.20.1.jar'
javap -classpath $mnaJar -p -c com.mna.spells.SpellCaster
```

Keep the target method and `require = 1` exact; adjust only the redirect
handler's captured enclosing arguments to the descriptor reported by Mixin.

- [ ] **Step 4: Register the Mixin while preserving the dirty config**

Add exactly this entry to the existing `"mixins"` array near the other
`core.*` entries:

```json
"core.SpellCasterMixin",
```

Before editing, inspect both the working-tree and index versions:

```powershell
git diff -- src/main/resources/mnagnosis.mixins.json
git diff --cached -- src/main/resources/mnagnosis.mixins.json
```

Do not reorder or remove the gravity-related entries already present in the
working tree.

- [ ] **Step 5: Compile and run the focused unit suite**

Run:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.spell.IneffableAffinityErosionTest"
```

Expected: Mixin compilation succeeds and all service tests pass.

- [ ] **Step 6: Add the Affinity Lock belt GameTest**

Add this test and imports for `ItemInit`, `ItemStack`, and `CuriosApi`:

```java
@GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
public static void affinityLockDoesNotPreventErosion(GameTestHelper helper) {
    Player player = helper.makeMockSurvivalPlayer();
    var magic = player.getCapability(PlayerMagicProvider.MAGIC).orElseThrow();
    for (Affinity affinity : Affinity.CoreSix()) {
        magic.setAffinityDepth(affinity, 5.0F);
    }
    var curios = CuriosApi.getCuriosInventory(player).orElseThrow();
    curios.setEquippedCurio(
            "belt", 0, new ItemStack(ItemInit.BELT_AFFINITY_LOCK.get())
    );
    SpellRecipe spell = new SpellRecipe(
            Shapes.SELF, SpellComponentRegistry.TRUE_DAMAGE
    );

    SpellCaster.AddAffinityAndMagicXP(spell, player);

    for (Affinity affinity : Affinity.CoreSix()) {
        helper.assertTrue(
                Math.abs(magic.getAffinityDepth(affinity) - 4.9F) < EPSILON,
                "The Affinity Lock belt prevented erosion of " + affinity
        );
    }
    helper.succeed();
}
```

- [ ] **Step 7: Run the Forge GameTest server**

Run:

```powershell
.\gradlew.bat runGameTestServer
```

Expected:

- `mixedMultiEffectSpellErodesOnceAndKeepsXp` passes.
- `ordinarySpellRetainsOrdinaryAffinityBehavior` passes.
- `invalidSpellDoesNotErode` passes.
- `affinityLockDoesNotPreventErosion` passes.
- All pre-existing MnAGnosis GameTests pass.

If a pre-existing test fails, distinguish a genuine regression from an
unrelated dirty-worktree failure using the first exception and the named
GameTest in `run/logs/latest.log`; do not weaken the new assertions.

- [ ] **Step 8: Commit the integration**

Review the config hunk before staging it:

```powershell
git diff -- src/main/resources/mnagnosis.mixins.json
git add -- src/main/java/com/vincenthuto/mnagnosis/mixin/core/SpellCasterMixin.java src/main/java/com/vincenthuto/mnagnosis/gametest/IneffableAffinityErosionGameTests.java
git add -p -- src/main/resources/mnagnosis.mixins.json
git diff --cached --check
git commit -m "feat: erode affinities on ineffable casts"
```

Stage only the new `core.SpellCasterMixin` JSON line from the already-dirty
Mixin config.

### Task 3: Full verification and handoff

**Files:**
- Verify only; no planned source changes.

**Interfaces:**
- Consumes: the service, Mixin integration, unit tests, and GameTests from
  Tasks 1 and 2.
- Produces: evidence that the build and behavior meet the approved spec.

- [ ] **Step 1: Run the complete unit suite**

Run:

```powershell
.\gradlew.bat test
```

Expected: `BUILD SUCCESSFUL` with all JUnit tests passing.

- [ ] **Step 2: Run a clean compile**

Run:

```powershell
.\gradlew.bat clean compileJava
```

Expected: `BUILD SUCCESSFUL`; Mixin annotation processing reports no missing
target or descriptor.

- [ ] **Step 3: Re-run the complete GameTest suite after the clean build**

Run:

```powershell
.\gradlew.bat runGameTestServer
```

Expected: all MnAGnosis GameTests pass, including all four
`IneffableAffinityErosionGameTests`.

- [ ] **Step 4: Inspect the final scoped diff**

Run:

```powershell
git status --short
git diff HEAD~2 -- src/main/java/com/vincenthuto/mnagnosis/common/spell/IneffableAffinityErosion.java src/test/java/com/vincenthuto/mnagnosis/common/spell/IneffableAffinityErosionTest.java src/main/java/com/vincenthuto/mnagnosis/mixin/core/SpellCasterMixin.java src/main/java/com/vincenthuto/mnagnosis/gametest/IneffableAffinityErosionGameTests.java src/main/resources/mnagnosis.mixins.json
```

Expected: only the planned service, tests, Mixin, and one additive Mixin-config
entry belong to this feature. Existing unrelated working-tree changes remain
uncommitted and intact.

- [ ] **Step 5: Record final evidence**

Report:

- the two feature commit hashes;
- the JUnit result;
- the clean compile result;
- the GameTest result;
- any unrelated pre-existing failures, with the first relevant log path and
  exception;
- confirmation that unrelated dirty-worktree edits were preserved.
