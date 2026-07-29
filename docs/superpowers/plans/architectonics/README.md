# Architectonics Implementation Plan Suite

> **Foundation override (2026-07-29):** The implemented
> [three-discipline foundation contract](../../specs/2026-07-29-three-discipline-foundation-contract.md)
> is authoritative. Architectonics uses packet IDs `64-255`, protocol `"5"`,
> the shared Relation Manuscript state, shared Contradiction handlers,
> opposite-hand instrument snapshots, persistent cast permits, and
> `ConservedBlockService`. The packet table and discipline-local foundation
> types below are historical inputs and must not be implemented in parallel.
> Autogenesis ships first.

This directory decomposes every Architectonic work in
`docs/superpowers/specs/2026-07-28-three-disciplines-idea-vault-design.md`
into an independently executable implementation plan. The suite deliberately
specifies a larger complete product than the first release needs. Tasks marked
**Core** are the minimum coherent gameplay contract; tasks marked **Enhancement**
can be removed without changing save formats or the native correctness model.

## Product Direction

Architectonics is a spell grammar, not a list of unrelated machines:

- A **component** answers what relation is rewritten.
- A **shape** answers where and when subsequent components resolve.
- An **instrument** records geometry or makes a dangerous multi-stage operation
  legible.
- An **apparatus** is reserved for a persistent, owned place whose lifecycle is
  too substantial for one cast.

Every spatial effect is server-authoritative. MnAGnosis owns its persistent
records, permission checks, closure rules, and recovery behavior. Immersive
Portals is an optional presentation and crossing enhancement; the mod must
remain correct and playable when it is absent.

## Work Classification

| Work | Grammar role | Registry or content ID | Primary plan |
|---|---|---|---|
| Unbounded Lattice | Surveying instrument | `mnagnosis:unbounded_lattice` | [01](01-unbounded-lattice-and-surveying.md) |
| Reassembled Land | Component | `mnagnosis:components/reassembled_land` | [02](02-reassembled-land.md) |
| Axial Ordination | Component | `mnagnosis:components/axial_ordination` | [03](03-axial-ordination.md) |
| Boundary Condition | Shape | `mnagnosis:boundary_condition` | [04](04-boundary-condition-shape.md) |
| Load-Bearing Principle | Component | `mnagnosis:components/load_bearing_principle` | [05](05-load-bearing-principle.md) |
| Lattice Emanation | Shape | `mnagnosis:lattice_emanation` | [06](06-lattice-emanation-shape.md) |
| Metric Compression | Component | `mnagnosis:components/metric_compression` | [07](07-metric-compression.md) |
| Axiom of Adjacency | Component | `mnagnosis:components/axiom_of_adjacency` | [08](08-axiom-of-adjacency.md) |
| Euclidean Refusal | Component | `mnagnosis:components/euclidean_refusal` | [09](09-euclidean-refusal.md) |
| Hollow Domain | Component | `mnagnosis:components/hollow_domain` | [10](10-hollow-domain.md) |
| Coordinate Transposition | Component plus commit instrument | `mnagnosis:components/coordinate_transposition`, `mnagnosis:transposition_loom` | [11](11-coordinate-transposition.md) |
| Unbounded Interior | Artifice apparatus | `mnagnosis:unbounded_casket` | [12](12-unbounded-interior.md) |
| World Seam | Component plus closure instrument | `mnagnosis:components/world_seam`, `mnagnosis:seam_ripper` | [13](13-world-seam.md) |

## Plan Index

1. [Shared Architectonic Runtime](00-shared-architectonic-runtime.md) defines
   geometry, persistence, spatial indexing, permissions, crossings, delayed
   spell execution, native rendering contracts, and the optional Immersive
   Portals boundary.
2. [Unbounded Lattice and Surveying](01-unbounded-lattice-and-surveying.md)
   defines the common player input language for planes, regions, pairs, and
   occupancy templates.
3. [Reassembled Land](02-reassembled-land.md) plans conservation-safe material
   assembly and projection.
4. [Axial Ordination](03-axial-ordination.md) plans axis and plane alignment for
   entities, projectiles, falling blocks, and spell manifestations.
5. [Boundary Condition](04-boundary-condition-shape.md) plans inside, outside,
   entering, and exiting shape semantics.
