# Architectonics Integration, Progression, and Release Implementation Plan

> **Superseded foundation tasks:** Relation progression plugs into the shared
> Manuscript capability; Dislocation is an `ExternalContradictionType`; packets
> use Architectonics IDs `64-255` without changing protocol `"5"`. Do not create
> the discipline-local capability, packet allocator, or ledger described later.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate all Architectonic plans into a coherent Tier-6 discipline with proof-based Manuscript progression, shared Dislocation/Closure rewards, bounded configuration, migrations, compatibility coverage, and staged release gates.

**Architecture:** Feature packages retain their own mechanics, while this plan supplies the integration manifest and three cross-feature policies: `RelationManuscriptState` records qualitative proof progression, `ArchitectonicContradictionService` expresses Dislocation through the existing Contradiction ledger, and an acceptance matrix verifies every work against native and optional spatial backends. Registration and data-contract tests prevent a partially integrated feature from silently shipping.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1, Mana and Artifice 3.1.11 recipes and spell registries, Forge capabilities/events/advancements/config, existing MnAGnosis authorship ledger and network, JUnit 5, Forge GameTests, Gradle/Jar inspection, optional Immersive Portals for Forge 3.0.7.

## Global Constraints

- This plan integrates plans `00` through `13`; it does not redesign their feature-local mechanics.
- Tier 6 does not become Tier 7.
- Architectonics is not an exclusive faction and introduces no resource bar.
- Proof progression is qualitative and finite; it is not repeatable experience grinding.
- Existing Mana and Artifice shapes and components remain useful.
- The existing Contradiction ledger stores Dislocation; do not add a second debt ledger.
- Responsible Closure is legible, rewarding, and cannot duplicate proof rewards or Fixed Points.
- `mnagnosis:fixed_point` is a consequence/proof recovered from eligible Closure, never an ore, mob grind drop, or generic recipe ingredient.
- A player can inspect why a work, cast, proof, or closure was denied through translatable server-authored messages.
- All server caps are enforced at creation and re-evaluated safely when configuration reload lowers a cap.
- Native spatial behavior is the release baseline. Immersive Portals is optional enhancement coverage.
- No feature force-loads chunks or bypasses protection adapters.
- Save migration is monotonic, idempotent, and preserves unknown work payloads.
- Common code must start on a dedicated server with no client or optional-mod classes present.
- Minecraft 1.20.1, Forge, Java 17, and Mana and Artifice 3.1.11 remain the baseline.

---

### Task 1: Registration and content manifest enforcement — Core

**Files:**
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/ArchitectonicsManifestTest.java`
- Create: `src/test/resources/architectonics/expected-components.txt`
- Create: `src/test/resources/architectonics/expected-shapes.txt`
- Create: `src/test/resources/architectonics/expected-items.txt`
- Create: `src/test/resources/architectonics/expected-work-codecs.txt`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/SpellComponentRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/BlockRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/BlockEntityRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/network/NetworkHandler.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java`

**Interfaces:**
- Consumes: registrations produced by plans `00`–`13`.
- Produces: a machine-readable expected-content manifest and a failing test
  whenever registration, recipe, model, texture, localization, codec, or
  handler is missing.

- [ ] **Step 1: Write the exact expected registries**

`expected-components.txt`:

```text
mnagnosis:components/reassembled_land
mnagnosis:components/axial_ordination
mnagnosis:components/load_bearing_principle
mnagnosis:components/metric_compression
mnagnosis:components/axiom_of_adjacency
mnagnosis:components/euclidean_refusal
mnagnosis:components/hollow_domain
mnagnosis:components/coordinate_transposition
mnagnosis:components/world_seam
```

`expected-shapes.txt` contains `mnagnosis:boundary_condition` and
`mnagnosis:lattice_emanation`. `expected-items.txt` contains
`unbounded_lattice`, `transposition_loom`, `unbounded_casket`,
`seam_ripper`, `fixed_point`, and `manuscript_of_relation`.
`expected-work-codecs.txt` contains every `ArchitectonicWorkType`.

- [ ] **Step 2: Write the failing manifest test**

For each expected component/shape, verify registry declaration, spell icon,
Tier-6 M&A recipe, name, description, and use-tag localization. For each item
or block, verify registry declaration, model, texture, recipe or explicit
proof-only exception, and localization. Verify every work type has one codec
and one handler. Reflect every packet registration and assert its ID lies in
the README reservation, all IDs are unique, omitted Enhancement packets leave
gaps, and no registration derives its ID from `id++` ordering.

Run: `./gradlew.bat test --tests "*ArchitectonicsManifestTest"`

Expected: test fails with a precise list of integration omissions.

- [ ] **Step 3: Normalize all registration call sites**

Keep spell effects and modifiers in `SpellComponentRegistry`; add a focused
`SpellShapeRegistry` if shape registration would otherwise mix registry keys.
Use uppercase `RegistryObject` field names for all newly added items/blocks.
Register menus, capabilities, codecs, and triggers exactly once on the correct
Forge or mod event bus. After the complete packet manifest is assembled, bump
`NetworkHandler.PROTOCOL` once from `"5"` to `"6"` and keep that value across
all feature cuts in this suite. Register packet groups in plan order through
`MnAGnosisPacketRegistrar`; numeric IDs still come only from
`ArchitectonicsPacketIds`.

- [ ] **Step 4: Fill manifest omissions without changing feature contracts**

Add missing data/assets/registration files reported by the test. Do not create
stand-in blank textures: each icon must distinguish its semantic geometry at
`16 x 16`, and apparatus block/item models must resolve in inventory and world.

- [ ] **Step 5: Run manifest and registry tests**

Run: `./gradlew.bat test --tests "*ArchitectonicsManifestTest"`

Expected: every expected ID, asset, recipe, codec, and handler is present.

Run: `./gradlew.bat runData`

Expected: data generation completes without duplicate or invalid registry IDs.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis src/main/resources src/test/java/com/vincenthuto/mnagnosis/common/architectonics/ArchitectonicsManifestTest.java src/test/resources/architectonics
git commit -m "feat: integrate architectonics content manifest"
```

### Task 2: Central caps and configuration reload policy — Core

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/config/ArchitectonicsLimits.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/config/ArchitectonicsConfigReloadService.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicCreationPolicy.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicCreationDecision.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicCreationReservation.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicReservationResult.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicCreationReservationSavedData.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicSavedData.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/axial/AxialOrdinationRegistration.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/boundary/BoundaryConditionRegistration.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/loadbearing/LoadBearingWorkService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/lattice/LatticeEmanationService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/metric/MetricCompressionService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/adjacency/AdjacencyService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/refusal/EuclideanRefusalService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/hollow/HollowDomainService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/interior/InteriorPlacementTransaction.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/interior/InteriorInitializationJournalEntry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/interior/InteriorInitializationRecovery.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/seam/WorldSeamService.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsIntegrationGameTests.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/config/ArchitectonicsLimitsTest.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicCreationPolicyTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/Config.java`
- Modify: `src/main/resources/assets/mnagnosis/lang/en_us.json`

