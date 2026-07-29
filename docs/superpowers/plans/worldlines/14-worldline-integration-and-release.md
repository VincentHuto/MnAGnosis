# Worldline Integration and Release Implementation Plan

> **Superseded foundation tasks:** protocol remains `"5"`; Worldline packets
> are manifest entries in `48-63`; Remainders use typed external Contradictions;
> Continuance progression plugs into the shared Manuscript capability.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate all Worldline features into one coherent registry, compatibility matrix, resource set, migration path, and verified multiplayer release.

**Architecture:** Feature packages remain modular and register through the runtime created by Plan 00. This plan owns the final collision-prone edits to central registries, common resources, compatibility declarations, operator diagnostics, and complete regression evidence.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1/47.4, Mana and Artifice 3.1.11 API, Curios, JUnit 5, Forge GameTest, Gradle

## Global Constraints

- Plans 00–13 must be implemented and their focused tests green before this plan begins.
- Preserve registry IDs, attribute bounds, limits, and Remainder behavior exactly as specified by their owning plans.
- Do not collapse separate components into modes during integration.
- Do not add an Immersive Portals dependency, remote client worlds, packet redirection, recursive portal rendering, or persistent chunk tickets.
- Do not weaken permission checks, consent, recovery, immutable-ledger rules, packet bounds, or replay allowlists to make integration tests pass.
- Central registry and language conflicts are resolved here; feature packages remain the authority for behavior.
- Network protocol remains `"6"` with packet IDs 0–8; Worldline packet
  classes are exactly `WorldlineSessionUpsertS2C`,
  `WorldlineSessionRemoveS2C`, `WorldlineVisualBatchS2C`, and
  `WorldlineActionC2S`.
- The only accepted pre-existing unit-test failure is the named Yaldabaoth asset-contract `ClassCastException`.

---

## File Structure

Create:

- `src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineFeatureRegistry.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineCompatibility.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineUse.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineCompatibilityResult.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineDiagnostics.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineAudit.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineAuditEntry.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/command/WorldlineCommand.java`
- `src/main/java/com/vincenthuto/mnagnosis/client/worldline/WorldlineClientRegistration.java`
- `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineFeatureRegistryTest.java`
- `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineCompatibilityTest.java`
- `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineResourceContractTest.java`
- `src/main/java/com/vincenthuto/mnagnosis/gametest/WorldlineIntegrationGameTests.java`

Modify:

- `src/main/java/com/vincenthuto/mnagnosis/common/spell/SpellComponentRegistry.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/authorship/AuthorshipRegistry.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/registry/BlockRegistry.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/registry/BlockEntityRegistry.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/registry/SoundRegistry.java`
- `src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java`
- `src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java`
- `src/main/resources/assets/mnagnosis/lang/en_us.json`
- `src/main/resources/assets/mnagnosis/sounds.json`

Add or verify feature-owned recipes, tags, blockstates, item/block models,
spell icons, particles, shaders, sounds, dimension data, and loot tables
under `src/main/resources`.

### Task 1: Consolidate modular feature registration

**Files:**
- Create: `WorldlineFeatureRegistry.java`
- Modify: the central registries listed above.
- Test: `WorldlineFeatureRegistryTest.java`

**Interfaces:**
- Produces:

```java
public final class WorldlineFeatureRegistry {
    public static void registerAll();
    public static void registerSessionTypes();
    public static void registerRemainderTypes();
    public static void registerResolutionAdapters();
    public static void registerConsequenceAdapters();
    public static void registerMutationAdapters();
    public static void registerEntityAttributes(
            EntityAttributeCreationEvent event);
    public static Set<ResourceLocation> componentIds();
    public static Set<ResourceLocation> sessionTypeIds();
    public static Set<ResourceLocation> remainderTypeIds();
    public static Set<ResourceLocation> entityIds();
    public static Set<ResourceLocation> itemIds();
    public static Set<ResourceLocation> apparatusIds();
    public static Set<ResourceLocation> soundIds();
}
```

- [ ] **Step 1: Write a failing complete-ID contract test**

Assert the exact component IDs:

```text
mnagnosis:components/causal_bookmark
mnagnosis:components/path_memorial
mnagnosis:components/deferred_arrival
mnagnosis:components/continuation
mnagnosis:components/causal_relay
mnagnosis:components/still_point
mnagnosis:components/foregone_path
mnagnosis:components/revision_of_outcome
mnagnosis:components/counterfactual_emanation
mnagnosis:components/the_long_moment
```

