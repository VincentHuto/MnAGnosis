# Three-Discipline Production Foundation Contract

**Status:** Implemented for MnAGnosis `1.1.0`.

MnAGnosis `1.2.0` exposes this dormant state through the
[Living Manuscript progression shell](2026-07-29-living-manuscript-progression-shell-design.md)
without changing the frozen protocol or discipline implementation order.

This contract supersedes discipline-local foundation assumptions in the
Autogenesis, Architectonics, and Worldlines plan suites. It intentionally adds
no player-facing components, instruments, recipes, proof grants, or progression
gates. Feature plans must consume these shared boundaries before adding content.

## Required implementation order

1. Autogenesis (`Definition`) proves the shared cast, instrument, Manuscript,
   and external-debt contracts with bounded entity-local behavior.
2. Architectonics (`Relation`) reuses those contracts and the conserved-block
   boundary before adding spatial persistence or transactions.
3. Worldlines (`Continuance`) follows after both, because delayed causality,
   replay safety, and recovery depend on the broadest set of shared contracts.

## Frozen network contract

Protocol remains `"5"`. Packet registration is manifest-based and fails on
duplicate classes, duplicate IDs, invalid owner ranges, or invalid size bounds.

| IDs | Owner |
|---:|---|
| `0-15` | Core (existing packets remain exactly `0-4`) |
| `16-47` | Autogenesis |
| `48-63` | Worldlines |
| `64-255` | Architectonics |

Feature omission leaves gaps. Registration order never assigns an ID. Custom
compressed payloads are capped at 32 KiB.

## Shared authored-cast contract

- Prepared casts are keyed by caster and spell fingerprint and expire after
  40 server ticks.
- A decorator chain invokes the effective downstream component exactly once.
  Recursive re-entry bypasses decoration and always clears its thread-local
  guard.
- Persistent work captures an immutable `AuthorshipCastPermit`; it may not
  consult an expired prepared-cast session later.
- An authored instrument is resolved only from the hand opposite the spell
  source. Inventory and Curios scans are forbidden.
- Instrument snapshots are typed, schema-versioned, defensively copied, and
  limited to 32 KiB compressed NBT.

## Shared Manuscript contract

One player capability owns Relation, Definition, and Continuance. Every
discipline advances through Perception, Intervention, Authorship, and Original
Work. Proofs are server-granted IDs with immutable grant time and evidence
UUID. Each discipline supplies one frozen progression definition. The
foundation registry is empty, so `1.1.0` exposes no proofs or visible
Manuscript content. Future schema versions fail closed to Perception.

## Shared Contradiction contract

There is one persistent ledger and one global cap of three. Authored Laws and
external discipline debts register lifecycle handlers in the same registry.
External payloads use typed codecs and their server action UUID is the
idempotency key. Overflow vents the oldest debt deterministically. A valid debt
is retained when its optional handler is unavailable; declaration is rejected
until a handler explicitly permits closure. Missing-handler fallback performs
no world mutation.

## Shared conservation contract

`ConservedBlockService` owns reserve, permission/event-aware placement,
restoration, and emergency drop behavior. Living Land remains the compatibility
facade and preserves its existing signatures. Architectonic matter movement
must begin with this service, then add a separate journal only when an operation
requires multi-block atomic recovery.

## Release gates

- Pure unit and contract tests pass.
- Forge GameTests, including Living Land conservation, pass.
- Production resources and the distributable JAR build.
- Dedicated-server classloading introduces no client-only dependency.
- A conflicting discipline plan must amend this contract and its consumers
  together; it may not create a local replacement.