**Interfaces:**
- Produces: immutable `ArchitectonicsLimits.snapshot()` read once per operation.
- Produces: deterministic reload handling for active works above a lowered cap.
- Produces: `ArchitectonicCreationPolicy.check(UUID ownerId, ArchitectonicWorkType type, ArchitectonicsLimits limits): ArchitectonicCreationDecision`.
- Produces: `ArchitectonicSavedData.addNew(ServerPlayer, ArchitectonicWork, ArchitectonicsLimits): ArchitectonicCreationDecision`.
- Produces: `ArchitectonicCreationDecision(boolean allowed, Reason reason, Component message)` with nested reasons `ACCEPTED`, `OWNER_CAP`, `SERVER_CAP`, `FEATURE_CAP`, `DUPLICATE_ID`, and `INVALID`.
- Produces: `ArchitectonicCreationPolicy.reserve(ServerPlayer, ArchitectonicWorkType, UUID operationId, ArchitectonicsLimits): ArchitectonicReservationResult`.
- Produces: `ArchitectonicCreationReservation(UUID operationId, UUID ownerId, ArchitectonicWorkType type, long limitsHash, long createdAt)`.
- Produces: `ArchitectonicReservationResult(ArchitectonicCreationDecision decision, Optional<ArchitectonicCreationReservation> reservation)`.
- Produces: `ArchitectonicCreationPolicy.commitReserved(MinecraftServer, UUID operationId, ArchitectonicWork): ArchitectonicCreationDecision`.
- Produces: `ArchitectonicCreationPolicy.cancelReservation(MinecraftServer, UUID operationId): boolean`.

- [ ] **Step 1: Write default/bound tests for every shared limit**

Use these server config keys and defaults:

| Key | Default | Allowed range |
|---|---:|---:|
| `architectonicsMaximumWorksPerPlayer` | 16 | 1–128 |
| `architectonicsMaximumWorksPerServer` | 256 | 16–4096 |
| `architectonicsMaximumPermissionVolume` | 32768 | 512–1048576 |
| `reassembledLandMaximumBlocks` | 384 | 32–384 |
| `boundaryMaximumEnumeratedBlocks` | 512 | 64–512 |
| `latticeMaximumResolutions` | 128 | 16–128 |
| `metricMaximumPerPlayer` | 4 | 0–32 |
| `adjacencyMaximumPerPlayer` | 2 | 0–16 |
| `refusalMaximumPerPlayer` | 4 | 0–32 |
| `hollowDomainMaximumBlocks` | 8000 | 512–8000 |
| `coordinateTranspositionMaximumBlocksPerRegion` | 512 | 32–512 |
| `worldSeamMaximumPerPlayer` | 1 | 0–1 |
| `worldSeamMaximumPerServer` | 4 | 0–4 |
| `worldSeamMaximumWidth` | 32 | 4–32 |
| `worldSeamMaximumHeight` | 32 | 4–32 |
| `architectonicsCrossingCooldownTicks` | 10 | 2–10 |

Also retain `architectonicsRespectSpawnProtection=true` and
`architectonicsAllowCrossDimension=true`. Unbounded Interior cell count
`4096`, grid `64 x 64`, spacing `256`, and World Seam flux lifetime `200`
ticks are save/closure invariants, not configuration. A server config may
lower an operation count but never raise a feature's hard safety ceiling.

- [ ] **Step 2: Run the limits test and verify RED**

Run: `./gradlew.bat test --tests "*ArchitectonicsLimitsTest"`

Expected: tests fail because exhaustive normalization, creation policy, and
reload behavior are not implemented yet.

- [ ] **Step 3: Verify and finalize immutable normalized snapshots**

```java
public record ArchitectonicsLimits(
        int maximumWorksPerPlayer,
        int maximumWorksPerServer,
        int maximumPermissionVolume,
        boolean respectSpawnProtection,
        boolean allowCrossDimension,
        int reassembledLandMaximumBlocks,
        int boundaryMaximumEnumeratedBlocks,
        int latticeMaximumResolutions,
        int metricMaximumPerPlayer,
        int adjacencyMaximumPerPlayer,
        int refusalMaximumPerPlayer,
        int hollowDomainMaximumBlocks,
        int coordinateTranspositionMaximumBlocksPerRegion,
        int worldSeamMaximumPerPlayer,
        int worldSeamMaximumPerServer,
        int worldSeamMaximumWidth,
        int worldSeamMaximumHeight,
        int crossingCooldownTicks
) {
    public static ArchitectonicsLimits snapshot() {
        return new ArchitectonicsLimits(/* one read of each Config value */);
    }
}
```

Feature services receive the snapshot at operation start and do not observe
half a config reload during a transaction.

`ArchitectonicSavedData.addNew` is the only user-creation seam. It invokes
`ArchitectonicCreationPolicy` before mutating SavedData, the spatial index, or a
backend. Every active-work creator in plans `03` through `10`, `12`, and `13`
must replace its direct `add` with `addNew`; plan `11` is a journaled block
transaction and creates no `ArchitectonicWork`, while plans `01` and `02` do
not create active work records. The exact callers are
`AxialOrdinationRegistration`, `BoundaryConditionRegistration`,
`LoadBearingWorkService`, `LatticeEmanationService`,
`MetricCompressionService`, `AdjacencyService`, `EuclideanRefusalService`,
`HollowDomainService`, `InteriorPlacementTransaction`, and
`WorldSeamService`. `replace` of the same work ID, codec load, quarantine restore,
transaction recovery, and startup reconstruction use explicitly named internal
paths and bypass creation caps without creating a second ID.

Count every known, active, user-owned work returned by `all()`, including
`INTERIOR_THRESHOLD` while its Casket is placed and `WORLD_SEAM`; count neither
quarantine entries, journals/reservations, derived backend faces, nor completed
Reassembled/Transposition receipts. A creation is accepted only when the
post-add count is at most both global caps and any stricter feature cap. Loaded
saves above a lowered cap remain authoritative, but no new ID is accepted until
both counts are below their limits.

The word “count” above describes active-work diagnostics. Admission additionally
counts nonterminal `ArchitectonicCreationReservation` records so concurrent
multi-stage operations cannot overbook a future slot. Ordinary casts use
atomic `addNew`; Unbounded Interior preselects its placement transaction UUID,
calls `reserve(INTERIOR_THRESHOLD, transactionId, limits)` before saving
`PREPARED`, stores that reservation ID in its existing initialization journal,
and later uses `commitReserved` when installing the threshold. A committed
reservation remains valid across a config reduction and recovery; it cannot
create any type/owner/work other than the tuple captured at reservation.
Rollback calls `cancelReservation`.