Assert Shape `mnagnosis:worldline`, Law Inscription
`mnagnosis:law_recurrence`, Law `mnagnosis:recurrence`, Suspension
interpretation `mnagnosis:consequence_without_cause`, items
`mnagnosis:causal_spindle`, `mnagnosis:palimpsest_lens`,
`mnagnosis:unspent_moment`, and apparatus
`mnagnosis:unfinished_hourglass`.

Assert exact session type IDs:

```text
mnagnosis:deferred_arrival
mnagnosis:continuation
mnagnosis:causal_relay
mnagnosis:still_point
mnagnosis:recurrence
mnagnosis:foregone_path
mnagnosis:revision_of_outcome
mnagnosis:counterfactual_emanation
mnagnosis:long_moment
mnagnosis:unfinished_hour
```

Assert external Remainder type IDs
`mnagnosis:bookmark_recall`, `mnagnosis:revision_of_outcome`,
`mnagnosis:unfinished_hour_recur`, and
`mnagnosis:unfinished_hour_restore`. Recurrence and Consequence Without Cause
remain their owning Law's ordinary debts and do not appear in this external
type registry. Assert entity IDs `mnagnosis:worldline_carrier` and
`mnagnosis:white_effigy`.

Assert both resolution and consequence adapter registries contain exactly
`mnagnosis:magic_damage`, `mnagnosis:true_damage`,
`mnagnosis:finite_effect`, and `mnagnosis:force`. Assert the mutation
registry contains exactly `mnagnosis:property_transition`,
`mnagnosis:revision_placement`, `mnagnosis:revision_clean_break`, and
`mnagnosis:unfinished_hour_blocks`.

- [ ] **Step 2: Run the contract test and verify failure**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineFeatureRegistryTest"
```

Expected: the consolidated feature registry does not exist.

- [ ] **Step 3: Implement one-time modular registration**

`WorldlineFeatureRegistry.registerAll()` is called once from common setup
inside `enqueueWork`. In fixed order it invokes
`registerSessionTypes()`, `registerRemainderTypes()`,
`registerResolutionAdapters()`, `registerConsequenceAdapters()`, and
`registerMutationAdapters()`. Each phase consumes feature-owned handles,
registers each ID exactly once, and throws on a duplicate; no phase calls a
different phase implicitly.

Session registration consumes the `sessionType()` handles exposed by Plans
03 through 10, 12, and 13 and passes each to
`WorldlineSessionTypes.register`. Remainder registration consumes
`BookmarkRecallLifecycle.TYPE`, `RevisionFeature.remainderType()`, and
`UnfinishedHourFeature.remainderTypes()` and passes each to
`WorldlineRemainderTypes.register`. Mutation registration consumes the
Plan-00 property adapter, `RevisionFeature.mutationAdapters()`, and
`UnfinishedHourFeature.mutationAdapter()`. Forge content remains registered
through the repository's existing `DeferredRegister` or M&A `RegisterEvent`
patterns.

`EntityRegistry` registers `WorldlineCarrierEntity.builder()` under
`mnagnosis:worldline_carrier` and
`CounterfactualFeature.entityBuilder()` under `mnagnosis:white_effigy`.
`WorldlineFeatureRegistry.entityIds()` returns exactly those two IDs.
The common mod-bus `EntityAttributeCreationEvent` calls
`registerEntityAttributes(event)` once to bind only the White Effigy's
feature-owned attribute supplier; the non-living carrier has none.

Refactor `AuthorshipRegistry` from hard-coded inscription conditionals to an
immutable modifier-ID-to-law-ID map while preserving Inversion, Exchange, and
Suspension behavior:

```java
private static final Map<ResourceLocation, ResourceLocation> LAW_BY_INSCRIPTION =
        Map.of(
                LAW_INVERSION_ID, INVERSION_LAW_ID,
                LAW_EXCHANGE_ID, EXCHANGE_LAW_ID,
                LAW_SUSPENSION_ID, SUSPENSION_LAW_ID,
                LAW_RECURRENCE_ID, RECURRENCE_LAW_ID
        );
```

- [ ] **Step 4: Run the registry contract**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineFeatureRegistryTest"
```

Expected: every content, session, Remainder, adapter, entity, and sound ID is
present exactly once and existing IDs are unchanged.