6. [Load-Bearing Principle](05-load-bearing-principle.md) plans structural
   capacity, redirected forces, fall mitigation, and explosion filtering.
7. [Lattice Emanation](06-lattice-emanation-shape.md) plans bounded vertex and
   edge spell repetition.
8. [Metric Compression](07-metric-compression.md) plans a visibly long corridor
   with shorter effective traversal.
9. [Axiom of Adjacency](08-axiom-of-adjacency.md) plans selectively transmitted
   relations between two surfaces.
10. [Euclidean Refusal](09-euclidean-refusal.md) plans reflection, return,
    quarter-turn, and lateral traversal redirection.
11. [Hollow Domain](10-hollow-domain.md) plans reversible omission from
    collision and interaction.
12. [Coordinate Transposition](11-coordinate-transposition.md) plans audited,
    journaled, atomic region exchange.
13. [Unbounded Interior](12-unbounded-interior.md) plans owned cells in a fixed
    interior dimension and the casket lifecycle.
14. [World Seam](13-world-seam.md) plans durable cut/stitch works, crossing
    reconciliation, Closure, and Dislocation.
15. [Integration, Progression, and Release](14-integration-progression-and-release.md)
    plans registration, configs, recipes, advancements, compatibility matrices,
    performance gates, migration, and release sequencing.

## Authoritative Shared Contracts

All plans consume the names below verbatim. A feature implementer must amend
plan `00` first if a shared name must change, then update every consumer in the
same commit.

```java
record PlaneFrame(
    ResourceKey<Level> dimension,
    Vec3 center,
    Vec3 axisW,
    Vec3 axisH,
    double width,
    double height
)

record RegionFrame(
    ResourceKey<Level> dimension,
    BlockPos min,
    BlockPos max
)

record SpatialTransform(
    PlaneFrame source,
    PlaneFrame destination,
    Quaternionf rotation,
    double scale
)
```

```java
enum RelationChannel {
    LIVING, VEHICLE, ITEM, PROJECTILE, FALLING_BLOCK, SPELL,
    FLUID, REDSTONE, LIGHT, SOUND, EXPLOSION
}

enum ArchitectonicWorkType {
    BOUNDARY_FIELD, LATTICE_EMANATION, AXIAL_FIELD,
    LOAD_BEARING_FIELD, METRIC_CORRIDOR, ADJACENCY,
    REFUSAL_PLANE, HOLLOW_DOMAIN, INTERIOR_THRESHOLD, WORLD_SEAM
}

enum DurableSignalDisposition {
    ACKNOWLEDGED, DEFERRED
}
```

The authoritative save lives in overworld `ArchitectonicSavedData`.
Per-dimension spatial indexes are derived caches and are always rebuildable.
No work may force-load a chunk. An unloaded endpoint pauses crossing,
transmission, or ticking without deleting the work.

Runtime mutation never silently impersonates an offline owner. Creation calls
`ArchitectonicPermissionService.mayAffect(ServerPlayer, ...)`; delayed work
calls its `(MinecraftServer, UUID ownerId, ...)` overload. The runtime overload
resolves the live owner and otherwise returns
`message.mnagnosis.architectonics.owner_offline` for `APPLY_FORCE`,
`TRANSFER_ENTITY`, `TRANSFER_WORLD_STATE`, `MOVE_BLOCKS`, and
`SUPPRESS_COLLISION`. Consequently Axial, Boundary, Load-Bearing, Metric,
Adjacency STITCH, Refusal, and state transmission pause while their owner is
offline. World Seam CUT continues to reject crossings, but an offline-owned
STITCH degrades to CUT. No fake player is created by default.

Packet IDs are reserved once for the whole suite; an omitted feature leaves a
gap and never causes following IDs to shift:

| Range | Owner |
|---|---|
| `0–4` | Existing MnAGnosis packets |
| `5–15` | Shared Architectonic runtime |
| `16–31` | Unbounded Lattice |
| `32–39` | Reassembled Land |
| `40–47` | Axial Ordination |
| `48–55` | Boundary Condition |
| `56–63` | Load-Bearing Principle |
| `64–71` | Lattice Emanation |
| `72–79` | Metric Compression |
| `80–95` | Axiom of Adjacency |
| `96–103` | Euclidean Refusal |
| `104–111` | Hollow Domain |
| `112–127` | Coordinate Transposition |
| `128–143` | Unbounded Interior |
| `144–175` | World Seam |
| `176–191` | Progression and Closure |

