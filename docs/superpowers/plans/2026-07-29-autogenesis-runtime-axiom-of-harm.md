# Autogenesis Runtime and Axiom of Harm Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use
> superpowers:subagent-driven-development (recommended) or
> superpowers:executing-plans to implement this plan task-by-task. Steps use
> checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver the first Autogenesis vertical slice: a foundation-aligned
cast runtime, the Axiom of Harm modifier, and the first Definition proof that
advances the Living Manuscript to Intervention.

**Architecture:** Reuse `AuthoredCastSessionStore`, `AuthorshipCastPermit`, and
`AuthoredComponentPipeline` rather than recreating the shared foundation.
Prepare an immutable Axiom selection during server mana calculation, wrap the
native M&A component call through the shared pipeline, and authorize exactly
one audited immunity check through a private thread-local scope. Grant the
Definition proof only when that scope records both a consumed immunity gate and
a successful native harm result.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.0, Mana and Artifice
3.1.11, Sponge Mixin 0.8.5, JUnit 5.10.2, Forge GameTest

## Global Constraints

- Work directly on the current `master` branch, as explicitly requested.
- Preserve all unrelated uncommitted work and stage only task-owned paths.
- Keep `NetworkProtocol.CURRENT` at `"5"` and register no packet.
- Use the real M&A registry IDs `mna:fire_damage` and `mna:poison`.
- Axiom ID is exactly `mnagnosis:axiom_of_harm`.
- Definition proof ID is exactly
  `mnagnosis:definition/axiom_of_harm`.
- Apply the Axiom mana multiplier exactly once as `incomingCost * 1.35F`.
- Axiom crosses only fire-type immunity or the
  `IGNORES_POISON_AND_REGEN` poison gate.
- No production code may precede its failing test.

---

## File Structure

Create focused runtime and harm packages:

```text
src/main/java/com/vincenthuto/mnagnosis/common/autogenic/
├── AutogenicAccess.java
├── AutogenicBootstrap.java
├── AutogenicCastRuntime.java
├── AutogenicComponentRuntime.java
├── AutogenicProgression.java
├── AutogenicSpellClassifier.java
└── harm/
    ├── AxiomOfHarmDecorator.java
    ├── AxiomOfHarmMana.java
    ├── AxiomOfHarmModifier.java
    ├── AxiomOfHarmSelection.java
    ├── FireDamageHarmAdapter.java
    ├── HarmAdapter.java
    ├── HarmAdapterRegistry.java
    ├── HarmGate.java
    ├── HarmInvocationScope.java
    ├── HarmSelection.java
    ├── HarmSelectionDecision.java
    ├── HarmTargetDecision.java
    ├── HarmTargetPolicy.java
    └── PoisonHarmAdapter.java

src/main/java/com/vincenthuto/mnagnosis/mixin/autogenic/
├── ComponentFireDamageHarmMixin.java
├── EntityFireImmunityMixin.java
├── LivingEntityPoisonImmunityMixin.java
└── PotionEffectComponentHarmMixin.java
```

Modify only these integration boundaries:

```text
src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java
src/main/java/com/vincenthuto/mnagnosis/common/authorship/AuthorshipEvents.java
src/main/java/com/vincenthuto/mnagnosis/common/progression/manuscript/ManuscriptDefinitions.java
src/main/java/com/vincenthuto/mnagnosis/common/spell/SpellComponentRegistry.java
src/main/java/com/vincenthuto/mnagnosis/mixin/core/SpellCasterMixin.java
src/main/resources/mnagnosis.mixins.json
src/main/resources/assets/mnagnosis/lang/en_us.json
```

### Task 1: Frozen harm adapters and deterministic selection

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmGate.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmAdapter.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmAdapterRegistry.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmSelection.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmSelectionDecision.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/FireDamageHarmAdapter.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/PoisonHarmAdapter.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmSelection.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicSpellClassifier.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmAdapterRegistryTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmSelectionTest.java`

**Interfaces:**
- Produces:

```java
enum HarmGate {
    FIRE_TYPE_IMMUNITY,
    UNDEAD_POISON_IMMUNITY
}

interface HarmAdapter<T extends SpellEffect> {
    ResourceLocation id();
    ResourceLocation componentId();
    Class<T> componentType();
    HarmGate gate();
}

record HarmSelection(
        int componentIndex,
        ResourceLocation componentId,
        ResourceLocation adapterId,
        HarmGate gate
) {}

