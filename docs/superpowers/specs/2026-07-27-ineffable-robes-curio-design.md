# Ineffable Robes Curio Design

## Goal

Replace the four-piece Ineffable armor set with one cosmetic
`mnagnosis:ineffable_robes` item equipped in the Curios `body` slot. The item
will render the complete existing Ineffable outfit and make room for armor worn
in vanilla equipment slots without granting armor or other gameplay effects.

## Item and Registration

- Keep `mnagnosis:ineffable_robes` as the surviving registry ID so existing
  robe stacks remain valid across the change.
- Replace its `ArmorItem` implementation with an ordinary stack-size-one Curio
  item of epic rarity.
- Give the item no armor, toughness, knockback resistance, durability, attribute
  modifiers, or special effects.
- Restrict the item to the Curios `body` slot through the Curios item tag and
  Curio capability/API expected by the project's pinned Curios 1.20.1 version.
- Declare Curios as a required runtime dependency because the item and renderer
  directly use its API.
- Remove the `ineffable_hood`, `ineffable_leggings`, and `ineffable_boots` item
  registrations. Their legacy stacks may disappear from worlds after updating;
  no migration into additional robe items is in scope.
- Remove obsolete translations and item-model resources for the three deleted
  items. Keep and update the `ineffable_robes` item model as needed for its
  inventory appearance.

## Rendering

Keep the existing custom `IneffableArmorLayer`, shader pipeline, model geometry,
and cloth animation. The layer will no longer inspect vanilla equipment slots
for four matching items. Instead, it will query the rendered living entity's
Curios inventory and render only when `ineffable_robes` is equipped in the
`body` slot.

One equipped robe renders the full Ineffable outfit:

- hood and head details;
- torso, shoulders, and arms;
- cloak, side cloth, and hanging lower cloth.

The model will expose independently renderable visual sections so clearance can
be applied only where armor is present:

- a non-empty helmet slot expands the hood section;
- a non-empty chest slot expands the torso, shoulders, arms, and cloak root;
- a non-empty leggings slot expands the lower cloth associated with the legs;
- a non-empty boots slot expands the lowest hanging portions where they overlap
  footwear.

Clearance will use small fixed scale/deformation constants owned by the
Ineffable model or render layer. Empty vanilla armor slots retain the current
model dimensions. Transform changes must be scoped with balanced pose-stack
operations or reset model-part scale values so one entity's armor state cannot
leak into another entity's render.

The renderer will continue to copy animation properties from its parent
humanoid model, run the existing cloth animation, configure the selected
Ineffable shader, and submit geometry through the current shader render type.

## Compatibility and Scope

- The custom layer remains attached to the same supported humanoid renderers.
- Curios inventory lookup must fail closed: missing capability, missing `body`
  handler, or an empty slot means the outfit is not rendered.
- Only the equipped body-slot robe activates the visual. A robe in ordinary
  inventory or a vanilla equipment slot does not activate it.
- Existing uncommitted model, texture, shader, gravity-shift, and related work
  in the worktree must be preserved.
- New special effects, protection, recipes, and legacy-item migration are
  explicitly deferred.

## Verification

Automated tests will cover logic that can be isolated from Minecraft rendering:

- `ineffable_robes` is recognized as the supported Curio item and exposes no
  armor attributes;
- Curios body-slot lookup distinguishes equipped, unequipped, missing-slot, and
  missing-capability cases;
- armor occupancy maps to the correct independent clearance sections;
- no armor occupancy produces the baseline clearance state.

The full project test suite and Forge compilation will run after implementation.
Client-side verification will confirm:

- the outfit appears only while the robe is equipped in the Curios body slot;
- all outfit sections render from the single item;
- each vanilla armor slot expands only its corresponding visual section;
- the shader and cloth animation still work;
- armor no longer clips through the robe at normal player poses;
- rendering one entity does not affect the next entity's model scale.
