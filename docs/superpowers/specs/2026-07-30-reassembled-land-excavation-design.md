# Reassembled Land Excavation Design

## Goal

Reassembled Land supports deliberate downward excavation with every Unbounded
Lattice pattern. Excavated matter is conserved in a temporary spoil pile near
the mouth of the excavation and returns to its exact original position when
Duration expires.

Above-ground construction remains unchanged.

## Cast Mode

A server-side cast enters excavation mode when the caster is aiming downward
and the spell impacts solid terrain. The impact is the mouth of the
excavation. The caster's look direction, including pitch, determines the
inward direction; the clicked block face does not turn a downward cast back
toward the sky.

All other casts use the existing construction transaction.

## Excavation Geometry

The excavation planner produces an ordered set of cells inside the terrain:

- **Wall:** a width-by-height planar cut at the impact, oriented across the
  caster's view.
- **Bridge:** a width-by-depth corridor following the aimed direction, with
  two blocks of walkable headroom.
- **Stair:** a descending staircase tunnel following the aimed direction.
  The floor profile is retained as steps while two blocks of body/head space
  are excavated above each step.
- **Pillar:** a cylindrical shaft whose axis follows the aimed direction.

Geometry is voxelized deterministically, deduplicated, loaded-only, and capped
by the existing 384-cell transaction limit. Replaceable cells need no move.
Non-air cells with block entities, fluids, unbreakable states, or positions
outside the world border reject the cast before mutation.

## Spoil Pile

Every excavated block is moved one-to-one into a replaceable spoil position
near the excavation mouth. Candidate positions:

1. remain in loaded chunks and inside the world border;
2. have stable support and can accept the exact excavated block state;
3. exclude the complete excavation volume;
4. exclude the player and the entrance approach;
5. prefer the sides and rear of the mouth;
6. fill a compact mound from its supported bottom layer upward.

The entrance and first approach cells remain unobstructed. Candidate ordering
is deterministic. If the planner cannot find one safe spoil position for
every excavated block, the cast fails with zero terrain mutation.

## Transaction and Restoration

Excavation uses the existing write-ahead receipt machinery. Each conserved
move records:

- source: the excavated terrain cell;
- target: its spoil-pile cell;
- exact original source state;
- exact original target state.

The forward transaction clears excavation sources and places their exact
states in the pile. No item drops are created.

At Duration expiry, forced idempotent restoration clears the spoil pile and
restores every excavated cell exactly. Natural changes to either endpoint do
not strand the receipt. Successful restoration closes the receipt and releases
receipt capacity. Expired legacy conflicted receipts continue to retry.

Existing piston, explosion, break, placement, and fluid protections apply to
both the excavation and spoil endpoints for the receipt lifetime.

## Failure Behavior

All validation and candidate selection occur before the write-ahead journal is
opened. An invalid cell, insufficient spoil capacity, unloaded endpoint, or
receipt-cap failure performs no mutation. A write failure uses the existing
idempotent rollback path.

## Verification

Pure tests cover downward orientation and hand-checked geometry for all four
patterns, including stair floor and two-block clearance.

Forge GameTests prove:

- each pattern removes solid terrain when cast downward;
- a stair tunnel descends into solid ground and remains walkable;
- the spoil mound stays beside the mouth without blocking the player or
  entrance;
- excavated block count equals spoil block count with no item drops;
- insufficient spoil space performs zero mutation;
- expiry restores the excavation, removes the mound, closes the receipt, and
  releases receipt capacity;
- ordinary above-ground construction behavior is unchanged.
