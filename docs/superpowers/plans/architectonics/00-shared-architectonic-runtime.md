# Shared Architectonic Runtime Implementation Plan

> **Prerequisite:** Implement against the authoritative
> [three-discipline foundation contract](../../specs/2026-07-29-three-discipline-foundation-contract.md).
> Do not recreate packet allocation, cast sessions, instruments, conservation,
> Manuscript persistence, or the Contradiction lifecycle in this package.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the dimension-aware geometry, persistence, indexing, permission, crossing, delayed-spell, networking, and compatibility runtime shared by every Architectonic work.

**Architecture:** Overworld `ArchitectonicSavedData` is the sole persistent authority for authored works; a rebuildable `ArchitectonicSpatialIndex` accelerates per-dimension queries. `ArchitectonicCrossingService` implements the native server-side correctness model, while `SpatialBackend` controls presentation and may select an isolated Immersive Portals adapter when that mod is present.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1, Minecraft SavedData and NBT APIs, Mana and Artifice 3.1.11 spell APIs, JOML quaternions, Forge SimpleChannel, JUnit 5, Forge GameTests, optional Immersive Portals for Forge 3.0.7.

## Global Constraints

- This plan supplies shared infrastructure only; no individual Architectonic component, shape, or apparatus is registered here. It does pre-register the otherwise inert Fixed Point material required by later apparatus recipes so every intermediate data pack is loadable.
- All mutation is server-authoritative and all persisted positions include a dimension key.
- `ArchitectonicSavedData` is stored in the overworld data storage even when a work has anchors in other dimensions.
- The spatial index is a derived cache and must be rebuildable from SavedData after load.
- No query, crossing, renderer, backend, or cleanup path adds an explicit or
  persistent chunk ticket. The only permitted unloaded-destination transition
  is a user-triggered vanilla player teleport into an Unbounded Interior; its
  normal short-lived arrival ticket is not reused by background work.
- Native behavior is complete without Immersive Portals; compatibility never owns work identity, expiry, permissions, Closure, or migrations.
- Every crossing uses a swept previous-to-current position test, a finite plane projection test, and a per-entity/per-work cooldown.
- Persistent shapes capture authorship authorization before `SpellCastEvent` clears `AuthorshipCastingService.PREPARED`.
- One cast creates at most one Contradiction; delayed component resolutions never create additional ledger entries.
- Packet decoders reject unknown work types, invalid dimensions, non-finite vectors, oversized lists, and stale revisions.
- Common classes contain no client-only imports and no hard runtime reference to optional-mod classes.
- Minecraft 1.20.1, Forge, Java 17, and Mana and Artifice 3.1.11 remain the baseline.

---

### Task 1: Dimension-aware geometry and transforms — Core

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/geometry/PlaneFrame.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/geometry/RegionFrame.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/geometry/SpatialTransform.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/geometry/ArchitectonicGeometryCodec.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/geometry/PlaneFrameTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/geometry/SpatialTransformTest.java`

**Interfaces:**
- Produces: `PlaneFrame(ResourceKey<Level>, Vec3, Vec3, Vec3, double, double)`
- Produces: `PlaneFrame.normal(): Vec3`
- Produces: `PlaneFrame.signedDistance(Vec3): double`
- Produces: `PlaneFrame.containsProjection(Vec3, double): boolean`
- Produces: `PlaneFrame.project(Vec3): Vec3`
- Produces: `RegionFrame(ResourceKey<Level>, BlockPos, BlockPos)`
- Produces: `RegionFrame.bounds(): AABB`
- Produces: `RegionFrame.blockCount(): long`
- Produces: `RegionFrame.contains(Vec3): boolean`
- Produces: `SpatialTransform.position(Vec3): Vec3`
- Produces: `SpatialTransform.direction(Vec3): Vec3`
- Produces: `SpatialTransform.velocity(Vec3): Vec3`
- Produces: NBT round trips in `ArchitectonicGeometryCodec`.

- [ ] **Step 1: Write geometry tests that establish the coordinate convention**

```java
@Test
void planeUsesOrthonormalWidthHeightAndRightHandedNormal() {
    PlaneFrame frame = new PlaneFrame(
            Level.OVERWORLD, Vec3.ZERO,
            new Vec3(2, 0, 0), new Vec3(1, 3, 0), 4, 6);
    assertEquals(1.0, frame.axisW().length(), 1.0e-9);
    assertEquals(1.0, frame.axisH().length(), 1.0e-9);
    assertEquals(new Vec3(0, 0, 1), frame.normal());
    assertTrue(frame.containsProjection(new Vec3(1.99, 2.99, 40), 0.01));
    assertFalse(frame.containsProjection(new Vec3(2.02, 0, 0), 0.01));
}