Reservation schema `1` stores operation ID, owner, type, accepted limit
snapshot hash, and created time in overworld SavedData. Save the reservation
before the feature journal. On startup, cancel an orphan whose linked feature
journal was never durably created; otherwise the feature recovery owns it.
Cap reservations at the configured global-work hard ceiling (`4,096`) and one
per operation ID. Tests crash between reserve/journal, journal/threshold,
threshold/commit, commit/removal, and exercise two owners racing for the last
global slot.

The executable feature-cap map is `METRIC_CORRIDOR ->
metricMaximumPerPlayer`, `ADJACENCY -> adjacencyMaximumPerPlayer`,
`REFUSAL_PLANE -> refusalMaximumPerPlayer`, and `WORLD_SEAM ->` both its
per-player and per-server caps. Hollow Domain's one-per-owner replacement rule
is enforced by its close-before-create service; Unbounded Interior allocation
uses its separate fixed cell registry. Every other work type uses the global
caps only.

- [ ] **Step 4: Implement lowering-cap behavior**

Never delete active works merely because the global cap was lowered. Reject new
creation while above the cap; natural expiry/Closure brings counts down.
Exception: rendering and per-tick resolution caps apply immediately by
deterministically pausing highest UUIDs, never by removing authority. World
Seams remain closable even when creation is disabled with a zero cap.

- [ ] **Step 5: Run tests and config reload GameTest**

Create/extend
`src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsIntegrationGameTests.java`.
Start above a newly lowered cap, verify all records remain inspectable and
closable, reject another creation, then close enough works and verify creation
resumes only when the proposed post-add count fits both caps. Parameterize the
test over every active work type and its public creation entry point. Also prove
that same-ID replacement and restart recovery do not consume another slot,
unknown quarantine entries do not count, direct public `add` is unavailable,
and accepted Interior reservations survive lowering/restart without letting
two operations consume the same last slot.

Run: `./gradlew.bat test --tests "*ArchitectonicsLimitsTest" --tests "*ArchitectonicCreationPolicyTest"`

Expected: all defaults, ranges, normalization, and snapshots pass.

Run: `./gradlew.bat runGameTestServer`

