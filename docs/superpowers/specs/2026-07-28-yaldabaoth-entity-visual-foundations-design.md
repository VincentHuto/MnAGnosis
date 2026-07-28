# Yaldabaoth Entity Visual Foundations Design

## Status and Scope

This pass creates the three damageable, spawnable visual foundations for the
Yaldabaoth encounter:

- Yaldabaoth, the Blind First Author.
- The white Sun outlined in black, representing Exposure.
- The black crescent Moon outlined in white, representing Omission.

The work implements Java entities, GeckoLib models, textures, renderers, idle
animations, and one sample combat animation for each entity. It deliberately
does not implement encounter AI, attacks, multipart damage forwarding, arena
control, orbit logic, celestial beams, boss bars, loot, sounds, projection
shaders, or the complete encounter controller.

## Canonical and Historical Grounding

The visual direction follows the canonical
[Yaldabaoth Capstone Design](2026-07-28-yaldabaoth-capstone-design.md), the
staged
[Yaldabaoth Foundation](yaldabaoth/06-yaldabaoth-foundation.md) and
[Celestial Foundations](yaldabaoth/07-celestial-foundations.md), and the
[MnAGnosis Lore and Design Bible](../../mnagnosis-lore-and-design-bible.md).

Yaldabaoth's lion-serpent form and lightning-fire eyes derive from the
*Apocryphon of John*. His blindness is ignorance and arrogance, not missing
eyes. *On the Origin of the World* reinforces his lion-like appearance,
borrowed authority, and ignorance of his origin. *The Reality of the Rulers*
reinforces the Chief Ruler's blindness and exclusive claim to divinity.

The paired Sun and crescent Moon are original MnAGnosis imagery, not an
attested historical pair. They express Yaldabaoth's counterfeit binary and
must remain visually distinct without relying on color alone.

Primary text references:

- <https://www.earlychristianwritings.com/text/apocryphonjohn.html>
- <https://gnosis.org/naghamm/origin.html>
- <https://www.earlychristianwritings.com/text/archons.html>

## Architecture

### Yaldabaoth

`YaldabaothEntity` is a damageable GeckoLib living entity with no goals,
navigation, autonomous target selection, or gravity. It owns synchronized
presentation state and a short combat-animation trigger that can be activated
by server code. Taking valid damage activates that trigger so the sample
animation can be inspected without an encounter controller.

The initial entity uses one authoritative hitbox. The GeckoLib geometry
contains stable, named bones for the lion head, jaw, mane, neck transition,
numbered serpent segments, tail, terminal sweep, and crown. Those names are
the future integration boundary for multipart hitboxes and contradiction
segments; this pass does not pretend that animated client bones are already
authoritative server hitboxes.

### Celestials

`AbstractCelestialEntity` holds behavior shared by the Sun and Moon:

- Independent health.
- No gravity or autonomous AI.
- Synchronized `HOSTILE`, `DORMANT`, and `WITNESS` allegiance.
- Synchronized sample combat-animation state.
- Safe NBT persistence with fallback from invalid saved values.
- No independent loot.

`YaldabaothSunEntity` and `YaldabaothMoonEntity` provide concrete types and
GeckoLib animation identities. Controller identifiers, encounter identifiers,
stagger, anti-focus intensity, and actual state mechanics remain deferred
until the encounter runtime exists.

### Client Boundary

Each entity has one `GeoModel` and one `GeoEntityRenderer`. All client-only
types remain under the client package and are referenced only from the
client-side renderer registration event. Common entity and registry classes
must be loadable by a dedicated server.

## Scale and Geometry

Yaldabaoth is a large but testable full serpent approximately 14–18 blocks
long, centered on a target length near 16 blocks. The lion head is
approximately 4–5 blocks across. The body reads as one articulated creature,
not a train of mobs.

The model uses:

- A broad lion face with an articulated jaw.
- A damaged-corona mane made from uneven, layered plates.
- Contained lightning-fire eyes.
- A neck transition into a chain of overlapping serpent segments.
- A tapered tail ending in a distinct terminal sweep segment.
- A crown bone reserved for later stolen-fire presentation.