@Test
void transformMapsPlaneCentersAndPreservesScaledVelocity() {
    SpatialTransform transform = Fixtures.quarterTurnTransform(2.0);
    assertVecEquals(transform.destination().center(),
            transform.position(transform.source().center()));
    assertVecEquals(new Vec3(0, 0, -2), transform.velocity(new Vec3(1, 0, 0)));
}
```

- [ ] **Step 2: Run the focused tests and verify RED**

Run: `./gradlew.bat test --tests "*PlaneFrameTest" --tests "*SpatialTransformTest"`

Expected: compilation fails because the geometry records do not exist.

- [ ] **Step 3: Implement canonicalization and finite-value validation**

```java
public PlaneFrame {
    Objects.requireNonNull(dimension);
    if (!finite(center) || !finite(axisW) || !finite(axisH)
            || !Double.isFinite(width) || !Double.isFinite(height)
            || width <= 0.0 || height <= 0.0) {
        throw new IllegalArgumentException("Invalid plane frame");
    }
    axisW = axisW.normalize();
    axisH = axisH.subtract(axisW.scale(axisH.dot(axisW))).normalize();
    if (axisW.cross(axisH).lengthSqr() < 1.0e-12) {
        throw new IllegalArgumentException("Collinear plane axes");
    }
}
```

Treat `width` and `height` as full extents. Transform positions relative to
the source center; rotate in local space; multiply offsets and velocity by
`scale`; then add the destination center. Directions rotate but do not scale.
`SpatialTransform` requires a finite normalized quaternion and a finite
strictly positive scale; reflections remain feature-local post-cross motion
because a quaternion plus positive uniform scale cannot represent an improper
transform.

- [ ] **Step 4: Implement explicit versioned NBT codecs**

Use keys `schema`, `dimension`, `center`, `axis_w`, `axis_h`, `width`,
`height`, `min`, `max`, `rotation`, and `scale`. Decode with `ResourceLocation`
and `ResourceKey.create(Registries.DIMENSION, id)`; return `DataResult.error`
instead of substituting the overworld for invalid data.

- [ ] **Step 5: Run focused and full unit tests and verify GREEN**

Run: `./gradlew.bat test`

Expected: all tests pass, including invalid vector, collinear axis, boundary
epsilon, inverse transform, scale, and NBT round-trip cases.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/architectonics/geometry src/test/java/com/vincenthuto/mnagnosis/common/architectonics/geometry
git commit -m "feat: add architectonic geometry primitives"
```

### Task 1A: Pre-register the inert Fixed Point recipe dependency — Core

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/item/FixedPointItem.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java`
- Create: `src/main/resources/assets/mnagnosis/models/item/fixed_point.json`
- Add: `src/main/resources/assets/mnagnosis/textures/item/fixed_point.png`
- Modify: `src/main/resources/assets/mnagnosis/lang/en_us.json`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/item/FixedPointItemTest.java`

**Interfaces:**
- Produces: item ID `mnagnosis:fixed_point`, stack size `16`, fire resistance,
  and bounded helpers for server-authored `work_id`, `cause`, and
  `delivery_receipt` display metadata.
- Preserves: no recipe, loot table, creative-tab entry, or ordinary acquisition
  path; plan `14` owns proof delivery.

- [ ] **Step 1: Write the registry and metadata bounds test**

Assert the item is present before the Transposition Loom and Unbounded Casket
recipes load, is fire resistant, stacks to 16, rejects malformed UUID/cause
metadata, and writes at most one schema-one display compound below `1 KiB`.
Assert no recipe, loot table, or creative-tab placement creates it.

- [ ] **Step 2: Run the focused test and verify RED**

Run: `./gradlew.bat test --tests "*FixedPointItemTest"`

Expected: compilation fails because the material is not registered.

- [ ] **Step 3: Register the inert material and minimal assets**

Register through the existing deferred item registry. The item has no gameplay
callback beyond tooltip formatting and no client class reference. Provide the
model, texture, and English name now so plans `11` and `12` can load their
recipes independently. Do not implement reward issuance here.

- [ ] **Step 4: Run registry/data tests**

Run: `./gradlew.bat test --tests "*FixedPointItemTest"`

Run: `./gradlew.bat runData`

Expected: the item and assets resolve, no recipe produces it, and later
apparatus recipe ingredients can reference the registered ID.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/item/FixedPointItem.java src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java src/main/resources/assets/mnagnosis/models/item/fixed_point.json src/main/resources/assets/mnagnosis/textures/item/fixed_point.png src/main/resources/assets/mnagnosis/lang/en_us.json src/test/java/com/vincenthuto/mnagnosis/common/item/FixedPointItemTest.java
git commit -m "feat: register fixed point material"
```

### Task 2: Work contracts, codecs, and central SavedData — Core

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/RelationChannel.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicWorkType.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicWork.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/QuarantinedArchitectonicEntry.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicWorkCodecRegistry.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicSavedData.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicSavedDataTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicWorkCodecRegistryTest.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsRuntimeGameTests.java`

**Interfaces:**
- Produces: `RelationChannel` with all eleven values from `README.md`.
- Produces: `ArchitectonicWorkType` with all ten values from `README.md`.
- Produces: `ArchitectonicWork`.
- Produces: `ArchitectonicWorkCodec<T extends ArchitectonicWork>`.
- Produces: `ArchitectonicWorkCodecRegistry.register(ArchitectonicWorkType, ArchitectonicWorkCodec<?>)`.
- Produces: `ArchitectonicSavedData.get(MinecraftServer): ArchitectonicSavedData`.
- Produces: `add`, `replace`, `remove`, `find`, and immutable `all`.
- Produces: immutable `quarantined()` entries that are never indexed, ticked,
  or passed to a feature handler.

- [ ] **Step 1: Write codec and SavedData failure tests**

```java
private record TestWork(UUID id, UUID owner, long expiresAt)
        implements ArchitectonicWork {
    public ArchitectonicWorkType type() {
        return ArchitectonicWorkType.AXIAL_FIELD;
    }
    public Set<ResourceKey<Level>> dimensions() {
        return Set.of(Level.OVERWORLD);
    }
    public AABB indexBounds(ResourceKey<Level> dimension) {
        return new AABB(0, 0, 0, 1, 1, 1);
    }
    public CompoundTag save() {
        return TestWorkCodec.save(this);
    }
}

@Test
void replacementIsIdempotentAndRevisionAdvances() {
    ArchitectonicSavedData data = new ArchitectonicSavedData();
    UUID id = UUID.randomUUID();
    data.add(new TestWork(id, OWNER, 40));
    long first = data.revision();
    data.replace(new TestWork(id, OWNER, 80));
    assertEquals(1, data.all().size());
    assertTrue(data.revision() > first);
}
```

Also assert duplicate `add` throws, missing `replace` throws, removal of an
absent ID is harmless, corrupt entries are quarantined as
`QuarantinedArchitectonicEntry`, and one malformed entry does not discard
valid siblings.