Expected: config reload GameTest and all existing GameTests pass.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/architectonics/config src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime src/main/java/com/vincenthuto/mnagnosis/Config.java src/main/resources/assets/mnagnosis/lang/en_us.json src/test/java/com/vincenthuto/mnagnosis/common/architectonics/config src/test/java/com/vincenthuto/mnagnosis/common/architectonics/runtime src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsIntegrationGameTests.java
git commit -m "feat: bound architectonics server limits"
```

### Task 3: Manuscript of Relation and finite proof progression — Core

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/RelationManuscriptStage.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/RelationProof.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/ArchitectonicFeature.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/ArchitectonicProgressionService.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/ProgressionGateResult.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/ProofGrantResult.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/PendingProofResult.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/ArchitectonicClosureReceiptSink.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/ArchitectonicClosureReceiptBridge.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/ArchitectonicSpellPartFeatureRegistry.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/ArchitectonicProgressionPreflight.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/mixin/core/SpellCasterProgressionGateMixin.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/IRelationManuscriptState.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/RelationManuscriptState.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/RelationManuscriptProvider.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/RelationManuscriptEvents.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/RelationProofPendingSavedData.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/item/RelationManuscriptItem.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/network/RelationManuscriptPacket.java`
- Create: `src/main/resources/data/mnagnosis/recipes/manuscript_of_relation.json`
- Create: `src/main/resources/assets/mnagnosis/models/item/manuscript_of_relation.json`
- Create: `src/main/resources/assets/mnagnosis/textures/item/manuscript_of_relation.png`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/progression/RelationManuscriptStateTest.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/progression/ArchitectonicProgressionIntegrationTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/authorship/AuthorshipCastingService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentReassembledLand.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentAxialOrdination.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ShapeBoundaryCondition.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentLoadBearingPrinciple.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ShapeLatticeEmanation.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentMetricCompression.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentAxiomOfAdjacency.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentEuclideanRefusal.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentHollowDomain.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentCoordinateTransposition.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/spell/ComponentWorldSeam.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/item/UnboundedLatticeItem.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/instrument/LatticeSurveyService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/reassembled/ReassembledClosureService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/reassembled/ReassembledClosureListeners.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/loadbearing/LoadBearingEvents.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/loadbearing/HostileShelterReceiptSavedData.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/loadbearing/HostileShelterListeners.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/metric/MetricCompressionService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/metric/MetricTraversalReceiptSavedData.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/metric/MetricTraversalListeners.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/adjacency/AdjacencyService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/adjacency/AdjacencyClosureListeners.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/hollow/HollowDomainService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/hollow/HollowClosureListeners.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/transposition/TranspositionClosureService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/transposition/TranspositionClosureListeners.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/interior/InteriorReleaseTransaction.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/CapabilityRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/network/NetworkHandler.java`
- Modify: `src/main/resources/mnagnosis.mixins.json`
- Modify: `src/main/resources/assets/mnagnosis/lang/en_us.json`

**Interfaces:**
- Produces: `RelationManuscriptStage { PERCEPTION, INTERVENTION, AUTHORSHIP, ORIGINAL_WORK }`.
- Produces: finite `RelationProof` IDs and one-time timestamps.
- Produces: `ArchitectonicProgressionService.require(ServerPlayer, ArchitectonicFeature): ProgressionGateResult`.
- Produces: `ArchitectonicProgressionService.grantProof(ServerPlayer, RelationProof, UUID evidenceId): ProofGrantResult`.
- Produces: `ArchitectonicProgressionService.grantProof(MinecraftServer, UUID recipientId, RelationProof, UUID evidenceId): ProofGrantResult`.
- Produces: `ProgressionGateResult(boolean allowed, ArchitectonicFeature feature, RelationManuscriptStage requiredStage, Component message)`.
- Produces: `ProofGrantResult { APPLIED, ALREADY_OWNED, QUEUED_OFFLINE, DEFERRED_CAP, INELIGIBLE }`.
- Produces: `PendingProofResult { QUEUED, ALREADY_QUEUED, DEFERRED_CAP }`.
- Produces: `RelationProofPendingSavedData.enqueue(UUID, RelationProof, UUID, long): PendingProofResult`.
- Produces: `RelationProofPendingSavedData.drain(ServerPlayer): int`.
- Produces: `ArchitectonicClosureReceiptBridge.submit(MinecraftServer, UUID ownerId, ResourceLocation cause, UUID evidenceId): DurableSignalDisposition`.
- Produces: `ArchitectonicClosureReceiptBridge.install(ArchitectonicClosureReceiptSink): void`; a second distinct sink fails startup.
- Produces: `ArchitectonicsPacketIds.RELATION_MANUSCRIPT = 176`.
- Produces: player capability clone/save/sync lifecycle.
- Produces: read-only Manuscript item UI/tooltip; the capability, not item NBT, is authoritative.

- [ ] **Step 1: Write progression transition tests**

Use these exact proofs:

```text
FIRST_MEASURE
RETURN_BORROWED_LAND
HOSTILE_FORCE_SHELTERED
TRAVERSE_COMPRESSED_CORRIDOR
CLOSE_USED_ADJACENCY
RECOVER_TRANSPOSED_REGIONS
EVACUATE_HOLLOW_DOMAIN
CLOSE_WORLD_SEAM
```

Start Tier-6 eligible players at `PERCEPTION`. Advance to `INTERVENTION` after
`FIRST_MEASURE`; to `AUTHORSHIP` after any three distinct Intervention proofs
including `RETURN_BORROWED_LAND`; and to `ORIGINAL_WORK` after all eight proofs.
Assert duplicate proof grants do nothing, out-of-order proof storage is
allowed but cannot skip stage requirements, clone preserves state, and a
non-Tier-6 player may inspect but not advance it.

- [ ] **Step 2: Run the state test and verify RED**

Run: `./gradlew.bat test --tests "*RelationManuscriptStateTest"`

Expected: compilation fails because manuscript state does not exist.

- [ ] **Step 3: Implement finite capability state**

Store `schema=1`, stage ordinal, a compound mapping proof ID to first-earned
game time plus its first server-authored evidence UUID, and revision. Do not
store points or progress percentages. Attach
to players, copy through non-End death clone, retain on End return, sync on
login/respawn/dimension change/proof grant, and invalidate old capabilities.

- [ ] **Step 4: Implement progression gates without replacing M&A tiering**

- Perception: Unbounded Lattice and native previews.
- Intervention: Foundation plans `02`–`07`.
- Authorship: Frontier plans `08`–`12`.
- World Seam: requires Authorship plus `CLOSE_USED_ADJACENCY` and
  `RECOVER_TRANSPOSED_REGIONS`.
- Original Work: recorded for future personalized work; it adds no content
  gate in this suite.

Use this exact registry table to make those categories executable:

| Feature | Required stage/additional proof | Successful proof hook |
|---|---|---|
| Unbounded Lattice completion | `PERCEPTION` | `FIRST_MEASURE` after the first valid completed survey |
| Reassembled Land | `INTERVENTION` | `RETURN_BORROWED_LAND` after a durable inverse receipt closes |
| Axial Ordination | `INTERVENTION` | none |
| Boundary Condition | `INTERVENTION` | none |
| Load-Bearing Principle | `INTERVENTION` | `HOSTILE_FORCE_SHELTERED` after positive mitigation of a non-owner hostile hit or explosion |
| Lattice Emanation | `INTERVENTION` | none |
| Metric Compression | `INTERVENTION` | `TRAVERSE_COMPRESSED_CORRIDOR` after one entity enters one end and exits the other |
| Axiom of Adjacency | `AUTHORSHIP` | `CLOSE_USED_ADJACENCY` after an owner closes a durably marked-used relation |
| Euclidean Refusal | `AUTHORSHIP` | none |
| Hollow Domain | `AUTHORSHIP` | `EVACUATE_HOLLOW_DOMAIN` after safe Closure of a domain the owner entered |
| Coordinate Transposition | `AUTHORSHIP` | `RECOVER_TRANSPOSED_REGIONS` after a committed receipt is exactly inverted |
| Unbounded Interior | `AUTHORSHIP` | none |
| World Seam | `AUTHORSHIP` plus `CLOSE_USED_ADJACENCY` and `RECOVER_TRANSPOSED_REGIONS` | `CLOSE_WORLD_SEAM` after perfect shared Closure |

`ORIGINAL_WORK` is recorded for future personalized work and adds no content
gate in this suite. `ArchitectonicSpellPartFeatureRegistry` maps every exact
component/shape registry ID from plans `02` through `10` and `13` to this table.
M&A 3.1.11 has no cancelable pre-cast Forge event, so implement one narrow
shared `SpellCasterProgressionGateMixin`. Inject the exact static
`SpellCaster.PlayerCast(ItemStack, Player, InteractionHand, Vec3, Vec3, Level,
boolean, boolean)` descriptor at HEAD with `cancellable=true`, `require=1`, and
`remap=false`. For a `ServerPlayer`, `ArchitectonicProgressionPreflight` decodes
the spell recipe from the held stack, resolves every Architectonic part through
the feature registry, and returns the highest unmet gate. On denial, send its
specific localized message and return
`SpellCastingResultCode.BLOCKED_BY_CONFIG.createResult()` before M&A checks or
consumes mana/reagents; creative mode does not bypass this gate. Client/nonplayer
casts pass through. Verify the target against the bundled 3.1.11 jar with
`compileJava` and a startup mixin audit.

`AuthorshipCastingService` may expose the same pure gate result to its
authorship UI, but is not treated as the cancellation seam. Each
component/shape service rechecks the same gate before world mutation to defend
nonstandard callers.
Loom/Casket menus check before accepting a survey, catalyst, commit, expansion,
or release action.

Proof hooks run only after their authoritative state is durable: receipt closed,
work `used` flag saved, safe evacuation/removal complete, or World Seam Closure
ledger reconciled. A proof grant is idempotent by proof enum; the first evidence
UUID/time remains immutable. Client packets and visual events never grant
proof.

The proof recipient is always the authoritative owner/caster recorded by the
survey, work, or receipt—not a protected victim, traversing entity, passenger,
nearby operator, or player who picks up an effect. `FIRST_MEASURE` goes to the
player completing their Lattice; every other row goes to the work/receipt
owner, even when an ally or non-player demonstrates the relation. The UUID
overload writes one bounded pending proof to overworld
`RelationProofPendingSavedData` when that owner is offline. Schema `1` keeps at
most one record per `(owner UUID, proof)`, at most `8` records per owner, and
at most `4,096` records globally. A record preserves the first evidence UUID
and earned game time. Duplicate evidence is idempotent; a different duplicate
cannot overwrite those first values. Invalid enum IDs, duplicate keys, or
oversized/corrupt roots are quarantined rather than partially loaded.
`enqueue` returns `DEFERRED_CAP` at either hard ceiling and leaves the
feature-local terminal receipt/event unacknowledged so its normal replay can
retry later; it never silently drops a proof. `drain` applies records
idempotently on login, persists the capability/player, removes only applied or
already-owned proofs, and then flushes the pending store. Tests cover the
`8/9` per-owner and `4096/4097` global boundaries, duplicate evidence, offline
restart, corrupt data, player clone, a crash between capability save and
pending removal, non-player subjects, ally/hostile beneficiaries, and
operator-triggered recovery.

During common setup, register one adapter with each feature-local listener
registry from plans `02`, `05`, `07`, `08`, `10`, and `11`. The adapters call
the UUID proof overload only after receiving the feature's terminal durable
receipt/event. Registration is idempotent and duplicate listener installation
fails startup; no earlier feature plan imports progression classes. The
apparent combination is deliberate: one run-once common-setup coordinator
makes repeated lifecycle calls a no-op, while each local registry rejects a
second distinct production handler. Use this
exact binding table:

| Feature-local signal | Recipient and progression action |
|---|---|
| `LatticeSurveyService` incomplete-to-complete transition | Completing player; grant `FIRST_MEASURE` after the completed survey NBT is durable |
| `ReassembledClosureListeners` closed inverse receipt | Receipt owner; grant `RETURN_BORROWED_LAND`, then submit the same receipt to Closure validation |
| `HostileShelterListeners` durable positive non-owner hostile-mitigation receipt | Work owner; grant `HOSTILE_FORCE_SHELTERED`, then acknowledge the feature receipt |
| `MetricTraversalListeners` durable completed end-to-end traversal receipt | Work owner; grant `TRAVERSE_COMPRESSED_CORRIDOR`, then acknowledge the feature receipt |
| `AdjacencyClosureListeners` closed `used=true` receipt | Work owner; grant `CLOSE_USED_ADJACENCY`, then submit the same receipt to Closure validation |
| `HollowClosureListeners` eligible owner-request close receipt | Work owner; grant `EVACUATE_HOLLOW_DOMAIN`, then submit the same receipt to Closure validation |
| `TranspositionClosureListeners` closed exact-inverse receipt | Receipt owner; grant `RECOVER_TRANSPOSED_REGIONS`, then submit the same receipt to Closure validation |
| `WorldSeamClosureCallbacks` committed safe-close receipt | Work owner; Task 4's combined callback grants `CLOSE_WORLD_SEAM`, then submits the same receipt to Closure validation |

Each adapter returns `DurableSignalDisposition.ACKNOWLEDGED` only after the
proof is applied or durably queued and any required Closure evidence/reward
reaches durable `PENDING`; otherwise it returns `.DEFERRED`.
`DEFERRED_CAP`, unavailable player data, or a failed evidence write leaves the
terminal feature receipt unacknowledged for replay.

Task 3 creates `ArchitectonicClosureReceiptBridge` with a default
`DEFERRED` sink. Reassembled, Adjacency, Hollow, and Transposition adapters
grant their proof idempotently, then call `submit` with only the owner, cause,
and feature receipt UUID. They remain unacknowledged during Task 3. Task 4
installs the one production sink, which re-reads the typed receipt from its
feature SavedData, validates it, and returns `ACKNOWLEDGED` only after the
shared reward entitlement is durable. This preserves task-by-task compilation
without deleting evidence before Closure classes exist.

The Lattice hook shares
the one authoritative completion method used by packet handling and direct
server callers, so neither path can bypass or double-grant it. On the first
incomplete-to-complete transition, mutate the held Lattice NBT, call public
`server.getPlayerList().saveAll()` as a rare player-data barrier, grant
`FIRST_MEASURE`, and save player data again before returning. No event dispatch
or inventory movement occurs inside that sequence. Login recovery scans the
player's persisted inventory for any valid completed owned Lattice when
`FIRST_MEASURE` is absent and applies it with the survey's stable completion
UUID; this closes the crash window after the first save. Tests inject failure
before and after both saves and exercise packet, direct-server, clone, and
login-recovery paths.
Because World Seam's callback also arbitrates forced Dislocation creation,
Task 4 installs its single combined proof/Closure adapter; Task 3 does not
install a competing partial callback.

Recipes remain Tier 6 M&A recipes. Component/shape application returns a
specific Manuscript-stage denial before world mutation.

- [ ] **Step 5: Add the read-only living Manuscript item**

Craft from a written book, Unbounded Lattice, black concrete, white concrete,
and a primal mote. Right-click opens a read-only page showing current stage,
named proofs, dates, and precise missing conditions. Losing or duplicating the
item does not lose or duplicate capability state.

Register `RelationManuscriptPacket` only at
`ArchitectonicsPacketIds.RELATION_MANUSCRIPT = 176` through
`MnAGnosisPacketRegistrar`; leave `177..191` reserved. This is not an additional
protocol bump and the ID is never derived from registration order.

- [ ] **Step 6: Run unit and progression GameTests**

Run: `./gradlew.bat test --tests "*RelationManuscriptStateTest" --tests "*ArchitectonicProgressionIntegrationTest"`

Expected: all stage, proof, clone, and serialization tests pass.

Run: `./gradlew.bat runGameTestServer`

Expected: every row in the feature table denies at the preflight and mutation
seams below its stage, every listed integration action grants its proof exactly
once only after durable success, and denial consumes no matter, catalyst, mana,
reagent, or work slot.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/architectonics src/main/java/com/vincenthuto/mnagnosis/common/authorship/AuthorshipCastingService.java src/main/java/com/vincenthuto/mnagnosis/common/item/UnboundedLatticeItem.java src/main/java/com/vincenthuto/mnagnosis/common/item/RelationManuscriptItem.java src/main/java/com/vincenthuto/mnagnosis/common/network/RelationManuscriptPacket.java src/main/java/com/vincenthuto/mnagnosis/common/registry/CapabilityRegistry.java src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java src/main/java/com/vincenthuto/mnagnosis/common/network/NetworkHandler.java src/main/java/com/vincenthuto/mnagnosis/mixin/core/SpellCasterProgressionGateMixin.java src/main/resources/mnagnosis.mixins.json src/main/resources/data/mnagnosis/recipes/manuscript_of_relation.json src/main/resources/assets/mnagnosis src/test/java/com/vincenthuto/mnagnosis/common/architectonics/progression
git commit -m "feat: add manuscript of relation progression"
```

