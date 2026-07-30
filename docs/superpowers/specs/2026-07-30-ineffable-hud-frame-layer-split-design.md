# Ineffable HUD Frame Layer Split Design

## Goal

Keep the portal field behind every structural frame detail and close both ends
of the live mana rail.

## Root Cause

The current portal quad renders after the monolithic concept base. The portal
rectangle overlaps 947 opaque white or gray pixels belonging to the frame
border and interior trapezoids, so those foreground details are replaced by
portal fragments.

The existing mana-cap texture contains a reusable five-pixel-wide vertical
closure, but runtime composition places it only at the live mana rail's right
edge.

## Texture Split

Split `ineffable_hud_concept_base.png` into two complementary 976×158 RGBA
textures:

### Backing

`ineffable_hud_concept_backing.png` retains only opaque `#050505` pixels inside
the source-space channel rectangle `(80, 52, 790, 54)`. Every other pixel is
transparent.

### Foreground

`ineffable_hud_concept_frame.png` retains every original base pixel except the
dark channel pixels assigned to the backing. It therefore contains:

- all white frame edges;
- all gray shadow and depth edges;
- both interior trapezoids;
- both end structures;
- exterior black details and square nodes.

Alpha-compositing the foreground over the backing without the portal must
reconstruct the original base pixel-for-pixel.

The original base texture remains in resources as the immutable source and
reference image, but runtime rendering uses the two derived layers.

## Runtime Composition

The exact high-resolution frame layer order becomes:

1. backing texture;
2. portal shader;
3. foreground frame texture;
4. disruption texture;
5. live mana texture;
6. left mana cap;
7. right mana cap;
8. paradox texture;
9. XP texture.

The current local portal width of `CHANNEL_WIDTH - 30` and the existing
resource-layer `Z + 1` translation are preserved. The foreground layer still
uses the complete original channel geometry, so its border and trapezoids cover
the portal wherever they overlap.

## Mana Caps

The existing `ineffable_hud_concept_mana_cap.png` remains the shared cap
texture.

When `manaWidth > 0`:

- the left cap renders at `CHANNEL_X`;
- the right cap renders at `manaCapX(manaWidth)`.

When `manaWidth == 0`, neither cap renders. For very small nonzero widths, cap
overlap is allowed and produces a closed minimal rail.

Both caps render after the mana rails so their vertical closures remain crisp.

## Architecture

### `IneffableHudConcept`

Add texture accessors for the backing and foreground assets. Keep
`baseTexture()` for tests and reference tooling.

### `IneffableHudRenderer`

Replace the runtime base blit with backing, portal, and foreground calls in
that order. Replace the single cap helper with one helper that draws both caps
from the shared cap texture.

No changes are made to resource-width calculation, paradox clipping, XP,
perspective, cube animation, shader logic, or badge rendering.

## Asset Generation

Generate the two PNG files mechanically from the original base image. The
generation operation copies pixels without resampling, antialiasing, color
conversion, or palette changes.

The split can be regenerated deterministically from the source image and
channel constants.

## Testing

Automated tests verify:

- backing and foreground are each 976×158;
- both derived textures contain visible pixels and transparent exterior;
- backing contains only transparent pixels and opaque `#050505`;
- foreground contains no opaque `#050505` pixels inside the channel rectangle;
- backing plus foreground reconstruct the original base exactly;
- runtime layer order is backing, portal, foreground, disruption, resources;
- left-cap X is `CHANNEL_X`;
- right-cap X continues to follow `manaCapX`;
- neither cap renders for zero mana;
- existing mana-width behavior remains unchanged.

Full verification runs the Gradle test suite, resource processing, and patch
hygiene checks.

## Acceptance Criteria

- Portal pixels never cover the white border, gray depth edge, or trapezoids.
- The portal remains visible through the intended dark channel opening.
- A vertical cap closes both ends of every nonempty mana rail.
- The current portal trim and resource depth adjustment remain intact.
- The split textures reconstruct the original base exactly.
- No other HUD behavior or rendering state changes.
