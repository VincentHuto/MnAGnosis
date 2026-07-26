# Living Land Design

## Purpose

Living Land is the second Ineffable universal-law component. It does not summon
generic earth projectiles or create damage with a terrain-themed particle. It
temporarily gives nearby terrain a hostile purpose: the environment reads its
own geometry, chooses an attack form, and physically relocates eligible matter
to pursue a selected creature.

This spec covers Living Land only. Reassembled Land and True Self remain
separate follow-up projects.

## Player Contract

- Living Land is a Tier 6 Ineffable spell component.
- It requires a hostile living-entity target and rejects blocks, dead targets,
  allies, the caster, and spectators.
- A successful cast creates a persistent conductor that follows the target and
  launches repeated terrain attacks for the authored duration.
- The attack form is selected automatically from the currently loaded terrain:
  - **Ceiling Crush:** a low, solid ceiling sends blocks downward.
  - **Wall Lances:** enclosing side walls fire horizontal one-block lances.
  - **Floor Teeth:** solid ground rises in opposed snapping strikes and is the
    open-terrain fallback.
- Every strike carries an exact source block state. The source becomes air only
  after all eligibility and protection checks pass.
- A completed strike attempts to deposit its carried block in a safe
  impact-adjacent position. If it cannot do so, the original source is restored.
- Damage is ordinary sourced magic/physical damage. It respects armor,
  invulnerability, hurt cooldown, teams, and normal game rules.
- The target can dodge moving strikes.

## Approaches Considered

### Persistent conductor and moving block strikes — selected

A `LivingLandControllerEntity` scans terrain and schedules waves. Each wave
spawns `LivingLandStrikeEntity` instances that carry real block states, move
toward the tracked target, collide, and settle or restore their blocks. This
provides honest conservation, readable timing, persistence, and one place for
cleanup rules.

### Instant terrain rays

This would move blocks and apply damage immediately along floor/wall/ceiling
axes. It is cheaper, but offers little visual anticipation and almost no room
for evasion.

### Cosmetic terrain

This would leave blocks untouched and apply ordinary spell damage under
particles. It is robust but fails the core promise that nearby matter has been
coerced into attacking.

## Terrain Classification

`LivingLandTerrain` performs a bounded, loaded-chunk-only scan centered on the
target. It never loads a chunk.

The scanner samples:

- floor columns from one to three blocks beneath the target;
- ceiling columns from two to five blocks above the target;
- the four horizontal cardinal rays from two blocks above the target out to the
  authored radius.

It returns a `LivingLandMode` and ordered source candidates.

Mode priority is deterministic:

1. `CEILING_CRUSH` when at least two eligible ceiling sources exist no more than
   five blocks above the target.
2. `WALL_LANCES` when at least two distinct horizontal sides provide eligible
   sources with clear air paths.
3. `FLOOR_TEETH` when at least two eligible floor sources exist.
4. no mode when the terrain cannot support a conserved attack.

The priority makes low caves feel oppressive, enclosed caves directional, and
open ground predatory without adding a second player-facing mode selector.

## Source Eligibility and Protection

A block may become a strike only when all of these are true:

- its chunk is already loaded;
- its state is not air and has no fluid;
- it has no block entity;
- destroy speed is finite and non-negative;
- it is not tagged `mnagnosis:living_land_immune`;
- it is not a portal, command block, structure block, jigsaw, bedrock, barrier,
  end portal frame, spawner, moving piston, or other explicitly protected block;
- `level.mayInteract(caster, position)` succeeds;
- Forge's block-break event does not cancel the edit.

Source removal does not drop items or experience. This prevents duplication:
the exact state is held by the strike until it is deposited or restored.

Placement requires an already loaded position, a replaceable destination, no
block entity conflict, world-border permission, and a non-cancelled Forge place
event. A carried state never overwrites a solid block.

## Strike Architecture

### `LivingLandControllerEntity`

The invisible server-authoritative controller stores:

- owner UUID;
- target UUID;
- remaining ticks;
- radius;
- magnitude;
- strike speed;
- next-wave delay;
- creation time.

It follows the target without force-loading. It expires when the owner or target
is unavailable, changes dimension, dies, becomes allied, or leaves loaded
terrain. Every 16 ticks it rescans and emits a wave. If a scan cannot find an
eligible mode, the wave is skipped rather than damaging terrain arbitrarily.

A caster may own at most two active Living Land controllers in a dimension.
Creating a third discards the oldest. Controller removal does not destroy
already-launched strikes; those finish their conservation lifecycle.

### `LivingLandStrikeEntity`

Each strike stores:

- owner and target UUIDs;
- original source position;
- carried block state;
- selected mode;
- damage;
- speed;
- age and maximum lifetime;
- whether its source has been restored or deposited.

The strike begins at the center of its removed source block and steers toward a
snapshot of the target's body position with a limited homing correction. It
does not teleport or pass through unloaded chunks. Its collision box is 0.8
blocks wide.

On living-target collision it applies damage once, adds mode-specific knockback,
then settles. On block collision, target loss, timeout, save/load inconsistency,
or removal it settles without damage. Settlement tries these positions in
order:

1. a mode-specific impact-adjacent position;
2. the nearest replaceable position in a bounded 3×3×3 shell;
3. the original source position.

If all placement attempts fail but the original source is still replaceable,
the original is restored. If another block now occupies every candidate,
settlement drops one corresponding block item only when the carried state has a
normal item form; otherwise it logs and discards the state. This terminal
fallback prevents silent duplication and avoids overwriting later player edits.

## Attributes and Balance

| Attribute | Default | Minimum | Maximum | Step | Meaning |
| --- | ---: | ---: | ---: | ---: | --- |
| Radius | 6 blocks | 4 | 12 | 1 | Terrain scan reach |
| Duration | 6 seconds | 3 | 20 | 1 | Controller lifetime |
| Magnitude | 1.0 | 0.5 | 3.0 | 0.5 | Damage and strikes per wave |
| Speed | 1.0 | 0.5 | 3.0 | 0.5 | Strike travel speed |

Runtime bounds:

- one wave every 16 ticks;
- `2 + floor(magnitude)` strikes per wave, capped at five;
- base damage `3 + 2 × magnitude`, capped at nine before global/config
  multipliers;
- travel speed `0.28 + 0.08 × speed`, capped at `0.52` blocks/tick;
- maximum strike lifetime 60 ticks;
- maximum two controllers per caster per dimension;
- maximum eight active Living Land strikes per caster; excess candidates are
  skipped.

## Mode Behavior

- **Ceiling Crush:** sources descend toward the target's upper body; impact
  biases blocks above/around the target and applies downward knockback.
- **Wall Lances:** sources launch from different cardinal sides toward the
  target's center; impact deposits outward from the struck side and applies
  horizontal knockback.
- **Floor Teeth:** sources rise from opposed positions around the feet; impact
  deposits beside the target and applies short upward knockback.

Modes affect source ordering, approach vector, particles, deposit preference,
and knockback. They do not alter eligibility or conservation guarantees.

## Visual Language

- The carried terrain block renders normally so the player can read what matter
  was taken.
- Sparse black and white square particles trace its path.
- A monochrome lattice briefly appears at the source hole and intended impact.
- Ceiling, wall, and floor modes use different lattice orientations but no
  colored effects.
- The spell icon depicts a black/white terrain lattice curling into teeth.

## Registration and Data

- Component ID: `mnagnosis:components/living_land`
- Controller entity ID: `mnagnosis:living_land_controller`
- Strike entity ID: `mnagnosis:living_land_strike`
- Immune block tag: `mnagnosis:living_land_immune`
- Tier 6 component recipe using a Primal Mote, Tesseract, greater earth and
  arcane motes, pointed dripstone, black concrete, and white concrete.
- Names and descriptions are added to `en_us.json`.

The Polarity modifier does not alter Living Land. Polarity remains specific to
components that explicitly define an inverse relationship.

## Persistence and Recovery

Controllers and strikes use synced entity data for client-visible state and NBT
for server persistence. Numeric values are clamped while loading.

Strike NBT includes the exact carried block state through
`NbtUtils.writeBlockState` and the original source position. A strike loaded
without a valid state settles immediately. A strike removed through any normal
path calls the same idempotent settlement routine. The `settled` flag prevents
duplicate placement or drops.

No controller or strike force-loads chunks. Entering an unloaded destination
causes settlement at the last safe loaded position or source restoration.

## Verification

Pure and GameTest coverage includes:

- deterministic ceiling/wall/floor classification and priority;
- source eligibility and protected-block exclusions;
- exact block-state removal, NBT round-trip, deposit, restoration, and
  no-duplication behavior;
- moving collision, dodgeable travel, timeout, and invalid-target cleanup;
- ordinary damage and mode-specific knockback;
- owner, ally, spectator, and dead-target rejection;
- two-controller and eight-strike caps;
- Tier 6 Ineffable component registration, attributes, recipe, localization,
  icon, entity types, and renderer classes;
- no edits in unloaded chunks;
- clean build, full GameTest suite, client registry/atlas initialization, and
  packaged-jar inspection.