record HarmSelectionDecision(
        Optional<HarmSelection> selection,
        Failure failure
) {
    enum Failure { NONE, ORDER_UNAVAILABLE, NO_COMPATIBLE_HARM }
}
```

- `HarmAdapterRegistry` is instance-owned, uses insertion-ordered maps,
  rejects duplicate adapter IDs and component IDs, rejects registration after
  freeze, and resolves only when frozen.
- `AxiomOfHarmSelection` receives a `HarmAdapterRegistry` plus a
  `Function<SpellEffect, ResourceLocation>` identity resolver. Production uses
  `SpellEffect::getRegistryName`; tests use literal identities.

- [ ] **Step 1: Write failing adapter and selection tests**

`HarmAdapterRegistryTest` covers exact ID/class/gate resolution, wrong-class
rejection, unknown IDs, duplicate adapter IDs, duplicate component IDs,
unfrozen resolution, mutation after freeze, and immutable snapshots.

`AxiomOfHarmSelectionTest` uses real `SpellRecipe` ordering and literal test
IDs. It covers unsupported harm followed by poison followed by fire, friendly
and neutral parts, duplicate compatible parts selecting index zero, null
component entries, missing identity, unfrozen registry, and True Damage alone.
The production change each test catches is an incorrect selection, widened
compatibility, or fail-open malformed-spell branch.

- [ ] **Step 2: Run the tests and confirm RED**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.autogenic.harm.HarmAdapterRegistryTest" --tests "com.vincenthuto.mnagnosis.common.autogenic.harm.AxiomOfHarmSelectionTest"
```

Expected: test compilation fails because the harm registry and selector do not
exist.

- [ ] **Step 3: Implement the minimum registry and selector**

Use exact built-in values:

```java
FireDamageHarmAdapter.ID = MnAGnosis.rloc("mna_fire_damage");
FireDamageHarmAdapter.COMPONENT_ID =
        ResourceLocation.fromNamespaceAndPath("mna", "fire_damage");
PoisonHarmAdapter.ID = MnAGnosis.rloc("mna_poison");
PoisonHarmAdapter.COMPONENT_ID =
        ResourceLocation.fromNamespaceAndPath("mna", "poison");
```

Scan an immutable copy of `spell.getComponents()` by integer index. A null
list, null part, or null component ID returns `ORDER_UNAVAILABLE`. A complete
scan with no exact registered match returns `NO_COMPATIBLE_HARM`.

- [ ] **Step 4: Run focused tests and compile**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.autogenic.harm.HarmAdapterRegistryTest" --tests "com.vincenthuto.mnagnosis.common.autogenic.harm.AxiomOfHarmSelectionTest"
.\gradlew.bat compileJava
```

- [ ] **Step 5: Commit Task 1**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm src/main/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicSpellClassifier.java src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmAdapterRegistryTest.java src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmSelectionTest.java
git diff --cached --check
git commit -m "feat: select Axiom harm gates"
```

### Task 2: Target policy and single-use native invocation scope

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmTargetDecision.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmTargetPolicy.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmInvocationScope.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmTargetPolicyTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmInvocationScopeTest.java`

**Interfaces:**

```java
enum HarmTargetDecision {
    ALLOW,
    INVALID_TARGET,
    ABSOLUTE_PROTECTION,
    ALLIED,
    PVP_DENIED
}

record HarmTargetFacts(
        boolean present,
        boolean alive,
        boolean removed,
        boolean loaded,
        boolean sameDimension,
        boolean invulnerable,
        boolean creativeOrSpectator,
        boolean allied,
        boolean pvpAllowed
) {}
```

`HarmTargetPolicy.evaluate(SpellSource, LivingEntity)` extracts facts and calls
the pure `evaluate(HarmTargetFacts)` branch. The pure overload makes every
policy branch testable without mocking Minecraft.

`HarmInvocationScope.open(...)` binds permit ID, component index and ID, part
identity, context identity, target UUID, adapter ID, and gate. Its
`invokeDamage` and `invokeEffect` bridges bind the exact native
`DamageSource`/`MobEffectInstance`, call the real native method, record the
boolean result, and close in `finally`. Gate queries consume authorization
once. `Outcome` reports `gateConsumed`, `nativeSucceeded`, and `gate`.

- [ ] **Step 1: Write failing policy and scope tests**

Policy tests cover each enum result with literal `HarmTargetFacts`.

Scope tests cover exact-match consumption, second-use rejection, different
target/context/part/native-object rejection, nested owner isolation, two
targets in one cast, native false results, and exception cleanup. Tests use
real `AuthorshipCastPermit` values and package-private functional seams that
return literal native results; they assert `Outcome`, never mock call counts.

- [ ] **Step 2: Run the tests and confirm RED**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.autogenic.harm.HarmTargetPolicyTest" --tests "com.vincenthuto.mnagnosis.common.autogenic.harm.HarmInvocationScopeTest"
```

