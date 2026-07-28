# Gravity Fixed-Surface Control and Overlay Design

## Problem

Block-targeted Gravity Shift surfaces leave players drifting along walls or
ceilings after movement input stops. The drift continues until the player
leaves the authored surface. Near ordinary floors and other adjoining terrain,
the first-person view can also be covered by a block texture even though the
shifted camera is not inside that block.

These symptoms have separate coordinate-frame causes:

- `LivingEntity.travel` applies gravity and drag in vanilla world axes. The
  current tail hook cancels world-down acceleration and patches only the
  surface tangent that maps onto world Y. It does not reproduce the complete
  vanilla movement update in the active gravity frame.
- A direction-changing attachment projects velocity only against the new
  support normal. Prior gravity-axis fall velocity can therefore become
  surface-parallel wall momentum.
- `ScreenEffectRenderer` samples around `Player.getX()`, `getEyeY()`, and
  `getZ()` using an upright world-axis cube. For a wall-gravity player,
  `getEyeY()` remains roughly one normal eye height above the anchor in world
  Y, so blocks one or two blocks above the real rotated eye can incorrectly
  trigger the inside-block overlay.

## Selected Design

Normalize ordinary movement in the active gravity frame, make fixed-surface
attachment discard obsolete fall velocity, and make first-person overlay
sampling follow the rotated eye. Preserve the existing collision solver,
camera rotation, and bounded near-plane clearance.

## Movement Normalization

Add one pure movement transform to `GravityPhysics`. It accepts the velocity
produced by vanilla travel together with:

- active gravity;
- the live Forge entity-gravity value;
- the horizontal friction multiplier vanilla used;
- the vertical drag multiplier vanilla used.

For the friction-retaining branch, the transform first reverses vanilla's
world-axis horizontal drag, vertical drag, and world-down gravity contribution
to recover the velocity immediately after movement and collision. It converts
that velocity to gravity-local coordinates, then reapplies:

- vanilla horizontal friction to both local surface tangents;
- vanilla gravity and vertical drag to local gravity Y.

The result is converted back to world coordinates. The no-friction branch
performs the same gravity remap without inventing drag.

This replaces the separate gravity-compensation and missing-tangent patch. It
does not introduce extra braking: ordinary blocks, ice, movement attributes,
knockback, and airborne momentum retain their vanilla multipliers.

## Fixed-Surface Attachment

When gravity changes from the previous frame into a non-world-down fixed
surface, attachment removes:

- velocity into or away from the new support plane; and
- velocity along the previous gravity axis.

The remaining velocity is genuine motion tangent to both frames. This prevents
accumulated world-down falling velocity from becoming an unexplained downward
wall slide. Releasing back to world-down preserves the current physical world
velocity, matching the existing release behavior.

The attachment API will pass both previous and new gravity directions into the
pure transition helper. The stricter prior-axis removal applies to
block-surface attachment; mobile adhesion behavior is not broadened by this
repair.

## Gravity-Aware Block Overlay

Add a pure client helper that produces the eight vanilla-style overlay sample
positions around an eye point, but constructs each offset in gravity-local
coordinates and rotates it into world space.

For shifted gravity, a focused `ScreenEffectRenderer` mixin replaces
`getOverlayBlock` at method entry:

1. calculate the authoritative rotated eye from the player anchor and active
   gravity;
2. generate eight gravity-oriented samples using the player's width and
   vanilla's small eye-volume offsets;
3. return the first visible, view-blocking block found at those samples;
4. return no overlay when all rotated samples are clear.

World-down players continue through vanilla unchanged. A genuinely embedded
shifted eye still receives the normal block overlay.

This is independent of `GravityCameraClearance`: the clearance helper protects
the render near plane from real collision geometry, while overlay sampling
decides whether to draw the full-screen inside-block texture.

## Components and Scope

Expected production changes are limited to:

- `GravityPhysics`;
- `GravityShiftApi`;
- the shifted `LivingEntity.travel` integration;
- a focused gravity overlay-sampling helper;
- a `ScreenEffectRenderer` client mixin and mixin registration.

The repair does not change authored surface radius or duration, support-face
selection, oriented hitboxes, collision resolution, movement input mapping,
camera rotation, shaders, networking format, mobile corner traversal, fluids,
flight, or spell registration.

## Failure Handling

- Invalid or zero drag factors fall back to a safe non-inverting transform
  instead of dividing by zero or producing non-finite velocity.
- Overlay sampling only substitutes vanilla behavior while shifted gravity is
  active.
- If no shifted overlay sample finds a view-blocking block, the mixin returns
  no block overlay; it does not alter camera or player state.

## Tests and Verification

Unit tests will cover:

- all six gravity directions using the same vanilla friction and gravity
  magnitudes;
- repeated no-input wall and ceiling updates matching ordinary-ground
  tangential decay;
- no accumulation of world-Y velocity during wall gravity;
- fixed-surface attachment removing prior gravity-axis fall velocity while
  preserving valid shared tangential motion;
- release to world-down preserving world velocity;
- eight rotated overlay samples for every gravity direction;
- nearby world-up terrain not being sampled for a wall-oriented eye;
- a block containing the real rotated eye still being selected.

Forge GameTests will cover a real fixed block surface, repeated zero-input
travel, and stable tangent position over multiple ticks. Existing gravity
camera, collision, support, packet, and surface tests remain in the regression
suite.

Final verification consists of focused unit tests, the complete unit suite,
Java compilation, the complete GameTest server, and a bounded client startup
that confirms both gravity movement and screen-effect mixins apply without
injection errors.
