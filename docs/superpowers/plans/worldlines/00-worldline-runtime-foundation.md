# Worldline Runtime Foundation Implementation Plan

> **Prerequisite:** Implement against the authoritative
> [three-discipline foundation contract](../../specs/2026-07-29-three-discipline-foundation-contract.md).
> Retain protocol `"5"` and allocate only IDs `48-63`. Reuse its cast,
> Manuscript, instrument, and Contradiction contracts.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the authoritative persistence, targeting, transaction, Remainder, networking, and visual infrastructure consumed by every Worldline feature.

**Architecture:** An overworld `SavedData` directory owns typed, revisioned sessions while narrow player and entity capabilities own private memories and active spatial leases. Feature handlers register codecs and behavior through bounded interfaces; clients receive only sanitized visual views, and the existing Authorship ledger remains the sole source of causal debt.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1/47.4, Mana and Artifice 3.1.11 API, Curios, SimpleChannel, NBT, JUnit 5, Forge GameTest

## Global Constraints

- Package root is `com.vincenthuto.mnagnosis`.
- Runtime IDs and NBT keys use the `mnagnosis` namespace and snake case.
- Active Worldline sessions are stored in overworld `WorldlineSavedData`; feature handlers never create a second saved-data store.
- Session payloads are typed and versioned. A public API must not accept an untyped `CompoundTag` payload.
- Every mutation increments the session revision.
- Corrupt or newer-version records enter `QUARANTINED`; they are retained and block overlapping work rather than being silently discarded.
- Closed tombstones remain for 1,200 ticks, capped at 64 per source dimension.
- Clients never receive authoritative block snapshots, arbitrary entity NBT, inventories, or permission data.
- There is no persistent Worldline chunk ticket and no Immersive Portals dependency.
- Other players are invalid component targets unless a feature plan defines explicit consent.
- Health, death, inventory, equipment, Curios, durability, mana, XP, advancements, knowledge, cooldowns, hunger, AI memory, and ownership are immutable ledgers.
- Worldline Remainders use the existing `ContradictionLedger`; no second resource bar is introduced.
- Preserve the network's existing packet IDs 0–4 and bump its protocol from `"5"` to `"6"` once.

---

## Immersive Portals Reference Boundary