- [ ] **Step 5: Commit registry integration**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineFeatureRegistry.java src/main/java/com/vincenthuto/mnagnosis/common/spell/SpellComponentRegistry.java src/main/java/com/vincenthuto/mnagnosis/common/authorship/AuthorshipRegistry.java src/main/java/com/vincenthuto/mnagnosis/common/registry src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineFeatureRegistryTest.java
git commit -m "feat: register worldline feature suite"
```

### Task 2: Enforce the cross-feature compatibility matrix

**Files:**
- Create: `WorldlineCompatibility.java`
- Create: `WorldlineUse.java`
- Create: `WorldlineCompatibilityResult.java`
- Test: `WorldlineCompatibilityTest.java`
- Modify: feature handlers only where a declaration is missing.

**Interfaces:**
- Produces:

```java
public enum WorldlineUse {
    SPELL_TARGET,
    SPATIAL_LEASE,
    REPLAY,
    CONSEQUENCE_TRANSFER,
    REVISION,
    HOUR_MUTATION,
    INTERVAL_ENTRY
}

public record WorldlineCompatibilityResult(
        boolean allowed,
        WorldlineFailure failure
) {}

public final class WorldlineCompatibility {
    public static WorldlineCompatibilityResult check(
            WorldlineUse use,
            ResourceLocation sourceId,
            Entity subject,
            Optional<WorldlineSpatialLease> existingLease
    );
}
```

- [ ] **Step 1: Write the failing matrix test**

Lock these decisions:

| Source | Recurrence | Suspension | Relay | Revision/Hour journal | Spatial lease |
|---|---|---|---|---|---|
| Causal Bookmark | rejected | rejected | rejected | observed only | requires no existing lease |
| Path Memorial | rejected | rejected | rejected | ignored | no lease |
| Worldline Shape | carrier only; payload checked separately | rejected | rejected | ignored | no target lease |
| Deferred Arrival | rejected | rejected | rejected | observed only | acquires `DEFERRED` |
| Continuation | finite harmful-effect and force result adapters only | existing safe adapters only | transferable effect/force | observed only | motion mode acquires `CONTINUING` |
| Causal Relay | rejected | rejected | recursion-guarded | observed only | no lease |
| Still Point | rejected | rejected | rejected | observed only | acquires `STILL` |
| Foregone Path | rejected | rejected | rejected | observed only | acquires `FOREGONE` |
| Revision | rejected | rejected | rejected | authoritative mutation | no lease |
| Counterfactual | rejected | rejected | rejected | observed only | no lease |
| Long Moment | rejected by all authored Laws | rejected | rejected | observation only | interval participation |
| Unfinished Hour | apparatus decisions only | rejected | rejected | authoritative journal | region reservation |

Other players are rejected except explicit Long Moment/Hour consent.

- [ ] **Step 2: Run the matrix test**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineCompatibilityTest"
```

Expected: compilation fails because the unified policy does not exist.

- [ ] **Step 3: Implement and route policy checks**

Every component, authored handler, replay adapter, relay adapter, journal
authorizer, and interval/apparatus start calls the shared policy before
mutating state. Guarded replay cannot create another journal event,
Remainder, recurrence, relay, or spatial lease.

- [ ] **Step 4: Run compatibility and feature tests**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineCompatibilityTest" --tests "com.vincenthuto.mnagnosis.common.worldline.*"
```

Expected: the matrix and every feature unit test pass.

- [ ] **Step 5: Commit compatibility**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineCompatibility.java src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineCompatibilityTest.java src/main/java/com/vincenthuto/mnagnosis/common/spell src/main/java/com/vincenthuto/mnagnosis/common/authorship
git commit -m "feat: enforce worldline compatibility"
```

### Task 3: Complete recipes, tags, assets, and localization

**Files:**
- Create: `WorldlineResourceContractTest.java`
- Create: `WorldlineClientRegistration.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java`
- Modify: `en_us.json`, `sounds.json`, and feature-owned resources.

- [ ] **Step 1: Write a failing resource-contract test**

For every ordinary component and Recurrence inscription, assert:

- A Tier-6 `mna:component` recipe under
  `data/mnagnosis/recipes/components/`.
- A readable 16×16 or 32×32 spell icon under
  `textures/spell/component/` or `textures/spell/modifier/`.
- Name, description, invalid-target, and recovery translation keys.

For Shape `mnagnosis:worldline`, assert the M&A shape recipe
`data/mnagnosis/recipes/shapes/worldline.json`, its 16x16 or 32x32 icon, and
its name/description keys.

For Causal Spindle, Palimpsest Lens, Unspent Moment, and Hourglass, assert
item models and translations. For the Hourglass, assert blockstate, block
model, item model, block loot table, recipe, and reversible-block tag. For
the interval, assert dimension, dimension type, and forbidden-dimension tag.

