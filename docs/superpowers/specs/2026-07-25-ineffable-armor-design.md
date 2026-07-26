# Ineffable Armor Set Design

## Objective

Convert the supplied `IneffebleRobes.bbmodel` humanoid model into a complete
four-piece, three-dimensional armor set for MnAGnosis on Forge 1.20.1. The
initial release is purely wearable armor with strong Tier 6 statistics and no
set bonus or special ability.

## Public Items

Register four Epic-rarity armor items:

| Registry ID | Display name | Equipment slot |
| --- | --- | --- |
| `mnagnosis:ineffable_hood` | Ineffable Hood | Head |
| `mnagnosis:ineffable_robes` | Ineffable Robes | Chest |
| `mnagnosis:ineffable_leggings` | Ineffable Leggings | Legs |
| `mnagnosis:ineffable_boots` | Ineffable Boots | Feet |

The spelling **Ineffable** is canonical even though the supplied source
filenames use `Ineffeble`.

The items will appear in the existing MnAGnosis creative tab. No recipes,
loot sources, repair ingredient, advancements, abilities, or set bonuses are
part of this change.

## Armor Material

Add an `INEFFABLE` armor material with:

- 4 armor for the hood.
- 10 armor for the robes.
- 7 armor for the leggings.
- 4 armor for the boots.
- 4 toughness.
- 0.15 knockback resistance.
- High Tier 6 durability.
- Epic item rarity.
- A conventional armor equip sound.
- No repair ingredient until a future progression source is designed.

The material is intentionally stronger than Netherite because it is a
post-Odin, Tier 6 reward set.

## Model Architecture

Create a Forge 1.20.1 `HumanoidModel` derived from the supplied Blockbench
geometry and the relevant structural patterns in Hemomancy's Silent Archon
armor.

The model will expose four separately baked layer locations. Each instance
will know its equipment slot and render only the geometry owned by that slot:

- **Head:** base head and all segmented hood geometry.
- **Chest:** torso, shoulders, arms, cloak, rear cloth, and side cloth.
- **Legs:** upper portions of both leg coverings.
- **Feet:** lower portions of both leg coverings.

The supplied model represents each leg with a full-height cube. The Forge
model will split those cubes into upper-leg and lower-leg sections so the
leggings and boots remain visually continuous without overlapping or
rendering twice.

The supplied model's 128×128 UV layout will remain authoritative. Its embedded
texture will be extracted as the model's runtime UV and alpha mask.

## Animation

The armor will copy pose state from the owning humanoid renderer so it follows
normal head, body, arm, and leg movement for walking, crouching, riding,
swimming, and other vanilla poses.

The segmented cloak, rear cloth, and side cloth will receive restrained
procedural movement adapted to Forge 1.20.1:

- Slow idle movement prevents rigid cloth.
- Walking adds modest movement proportional to limb swing.
- Adjacent cloth segments use phase offsets to avoid moving as one solid slab.

These animations are presentation-only and require no synchronized entity
state.

## Rendering

Create a dedicated `IneffableArmorLayer` rather than relying on Minecraft's
standard armor texture renderer.

`IneffableArmorItem` will return MnAGnosis's existing empty armor model on the
client. This suppresses vanilla armor geometry and prevents double rendering.
The dedicated layer will:

1. Inspect all four equipment slots.
2. Select the matching baked Ineffable model.
3. Copy the parent humanoid pose into that model.
4. Render only the selected slot's geometry.
5. Use MnAGnosis's existing Doppleganger render type and shader.

The embedded Blockbench texture supplies UV and transparency information only.
The Doppleganger shader supplies the visible monochrome TV-static appearance,
matching the current Primal robe treatment. No conventional colored armor
texture is displayed in this version.

Register the render layer for:

- Both player skin types.
- Armor stands.
- Zombies.
- Husks.
- Drowned.
- Skeletons.
- Strays.

This matches the humanoid coverage already anticipated by MnAGnosis's client
layer registration.

## Item Presentation

Add item model JSON files for all four registry entries. Until bespoke item
icons are supplied, each item will inherit the corresponding recognizable
vanilla armor item silhouette. This affects inventory presentation only; the
equipped armor always uses the custom model and static shader.

Add English localization for the four approved display names.

## Error Handling and Compatibility

- Missing client model layers must fail during client startup rather than
  silently falling back to duplicate vanilla armor.
- Server-side item registration and armor behavior must not reference client
  renderer classes during dedicated-server startup.
- The render layer will check both item identity and equipment slot before
  selecting model geometry.
- Existing Primal armor rendering and Truth rendering remain unchanged.
- Existing unrelated worktree changes, including armor textures and ongoing
  Ineffable faction work, remain untouched.

## Verification

Add automated GameTests that verify:

- All four item registry IDs resolve.
- Each item occupies its intended armor slot.
- All four items use the Ineffable armor material.
- Defense values are 4, 10, 7, and 4 for head, chest, legs, and feet.
- Toughness is 4 and knockback resistance is 0.15.
- Durability is higher than the existing Primal armor.
- The items have Epic rarity.

Run:

- Java compilation.
- Resource processing.
- The complete Forge GameTest server.
- A client startup smoke test sufficient to bake all four model layers and
  initialize the armor render layer.
- `git diff --check`.

Manual in-game verification will cover:

- Default and slim player alignment.
- All four pieces individually and as a complete set.
- No duplicated geometry between leggings and boots.
- Hood head tracking.
- Arm and leg movement.
- Crouching, riding, swimming, and armor-stand poses.
- Cloak and cloth animation.
- TV-static shader appearance and absence of the original colored texture.
- Rendering on each registered humanoid mob type.

