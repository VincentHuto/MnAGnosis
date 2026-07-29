# Worldlines Implementation Program

> **Foundation override (2026-07-29):** The implemented
> [three-discipline foundation contract](../../specs/2026-07-29-three-discipline-foundation-contract.md)
> is authoritative. Worldlines uses packet IDs `48-63`, protocol `"5"`, the
> shared Continuance Manuscript state, shared cast permits/instrument snapshots,
> and shared typed Contradiction lifecycle. Packet IDs `5-8`, protocol `"6"`,
> and discipline-local foundation types below are superseded. Worldlines begins
> only after Autogenesis and Architectonics exercise the shared contracts.

This directory is the implementation handoff for the Worldline material in
[`2026-07-28-three-disciplines-idea-vault-design.md`](../../specs/2026-07-28-three-disciplines-idea-vault-design.md).
The plans intentionally favor a broad, complete feature surface with explicit
trim seams. Persistence, permission checks, idempotence, conservation rules,
and recovery behavior are never trim candidates.

## Classification

| Concept | Registry or system ID | Form |
|---|---|---|
| Causal Bookmark | `mnagnosis:components/causal_bookmark` | Mana and Artifice `SpellEffect` |
| Path Memorial | `mnagnosis:components/path_memorial` | Mana and Artifice `SpellEffect` |
| Worldline | `mnagnosis:worldline` | Mana and Artifice `Shape` |
| Causal Spindle | `mnagnosis:causal_spindle` | Included Artifice path-storage item and removable trim seam |
| Deferred Arrival | `mnagnosis:components/deferred_arrival` | Mana and Artifice `SpellEffect` |
| Continuation | `mnagnosis:components/continuation` | Mana and Artifice `SpellEffect` |
| Causal Relay | `mnagnosis:components/causal_relay` | Mana and Artifice `SpellEffect` |
| Still Point | `mnagnosis:components/still_point` | Mana and Artifice `SpellEffect` |
| Recurrence | `mnagnosis:law_recurrence` / `mnagnosis:recurrence` | Law Inscription and authored Law |
| Foregone Path | `mnagnosis:components/foregone_path` | Mana and Artifice `SpellEffect` |
| Revision of Outcome | `mnagnosis:components/revision_of_outcome` | Mana and Artifice `SpellEffect` |
| Palimpsest Lens | `mnagnosis:palimpsest_lens` | Optional Curios equipment |
| Counterfactual Emanation | `mnagnosis:components/counterfactual_emanation` | Mana and Artifice `SpellEffect` |
| Consequence Without Cause | `mnagnosis:consequence_without_cause` | Interpretation of Suspension |
| The Long Moment | `mnagnosis:components/the_long_moment` | Mana and Artifice `SpellEffect` |
| Unspent Moment | `mnagnosis:unspent_moment` | Long Moment reward and Hour catalyst |
| The Unfinished Hour | `mnagnosis:unfinished_hourglass` | Artifice block, item, and block entity |

This program accounts for every entry under the spec's **Worldline
components** heading, the shared Worldline shape, and the supporting
instruments needed to make those mechanics concrete. Bell After Tolling,
Pendulum of the Still Point, Compass to the Road Not Taken, and Shoes of the
Returning Step remain separate instrument-vault concepts; they are not
silently substituted for, or added to, the component scope.

## Plan Index

1. [`00-worldline-runtime-foundation.md`](00-worldline-runtime-foundation.md)
   defines all shared interfaces, persistence, transactions, Remainder
   integration, networking, and rendering budgets.
