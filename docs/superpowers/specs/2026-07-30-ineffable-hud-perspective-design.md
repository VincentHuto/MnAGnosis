# Ineffable HUD Perspective Design

## Goal

Angle the complete Ineffable HUD away from the player and into the screen,
matching the supplied mockup while preserving the current compact footprint and
resource readability.

## Visual Contract

The HUD is treated as one physical panel rather than a collection of separately
positioned widgets. Its left side remains anchored near the current HUD origin,
while the assembly rises toward the right and receives mild 3D foreshortening.

The perspective applies to every visible HUD element:

- the badge frame and animated badge item;
- the level number;
- the concept frame and disruption layer;
- mana, mana cap, paradox, and XP fills;
- contradiction marks;
- the four animated black and white cubes.

Nothing remains flat behind or in front of the transformed panel. The angle is
deliberately restrained so text and resource values remain legible.

## Transform

A dedicated `IneffableHudPerspective` helper owns the transform constants and
applies them around a left-center pivot in HUD-local coordinates.

The transform uses:

- negative three degrees of Z rotation, lifting the right edge;
- positive ten degrees of Y rotation, pushing the right edge deeper;
- positive six degrees of X rotation for vertical foreshortening;
- scale `(0.98, 0.90, 1.0)` to preserve the compact screen footprint.

The exact constants are fixed and testable. They are not animated and do not
depend on camera motion, GUI scale, mana state, or counterlaw state.

The transform is applied after translating to the Mana and Artifice HUD origin
but before rendering any Ineffable HUD content. This lets every existing draw
path inherit the same pose, including the custom cube geometry.

## Architecture

### `IneffableHudPerspective`

This focused helper:

- exposes the transform's pivot, rotations, and scale;
- applies the pose operations in a documented order;
- provides pure point-projection math for tests;
- keeps perspective policy out of `IneffableHudRenderer`.

The pure projection mirrors the pose operation order closely enough to verify
the visible contract: the left anchor remains stable, the right edge rises,
the width and height remain within compact HUD bounds, and points have a
rightward depth gradient.

### `IneffableHudRenderer`

The renderer keeps its current outer push/pop pair. Immediately after moving to
`hudX, hudY`, it applies `IneffableHudPerspective`. Badge, frame, live layers,
contradictions, and cubes then render through the transformed pose without
changing their local coordinates.

No texture is regenerated or rasterized. Resource clipping, the live mana cap,
and cube animation continue to use their existing geometry and timing.

## Rendering Safety

The perspective helper mutates only the pose enclosed by
`IneffableHudRenderer`'s existing push/pop scope. It does not replace the global
projection matrix or create an off-screen framebuffer, so other HUD overlays
cannot inherit the transform.

The cube renderer already consumes the active GUI pose, so its six-faced
geometry follows the panel automatically. Its existing buffer flush and render
state restoration remain unchanged.

## Testing

Automated tests cover:

- fixed transform constants;
- an invariant left-side pivot;
- a right edge that projects above and behind the left edge;
- bounded projected width and height;
- deterministic projection;
- unchanged local resource-width calculations;
- successful compilation of all existing HUD rendering paths.

Full verification runs the Gradle test suite, resource processing, and patch
hygiene checks.

## Acceptance Criteria

- The whole HUD visibly angles upward and away toward the right.
- Badge, level, bars, fills, marks, and cubes remain spatially unified.
- The left edge stays near its current location.
- Resource values and level text remain readable.
- The HUD remains compact and does not overlap substantially more screen space.
- No other HUD or world rendering receives the transform.
- Existing live bar clipping and cube animation remain functional.
