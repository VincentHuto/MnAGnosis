# MnAGnosis Lore and Design Bible - Document Design

## Goal

Create a polished, maintainable Markdown document that defines MnAGnosis as a late-game
addon to Mana and Artifice for Minecraft 1.20.1. The document must serve two purposes:
preserve the mod's lore and creative identity, and track its current implementation,
future systems, and production decisions.

The document is a living creative bible, not a complete implementation specification
for every future feature.

## Creative Foundation

MnAGnosis treats reality as real but low-order: an arrangement that appears absolute
only to beings without the knowledge or power to edit it. Ascension does not reveal
that the world is false. It reveals that matter, magic, identity, and natural law are
contingent and reshapeable.

The player progresses from accomplished mage to god-slayer, then to a perceiver of
deeper laws, and finally toward becoming an emerging peer of the higher powers that
author or manipulate reality.

The setting will use selected historical Gnostic terms, including Gnosis, Aeons, and
emanation, inside an otherwise original cosmology. It will not directly reproduce a
historical Gnostic system.

## Canonical Late-Game Foundation

The existing Tier 6 flow is the current canon foundation:

- Odin is a late-game gate because defeating him demonstrates that the player can
  overcome a god.
- A qualified Tier 5 player summons Truth rather than ascending immediately.
- Truth requests a Codex Arcana and a Chimerite Manaweaver Wand as offerings.
- The player then advances beyond ordinary Mana and Artifice progression.
- Truth, the animated tesseract, monochrome rendering, noise, scanlines, and glitch
  dissolution establish the current visual vocabulary.

The future target expands the gate into the Pentad of Authorities. The player must
defeat the Council Warden, Faerie Queen, Demon Lord, Wither Lich, and Odin. The player
must also complete a meaningful exchange with the Broker. The victories establish
mastery over the factions' highest authorities; the Broker establishes understanding
of exchange, equivalence, and knowledge that cannot be seized by force.

## Document Architecture

The Markdown document will be a single volume divided into two clearly separated books.

### Book I - Gnosis: Lore and Creative Identity

Book I is the imaginative source of truth. It will contain:

1. A concise creative manifesto and core thesis.
2. The player's ascension arc.
3. The original cosmology and carefully selected Gnostic vocabulary.
4. Truth, the Tesseract, higher powers, abstraction, emanation, and ascension.
5. The Pentad of Authorities and the Broker's symbolic roles.
6. The meaning of the Codex Arcana and Chimerite Manaweaver Wand offerings.
7. Visual, shader, animation, particle, interface, and audio language.
8. Short in-world fragments suitable for tooltips, advancements, encounters, and
   environmental storytelling.
9. A terminology register.
10. An idea garden for unexplored entities, artifacts, spaces, and phenomena.

Lore entries will use three explicit maturity labels:

- **Canon:** accepted creative truth or established implementation.
- **Strong Direction:** intended direction that may still change in execution.
- **Unbound Idea:** deliberately speculative material.

### Book II - The Work: Technical and Future-State Design

Book II is the production reference. It will contain:

1. A current-state inventory covering Tier 6, Truth, offerings, the tesseract, shaders,
   and existing progression integration.
2. The target ascension flow and its prerequisite matrix.
3. Planned feature families: progression, encounters, artifacts, spells, world
   effects, interface corruption, shaders, particles, audio, and advancements.
4. Reusable feature cards with purpose, player experience, lore connection,
   dependencies, technical notes, and acceptance criteria.
5. A roadmap organized as Now, Next, Later, and Dream State.
6. A decision log, unresolved design questions, and revision history.

Production entries will use five status labels:

- **Implemented**
- **In Progress**
- **Designed**
- **Exploring**
- **Deferred**

## Initial Content Scope

The initial document will be substantive enough to guide future brainstorming without
pretending that unchosen mechanics are settled. It will fully articulate the core
thesis, progression arc, visual language, known Tier 6 flow, Pentad/Broker direction,
and organizing frameworks. Future entities, spells, biomes, structures, and prestige
systems will begin as bounded concept seeds or feature-card examples.

The document will distinguish:

- what exists in the repository now;
- what the creator has explicitly approved as future direction;
- what is newly proposed for exploration.

## Markdown Presentation

The document will express the MnAGnosis identity through Markdown-native structure:

- a linked table of contents;
- restrained monochrome symbols and text dividers;
- short atmospheric epigraphs;
- consistent heading hierarchy;
- blockquotes for in-world fragments;
- compact tables for status, progression, and feature tracking;
- reusable entry and feature-card templates;
- strong separation between Book I and Book II.

Decorative marks must remain plain-text friendly and readable in raw Markdown as well
as rendered views. The document will not depend on embedded HTML, external stylesheets,
or images to communicate its structure.

## Maintainability

The document will use consistent Markdown headings, compact tables, status markers,
and reusable entry patterns. A linked table of contents will make the two books easy
to scan. The final structure must support future editing without requiring the entire
document to be redesigned.

## Verification and Delivery

The final Markdown file will be checked for coherent heading levels, valid internal
links, readable tables, consistent status labels, repository accuracy, placeholder
text, contradictions, and accidental claims that speculative ideas are implemented.

Only the final Markdown source file will be delivered unless additional formats are
requested.

## Boundaries

- This task creates the living lore/design document; it does not implement new mod
  mechanics.
- The existing dirty worktree changes are not part of this task and will not be
  modified.
- Exact mechanics for tracking all five boss victories and the Broker exchange remain
  future feature-design work. The document will record their intended role without
  inventing a technical commitment.
