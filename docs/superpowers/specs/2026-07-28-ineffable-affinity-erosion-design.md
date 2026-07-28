# Ineffable Affinity Erosion Design

## Summary

Casting a spell that contains a MnAGnosis ineffable spell-effect component will
replace Mana and Artifice's ordinary affinity gain with erosion of the player's
six core affinities. This expresses the caster gradually severing their ties to
the world's ordinary magical structure and gives repeated ineffable casting a
persistent opportunity cost.

## Goals

- Make every completed ineffable cast lower the six core affinities.
- Replace, rather than supplement, the cast's normal affinity gain.
- Apply the cost exactly once per completed cast.
- Keep normal magic XP and rote progression intact.
- Make the cost unavoidable with the Affinity Lock belt.
- Keep the rule centralized so future ineffable components can be added safely.

## Non-goals

- Do not change faction-derived affinities.
- Do not change ordinary Mana and Artifice spells.
- Do not add a new affinity, capability, HUD element, configuration option, or
  recovery mechanic.
- Do not rebalance mana costs, cooldowns, magic XP, or rote XP.

## Qualifying Spells

A spell is ineffable when its definition contains at least one of these
MnAGnosis spell-effect component registry IDs:

- `mnagnosis:components/true_damage`
- `mnagnosis:components/gravity_convergence`
- `mnagnosis:components/gravity_shift`
- `mnagnosis:components/living_land`
- `mnagnosis:components/banish`

Detection is based on stable registry IDs rather than Java object identity.
Shapes and modifiers alone do not qualify a spell. A mixed spell containing
both an ineffable effect and ordinary Mana and Artifice effects does qualify.

The qualifying registry IDs will be exposed through one classifier so adding a
future ineffable effect requires changing a single list.

## Affinity Behavior

On the normal successful-cast path where Mana and Artifice awards affinity and
magic XP, a qualifying spell performs this transaction:

1. Skip every ordinary affinity shift for that cast, including shifts
   contributed by ordinary components in a mixed spell.
2. Subtract `0.1` from each of the six core affinities:
   `ARCANE`, `EARTH`, `ENDER`, `FIRE`, `WATER`, and `WIND`.
3. Clamp each result independently to a minimum of `0.0`.
4. Force the player-magic capability to synchronize after a real change.

`BLOOD`, `HELLFIRE`, `ICE`, `LIGHTNING`, and `UNKNOWN` are not changed.

The erosion happens once per cast, not once per component. A spell containing
several ineffable effects still removes only `0.1` from each core affinity.
The Affinity Lock belt does not suppress erosion. Failed casts never reach the
award path and therefore do not erode affinity. Channeled spells erode once
when their completed channel reaches the same award path used for affinity and
XP.

Direct, clamped affinity assignment is required for erosion. Passing a negative
amount to Mana and Artifice's `shiftAffinity` API is invalid for this purpose:
that API is designed for positive shifts, affects other affinities, and does
not clamp the shifted affinity to zero.

## Architecture

### Ineffable spell classifier

A focused utility/service owns the set of qualifying component IDs and answers
whether an `ISpellDefinition` is ineffable. It has no player state and can be
tested independently.

### Core affinity erosion service

A focused server-side service owns the core-affinity list, the `0.1` erosion
constant, clamping, and capability synchronization. It accepts the player's
`IPlayerMagic` capability and changes only the six selected depths.

### Mana and Artifice integration

A narrow Mixin targets Mana and Artifice's
`SpellCaster.AddAffinityAndMagicXP` flow:

- At the single award entry, it classifies the spell and applies erosion once.
- At the ordinary `IPlayerMagic.shiftAffinity` invocation, it suppresses the
  shift when the same spell is ineffable.

The surrounding Mana and Artifice method continues normally, preserving magic
XP, rote XP, component progress, and all unrelated cast bookkeeping. The
erosion is placed before Mana and Artifice's Affinity Lock check, so the belt
cannot bypass it.

The integration must remain server-authoritative. Client execution, if the
upstream method is ever reached client-side, performs no mutation.

## Failure and Compatibility Behavior

- Missing player-magic capability: do nothing and leave the cast functional.
- Empty, null, or invalid spell definition: treat it as ordinary and do
  nothing.
- Affinity already at zero: leave it at zero.
- No core affinity changed: do not request an unnecessary sync.
- If Mana and Artifice changes the targeted award method in an incompatible
  update, Mixin application should fail visibly during development rather than
  silently applying the mechanic at the wrong lifecycle point.

## Testing

Automated coverage will verify:

- Every listed MnAGnosis effect qualifies.
- An ordinary Mana and Artifice spell does not qualify.
- A mixed spell qualifies.
- One erosion transaction subtracts exactly `0.1` from all six core
  affinities.
- Values below `0.1` clamp to zero.
- Blood, Hellfire, Ice, Lightning, and Unknown remain unchanged.
- A spell with multiple ineffable effects erodes only once.
- A qualifying mixed spell receives no ordinary affinity gain.
- Magic XP and rote behavior remain on the upstream path.
- The Affinity Lock belt does not block erosion.
- Failed casts do not erode affinity.

Pure classification and erosion math should use fast unit tests where
practical. Capability synchronization and the Mixin integration should use the
project's Forge GameTest pattern.

## Acceptance Criteria

The feature is complete when a successful cast containing any registered
MnAGnosis ineffable effect lowers each core affinity by `0.1`, never raises an
ordinary affinity from that cast, never lowers a non-core affinity, works
through an equipped Affinity Lock belt, and preserves the cast's other XP and
progression effects.
