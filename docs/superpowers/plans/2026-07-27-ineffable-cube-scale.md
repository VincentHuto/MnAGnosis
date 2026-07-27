# Ineffable Cube Scale Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reduce all outlined Ineffable cube particles to quarter scale without changing their appearance or behavior.

**Architecture:** Keep the shared `OutlinedCubeParticle` renderer and every emitter unchanged. Move its base half-size calculation into a small side-neutral helper so the existing GameTest suite can enforce the approved maximum, then change only the random range.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1 particle API, Forge GameTest.

## Global Constraints

- Full cube width must remain between `0.038F` and `0.060F` blocks.
- Do not change particle count, lifetime, velocity, rotation, variants, textures, or integrations.
- Preserve all unrelated working-tree changes.

---

### Task 1: Lock and reduce particle scale

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/particle/IneffableParticleScale.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/particle/OutlinedCubeParticle.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `public static float baseHalfSize(float randomUnit)` returning a value from `0.019F` through `0.030F`.
- Consumes: `RandomSource.nextFloat()` in `OutlinedCubeParticle`.

- [ ] **Step 1: Write the failing regression test**

```java
@GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
public static void ineffableCubeParticlesStayAtQuarterScale(GameTestHelper helper) {
    helper.assertTrue(
            Math.abs(IneffableParticleScale.baseHalfSize(0.0F) - 0.019F) < 0.00001F,
            "Minimum cube half-size must remain at quarter scale"
    );
    helper.assertTrue(
            IneffableParticleScale.baseHalfSize(1.0F) * 2.0F <= 0.060F,
            "Maximum cube width must not exceed 0.060 blocks"
    );
    helper.succeed();
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run: `.\gradlew.bat compileJava`

Expected: compilation failure because `IneffableParticleScale` does not exist.

- [ ] **Step 3: Add the minimal scale helper**

```java
public final class IneffableParticleScale {
    public static float baseHalfSize(float randomUnit) {
        float normalized = Math.max(0.0F, Math.min(1.0F, randomUnit));
        return 0.019F + normalized * 0.011F;
    }
}
```

- [ ] **Step 4: Route the renderer through the helper**

Replace:

```java
this.quadSize = 0.075F + random.nextFloat() * 0.045F;
```

with:

```java
this.quadSize = IneffableParticleScale.baseHalfSize(random.nextFloat());
```

- [ ] **Step 5: Run focused and full verification**

Run:

```powershell
.\gradlew.bat runGameTestServer
.\gradlew.bat build
```

Expected: all required GameTests pass, including `ineffableCubeParticlesStayAtQuarterScale`, and build exits `0`.

- [ ] **Step 6: Commit only the scale fix**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/particle/IneffableParticleScale.java src/main/java/com/vincenthuto/mnagnosis/client/particle/OutlinedCubeParticle.java src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java
git commit --only -m "fix: shrink ineffable cube particles" -- src/main/java/com/vincenthuto/mnagnosis/common/particle/IneffableParticleScale.java src/main/java/com/vincenthuto/mnagnosis/client/particle/OutlinedCubeParticle.java src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java
```