- [ ] **Step 2: Run the focused tests and verify RED**

Run: `./gradlew.bat test --tests "*ArchitectonicSavedDataTest" --tests "*ArchitectonicWorkCodecRegistryTest"`

Expected: compilation fails because the runtime contracts do not exist.

- [ ] **Step 3: Implement the work interfaces and codec registry**

```java
public interface ArchitectonicWork {
    UUID id();
    UUID owner();
    ArchitectonicWorkType type();
    Set<ResourceKey<Level>> dimensions();
    long expiresAt(); // Long.MAX_VALUE means persistent until explicit Closure.
    AABB indexBounds(ResourceKey<Level> dimension);
    CompoundTag save();
}
```

The outer SavedData entry owns `schema=1`, `id`, `owner`, `type`, and
`payload`. Individual codecs own payload schemas. A known type decodes to
`ArchitectonicWork`; an unknown/invalid outer type decodes to:

```java
public record QuarantinedArchitectonicEntry(
        UUID id,
        Optional<UUID> owner,
        String rawTypeId,
        CompoundTag rawEntry,
        Component reason
) {}
```

It does not implement `ArchitectonicWork`. Preserve `rawEntry` byte-for-byte
on save so removing a feature or opening a future-version save does not
destroy data. `ArchitectonicWorkCodecRegistry` has codecs only for the ten
known `ArchitectonicWorkType` values.

- [ ] **Step 4: Implement overworld-owned SavedData**

```java
public static ArchitectonicSavedData get(MinecraftServer server) {
    return server.overworld().getDataStorage().computeIfAbsent(
            ArchitectonicSavedData::load,
            ArchitectonicSavedData::new,
            "mnagnosis_architectonics");
}
```

Store active and quarantined records in separate insertion-ordered maps,
increment a persisted `revision` for every mutation, call `setDirty()`, and
expose defensive snapshots. `all()` returns active works only. Keep expired
active records until the lifecycle service performs feature-specific cleanup.
Only operator quarantine commands can retry/restore raw entries.

- [ ] **Step 5: Run focused tests and a save/load GameTest**

Create `src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsRuntimeGameTests.java`
with a test that adds a work, serializes through `save`, reloads it, and verifies
ID, owner, dimensions, expiry, and unknown-entry preservation.

Run: `./gradlew.bat test --tests "*ArchitectonicSavedDataTest" --tests "*ArchitectonicWorkCodecRegistryTest"`

Expected: all focused tests pass.

Run: `./gradlew.bat runGameTestServer`

Expected: all GameTests pass.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime src/test/java/com/vincenthuto/mnagnosis/common/architectonics/runtime src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsRuntimeGameTests.java
git commit -m "feat: persist architectonic works"
```

### Task 3: Spatial index and lifecycle dispatch — Core

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicSpatialIndex.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ExpiryDisposition.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/DurableSignalDisposition.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicWorkHandler.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicWorkHandlers.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicLifecycleService.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicEvents.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicSpatialIndexTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsRuntimeGameTests.java`

**Interfaces:**
- Consumes: `ArchitectonicSavedData` and `ArchitectonicWork`.
- Produces: `ArchitectonicSpatialIndex.rebuild(MinecraftServer, Collection<ArchitectonicWork>)`.
- Produces: `ArchitectonicSpatialIndex.query(ResourceKey<Level>, AABB, Predicate<ArchitectonicWork>): List<ArchitectonicWork>`.
- Produces: `ArchitectonicWorkHandler.tick`, `onExpire`, and `onRemove`.
- Produces: `ExpiryDisposition { REMOVE, REPLACED, DEFER }`.
- Produces: `DurableSignalDisposition { ACKNOWLEDGED, DEFERRED }` for
  feature-local replayable receipt listeners.
- Produces: `ArchitectonicLifecycleService.tick(MinecraftServer)`.

- [ ] **Step 1: Write index tests for dimension and cell boundaries**

Use `32 x 32 x 32` block index cells. Assert that a work spanning negative
coordinates is returned once, a work in the Nether is never returned from an
overworld query, replacement removes stale cells, and rebuilding yields the
same query IDs as incremental insertion.

- [ ] **Step 2: Run the index test and verify RED**

Run: `./gradlew.bat test --tests "*ArchitectonicSpatialIndexTest"`

Expected: compilation fails because `ArchitectonicSpatialIndex` does not exist.

- [ ] **Step 3: Implement the rebuildable index**

```java
public List<ArchitectonicWork> query(
        ResourceKey<Level> dimension,
        AABB bounds,
        Predicate<ArchitectonicWork> filter
) {
    return coveredCells(bounds).stream()
            .flatMap(cell -> idsByDimensionAndCell
                    .getOrDefault(dimension, Map.of())
                    .getOrDefault(cell, Set.of()).stream())
            .distinct()
            .map(worksById::get)
            .filter(Objects::nonNull)
            .filter(work -> work.indexBounds(dimension).intersects(bounds))
            .filter(filter)
            .sorted(Comparator.comparing(ArchitectonicWork::id))
            .toList();
}
```

- [ ] **Step 4: Implement lifecycle event ordering**

On `ServerStartedEvent`, register codecs/handlers, load SavedData, rebuild the
index from `all()` only, and call each handler's recovery hook. On
`ServerTickEvent` END phase,
tick only records whose referenced levels are present; skip unloaded anchor
chunks; expire at `gameTime >= expiresAt`; then obey the handler's explicit
expiry disposition:

```java
ExpiryDisposition disposition = handler.onExpire(server, expiredWork);
switch (disposition) {
    case REMOVE -> removeWorkAndBackend(server, expiredWork.id());
    case REPLACED -> verifyReplacementExistsWithLaterExpiry(expiredWork.id());
    case DEFER -> keepExpiredWorkForEventDrivenOrNextTickRetry(expiredWork.id());
}
```

