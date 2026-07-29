# Autogenesis Runtime and Axiom of Harm Design

**Status:** Approved for implementation.

## Purpose and Scope

This release begins Autogenesis with the smallest complete mechanical slice:
the shared Autogenesis cast boundary and Axiom of Harm. It consumes the
implemented three-discipline production foundation and Living Manuscript
progression shell rather than recreating their services.

The slice adds:

- a frozen Autogenesis spell-part and decorator registry;
- a Tier-6 Mana and Artifice modifier named `mnagnosis:axiom_of_harm`;
- exact adapters for fire-type immunity and undead poison immunity;
- a single-use, cast-bound authorization scope around the native harm call;
- the first Definition proof and the Perception-to-Intervention transition;
- focused unit, contract, resource, and Forge GameTest coverage.

It does not add the entity identity-revision capability, pattern registries,
Autogenesis command packets, state synchronization, shared entity visuals,
instruments, or any later Autogenic work. Those systems begin when a stateful
work first requires them.

## Binding Foundation Contracts

The following implemented services remain the sole owners of their concerns:

- `AuthoredComponentPipeline` owns decorator ordering, exactly-once downstream
  invocation, and recursive re-entry protection.
- `AuthoredCastSessionStore` owns prepared-cast lifetime and caster/fingerprint
  lookup.
- `AuthorshipCastPermit` is the immutable authority captured for persistent or
  delayed authored work.
- `AuthoredInstrumentRegistry` remains the only authored-instrument lookup
  boundary.
- `ManuscriptState` remains the only player progression store.
- `DisciplineProgressionRegistry` and `ManuscriptDefinitions` remain the
  progression-definition boundary.
- `PacketManifest` remains the packet allocation authority. Protocol stays
  `"5"` and this slice registers no packet.
- The shared Contradiction ledger and conserved-block service are unchanged.

The legacy Autogenic runtime plan remains useful for entity-local revision
safety, targeting policy, and future typed adapters. Its duplicate cast
sessions, component pipeline, instrument lookup, packet registrar, Manuscript
capability, and protocol-bump assumptions are superseded.

## Runtime Architecture

### Autogenesis access

`AutogenicAccess` answers whether a player may use a registered Autogenic part.
For this slice, access requires the existing Tier-6 Ineffable faction and an
initiated Living Manuscript Definition track. Axiom is available at
Perception; the successful proof advances the player to Intervention.

Access is checked on the server. Client recipe visibility and presentation do
not grant authority.

### Spell classification

`AutogenicSpellClassifier` inspects the ordered Mana and Artifice spell
definition and returns registered Autogenic decorators. It fails closed when
component order, part identity, or registry identity is unavailable.

An ordinary spell containing no registered Autogenic part bypasses
Autogenesis entirely.

### Decorator registration

`AutogenicBootstrap` freezes all Autogenesis extension registries during common
setup. Axiom registers one decorator into the shared
`AuthoredComponentPipeline`; Autogenesis does not create a second pipeline.

The decorator receives immutable invocation context containing:

- the active `AuthorshipCastPermit`;
- ordered component index and exact modified part;
- `SpellSource` and `SpellContext`;
- the local target;
- the selected harm adapter and gate.

It invokes the effective native component exactly once.

## Axiom of Harm

### Spell grammar and cost

Axiom is registered in `Registries.Modifier` under
`mnagnosis:axiom_of_harm`. It is Tier 6 and uses the existing Ineffable faction.

Its final cost is:

```text
base-or-authored mana cost × 1.35
```

The surcharge applies once per spell. Repeated Axiom modifiers neither stack
nor open additional immunity gates.

### Deterministic selection

The selector scans `ISpellDefinition.getComponents()` in recipe order and
chooses the first component that is:

1. harmful according to its native use tag;
2. registered under the exact expected component ID; and
3. an instance of the adapter's exact compatible runtime type.

The initial frozen mapping is:

| Native component | Gate crossed |
|---|---|
| `mna:components/fire_damage` | fire-type immunity |
| `mna:components/poison` | undead poison immunity |

True Damage and all other harmful components are unsupported. Missing order
data, null parts, missing IDs, ambiguous adapters, or an unfrozen registry
invalidate Axiom rather than guessing.

### Target policy

Authorization is denied for a null, dead, removed, unloaded, cross-dimension,
invulnerable, creative, spectator, allied, or PvP-protected target.

Being fire-immune or undead is not itself a rejection because that is the
specific proposition Axiom is allowed to revise. Boss and technical-entity
handling follows the native harm call unless an explicit deny rule is needed
by a tested compatibility case.

