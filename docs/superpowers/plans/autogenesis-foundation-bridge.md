# Autogenesis Plan-Suite Foundation Bridge

The detailed Autogenesis suite currently lives on branch
`docs/autogenic-component-plans`. It is not merged into this branch because its
foundation and release documents still encode contracts superseded by the
implemented
[three-discipline foundation](../specs/2026-07-29-three-discipline-foundation-contract.md).

Before feature implementation or documentation merge, amend that suite as one
documentation-only change:

- `README.md`: make this foundation a prerequisite and retain Autogenesis as
  the first discipline delivered.
- `01-autogenic-runtime.md`: use `AuthoredComponentPipeline`,
  `AuthoredCastSessionStore`, `AuthoredInstrumentRegistry`, and
  `AuthorshipCastPermit`; keep Autogenic entity identity/revision state local
  only where it is genuinely entity-specific.
- `16-integration-and-release.md`: replace protocol `8` with protocol `5`;
  allocate Autogenesis packets only in `16-47`; consume the shared Definition
  Manuscript definition and typed external Contradictions; do not recreate the
  packet registrar, player Manuscript capability, cast pipeline, instrument
  lookup, or debt ledger.
- Move the Autogenesis content release beyond progression-shell version `1.2.0`; the
  exact next version is chosen when its feature cut is frozen.

These replacements are binding. The remaining entity-local safety contracts,
bounded revision model, target/protection policy, typed identity adapters, and
feature-specific tests in the Autogenesis suite remain useful planning input.
