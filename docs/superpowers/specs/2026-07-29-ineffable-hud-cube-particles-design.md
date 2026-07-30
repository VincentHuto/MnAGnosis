# Ineffable HUD Cube Particles Design

## Goal

Render miniature rotating 3D Ineffable black and white cubes over the compact
HUD frame near its existing square circuit nodes. The effect should feel alien
and spatial while remaining anchored, readable, and inexpensive.

## Visual Behavior

Four cubes hover over the frame:

- two near the upper-left circuit nodes;
- two near the lower-right circuit nodes.

The emitters alternate between the existing
`ineffable_white_cube.png` and `ineffable_black_cube.png` particle textures.
Every cube has a distinct deterministic phase, XYZ rotation rate, size pulse,
opacity envelope, and vertical bob. The motion may drift roughly one GUI pixel
away from its anchor before returning, but must never obscure a large portion
of the mana or paradox channel.

The cubes render after the badge, frame, mana, paradox, XP, and disruption
layers. They therefore appear to hover over the textured frame rather than
being baked into it.

## Architecture

### `IneffableHudCubeLayout`

A pure client-side model owns:

- the four source-space node anchors;
- source-to-display coordinate conversion;
- deterministic animation phase offsets;
- cube size, bob, opacity, and XYZ rotation calculations.

It has no Minecraft rendering dependencies, making the animation behavior
directly testable.

### `IneffableHudCubeRenderer`

A dedicated renderer:

- consumes layout samples and the active GUI pose;
- emits six textured faces per cube;
- uses the same face topology and UV orientation as `OutlinedCubeParticle`;
- binds the existing black or white cube texture for each emitter;
- renders full-bright translucent vertices with depth testing disabled for
  reliable HUD visibility;
- restores blend, cull, shader, and depth state after drawing.

The renderer is stateless. It creates no persistent particle instances and
does not add particles to the client world.

### `IneffableHudRenderer`

The HUD renderer passes game time plus partial tick to the cube renderer after
all existing frame layers have rendered. The HUD mixin forwards its existing
`partialTick` argument. If no client level exists, the cube overlay is skipped.

## Anchor Contract

The anchors correspond to the concept texture's permanent circuit nodes:

- source `(171.5, 13.5)`;
- source `(206.5, 20.5)`;
- source `(782.5, 144.5)`;
- source `(821.5, 134.5)`.

They are converted through the frame's existing 976×158 source-to-153×25
display transform and offset by the HUD frame origin. Anchor order determines
texture alternation and phase; it does not change at runtime.

## Animation Contract

- Animation time is `client game time + partial tick`.
- Each cube rotates independently around X, Y, and Z.
- The base displayed half-size remains between 1.5 and 2.5 GUI pixels.
- Bob and lateral phase offsets remain within approximately one GUI pixel.
- Opacity remains high enough to read the black and white particle textures
  over both the frame and world.
- Motion is deterministic for a given time and anchor index.
- Config-disabled counterlaw animation does not disable these cubes; they are
  part of the base Ineffable HUD identity.

## Performance and State Safety

Four cubes at six quads each produce 96 vertices per HUD frame. The renderer
does not allocate world particles, perform random sampling, or retain mutable
animation state.

Rendering must flush only its own cube render types and restore the relevant
render state so later HUD elements are unaffected.

## Testing

Automated tests cover:

- exactly four anchors;
- correct source-to-display anchor conversion;
- alternating black and white texture selection;
- deterministic samples for the same time and index;
- distinct rotations for different emitters;
- bounded bob, drift, size, and opacity;
- forwarding of partial tick through the HUD entry point;
- continued existence and 16×16 dimensions of both particle textures.

Verification includes the focused cube-layout tests, the full Gradle suite,
resource processing, and a visual preview at the compact HUD scale.

## Acceptance Criteria

- Four miniature cubes visibly rotate in 3D over the intended circuit nodes.
- Their textures match the existing Ineffable world particles.
- They remain attached to the HUD while the camera and player move.
- Their motion feels deliberately strange without making resources unreadable.
- No world particles are spawned and no render state leaks into other HUD
  elements.
- Existing mana, paradox, XP, badge, level, and contradiction behavior remains
  intact.
