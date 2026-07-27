# Ineffable Cube Scale Design

## Problem

The outlined Ineffable cubes are geometrically small in world units, but first-person hand particles render very close to the camera. Their current 0.15–0.24 block width therefore becomes screen-filling and obscures play.

## Approved behavior

- Reduce every Ineffable cube to approximately one-quarter of its current linear size.
- Target a randomized full width of 0.038–0.060 blocks.
- Preserve the existing black/white variants, opposing outlines, rotation, movement, lifetime, particle counts, and spell integrations.
- Apply one shared scale range to first-person hands, third-person hands, and all world effects.

## Verification

Extract the randomized half-size boundaries into side-neutral pure math. A GameTest regression must enforce a maximum base half-size of `0.030F`; the implementation will use `0.019F–0.030F`.