`REMOVE` means cleanup completed and lifecycle removes the original record.
`REPLACED` means the handler already replaced it atomically (for example, a
Hollow Domain extended by 20 ticks); lifecycle must not remove that replacement.
`DEFER` leaves the same expired record reserved when safe cleanup cannot run,
including an offline owner or unloaded evacuation target. Rate-limit repeated
`DEFER` attempts to once per 20 ticks and let login/chunk-load hooks retry
immediately. `onRemove` is invoked only for a record actually removed, never for
`REPLACED` or `DEFER`. On server stop, clear only derived caches.

Every feature-local durable-receipt listener registry starts with a default
handler that returns `DurableSignalDisposition.DEFERRED`, accepts exactly one
production handler during common setup, and throws on a second registration.
Receipt owners replay only terminal, unacknowledged records; they persist
acknowledgement before compacting evidence to a bounded tombstone.

- [ ] **Step 5: Add lifecycle GameTests**

Verify a transient work expires once and its handler receives one cleanup call;
`REMOVE` deletes it, `REPLACED` preserves the new revision and later expiry,
`DEFER` preserves the same expired record without a busy loop, an
unloaded-endpoint work remains saved and paused, and an unknown entry remains
untouched.

Run: `./gradlew.bat test --tests "*ArchitectonicSpatialIndexTest"`

Expected: all focused tests pass.

Run: `./gradlew.bat runGameTestServer`

Expected: all GameTests pass.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime src/test/java/com/vincenthuto/mnagnosis/common/architectonics/runtime src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsRuntimeGameTests.java
git commit -m "feat: index and tick architectonic works"
```

### Task 4: Central permission and loaded-region policy — Core

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/permission/ArchitectonicAction.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/permission/PermissionResult.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/permission/ArchitectonicPermissionAdapter.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/permission/ArchitectonicPermissionService.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/config/ArchitectonicsLimits.java`
- Create: `src/main/resources/data/mnagnosis/tags/blocks/architectonics_immutable.json`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/permission/ArchitectonicPermissionServiceTest.java`

**Interfaces:**
- Produces: `ArchitectonicAction { SURVEY, APPLY_FORCE, MOVE_BLOCKS, SUPPRESS_COLLISION, TRANSFER_ENTITY, TRANSFER_WORLD_STATE, CREATE_TRANSIENT_WORK, CREATE_PERSISTENT_WORK, CLOSE_PERSISTENT_WORK }`.
- Produces: `PermissionResult(boolean allowed, Component denialMessage)`.
- Produces: `ArchitectonicPermissionService.mayAffect(ServerPlayer, ResourceKey<Level>, AABB, ArchitectonicAction): PermissionResult`.
- Produces: `ArchitectonicPermissionService.mayAffect(MinecraftServer, UUID, ResourceKey<Level>, AABB, ArchitectonicAction): PermissionResult`.
- Produces: `ArchitectonicPermissionService.isLoaded(ServerLevel, AABB): boolean`.
- Produces: immutable `ArchitectonicsLimits.snapshot()` with every suite limit
  used by plans `02` through `13`.

- [ ] **Step 1: Write denial-precedence tests**

Assert denial for non-finite/oversized bounds, missing level, unloaded chunk,
outside build height, vanilla spawn protection, immutable tagged blocks for
`MOVE_BLOCKS`, cancelled synthetic Forge break/place checks, and any denying
adapter. Assert `SURVEY` may read an immutable block but still cannot read an
unloaded chunk.

- [ ] **Step 2: Run the focused test and verify RED**

Run: `./gradlew.bat test --tests "*ArchitectonicPermissionServiceTest"`

Expected: compilation fails because permission classes do not exist.

- [ ] **Step 3: Implement the fixed result contract**

```java
public record PermissionResult(boolean allowed, Component denialMessage) {
    public static PermissionResult allow() {
        return new PermissionResult(true, Component.empty());
    }
    public static PermissionResult deny(String translationKey) {
        return new PermissionResult(false, Component.translatable(translationKey));
    }
}
```

Evaluate cheapest universal checks first, then vanilla/Forge checks, then
registered adapters in deterministic mod-ID order. Cache positive
`CREATE_TRANSIENT_WORK` results for one tick only; never cache mutation or
persistent-work decisions.

The UUID overload is for delayed runtime work. Resolve the live
`ServerPlayer`; if absent, deny `APPLY_FORCE`, `TRANSFER_ENTITY`,
`TRANSFER_WORLD_STATE`, `MOVE_BLOCKS`, and `SUPPRESS_COLLISION` with
`message.mnagnosis.architectonics.owner_offline`. Do not create a Forge fake
player implicitly. Adapters receive the resolved live player, action, source
or destination bounds, and work owner; a future explicit adapter may define a
server-approved offline principal without changing feature code.

- [ ] **Step 4: Add config-backed safety bounds**

Modify `src/main/java/com/vincenthuto/mnagnosis/Config.java` to define the
complete plan-14 limit set now, before feature plans compile:
`architectonicsMaximumWorksPerPlayer=16`,
`architectonicsMaximumWorksPerServer=256`,
`architectonicsMaximumPermissionVolume=32768`,
`reassembledLandMaximumBlocks=384`,
`boundaryMaximumEnumeratedBlocks=512`,
`latticeMaximumResolutions=128`,
`metricMaximumPerPlayer=4`, `adjacencyMaximumPerPlayer=2`,
`refusalMaximumPerPlayer=4`, `hollowDomainMaximumBlocks=8000`,
`coordinateTranspositionMaximumBlocksPerRegion=512`,
`worldSeamMaximumPerPlayer=1`, `worldSeamMaximumPerServer=4`,
`worldSeamMaximumWidth=32`, `worldSeamMaximumHeight=32`,
`architectonicsCrossingCooldownTicks=10`,
`architectonicsRespectSpawnProtection=true`, and
`architectonicsAllowCrossDimension=true`.

Create the immutable `ArchitectonicsLimits` record with the exact fields and
one-read `snapshot()` shown in plan `14` Task 2. Clamp only to the locked hard
ceilings/ranges declared there. Plans `02` through `13` may consume this type
immediately; plan `14` later adds exhaustive bounds tests, creation policy, and
reload behavior rather than creating the facade. Values are server config,
never trusted from client packets.

- [ ] **Step 5: Run tests and compile dedicated common code**

Run: `./gradlew.bat test --tests "*ArchitectonicPermissionServiceTest"`

Expected: all permission tests pass.

Run: `./gradlew.bat compileJava`

Expected: compilation succeeds without client-only imports.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/architectonics/permission src/main/java/com/vincenthuto/mnagnosis/common/architectonics/config/ArchitectonicsLimits.java src/main/resources/data/mnagnosis/tags/blocks/architectonics_immutable.json src/main/java/com/vincenthuto/mnagnosis/Config.java src/test/java/com/vincenthuto/mnagnosis/common/architectonics/permission
git commit -m "feat: centralize architectonic permissions"
```

