# Stage 14 — Rewards and Original Work Bridge

## Purpose

Grant boss rewards per eligible participant, integrate the Ineffable faction
token, and establish the narrow persistent contract through which the first
victory unlocks an Original Work.

## Reward Eligibility

The encounter controller records meaningful participation through presence,
damage, authored actions, or supported encounter objectives. The killing blow
is neither required nor sufficient by itself.

Each eligible participant receives:

- One Ineffable Thesis.
- A uniformly selected 64–128 Marks of No Authority.
- The hidden challenge advancement **No Other**.

The first clear additionally grants once per player:

- One Axiomatic Husk.
- Permanent Original Work authorship access.
- One guaranteed lion-serpent trophy.

Reward delivery uses an idempotent encounter-and-player receipt. Inventory
failure sends recoverable items through the established safe-delivery path.

## Ineffable Thesis

The Thesis selects from the eligible Ineffable spell-part pool and prioritizes
parts the recipient has not learned. When the player knows every eligible
part, it produces a duplicate Thesis that the Broker exchanges for exactly 32
Marks of No Authority.

The pool must be explicit and versioned so adding a spell part does not alter
old unresolved Thesis items unpredictably.

## Marks of No Authority

Register the Mark as the canonical Ineffable faction token and return it from
`IneffableFaction.getTokenItem()`. Boss drops establish the initial supply;
this stage does not introduce generic ore, mob farming, or a repeatable common
recipe.

## Original Work Contract

This stage owns only:

- Persistent `originalWorkUnlocked` state.
- Axiomatic Husk grant and recovery.
- An advancement or criterion announcing the capability.
- A stable query for the eventual Original Work system.

It does not design or implement the full editor, scripting system, or complete
Original Work progression.

## Verification

- Simultaneous finishing damage delivers one receipt per eligible player.
- Restart and reconnect cannot duplicate first-clear or repeatable rewards.
- Ineligible spectators receive nothing.
- Thesis prioritizes unknown parts and uses the exact all-known fallback.
- The Mark is exposed through the faction interface.
- Original Work unlock and Husk recovery survive death and item loss.

## Handoff

First-clear state selects projection behavior in
[Stage 15](15-authored-projection-rematches.md).