### Task 4: Dislocation, Closure bridge, and Fixed Point rewards — Core

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ArchitectonicContradictionService.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ArchitectonicClosureRewardSavedData.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ClosureRewardDelivery.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ClosureRewardDeliveryPhase.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ClosureRewardDeliveryService.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/DeliveryRecoveryReport.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ArchitectonicClosureValidator.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ArchitectonicClosureValidatorRegistry.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ArchitectonicClosureEvidenceSavedData.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/DislocationLawHandler.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ClosureOutcome.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ClosureRewardResult.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/BeginDeliveryResult.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ClosureValidationResult.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ContradictionCreationResult.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/progression/ArchitectonicClosureReceiptBridge.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/item/FixedPointItem.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ArchitectonicContradictionServiceTest.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ClosureRewardDeliveryServiceTest.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/closure/ArchitectonicClosureValidatorRegistryTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/reassembled/ReassembledClosureService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/adjacency/AdjacencyService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/hollow/HollowDomainService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/transposition/TranspositionClosureService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/interior/InteriorReleaseTransaction.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/interior/InteriorReleaseClosureReceiptSavedData.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/interior/InteriorReleaseClosureListeners.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/seam/WorldSeamClosureService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/seam/WorldSeamClosureCallbacks.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/seam/WorldSeamClosureReceiptSavedData.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/authorship/law/AuthoredLawRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java`
- Modify: `src/main/resources/assets/mnagnosis/lang/en_us.json`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsIntegrationGameTests.java`