The Sun and Moon are separate shallow three-dimensional forms approximately
3–4 blocks across. They are readable from either side and at oblique angles.
The Sun is a brilliant white disc inside a crisp black modeled rim. The Moon
is an absolute-black crescent inside a crisp white modeled rim. Geometry, not
texture color alone, communicates disc versus crescent.

## Texture Direction

Textures use detailed Minecraft-native pixel art:

- Yaldabaoth: 128×128.
- Sun: 64×64.
- Moon: 64×64.

Yaldabaoth uses muted gold, aged bone, ember, and storm-dark tones. The mane
resembles a damaged solar corona rather than ordinary fur. Plate seams and
underside scales make the long silhouette legible during motion. The palette
avoids making him appear either purely holy or generically demonic.

The celestials use hard monochrome boundaries and minimal internal gradients.
Their outlines exist in modeled geometry as well as texture pixels, preserving
their identities under distance, lighting variation, reduced effects, and
common color-vision differences.

## Animation Design

Every entity has a looping idle animation and one non-looping sample combat
animation.

### Yaldabaoth

The idle favors long stillness: slow chest and neck breathing, slight
segment-by-segment coil drift, sparse mane movement, and small discontinuous
head corrections.

The sample combat animation combines an opening lion jaw, a forward roar snap,
a traveling motion through the serpent segments, and a terminal tail sweep.
It demonstrates the named-bone chain without producing damage or knockback.

### Sun

The idle uses slow axial rotation and a restrained pulse between the white
face and black boundary.

The sample combat animation compresses, turns toward its forward axis, locks,
and flares as if releasing a judgment beam. No beam entity or damage is
created.

### Moon

The idle gently rocks while nested crescent layers shift by a small amount,
suggesting an unstable omission in space.

The sample combat animation draws backward, rotates briefly edge-on, and
slashes through a broad crescent arc. It does not erase projectiles or alter
terrain.

### Trigger Contract

Each entity exposes a public, server-safe method to trigger its sample combat
animation. The trigger is synchronized so all tracking clients see the same
presentation. A valid damage event invokes the same method for easy manual and
automated testing. Repeated damage during an active sample animation restarts
or extends it according to one consistent timer policy defined in the
implementation plan.

## Persistence and Failure Behavior

Only stable presentation state is saved. Invalid or unknown enum values fall
back to the safest base state:

- Celestial allegiance falls back to `HOSTILE`.
- Combat animation falls back to idle.

Entities never save client animation objects. They save primitive state and
reconstruct GeckoLib presentation after loading. They have no drops and do not
despawn through ordinary mob rules. This makes them reliable visual test
subjects while avoiding accidental reward or encounter behavior.

## Verification

Test-first implementation will cover:

- Safe enum and NBT serialization.
- Damage-triggered combat-animation timing.
- Celestial allegiance transitions.
- Invalid saved state fallback.
- Registration and spawning of all three entities.
- Independent health and ordinary damage.
- No gravity, navigation goals, or autonomous targets.
- Required GeckoLib model bone names.
- Required idle and combat animation names.
- Valid geometry and animation JSON.
- Exact texture dimensions and resolvable asset paths.
- Common-side compilation without client renderer linkage.

Verification runs the JUnit suite, focused GameTests where practical, Java
compilation, resource processing, and the complete Gradle build.

## Explicit Exclusions

This pass does not add:

- Spawn eggs or summon items.
- Encounter AI or movement controllers.
- Real attacks, damage lanes, beams, projectile erasure, or floor omission.
- Multipart server hitboxes or damage multipliers.
- Boss bars, sounds, subtitles, loot, or advancements.
- Controller ownership, encounter UUIDs, stagger, or anti-focus behavior.
- Original/projection variant rendering.
- Crown mechanics, Claims, authored actions, or encounter phase transitions.

These exclusions keep the result aligned with the requested visual foundation
while preserving deliberate integration points for the staged Yaldabaoth
roadmap.

