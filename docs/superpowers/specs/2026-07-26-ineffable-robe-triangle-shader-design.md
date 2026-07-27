# Ineffable Robe Triangle Shader Design

## Goal

Replace the Ineffable armor's television-static surface with an animated
triangular lattice inspired by the supplied Shadertoy reference. The result
must combine the reference's crisp geometry and traveling diagonal bands with
the robe's existing dark, shaded appearance.

## Scope

The change applies to every equipped piece rendered by
`IneffableArmorLayer`: hood, chest robes, leggings, and boots. It changes only
the dedicated `doppleganger` shader and the robe-specific uniform values needed
to tune that shader. The armor model, equipment checks, cloth animation,
texture mask, and render type remain unchanged.

The unrelated `noise` and `truth_glitch` shaders are explicitly out of scope.

## Rendering Design

The fragment shader will continue to sample
`ineffable_static_mask.png`. Transparent mask pixels will be discarded so the
existing armor silhouette and authored UV coverage remain intact.

Opaque mask pixels will use their armor UV coordinates to build an equilateral
triangle grid. The implementation will adapt the supplied three-axis
barycentric-style Shadertoy construction to GLSL 1.50:

1. Scale and skew the UV coordinates into a triangular lattice.
2. Derive integer cell identifiers and distances to the three cell edges.
3. Draw narrow, dark seams where any edge distance crosses the configured
   threshold.
4. Compute a time-varying diagonal wave from the cell identifier.
5. Blend the wave over a charcoal base, illuminating selected triangle cells
   from dark gray through off-white.

The wave transition will be softened enough to avoid harsh frame-to-frame
flicker while keeping individual triangle cells crisp. The underlying
Minecraft vertex color, overlay, lightmap, color modulator, alpha, and fog will
remain part of the final output. This preserves environmental shading rather
than making the entire robe unlit white.

## Motion

`GameTime` will drive the cell wave continuously. The visual motion will be a
broad diagonal sweep resembling the reference image rather than randomized
per-pixel static.

The existing vertex shader displacement controlled by
`BotaniaDisfiguration` will remain. This preserves the robe's subtle geometry
jitter while the new surface pattern animates independently.

## Tunable Values

The existing robe render setup will be updated to express pattern controls
with clear shader uniform names where necessary. Initial values will target:

- a dense but readable grid across the 128×128 armor UV layout;
- narrow black or near-black triangle seams;
- a charcoal resting color that still responds to Minecraft lighting;
- off-white highlights rather than fully emissive white;
- a moderate diagonal sweep speed comparable to the reference;
- the current subtle vertex displacement magnitude.

These values are constants for the Ineffable armor layer. No gameplay config
or user-facing setting is needed.

## Compatibility and Failure Behavior

The shader remains a Minecraft core shader using
`DefaultVertexFormat.NEW_ENTITY`; no new texture, framebuffer, render pass, or
external dependency is introduced. If a uniform lookup is unavailable during
resource reload, Minecraft's existing `safeGetUniform` behavior prevents a
client crash, while shader compilation errors remain visible through the
normal resource-reload diagnostics.

## Verification

Verification will include:

1. A focused source-level regression test, where practical in the existing
   Gradle setup, that confirms the robe shader contains the triangular lattice
   path and no longer contains the random television-static path.
2. `processResources` to validate resource processing and JSON inclusion.
3. `compileJava` to validate any robe-layer uniform changes.
4. A full Gradle build if the focused checks pass within the available
   environment.

Visual acceptance criteria:

- the armor is predominantly charcoal rather than solid white;
- crisp triangular cells are visible on every equipped piece;
- bright gray/off-white bands travel diagonally through the cells;
- black seams remain legible inside bright bands;
- the pattern does not render outside the existing mask;
- Minecraft lighting, fog, overlays, cloth motion, and subtle vertex jitter
  remain functional.
