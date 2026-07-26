# Rooted Precision Tendril Design

## Goal

Precision Living Land must visibly emerge from intact terrain and remain
connected to that terrain while reaching its target. It must read as the land
stretching itself, not as copied blocks becoming a detached projectile.

## Diagnosed failure

Precision intentionally leaves its source blocks in place, but the strike is
created at the center of the first source block. The initial projected render
is consequently buried inside intact terrain. The strike also does not retain
or synchronize a source anchor: every articulated segment follows the moving
head, so even after emergence the visual detaches from the land.

## Behavior

- The controller passes the selected terrain face and emergence direction into
  the strike.
- A projected strike begins just outside that face, making its first frame
  visible without altering the source block.
- The strike synchronizes an immutable root position to clients.
- The head continues its current curved pursuit of the target.
- The final segment remains fixed at the root. Intermediate segments form a
  deterministic, animated curve between the root and head.
- The projected renderer draws continuous elongated terrain sections between
  adjacent curve points using the copied source block states. The sections
  expand as the head travels, creating the appearance of the source terrain
  stretching outward. Black-and-white projection feedback is limited to
  particles and never replaces or encloses the copied terrain texture.
- A wave examines scanned candidates until it has launched its requested
  number of tendrils. A rejected candidate does not consume a launch slot.
- Each candidate attempts the magnitude-requested pillar length first, then
  shorter lengths down to the three-block minimum. This preserves magnitude
  scaling where terrain supports it without making ordinary uneven terrain
  silently cancel the entire wave.
- Non-Precision Living Land retains its existing traveling articulated pillar.
- Impact, damage, duration, capacity limits, and non-destructive settlement
  remain unchanged.

## Verification

GameTests will prove that:

1. a projected controller with valid terrain launches a strike;
2. the strike emerges outside the intact source face;
3. its synchronized root remains fixed while the head advances;
4. projected segment endpoints span from the head to that root;
5. the original terrain remains unchanged.
6. invalid early candidates are skipped and shallow valid candidates fall
   back to a three-block tendril.

The full GameTest suite and Gradle build must pass.
