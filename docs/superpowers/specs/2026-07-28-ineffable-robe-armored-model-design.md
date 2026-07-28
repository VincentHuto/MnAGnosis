# Ineffable Robe Armored Model Selection

## Goal

Replace runtime chest and leggings clearance transformations with the supplied
scaled-up, trimmed Blockbench geometry. Continue rendering the original hood
only when the wearer has no item in the head armor slot.

## Selection Rules

The ineffable robe continues to render only while the `ineffable_robe` Curio is
equipped in its existing body slot.

- If either the chest slot or leggings slot is occupied, render the new armored
  robe body.
- If neither the chest slot nor leggings slot is occupied, render the original
  robe body.
- If the head slot is empty, render the original robe hood alongside the
  selected body.
- If the head slot is occupied, do not render the robe hood.
- Boots do not affect body-model selection.

These rules apply independently. For example, a player wearing a chestplate but
no helmet receives the armored robe body plus the original robe hood.

## Architecture

Add the supplied Blockbench export as a second client-side model with its own
model-layer location and layer definition. Adapt the export to the project's
Minecraft mappings, naming, and model conventions without changing its authored
geometry.

Keep the original model as the source of the normal body and hood. Give the
render layer explicit access to:

1. the original robe model;
2. the armored robe model; and
3. a small model-selection policy derived from occupied equipment slots.

The render layer selects one body model per frame. Hood visibility is controlled
separately on the original model so the hood can accompany either body without
rendering the original body twice.

Both variants use the existing shader buffer, light and overlay values, Curios
lookup, and entity animation state.

## Animation and Rendering

The armored model's body, arms, and cloth parts follow the corresponding parent
humanoid transforms. Its cloth chains receive the same walking and idle motion
style as the existing robe wherever the supplied hierarchy supports it.

When the armored body is active, the old chest and leggings clearance offsets
and scales are not applied. The original body retains its authored baseline
geometry. Helmet clearance inflation is removed in favor of hiding the hood
whenever the head slot is occupied.

## Testing

Introduce a dependency-light selection policy that can be unit tested without a
Minecraft renderer. Tests cover:

- no armor selects the original body and shows the hood;
- helmet only selects the original body and hides the hood;
- boots only selects the original body and shows the hood;
- chestplate only selects the armored body and shows the hood;
- leggings only selects the armored body and shows the hood;
- chestplate or leggings with a helmet selects the armored body and hides the
  hood.

Run the focused selection tests, the existing ineffable armor tests, and the
project's compile/test tasks. Existing unrelated worktree changes remain
untouched.

