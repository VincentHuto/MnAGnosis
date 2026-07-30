# Ineffable HUD Texture Redesign

## Goal

Replace the predominantly code-drawn Ineffable mana HUD with a clean,
pixel-exact texture atlas based on the approved Contained-to-Desynchronized
concept. Preserve the current footprint and gameplay information while giving
the badge its own integrated frame.

## Visual Direction

The HUD uses a stark near-black, white, and gray palette with sparse existing
cyan signals. Every structural edge is hard-edged and aligned to the pixel
grid. The frame remains recognizable at every paradox level.

The visual progression is:

1. **Contained:** a crisp, continuous frame with orderly upper and lower rails.
2. **Lattice:** the paradox lattice advances from the right while selected rail
   sections begin to disagree by one pixel.
3. **Local inversion:** additional frame sections swap or offset, increasing
   the sense of contradiction without obscuring resource values.
4. **Contradiction:** the strongest approved displacement is used, but the
   outer silhouette and channel remain legible.

The new badge cradle is visually joined to the frame's left cap. It retains
the existing rendered badge item and the centered magic-level text beneath it.

## Layout Contract

- Preserve the existing 153 by 16 pixel mana-frame footprint.
- Preserve the current HUD origin and overall placement.
- Preserve the dynamic mana channel dimensions used by the custom renderer.
- Preserve the existing `ICastingResourceGuiProvider` frame coordinates so
  fallback or upstream rendering remains compatible.
- Keep the badge item at 16 by 16 pixels inside a newly textured square cradle.
- Keep the magic-level label beneath the cradle.

The texture atlas remains 256 by 256 pixels. The existing stable frame remains
at the provider-compatible origin. Additional atlas regions hold reusable
sprites for the badge cradle, disrupted frame states, mana rails, paradox
lattice, XP strip, and any small static circuit details.

## Rendering Responsibilities

### Texture-owned

- Outer frame silhouette and angular end caps
- Stable rails and circuit branches
- Badge cradle and its connection to the frame
- Mana rail appearance
- Paradox lattice appearance
- XP strip appearance
- Static detail nodes
- State-specific displaced or locally inverted frame sections

### Code-owned

- Selecting the frame state from the paradox ratio
- Cropping mana, paradox, and XP sprites to live values
- Positioning paradox fill from right to left
- Applying the existing restrained animation phase to displaced overlays
- Rendering the badge item and magic-level text
- Rendering contradiction/debt markers whose count and condition are dynamic

No dynamic resource value is baked into a state texture.

## State and Data Flow

`IneffableHudRenderer` continues to read mana, paradox, experience, magic
level, and the client authorship snapshot. It calculates clamped widths and a
paradox state exactly once per render.

The renderer then:

1. Blits the stable frame and integrated badge cradle.
2. Blits the state-specific disruption layer when paradox warrants it.
3. Crops and blits mana from left to right.
4. Crops and blits paradox from right to left.
5. Crops and blits XP along the baseline.
6. Draws the badge item, level text, and live contradiction markers.

Invalid, non-finite, or non-positive resource maxima retain the existing safe
behavior: the HUD is skipped or the corresponding width resolves to zero.

## Implementation Boundaries

- Keep atlas coordinates centralized as named constants rather than scattering
  numeric UV values through rendering methods.
- Remove code-drawn geometry only after an equivalent atlas layer exists.
- Do not refactor unrelated faction, casting-resource, or authorship behavior.
- Do not change resource thresholds or gameplay semantics as part of this
  visual cleanup.
- Use nearest-neighbor pixel artwork with transparency; do not introduce
  antialiasing, gradients, or filtering artifacts.

## Testing and Verification

Automated checks will cover:

- The atlas exists, remains 256 by 256, and contains required nontransparent
  sprite regions.
- The stable provider frame remains at its compatible coordinates.
- Mana, paradox, overlap, and inset geometry retain their current behavior.
- Paradox thresholds select the intended visual state.
- Atlas coordinates remain within bounds and match the declared sprite sizes.
- Static frame geometry is rendered through texture blits rather than rebuilt
  from large groups of `fill(...)` calls.

Verification will run the focused Ineffable HUD tests, the project's full test
suite, and a rendered visual inspection at representative low, medium, and
high paradox values.

## Acceptance Criteria

- The HUD closely reflects the approved Contained and Desynchronized concepts.
- Most visible structural artwork comes from the texture atlas.
- The badge has a clean integrated square frame while retaining its item and
  level display.
- Mana, paradox, XP, and contradiction information remain readable and behave
  as before.
- The HUD occupies the same screen footprint and does not disturb adjacent UI.
- The texture is crisp at native Minecraft GUI scale with no unintended seams,
  blur, or transparency defects.