Use the official 1.20.1
[`PortalState`](https://github.com/iPortalTeam/ImmersivePortalsMod/blob/1.20.1/imm_ptl_core/src/main/java/qouteall/imm_ptl/core/portal/PortalState.java)
as inspiration for keeping dimensions, origins, orientation, scale, and bounds
in one canonical serializable value while deriving render geometry and caches.
Use its
[`PortalRendering`](https://github.com/iPortalTeam/ImmersivePortalsMod/blob/1.20.1/imm_ptl_core/src/main/java/qouteall/imm_ptl/core/render/context_management/PortalRendering.java)
scoping pattern as inspiration for guaranteed render-state cleanup, and use
the official
[implementation notes](https://qouteall.fun/immptl/wiki/Implementation-Details.html)
as a checklist of systems MnAGnosis deliberately does not need.

Do not copy or depend on Immersive Portals code. Worldlines do not implement
multiple loaded client worlds, dimension-wrapped vanilla packets, remote
chunk visibility, recursive stencil rendering, front clipping, cross-world
collision, remote interaction, or seamless client-authoritative teleport.

## File Structure

Create focused files under
`src/main/java/com/vincenthuto/mnagnosis/common/worldline/`:

- `WorldlinePhase.java`, `WorldlineSessionHeader.java`,
  `WorldlineSessionType.java`, `WorldlinePayloadCodec.java`,
  `WorldlineSessionTypes.java`, `WorldlineSessionHandler.java`,
  `WorldlineSessionRecord.java`,
  `WorldlineActionResult.java`, `WorldlineRecoveryReason.java`, and
  `WorldlineRuntime.java` own the state machine and type dispatch.
- `WorldlineSavedData.java` owns persistence, spatial indexing, tombstones,
  quarantine, and the server-private signing secret.
- `WorldlineSignatureService.java` signs and verifies bounded canonical
  payloads for Artifice items such as the Causal Spindle. It never exposes
  key material to a client.
- `WorldlineTargetRef.java`, `WorldlineSurfaceTransform.java`,
  `WorldlineBounds.java`, `WorldlineSafePlacement.java`, and
  `WorldlinePlacementResult.java` own canonical spatial data.
- `WorldlinePermissionPolicy.java` centralizes player consent and Forge
  entity/block permission checks used by Long Moment and Unfinished Hour.
- `WorldlineMemory.java`, `IWorldlineMemory.java`,
  `WorldlineMemoryProvider.java`, and `WorldlineMemoryEvents.java` own private
  player memories and processed-action receipts.
- `WorldlineAuthorshipAccess.java` maps the existing Tier-6/Ineffable state
  and durable practiced-Law receipts to feature progression gates.
- `WorldlineBookmarkMemory.java`, `WorldlinePath.java`,
  `WorldlinePathCodec.java`, and `WorldlinePathRecording.java` are the typed,
  bounded values stored by that capability; Plans 01 and 02 implement their
  feature behavior without replacing these records or their codec.
- `WorldlineEntityState.java`, `IWorldlineEntityState.java`,
  `WorldlineEntityStateProvider.java`, and `WorldlineEntityEvents.java` own
  one active spatial lease per entity.
- `WorldlineSpatialMode.java` and `WorldlineSpatialLease.java` are the shared
  identity, owner, mode, and timing record used by Plans 03, 04, 06, and 08.
  Feature-specific restoration data remains in the typed session payload.
- `WorldlineExecutionGuard.java`, `ResolutionEnvelope.java`,
  `WorldlineResolutionCapture.java`, `WorldlineResolutionResult.java`,
  `WorldlineResolutionAdapter.java`, `WorldlineResolutionAdapters.java`,
  `WorldlineConsequenceContext.java`, `WorldlineConsequenceAdapter.java`, and
  `WorldlineConsequenceAdapters.java`, plus
  `WorldlineCapturedConsequence.java`, own guarded replay and consequence
  transfer.
- `WorldlineEvent.java`, `WorldlineJournal.java`,
  `WorldlineHistoryFrame.java`, `WorldlineHistory.java`,
  `WorldlineMutationAdapter.java`, `WorldlineMutationAdapters.java`,
  `WorldlineMutationValidation.java`, `WorldlineMutationContext.java`,
  `WorldlineMutationResult.java`, and `WorldlineEvents.java` own bounded
  observation and reversible transactions.
- `WorldlineRemainderService.java`, `WorldlineRemainderHandler.java`,
  `WorldlineRemainderType.java`, `WorldlineRemainderPayloadCodec.java`,
  `WorldlineRemainderLifecycle.java`, `WorldlineRemainderTypes.java`,
  `WorldlineTypedDebt.java`, and `WorldlineDebtResult.java` integrate typed
  external causal debts with Authorship.
- `WorldlineApi.java`, `WorldlineStartResult.java`,
  `WorldlineDecisionResult.java`, and `WorldlineFailure.java` expose stable
  calls to feature packages.
- `WorldlineSessionView.java` and `WorldlineVisualEvent.java` are sanitized
  common-side network values. They contain no client classes and may be
  produced safely on a dedicated server.

Create client files under
`src/main/java/com/vincenthuto/mnagnosis/client/worldline/`:

- `ClientWorldlineSessions.java`, `WorldlineVisualTimer.java`,
  `WorldlineRenderScope.java`, and
  `WorldlineRenderer.java`. The client package imports the common
  `WorldlineSessionView`; it does not define a second wire type.

Create shared packet files under
`src/main/java/com/vincenthuto/mnagnosis/common/network/`:

- `WorldlineSessionUpsertS2C.java`
- `WorldlineSessionRemoveS2C.java`
- `WorldlineVisualBatchS2C.java`
- `WorldlineActionC2S.java`

Create tests under:

- `src/test/java/com/vincenthuto/mnagnosis/common/worldline/`
- `src/test/java/com/vincenthuto/mnagnosis/client/worldline/`
- `src/main/java/com/vincenthuto/mnagnosis/gametest/WorldlineRuntimeGameTests.java`

Modify only where required:

- `src/main/java/com/vincenthuto/mnagnosis/common/network/NetworkHandler.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/authorship/law/AuthoredLawHandler.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/authorship/law/AuthoredLawRegistry.java`
- `src/main/java/com/vincenthuto/mnagnosis/common/authorship/state/IneffableCastingStateEvents.java`
- `src/main/java/com/vincenthuto/mnagnosis/Config.java`
- `src/main/java/com/vincenthuto/mnagnosis/client/ClientConfig.java`
- `src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java`

### Task 1: Define canonical domain types and codecs

**Files:**
- Create: the domain-type files listed above.
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineDomainTest.java`

**Interfaces:**
- Produces:
  - `WorldlineSessionHeader`
  - `WorldlineTargetRef`
  - `WorldlineSurfaceTransform`
  - `WorldlineBounds`
  - `WorldlineSessionType<T>`
  - `WorldlineSessionHandler<T>`
  - `WorldlineSessionRecord<T>`

- [ ] **Step 1: Write failing domain and codec tests**

Test entity and block target round trips, quaternion normalization, source
dimension preservation, phase validation, unknown payload versions, bounds
chunk calculation, and revision monotonicity:

```java
@Test
void headerTransitionAlwaysAdvancesRevision() {
    WorldlineSessionHeader active = fixture(WorldlinePhase.ACTIVE, 7L);
    WorldlineSessionHeader deciding =
            active.transition(WorldlinePhase.AWAITING_DECISION, 220L);

    assertEquals(8L, deciding.revision());
    assertEquals(WorldlinePhase.AWAITING_DECISION, deciding.phase());
    assertThrows(
            IllegalStateException.class,
            () -> deciding.transition(WorldlinePhase.PREPARING, 221L)
    );
}
```

- [ ] **Step 2: Run the domain test and verify it fails**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineDomainTest"
```

Expected: test compilation fails because the Worldline domain types do not
exist.

- [ ] **Step 3: Implement the exact phase and target contracts**

```java
public enum WorldlinePhase {
    PREPARING,
    ACTIVE,
    AWAITING_DECISION,
    RESOLVING_ACCEPT,
    RESOLVING_RESTORE,
    RECOVERING,
    QUARANTINED,
    CLOSED
}

public record WorldlineSessionHeader(
        UUID id,
        ResourceLocation typeId,
        UUID ownerId,
        ResourceKey<Level> sourceDimension,
        WorldlineBounds bounds,
        WorldlinePhase phase,
        long createdGameTime,
        long deadlineGameTime,
        long revision
) {
    public WorldlineSessionHeader transition(
            WorldlinePhase next,
            long nextDeadline
    );
}

public sealed interface WorldlineTargetRef
        permits WorldlineTargetRef.EntityTarget, WorldlineTargetRef.BlockTarget {
    ResourceKey<Level> dimension();

    record EntityTarget(
            ResourceKey<Level> dimension,
            UUID entityId
    ) implements WorldlineTargetRef {}

    record BlockTarget(
            ResourceKey<Level> dimension,
            BlockPos pos,
            Direction face
    ) implements WorldlineTargetRef {}
}
```

Legal state transitions are:

```text
PREPARING -> ACTIVE | RECOVERING | QUARANTINED
ACTIVE -> AWAITING_DECISION | RESOLVING_ACCEPT | RECOVERING | QUARANTINED
AWAITING_DECISION -> RESOLVING_ACCEPT | RESOLVING_RESTORE | RECOVERING | QUARANTINED
RESOLVING_ACCEPT -> CLOSED | RECOVERING | QUARANTINED
RESOLVING_RESTORE -> ACTIVE | CLOSED | RECOVERING | QUARANTINED
RECOVERING -> CLOSED | QUARANTINED
QUARANTINED -> RECOVERING
CLOSED -> no transition
```

`WorldlineSurfaceTransform` must reject non-finite coordinates, non-positive
scale, and non-normalizable quaternions. Its `toDestination` and `toSource`
methods are exact inverses within `1.0E-6`.

- [ ] **Step 4: Define typed session dispatch**

```java
public record WorldlineSessionType<T>(
        ResourceLocation id,
        int payloadVersion,
        WorldlinePayloadCodec<T> codec,
        WorldlineSessionHandler<T> handler
) {}

public final class WorldlineSessionTypes {
    public static <T> void register(WorldlineSessionType<T> type);
    public static Optional<WorldlineSessionType<?>> get(ResourceLocation id);
    public static Set<ResourceLocation> ids();
}

public interface WorldlinePayloadCodec<T> {
    CompoundTag encode(T value);
    DataResult<T> decode(int storedVersion, CompoundTag tag);
}

public interface WorldlineSessionHandler<T> {
    WorldlineSessionRecord<T> tick(
            MinecraftServer server,
            WorldlineSessionRecord<T> record
    );

    WorldlineSessionRecord<T> recover(
            MinecraftServer server,
            WorldlineSessionRecord<T> record,
            WorldlineRecoveryReason reason
    );

    WorldlineActionResult<T> act(
            ServerPlayer actor,
            WorldlineSessionRecord<T> record,
            ResourceLocation action,
            UUID nonce,
            CompoundTag boundedPayload
    );

    WorldlineSessionView visualView(
            ServerPlayer observer,
            WorldlineSessionRecord<T> record
    );
}

public record WorldlineActionResult<T>(
        WorldlineDecisionResult decision,
        WorldlineSessionRecord<T> record
) {}
```

The type registry rejects duplicate IDs. Decode failures retain the raw tag in
a quarantined record and log the session UUID, type ID, payload version, and
source dimension.

- [ ] **Step 5: Run the focused test**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineDomainTest"
```

Expected: all domain, transition, and codec tests pass.

- [ ] **Step 6: Commit the domain layer**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/worldline src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineDomainTest.java
git commit -m "feat: define worldline runtime domain"
```

### Task 2: Persist sessions, tombstones, and quarantine

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineSavedData.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineRuntime.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineSignatureService.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineSavedDataTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineSignatureServiceTest.java`

**Interfaces:**
- Consumes: `WorldlineSessionType<T>` and `WorldlineSessionRecord<T>`.
- Produces:

```java
public final class WorldlineRuntime {
    public static WorldlineRuntime get(MinecraftServer server);
    public <T> WorldlineStartResult start(
            WorldlineSessionType<T> type,
            WorldlineSessionHeader header,
            T payload
    );
    public Optional<WorldlineSessionRecord<?>> session(UUID id);
    public List<WorldlineSessionRecord<?>> sessions(ResourceLocation typeId);
    public List<WorldlineSessionRecord<?>> sessionsIntersecting(
            ResourceKey<Level> dimension,
            AABB bounds
    );
    public WorldlineJournal journal();
    public WorldlineHistory history();
    public WorldlineRemainderService remainders();
    public WorldlineSignatureService signatures();
    public void reconcileEntity(Entity entity);
    public WorldlineDecisionResult act(
            ServerPlayer actor,
            UUID sessionId,
            long expectedRevision,
            ResourceLocation action,
            UUID nonce,
            CompoundTag boundedActionPayload
    );
    public void tick(MinecraftServer server);
}

public final class WorldlineSignatureService {
    public byte[] sign(
            UUID ownerId,
            ResourceLocation purpose,
            byte[] canonicalPayload
    );
    public boolean verify(
            UUID ownerId,
            ResourceLocation purpose,
            byte[] canonicalPayload,
            byte[] signature
    );
}
```

`sessions(typeId)` returns only active, non-quarantined records in stable
creation order. Plans 12 and 13 use it for global cell and apparatus caps;
feature plans must not maintain a second counter.

The runtime owns one instance of each service returned by `journal()`,
`history()`, `remainders()`, and `signatures()`. These accessors are the
only feature-facing injection seam; feature code must not instantiate,
reflect into, or cache a separate service.

- [ ] **Step 1: Write failing persistence tests**

Cover stable ordering, two sessions from different dimensions, owner and
server caps, overlap queries, tombstone expiry, duplicate starts, stale
actions, corrupt payload quarantine, total serialized-size rejection, and
stable `sessions(typeId)` filtering.

The hard caps are:

```java
static final int MAX_ACTIVE_PER_OWNER = 8;
static final int MAX_TOMBSTONES_PER_DIMENSION = 64;
static final long TOMBSTONE_TICKS = 1_200L;
static final int MAX_ACTION_BYTES = 4 * 1_024;
static final int MAX_SAVED_BYTES_PER_SOURCE_DIMENSION = 2 * 1_024 * 1_024;
```

- [ ] **Step 2: Verify the tests fail**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineSavedDataTest"
```

Expected: compilation fails because `WorldlineSavedData` and
`WorldlineRuntime` do not exist.

- [ ] **Step 3: Implement overworld storage**

Use data name `mnagnosis_worldlines`. Store:

```text
schema
next_order
signing_secret
sessions[]
  header
  payload_version
  payload
  raw_quarantine_payload
tombstones[]
  session_id
  source_dimension
  terminal_revision
  closed_at
processed_action_ids[]
```

`signing_secret` is exactly 32 random bytes generated with `SecureRandom`
when the saved data is first created and persisted before any signature is
issued. A missing legacy key is generated and marks the data dirty. A key
with any other length is logged and replaced; existing signatures then fail
closed. The key is never included in a packet, diagnostic dump, log, item
tooltip, or feature payload.

Resolve the overworld through `server.overworld()`. Mark the data dirty before
performing an external side effect, and mark the action/session receipt before
releasing an entity, applying a replay, granting an item, or editing a block.

- [ ] **Step 4: Implement revisioned action handling**

Reject actions when:

- Actor is not the session owner or an explicitly registered participant.
- Session is terminal or quarantined.
- `expectedRevision` differs from the current revision.
- Payload exceeds 4 KiB.
- Nonce has already been claimed.
- Handler does not declare the action ID.

A rejected stale action returns `WorldlineFailure.STALE_REVISION` and requests
a fresh owner sync without mutating the record.

- [ ] **Step 5: Implement and test server-private signatures**

Use `HmacSHA256` with the persisted 32-byte secret. Domain-separate the MAC
input with a fixed schema byte, the owner's 16 UUID bytes, the UTF-8
`purpose` ID, and the canonical payload length before the payload bytes.
Reject canonical payloads over 32 KiB, reject signatures whose length is not
exactly 32 bytes, and compare expected and supplied signatures with
`MessageDigest.isEqual`.

The service accepts bytes that a feature-specific codec has already placed
in a deterministic field order; it does not serialize arbitrary NBT.
Plan 02 uses purpose `mnagnosis:causal_spindle_path` and
`WorldlinePathCodec.canonicalBytes(path)`.

Tests cover signature survival across save/reload, distinct owners and
purposes, payload tampering, wrong-length signatures, the 32 KiB bound,
legacy missing-key generation, corrupt-key rotation, and failure after
rotation.

- [ ] **Step 6: Run persistence and signature tests**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineSavedDataTest" --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineSignatureServiceTest"
```

Expected: all persistence, cap, quarantine, idempotence, and signature tests
pass.

- [ ] **Step 7: Commit persistence**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineSavedData.java src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineRuntime.java src/main/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineSignatureService.java src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineSavedDataTest.java src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineSignatureServiceTest.java
git commit -m "feat: persist worldline sessions"
```

### Task 3: Add private memory and spatial-lease capabilities

**Files:**
- Create: the `WorldlineMemory*` and `WorldlineEntityState*` files from the
  file structure.
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/CapabilityRegistry.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineCapabilityTest.java`
- Test: `src/main/java/com/vincenthuto/mnagnosis/gametest/WorldlineRuntimeGameTests.java`

**Interfaces:**
- Produces:

```java
public interface IWorldlineMemory {
    Optional<WorldlineBookmarkMemory> bookmark(UUID subjectId);
    void putBookmark(WorldlineBookmarkMemory bookmark);
    Optional<WorldlineBookmarkMemory> removeBookmark(UUID subjectId);
    Optional<WorldlinePath> committedPath();
    void setCommittedPath(WorldlinePath path);
    Optional<WorldlinePathRecording> recording();
    void setRecording(WorldlinePathRecording recording);
    void clearRecording();
    boolean claimAction(UUID actionId);
    boolean hasPracticed(ResourceLocation authorshipId);
    void markPracticed(ResourceLocation authorshipId);
    long lastAssistedMovementTick();
    void markAssistedMovement(long gameTime);
}

public final class WorldlineAuthorshipAccess {
    public static final ResourceLocation RECURRENCE_PRACTICE =
            MnAGnosis.rloc("recurrence");

    public static boolean hasContinuanceAuthorship(ServerPlayer player);
    public static boolean hasRecurrenceAccess(ServerPlayer player);
}

public record WorldlineBookmarkMemory(
        UUID bookmarkId,
        UUID ownerId,
        UUID subjectId,
        ResourceKey<Level> dimension,
        Vec3 feetPosition,
        float yaw,
        float pitch,
        Vec3 velocity,
        float fallDistance,
        EntityDimensions dimensions,
        long createdGameTime,
        long expiresGameTime,
        long insertionOrder,
        List<Vec3> safeCandidates
) {}

public record WorldlinePath(
        ResourceKey<Level> dimension,
        List<Vec3> nodes,
        double arcLength,
        long recordedGameTime,
        UUID ownerId
) {}

public record WorldlinePathRecording(
        ResourceKey<Level> dimension,
        List<Vec3> sampledNodes,
        double arcLength,
        Vec3 lastSample,
        float lastYaw,
        long startedGameTime,
        long deadlineGameTime
) {}

public enum WorldlineSpatialMode {
    DEFERRED,
    CONTINUING,
    STILL,
    FOREGONE
}

public record WorldlineSpatialLease(
        UUID operationId,
        UUID ownerId,
        WorldlineSpatialMode mode,
        long startedGameTime,
        long dueGameTime
) {}

public interface IWorldlineEntityState {
    Optional<WorldlineSpatialLease> lease();
    boolean tryAcquire(WorldlineSpatialLease lease);
    boolean release(UUID operationId);
}

public final class WorldlinePathCodec {
    public static final int SCHEMA = 1;
    public static final int MAX_CANONICAL_BYTES = 3 * 1_024;
    public static CompoundTag encode(WorldlinePath path);
    public static DataResult<WorldlinePath> decode(CompoundTag tag);
    public static byte[] canonicalBytes(WorldlinePath path);
    public static DataResult<WorldlinePath> decodeCanonical(byte[] bytes);
}
```

`WorldlinePathCodec` owns both capability NBT and the deterministic bytes
signed by Plan 02. Quantize every accepted node to `1/4096` block before
storing it. Canonical format version `1` writes fixed field order: schema,
owner UUID, at most 128 UTF-8 bytes of dimension ID, recorded game time,
node count, first absolute position as three signed fixed-point longs, each
later delta as three signed 24-bit fixed-point integers, and recomputed
fixed-point arc length. With 256 nodes this remains below 3 KiB.

Reject non-canonical negative zero, trailing bytes, non-minimal encodings,
delta overflow, non-finite input, an arc-length mismatch, or a decode whose
re-encoding differs byte-for-byte.

- [ ] **Step 1: Write failing capability and clone-policy tests**

Verify:

- Maximum eight bookmarks with deterministic oldest replacement.
- Exactly one committed path and one active recording.
- Sixty-four processed action IDs with oldest-first eviction.
- Sixteen practiced-Authorship IDs with duplicate suppression; these receipts
  survive death and ordinary clone.
- Bookmark data is omitted on death clone.
- Committed path and receipts survive death clone.
- All private memory survives non-death clone.
- Only one entity lease can be acquired.
- A release with the wrong operation UUID changes nothing.
- Path nodes quantize deterministically; a maximum 256-node path remains
  below `MAX_CANONICAL_BYTES` and decodes to the same canonical bytes.
- Non-minimal path bytes, trailing bytes, delta overflow, invalid dimension
  length, and arc-length mismatch fail without replacing private memory.

- [ ] **Step 2: Run the tests and verify failure**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineCapabilityTest"
```

Expected: compilation fails because the capability types do not exist.

- [ ] **Step 3: Implement capability serialization and events**

Attach `mnagnosis:worldline_memory` to players and
`mnagnosis:worldline_entity` to living entities, projectiles, item entities,
and falling blocks. Register both through `RegisterCapabilitiesEvent`.

On `PlayerEvent.Clone`, call:

```java
newMemory.copyFrom(oldMemory, event.isWasDeath());
```

`hasContinuanceAuthorship` is true only when the M&A progression capability
reports Tier 6 and allied faction
`IneffableFactionRegistry.INEFFABLE_FACTION`.
`hasRecurrenceAccess` additionally requires
`IWorldlineMemory.hasPracticed(WorldlineAuthorshipAccess.RECURRENCE_PRACTICE)`.
Plan 07 writes that receipt only after the first successful, debt-creating
Recurrence authored cast. This introduces no unfinished manuscript item or
unobtainable progression step.

On entity join or player login, call:

```java
WorldlineRuntime.get(server).reconcileEntity(entity);
```

On logout, forget only client-watch and transient input state. Active
authoritative sessions remain in SavedData.

- [ ] **Step 4: Add GameTests for attachment and reconciliation**

GameTests must prove that a saved lease releases once, a duplicate
reconciliation does nothing, death clone clears bookmarks but preserves a
path, and no health or inventory fields appear in serialized memory.

- [ ] **Step 5: Run focused tests and compile GameTests**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineCapabilityTest"
.\gradlew.bat compileJava
```

Expected: capability tests pass and GameTests compile.

- [ ] **Step 6: Commit capabilities**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/worldline src/main/java/com/vincenthuto/mnagnosis/common/registry/CapabilityRegistry.java src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineCapabilityTest.java src/main/java/com/vincenthuto/mnagnosis/gametest/WorldlineRuntimeGameTests.java
git commit -m "feat: add worldline memory and lease state"
```

### Task 4: Implement safe targeting, placement, and execution guards

**Files:**
- Create: `WorldlineSafePlacement.java`, `WorldlineExecutionGuard.java`,
  `WorldlinePermissionPolicy.java`,
  `ResolutionEnvelope.java`, `WorldlineResolutionAdapter.java`,
  `WorldlineResolutionAdapters.java`, `WorldlineConsequenceAdapter.java`,
  `WorldlineConsequenceAdapters.java`, and
  `WorldlineCapturedConsequence.java`.
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineSafetyTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineExecutionGuardTest.java`

**Interfaces:**
- Produces:

```java
public record ResolutionEnvelope(
        UUID originCastId,
        UUID ownerId,
        ResourceLocation adapterId,
        WorldlineTargetRef target,
        CompoundTag adapterPayload,
        int recurrenceDepth
) {}

public interface WorldlineResolutionAdapter {
    ResourceLocation id();
    boolean supports(ResourceLocation componentId);
    DataResult<ResolutionEnvelope> capture(WorldlineResolutionCapture capture);
    WorldlineResolutionResult replay(
            MinecraftServer server,
            ResolutionEnvelope envelope
    );
    void vent(ServerPlayer owner, ResolutionEnvelope envelope);
}

public interface WorldlineConsequenceAdapter<E> {
    ResourceLocation id();
    Optional<E> capture(WorldlineConsequenceContext context);
    boolean applyAtomically(WorldlineConsequenceContext context, E value);
}

public record WorldlineCapturedConsequence<E>(
        WorldlineConsequenceAdapter<E> adapter,
        E value
) {}

public final class WorldlineResolutionAdapters {
    public static void register(WorldlineResolutionAdapter adapter);
    public static Optional<WorldlineResolutionAdapter> get(
            ResourceLocation adapterId);
    public static Optional<WorldlineResolutionAdapter> forComponent(
            ResourceLocation componentId);
    public static DataResult<ResolutionEnvelope> capture(
            WorldlineResolutionCapture capture);
}

public final class WorldlineConsequenceAdapters {
    public static <E> void register(WorldlineConsequenceAdapter<E> adapter);
    public static Optional<WorldlineConsequenceAdapter<?>> get(
            ResourceLocation adapterId);
    public static Optional<WorldlineCapturedConsequence<?>> capture(
            WorldlineConsequenceContext context);
    public static <E> boolean applyAtomically(
            WorldlineConsequenceContext context,
            WorldlineCapturedConsequence<E> captured);
}

public final class WorldlinePermissionPolicy {
    public static boolean canAffectEntity(
            ServerPlayer actor,
            LivingEntity subject,
            boolean explicitConsent);
    public static boolean canReviseBlock(
            ServerPlayer owner,
            ServerLevel level,
            BlockPos pos,
            InteractionHand hand);
}
```

- [ ] **Step 1: Write failing safety-policy tests**

Cover deterministic 3×3×3 safe-position order, collision, solid support,
lava/fire/powder-snow rejection, world border, unloaded chunk rejection,
vehicle rejection, other-player rejection, same UUID resolution, and
same-dimension non-player rules.

- [ ] **Step 2: Write failing guard and registry tests**

Verify:

- Guard depth begins at zero and unwinds in `finally`.
- Depth greater than one is rejected.
- An exception cannot leave the thread guarded.
- Duplicate adapter/component ownership is rejected.
- Unknown adapter IDs decode as failures rather than falling back.
- Replayed actions preserve the original caster UUID.
- Adapter lookup and capture are stable, reject duplicate IDs, and never
  fall back to arbitrary component or event serialization.
- `canAffectEntity` rejects another player unless `explicitConsent` is true
  and current alliance/PvP/target rules permit the operation.
- `canReviseBlock` applies world border, loaded-chunk, `mayInteract`, Forge
  break/place cancellation, and protection hooks without mutating the block.

- [ ] **Step 3: Run both focused tests**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineSafetyTest" --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineExecutionGuardTest"
```

Expected: compilation fails because the safety and execution types do not
exist.

- [ ] **Step 4: Implement safe placement**

Expose:

```java
public static WorldlinePlacementResult find(
        ServerLevel level,
        Entity subject,
        Vec3 preferred,
        int radius,
        boolean requireSupport
);
```

Search radius in stable Manhattan distance, then Y, X, Z order. Do not call
`getChunk` for an unloaded candidate. Before moving a player, post the Forge
teleport event used by the repository's existing movement features. Return a
typed failure and leave the subject unchanged on cancellation.

- [ ] **Step 5: Implement guarded adapter execution**

Adapters own all serialization and legality checks. The registry initially
contains IDs for:

- `mnagnosis:magic_damage`
- `mnagnosis:true_damage`
- `mnagnosis:finite_effect`
- `mnagnosis:force`

Block mutation, healing, death, inventory, resource, summon, teleport, and
Worldline adapters are absent from the replay registry.

The consequence registry exposes the same four IDs where their event form is
meaningful. Damage capture selects `magic_damage` or `true_damage` from the
authoritative damage type; finite effects and bounded impulses select
`finite_effect` and `force`. Registration is centralized in Plan 14, and a
feature consumes only `capture` plus `applyAtomically`.

- [ ] **Step 6: Run both focused tests**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineSafetyTest" --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineExecutionGuardTest"
```

Expected: all safety, guard, and adapter-registry tests pass.

- [ ] **Step 7: Commit safety and adapters**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/worldline src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineSafetyTest.java src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineExecutionGuardTest.java
git commit -m "feat: guard worldline movement and replay"
```

### Task 5: Implement the bounded event journal and mutation transactions

**Files:**
- Create: the journal and mutation files from the file structure.
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineJournalTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineMutationTest.java`

**Interfaces:**
- Produces:

```java
public sealed interface WorldlineEvent
        permits WorldlineEvent.BlockMutation, WorldlineEvent.Damage,
                WorldlineEvent.Death, WorldlineEvent.SpellResolution,
                WorldlineEvent.Explosion, WorldlineEvent.ProjectileImpact {
    UUID eventId();
    UUID sessionId();
    long gameTime();
    WorldlineVisualEvent visualSummary();

    record BlockMutation(
            UUID eventId, UUID sessionId, long gameTime,
            ResourceKey<Level> dimension, BlockPos pos,
            ResourceLocation sourceId, BlockState before, BlockState after,
            WorldlineVisualEvent visualSummary
    ) implements WorldlineEvent {}

    record Damage(
            UUID eventId, UUID sessionId, long gameTime,
            ResourceKey<Level> dimension, UUID subjectId,
            ResourceLocation damageType, Optional<UUID> attackerId,
            float amount, WorldlineVisualEvent visualSummary
    ) implements WorldlineEvent {}

    record Death(
            UUID eventId, UUID sessionId, long gameTime,
            ResourceKey<Level> dimension, UUID subjectId,
            Optional<UUID> attackerId, WorldlineVisualEvent visualSummary
    ) implements WorldlineEvent {}

    record SpellResolution(
            UUID eventId, UUID sessionId, long gameTime,
            ResourceKey<Level> dimension, UUID ownerId,
            String spellFingerprint, ResourceLocation componentId,
            WorldlineTargetRef target, WorldlineVisualEvent visualSummary
    ) implements WorldlineEvent {}

    record Explosion(
            UUID eventId, UUID sessionId, long gameTime,
            ResourceKey<Level> dimension, Optional<UUID> sourceId,
            Vec3 center, float radius, WorldlineVisualEvent visualSummary
    ) implements WorldlineEvent {}

    record ProjectileImpact(
            UUID eventId, UUID sessionId, long gameTime,
            ResourceKey<Level> dimension, UUID projectileId,
            Optional<UUID> ownerId, WorldlineTargetRef hit,
            WorldlineVisualEvent visualSummary
    ) implements WorldlineEvent {}
}

public interface WorldlineMutationAdapter {
    ResourceLocation id();
    boolean supports(BlockState before, BlockState after);
    WorldlineMutationValidation validate(
            ServerPlayer owner,
            ServerLevel level,
            BlockPos pos,
            BlockState before,
            BlockState after
    );
    WorldlineMutationResult restore(WorldlineMutationContext context);
    WorldlineMutationResult reenact(WorldlineMutationContext context);
}

public final class WorldlineMutationAdapters {
    public static void register(WorldlineMutationAdapter adapter);
    public static Optional<WorldlineMutationAdapter> get(
            ResourceLocation adapterId);
    public static Optional<WorldlineMutationAdapter> find(
            BlockState before,
            BlockState after);
}

public final class WorldlineJournal {
    public void record(WorldlineEvent event);
    public List<WorldlineEvent> events(UUID sessionId);
    public List<WorldlineEvent> recent(
            ResourceKey<Level> dimension,
            AABB bounds,
            long sinceGameTime,
            int limit
    );
    public void authorizeBlockMutation(
            UUID sessionId,
            BlockPos pos,
            ResourceLocation sourceId,
            UUID actionId
    );
}

public record WorldlineHistoryFrame(
        UUID subjectId,
        ResourceKey<Level> dimension,
        Vec3 feetPosition,
        Vec3 velocity,
        float yaw,
        float pitch,
        Pose pose,
        EntityDimensions dimensions,
        long gameTime
) {}

public final class WorldlineHistory {
    public List<WorldlineHistoryFrame> frames(
            UUID observingPlayerId,
            ResourceKey<Level> dimension,
            UUID subjectId,
            long sinceGameTime,
            int limit
    );
}
```

`recent` clamps `limit` to `0..256`, never returns events older than
`sinceGameTime`, and orders equal-tick events by UUID. The runtime retains
only the preceding 40 ticks of non-session history around eligible Tier-6
Ineffable players, capped at 128 tracked subjects per player; session-owned
events follow their owning feature's longer bound.

`WorldlineHistory` samples eligible subjects every five ticks, retains only
the preceding 40 ticks, clamps `limit` to `0..9`, and tracks no more than 128
subjects per eligible Tier-6 Ineffable player. A frame contains movement and
render dimensions only; it never contains health, effects, equipment,
inventory, ownership NBT, or AI state.

Register the foundation property-only mutation adapter as
`mnagnosis:property_transition`. Plans 09 and 13 expose
`mnagnosis:revision_placement`, `mnagnosis:revision_clean_break`, and
`mnagnosis:unfinished_hour_blocks`; Plan 14 registers all four exactly once.

- [ ] **Step 1: Write failing journal-bound tests**

Assert a 256-event cap, stable chronological ordering, duplicate event-ID
suppression, category coalescing after overflow, eight-trace visual-summary
cap for Remainders, and a 40-tick movement-history window.

- [ ] **Step 2: Write failing transaction tests**

Assert:

- Property-only transitions require the same block ID.
- Block entities, fluids, portals, scheduled-tick blocks, and unknown
  transitions are rejected.
- A mutation without a matching authorization receipt is not restorable.
- Permission denial during preflight changes no blocks.
- A committed transaction persists its cursor before each batch.
- Duplicate restoration does not produce drops or edit twice.

- [ ] **Step 3: Run tests and verify failure**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineJournalTest" --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineMutationTest"
```

Expected: compilation fails because the journal and transaction APIs do not
exist.

- [ ] **Step 4: Implement Forge event capture**

Subscribe to block interaction/place/break, living hurt/death, explosion,
projectile impact, entity join, and the M&A component/spell events already
used by `AuthorshipEvents`. Return immediately when no journal region or
history-enabled player can observe the event, or when
`WorldlineExecutionGuard.isGuarded()` is true.

Journal records carry compact identifiers and visual summaries. Arbitrary
entity NBT and inventory data are never recorded.

- [ ] **Step 5: Implement permission preflight**

Use `level.mayInteract`, world-border checks, `ForgeHooks`, `BlockSnapshot`,
and the same place/break cancellation patterns used by
`LivingLandConservation`. Preflight every changed position before applying
the first mutation. A single denial rejects the complete transaction.

- [ ] **Step 6: Run focused tests**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineJournalTest" --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineMutationTest"
```

Expected: all journal, bound, authorization, preflight, and idempotence tests
pass.

- [ ] **Step 7: Commit journal and transactions**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/worldline src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineJournalTest.java src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineMutationTest.java
git commit -m "feat: journal reversible worldline events"
```

### Task 6: Integrate external Remainders with Authorship

**Files:**
- Create: `WorldlineRemainderService.java`
- Create: `WorldlineRemainderHandler.java`
- Modify: `AuthoredLawHandler.java`
- Modify: `AuthoredLawRegistry.java`
- Modify: `IneffableCastingStateEvents.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineRemainderServiceTest.java`

**Interfaces:**
- Produces:

```java
public interface WorldlineRemainderService {
    <T> WorldlineDebtResult open(
            ServerPlayer owner,
            UUID actionId,
            WorldlineRemainderType<T> type,
            float paradox,
            int safeCasts,
            T payload
    );
    <T> List<WorldlineTypedDebt<T>> debts(
            ServerPlayer owner,
            WorldlineRemainderType<T> type
    );
    Optional<Contradiction> perfectClose(ServerPlayer owner, UUID debtId);
    Optional<Contradiction> forceClose(ServerPlayer owner, UUID debtId);
    void vent(ServerPlayer owner, Contradiction debt);
}

public interface WorldlineRemainderPayloadCodec<T> {
    CompoundTag encode(T payload);
    DataResult<T> decode(int storedVersion, CompoundTag tag);
}

public interface WorldlineRemainderLifecycle<T> {
    boolean canDeclareClosure(
            ServerPlayer owner,
            Contradiction debt,
            T payload
    );
    default void onClosed(
            ServerPlayer owner,
            Contradiction debt,
            T payload
    ) {}
    void vent(ServerPlayer owner, Contradiction debt, T payload);
}

public record WorldlineRemainderType<T>(
        ResourceLocation id,
        int payloadVersion,
        WorldlineRemainderPayloadCodec<T> codec,
        WorldlineRemainderLifecycle<T> lifecycle
) {}

public record WorldlineTypedDebt<T>(
        Contradiction debt,
        T payload
) {}

public final class WorldlineRemainderTypes {
    public static <T> void register(WorldlineRemainderType<T> type);
    public static Optional<WorldlineRemainderType<?>> get(ResourceLocation id);
}
```

Add to `AuthoredLawHandler`:

```java
default boolean canDeclareClosure(Contradiction debt) {
    return true;
}
```

- [ ] **Step 1: Write failing lifecycle tests**

Cover duplicate action IDs, ledger capacity Vent, safe-cast validation,
paradox synchronization, close idempotence, declaration rejection for
non-force-closable debts, and deterministic handler routing.

- [ ] **Step 2: Run the focused test**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineRemainderServiceTest"
```

Expected: compilation fails because the Remainder service does not exist.

- [ ] **Step 3: Implement the hidden Worldline handler**

Use law ID `mnagnosis:worldline_remainder`. It is registered in
`AuthoredLawRegistry` but has no Law Inscription, returns no selectable
interpretations, and supports no ordinary component. Its payload includes:

```text
expression=mnagnosis:remainder
external_action_id
source_session_id
remainder_type
payload_version
phase
visual_summaries[0..8]
typed_payload
```

The service writes the session/action receipt before adding the debt. If
ledger overflow vents an older debt, route that debt through its handler
exactly once before syncing Authorship.

`WorldlineRemainderHandler` is the only `AuthoredLawHandler` for external
Worldline debts. Feature plans register a `WorldlineRemainderType<T>`; they do
not subclass or replace the Authored handler. `debts(owner, type)` decodes
only matching type IDs, sorts in ledger order, and omits malformed entries
after logging them. A malformed entry remains in the ledger and uses the
shared conservative Vent fallback: claim its action receipt, drain up to its
stored Paradox as Ineffable mana, emit a generic Remainder visual, and perform
no world edit.

- [ ] **Step 4: Integrate declaration policy**

`AuthorshipControlService` and `DeclareClosurePacket` must call
`handler.canDeclareClosure(debt)` before selecting a declared Closure.
Bookmark, Recurrence, Revision, Consequence Without Cause, and Hour debts
provide their own forced-closure policy. Unknown Worldline payloads cannot be
declared.

- [ ] **Step 5: Run the lifecycle and existing Authorship tests**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlineRemainderServiceTest" --tests "com.vincenthuto.mnagnosis.common.authorship.*"
```

Expected: the Worldline lifecycle tests and all existing Authorship unit tests
pass.

- [ ] **Step 6: Commit Remainder integration**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/worldline src/main/java/com/vincenthuto/mnagnosis/common/authorship src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlineRemainderServiceTest.java
git commit -m "feat: integrate worldline remainders"
```

### Task 7: Add shared packets, client cache, and bounded renderer

**Files:**
- Create: the four packet files and all client Worldline files from the file
  structure.
- Modify: `NetworkHandler.java`
- Modify: `ClientConfig.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/client/worldline/WorldlineClientStateTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlinePacketTest.java`

**Interfaces:**
- Produces:

```java
public record WorldlineSessionView(
        UUID id,
        ResourceLocation typeId,
        ResourceKey<Level> displayDimension,
        AABB bounds,
        WorldlinePhase phase,
        long serverGameTime,
        long deadlineGameTime,
        long revision,
        List<WorldlineVisualEvent> events
) {}

public record WorldlineVisualEvent(
        UUID eventId,
        ResourceLocation kind,
        Vec3 origin,
        Vec3 direction,
        int argb,
        long eventGameTime,
        int lifetimeTicks,
        Component label
) {
    public WorldlineVisualEvent {
        if (lifetimeTicks < 1 || lifetimeTicks > 600) {
            throw new IllegalArgumentException("Visual lifetime outside 1..600");
        }
    }
}

public record WorldlineActionC2S(
        UUID sessionId,
        long expectedRevision,
        UUID nonce,
        ResourceLocation action,
        CompoundTag payload
) {}
```

- [ ] **Step 1: Write failing packet and cache tests**

Verify encode/decode equality, 32-KiB rejection, action payload 4-KiB
rejection, increasing revisions, removal tombstones, monotonic countdown,
maximum 64 global traces, maximum 32 session traces, and resource-reload
cache clearing.

- [ ] **Step 2: Run focused tests**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlinePacketTest" --tests "com.vincenthuto.mnagnosis.client.worldline.WorldlineClientStateTest"
```

Expected: compilation fails because packets and client state do not exist.

- [ ] **Step 3: Register packet IDs and protocol**

Change `PROTOCOL` from `"5"` to `"6"` and register:

```text
5 WorldlineSessionUpsertS2C
6 WorldlineSessionRemoveS2C
7 WorldlineVisualBatchS2C
8 WorldlineActionC2S
```

The C2S handler obtains the sender, bounds payload size, enqueues work, and
delegates to `WorldlineRuntime.get(sender.server).act`. It never trusts client dimension,
position, owner, phase, deadline, or participant data.

- [ ] **Step 4: Implement watcher synchronization**

Every ten ticks, observe sessions for players who are in the display
dimension and within 64 blocks of their bounds, plus locked participants.
Send upserts only when the observer's last revision is lower. Send removals
when observation ends. Login, respawn, dimension change, and approach receive
a full current summary.

- [ ] **Step 5: Implement the renderer and fallback**

Render at `RenderLevelStageEvent.Stage.AFTER_LEVEL`. `WorldlineRenderScope`
must restore blend, depth, shader, and framebuffer state in `close()` and be
used through try-with-resources.

Client config entries:

```java
WORLDLINE_POST_EFFECTS = builder.define("worldlinePostEffects", true);
WORLDLINE_REDUCED_MOTION = builder.define("worldlineReducedMotion", false);
WORLDLINE_MAX_VISUAL_EVENTS =
        builder.defineInRange("worldlineMaxVisualEvents", 64, 0, 64);
```

Fallback presentation uses depth-tested black/white lines, planes, event
glyphs, and countdown text. Mechanical information must remain readable with
post effects disabled.

- [ ] **Step 6: Run focused tests and a dedicated-server compile**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.WorldlinePacketTest" --tests "com.vincenthuto.mnagnosis.client.worldline.WorldlineClientStateTest"
.\gradlew.bat compileJava
```

Expected: all focused tests pass and no client class is loaded by common
packet handling.

- [ ] **Step 7: Commit networking and visuals**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/network src/main/java/com/vincenthuto/mnagnosis/client/worldline src/main/java/com/vincenthuto/mnagnosis/client/ClientConfig.java src/test/java/com/vincenthuto/mnagnosis/common/worldline/WorldlinePacketTest.java src/test/java/com/vincenthuto/mnagnosis/client/worldline/WorldlineClientStateTest.java
git commit -m "feat: synchronize worldline presentation"
```

### Task 8: Expose the feature API, configuration, and runtime hooks

**Files:**
- Create: `WorldlineApi.java`, `WorldlineStartResult.java`,
  `WorldlineDecisionResult.java`, and `WorldlineFailure.java`.
- Modify: `Config.java`
- Modify: `MnAGnosis.java`
- Modify: `WorldlineRuntimeGameTests.java`

**Interfaces:**
- Produces:

```java
public record WorldlineDecisionResult(
        Status status,
        WorldlineFailure failure,
        long authoritativeRevision
) {
    public enum Status {
        APPLIED,
        NO_CHANGE,
        REJECTED
    }

    public boolean applied() {
        return status == Status.APPLIED;
    }
}
```

```java
public final class WorldlineApi {
    public static <T> WorldlineStartResult start(
            ServerPlayer owner,
            WorldlineSessionType<T> type,
            WorldlineBounds bounds,
            long deadlineGameTime,
            T payload
    );
    public static WorldlineDecisionResult decide(
            ServerPlayer owner,
            UUID sessionId,
            long expectedRevision,
            ResourceLocation action,
            UUID nonce,
            CompoundTag boundedPayload
    );
    public static Optional<WorldlineSessionView> sessionAt(
            ServerLevel level,
            Vec3 position
    );
    public static void recordSpellResolution(
            ServerLevel level,
            WorldlineEvent.SpellResolution event
    );
    public static boolean authorizeBlockMutation(
            ServerLevel level,
            UUID sessionId,
            BlockPos pos,
            ResourceLocation sourceId,
            UUID actionId
    );
    public static void abortOwnedSessions(
            ServerPlayer owner,
            WorldlineRecoveryReason reason
    );
}
```

- [ ] **Step 1: Write failing API-result tests**

Stable failure values are:

```java
NONE,
INVALID_TARGET,
PERMISSION_DENIED,
UNLOADED,
OUT_OF_BOUNDS,
OVERLAP,
OWNER_CAP,
SERVER_CAP,
CONFLICTING_LEASE,
UNSUPPORTED,
STALE_REVISION,
DUPLICATE_ACTION,
QUARANTINED,
PAYLOAD_TOO_LARGE
```

Test that user-facing failures carry translatable keys
`mnagnosis.worldline.failure.<snake_case>` and do not expose exceptions.

- [ ] **Step 2: Add common configuration**

Define bounded common values for:

- `worldlineMaxSessionsPerOwner = 8`
- `worldlineMaxFieldsPerOwner = 2`
- `worldlineMaxPathNodes = 256`
- `worldlineMaxHistoricalEntities = 128`
- `worldlineMaxJournalEvents = 256`
- `worldlineMaxIntervalBlocks = 4096`
- `worldlineMaxIntervalSessions = 8`
- `worldlineAllowOtherPlayerTargets = false`

Hard-codec limits remain authoritative even if config files contain larger
values.

- [ ] **Step 3: Register tick and lifecycle hooks**

On server tick END, call `WorldlineRuntime.get(server).tick(server)` once. On server stopping,
flush dirty SavedData and clear thread-local/client-only caches. On login,
clone, respawn, dimension change, logout, entity join, and chunk load, invoke
the reconciliation methods defined by earlier tasks.

- [ ] **Step 4: Run unit tests**

```powershell
.\gradlew.bat test --tests "com.vincenthuto.mnagnosis.common.worldline.*"
```

Expected: all Worldline foundation unit tests pass.

- [ ] **Step 5: Run foundation GameTests**

```powershell
.\gradlew.bat runGameTestServer
```

Expected: `WorldlineRuntimeGameTests` pass for attachment, action
idempotence, stale revisions, save/reload, lease reconciliation, permission
cancellation, and no-force-load behavior; all pre-existing GameTests remain
unchanged.

- [ ] **Step 6: Commit the public runtime**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/worldline src/main/java/com/vincenthuto/mnagnosis/Config.java src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java src/main/java/com/vincenthuto/mnagnosis/gametest/WorldlineRuntimeGameTests.java
git commit -m "feat: expose worldline runtime"
```

### Task 9: Verify the shared foundation

**Files:**
- Verify only.

- [ ] **Step 1: Run the complete unit suite**

```powershell
.\gradlew.bat test --console=plain
```

Expected: every new Worldline test passes. The only permitted full-suite
failure is the pre-existing
`YaldabaothAssetContractTest.yaldabaothIdleIsTallAndCompactWhileMovementIsLowAndExtended`
`ClassCastException`.

- [ ] **Step 2: Run a clean compile**

```powershell
.\gradlew.bat clean compileJava --console=plain
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run all GameTests**

```powershell
.\gradlew.bat runGameTestServer
```

Expected: all Worldline and pre-existing GameTests pass.

- [ ] **Step 4: Inspect scope and serialization**

```powershell
git diff --check
git status --short
rg -n "Health|Inventory|Curios|Xp|Experience|Advancement" src/main/java/com/vincenthuto/mnagnosis/common/worldline
```

Inspect every match and confirm immutable-ledger data is used only for
rejection/validation, never snapshot restoration. Confirm no Immersive
Portals package or dependency was added.

- [ ] **Step 5: Record the foundation handoff**

Record the nine task commit hashes, focused test counts, full-suite baseline
exception, GameTest result, protocol version, public signatures, payload
versions, and any deliberately unused trim seams in the implementation PR.
