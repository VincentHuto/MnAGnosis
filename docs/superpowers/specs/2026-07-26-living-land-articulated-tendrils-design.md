# Living Land Articulated Tendrils Design

## Goal

Living Land pillars must move like animated inorganic creatures rather than
rigid objects under telekinesis. The existing three-to-five-state payload,
physical conservation, and Precision projection rules remain unchanged.

## Motion

Each strike becomes a spring-chain tendril. The entity position is the leading
segment. Remaining segment positions follow a delayed path with a fixed
0.78-block constraint, producing bends and whip-like overshoot.

The head retains limited target homing but adds deterministic lateral motion.
The lateral plane and phase depend on mode and entity ID:

- Ceiling Crush droops laterally before snapping downward.
- Wall Lances serpentine across the horizontal plane.
- Floor Teeth coil upward before inclining toward the target.

During the first six ticks, segment spacing grows from zero to full length so
the blocks appear to emerge sequentially from the source. After emergence, all
segments follow the head through a critically damped positional constraint.
The chain remains one entity for networking and persistence.

## Segment State

The strike stores current and previous positions for five segments on both
server and client. Configure and NBT load initialize every segment at the
source. Each tick:

1. copy current positions to previous positions;
2. move the head using homing plus deterministic lateral acceleration;
3. constrain each follower toward its predecessor at the current emergence
   spacing;
4. apply a small mode-specific bend bias;
5. retain positions for rendering and swept collision.

Segment arrays are transient. NBT persists the head and payload as before;
loading reconstructs a collapsed chain that naturally re-emerges. This avoids
large per-tick network payloads. Client segment simulation uses the tracked
head position as its authoritative anchor and the same deterministic rules.

## Rendering

Each carried block renders at its own interpolated segment position relative
to the entity head. It rotates toward the next segment, or along the local
tangent for the final segment. Small alternating roll offsets prevent the
chain from reading as a mechanically aligned row.

Precision keeps its black-and-white projected shell, applied independently to
every articulated segment.

## Collision and Settlement

Collision uses a swept 0.4-radius AABB from each segment's previous position to
its current position. A hit from any segment applies damage once.

Physical settlement uses the local tangent at the leading segment rather than
the entity velocity. A miss, invalid target, or timeout still restores or
settles every conserved block through the existing idempotent payload.
Precision still performs no placement, restoration, or drops.

## Verification

Pure tests verify emergence spacing, follower constraints, deterministic
curvature, mode-distinct bend directions, and local tangent calculation.
GameTests verify curved segment positions, non-central swept collision, exact
payload persistence, physical recovery, and Precision projection. Final
verification includes a clean build, complete GameTests, client renderer
initialization, and packaged-JAR audit.