- [ ] **Step 2: Run the resource test and record missing paths**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineResourceContractTest"
```

Expected: failure output lists every missing or malformed resource.

- [ ] **Step 3: Add canonical shared presentation resources**

Register sounds:

```text
mnagnosis:worldline_mark
mnagnosis:worldline_depart
mnagnosis:worldline_arrive
mnagnosis:worldline_tick
mnagnosis:worldline_release
mnagnosis:worldline_remainder_vent
mnagnosis:unfinished_hour_begin
mnagnosis:unfinished_hour_decide
mnagnosis:recurrence_arm
mnagnosis:recurrence_fire
mnagnosis:foregone_preview
mnagnosis:foregone_commit
mnagnosis:revision_arm
mnagnosis:revision_accept
mnagnosis:revision_reject
mnagnosis:counterfactual_preview
mnagnosis:counterfactual_manifest
mnagnosis:counterfactual_break
mnagnosis:consequence_without_cause
mnagnosis:consequence_cause_paid
```

All public strings use “Remainder” for Worldline presentation while ledger
internals continue to use `Contradiction`.

`WorldlineResourceContractTest` asserts every ID in
`WorldlineFeatureRegistry.soundIds()` has a `SoundRegistry` handle, a
`sounds.json` entry, an existing OGG file, and a subtitle translation. It
also asserts the shared sounds delegated by Plans 02–06 and all feature
sounds delegated by Plans 07–11; a plan may not name an unregistered sound.

- [ ] **Step 4: Register all client-only Worldline entry points**

Create `WorldlineClientRegistration` and call it only from existing
client-side event/setup paths:

```java
public final class WorldlineClientRegistration {
    public static void registerEntityRenderers(
            EntityRenderersEvent.RegisterRenderers event);
    public static void registerWorldlineLayers();
    public static void registerHourPresentation(FMLClientSetupEvent event);
}
```

`registerEntityRenderers` binds the server-registered
`mnagnosis:worldline_carrier` to Minecraft's `NoopRenderer` because its
sanitized segment is drawn by Plan 00, and binds
`mnagnosis:white_effigy` to `WhiteEffigyRenderer`.
`registerWorldlineLayers` adds
`LongMomentRenderLayer` and the Hour source-bound layer to Plan 00's
`WorldlineRenderer`. `registerHourPresentation` registers the Unfinished
Hour block-entity renderer and owner decision-screen factory. Common setup
must never reference these client classes. Add a dedicated-server
classloading test and client registration assertions for every delegated
renderer, layer, and screen.

- [ ] **Step 5: Verify JSON, client registration, and resource contracts**

```powershell
Get-ChildItem src/main/resources -Recurse -Filter *.json | ForEach-Object {
    Get-Content -Raw $_.FullName | ConvertFrom-Json | Out-Null
}
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineResourceContractTest"
```

Expected: all JSON parses and the complete resource contract passes.

- [ ] **Step 6: Commit resources**

```powershell
git add -- src/main/resources src/main/java/com/vincenthuto/mnagnosis/client/worldline/WorldlineClientRegistration.java src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineResourceContractTest.java src/main/java/com/vincenthuto/mnagnosis/common/registry/SoundRegistry.java
git commit -m "feat: add worldline resources"
```

### Task 4: Add diagnostics and migration acceptance

**Files:**
- Create: `WorldlineDiagnostics.java`
- Create: `WorldlineAudit.java`
- Create: `WorldlineAuditEntry.java`
- Create: `WorldlineCommand.java`
- Modify: `MnAGnosis.java`
- Modify: `WorldlineIntegrationGameTests.java`

**Interfaces:**
- Produces:

```java
public record WorldlineAudit(
        int active,
        int recovering,
        int quarantined,
        int tombstones,
        long estimatedBytes,
        List<WorldlineAuditEntry> entries
) {}