### Task 5: Swept crossing service and native backend — Core

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/crossing/CrossingResult.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/crossing/CrossingCooldowns.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/crossing/ArchitectonicCrossingService.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/crossing/ArchitectonicPhysicalPlaneRouter.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/crossing/ArchitectonicPhysicalCrossingCandidate.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/crossing/ArchitectonicEntityMotionCapture.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/crossing/ArchitectonicPostMotionObserver.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/crossing/ArchitectonicPostMotionObservers.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/mixin/core/ServerLevelEntityMotionMixin.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/mixin/core/EntityDiscontinuityMixin.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/spell/ArchitectonicSpellPlaneRouter.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/spell/ArchitectonicSpellRouteCandidate.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/spell/ArchitectonicSpellRoutingService.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/mixin/core/SpellCasterArchitectonicRoutingMixin.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/backend/SpatialBackend.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/backend/SpatialBackendKind.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/backend/NativeSpatialBackend.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/backend/SpatialBackendFactory.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/backend/SpatialBackendManager.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/crossing/ArchitectonicCrossingServiceTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/spell/ArchitectonicSpellRoutingServiceTest.java`
- Modify: `src/main/resources/mnagnosis.mixins.json`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsRuntimeGameTests.java`

**Interfaces:**
- Consumes: `PlaneFrame`, `SpatialTransform`, `RelationChannel`, permissions, and work index.
- Produces: `ArchitectonicCrossingService.tick(MinecraftServer): void`.
- Produces: `ArchitectonicCrossingService.tryCross(Entity, PlaneFrame, SpatialTransform, EnumSet<RelationChannel>, UUID workId, UUID ownerId): CrossingResult`.
- Produces: `ArchitectonicPhysicalPlaneRouter.candidates(Entity, MotionSegment): List<ArchitectonicPhysicalCrossingCandidate>`.
- Produces: `ArchitectonicPostMotionObservers.register(ArchitectonicPostMotionObserver): void`.
- Produces: one central post-movement arbitration pass ordered by hit fraction,
  physical relation priority, then work UUID.
- Produces: `SpatialBackend.kind(): SpatialBackendKind`.
- Produces: `SpatialBackend.install(MinecraftServer, ArchitectonicWork): void`.
- Produces: `SpatialBackend.update(MinecraftServer, ArchitectonicWork): void`.
- Produces: `SpatialBackend.remove(MinecraftServer, UUID): void`.
- Produces: `SpatialBackend.supportsRecursiveView(): boolean`.
- Produces: `SpatialBackendManager.get`, `onWorkAdded`, `onWorkUpdated`,
  `onWorkRemoved`, and `shutdown`.
- Produces: `ArchitectonicSpellPlaneRouter.candidates(MinecraftServer, ServerLevel, SpellSource, ISpellDefinition): List<ArchitectonicSpellRouteCandidate>`.
- Produces: one shared `SpellCaster.Affect` interception and deterministic
  nearest-plane arbitration for every spell-transmitting work.

- [ ] **Step 1: Write crossing solver tests**

Test front-to-back, back-to-front rejection for a one-way face, high-speed
swept crossing, collision-clipped movement, piston displacement, a same-tick
teleport discontinuity reset, miss outside finite plane bounds, a passenger
stack, projectile owner preservation, yaw/pitch rotation, scaled velocity,
blocked destination, unloaded destination, and same-work re-entry during
cooldown. Register overlapping Metric, Adjacency, Refusal, and World Seam
physical routers in shuffled order; nearest fraction must win, with exact-hit
ties ordered `WORLD_SEAM_CUT`, `WORLD_SEAM_STITCH`, `REFUSAL`, `ADJACENCY`,
`METRIC`, then work UUID. Spell-routing
tests register mock Adjacency, Refusal, and World Seam routers, then assert
nearest intersection wins regardless of registration/mixin order. Exact-hit
ties sort `WORLD_SEAM_CUT`, `WORLD_SEAM_STITCH`, `REFUSAL`, `ADJACENCY`, then
work UUID. Assert one shared recursion guard permits at most eight distinct
work hops and never revisits a work.

- [ ] **Step 2: Run the crossing test and verify RED**

Run: `./gradlew.bat test --tests "*ArchitectonicCrossingServiceTest"`

Expected: compilation fails because the crossing service does not exist.

- [ ] **Step 3: Implement finite swept-plane crossing**