### Single-use authorization

The invocation scope uses a private thread-local stack. Authorization binds:

- cast permit ID;
- component index, registry ID, and modified-part identity;
- `SpellContext` identity;
- target UUID;
- adapter ID and harm gate;
- native `DamageSource` or `MobEffectInstance` identity where applicable.

The exact matching native immunity check consumes the authorization. A second
check, another target, component, spell context, native harm object, nested
cast, or later tick cannot borrow it. All frames close in `finally`, including
exceptional paths.

No bypass state is stored in capabilities, saved data, entity maps, NBT,
packets, or cast-session state.

### Native semantic preservation

Mixins alter only the audited immunity decision. The original damage or effect
application remains responsible for:

- damage source, amount, causing entity, and direct entity;
- Forge attack, damage, and effect applicability events;
- armor, resistance, enchantments, cooldown frames, absorption, and shields;
- poison duration, amplifier, permanence, reagents, and application result;
- attribution, sounds, and particles.

Fire resistance is not fire immunity and remains effective. Axiom never grants
an invulnerability, armor, resistance, shield, creative, spectator, team, or
PvP bypass.

## Living Manuscript Progression

The proof ID is exactly:

```text
mnagnosis:definition/axiom_of_harm
```

The server grants it only after the invocation scope confirms that:

1. the target possessed the selected immunity;
2. the matching immunity gate consumed its authorization; and
3. the native component completed a real target application.

Selection, casting, hitting an already vulnerable target, a cancelled native
event, or a failed application grants no proof.

The proof uses the existing immutable grant-time and evidence-UUID model.
Repeated success is idempotent. The Definition progression definition evaluates
the Revelation proof alone as Perception and Revelation plus the Axiom proof as
Intervention. Relation and Continuance remain unchanged at Perception.

The next Manuscript snapshot naturally reflects the new stage; no new packet or
client mutation path is added.

## Data and Control Flow

1. Server mana calculation detects Axiom and applies the 35% surcharge after
   existing authorship cost calculation.
2. Cast preparation classifies the ordered components and captures the selected
   adapter in the existing authored-cast context.
3. Component application enters the shared authored-component pipeline.
4. The Axiom decorator validates access, selection identity, and target policy.
5. It opens one authorization frame and invokes the native component once.
6. A narrow Mixin consumes the frame at the matching immunity gate.
7. Native Mana and Artifice and Forge code performs the harm application.
8. A confirmed successful crossing grants the Definition proof
   idempotently.
9. The next Living Manuscript open receives the ordinary bounded core snapshot
   showing Intervention.

## Failure Handling

All uncertainty fails closed. Invalid spell structure, missing registry data,
wrong component class, unsupported harm, inaccessible progression, invalid
target, unmatched scope, cancelled application, and exceptional native calls
produce no bypass and no proof.

Registry duplication or mutation after freeze is a startup error. Mixins use
strict injection requirements so a changed Mana and Artifice call site fails
development verification rather than silently broadening behavior.

## Testing and Verification

TDD covers:

- component-order preservation and first-compatible selection;
- frozen adapter registration, exact ID/class matching, and duplicates;
- unsupported harm and True Damage rejection;
- 35% cost once per spell;
- access at Definition Perception and rejection before initiation;
- invalid, allied, creative, spectator, and PvP-denied targets;
- independent target authorization and nested-call isolation;
- single consumption and guaranteed thread-local cleanup;
- exact fire and poison Mixin boundaries;
- preservation of native source/effect objects and protection events;
- no behavior change for Axiom-free spells;
- proof denial when no immunity was crossed or native application failed;
- idempotent proof grant after successful crossing;
- Definition transition to Intervention with other tracks unchanged;
- modifier registration, recipe, translation, texture, and packaged resources.

Focused JUnit runs precede implementation for each behavior. Forge GameTests
cover real fire-immune and undead targets, negative controls, proof persistence,
and Manuscript stage projection. Final verification includes focused tests,
the full unit suite, compilation, resource processing, GameTests, dedicated
server classloading, and JAR inspection.

## Delivery Boundary

This implementation is complete when the foundation-aligned Autogenesis cast
boundary and Axiom work end to end, the Definition proof advances the Living
Manuscript to Intervention, and all stated verification gates pass.

Later Autogenic works may extend the frozen registries through explicit
bootstrap entries. They may not replace the shared foundation or widen Axiom's
two audited immunity gates without a new design amendment.
