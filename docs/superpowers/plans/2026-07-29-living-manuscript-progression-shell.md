# Living Manuscript Progression Shell Implementation Plan

**Goal:** Expose all three discipline tracks through a persistent, read-only
Living Manuscript without implementing discipline mechanics.

**Architecture:** Register three Revelation-only definitions, grant them
idempotently from Truth or Tier-6 migration, and display a bounded server
snapshot in a client-only screen. The player capability remains authoritative.

## Implemented tasks

- [x] Register Definition, Relation, and Continuance Revelation proofs that
  evaluate to Perception.
- [x] Add idempotent initiation with immutable evidence UUID and server time.
- [x] Reserve core packet ID `5` for a bounded three-discipline S2C snapshot.
- [x] Add the Living Manuscript item, replacement recipe, Truth grant, and
  existing-player migration.
- [x] Add a two-page, three-tab read-only screen with keyboard navigation,
  visible stage ladder, and veiled future proofs.
- [x] Add unit, resource-contract, and Forge GameTest coverage.
- [x] Pass the complete JUnit, Forge GameTest, dedicated-server, resource, and
  reobfuscated JAR release gates.