```java
MotionSegment segment = ArchitectonicEntityMotionCapture
        .completedSegment(entity).orElseThrow();
double previous = plane.signedDistance(segment.start());
double current = plane.signedDistance(entity.position());
if (!(previous > CROSS_EPSILON && current <= -CROSS_EPSILON)) {
    return CrossingResult.NO_CROSSING;
}
double t = Mth.clamp(previous / (previous - current), 0.0, 1.0);
Vec3 intersection = segment.start().lerp(segment.end(), t);
if (!plane.containsProjection(intersection, entity.getBbWidth() * 0.5)) {
    return CrossingResult.OUTSIDE_APERTURE;
}
```

Do not reconstruct motion as `position - deltaMovement`: collision clipping,
pistons, knockback, and other movement can make that vector differ from the
actual start/end segment. `ServerLevelEntityMotionMixin` captures the root
entity position/dimension at `ServerLevel#tickNonPassenger` HEAD and submits
the completed segment at RETURN. `EntityDiscontinuityMixin` marks explicit
teleport/absolute-move/dimension-change paths so the RETURN hook discards that
segment; the shared crossing transfer also sets this marker. Confirm exact
1.20.1 mapped descriptors with `compileJava` and require each injector once.
Passengers are evaluated only with their root vehicle.

Map entity classes to relation channels. Move a root vehicle and all
passengers as one transaction. Use vanilla dimension transfer for
cross-dimensional movement, rotate velocity/view after recreation, preserve
projectile owner UUID, and place the entity `0.05` blocks beyond the
destination normal.

Before moving the root, revalidate its source AABB and the transformed
destination AABB through the UUID permission overload with
`ArchitectonicAction.TRANSFER_ENTITY`. A missing/offline owner, revoked claim,
or denied destination returns `CrossingResult.DENIED_PERMISSION` and performs
no partial movement. Callers choose whether denial means an inert relation or
a CUT-style rejection; the shared crossing service never guesses.

Intercept the static four-argument `SpellCaster.Affect` at HEAD exactly once in
`SpellCasterArchitectonicRoutingMixin`. The mixin delegates to
`ArchitectonicSpellRoutingService.route`; it contains no feature logic. Each
registered `ArchitectonicSpellPlaneRouter` returns zero or more candidates
with hit fraction, work ID/owner, kind priority, destination level/source, and
permission result plus a server-only
`onResolution(SpellCastingResult)` completion callback. The service selects
nearest hit, applies the fixed tie
order, enters one `ThreadLocal<LinkedHashSet<UUID>>`, invokes `Affect` from the
selected continuation, invokes its callback exactly once with the returned
result, and cancels the outer call with that result. Callback exceptions are
logged and cannot replace the spell result. It clears the guard in `finally`.
Feature plans never add another `Affect` HEAD mixin. Tests cover success,
component failure, thrown continuation, and callback failure cardinality.

Physical crossing follows the same one-owner pattern but a separate registry.
The post-movement hook asks every registered router for all candidates
intersecting the actual swept AABB, globally sorts by hit fraction and the fixed
physical priority, and executes only the first valid candidate for that root in
the tick. A permission/unload rejection may fall through to the next candidate
only when it does not represent CUT; CUT is a terminal selected relation.
Metric, Adjacency, Refusal, and World Seam handlers maintain expiry/effects but
never invoke `tryCross` directly. This removes feature-handler registration
order from physical behavior.

After arbitration (including the no-candidate case), invoke every registered
`ArchitectonicPostMotionObserver` exactly once with the root UUID, immutable
actual `MotionSegment`, selected candidate ID if any, and result. Observers may
update bounded progression trackers but cannot add crossing candidates, move
entities, or change the already-selected result. Skip observers for
discontinuous/teleport segments. Isolate observer exceptions, preserve stable
registration order only for diagnostics, and test that zero/one/many routers
still yield exactly one observer callback. Metric plan `07` uses this seam to
recognize the later outward walk after its initial threshold transfer.

- [ ] **Step 4: Implement safety and cooldown policy**

Key cooldowns by `(entity UUID, work UUID)` for `10` ticks and clear them on
entity removal. Reject sleeping, changing-dimension, removed, boss-part,
non-owning passenger, and destination-collision cases. Search safe exit offsets
`0.05, 0.30, 0.55, 0.80, 1.05`; if none is collision-free, do not cross.

- [ ] **Step 5: Implement native backend lifecycle**

`NativeSpatialBackend` stores no authority. `install` and `update` publish a
revisioned snapshot to tracking clients; `remove` publishes a tombstone.
`kind()` returns `NATIVE` and `supportsRecursiveView()` returns `false`.
`SpatialBackendFactory` selects one server-scoped backend at startup.
Feature code calls only `SpatialBackendManager.onWorkAdded`,
`onWorkUpdated`, and `onWorkRemoved`; it never casts or selects a backend. The
manager catches a per-work optional-backend failure, removes that optional
projection, and installs the same work in `NativeSpatialBackend`.

- [ ] **Step 6: Run unit and GameTests**

Run: `./gradlew.bat test --tests "*ArchitectonicCrossingServiceTest" --tests "*ArchitectonicSpellRoutingServiceTest"`

Expected: all crossing solver tests pass.

Run: `./gradlew.bat runGameTestServer`

Expected: native same-dimension and cross-dimension crossing GameTests pass,
and an unloaded destination produces no movement or chunk ticket.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/architectonics/crossing src/main/java/com/vincenthuto/mnagnosis/common/architectonics/backend src/main/java/com/vincenthuto/mnagnosis/common/architectonics/spell src/main/java/com/vincenthuto/mnagnosis/mixin/core src/main/resources/mnagnosis.mixins.json src/test/java/com/vincenthuto/mnagnosis/common/architectonics/crossing src/test/java/com/vincenthuto/mnagnosis/common/architectonics/spell src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsRuntimeGameTests.java
git commit -m "feat: add native architectonic crossings"
```

### Task 6: Persistent spell payloads and captured authorship permits — Core

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/spell/AuthorshipCastPermit.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/spell/PersistentSpellPayload.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/spell/PersistentResolutionResult.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/spell/PersistentSpellExecutor.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/authorship/AuthorshipCastingService.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/authorship/AuthorshipEvents.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/architectonics/spell/PersistentSpellExecutorTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsRuntimeGameTests.java`

