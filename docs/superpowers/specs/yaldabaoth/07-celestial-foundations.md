# Stage 07 — Celestial Foundations

## Purpose

Create reusable Sun and Moon support entities with hostile, dormant, and
allied states under encounter-controller ownership.

## Shared Contract

Each celestial has:

- Its own health and stagger meter.
- A stable orbit slot and direction.
- `HOSTILE`, `DORMANT`, and `WITNESS` allegiance.
- Controller and encounter identifiers.
- Telegraph, active attack, recovery, and disabled states.
- No independent loot or persistence outside a valid encounter.

Focusing one celestial repeatedly raises the other's intensity through a
bounded anti-focus meter. This changes attack cadence or overlap, not maximum
damage without warning.

## Sun — Exposure

The Sun is a bright white disc with a black outline.

- Draw geometric lanes before igniting them.
- Track a judgment beam with a readable lock-in point.
- Apply Exposure so illuminated targets, including permitted encounter
  entities, take amplified damage.
- In Witness state, pin selected serpent segments with vertical white beams.

## Moon — Omission

The Moon is a black crescent with a white outline.

- Cast moving shadows that erase hostile projectiles.
- Conceal safe paths until players enter or trace the white outline.
- Temporarily omit marked floor sectors through controller-owned collision
  changes.
- In Witness state, remove protection from Sun-pinned serpent segments.

No omitted sector may strand a player without a telegraphed route or recovery.

## Dormancy

Stagger resolution converts a celestial to a non-attacking orbiting sigil. A
dormant celestial retains encounter identity and can later be redefined as a
Witness; it is not dead and cannot drop rewards.

## Verification

- Separate health and stagger values sync correctly.
- Anti-focus intensity rises, falls, and caps predictably.
- Exposure affects only controller-approved targets.
- Projectile erasure cannot delete unrelated protected entities.
- Omitted floor sectors restore after attack, wipe, and restart.
- Witness attacks cannot kill Yaldabaoth without participant damage.
- Visual identity remains legible without color.

## Handoff

Supplies both support entities to
[Stage 08](08-act-one-counterfeit-sky.md) and their Witness actions to
[Stage 13](13-act-three-no-other.md).
