# MnAGnosis

## Lore and Design Bible

> The world is not false. It is unfinished.

| Field | Value |
|---|---|
| Project | Mana and Artifice: Gnosis |
| Platform | Minecraft Forge 1.20.1 |
| Purpose | Late-game addon to Mana and Artifice |
| Document role | Living lore bible and technical future-state reference |
| Snapshot | 2026-07-25 |

This is the central creative and production reference for MnAGnosis. It records what the
mod means, what already exists, what direction has been chosen, and what remains open
for invention.

The document is intentionally divided into two books:

- **Book I - Gnosis** defines the lore, themes, symbols, language, and desired player
  experience.
- **Book II - The Work** records the implementation snapshot, target systems, feature
  concepts, priorities, and unresolved design work.

---

## How to Use This Bible

Every substantial entry should carry a maturity or production label. The labels keep a
powerful idea from accidentally becoming canon and keep a finished mechanic from being
mistaken for a loose concept.

### Lore maturity

| Label | Meaning |
|---|---|
| **Canon** | Accepted creative truth or meaning already established by the mod |
| **Strong Direction** | Chosen direction whose exact expression may still change |
| **Unbound Idea** | Speculative material preserved for future exploration |

### Production status

| Label | Meaning |
|---|---|
| **Implemented** | Present in the repository and intended to function now |
| **In Progress** | Actively being built or revised |
| **Designed** | Direction is established but implementation is not complete |
| **Exploring** | Worth investigating; requirements are not settled |
| **Deferred** | Intentionally outside the current development horizon |

### Editing rule