Expected: compilation fails because policy and scope types do not exist.

- [ ] **Step 3: Implement policy and scope**

Use one `ThreadLocal<Deque<Frame>>`. Keep frame mutation private. Public Mixin
bridges may only bind a native call or query the exact active gate. No method
may open arbitrary authorization without a complete `AuthorshipCastPermit` and
decorated component context.

- [ ] **Step 4: Run focused tests and compile**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.autogenic.harm.HarmTargetPolicyTest" --tests "com.vincenthuto.mnagnosis.common.autogenic.harm.HarmInvocationScopeTest"
.\gradlew.bat compileJava
```

- [ ] **Step 5: Commit Task 2**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmTargetDecision.java src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmTargetPolicy.java src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmInvocationScope.java src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmTargetPolicyTest.java src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/HarmInvocationScopeTest.java
git diff --cached --check
git commit -m "feat: scope Axiom immunity authorization"
```

### Task 3: Foundation-aligned cast runtime, modifier, and mana surcharge

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicAccess.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicComponentRuntime.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicCastRuntime.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicBootstrap.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmMana.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmModifier.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmDecorator.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/authorship/AuthorshipEvents.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/SpellComponentRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/mixin/core/SpellCasterMixin.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicComponentRuntimeTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmManaTest.java`

**Interfaces:**

`AutogenicCastRuntime` owns an
`AuthoredCastSessionStore<PreparedAutogenicCast>`. Mana preparation:

```java
float prepareManaCost(ServerPlayer player, ISpellDefinition spell, float cost)
```

forgets any prior session when Axiom is absent, rejects inaccessible or invalid
Axiom with `Float.MAX_VALUE`, otherwise prepares the selected adapter and
returns `AxiomOfHarmMana.adjustedCost(cost)`.

`AutogenicComponentRuntime` wraps one
`AuthoredComponentPipeline<ComponentApplicationResult>` and a thread-local
immutable invocation context. Its no-Axiom path invokes the native supplier
directly. Recursive entry relies on the shared pipeline guard.

`SpellCasterMixin` redirects only the call to
`SpellEffect.ApplyEffect(...)` inside
`SpellCaster.lambda$ApplyComponents$13` and delegates to
`AutogenicCastRuntime.applyComponent(...)`.

- [ ] **Step 1: Write failing pipeline and mana tests**

Tests prove ordinary components call native exactly once, Axiom decoration
calls native exactly once, recursive entry bypasses decoration, exceptions
clear context, `100F` becomes `135F`, duplicate Axiom modifiers remain `135F`,
and non-finite/negative costs fail closed.

- [ ] **Step 2: Run the tests and confirm RED**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.autogenic.AutogenicComponentRuntimeTest" --tests "com.vincenthuto.mnagnosis.common.autogenic.harm.AxiomOfHarmManaTest"
```

- [ ] **Step 3: Implement runtime and modifier integration**

Register:

```java
public static final ResourceLocation AXIOM_OF_HARM_ID =
        MnAGnosis.rloc("axiom_of_harm");
public static final AxiomOfHarmModifier AXIOM_OF_HARM =
        new AxiomOfHarmModifier(
                MnAGnosis.rloc("textures/spell/modifier/axiom_of_harm.png")
        );
```

In `AuthorshipEvents.calculateManaCost`, run existing authorship calculation
first, then `AutogenicCastRuntime.prepareManaCost`. In `finishCast`, finalize
existing authorship state and forget the Autogenic session in `finally`.
`AutogenicBootstrap.bootstrap()` registers both adapters, freezes the registry,
and registers one Axiom decorator. Call it from queued common setup before
network installation.

- [ ] **Step 4: Run focused tests, authorship regressions, and compile**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.autogenic.*" --tests "com.vincenthuto.mnagnosis.common.authorship.*"
.\gradlew.bat compileJava
```

- [ ] **Step 5: Commit Task 3**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/autogenic src/main/java/com/vincenthuto/mnagnosis/common/authorship/AuthorshipEvents.java src/main/java/com/vincenthuto/mnagnosis/common/spell/SpellComponentRegistry.java src/main/java/com/vincenthuto/mnagnosis/mixin/core/SpellCasterMixin.java src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java src/test/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicComponentRuntimeTest.java src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmManaTest.java
git diff --cached --check
git commit -m "feat: add Autogenesis cast runtime"
```