**Interfaces:**
- Produces: law ID `mnagnosis:architectonics/dislocation`.
- Produces: `create(ServerPlayer, ResourceLocation cause, float paradox, CompoundTag payload): Optional<Contradiction>`.
- Produces: `createForReceipt(ServerPlayer owner, UUID receiptId, ResourceLocation cause, float paradox, CompoundTag payload): ContradictionCreationResult`.
- Produces: `ContradictionCreationResult(Optional<UUID> contradictionId, Component message)` with `created()` defined as `contradictionId.isPresent()`.
- Produces: `completeClosure(ServerPlayer, Optional<UUID> contradictionId, ClosureOutcome): ClosureRewardResult`.
- Produces: `ArchitectonicClosureRewardSavedData.begin(UUID workId, UUID ownerId, ResourceLocation cause, Optional<UUID> contradictionId, ClosureOutcome outcome): BeginDeliveryResult`.
- Produces: `BeginDeliveryResult { STARTED, RESUMED, ALREADY_DELIVERED, REJECTED_CONFLICT }`.
- Produces: `ClosureRewardDeliveryService.deliverOrDefer(MinecraftServer, UUID workId): ClosureRewardResult`.
- Produces: `ClosureRewardDeliveryService.recover(MinecraftServer): DeliveryRecoveryReport`.
- Produces: `DeliveryRecoveryReport(int delivered, int deferred, int repaired, int quarantined)`.
- Produces: `ArchitectonicClosureValidator.validate(ServerPlayer, ClosureOutcome): ClosureValidationResult`.
- Produces: `ClosureRewardResult { PENDING, DELIVERED, ALREADY_REWARDED, DEFERRED_OFFLINE, DEFERRED_INVENTORY, INVALID_EVIDENCE, DEBT_FAILURE }`.
- Produces: `ClosureValidationResult(boolean valid, Optional<ClosureOutcome> trustedOutcome, Component message)`.
- Produces: `ArchitectonicClosureValidatorRegistry.register(ResourceLocation cause, ArchitectonicClosureValidator): void`.
- Produces: one `mnagnosis:fixed_point` for the first eligible perfect Closure
  of a work, whether or not that work has a matching Dislocation debt.

- [ ] **Step 1: Write ledger and anti-duplication tests**

Assert Dislocation is stored in the existing `ContradictionLedger`, respects
ledger cap/vent ordering, syncs existing authorship HUD state, serializes cause
and work UUID, and closes only when a feature-specific validator supplies a
matching `ClosureOutcome`. Assert a safe work Closure with no debt may still
claim its proof reward. Assert repeated calls, reconnects, a full inventory,
death after confirmed delivery, and server restart yield at most one Fixed
Point. Inject failure after every reward-ledger save, optional-debt close,
player-data save, inventory verification, and final ledger save. Each phase must
provide both at-most-once issuance and eventual delivery once the owner is
online with one free slot.

- [ ] **Step 2: Run the closure test and verify RED**

Run: `./gradlew.bat test --tests "*ArchitectonicContradictionServiceTest"`

Expected: compilation fails because the service does not exist.

- [ ] **Step 3: Implement the shared-ledger bridge**

Use `lawId=mnagnosis:architectonics/dislocation` and
`interpretationId=cause`. Payload schema `1` contains work UUID, owner,
dimension-qualified anchors, durable imbalance summary, `reward_claimed`, and
feature payload no larger than `16 KiB`. Use the same paradox reconciliation
and vent callbacks as other authored laws.

Register exactly one `WorldSeamClosureCallbacks` adapter. For
`prepareForced`, call `createForReceipt` before permitting seam removal.
`createForReceipt` stores the feature receipt UUID in the Contradiction payload
and a bounded idempotency index; replay returns the original Contradiction ID
without adding Paradox or venting twice. Capacity, ownership, or payload
failure returns a durable rejection and leaves the seam intact. For
`onSafeClosed`, first translate the feature-local
`WorldSeamClosureEvidence` into server-owned Closure evidence, then durably
queue/grant `CLOSE_WORLD_SEAM`, call `completeClosure`, and acknowledge the
plan-13 receipt only after the reward ledger is at least `PENDING`. Replay of a
closed, unacknowledged receipt is the normal recovery path. Cap the receipt
idempotency index at the same `4,096` global Closure-evidence ceiling and
compact an entry only after its feature receipt is acknowledged and its debt
or reward reaches a terminal durable phase.

- [ ] **Step 4: Implement exact Closure outcomes**

```java
public record ClosureOutcome(
        UUID workId,
        ResourceLocation cause,
        boolean restoredOriginalRelation,
        boolean reconciledDurableCrossings,
        boolean clearOfTransientFlux,
        CompoundTag evidence
) {
    public boolean perfect() {
        return restoredOriginalRelation
                && reconciledDurableCrossings
                && clearOfTransientFlux;
    }
}
```

Feature validators create the outcome; the shared service never trusts a
client assertion. `ArchitectonicClosureRewardSavedData` lives in overworld
data storage under `mnagnosis_architectonic_closure_rewards`. It stores one
schema-one `ClosureRewardDelivery` per claimed work UUID with owner, cause,
optional contradiction ID, bounded validator evidence hash, a preselected
random `deliveryReceipt` UUID, and phase `PENDING`, `DEBT_RECONCILED`,
`PREPARED`, or `DELIVERED`.

`completeClosure` first validates the outcome, then calls `begin` and
immediately saves the `PENDING` entitlement before changing the optional debt.
It closes the matching ledger entry idempotently, marks
`DEBT_RECONCILED`, saves again, grants the feature's one-time Closure proof, and
calls `deliverOrDefer`. A preexisting delivery never creates another receipt:
`DELIVERED` returns `ALREADY_REWARDED`, while an incomplete phase resumes the
same delivery.