When an idea changes status, move it rather than duplicating it. Preserve major changes
in the [Decision Log](#decision-log) and [Revision History](#revision-history).

---

## Contents

- [Book I - Gnosis](#book-i---gnosis)
  - [Creative Manifesto](#creative-manifesto)
  - [Core Axioms](#core-axioms)
  - [The Player's Ascent](#the-players-ascent)
  - [Cosmology](#cosmology)
  - [Truth](#truth)
  - [The Tesseract](#the-tesseract)
  - [The Pentad of Authorities](#the-pentad-of-authorities)
  - [The Broker](#the-broker)
  - [The Two Offerings](#the-two-offerings)
  - [Sensory Language](#sensory-language)
  - [Writing Voice](#writing-voice)
  - [Fragment Bank](#fragment-bank)
  - [Terminology](#terminology)
  - [Idea Garden](#idea-garden)
- [Book II - The Work](#book-ii---the-work)
  - [Current-State Snapshot](#current-state-snapshot)
  - [Target Ascension Flow](#target-ascension-flow)
  - [Technical Design Principles](#technical-design-principles)
  - [Feature Families](#feature-families)
  - [Feature Cards](#feature-cards)
  - [Roadmap](#roadmap)
  - [Decision Log](#decision-log)
  - [Open Questions](#open-questions)
  - [Reusable Entry Templates](#reusable-entry-templates)
  - [Revision History](#revision-history)

---

# Book I - Gnosis

> There is no final spell. There is only the moment the mage understands that a spell
> is a courtesy paid to lesser laws.

## Creative Manifesto

**Canon**

MnAGnosis begins where conventional magical progression ends.

Mana and Artifice teaches the player to work within a magical world: gather mana,
discover components, compose spells, join a faction, and rise through an established
order. MnAGnosis asks what happens after the player masters that order and discovers
that its laws are neither infinite nor final.

The world is not an illusion. Stone is stone. Death has weight. Gods possess real
power. The revelation is that all of these are arrangements - stable, meaningful, and
real, but not absolute. To a mortal, reality is a prison of facts. To an ascendant,
those same facts are materials.

Gnosis is the knowledge that existence can be authored.

The player does not escape the world. The player outgrows the scale at which the world
appears immutable.

## Core Axioms

### Reality is low-order

**Canon**

The ordinary world is a valid but limited expression of deeper principles. It is
"simple" only from the perspective of beings capable of perceiving and editing the
rules beneath it.

### Knowledge and power converge

**Canon**

At lower tiers, knowledge produces better tools. At the threshold of Gnosis, knowledge
changes what the player is. Understanding becomes a form of authority.

### Ascension is self-authored

**Canon**

No god grants the final rank. Factions, rituals, artifacts, and higher powers may open
the threshold, but the act of crossing it belongs to the player.

### Gods are mighty, not ultimate

**Canon**

Defeating Odin matters because he is genuinely divine. The victory proves that
divinity is a station within existence, not the ceiling above it.

### Form is an interface

**Strong Direction**

Bodies, items, blocks, and even familiar geometry are interfaces through which deeper
structures become temporarily legible. Shifting forms are not visual decoration; they
suggest that the observed object has more states than the world can display at once.

### Truth does not explain itself

**Canon**

Truth reveals through encounter, transformation, contradiction, and demand. It should
never become a conventional quest-giver who summarizes the cosmology.

## The Player's Ascent

The ascent should feel like a change in category, not another tier of larger numbers.

| Stage | Player identity | Proof | Meaning |
|---|---|---|---|
| I. Practitioner | A mage who manipulates known magic | Ordinary Mana and Artifice progression | The player learns the grammar of the world |
| II. Master | A faction-aligned authority | Tier 5 mastery | The player commands an established magical tradition |
| III. Iconoclast | A conqueror of authorities | The Pentad of Authorities | Every factional answer has been overcome |
| IV. Counterparty | One whose value is recognized | The Broker exchange | The player can participate in higher equivalence |
| V. Witness | One who survives contact with Truth | The offering encounter | The player sees that magical law is contingent |
| VI. Ascendant | An emerging peer of higher powers | Self-authored transformation | The player begins to edit rather than obey reality |

### The emotional arc

**Strong Direction**

1. **Competence:** the player feels like an exceptional mage.
2. **Dominance:** the player defeats beings who once defined the limits of power.
3. **Disorientation:** familiar magical systems begin behaving like insufficient
   approximations.
4. **Recognition:** Truth and the higher powers respond to the player as a potential
   equal rather than a petitioner.
5. **Authorship:** progression becomes the creation of new possibilities, not the
   acquisition of permission.

Ascension should feel exhilarating, strange, and slightly lonely. The player gains
freedom while losing the comfort of believing that anyone else has the final answer.

## Cosmology

### The Low Order

**Strong Direction**

The Low Order is not a false world. It is reality as rendered under a restricted set of
rules: three spatial dimensions, forward time, stable identities, conserved forms, and
magic expressed through repeatable components.

Most beings mistake consistency for necessity.

### Gnosis

**Canon**

Gnosis is direct knowledge of the structures beneath magical law. It cannot be reduced
to information in a book. A fact can be taught; Gnosis must alter the knower.

Possible gameplay expressions include:

- perceiving hidden relations between blocks, entities, spells, or dimensions;
- treating spell components as editable principles;
- exchanging one category of property for another;
- occupying or projecting through impossible geometry;
- changing the conditions under which ordinary magic operates.

### Emanation

**Strong Direction**

Emanation describes how a higher-order principle becomes a lower-order phenomenon
without ceasing to exist in its greater form.

A higher power's visible body may be an emanation rather than the total being. An
artifact may be an emanation of a law. A spell may be an emanation of the caster's
will constrained into a pattern the world can accept.

### Aeons

**Strong Direction**

Aeons are higher-order intelligences, principles, or authoring powers. "Aeon" should
remain a category rather than a single species. Some may possess personality; others
may be closer to living laws.

The player does not simply become stronger than an Aeon. The player becomes capable of
entering the same kind of relationship with reality.

### Higher powers

**Canon**

Higher powers are not automatically gods, and gods are not automatically higher-order.
A god may dominate a region of the Low Order while remaining bound to identity,
causality, and form. A higher power may have little interest in worship yet possess
authority over the rules by which gods exist.

### The Unwritten

**Unbound Idea**

The Unwritten is the possibility-space from which new arrangements can be authored. It
is not emptiness, a dimension, or an afterlife. It is what reality has not yet agreed
to become.

## Truth

**Canon**

Truth is the entity that appears at the Tier 6 threshold. It waits with an outstretched
hand, recognizes a specific owner, accepts a Codex Arcana and Chimerite Manaweaver
Wand, and presides over the player's final step beyond ordinary progression.

Truth is rendered as an unnatural, full-bright presence whose late transformation uses
black-and-white digital noise and glitch dissolution.

### What Truth means

**Strong Direction**

Truth is not merely honesty, factual correctness, or a person with secret knowledge.
Truth is the condition in which the hidden editability of existence becomes
impossible to ignore.

Truth may be understood simultaneously as:

- a witness to beings approaching ascension;
- an emanation of a higher-order principle;
- a threshold that takes an anthropomorphic form for the player's benefit;
- reality recognizing that one of its inhabitants has learned to author it.

These interpretations may remain unresolved. Ambiguity is useful as long as Truth's
role is coherent.

### Behavior rules

**Strong Direction**

- Truth speaks rarely.
- Truth never begs, threatens, congratulates, or provides exposition.
- Truth behaves as though the player's arrival was both expected and statistically
  impossible.
- Truth should appear more stable when ignored and less stable when closely observed.
- Truth's transformations should make the renderer feel inadequate, not make Truth
  feel wounded.

## The Tesseract

**Canon**

MnAGnosis already contains tesseract item and block concepts rendered from rotating
four-dimensional vertices projected into three dimensions.

### Symbolic role

**Strong Direction**

The tesseract is the first honest object.

Ordinary objects conceal their deeper constraints by appearing stable. The tesseract
openly demonstrates that what the player sees is only a projection. Its shifting shape
is not mutation; it is incomplete perception.

The tesseract can symbolize:

- a higher-order object intersecting the Low Order;
- compressed space or impossible containment;
- the player's ability to perceive relations beyond ordinary geometry;
- the transition from using mana to editing the space in which mana behaves.

### Visual rule

The tesseract should never look like a decorative rotating cube. Its projection should
occasionally feel discontinuous: edges exchanging depth, silhouettes passing through
themselves, rotations accelerating without momentum, or static revealing a different
orientation for a single frame.

## The Pentad of Authorities

**Strong Direction**

Before Truth recognizes the player, the player must defeat all five faction bosses.
Together they form the Pentad of Authorities: the highest powers presented by the
established magical orders.

The requirement is not a scavenger checklist. Each victory refutes a different claim
about what ultimately governs a mage.

| Authority | Domain represented | Lesson carried into ascension |
|---|---|---|
| Council Warden | Structure, law, disciplined artifice | Systems are tools; no system is sovereign |
| Faerie Queen | Change, relation, glamour, living magic | Identity can remain meaningful without remaining fixed |
| Demon Lord | Will, appetite, force, dominion | Power without understanding remains captive to desire |
| Wither Lich | Persistence, death, memory, refusal | Survival is not transcendence, and continuity is not selfhood |
| Odin | Divinity, sacrifice, knowledge, kingship | Even a god who paid for wisdom is not the final authority |

These symbolic readings are strong direction, not restrictions on Mana and Artifice's
existing lore. MnAGnosis should add a higher-order interpretation without flattening
the bosses into allegorical tokens.

### Why all five

**Canon**

The player must stand beyond factional partiality. A single faction teaches one valid
way of arranging magic. Ascension requires proof that the player can understand,
survive, and overcome every major magical authority without being contained by any
one of them.

## The Broker

**Strong Direction**

The Broker is the sixth prerequisite, but not a sixth conquest.

Combat proves that the player can overcome authority. The Broker proves that the
player understands equivalence: the ability to recognize value, accept cost, and
participate in an exchange that cannot be reduced to force.

The required trade should feel singular and deliberate. It should not be satisfied by
buying an arbitrary common item.

### Desired meaning

- Something must be surrendered that reflects the player's completed magical life.
- The received object, mark, or permission should be useless to an ordinary mage.
- The Broker should recognize the trade as unusual without explaining its ultimate
  purpose.
- The exchange should make the player a counterparty, not a customer.

### Candidate forms

**Unbound Idea**

- Trade the five proofs of victory for a single impossible currency.
- Exchange a perfected spell or named artifact for a blank principle.
- Purchase "the space between tiers," represented by an item with no conventional
  description.
- Give the Broker something the player's history says cannot be replaced.

## The Two Offerings

**Canon**

Truth requires:

1. a **Codex Arcana**;
2. a **Chimerite Manaweaver Wand**.

### The Codex Arcana

**Strong Direction**

The Codex represents received knowledge: the accumulated grammar, discoveries, and
institutions through which the player learned magic.

Offering it does not reject learning. It demonstrates that the player can release the
authority of what has already been written.

### The Manaweaver Wand

**Strong Direction**

The wand represents enacted will: the player's ability to impose learned patterns on
mana and make knowledge operational.

Offering it demonstrates that the player is ready to act without depending on the
highest conventional instrument of their former state.

### The paired meaning

Book and wand are knowledge and agency, theory and act, pattern and imposition. Truth
requires both because ascension belongs neither to passive enlightenment nor to blind
power.

The player arrives with proof of everything a mage can know and everything a mage can
do. Truth takes both. What remains is the author.

## Sensory Language

### Palette

**Canon**

Black and white are the dominant colors of Gnosis.

They should not merely communicate morality or emptiness. They represent information
reduced to its most severe contrast: signal and absence, assertion and erasure.

| Use | Direction |
|---|---|
| White | Exposure, impossible luminosity, overcomplete information, Truth |
| Black | Omission, depth without texture, rejected state, the unreadable |
| Gray | Transition, uncertainty, projection error, ordinary matter losing authority |
| Rare accent color | A meaningful exception tied to a source, faction, or unstable law |

Accent colors should be rare enough that their appearance becomes an event.

### Static and digital noise

**Canon**

Static is reality failing to resolve a higher-order state. It should behave like
information, not smoke:

- horizontal scanline loss;
- high-frequency black/white grain;
- brief duplicated silhouettes;
- displaced slices;
- frame-local discontinuities;
- patterns that respond to gaze, distance, or progression.

Avoid generic rainbow glitch effects. MnAGnosis static is severe, monochrome, and
intentional.

### Shifting geometry

**Strong Direction**

- Forms may exchange inside and outside.
- Rotation may occur across planes the camera cannot represent.
- Objects may show incompatible projections in consecutive frames.
- Repetition should contain slight mathematical disagreement.
- Symmetry should become unsettling through near-perfection, not random deformation.

### Shaders

**Strong Direction**

Shaders should communicate a specific metaphysical failure:

| Effect | Meaning |
|---|---|
| Scanline dissolve | The current representation is being withdrawn |
| White full-bright surface | The object is not participating in ordinary lighting |
| Black/white noise aura | Multiple unresolved states are competing for display |
| Doppleganger or echo | Identity is no longer restricted to one rendered position |
| Depth or projection reversal | The observer's spatial assumptions are insufficient |
| UI corruption | The player's instruments are unable to describe the new state |

### Particles

Particles should favor ordered strangeness over volume:

- lines, planes, rings, grids, and orbiting points;
- particles that freeze when directly observed;
- emissions that move toward a source before being created;
- white ash and enchantment traces around Truth;
- sparse particles whose paths imply invisible geometry.

### Interface effects

**Exploring**

Late-game interface corruption can show the system failing to classify the player:

- tier numerals briefly replaced by symbols;
- an Oculus requirement that changes when not focused;
- text duplicated one pixel out of alignment;
- black bars that expose white static underneath;
- familiar progress indicators exceeding or escaping their frames.

All interface effects need accessibility toggles and must not prevent the player from
understanding required actions.

### Motion

- Use long stillness before sharp discontinuity.
- Prefer impossible interpolation to constant frantic movement.
- Let an object arrive at a pose without visibly passing through intervening poses.
- Make higher powers feel indifferent to gravity and momentum.
- Reserve camera disruption for rare threshold moments.

### Audio

**Strong Direction**

The soundscape should combine absence, precision, and damaged transmission:

- sudden removal of ambient sound;
- narrow-band static rather than constant broadband noise;
- reversed or time-smeared spell sounds;
- distant speech with no stable words;
- pure tones that form or break mathematical intervals;
- impacts followed by sound arriving in the wrong order;
- brief choir textures used as scale, not as a default marker of holiness.

Silence is one of the mod's strongest possible effects. Use it deliberately.

## Writing Voice

MnAGnosis language should be concise, declarative, and slightly impossible.

### Preferred qualities

- Speak as if strange statements are obvious facts.
- Imply scale through restraint.
- Use mathematical or architectural language beside mystical language.
- Let the player infer the connection between a mechanic and its meaning.
- Address the player as a becoming subject, not a chosen hero.

### Avoid

- lengthy exposition during encounters;
- constant use of "eldritch," "ancient," or "beyond comprehension";
- treating insanity as the automatic result of forbidden knowledge;
- random corrupted characters that do not communicate anything;
- declaring the world fake or meaningless;
- reducing historical Gnosticism to an aesthetic collage.

### Naming patterns

Good names should resemble one of these structures:

- a plain absolute: **Truth**, **Measure**, **Interval**, **Witness**;
- a technical impossibility: **Unbounded Lattice**, **Negative Axis**;
- an act of authorship: **Revision**, **Sever the Constant**, **Name the Empty**;
- a restrained title: **The First Emanation**, **The Unheld Shape**.

## Fragment Bank

These are tone references and candidate lines, not all canon dialogue.

### Canon or near-canon

> Truth waits with an outstretched hand.

> You have advanced beyond comprehension. The fabric of the universe is now yours to
> weave.

### Strong direction

> Five authorities named themselves final. You learned to count beyond them.

> A god is a law that learned to speak.

> You did not break the world. You discovered where it bends.

> The shape is not changing. Your answer is.

> The Broker does not sell truth. The Broker sells the right to pay its cost.

> Bring what taught you. Bring what obeyed you. Leave as neither student nor master.

> Matter is a habit with excellent memory.

> The sixth tier is not above the fifth. It is outside the question.

> You have mistaken consistency for necessity.

> Nothing here is hidden. You have only been looking from within it.

### Advancement or objective candidates

| Candidate | Possible use | Maturity |
|---|---|---|
| **A Closed Pantheon** | Defeat all five authorities | Unbound Idea |
| **Terms Accepted** | Complete the Broker's threshold exchange | Unbound Idea |
| **The Outstretched Hand** | Encounter Truth | Unbound Idea |
| **Knowledge and Agency** | Offer both required items | Unbound Idea |
| **Outside the Question** | Reach Tier 6 | Unbound Idea |
| **First Revision** | Perform the first reality-authoring act | Unbound Idea |

## Terminology

| Term | Working definition | Maturity |
|---|---|---|
| Gnosis | Transformative knowledge of the editable structures beneath magical law | Canon |
| Low Order | Ordinary reality experienced under stable, limited rules | Strong Direction |
| Ascendant | A being beginning to author rather than merely obey reality | Canon |
| Aeon | A higher-order intelligence, principle, or authoring power | Strong Direction |
| Emanation | A lower-order expression of a greater being or principle | Strong Direction |
| Truth | The Tier 6 threshold entity and possible emanation of revelation itself | Canon |
| Pentad of Authorities | The five faction bosses considered as the completed limits of established magic | Strong Direction |
| Counterparty | A being recognized as capable of meaningful higher exchange | Strong Direction |
| Authoring | Deliberate revision of the rules or relationships underlying phenomena | Strong Direction |
| Projection | The incomplete lower-dimensional appearance of a higher-order form | Strong Direction |
| Unwritten | Possibility not yet committed to a stable reality | Unbound Idea |
| Revision | A bounded act that changes a rule rather than an object alone | Unbound Idea |

## Idea Garden

Nothing in this section is committed. Ideas remain here until promoted through a
design decision.

### Entities

- **The Measure:** an intelligence that can describe anything except the player after
  ascension.
- **The Interval:** a presence visible only between animation frames or server ticks.
- **Witnesses:** non-hostile shapes that appear after major reality revisions and
  silently compare the before and after states.
- **The Redactor:** an entity that removes properties rather than dealing damage.

### Spaces

- A room whose geometry changes according to the spell held in the player's hand.
- A white void containing black silhouettes of structures that have not been built.
- A higher-order observatory where dimensions appear as neighboring surfaces.
- A corridor in which every doorway returns to the same location with one rule changed.

### Artifacts

- **The Blank Principle:** an ingredient that acquires a property only when used.
- **Unbounded Lattice:** a tesseract-derived focus for spatial or relational spells.
- **The Broker's Remainder:** what remains after an exchange that was mathematically
  exact but metaphysically incomplete.
- **Negative Crown:** regalia that removes classifications from the wearer.

### Spells or revisions

- Exchange distance with duration.
- Make two positions adjacent for one action.
- Remove a target's relationship to gravity.
- Cause a spell to remember a previous target.
- Replace collision with observation: a block is solid only while watched.
- Temporarily treat an entity's name, faction, or owner as an editable component.

### Spell components and modifiers

- **Exchange of Stations:** swap the player with an entity in line of sight,
  preserving identity, health, inventory, and effects while exchanging position and
  facing.
- **Causal Bookmark:** mark an entity's position, dimension, rotation, and safe return
  space, then recall that entity to the recorded location later. A stronger state-based
  revision may also restore velocity and other selected properties without restoring
  health or inventory.
- **Axiom of Harm:** a damage component modifier that removes the target's relevant
  resistance or immunity while preserving the component's original damage type; fire
  can harm Blazes, drowning can affect undead, and physical damage can strike phased
  entities.
- **Gravity Collapse:** a gravity component that creates an inward pull toward a
  selected point, gathering mobs, items, projectiles, or other valid entities.
- **Gravity Convergence:** a controllable gravity-field component with attract and
  repel polarity, allowing mobs to be drawn toward a point or expelled from it.
- **Singularity:** manifest a block-absorbing gravity sphere that pulls nearby terrain
  into a dense celestial mass. With the **Precision** modifier, the caster may choose
  between true collection, which removes and gathers blocks, and projection, which
  creates a visual clone of the terrain while leaving the source blocks in place.
- **Thaumaturgic Link Biome Replacement:** use a thaumaturgic link to identify a source
  biome and replace the biome identity of a linked destination, potentially changing
  climate, weather, vegetation, ambient effects, and mob rules without requiring a
  conventional world-generation event.
- **Emanate Creature:** use a crystallized mob or phylactery mob as a reusable pattern
  source without consuming the stored creature, then summon emanations of it in
  alternate shapes such as an avatar, swarm, guardian, projectile, or temporary body.
- **Population Edit:** rewrite one mob type into another across a selected target or
  population, preserving or deliberately reauthoring selected traits such as size,
  equipment, health, and allegiance.
- **Predatory Principle:** impose a new behavioral law on normally peaceful mobs,
  turning them into hostile allies, coordinated hunters, or creatures that attack
  according to a chosen rule rather than their ordinary AI.
- **True Self:** create an autonomous self-manifestation that uses the player's
  available spells, equipment, and selected abilities, but acts through its own AI
  instead of copying the player's exact movement and actions like a simulacrum or
  decoy. The manifestation may choose targets, spells, positioning, and tactics
  independently while remaining recognizably the player's authored counterpart.
- **The Reassembled Land:** let the player define a shape or choose from authored
  templates such as bridges, pyramids, pillars, walls, floors, and stairs, then pull
  surrounding terrain from a configurable radius and depth into that shape. The land
  is not simply placed from an item; nearby matter is coerced into the player's chosen
  geometry.
- **Living Land:** a terrain-aware combat component that makes the environment attack
  a selected creature. In a cave, nearby walls can fire thin one-block pillars that
  launch toward and pummel the target; on open ground, the floor can rise and snap at
  the target; beneath a low ceiling, the ceiling can slam downward and crush it. The
  component chooses or allows the player to select different attack modes based on
  the available terrain, with each mode changing the surrounding land as part of the
  attack.
- **Gravitational Down:** mark an area by radius, depth, shape, and orientation, then
  redefine its local gravitational down. Players and mobs can walk across walls and
  ceilings as though those surfaces were floors, while the boundary determines how
  entities enter, exit, fall, and reorient within the field.
- **Worldline (Shape):** follow, record, or replay a path defined by a caster, entity,
  projectile, or previous spell effect. Components applied through the shape can travel
  along that path, leave a persistent trail, or retrace the path later.
  - **Delay** controls when recording, movement, or replay begins.
  - **Duration** controls how long the worldline records, remains active, or continues
    replaying.
  - **Range** controls the maximum spatial length or distance the worldline may travel
    from its source.
  - **Radius** may control the thickness of the path, while **Speed** may control the
    rate of traversal or replay and **Precision** may control path fidelity.
- **Stamp (Shape):** apply a component inside a geometric template selected by the
  player, such as a bridge, pyramid, pillar, wall, floor, staircase, dome, or saved
  custom form. The stamp defines the arrangement of the effect, while existing radius,
  height, and width attributes define its overall bounds.
  - **Subshape (Modifier):** determines the internal template or sub-shape used by the
    stamp, allowing the player to choose, rotate, mirror, or author the specific form
    without changing the spell's component.
  - **Precision** may determine whether the stamp is a true terrain operation or a
    projected preview, while **Magnitude** may determine how much surrounding matter
    can be coerced into the selected form.
- **Orbit (Shape):** cause a component, its manifestations, or its affected targets to
  circle a selected entity or point. **Radius** controls orbital distance, **Speed**
  controls angular velocity, **Magnitude** controls the number of orbiting instances,
  and **Duration** controls how long the orbit persists.
- **Proxy (Shape):** select an entity, block, simulacrum, projectile, or thaumaturgic
  link as the spell's point of origin, allowing the component to be delivered from that
  proxy rather than the caster. **Range** controls proxy selection distance,
  **Duration** controls how long the proxy remains available, and **Precision**
  determines whether the spell uses the proxy's facing or the caster's facing.
- **Aspect Externalization:** extract an intangible property from a target and manifest
  it as a physical magical object or entity that can be moved, stored, destroyed,
  exchanged, or applied elsewhere. Candidate aspects include immunity, aggression,
  gravity, poison, enchantment, allegiance, and other bounded properties.
- **True Damage (Damage Component):** a prohibitively expensive Ineffable spell
  component with its own damage type that bypasses ordinary armor, resistance effects,
  damage reduction, and innate damage-type immunity. Unlike **Axiom of Harm**, it does
  not preserve or revise another component's damage category; it authors harm directly.
  The **Damage** modifier controls its output through sharply escalating mana costs,
  keeping even modest increases consequential. Its visual language is a severe eruption
  of black and white cubes that assemble around, intersect, and then withdraw from the
  target, accompanied by brief monochrome television-static particles, frame-local
  visual breakup, and a clipped burst of narrow-band static instead of a conventional
  elemental impact. Absolute administrative or encounter protections may remain
  outside its scope.

---

# Book II - The Work

> The following is not prophecy. It is a list of revisions.

## Current-State Snapshot

This snapshot distinguishes repository evidence from future intent. It is not a claim
that every feature is release-ready.

### Platform and integration

| Area | Status | Current state |
|---|---|---|
| Minecraft target | **Implemented** | Forge-based Minecraft 1.20.1 project |
| Mana and Artifice integration | **Implemented** | Direct addon dependency with compatibility Mixins and data resources |
| GeckoLib rendering | **Implemented** | Used for the Truth entity model and animation |
| JEI and Curios environment | **Implemented** | Declared as project dependencies |

### Progression

| Feature | Status | Current state |
|---|---|---|
| Tier 6 storage and clamp | **Implemented** | MnAGnosis raises the Mana and Artifice terminal tier to 6 |
| Tier 6 Oculus handling | **Implemented** | Client compatibility treats Tier 6 as the terminal tier |
| Odin Tier 5 requirement | **Implemented** | Progression data references the existing Odin-defeat advancement |
| Faction advancement interception | **In Progress** | Faction completion paths are being adapted around the Truth threshold |
| All-five-boss requirement | **Designed** | Approved future direction; not yet the complete gate |
| Broker exchange requirement | **Designed** | Approved future direction; exact trade and tracking remain open |

### Truth encounter

| Feature | Status | Current state |
|---|---|---|
| Owned Truth encounter | **Implemented** | A player-bound Truth entity can be summoned or replaced |
| Codex offering | **Implemented** | Truth accepts one Codex Arcana |
| Wand offering | **Implemented** | Truth accepts one Chimerite Manaweaver Wand |
| Offering preservation | **Implemented** | Item data is copied and stored; incomplete encounters refund offerings |
| Tier 6 completion | **Implemented** | Completing both offerings advances an eligible player |
| Finale sequence | **Implemented** | Timed animation, particles, grin/flame phases, and eventual discard |
| Monochrome glitch dissolution | **Implemented** | A dedicated shader uses scanline noise and black/white output |

### Tesseract and visuals

| Feature | Status | Current state |
|---|---|---|
| Tesseract item | **Implemented** | Custom item renderer projects rotating 4D vertices into 3D |
| Tesseract block | **Implemented** | Block entity renderer provides animated projected geometry |
| Tesseract visual alignment | **Exploring** | Current cyan/blue presentation can be revised toward the monochrome Gnosis language |
| Noise shader | **Implemented** | Core shader resources exist |
| Doppleganger shader | **Implemented** | Core shader resources exist |
| Truth glitch shader | **Implemented** | Core shader resources exist and support the finale |

### Existing content seeds

- Primal mana items and armor provide a material direction that can be integrated more
  deliberately with ascension.
- Several shader and render helpers already establish a foundation for shared effects.
- Truth's current system messages provide the beginning of the mod's writing voice.
- The current progression architecture favors narrow compatibility changes instead of
  replacing Mana and Artifice systems wholesale.

## Target Ascension Flow

### Desired player flow

1. Reach Mana and Artifice Tier 5.
2. Defeat the Council Warden.
3. Defeat the Faerie Queen.
4. Defeat the Demon Lord.
5. Defeat the Wither Lich.
6. Defeat Odin.
7. Complete the unique Broker exchange.
8. Perform the player's normal faction culmination action.
9. Summon Truth instead of immediately receiving Tier 6.
10. Offer a Codex Arcana and Chimerite Manaweaver Wand.
11. Witness the finale and advance to Tier 6.
12. Begin post-ascension progression based on authorship rather than ordinary tiering.

The order of the five boss victories should remain flexible unless later testing shows
that a fixed order produces a substantially better experience.

### Prerequisite matrix

| Prerequisite | Proof concept | Current status | Design need |
|---|---|---|---|
| Council Warden defeated | Council authority overcome | **Designed** | Select reliable advancement or trigger |
| Faerie Queen defeated | Fey authority overcome | **Designed** | Select reliable advancement or trigger |
| Demon Lord defeated | Demon authority overcome | **Designed** | Select reliable advancement or trigger |
| Wither Lich defeated | Undead authority overcome | **Designed** | Select reliable advancement or trigger |
| Odin defeated | Divine authority overcome | **Implemented** | Fold existing condition into combined gate |
| Broker exchange completed | Higher equivalence recognized | **Designed** | Define trade, trigger, persistence, and retroactive behavior |
| Faction culmination performed | Player invokes their mastered path | **In Progress** | Keep all faction paths semantically equivalent |
| Two offerings accepted | Knowledge and agency surrendered | **Implemented** | Preserve current item data and refund safety |

### Gate behavior requirements

**Designed**

- Boss proofs persist per player.
- Existing qualified saves should receive retroactive credit where the source mod
  exposes reliable advancements.
- The Oculus should communicate missing requirements without spoiling their full
  metaphysical meaning.
- A player may complete the five victories in any order.
- The Broker requirement must be specific enough to avoid accidental completion.
- Repeating a boss or trade cannot corrupt or duplicate progress.
- Tier 6 remains terminal until a separate post-ascension system is explicitly
  designed.

## Technical Design Principles

### Compatibility first

Extend Mana and Artifice through narrow, intentional hooks. Avoid copying or replacing
large upstream methods when a redirect, argument modification, advancement condition,
event, or data resource can express the change.

### Server authority

Progression proofs, offerings, ascension eligibility, and reality-changing mechanics
must be decided server-side. Clients may visualize state but must not grant it.

### Data before hardcoding

Prefer advancements, tags, recipes, or configuration-driven requirement lists when
they can represent a rule cleanly. Code should own behavior that cannot be expressed
reliably through data.

### Meaningful spectacle

Effects must correspond to state transitions. Expensive shaders, particles, camera
changes, and sound suppression should be concentrated at moments with mechanical or
lore significance.

### Accessibility and performance

Provide reduced-glitch, reduced-camera, and lower-effect options before shader-heavy
features become widespread. Critical objectives must remain readable without visual
distortion.

### Fail clearly

Compatibility hooks should fail during development when upstream signatures change.
Progression should never fail silently and leave a player unable to understand which
requirement is missing.

### Preserve the base mod's value

Late-game additions should reinterpret and culminate Mana and Artifice systems rather
than making its factions, spells, rituals, or artifacts irrelevant.

## Feature Families

| Family | Purpose | Current horizon |
|---|---|---|
| Ascension progression | Prove total mastery and cross the Tier 6 threshold | Now / Next |
| Truth encounters | Give thresholds an embodied but ambiguous witness | Now |
| Higher-order artifacts | Let players manipulate relations rather than raw statistics | Later |
| Reality revisions | Create bounded, legible rule-changing magic | Later |
| Impossible geometry | Make abstraction spatial and interactive | Next / Later |
| Shader language | Reuse consistent static, projection, echo, and withdrawal effects | Next |
| Interface instability | Show ordinary instruments failing to classify ascension | Later |
| Higher powers | Introduce peers, rivals, principles, and authoring intelligences | Dream State |
| Post-Tier 6 progression | Replace linear tiers with choices of authorship | Later |
| Environmental storytelling | Seed the world with evidence of prior revisions | Later |

## Feature Cards

### Card: Pentad Proof Ledger

| Field | Value |
|---|---|
| Status | **Designed** |
| Purpose | Track defeat of all five faction authorities per player |
| Player experience | Victories across every magical tradition gradually complete the final Oculus gate |
| Lore connection | The player moves beyond factional partiality and closes the established pantheon |
| Dependencies | Mana and Artifice boss triggers or advancements; player persistence; Oculus presentation |
| Technical direction | Prefer advancement-backed proofs with server-side aggregation and retroactive checks |
| Acceptance criteria | All five bosses are required; order is flexible; proofs persist; repeat kills are harmless; existing advancements can restore credit |

### Card: The Broker's Threshold Exchange

| Field | Value |
|---|---|
| Status | **Designed** |
| Purpose | Add a noncombat proof of equivalence before Truth appears |
| Player experience | The Broker offers one exceptional trade whose meaning becomes clear only at the threshold |
| Lore connection | The player is recognized as a counterparty rather than a customer |
| Dependencies | Broker trade-selection event or interaction hook; unique input/output; player persistence |
| Technical direction | Inject or select the trade through the source mod's Broker trade event when possible; record completion server-side |
| Acceptance criteria | Only the intended exchange grants credit; it cannot be granted by ordinary trades; credit survives reload; repeated trades do not duplicate progression |

### Card: Combined Tier 6 Requirement

| Field | Value |
|---|---|
| Status | **Designed** |
| Purpose | Replace the Odin-only requirement with the Pentad plus Broker proof |
| Player experience | The Oculus becomes a final ledger of mastered authorities and one unresolved exchange |
| Lore connection | No single faction or god can certify ascension |
| Dependencies | Pentad ledger; Broker proof; existing Tier 6 compatibility layer |
| Technical direction | Expose a single aggregate readiness condition while retaining inspectable component proofs |
| Acceptance criteria | Truth cannot be summoned early; all faction culmination paths use the same readiness rule; missing requirements are diagnosable |

### Card: Truth Finale

| Field | Value |
|---|---|
| Status | **Implemented** |
| Purpose | Convert the final offering into a visible metaphysical threshold |
| Player experience | Truth changes expression, produces flame and particles, then dissolves through black-and-white scanlines |
| Lore connection | The rendered form is withdrawn as the player crosses beyond its lesson |
| Dependencies | Truth entity data; GeckoLib animation; custom render layers; truth glitch shader |
| Technical direction | Preserve server-owned timing and client-only presentation |
| Acceptance criteria | Finale runs once; Tier 6 is granted only to an eligible owner; entity expires cleanly; no residual aura remains |

### Card: Monochrome Tesseract Revision

| Field | Value |
|---|---|
| Status | **Exploring** |
| Purpose | Bring the existing tesseract into the Gnosis visual system |
| Player experience | The projection shifts between precise wireframe, black absence, and white static discontinuity |
| Lore connection | The tesseract is a higher-order object that the renderer can only approximate |
| Dependencies | Shared 4D projection utility; item and block renderers; optional shader render type |
| Technical direction | Consolidate duplicated projection logic before adding new effects |
| Acceptance criteria | Item and block agree visually; no debug output; motion is deterministic enough for multiplayer presentation; reduced-effects mode remains readable |

### Card: Shared Gnosis Effect Library

| Field | Value |
|---|---|
| Status | **Exploring** |
| Purpose | Prevent every late-game feature from inventing an unrelated glitch style |
| Player experience | Static, echoes, dissolves, and projection failures form a recognizable visual language |
| Lore connection | Each effect communicates a particular failure of lower-order representation |
| Dependencies | Shader registry; render types; configuration; performance testing |
| Technical direction | Define reusable effect parameters rather than one shader per object when practical |
| Acceptance criteria | Effects have named meanings; intensity can be configured; unsupported hardware receives a clean fallback |

### Card: Ascendant State

| Field | Value |
|---|---|
| Status | **Exploring** |
| Purpose | Give Tier 6 a new mode of progression rather than ordinary stat inflation |
| Player experience | The player chooses bounded laws or relationships they can author |
| Lore connection | The player becomes an emerging peer of higher powers |
| Dependencies | Stable Tier 6 gate; save model; balance framework; authoring mechanic vocabulary |
| Technical direction | Favor unlockable verbs and tradeoffs over a seventh numeric tier |
| Acceptance criteria | Choices alter play style; base Mana and Artifice remains useful; power has explicit scope and counterplay |

## Roadmap

### Now

1. Stabilize the existing Tier 6 and Truth path.
2. Finish faction culmination compatibility work.
3. Remove renderer debug behavior and verify the existing tesseract paths.
4. Treat this bible as the source of truth for lore and status.
5. Add tests around current eligibility, offering safety, terminal-tier behavior, and
   encounter replacement.

### Next

1. Design the persistent Pentad proof model.
2. Verify the source mod's advancements or triggers for all five bosses.
3. Define the Broker's unique exchange and technical hook.
4. Replace the Odin-only aggregate gate with the Pentad plus Broker requirement.
5. Update Oculus communication and retroactive credit behavior.
6. Consolidate shared tesseract projection code and align its palette with Gnosis.

### Later

1. Design post-Tier 6 progression around authoring choices.
2. Create the first higher-order artifact with one clear relational verb.
3. Build an impossible-geometry encounter or structure.
4. Introduce controlled interface instability with accessibility settings.
5. Expand the shared shader and audio language.
6. Seed environmental evidence of prior ascendants or failed revisions.

### Dream State

- Reality revisions that alter bounded world rules without destroying multiplayer
  legibility.
- Encounters with Aeons who treat the player as a novice peer.
- Spaces represented through changing projections instead of conventional rooms.
- Spells that edit ownership, adjacency, duration, identity, or causality.
- A prestige structure that changes the ontology of the player rather than resetting
  numbers for another climb.
- Server-wide threshold events in which every player briefly sees the same impossible
  state from a different projection.

## Decision Log

| Date | Decision | Reason |
|---|---|---|
| 2026-07-25 | Use a hybrid living bible | Lore and technical direction need one shared source without being mixed together |
| 2026-07-25 | Divide the bible into Book I and Book II | Flavor can remain atmospheric while production information stays actionable |
| 2026-07-25 | Use selected Gnostic terms in an original cosmology | Preserve thematic resonance without directly reproducing a historical system |
| 2026-07-25 | Reality is reshapeable, not false | The player transcends scale and constraint rather than escaping a meaningless illusion |
| 2026-07-25 | The player becomes an emerging peer of higher powers | Ascension is a categorical transformation, not only a stronger mortal state |
| 2026-07-25 | Preserve Odin, Truth, and the two offerings as canon | The existing implementation already expresses the desired late-game threshold |
| 2026-07-25 | Require all five faction bosses in the target state | Ascension must demonstrate mastery beyond any single magical authority |
| 2026-07-25 | Require a meaningful Broker exchange | The threshold needs a proof of equivalence and understanding that combat cannot provide |
| 2026-07-25 | Keep black and white as the dominant visual language | Severe contrast supports Truth, abstraction, static, and representational failure |
| 2026-07-25 | Maintain the bible as Markdown | The document should live beside the code and remain easy to revise and diff |

## Open Questions

These are intentional design questions, not missing text.

### Progression questions

1. Which exact Mana and Artifice advancements or triggers reliably prove each boss
   victory?
2. Should the Oculus show all six prerequisites immediately, reveal them gradually, or
   present them through a single cryptic aggregate?
3. Can existing saves receive retroactive Broker credit, or must the threshold trade
   always occur after installing MnAGnosis?
4. Does the normal faction culmination action remain necessary after the player has
   already defeated every faction boss?

### Broker questions

1. What is valuable enough to make the threshold exchange meaningful without creating
   irreversible regret?
2. Does the player receive an item, a mark, an advancement, or an apparently empty
   result?
3. Should the exchange consume boss trophies, merely verify them, or remain independent
   of the Pentad proofs?

### Truth questions

1. Is Truth a singular being, a repeated emanation, or an event wearing a body?
2. Does every player encounter a private Truth, or can other players witness the
   threshold?
3. What survives from the current offering items after ascension, if anything?
4. Should Truth return after Tier 6 for later acts of authorship?

### Post-ascension play

1. What is the first mechanic that proves Tier 6 is categorically different?
2. How can reality editing remain useful in multiplayer without becoming unrestricted
   griefing or bypassing every challenge?
3. Are authoring powers learned, bargained for, discovered, or invented?
4. What costs preserve meaning once ordinary resources feel small?

### Presentation

1. Which effects require shader fallbacks?
2. How should reduced-glitch mode preserve metaphysical meaning?
3. What rare accent color, if any, belongs specifically to the player after ascension?
4. How much interface corruption remains compelling before it obstructs play?

## Reusable Entry Templates

### Lore entry

```markdown
### [Name]

**Maturity:** **Canon** / **Strong Direction** / **Unbound Idea**

**Plain statement:** One sentence describing what this is.

**Meaning:** What it expresses within the themes of MnAGnosis.

**Player contact:** How the player encounters or learns about it.

**Sensory language:** Shape, motion, palette, shader, particle, and audio direction.

**Boundaries:** What this concept must not become.

**Related entries:** Links to connected sections.
```

### Feature card

```markdown
### Card: [Feature name]

| Field | Value |
|---|---|
| Status | **Implemented** / **In Progress** / **Designed** / **Exploring** / **Deferred** |
| Purpose | Why the feature exists |
| Player experience | What the player sees and does |
| Lore connection | What the mechanic means |
| Dependencies | Systems or assets required first |
| Technical direction | Preferred implementation boundary |
| Acceptance criteria | Observable conditions for completion |
```

### Decision record

```markdown
| YYYY-MM-DD | Decision | Reason |
```

### Open question

```markdown
1. **Question:** The exact choice that remains open.
   - **Why it matters:** The player, lore, or technical consequence.
   - **Decision trigger:** What must be learned before choosing.
```

## Revision History

| Date | Revision |
|---|---|
| 2026-07-25 | Created the two-book living bible from the approved lore and technical design |
