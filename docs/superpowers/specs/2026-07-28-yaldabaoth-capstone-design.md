# Yaldabaoth Capstone Design

## Status and Role

**Lore maturity:** Canon
**Production status:** Designed
**Encounter role:** Post-mastery final boss of MnAGnosis

Yaldabaoth is the Blind First Author: the genuine author of much of the Low
Order who mistakes one successful arrangement for the only permissible
reality. The world is real, not a lie. His failure is his claim that no other
authorship may exist.

The player confronts him only after achieving Authorship in Relation,
Definition, and Continuance. Defeating him ends his authority without adding
Tier 7. Victory gives the player the substrate and permission state for a first
personalized Original Work.

The target first-clear duration is 12–15 minutes for a prepared solo player.
Practiced projection rematches should settle near 8–10 minutes.

## Historical Grounding

The encounter draws on the lion-faced serpent, fiery eyes, borrowed light,
layered heavens, subordinate rulers, and the Chief Ruler's boast that no other
god exists. Primary design references:

- [The Secret Book of John](https://www.gnosis.org/naghamm/apocjn-long.html)
- [On the Origin of the World](https://www.gnosis.org/naghamm/origin-Barnstone.html)
- [The Reality of the Rulers](https://www.gnosis.org/naghamm/hypostas.html)

The paired Sun and crescent Moon are an original MnAGnosis symbol. They
represent Yaldabaoth's counterfeit binary: the assertion that every possible
state must resolve into one of two answers he controls. They must not be
presented as an attested historical pair.

## Progression to the First Firmament

Each post-Tier-6 Manuscript supplies one non-craftable proof of mastery:

- Relation grants the **Exception of Distance**.
- Definition grants the **Exception of Identity**.
- Continuance grants the **Exception of Sequence**.

The Broker accepts all three in a singular exchange and returns the **Writ of
No Authority**, a permission declaring that its bearer requires no permission.
Inserting the Writ into a Tesseract at the Unwritten Observatory opens the
First Firmament.

The portal has a short opt-in window. The Writ bearer owns the attempt; other
players join voluntarily before the roster locks. Nobody is transported
automatically. The Writ is never consumed by a failed attempt.

## First Firmament

The First Firmament is a static MnAGnosis dimension containing widely
separated reusable arena cells. Each cell is a circular black plane surrounded
by seven thin heaven-rings. An encounter controller allocates and restores
cells, owns the participant roster, persists encounter state, and distributes
rewards.

The original Yaldabaoth uses muted gold, bone, ember, and storm-dark accents.
His body is one authoritative entity with multipart serpent hitboxes bound to
named model segments. Sun and Moon are separate controller-owned entities.

Qualified participants receive three server-validated, arena-only actions:

- **Relation** links two compatible marked subjects.
- **Definition** changes one highlighted combat property.
- **Continuance** captures and honestly replays one declared event.

These actions create transitions and large advantages. They do not replace
ordinary weapons, spell building, movement, or sustained combat.

## Act I — The Counterfeit Sky

Yaldabaoth coils outside the arena while Sun and Moon orbit in opposite
directions.

### Sun: Exposure

The Sun is brilliant white with a black outline. It draws geometric damage
lanes, fires a tracking judgment beam, and amplifies damage to anything caught
in its light, including the Moon and Yaldabaoth's passing segments.

### Moon: Omission

The Moon is a black crescent with a white outline. Its moving shadow erases
projectiles, conceals safe paths until players enter the white outline, and
temporarily removes marked floor sectors.

The celestials have separate health and stagger meters. Excessive focus on one
intensifies the other, encouraging target alternation without requiring
simultaneous attackers. Yaldabaoth interferes with tail sweeps, lion roars that
reverse knockback direction, and declared Edicts.

At the required stagger thresholds, the player:

1. Relates Sun and Moon.
2. Redefines the Sun's beam to affect its counterpart.
3. Continues that beam through their orbital crossing.

The eclipse breaks the false binary. Both celestials collapse into dormant
orbiting sigils instead of dying. Yaldabaoth enters bodily and declares:

> WHAT I HAVE MADE CANNOT EXCEED ME.

## Act II — The Law That Calls Itself God

Damage to ordinary body segments remains useful but reduced. Correct authored
counters expose contradiction segments that transfer heavy damage to the
shared boss health.

The three Claims enter at approximately 100–70%, 70–40%, and 40–10% health.
Once introduced, earlier Claims remain available for increasingly demanding
but readable combinations.

### Claim I: There Is No Outside

Yaldabaoth coils into walls, joins distant floor sectors, and changes local
gravity as his body passes. Relation binds an exposed segment to a firmament
anchor. His next burrow or charge pulls that segment somewhere the rest of his
body cannot geometrically follow, exposing the first contradiction.

### Claim II: I Name What Lives

Yaldabaoth assigns false identities: Council subject, demon, undead, fae
plaything, or divine supplicant. Spectral attacks recall all five established
Authorities. Definition declares the player **Unwritten**, rejects the imposed
identity, and reveals the true lion head among projected copies.

### Claim III: No Consequence Precedes Me

Attack paths, impacts, hazards, and knockback resolve out of order.
Continuance captures one declared sequence and forces it to conclude honestly,
stunning Yaldabaoth and exposing the final contradiction segments.

At low health, Yaldabaoth withdraws stolen fire into an invulnerable crown. The
player relates the dormant celestials to the crown, redefines them from
creations into Witnesses, and continues the earlier eclipse. They awaken,
reverse orbit, and strip away the crown.

## Act III — No Other

The allied Sun pins selected segments with vertical white beams. The allied
Moon removes their protection with black crescent cuts. Their overlap exposes
the lion head for unrestricted damage windows.

Yaldabaoth combines the established mechanics at maximum tempo but introduces
no new rule. At critical health, the seven rings close while he attempts:

> I AM, AND THERE IS NO OTHER.

A final eclipse interrupts the Edict. Participants must personally inflict the
finishing damage; the celestials cannot complete the victory for them.

Death is revocation rather than a corpse explosion. The serpent unthreads into
black-and-white propositions, the roar becomes silent, and the vanished head's
mouth continues moving. Sun and Moon pass through one another, lose their
outlines, and become ordinary distant lights. The arena opens onto the
Unwritten Observatory.

## Rewards and Aftermath

Every eligible participant receives:

- One **Ineffable Thesis**, prioritizing an eligible spell part the player has
  not learned. If all are known, the resulting duplicate is exchangeable
  through the Broker for 32 Marks.
- **64–128 Marks of No Authority**, registered as the Ineffable faction token.
- The hidden challenge advancement **No Other**
  (`mnagnosis:boss/defeat_yaldabaoth`).

The first victory additionally grants once per player:

- One **Axiomatic Husk**, the substrate for a first Original Work.
- Permanent Original Work authorship access.
- A guaranteed lion-serpent trophy.

These first-clear effects are idempotent and participant-based rather than
dependent on the killing blow.

## Authored Projection Rematches

The original victory is ontological: Yaldabaoth no longer exists as an
authority over the player. Later attempts use **Reconstruct the First Error**,
through which the player authors a practice emanation.

The projection is monochrome static with missing frames, inverted surfaces,
projection seams, and dialogue that no longer claims original divinity. It
grants Thesis, Marks, and rare cosmetics but never repeats the Axiomatic Husk,
Original Work unlock, or guaranteed trophy. Its death collapses like a
deliberately dismissed diagram.

## Encounter Contracts

The implementation must expose concepts equivalent to:

- `EncounterVariant { ORIGINAL, PROJECTION }`
- `EncounterPhase { CELESTIALS, CLAIMS, REVOCATION, COMPLETE, RESETTING }`
- `ExceptionKind { RELATION, DEFINITION, CONTINUANCE }`

The encounter controller owns phase transitions, Edicts, valid Exception
targets, celestial allegiance, participant scaling, persistence, cleanup, and
reward eligibility.

Death removes a participant from the active attempt. The encounter resets when
no living participant remains. Disconnects receive a grace period and then
count as leaving. If state restoration cannot prove agreement between arena,
boss, and roster, it returns players safely, preserves the Writ, removes
transient entities, and restores the arena template.

Placed and destroyed blocks never survive cleanup. Party scaling changes
health, attack overlap, and recovery time, never mechanic solvability.

## Readability and Acceptance

- Every damaging Edict has a subtitle, unique geometric telegraph, distinct
  sound, and at least 1.25 seconds of warning.
- No solo pattern demands more than two simultaneous high-attention responses.
- Invalid Exception combinations give immediate feedback without consuming
  the action or causing an unavoidable wipe.
- Original and projection variants remain distinguishable without relying on
  color alone.
- Reduced-particle settings preserve every required combat cue.
- Verification covers solo, two-player, and four-player victories; wipes;
  disconnects; restart recovery; dimension unload; cleanup; reward
  idempotency; multipart hitboxes; gravity camera behavior; subtitles; and
  low-end performance.

## V1 Boundaries

The initial release excludes:

- Tier 7.
- A Seven Heavens gauntlet.
- Escalating rematch difficulties.
- Mandatory multiplayer roles.
- A complete Original Work editor inside the boss feature.
- Generic resource grinding for Exceptions or entry attempts.

## Staged Roadmap

1. [Exception of Distance](yaldabaoth/01-exception-of-distance.md)
2. [Exception of Identity](yaldabaoth/02-exception-of-identity.md)
3. [Exception of Sequence](yaldabaoth/03-exception-of-sequence.md)
4. [Writ and Firmament Entry](yaldabaoth/04-writ-and-firmament-entry.md)
5. [First Firmament Runtime](yaldabaoth/05-first-firmament-runtime.md)
6. [Yaldabaoth Foundation](yaldabaoth/06-yaldabaoth-foundation.md)
7. [Celestial Foundations](yaldabaoth/07-celestial-foundations.md)
8. [Act I: The Counterfeit Sky](yaldabaoth/08-act-one-counterfeit-sky.md)
9. [Claim: There Is No Outside](yaldabaoth/09-claim-no-outside.md)
10. [Claim: I Name What Lives](yaldabaoth/10-claim-name-what-lives.md)
11. [Claim: No Consequence Precedes Me](yaldabaoth/11-claim-no-consequence.md)
12. [Claims and Crown Transition](yaldabaoth/12-claims-and-crown-transition.md)
13. [Act III: No Other](yaldabaoth/13-act-three-no-other.md)
14. [Rewards and Original Work Bridge](yaldabaoth/14-rewards-and-original-work-bridge.md)
15. [Authored Projection Rematches](yaldabaoth/15-authored-projection-rematches.md)
16. [Integration and Release](yaldabaoth/16-integration-and-release.md)

## Decision Log

- Yaldabaoth is a post-mastery capstone, not the Tier 6 gateway.
- He is the Blind First Author, not a false claimant or former mortal.
- Combat mixes an authorship examination with a spectacle duel.
- The encounter is solo-first and party-scaled.
- The arena is a reusable pocket cosmos.
- Encounter verbs are earned universal actions rather than a prescribed spell
  loadout.
- Sun and Moon are defeated, rewritten, and turned against Yaldabaoth.
- First victory unlocks an Original Work; rematches are repeatable projections.
- The encounter uses three acts rather than a Seven Heavens gauntlet.
- The faction token is the Mark of No Authority.