Delivery never drops an unconfirmed reward into the world. With no free
inventory slot or an offline owner, keep `DEBT_RECONCILED` and retry on login,
respawn, inventory close, and once per 20 ticks. With space, save `PREPARED`
before inserting exactly one count-one Fixed Point tagged with the stable
receipt/work/cause, call public `server.getPlayerList().saveAll()` as the rare
player-data durability barrier (`PlayerList.save(ServerPlayer)` is protected in
1.20.1), rescan for exactly that receipt, then mark
`DELIVERED` and save the reward ledger. No event dispatch or tick is allowed
inside that server-thread critical section.

Recovery of `PREPARED` first scans the online or persisted owner inventory and,
defensively, loaded `ItemEntity` stacks for the receipt. If exactly one exists,
mark `DELIVERED`; if duplicates exist, retain one and invalidate the others
before any recipe can consume them. If none exists, reset to
`DEBT_RECONCILED` and retry the same receipt: because delivery never drops and
the insert/player-save/ledger-save sequence does not yield to gameplay, absence
from durable player data proves the attempted insert did not become a movable
item. A `DELIVERED` reward is ordinary property thereafter and is never reminted
if the player later drops, consumes, or loses it.

Register validators once during common setup, install the one production
`ArchitectonicClosureReceiptSink`, and use the following executable producer
matrix. Task 4 also registers the default-deferred
`InteriorReleaseClosureListeners` adapter, which submits cause
`mnagnosis:unbounded_interior` through that sink and acknowledges only on a
durable entitlement. Duplicate sink, listener, or validator registration fails
startup:

| Cause | Authoritative evidence and call site |
|---|---|
| `mnagnosis:reassembled_land` | Closed Reassembled receipt whose durable live hash equals its original hash; the registered adapter submits it after receipt closure saves |
| `mnagnosis:axiom_of_adjacency` | Owner-close tombstone for a persisted `used=true` work with no crossing in flight; the registered adapter submits it after backend/work removal saves |
| `mnagnosis:hollow_domain` | Closed receipt from an explicit `OWNER_REQUEST` proving that the owner entered, was evacuated to a loaded safe position, and the work was removed; expiry, replacement, and administrative cleanup are ineligible |
| `mnagnosis:coordinate_transposition` | Closed Transposition receipt whose inverse original hash passed the endpoint durability barrier; the registered adapter submits it after receipt closure saves |
| `mnagnosis:unbounded_interior` | Closed feature-local release receipt proving occupants evacuated, threshold removed, shell/cell cleared, and Casket unbound; the Task-4 listener submits its stable evidence UUID after cleanup saves |
| `mnagnosis:world_seam` | Closed feature receipt carrying reconciled `SeamClosureLedger` evidence with no pending durable crossing or transient flux; the combined callback submits it and acknowledges only after shared durability |

Each producer first writes a bounded schema-one record to
`ArchitectonicClosureEvidenceSavedData`, keyed by evidence/work UUID and cause.
The registered validator re-reads that server-owned record, checks owner/cause,
feature-specific hashes and terminal phase, and constructs or confirms the
three `ClosureOutcome` booleans. `completeClosure` rejects an unregistered cause,
missing/mismatched evidence, client packet invocation, or non-perfect result.
Evidence remains until the reward reaches `DELIVERED`, then compacts to a
claimed UUID tombstone. Integration tests invoke every producer, corrupt each
evidence field, replay it after restart, and prove only the valid terminal path
grants its proof/reward once.

- [ ] **Step 5: Activate the pre-registered Fixed Point as proof-only material**

Consume the item registered in plan `00`; do not register it a second time.
It retains no crafting recipe, ordinary loot table, or creative-tab source. It
is fire resistant, stacks to `16` only when metadata matches, carries bounded
closure work/cause/receipt display data, and is used by Transposition Loom,
Unbounded Casket, and Seam-Ripper recipes.

- [ ] **Step 6: Run tests and Closure GameTests**

Run: `./gradlew.bat test --tests "*ArchitectonicContradictionServiceTest" --tests "*ClosureRewardDeliveryServiceTest" --tests "*ArchitectonicClosureValidatorRegistryTest"`

Expected: all ledger, vent, Closure, crash-phase, eventual-delivery, and reward
anti-duplication tests pass.

Run: `./gradlew.bat runGameTestServer`

Expected: safe Closure awards one Fixed Point and proof; forced or incomplete
Closure creates/retains Dislocation and awards none.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/architectonics src/main/java/com/vincenthuto/mnagnosis/common/item/FixedPointItem.java src/main/java/com/vincenthuto/mnagnosis/common/authorship/law/AuthoredLawRegistry.java src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java src/main/resources/assets/mnagnosis src/test/java/com/vincenthuto/mnagnosis/common/architectonics/closure src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsIntegrationGameTests.java
git commit -m "feat: reconcile dislocation through closure"
```

### Task 5: Save schema audit, migrations, and recovery commands — Core

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/migration/ArchitectonicsDataVersion.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/migration/ArchitectonicsMigrator.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/migration/MigrationReport.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/migration/ArchitectonicsMigratorTest.java`
- Test: `src/test/resources/architectonics/saves/schema-1-all-work-types.nbt`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicSavedData.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicEvents.java`

**Interfaces:**
- Produces: global data schema version `1`.
- Produces: `migrate(CompoundTag): DataResult<MigrationReport>`.
- Preserves: unknown work entries byte-for-byte.
- Adds: `/mnagnosis architectonics recover`, `quarantine list`, and
  `quarantine restore <uuid>` operator commands.

- [ ] **Step 1: Create a golden save containing every work type**

The fixture contains the minimum and maximum valid form of all ten
`ArchitectonicWorkType` payloads, one unknown future type, Lattice schema,
Manuscript state, Dislocation entry, casket allocation, transposition journal,
World Seam closure ledger, and a paused unloaded-dimension endpoint.

- [ ] **Step 2: Write migration idempotence tests**

Assert loading schema 1 and saving twice is byte-stable after canonical key
ordering, unknown entries survive, corrupt known entries move to quarantine,
transactions recover according to their journals, duplicate work IDs resolve
to the highest revision, and no migration loads a dimension or chunk.

- [ ] **Step 3: Run the migration test and verify RED**

Run: `./gradlew.bat test --tests "*ArchitectonicsMigratorTest"`

Expected: compilation fails because migration classes do not exist.

- [ ] **Step 4: Implement monotonic migration dispatch**

Reject negative/future global schemas for automatic mutation and retain the
raw file for operator inspection. Apply one pure NBT transform per version.
Write a backup `mnagnosis_architectonics.pre-v<schema>.dat` before the first
successful migration and never overwrite an existing backup.

- [ ] **Step 5: Implement recovery diagnostics**

`recover` rebuilds the spatial index, replays incomplete transposition
journals, removes orphan optional-backend faces, verifies casket cell
allocations, and emits a count-only report. It never closes a Seam, releases an
occupied interior cell, or deletes quarantine automatically.

- [ ] **Step 6: Run migration tests and restart GameTests**

Run: `./gradlew.bat test --tests "*ArchitectonicsMigratorTest"`

Expected: all golden-save, idempotence, unknown, and quarantine tests pass.

Run: `./gradlew.bat runGameTestServer`

Expected: all work types survive a simulated restart and reindex without
duplicate effects or forced chunks.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/architectonics/migration src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime src/test/java/com/vincenthuto/mnagnosis/common/architectonics/migration src/test/resources/architectonics/saves
git commit -m "feat: migrate and recover architectonics saves"
```

