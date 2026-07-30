# Ineffable HUD Deeper Perspective Design

## Goal

Make the Ineffable HUD read as a panel extending farther into the screen at its
right edge, without changing the apparent anchor or scale of the left badge.

## Visual Contract

The existing left-center pivot remains fixed. The HUD keeps its current upward
tilt, vertical foreshortening, compact scale, and unified treatment of the
badge, bars, portal, marks, text, and animated cubes.

Only the depth recession increases: the far-right edge becomes more compressed
and moves substantially farther away from the player. The result should remain
readable rather than becoming an extreme tunnel effect.

## Transform

`IneffableHudPerspective` remains the single owner of the HUD transform. Its
positive Y-axis rotation increases from 10 degrees to 18 degrees.

All other transform values remain unchanged:

- pivot `(14, 18)`;
- Z rotation `-3` degrees;
- X rotation `6` degrees;
- scale `(0.98, 0.90, 1.0)`.

The transform continues to apply once around the complete HUD. No texture,
shader, layer order, local draw coordinate, or animation behavior changes.

## Testing

Projection tests will establish the new right-edge coordinates before the
production constant changes. They will verify:

- the left-center pivot remains fixed;
- the right edge has substantially more negative depth than at 10 degrees;
- the pose stack matches the pure projection;
- the projected HUD remains within compact, readable width and height bounds.

Full verification runs the Gradle test suite, resource processing, and
`git diff --check`.

## Acceptance Criteria

- The left badge remains visually anchored at its current position and size.
- The far-right edge visibly recedes deeper into the screen.
- The existing upward tilt remains.
- The complete HUD continues to behave as one physical panel.
- The mana bar and other HUD information remain readable.
- No portal, texture, shader, particle, or layer-order behavior changes.
