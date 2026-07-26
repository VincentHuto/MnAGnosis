# Ineffable Hybrid Authorship Design

## Purpose

Tier 6 must reframe the player's relationship with magic. Earlier tiers teach the
player to assemble spells within Mana and Artifice's established grammar. Ineffable
casting lets the player inscribe and reinterpret relationships that the grammar
normally treats as fixed.

The system must reward sequencing, deliberate tradeoffs, and understanding magical
relationships rather than provide another path to larger damage numbers.

## Design principles

- Tier 6 edits bounded magical laws; it does not grant unrestricted world editing.
- Authored effects remain legible, deterministic, and server-authoritative.
- Power creates a specific obligation rather than generic heat or random punishment.
- Closure is the intended answer to Paradox. Waiting never solves it.
- Venting is a predictable consequence of an ignored obligation.
- Existing Mana and Artifice casting remains unchanged when no Law Inscription is
  present.
- Initial compatibility is curated. Unsupported components are rejected instead of
  receiving guessed behavior.

## Core casting loop

An Ineffable spell may contain at most one **Law Inscription**. The initial laws are
Inversion, Exchange, and Suspension. The inscription determines the category of
relationship the spell may alter.

At casting time, the player selects one compatible **Interpretation**. The
Interpretation changes how the spell resolves and creates a named **Contradiction**.
Each Contradiction records:

- a stable unique identifier and creation order;
- its Law Inscription and Interpretation identifiers;
- its Paradox magnitude;
- its remaining safe casts;
- the requirements for Perfect and Forced Closure;
- the minimal target or value payload needed for deterministic Venting.

The resulting play sequence is:

`inscribe -> interpret -> incur debt -> arrange closure`

The player may carry at most three Contradictions. Creating a fourth immediately
Vents the oldest unresolved Contradiction after the creating spell resolves.

## Hybrid authorship

The Law Inscription is embedded in the spell recipe so the spell has a durable,
inspectable identity. Its current Interpretation is selected at cast time so the same
authored spell can be applied differently in different situations.

The available Interpretations are derived from both the inscription and the
compatible components present in the spell. A spell never exposes an Interpretation
that its component set cannot support.

The selected Interpretation is stored in the player's server-authoritative
Ineffable Casting State. It is keyed to a SHA-256 fingerprint of the equipped spell's
canonical serialized definition after runtime state, display text, and the selected
Interpretation are removed. This avoids mutating Mana and Artifice spell items merely
to cycle a casting stance.

## Initial laws

### Inversion

Inversion reverses a curated relationship instead of blindly negating a number.
Initial relationship families are:

- Vector: push and pull.
- Vitality: healing and harm.
- Revelation: reveal and conceal.
- Presence: summon and banish.
- Motion: acceleration and arrest.

Perfect Closure intentionally performs the recorded complementary relationship.
Forced Closure applies a weaker compatible inverse and pays the normal Forced
Closure surcharge. Venting expresses the recorded inverse involuntarily, centered on
the original target when valid and otherwise on the caster.

### Exchange

Exchange swaps or transfers a conserved property between two valid subjects. Initial
property families are:

- position;
- velocity;
- compatible status effects;
- remaining compatible effect duration;
- a bounded amount of casting mana.

Perfect Closure restores the recorded property or performs an equivalent balancing
exchange. Forced Closure repays the recorded imbalance from the caster's Ineffable
mana. Venting reverses the oldest unresolved exchange using its recorded subjects.
When a subject is unavailable, the handler uses its declared fallback without
loading a chunk.

### Suspension

Suspension moves a cost or consequence forward in the casting sequence. Initial
forms are:

- defer a bounded portion of a mana cost;
- delay an effect's activation;
- hold received force or damage briefly;
- suspend a compatible effect's expiration.

Perfect Closure deliberately releases or pays the suspended value. Forced Closure
pays it immediately plus the configured Forced Closure surcharge. Venting releases the recorded
consequence without favorable targeting or timing.

## Compatibility registry

