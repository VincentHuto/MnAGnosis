# Gravity Convergence Design

## Purpose

Gravity Convergence is the first Ineffable spell component that reframes magic
as authorship over a universal law rather than a stronger damage source. It
creates a persistent, visible field whose behavior is determined by the spell
shape and whose direction can be reversed by the Polarity modifier.

This spec covers Gravity Convergence only. Living Land, Reassembled Land, and
True Self remain separate follow-up projects, in that order.

## Player Contract

- The component is available only to the Tier 6 Ineffable faction.
- Casting without Polarity creates an attractive field.
- Adding the dedicated Polarity modifier creates a repulsive field.
- The field anchor is inferred from the final `SpellTarget`:
  - a self target follows the caster;
  - another entity target follows that entity;
  - a block target remains fixed at the targeted position;
  - projectile and other impact shapes inherit the entity or block target
    produced at impact.
- The field affects living entities, dropped items, and projectiles.
- It never affects its caster, spectators, allied players, or entities owned by
  the caster.
- It never moves blocks, force-loads chunks, or deals direct damage.
- Environmental consequences remain real: a displaced target can still suffer
  collision, void, lava, or later fall damage.

## Field Architecture

Each successful cast spawns a server-authoritative `GravityFieldEntity`.
Minecraft's normal entity persistence and tracking handle save/load and client
visibility. The field stores:

- owner UUID;
- anchor mode (`FIXED`, `CASTER`, or `TARGET`);
- optional tracked target UUID;
- polarity;
- radius;
- remaining duration in ticks;
- magnitude;
- response speed.

Moving fields resolve their anchor on each server tick. They discard themselves
without loading chunks when their anchor is removed, changes dimension, or is
otherwise unavailable. Fixed fields persist at their cast position.

A caster may own at most three active Gravity Convergence fields in a
dimension. Creating a fourth discards the oldest active field before spawning
the new one. This keeps the mechanic expressive while bounding server work.

## Attributes and Balance

The component exposes four ordinary M&A attributes:

| Attribute | Default | Minimum | Maximum | Step | Meaning |
| --- | ---: | ---: | ---: | ---: | --- |
| Radius | 5 blocks | 3 | 12 | 1 | Spherical field size |
| Duration | 8 seconds | 4 | 30 | 2 | Persistent lifetime |
| Magnitude | 1.0 | 0.5 | 3.0 | 0.5 | Force strength |
| Speed | 1.0 | 0.5 | 3.0 | 0.5 | Acceleration response |

Runtime safety bounds are independent of authored attributes:

- maximum acceleration added per tick: `0.12` blocks/tick²;
- maximum resulting velocity: `1.50` blocks/tick;
- attraction capture-shell radius: `0.85` blocks;
- no force is applied outside the radius;
- targets at the exact center receive damping rather than an undefined vector.

Attraction accelerates targets toward the center and smoothly damps them into
the capture shell instead of crushing them into a single point. Repulsion
accelerates away from the center and fades over the outer 20% of the radius so
targets leave without a sharp velocity discontinuity.

## Target Protection

The target filter is centralized and deterministic:

- exclude the field entity and owner;
- exclude removed, no-physics, and spectator entities;
- include `LivingEntity`, `ItemEntity`, and `Projectile`;
- exclude allied living entities according to `owner.isAlliedTo(candidate)`;
- exclude tameable animals owned by the caster;
- exclude projectiles whose owner is the caster;
- exclude other entities that expose the caster UUID as their owner when the
  vanilla ownership interfaces make that relationship available.

The field marks moved entities as having impulse and synchronizes their new
velocity. It does not override collision or invulnerability rules.

## Visual Language

The field is intentionally monochrome and geometric:

- sparse black and white square/block traces spawn throughout the volume;
- traces drift along the current force direction;
- attraction traces bend inward; repulsion traces bend outward;
- a brief denser square shell at cast time communicates the field boundary;
- the spell component and Polarity icons use the same black lattice language.

Visuals are client-only feedback derived from the tracked field entity. They do
not drive physics and may be reduced by particle settings without changing
gameplay.

## Registration and Data

- Component registry ID: `mnagnosis:components/gravity_convergence`
- Modifier registry ID: `mnagnosis:polarity`
- Entity registry ID: `mnagnosis:gravity_field`
- Component recipe: Tier 6, using the Primal Mote, Tesseract, greater arcane and
  ender motes, iron, black concrete, and white concrete.
- Modifier recipe: Tier 6, using a Tesseract, magnetic materials, and
  monochrome concrete.
- Both parts have names and attribute descriptions in `en_us.json`.

Polarity is a normal craftable Tier 6 Ineffable modifier, not an authored Law
inscription. Gravity Convergence detects it from
`SpellContext#getSpell().getModifiers()`.

## Failure and Lifecycle Rules

- A null/invalid caster or unsupported target returns `FAIL` and spawns nothing.
- Entity targets that are already removed return `FAIL`.
- A valid cast returns `SUCCESS` only after the field is added to the level.
- If a moving anchor later becomes invalid, the field simply expires.
- Duration reaches zero on the server and discards the entity.
- Old saved fields with missing optional tags load conservative defaults and
  remain bounded by runtime clamps.

## Verification

GameTests cover:

- component, modifier, entity, recipe, icon, and localization registration;
- Tier 6 Ineffable craftability;
- shape-dependent fixed, caster-following, and entity-following anchors;
- attraction and repulsion force direction;
- capture-shell damping and outer-edge repulsion falloff;
- exclusions for owner, ally, owned tameable, owned projectile, and spectator;
- inclusion of hostile living entities, dropped items, and foreign projectiles;
- no direct health loss;
- duration expiry and invalid-anchor expiry;
- the three-fields-per-caster replacement rule;
- NBT round-trip and conservative clamping.

The final verification also runs a clean build, the full GameTest server suite,
and a client startup smoke test through resource and entity renderer
registration.