`ArchitectonicsPacketIds` exposes these constants and
`MnAGnosisPacketRegistrar` rejects duplicate/out-of-range registration during
common setup. Individual plans do not increment the channel protocol. Plan
`14` changes protocol `5` to `6` once after the complete packet manifest is
assembled.

The initial exact manifest is:

| ID | `ArchitectonicsPacketIds` constant | Packet |
|---:|---|---|
| `5` | `ARCHITECTONIC_SNAPSHOT` | `ArchitectonicSnapshotPacket` |
| `6` | `ARCHITECTONIC_REMOVE` | `ArchitectonicRemovePacket` |
| `16` | `LATTICE_SET_MODE` | `SetLatticeModePacket` |
| `17` | `LATTICE_COMPLETE` | `CompleteLatticeSurveyPacket` |
| `18` | `LATTICE_CLEAR` | `ClearLatticeSurveyPacket` |
| `19` | `LATTICE_SET_SELECTION` | `SetLatticeSelectionPacket` |
| `20` | `LATTICE_PREVIEW` | `LatticePreviewPacket` |
| `32` | `REASSEMBLED_TRANSIT` | `ReassembledTransitPacket` |
| `40` | `AXIAL_FIELD_SYNC` | `AxialFieldSyncPacket` |
| `48` | `BOUNDARY_FIELD_SYNC` | `BoundaryFieldSyncPacket` |
| `56` | `LOAD_BEARING_FIELD_SYNC` | `LoadBearingFieldSyncPacket` |
| `80` | `ADJACENCY_ENDPOINT_SAMPLE` | `AdjacencyEndpointSamplePacket` |
| `104` | `HOLLOW_DOMAIN_PREVIEW` | `HollowDomainPreviewPacket` |
| `105` | `HOLLOW_DOMAIN_STATE` | `HollowDomainStatePacket` |
| `112` | `TRANSPOSITION_SUBMIT_SURVEY` | `SubmitTranspositionSurveyPacket` |
| `113` | `TRANSPOSITION_LOOM_STATE` | `TranspositionLoomStatePacket` |
| `128` | `UNBOUNDED_CASKET_ACTION` | `UnboundedCasketActionPacket` |
| `129` | `UNBOUNDED_CASKET_STATE` | `UnboundedCasketStatePacket` |
| `144` | `WORLD_SEAM_STATE` | `WorldSeamStatePacket` |
| `145` | `WORLD_SEAM_SOUND` | `WorldSeamSoundPacket` |
| `176` | `RELATION_MANUSCRIPT` | `RelationManuscriptPacket` |

Every unlisted number in a reserved range remains unavailable for
registration-order allocation. Tests assert every packet class maps to exactly
one manifest constant and that no constant is registered twice.

The Lattice NBT root is `mnagnosis:lattice` with schema version `1`. It stores
at most eight raw survey points. Templates store occupancy only in a maximum
`9 x 9 x 9` volume; they never copy block states, inventories, block-entity
NBT, scheduled ticks, entities, or absolute source coordinates.

Completed survey accessors are fixed:

```java
record PlaneSurvey(int schemaVersion, UUID owner, PlaneFrame plane)
record PlanePairSurvey(
    int schemaVersion, UUID owner, PlaneFrame first, PlaneFrame second
)
record RegionSurvey(int schemaVersion, UUID owner, RegionFrame region)
record RegionPairSurvey(
    int schemaVersion, UUID owner, RegionFrame first, RegionFrame second
)
record TemplateSurvey(
    int schemaVersion,
    UUID owner,
    BlockPos localAnchor,
    int sizeX,
    int sizeY,
    int sizeZ,
    BitSet occupied
)
```

## Native and Immersive Portals Boundary

Core spatial gameplay uses MnAGnosis services:

- finite oriented planes with explicit one-way faces;
- swept crossing detection and per-entity loop cooldowns;
- server-side position, velocity, yaw, and pitch transforms;
- conservative chunk-loaded checks;
- wireframe or masked client rendering;
- SavedData ownership and recovery.