**Interfaces:**
- Produces: `AuthorshipCastPermit` with the exact fields in `README.md`.
- Produces: `AuthorshipCastingService.capturePersistentPermit(ServerPlayer, ISpellDefinition, long, CompoundTag): AuthorshipCastPermit`.
- Produces: `PersistentSpellPayload.capture(ServerPlayer, SpellSource, ISpellDefinition, long, int, CompoundTag): PersistentSpellPayload`.
- Produces: `PersistentSpellExecutor.apply(ServerLevel, PersistentSpellPayload, SpellTarget): PersistentResolutionResult`.

- [ ] **Step 1: Write permit lifecycle tests**

Assert an ordinary cast captures empty law/interpretation, an authored cast
captures the prepared handler values before finalize, the short-lived capture
is consumed by finalize as one applied persistent event, permit NBT
round-trips, expiry denies resolution, resolution count cannot exceed its cap,
and repeated delayed applications do not append ledger entries.

- [ ] **Step 2: Run the focused test and verify RED**

Run: `./gradlew.bat test --tests "*PersistentSpellExecutorTest"`

Expected: compilation fails because the permit and executor do not exist.

- [ ] **Step 3: Implement the immutable permit**

```java
public record AuthorshipCastPermit(
        UUID casterId,
        String spellFingerprint,
        Optional<ResourceLocation> lawId,
        Optional<ResourceLocation> interpretationId,
        float baseManaCost,
        long issuedGameTime,
        long expiresAtGameTime,
        CompoundTag payload
) {
    public boolean authored() {
        return lawId.isPresent() && interpretationId.isPresent();
    }
}
```

Reject mismatched optional law/interpretation, negative or non-finite mana,
expiry before issue, fingerprints longer than `256`, and payloads larger than
`16 KiB` when serialized.

- [ ] **Step 4: Capture authorization before cast finalization**

`Shape.Target` has no `SpellContext`, so do not introduce a thread-local or
mixin accessor. `capturePersistentPermit` reads `PREPARED` while the initial
shape target is being created. It copies law, interpretation, base cost, and
the supplied bounded `workPayload`, then stores a short-lived
`PersistentCapture` keyed by caster UUID and `SpellFingerprint`; it never
removes `PREPARED`. `finalizeCast`, which does receive `SpellContext`, removes
the matching capture, sets `authored_applied=true` and
`persistent_scheduled=true`, copies `workPayload` to the context metadata, and
performs one ledger transition. Multiple captures for the same cast coalesce;
reject a second capture whose fingerprint differs from the prepared cast.

- [ ] **Step 5: Implement bounded downstream component serialization**

Serialize the spell's component list but never invoke its shape list during
delayed execution. This prevents recursive field creation without needing an
originating-shape parameter. Store M&A component registry IDs, modifier IDs,
attribute values, permit, `maxResolutions`, and current count. Cap payload NBT
at `64 KiB`.

- [ ] **Step 6: Implement delayed resolution**

Resolve the original player by UUID. If offline, dead, ineligible by immutable
permit, or the target level/chunk is unavailable, return a non-consuming
result. Apply captured authored interpretation without consulting `PREPARED`;
increment the count only when at least one component returns success; never
charge mana or add a Contradiction during delayed resolution.

- [ ] **Step 7: Run unit and cast lifecycle GameTests**

Run: `./gradlew.bat test --tests "*PersistentSpellExecutorTest"`

Expected: all permit and resolution tests pass.

Run: `./gradlew.bat runGameTestServer`

Expected: a persistent shape resolves twice after `SpellCastEvent`, produces
one ledger entry total, survives save/load, and expires at its captured time.

- [ ] **Step 8: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/architectonics/spell src/main/java/com/vincenthuto/mnagnosis/common/authorship/AuthorshipCastingService.java src/main/java/com/vincenthuto/mnagnosis/common/authorship/AuthorshipEvents.java src/test/java/com/vincenthuto/mnagnosis/common/architectonics/spell src/main/java/com/vincenthuto/mnagnosis/gametest/ArchitectonicsRuntimeGameTests.java
git commit -m "feat: capture persistent spell authorization"
```

### Task 7: Work snapshots, diagnostics, and native rendering — Core

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/network/ArchitectonicSnapshotPacket.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/network/ArchitectonicRemovePacket.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/network/ArchitectonicsPacketIds.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/network/MnAGnosisPacketRegistrar.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicSnapshot.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/architectonics/ClientArchitectonicWorks.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/architectonics/ArchitectonicWorldRenderer.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/network/ArchitectonicSnapshotPacketTest.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/architectonics/ArchitectonicWorldRendererContractTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/network/NetworkHandler.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/Config.java`

**Interfaces:**
- Produces: versioned, bounded `ArchitectonicSnapshot`.
- Produces: `ClientArchitectonicWorks.upsert`, `remove`, `clear`, and `visible`.
- Produces: shared line/plane/volume render primitives selected by work type.
- Produces: fixed packet-ID reservations and duplicate/range validation.

- [ ] **Step 1: Write packet bound and stale-revision tests**

Assert encode/decode for all work types, rejection above `64` planes or
`1024` line segments, rejection of non-finite coordinates, last-write-wins by
work revision, tombstone removal, and cache clear on disconnect.

- [ ] **Step 2: Run packet tests and verify RED**

Run: `./gradlew.bat test --tests "*ArchitectonicSnapshotPacketTest" --tests "*ArchitectonicWorldRendererContractTest"`

Expected: compilation fails because snapshot classes do not exist.

- [ ] **Step 3: Add packets without renumbering existing messages**