2. [`01-causal-bookmark.md`](01-causal-bookmark.md)
3. [`02-path-memorial-and-worldline-shape.md`](02-path-memorial-and-worldline-shape.md)
4. [`03-deferred-arrival.md`](03-deferred-arrival.md)
5. [`04-continuation.md`](04-continuation.md)
6. [`05-causal-relay.md`](05-causal-relay.md)
7. [`06-still-point.md`](06-still-point.md)
8. [`07-recurrence-law.md`](07-recurrence-law.md)
9. [`08-foregone-path.md`](08-foregone-path.md)
10. [`09-revision-of-outcome.md`](09-revision-of-outcome.md)
11. [`10-counterfactual-emanation.md`](10-counterfactual-emanation.md)
12. [`11-consequence-without-cause.md`](11-consequence-without-cause.md)
13. [`12-the-long-moment.md`](12-the-long-moment.md)
14. [`13-the-unfinished-hour.md`](13-the-unfinished-hour.md)
15. [`14-worldline-integration-and-release.md`](14-worldline-integration-and-release.md)

## Dependency Graph

```text
00 Shared Worldline runtime
 ├─ movement lane:     01 → 02, 03, 08
 ├─ consequence lane: 04 → 05, 07, 11
 └─ field/history:     06, 09, 10, 12

07 Recurrence ───────┐
09 Mutation layer ───┼─→ 13 The Unfinished Hour
12 Unspent Moment ───┘

01–13 ─────────────────→ 14 Integration and release
```

After Plan 00 lands, the three lanes can be implemented in parallel. Within
each lane, plans that do not have an arrow between them are independent.
Plan 14 owns the final edits to central registries and shared localization so
parallel branches do not repeatedly collide in those files.

## Binding Program Rules

- Target Forge 1.20.1, Java 17, and the repository's current Mana and
  Artifice dependency.
- Worldlines remain server-authoritative. Clients receive revisioned visual
  summaries, never authoritative block snapshots, permission data, entity
  NBT, or player inventories.
- Do not add Immersive Portals as a runtime or compile dependency. Borrow its
  explicit transform-state and bounded rendering ideas only.
- Do not implement remote client worlds, redirected vanilla packets,
  recursive portal rendering, remote chunk synchronization, cross-world
  collision, or interaction through a visual surface.
- Other players cannot be involuntarily teleported, hidden, redirected, or
  movement-locked. Long Moment and Unfinished Hour participation requires
  explicit crouch consent.
- No Worldline operation rewinds health, death, inventory, equipment,
  Curios, durability, mana, XP, advancements, knowledge, cooldowns, hunger,
  AI memory, ownership, or arbitrary entity NBT.
- Do not add a second risk resource. Authored Worldline risk uses the existing
  `ContradictionLedger` and is presented as a Remainder.
- No operation adds persistent chunk tickets. An unavailable entity or chunk
  waits for natural availability or resolves through the plan's conservative
  fallback.
- Every delayed operation must be idempotent across duplicate packets,
  logout, clone, save/reload, and the crash window between claiming and
  resolving state.

## Shared Limits

| Limit | Value |
|---|---:|
| Active sessions per owner | 8 |
| Spatial lease per entity | 1 |
| Active Long Moments per owner/server | 1 / 8 |
| Active Unfinished Hours per owner/dimension/server | 1 / 1 / 4 |
| Worldline path nodes | 256 |
| Historical subjects per eligible player | 128 |
| Journal entries per Hour | 256 |
| Escrowed Hour stacks | 128 |
| Recorded Hour living subjects | 32 |
| Interval snapshot block states | 4,096 |
| Visible traces globally/per session | 64 / 32 |
| Custom Worldline packet payload | 32 KiB |
| Closed tombstone retention | 1,200 ticks |
| Closed tombstones per source dimension | 64 |

## Planned Trim Order

If implementation cost must be reduced, remove features in this order:

1. Advanced post-processing and dense visual traces.
2. The implemented Causal Spindle path-sharing module.
3. The Palimpsest Lens Magnitude 2 placement branch; preserve Magnitude 3's
   clean-break safety and conservation contract.
4. Long Moment multiplayer-specific presentation density; preserve real
   willing-player participation, consent, transfer, return, and caps.
5. Counterfactual multi-branch preview polish.

The mechanical core, bounded fallback visuals, recipes, localization,
permission checks, conservation rules, persistence, recovery, and automated
tests remain required.