### Task 4: Cross only the two audited native immunity checks

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/mixin/autogenic/ComponentFireDamageHarmMixin.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/mixin/autogenic/EntityFireImmunityMixin.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/mixin/autogenic/PotionEffectComponentHarmMixin.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/mixin/autogenic/LivingEntityPoisonImmunityMixin.java`
- Modify: `src/main/resources/mnagnosis.mixins.json`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmMixinBoundaryTest.java`

**Interfaces:**

- `ComponentFireDamageHarmMixin` redirects the sole `Entity.hurt` call in
  `ComponentFireDamage.ApplyEffect` through
  `HarmInvocationScope.invokeDamage`.
- `EntityFireImmunityMixin` redirects only `Entity.fireImmune()` inside
  `Entity.isInvulnerableTo(DamageSource)`. It returns false only when the exact
  scoped entity and `DamageSource` consume `FIRE_TYPE_IMMUNITY`; removed,
  general invulnerability, fall immunity, and Forge hooks remain native.
- `PotionEffectComponentHarmMixin` redirects the sole
  `LivingEntity.addEffect(MobEffectInstance)` call through
  `HarmInvocationScope.invokeEffect`.
- `LivingEntityPoisonImmunityMixin` redirects only the
  `EntityType.is(EntityTypeTags.IGNORES_POISON_AND_REGEN)` check in
  `LivingEntity.canBeAffected`. It returns false only for the exact scoped
  poison instance and `UNDEAD_POISON_IMMUNITY`.

- [ ] **Step 1: Write failing behavioral boundary tests**

Test public scope bridges with real damage/effect identities and assert that
fire authorization does not permit poison, poison authorization does not
permit regeneration, different native objects fail, and absolute target
protection remains denied. Add a bytecode characterization assertion using
Mixin annotations only for `method`, target invocation, ordinal, and
`require = 1`; the behavior assertions remain the primary tests.

- [ ] **Step 2: Run the test and confirm RED**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.autogenic.harm.AxiomOfHarmMixinBoundaryTest"
```

- [ ] **Step 3: Implement the four strict Mixins**

Use `remap = false` only for M&A classes and calls. Keep normal remapping for
Minecraft `Entity` and `LivingEntity` Mixins. Every redirect has `require = 1`.
Add the four classes under the common `mixins` list.

- [ ] **Step 4: Run all Axiom tests and compile**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.autogenic.*"
.\gradlew.bat compileJava
```

- [ ] **Step 5: Commit Task 4**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/mixin/autogenic src/main/resources/mnagnosis.mixins.json src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmMixinBoundaryTest.java
git diff --cached --check
git commit -m "feat: cross audited Axiom immunity gates"
```

### Task 5: Definition proof and Living Manuscript Intervention

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicProgression.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/progression/manuscript/ManuscriptDefinitions.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmDecorator.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicProgressionTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/progression/manuscript/ManuscriptDefinitionsTest.java`

**Interfaces:**

```java
AutogenicProgression.AXIOM_OF_HARM_PROOF =
        MnAGnosis.rloc("definition/axiom_of_harm");

ProofGrantResult grantAxiomProof(
        IManuscriptState state,
        UUID evidenceId,
        long gameTime
);
```

The Definition progression definition accepts Revelation and Axiom proof IDs.
It returns Intervention only when both exist. Relation and Continuance keep
their Revelation-only, always-Perception definitions.

The decorator grants only when target policy allowed, authorization was
consumed, native result succeeded, and component result is `SUCCESS`. Evidence
is the target UUID and earned time is server game time.

- [ ] **Step 1: Write failing proof and stage tests**

Test Revelation alone → Perception, Axiom alone rejected as incomplete access,
Revelation plus Axiom → Intervention, repeated grant → `ALREADY_OWNED`, evidence
and time preservation, and Relation/Continuance unchanged.

- [ ] **Step 2: Run tests and confirm RED**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.autogenic.AutogenicProgressionTest" --tests "com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitionsTest"
```

- [ ] **Step 3: Implement proof registration and grant**

Keep `ManuscriptInitiationService.DEFAULT` using
`ManuscriptDefinitions.createRegistry()`, so initiation automatically sees the
expanded frozen Definition definition. Do not mutate the client or send a new
packet.

- [ ] **Step 4: Run progression, snapshot, and Axiom tests**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.progression.manuscript.*" --tests "com.vincenthuto.mnagnosis.common.autogenic.*" --tests "com.vincenthuto.mnagnosis.common.network.ManuscriptSnapshotPacketTest"
.\gradlew.bat compileJava
```