Keep `NetworkHandler.PROTOCOL` at `5` until plan `14` performs the one suite
bump. Register runtime snapshot/remove at fixed IDs `5` and `6` through
`MnAGnosisPacketRegistrar`; reserve `7..15`. The registrar throws during common
setup on a duplicate or a packet outside its owner's range. Server handlers
only encode immutable snapshots; client handlers enqueue cache mutation. Send
full snapshots on login, dimension change, and start tracking; send deltas
after work revision.

- [ ] **Step 4: Implement the native visual language**

Render black/white/gold measured lines, finite translucent planes, direction
ticks, and bounded volume wireframes. Cull beyond
`architectonicsClientRenderDistance=128`, cap visible works at `128`, sort
translucent planes back-to-front, restore pose/render state in `finally`, and
show no through-portal recursion in the native backend.

- [ ] **Step 5: Add operator diagnostics**

Register `/mnagnosis architectonics list [dimension]`,
`inspect <uuid>`, `remove <uuid>`, and `reindex`. Require permission level `2`
for read commands and `3` for mutation. `inspect` prints owner, type,
dimensions, expiry, revision, backend, and loaded/paused state without dumping
arbitrary NBT.

- [ ] **Step 6: Run tests and manual client checks**

Run: `./gradlew.bat test`

Expected: all unit and render-contract tests pass.

Run: `./gradlew.bat runClient`

Verify: native planes and lines cull correctly, disconnect clears cache,
F3+A does not duplicate works, and no GL state leaks affect vanilla entities.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/network src/main/java/com/vincenthuto/mnagnosis/common/architectonics/runtime/ArchitectonicSnapshot.java src/main/java/com/vincenthuto/mnagnosis/client/architectonics src/test/java/com/vincenthuto/mnagnosis/common/network src/test/java/com/vincenthuto/mnagnosis/client/architectonics src/main/java/com/vincenthuto/mnagnosis/Config.java
git commit -m "feat: sync and render architectonic works"
```

### Task 8: Optional Immersive Portals backend — Enhancement

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/compat/immersiveportals/ImmersivePortalsSpatialBackend.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/compat/immersiveportals/ImmersivePortalsBridge.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/compat/ImmersivePortalsIsolationTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/architectonics/backend/SpatialBackendFactory.java`
- Modify: `build.gradle`
- Modify: `src/main/resources/META-INF/mods.toml`

**Interfaces:**
- Consumes: `SpatialBackend` and authoritative work snapshots.
- Produces: optional recursive-view portal/mirror projection.
- Preserves: native service ownership of crossing, permissions, work lifecycle, and recovery.

- [ ] **Step 1: Write optional-classloading tests**

Verify that loading every class outside
`com.vincenthuto.mnagnosis.compat.immersiveportals` succeeds when Immersive
Portals classes are absent. Verify factory fallback returns
`NativeSpatialBackend` after reflective load failure.

- [ ] **Step 2: Run the isolation test and verify RED**

Run: `./gradlew.bat test --tests "*ImmersivePortalsIsolationTest"`

Expected: test fails because the compatibility factory selection is absent.

- [ ] **Step 3: Add compile-only dependency and optional metadata**

```groovy
compileOnly fg.deobf(
    "curse.maven:immersive-portals-for-forge-355440:6368524"
)
```

Add an optional `immersive_portals` dependency with `mandatory=false`,
`versionRange="[3.0.7,)"`, `ordering="AFTER"`, and `side="BOTH"`. Do not add
it to the distributed jar or require it from native tests.

- [ ] **Step 4: Isolate backend construction**

`SpatialBackendFactory` checks `ModList.get().isLoaded("immersive_portals")`
then reflectively calls a no-argument bridge factory by class name. It catches
`ReflectiveOperationException`, `LinkageError`, and API validation failure,
logs one warning, and returns the native backend. No common-class constant
pool contains an Immersive Portals type.

- [ ] **Step 5: Map works to explicit one-way faces**

Create one portal face for a one-way relation, two for a bidirectional
single-sided relation, and four for a bidirectional biface relation. Copy
plane center, width axis, height axis, width, height, destination dimension,
destination center, rotation, and scale. Mirror views are non-teleportable.
Tag generated entities with the MnAGnosis work UUID and remove only entities
carrying that exact tag.

- [ ] **Step 6: Keep MnAGnosis crossing authoritative**

Cancel or disable IP teleportation for works whose native channel filter,
Closure state, permission state, or cooldown denies crossing. Reconcile
duplicate threshold callbacks by accepting only the first crossing for a
`(entity, work, tick)` tuple. On backend failure, remove compatibility faces
and continue with native wireframes and crossings.

- [ ] **Step 7: Run the compatibility matrix**

Run without Immersive Portals:

`./gradlew.bat clean test runGameTestServer`

Expected: build and all tests pass; the native backend is selected.

Run a development client with Immersive Portals 3.0.7 and its required runtime
dependencies installed:

`./gradlew.bat runClient`

Verify: recursive plane view, front clipping, one-way orientation,
cross-dimensional crossing, removal, resource reload, and fallback after a
forced bridge error.

- [ ] **Step 8: Inspect the production jar**

Run: `./gradlew.bat clean build`

Expected: `BUILD SUCCESSFUL`.

Run: `jar tf build/libs/mnagnosis-*.jar | Select-String "immersiveportals|qouteall"`

Expected: only MnAGnosis compatibility class names appear; no third-party
classes are bundled.

- [ ] **Step 9: Commit**

```powershell
git add build.gradle src/main/resources/META-INF/mods.toml src/main/java/com/vincenthuto/mnagnosis/compat/immersiveportals src/main/java/com/vincenthuto/mnagnosis/common/architectonics/backend/SpatialBackendFactory.java src/test/java/com/vincenthuto/mnagnosis/compat
git commit -m "feat: add optional immersive portals backend"
```
