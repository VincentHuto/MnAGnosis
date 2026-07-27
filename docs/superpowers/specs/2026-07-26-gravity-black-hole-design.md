# Gravity Convergence Black Hole Design

## Intent

Gravity Convergence should read as a localized failure of spacetime, not as an
invisible force volume decorated with block particles. Its center is a real,
lightless sphere; the world immediately around it visibly bends; a thin,
unstable photon ring makes its silhouette readable against dark terrain.

## Visual Layers

1. **Event horizon:** `GravityFieldRenderer` draws a tessellated UV sphere at
   the entity origin. It is full-bright opaque black, has no shadow, and rotates
   only the surrounding ring—not the horizon itself.
2. **Photon ring:** crossed camera-facing annuli surround the horizon. Their
   monochrome white/gray segments rotate in opposite directions and vary subtly
   with polarity, making the orb legible without adding conventional elemental
   color.
3. **Gravitational lens:** an `AFTER_LEVEL` post-process copies the completed
   world image through a fragment shader. For each visible gravity field, the
   shader samples pixels radially toward the horizon and adds a narrow photon
   ring. Distortion is spatially bounded and falls to zero outside the halo.
4. **Residual field motion:** sparse black-and-white particles remain at the
   gameplay radius so players can still judge the affected volume. They are
   secondary to the black-hole center.

## Lens Contract

- Support three simultaneous lenses, matching
  `GravityFieldEntity.MAX_FIELDS_PER_OWNER`.
- Each shader lens uses `(screenX, screenY, screenRadius, signedPolarity)`.
- Projection ignores fields behind the camera, outside an expanded viewport,
  too close to the near plane, or hidden behind an opaque block.
- Screen radius derives from projecting the sphere center and one camera-up
  offset, so it scales naturally with FOV, window size, and distance.
- Lensing strength is clamped. The event horizon is never allowed to occupy the
  whole screen or generate singular texture coordinates.
- Attract and repel retain the same black-hole identity. Their polarity only
  reverses the ring drift and slightly changes radial sampling.

## Lifecycle and Compatibility

- The post chain is created lazily when a visible field first needs it, resized
  with the main render target, and closed on logout or resource reload.
- If shader creation fails, MnAGnosis logs the failure once and keeps the sphere
  renderer active. The gameplay entity and force calculation never depend on
  client shader availability.
- The lens is a standalone pass invoked after world rendering, so it can coexist
  with the existing Truth grayscale effect instead of replacing
  `GameRenderer.currentEffect()`.
- GUI and HUD pixels are not distorted because the pass runs before GUI render.

## Verification

- GameTests cover lens falloff, bounds, polarity sign, and the three-field cap.
- Resource-contract tests require the post-chain JSON and matching program
  vertex/fragment/metadata files.
- A full build verifies client-only class linkage and shader resource packaging.
- A bounded client launch verifies the post chain compiles without shader or
  framebuffer errors.

