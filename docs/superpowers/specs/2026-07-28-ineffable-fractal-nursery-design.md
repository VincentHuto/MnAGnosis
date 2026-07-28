# Ineffable Robes Fractal Nursery Design

## Goal

Replace the fragmented `FRACTAL_FLASH` robe effect with a continuous,
charcoal-dominant field of small, recognizable Mandelbrot forms. The field
crawls across the robe while new buds emerge behind it. Fine silver-white
boundary filaments provide the visible detail; Mandelbrot interiors must not
produce large white or gray patches.

The other Ineffable Robes shader modes remain unchanged.

## Current Problem

The existing fractal mode evaluates the pattern from the armor texture UVs.
The robe model is split into many separate UV islands, so the same formula
restarts on each face and model part. The current near-zero, rapidly
oscillating scale further collapses the image into disconnected bright noise.
The result does not read as a Mandelbrot set and does not flow coherently over
the garment.

## Coordinate System

The vertex shader will pass the robe vertex position to the fragment shader as
a model-space varying. The fragment shader will derive its two-dimensional
fractal coordinates from this shared model-space position rather than from
`texCoord0`.

A cylindrical projection around the wearer will use height for one axis and
the angle around the model for the other. This makes the nursery wrap around
the robe and keeps adjacent model parts in the same procedural field. The
unavoidable angular seam will be placed along the back of the robe, where it
is least visible. The hood and moving cloth pieces will sample the same field
because their submitted vertices already include their model-part transforms.

The existing UV coordinates remain available to the other shader modes.

## Fractal Nursery

The projected coordinate is divided into a repeating grid of moderately sized
cells. Each cell uses a deterministic hash of its integer cell coordinate to
select:

- A local center offset.
- A small rotation.
- A scale within a narrow range.
- A birth-phase offset.
- A modest drift variation.

Every cell evaluates the actual quadratic Mandelbrot recurrence:

`z = z² + c`

The cell's local coordinate supplies `c`, with the traditional Mandelbrot
shape centered and scaled so that a complete small bud can be recognized.
Neighboring cells use staggered phases, preventing synchronized flashing or a
regular checkerboard appearance.

Two nearby nursery scales will be blended: a primary layer of clearly readable
buds and a sparser secondary layer of smaller buds. This adds natural
complexity without filling the robe with brightness.

## Motion and Lifecycle

A slow shared flow vector moves the projected field upward and slightly
sideways. A gentle sinusoidal cross-current changes the path over time without
making the pattern jitter.

Each cell has a repeating lifecycle:

1. The bud fades in through its fine outer boundary.
2. Its scale expands slightly, revealing additional Mandelbrot detail.
3. It remains readable while the shared field carries it onward.
4. It fades out before the next deterministic cell generation replaces it.

Because the field itself moves continuously and cell lifecycles are staggered,
new buds appear behind the passing growth rather than making the entire robe
pulse at once. Time functions will remain continuous at cycle boundaries.

## Contour Rendering and Color

The shader will use smooth Mandelbrot escape-time values to form narrow
anti-aliased contour bands near the set boundary. It will not fill the
Mandelbrot interior with white. Interior and empty space both resolve to the
robe's charcoal lighting, while only the boundary filaments rise toward a
soft silver-white.

Brightness will be limited in three ways:

- Narrow derivative-aware contour widths.
- Per-cell lifecycle opacity.
- A capped combination of the two nursery layers.

The existing lighting, overlay, alpha, and fog integration remain intact.
The visible result should be predominantly charcoal at every animation phase,
with no broad gray or white slabs.

## Shader Interface

The large set of uniforms specific to the old fractal-flash implementation
will be replaced with a smaller group describing:

- Field scale and flow speed.
- Primary and secondary cell sizes.
- Mandelbrot iteration count.
- Contour width and brightness.
- Bud growth range and lifecycle speed.

`IneffableArmorLayer` will set conservative defaults tuned for readability and
performance. `doppleganger.json`, the Java uniform configuration, and the GLSL
declarations must remain synchronized. `ShaderMode == 3` will continue to
select this effect, preserving the existing configuration value.

## Performance and Compatibility

The fragment shader targets GLSL 1.50 and must avoid unsupported features.
Mandelbrot iteration loops will use a fixed compile-time maximum with a uniform
early exit, matching the project's established shader style.

The nursery will use two Mandelbrot evaluations per fragment and no
multi-sample motion-blur loop. A moderate default iteration count will preserve
the characteristic silhouette without multiplying the current shader cost.
Derivative-based antialiasing will replace the old blur-sample controls.

All changes are limited to the Ineffable robe shader assets, their uniform
configuration, and focused shader-contract tests. Existing uncommitted armor
model work must be preserved.

## Failure Behavior

Uniform inputs will be clamped to safe nonzero ranges in GLSL so malformed
values cannot cause division by zero or an entirely saturated field. If a
uniform is unavailable during shader reload, the existing safe uniform lookup
behavior remains in effect. Shader compilation or resource-loading errors must
remain visible through the normal Minecraft resource reload logs.

## Verification

Automated checks will verify that:

- The vertex shader exports model-space coordinates used by fractal mode.
- Fractal mode no longer derives its field from texture UVs.
- The shader contains the quadratic Mandelbrot recurrence and smooth
  escape-time contour logic.
- The old blur loop and obsolete fractal uniforms are removed consistently
  from GLSL, JSON, and Java.
- `FRACTAL_FLASH` remains mapped to uniform value `3`.
- The project test suite and resource-processing build complete successfully.

Visual in-game acceptance requires:

- Many small Mandelbrot buds are recognizable at ordinary third-person camera
  distance.
- The nursery crawls continuously across adjacent robe pieces.
- New buds appear behind the moving field without synchronized flashing.
- Charcoal remains the dominant color throughout the animation.
- No large white or gray patches appear on the hood, torso, sleeves, or lower
  robe.
- The back seam is unobtrusive during normal movement.
