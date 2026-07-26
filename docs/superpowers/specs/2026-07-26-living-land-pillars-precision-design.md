# Living Land Pillars and Precision Design

## Purpose

Living Land must attack with readable pieces of terrain rather than isolated
blocks. Each attack becomes a rigid three-to-five-block pillar assembled from
contiguous terrain. The new Tier 6 Ineffable Precision modifier changes a
physical terrain operation into projection: the spell carries exact visual
copies while leaving every source block untouched.

## Pillar Contract

- A Living Land strike owns one ordered payload of three to five exact block
  states.
- Sources extend away from the target into the supporting floor, wall, or
  ceiling, producing a thin contiguous pillar.
- Physical acquisition is atomic. If any source becomes invalid or protected,
  no source is removed.
- A successful physical pillar removes every source without drops, persists
  every state and source position, and settles every state exactly once.
- A projected pillar snapshots the same eligible states without removal,
  break events, placement, restoration, or item drops.
- The rigid formation rotates with its travel direction and tests collision
  along each rendered block segment.

## Modes

- Ceiling Crush drops a vertical column from a low ceiling.
- Wall Lances fire a horizontal column from the selected wall.
- Floor Teeth lift a vertical column and incline it toward the target.

Each wave launches one pillar at Magnitude below 2.0 and two pillars at
Magnitude 2.0 or greater. Pillar length is `3 + floor(magnitude - 1)`, clamped
to three through five. A caster may have four active pillars per dimension.

## Precision Modifier

`mnagnosis:precision` is a Tier 6 Ineffable modifier registered alongside
Polarity. Living Land detects it through the spell context and configures its
controller for projection.

Projection is server-authoritative. The server snapshots source positions and
states, spawns an ordinary pillar entity marked projected, and never edits the
world. Projected pillars deal the same damage and knockback as physical
pillars, then disappear. Their renderer adds a restrained alternating
black-and-white lattice shell to communicate that the matter is an authored
echo rather than displaced terrain.

The modifier is intentionally reusable by later terrain components. Living
Land consumes only the boolean projected/physical interpretation.

## Data Model

`LivingLandPillarPayload` owns an ordered list of `Entry(source, state)` plus a
`projected` flag and a settled flag. It provides:

- atomic physical acquisition with rollback;
- projection snapshot without mutation;
- NBT serialization of every exact block state;
- idempotent multi-block settlement;
- emergency restoration or one corresponding item per physical entry.

The strike synchronizes payload length, projection, and five block-state IDs
for rendering. Full source positions and states remain in NBT for persistence.

## Movement, Collision, and Settlement

The pillar entity moves as one homing body. Its normalized velocity defines its
long axis. Rendering places block cubes at one-block offsets around the entity
center. Collision constructs a swept 0.8-block AABB for every segment between
the previous and current positions. Any segment touching the selected target
causes a single damage and knockback event.

Physical settlement first tries to place the complete ordered pillar near the
impact using its current axis. If that cannot be done without overwriting
blocks or violating protection events, each entry restores to its source.
Remaining blocked entries become at most one corresponding block item each.
Projected payloads simply mark themselves settled and disappear.

## Presentation and Data

- Add `PrecisionModifier`, registry identity, recipe, icon, and English text.
- The physical renderer draws exact carried terrain.
- The projected renderer draws the same terrain plus sparse monochrome lattice
  particles and a thin alternating black/white outline.
- The existing controller and strike entity IDs remain unchanged.

## Verification

GameTests cover atomic acquisition rollback, three-to-five-block payloads,
exact multi-state NBT round trips, idempotent settlement, projection leaving
terrain unchanged, no projected drops or placement, modifier registration and
recipe resolution, pillar-count scaling, active caps, and per-segment
collision. A clean build, full GameTest run, client initialization smoke, and
JAR audit complete verification.
