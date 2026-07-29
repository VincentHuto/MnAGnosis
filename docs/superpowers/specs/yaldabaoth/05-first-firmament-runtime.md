![Stage 05 concept preview](../../../concept-art/yaldabaoth-stages/05-first-firmament-runtime.png)

# Stage 05 — First Firmament Runtime

## Purpose

Provide a server-safe pocket arena runtime that can host simultaneous,
independent Yaldabaoth attempts and restore itself after every terminal state.

## Dimension and Cells

The First Firmament is one static dimension. Encounter cells sit at fixed,
widely separated coordinates and are restored from a versioned structure
template.

Each cell records:

- Cell identifier and template version.
- Encounter owner and locked participant UUIDs.
- `ORIGINAL` or `PROJECTION` variant.
- Current encounter phase.
- Boss, celestial, and controller UUIDs.
- Living, dead, disconnected, and returned participant states.
- Allocation, disconnect-grace, and cleanup timestamps.

The allocator never assigns an occupied, restoring, or version-incompatible
cell.

## Participant Lifecycle

- Roster size is captured when the portal locks.
- Difficulty scaling uses that locked size for the complete attempt.
- Death returns a participant to the Observatory and removes them from the
  active combat roster.
- Disconnected participants receive a bounded grace period. Expiry returns
  them on next login and counts them as having left.
- The attempt wipes when no living, connected-or-grace-period participant
  remains.
- A successful encounter returns every participant safely after rewards are
  committed.

## Persistence and Recovery

The controller persists at every phase transition and material roster change.
On load it verifies that cell, controller, boss, celestials, and roster agree.
If proof is incomplete, recovery must:

1. Mark the cell restoring.
2. Return online participants to the Observatory.
3. Queue safe return for offline participants.
4. Remove all encounter-owned transient entities.
5. restore the structure template.
6. Release the cell only after restoration succeeds.

Blocks placed, removed, or transformed during combat never survive cleanup.
Inventories and non-encounter entities cannot remain in the cell.

## Interfaces

The runtime must provide concepts equivalent to:

- `allocate(owner, participants, variant) -> encounter`
- `lockRoster(encounterId)`
- `markParticipantDead(encounterId, playerId)`
- `complete(encounterId, outcome)`
- `abortAndRestore(encounterId, reason)`

## Verification

- Allocate multiple cells without cross-encounter entity or packet leakage.
- Restore after wipe, owner logout, server restart, dimension unload, and
  partial entity loss.
- Confirm block snapshots and templates return to their canonical hashes.
- Confirm a cell is never reused before cleanup finishes.
- Confirm queued returns work for offline players.

## Handoff

Hosts the controller and entities introduced by Stages 06–15.