### Task 6: Performance, multiplayer, protection, and backend acceptance matrix — Core

**Files:**
- Create: `docs/testing/architectonics-acceptance-matrix.md`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/ArchitectonicsPerformanceContractTest.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/ArchitectonicsDedicatedServerIsolationTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsIntegrationGameTests.java`
- Modify: `README.txt`

**Interfaces:**
- Produces: one repeatable matrix for all components, shapes, instruments,
  apparatus, save/restart paths, and optional backend states.
- Establishes: measurable tick, packet, memory, and work-count budgets.

- [ ] **Step 1: Write the matrix before running it**

For every plan `01`–`13`, include:

- single-player native create/use/expire/close;
- two-player owner/ally/hostile behavior;
- dedicated server with no client classes;
- protection adapter allow/deny;
- source and destination chunk unload/reload;
- logout/death/respawn;
- server save/stop/restart;
- config-cap rejection and lowering;
- malformed/stale packet;
- native backend;
- Immersive Portals absent;
- Immersive Portals present for plans `07`–`09`, `12`, and `13`;
- Oculus present with and without Immersive Portals;
- forced backend failure and native fallback.

- [ ] **Step 2: Write performance contract tests**

Generate 256 inert indexed works and assert an empty nearby query examines no
more than eight index cells and returns within `2 ms` median over 1000
iterations on the test JVM. Assert snapshots remain below `64 KiB`, one player
receives no more than `128 KiB/s` steady-state Architectonics traffic, and no
single feature resolves more subjects/blocks than its config cap.

- [ ] **Step 3: Run unit and GameTest suites**

Run: `./gradlew.bat clean test`

Expected: all unit, asset, codec, contract, and classloading tests pass.

Run: `./gradlew.bat runGameTestServer`

Expected: all existing and Architectonics GameTests pass.

- [ ] **Step 4: Run dedicated and client matrices**

Run: `./gradlew.bat runServer --args nogui`

Verify: dedicated server reaches ready state with no client classloading,
optional dependency, codec, or mixin errors.

Run: `./gradlew.bat runClient`

Verify native rows, then repeat the specified rows with Immersive Portals
3.0.7 and its runtime dependencies, and with Oculus. Record mod versions,
result, log path, and screenshot/video evidence in the acceptance matrix.

- [ ] **Step 5: Profile maximum supported workloads**

Use Spark or Java Flight Recorder outside the distributed mod. Exercise max
Boundary/Lattice resolutions, 256 works, Hollow Domain 8000 blocks,
Transposition 512+512 blocks, four 32x32 World Seams, and 20 simultaneous
crossing entities. Acceptance: Architectonics adds under `5 ms` to the 95th
percentile server tick in the synthetic stress scene and allocates under
`2 MiB/s` after warmup. If a Core feature misses budget, reduce its default cap
before release rather than relying on optional rendering.

- [ ] **Step 6: Commit**

```powershell
git add docs/testing/architectonics-acceptance-matrix.md src/test/java/com/vincenthuto/mnagnosis/common/architectonics src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsIntegrationGameTests.java README.txt
git commit -m "test: verify architectonics integration matrix"
```

### Task 7: Staged release cuts and final verification — Core

**Files:**
- Create: `docs/architectonics-player-guide.md`
- Create: `docs/architectonics-operator-guide.md`
- Create: `docs/architectonics-compatibility.md`
- Create: `docs/architectonics-changelog.md`
- Modify: `docs/testing/architectonics-acceptance-matrix.md`

**Interfaces:**
- Produces: player-facing controls/Closure guidance, operator recovery/config
  guidance, compatibility limits, and staged changelog.
- Produces: seven independently releasable cuts with explicit go/no-go gates.

- [ ] **Step 1: Document the seven release cuts**

1. Shared runtime, Unbounded Lattice, Reassembled Land, Boundary Condition,
   and Lattice Emanation.
2. Axial Ordination and Load-Bearing Principle.
3. Metric Compression and Euclidean Refusal.
4. Axiom of Adjacency Core channels.
5. Hollow Domain and Coordinate Transposition.
6. Unbounded Interior.
7. World Seam, then optional Enhancement channels/recursive views.

Each cut requires manifest tests, unit tests, GameTests, dedicated-server
start, native multiplayer row, save/restart row, no high-severity log errors,
and migration from the previous cut.

- [ ] **Step 2: Write player guidance**

Explain Lattice point grammars, offhand consumption, all modifier mappings,
projection versus material operations, Closure feedback, Fixed Point origin,
safe failure messages, interior recovery, and World Seam reconciliation.
State that unloaded endpoints pause rather than force chunks.

- [ ] **Step 3: Write operator guidance**

Document every config key/default/range, permissions, cap-lowering behavior,
diagnostic/recovery/quarantine commands, transaction journals, casket cell
recovery, World Seam forced Closure consequences, backup paths, and optional
backend fallback.

- [ ] **Step 4: Run the complete verification from a clean tree**

Run: `./gradlew.bat clean test runGameTestServer build`

Expected: `BUILD SUCCESSFUL`, all tests pass, and the reobfuscated jar is
created.

Run: `jar tf build/libs/mnagnosis-*.jar | Select-String "architectonics|reassembled_land|world_seam|unbounded"`

Expected: every manifest asset/data/class family is present and no third-party
class is bundled.

- [ ] **Step 5: Inspect logs and working tree**

Run:

```powershell
Select-String -Path 'run\logs\latest.log' -Pattern 'ERROR|Exception|Mixin apply failed|Missing mapping'
git status --short
git diff --check
```

Expected: no unexplained runtime errors, only intended files are changed, and
`git diff --check` reports no whitespace errors.

- [ ] **Step 6: Commit**

```powershell
git add docs/architectonics-player-guide.md docs/architectonics-operator-guide.md docs/architectonics-compatibility.md docs/architectonics-changelog.md docs/testing/architectonics-acceptance-matrix.md
git commit -m "docs: prepare architectonics release"
```