An **Authored Law Registry** owns every supported relationship between a Law
Inscription and a spell component. Each handler must define:

- component and spell compatibility;
- available Interpretation identifiers;
- transformed cast behavior;
- Paradox calculation;
- Perfect Closure recognition;
- Forced Closure validation and result;
- Venting behavior;
- fallback behavior for unavailable targets.

Handlers are isolated from one another. Adding a later law such as Recursion must not
require changes to the Contradiction ledger, shared-capacity rules, or HUD state
model.

## Paradox calculation

Paradox uses the same unit scale and maximum capacity as Ineffable mana.

Unless a handler has a more appropriate conserved-value calculation, its default is:

`ceil(base mana cost * law coefficient * interpretation magnitude)`

The result is clamped to at least one and at most the player's maximum Ineffable mana
capacity. Initial coefficients are:

- Inversion: `0.35`.
- Exchange: `0.50`.
- Suspension: the larger of `0.50` or the fraction of the original consequence that
  was deferred.

Interpretation magnitude defaults to `1.0` and may be raised by handlers that alter a
larger share of the original spell. These values are server-configurable.

## Shared Mana and Paradox capacity

Current mana fills the capacity from the left. Paradox fills the same capacity from
the right. The safe-state invariant is:

`current mana + total Paradox <= maximum mana`

Paradox therefore reduces the amount to which mana may regenerate. Passive
regeneration and ordinary restoration stop at `maximum mana - total Paradox`; they
never Vent a debt and cannot be used to clear Paradox.

When an authored cast adds Paradox that would violate the invariant, unresolved
Contradictions Vent oldest-first until the new state fits. If the newly created
Contradiction still cannot fit, it Vents immediately after its spell resolves.
Authored magic is therefore permitted when dangerous rather than silently blocked.

Paradox never passively decays. Every Paradox unit belongs to one Contradiction and is
removed only when that Contradiction closes or Vents.

## Safe-cast windows

A new Contradiction permits three further successful casts by default. A law handler
may lower this to one or two for a particularly severe Interpretation, but may not
raise it above three.

After a successful spell resolves:

- newly created Contradictions do not age from their creating cast;
- every older unresolved Contradiction loses one safe cast;
- a Contradiction resolved by that cast does not age;
- any Contradiction reaching zero Vents after the cast resolves, oldest first.

Time, logout, dimension travel, and sleeping do not change safe-cast counts.

## Closure

### Perfect Closure

Perfect Closure is recognized automatically when a cast fulfills the recorded
complementary relationship.

- It removes the complete Contradiction and all linked Paradox.
- It charges only the closing spell's normal mana cost.
- It does not age the Contradiction it resolves.
- It still ages every other older unresolved Contradiction.

### Forced Closure

The player may select one Contradiction in the Law Wheel and declare the next
compatible spell as a Forced Closure attempt.

- The server validates the selected debt, equipped spell, target, and compatibility.
- The surcharge is `ceil(Paradox magnitude * 1.25)` Ineffable mana by default.
- A successful Forced Closure removes the complete Contradiction and all linked
  Paradox.
- If the player cannot afford both the spell and surcharge, the cast is rejected
  before resolution; no mana, safe casts, or targets are changed.
- The surcharge multiplier is server-configurable.

## Venting

A Contradiction Vents when:

- its safe-cast count reaches zero;
- it is the oldest debt when a fourth Contradiction is created;
- shared capacity cannot contain newly generated Paradox;
- its handler determines that a required recorded subject is permanently invalid.

Venting executes the handler's deterministic consequence, then removes the
Contradiction and all its linked Paradox even when a fallback was required. Venting
never force-loads chunks.

The expected consequence is visible from the debt's law and interpretation. Venting
does not select from a generic random mishap table.

## Controls

A configurable **Authorship key** controls the cast-time system:

- Tap cycles the equipped authored spell's compatible Interpretations.
- Hold opens the compact Law Wheel.
- The wheel shows available Interpretations and the three ordered Contradictions.
- Selecting a Contradiction and choosing **Declare Closure** arms one Forced Closure
  attempt.