public final class WorldlineDiagnostics {
    public static WorldlineAudit audit(MinecraftServer server);
    public static WorldlineDecisionResult release(
            CommandSourceStack source,
            UUID sessionId
    );
}
```

- [ ] **Step 1: Write failing audit and migration GameTests**

Test loading:

- A world with no Worldline data.
- Current schema with simultaneous source dimensions.
- Older player capability data without Worldline keys.
- Unknown session type and newer payload version.
- Active session whose owner is offline.
- Recovery cursor interrupted after one block batch.

Assert unknown data quarantines, remains visible to audit, and does not crash
login or server tick.

- [ ] **Step 2: Add operator commands**

Register:

```text
/mnagnosis worldline audit
/mnagnosis worldline inspect <session-uuid>
/mnagnosis worldline release <session-uuid>
```

Require permission level 2. `audit` and `inspect` are read-only. `release`
invokes the handler's conservative recovery path, records an action receipt,
and never deletes raw quarantine data or force-loads a dimension.

- [ ] **Step 3: Run integration GameTests**

```powershell
.\gradlew.bat runGameTestServer
```

Expected: migration, quarantine, audit, release, and all feature GameTests
pass.

- [ ] **Step 4: Commit diagnostics**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineDiagnostics.java src/main/java/com/vincenthuto/mnagnosis/common/command/WorldlineCommand.java src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java src/main/java/com/vincenthuto/mnagnosis/gametest/WorldlineIntegrationGameTests.java
git commit -m "feat: add worldline recovery diagnostics"
```

### Task 5: Run multiplayer and performance acceptance

**Files:**
- Modify only when a measured failure identifies a defect.

- [ ] **Step 1: Run a two-hour dedicated-server soak**

Exercise two to four clients with:

- Concurrent sessions in three dimensions.
- Consent, withdrawal, death, disconnect, and reconnect.
- Restart during every Worldline phase.
- Natural chunk unload and reload during recovery.
- Repeated stale and duplicate decision packets.
- Maximum path, field, echo, journal, escrow, and interval bounds.
- Protection-mod cancellation of Revision and Hour restoration.

Record server tick time, saved-data size, packet counts, and client frame time
at five-minute intervals.

- [ ] **Step 2: Enforce performance gates**

Acceptance:

- Worldline server work adds no more than 2 ms to the 95th-percentile tick
  with eight ordinary sessions active.
- One active Hour restore batch remains below 5 ms at the 95th percentile.
- No custom Worldline packet exceeds 32 KiB, and the complete Causal
  Spindle item NBT remains at or below its 16 KiB feature cap.
- No observer receives another dimension's private session data.
- Saved Worldline data remains below 2 MiB per source dimension.
- Client reduced-effects mode renders 64 traces without sustained frame time
  above 16.7 ms on the project's reference development machine.
- Session and cell counts return to zero after terminal cleanup.

- [ ] **Step 3: Run visual fallbacks**

Repeat Long Moment, Still Point, Foregone Path, Recurrence Vent, and Hour
decision flows with post effects disabled, reduced motion enabled, and shader
initialization forced to fail. Confirm all target, boundary, choice, countdown,
and danger information remains visible.

- [ ] **Step 4: Record soak evidence**

Attach the server log, config, player count, session script, timing table,
packet maxima, screenshots of full and fallback presentation, and final audit
output to the implementation PR.

### Task 6: Complete release verification

**Files:**
- Verify only.

- [ ] **Step 1: Run the complete unit suite**

```powershell
.\gradlew.bat test --console=plain
```

Expected: all new Worldline tests pass. If the pre-existing Yaldabaoth
`ClassCastException` remains, verify it is the only failure and its stack
trace is unchanged.

- [ ] **Step 2: Run a clean production build**

```powershell
.\gradlew.bat clean build --console=plain
```

Expected: compilation, resource processing, Mixin processing, and packaging
complete. A build blocked solely by the named baseline test is documented
with the passing `clean compileJava` and targeted Worldline suite.

- [ ] **Step 3: Run every GameTest**

```powershell
.\gradlew.bat runGameTestServer
```

Expected: all existing and Worldline GameTests pass.

- [ ] **Step 4: Inspect dependency and scope**

```powershell
git diff --check
git status --short
.\gradlew.bat dependencies | Select-String -Pattern "Immersive|iPortal|qouteall"
rg -n "forceChunk|addRegionTicket|portal.*packet|qouteall" src/main/java build.gradle gradle.properties
```

Expected: no whitespace errors, no Immersive Portals dependency, no remote
portal implementation, and no persistent Worldline chunk ticket.

- [ ] **Step 5: Audit requirement coverage**

Use `README.md` as the checklist. For every listed concept, record:

- Registry or apparatus ID.
- Owning plan and implementation commit.
- Focused unit-test class.
- GameTest method group.
- Recipe/icon/localization status.
- Normal completion behavior.
- Logout/death/restart recovery behavior.
- Remainder behavior, or explicit natural Closure.
- Full-effects and reduced-effects presentation evidence.

- [ ] **Step 6: Commit final integration corrections**

If Tasks 5–6 required scoped corrections:

```powershell
git add -- src/main src/test
git commit -m "fix: complete worldline release acceptance"
```

If no correction was required, do not create an empty commit.