The optional compatibility layer may create Immersive Portals portal or mirror
entities for recursive views, clipping, and smoother threshold traversal. It
must not become the authority for work identity, expiry, Closure, permissions,
or save migration. The compatibility design follows the one-way-face model
used by Immersive Portals rather than assuming one portal entity is inherently
bidirectional. Relevant inspiration:

- [Immersive Portals 1.20.1 source](https://github.com/iPortalTeam/ImmersivePortalsMod/tree/1.20.1)
- [Immersive Portals implementation notes](https://qouteall.fun/immptl/wiki/Implementation-Details)

## Cross-Plan Invariants

- Minecraft `1.20.1`, Forge, Java `17`, and Mana and Artifice `3.1.11` remain
  the supported baseline.
- All gameplay mutation starts on the logical server.
- All positions are dimension-qualified; raw `BlockPos` is never a persistent
  cross-dimensional identity.
- Before background work reads or mutates world state, call
  `level.hasChunkAt(pos)` or an equivalent loaded-region check. Architectonics
  never adds explicit or persistent chunk tickets. A user-triggered vanilla
  player teleport into an Unbounded Interior may create only vanilla's normal
  short-lived player/destination ticket; no work ticks or probes the cell until
  that arrival has loaded it.
- Claim/protection integration is centralized in
  `ArchitectonicPermissionService`; feature code does not special-case a
  protection mod.
- Every runtime transfer revalidates source and destination with
  `ArchitectonicAction.TRANSFER_ENTITY` or `TRANSFER_WORLD_STATE`; permission
  granted at creation is not a permanent claim bypass.
- Ordinary casts remain useful. Architectonics extends Mana and Artifice spell
  grammar and does not add Tier 7, a new faction, or a new resource bar.
- Contradiction remains the shared ledger. Architectonics expresses unresolved
  cost as Dislocation entries and satisfies them through legible Closure.
- Persistent works capture immutable authorship authorization when cast.
  Delayed execution cannot consult an already-cleared prepared-cast map.
- Matter-moving operations are conservative, transaction-based, and
  crash-recoverable. No code path duplicates a block state or inventory.
- Removal, expiry, death, logout, dimension unload, server restart, and config
  cap reduction each have explicit behavior in the owning feature plan.
- Packet decoders validate dimensions, counts, lengths, enum ordinals,
  permissions, ownership, distance, and menu/container identity.
- Every packet uses its reserved `ArchitectonicsPacketIds` constant; packet
  order and optional feature presence never determine an ID.
- Dedicated-server classloading is a release gate; common code never imports
  client-only or optional-mod-only classes.

## Parallel Work Lanes

The following lanes can proceed concurrently after plans `00` and `01` land:

| Lane | Plans | Shared touch points |
|---|---|---|
| A: conservative matter | `02`, then `11` | conservation service, permission service, transaction journal |
| B: fields and shapes | `03`, `04`, `05`, `06` | work records, persistent spell payloads, spatial index |
| C: traversal relations | `07`, `08`, `09` | plane transforms, crossing service, native renderer |
| D: world omission | `10` | collision/raycast query bridge and evacuation |
| E: authored places | `12` | dimension data, ownership, safe teleport |
| F: transcendent relation | `13` | every relation channel, Closure ledger, Dislocation |

Within one lane, honor the listed order. Across lanes, workers own feature
packages and feature tests; changes to shared interfaces require a short
cross-lane design commit before consumers continue.

## Recommended Delivery Cuts

1. **Core vocabulary:** `00`, `01`, Reassembled Land, Boundary Condition, and
   Lattice Emanation. This proves surveying, conservation, and spell-shape
   composition.
2. **Physical relations:** Axial Ordination and Load-Bearing Principle.
3. **Native traversal:** Metric Compression and Euclidean Refusal.
4. **Adjacency:** Axiom with living, vehicles, items, projectiles, and spells.
5. **World editing:** Hollow Domain and Coordinate Transposition.
6. **Authored place:** Unbounded Interior.
7. **Transcendent work:** World Seam.
8. **Enhancement pass:** recursive views, light/sound/fluid/redstone channels,
   richer shaders, and admin visualization.

Every cut must pass the native-backend test matrix before optional Immersive
Portals work begins.