- Perfect Closure requires no manual declaration.

All client requests are validated by the server against the currently equipped spell
and authoritative casting state.

## Counterlaw HUD

The HUD follows the approved monochrome Counterlaw design:

- a thin angular black frame;
- white mana advancing from left to right;
- a balanced black square lattice representing Paradox and advancing right to left;
- a denser lattice in any transient overlap during collision resolution;
- three detached squares representing Contradictions from oldest to newest;
- geometric degradation of each square as its remaining safe casts decrease;
- local figure-and-void inversion beginning at `45%` Paradox;
- multiple locally inverted frame sections beginning at `80%` Paradox.

Below `20%`, the frame remains stable and the small Paradox lattice is the only
instability. From `20%` through `44%`, the balanced lattice becomes the dominant
warning. From `45%` through `79%`, local frame segments invert. From `80%` through
`100%`, multiple segments contradict simultaneously.

No required state relies on color. Lattice extent, detached-square integrity, text,
and frame geometry communicate every state. A reduced-motion client option disables
positional jitter and animated transitions while retaining static inversion and all
state information.

## Server state and synchronization

A server-authoritative **Ineffable Casting State** stores:

- the three ordered Contradiction records;
- total Paradox derived from those records;
- the currently declared Forced Closure target;
- selected Interpretations keyed by equipped-spell fingerprint.

The state serializes to player capability NBT and survives player death, respawn,
logout, and dimension travel. Death cannot be used as Closure. Login and respawn
synchronization send only the state required by the HUD and Law Wheel.

Packets may request Interpretation changes or Forced Closure declarations. They may
not directly set Paradox, mana, Contradiction payloads, or Venting results.

## Failure handling

- Unsupported Law Inscription and component combinations are rejected during spell
  construction or validation.
- Authored effects repeat the underlying spell's normal target, PvP, team, claim,
  invulnerability, and permission checks. A law cannot move, harm, exchange with, or
  suspend consequences for a subject the base spell could not legally affect.
- Venting uses the original spell's owner and damage attribution so protections,
  advancement logic, and death messages remain coherent.
- A stale client Interpretation request is rejected and the authoritative selection
  is resynchronized.
- Missing entities and unloaded dimensions use each handler's declared fallback.
- No Closure or Venting path force-loads a chunk.
- Failed Forced Closure validation changes no resource or casting state.
- Corrupt or unknown serialized law identifiers are removed during load and their
  associated Paradox is not retained.
- Ordinary M&A spells and non-Ineffable players bypass the authorship system.

## Testing

Automated tests must cover:

- every initial law/component compatibility entry;
- rejection of unsupported inscriptions and Interpretations;
- deterministic Paradox calculations and configuration overrides;
- shared-capacity regeneration and restoration caps;
- three-debt ordering and fourth-debt oldest-first Venting;
- safe-cast aging and post-resolution Vent timing;
- Perfect and Forced Closure, including insufficient mana;
- immediate Venting of an oversized new Contradiction;
- invalid, dead, offline, unloaded, and cross-dimension subjects;
- save/load, persistence through death and respawn, login synchronization, and legacy
  migration;
- PvP, team, claim, invulnerability, and target-validity protection;
- multiplayer packet authority and stale requests;
- ordinary Mana and Artifice casting remaining unchanged;
- HUD threshold states and reduced-motion presentation.

## Non-goals for the first release

- Generic inferred inverses for arbitrary third-party components.
- More than one Law Inscription per spell.
- More than three simultaneous Contradictions.
- Recursion, unrestricted displacement, permanent terrain rewriting, or free-form
  scripting.
- Random Venting tables.
- Passive Paradox decay.
- Tier 7 or additional ordinary stat progression.

## Acceptance criteria

The design succeeds when a Tier 6 player can construct an authored spell, choose a
compatible cast-time Interpretation, understand the exact debt created, plan either
form of Closure within a short spell sequence, and deliberately risk a predictable
Vent. The system must create new magical decisions while leaving uninscribed M&A
spells useful and unchanged.