- [ ] **Step 5: Commit Task 5**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicProgression.java src/main/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmDecorator.java src/main/java/com/vincenthuto/mnagnosis/common/progression/manuscript/ManuscriptDefinitions.java src/test/java/com/vincenthuto/mnagnosis/common/autogenic/AutogenicProgressionTest.java src/test/java/com/vincenthuto/mnagnosis/common/progression/manuscript/ManuscriptDefinitionsTest.java
git diff --cached --check
git commit -m "feat: advance Definition through Axiom"
```

### Task 6: Resources, GameTests, and release verification

**Files:**
- Create: `src/main/resources/data/mnagnosis/recipes/axiom_of_harm.json`
- Create: `src/main/resources/assets/mnagnosis/textures/spell/modifier/axiom_of_harm.png`
- Modify: `src/main/resources/assets/mnagnosis/lang/en_us.json`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmResourceTest.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/gametest/AxiomOfHarmGameTests.java`

**Interfaces:**

Recipe:

```json
{
  "type": "mna:modifier",
  "tier": 6,
  "output": "mnagnosis:axiom_of_harm",
  "items": [
    "mnagnosis:tesseract",
    "minecraft:flint_and_steel",
    "minecraft:fermented_spider_eye",
    "minecraft:nether_star",
    "minecraft:black_concrete",
    "minecraft:white_concrete"
  ],
  "patterns": [
    "mna:manaweave_patterns/triangle",
    "mna:manaweave_patterns/inverted_triangle"
  ]
}
```

Translations:

```json
"modifier.mnagnosis.axiom_of_harm": "Axiom of Harm",
"modifier.mnagnosis.axiom_of_harm.description": "The first compatible harm denies one native immunity."
```

Reuse the existing Ineffable white/black spell-icon visual language to create a
distinct 16×16 or 32×32 PNG with transparent background; do not add a runtime
image dependency.

- [ ] **Step 1: Write failing resource and GameTest contracts**

The resource test parses JSON, verifies exact output/tier/type, validates all
referenced item IDs and patterns, reads the PNG through `ImageIO`, and asserts
nonzero dimensions and alpha. GameTests cover a fire-immune entity, an undead
poison target, already-vulnerable negative controls, and idempotent proof
projection.

- [ ] **Step 2: Run resource test and confirm RED**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.autogenic.harm.AxiomOfHarmResourceTest"
```

- [ ] **Step 3: Add resources and GameTests**

Create the recipe, icon, translations, and GameTest class. Use runtime spell
application paths in GameTests; do not directly call proof grant as a substitute
for crossing an immunity.

- [ ] **Step 4: Run focused and full verification**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.autogenic.*" --tests "com.vincenthuto.mnagnosis.common.progression.manuscript.*" --tests "com.vincenthuto.mnagnosis.common.authorship.*"
.\gradlew.bat test
.\gradlew.bat compileJava
.\gradlew.bat processResources
.\gradlew.bat runGameTestServer
.\gradlew.bat jar
jar tf build\libs\mnagnosis-1.2.0.jar | Select-String 'autogenic|axiom_of_harm'
```

Expected: all new focused tests and GameTests pass; full-suite status is
reported exactly from fresh output; compilation, resources, and JAR succeed;
the packaged JAR contains Axiom classes and resources; protocol remains `"5"`.

- [ ] **Step 5: Audit and commit Task 6**

```powershell
git diff --check
git status --short
git diff --name-only da9eda5..HEAD
rg -n 'PROTOCOL|CURRENT' src/main/java/com/vincenthuto/mnagnosis/common/network/NetworkProtocol.java
git add -- src/main/resources/data/mnagnosis/recipes/axiom_of_harm.json src/main/resources/assets/mnagnosis/textures/spell/modifier/axiom_of_harm.png src/main/resources/assets/mnagnosis/lang/en_us.json src/test/java/com/vincenthuto/mnagnosis/common/autogenic/harm/AxiomOfHarmResourceTest.java src/main/java/com/vincenthuto/mnagnosis/gametest/AxiomOfHarmGameTests.java
git diff --cached --check
git commit -m "feat: ship Axiom of Harm"
```
