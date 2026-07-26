# Primal Mote Visual Rework

## Scope

Remove the temporary Primal armor set completely and replace the Mote of Primal
Mana's flat item presentation with an animated monochrome tesseract containing
abstract shrieking faces.

The existing blue Tesseract item and Tesseract block retain their current visual
identity.

## Primal armor removal

The following four items cease to be registered:

- Primal Crown
- Primal Chestplate
- Primal Leggings
- Primal Boots

Remove their item classes, armor materials, client render layers, model layer
registrations, item models, textures, and localization entries. Remove every
creative-tab and renderer reference that would attempt to resolve those registry
objects.

Existing worlds that contain the removed registry identifiers may report them as
missing mappings. No migration or replacement item is introduced because the set
was explicitly temporary.

The Mote of Primal Mana remains registered and retains its registry identifier,
display name, stack behavior, and recipe uses.

## Shared tesseract rendering

Extract the reusable projection, edge generation, and transform logic from the
existing item tesseract renderer into a small rendering core. The original
Tesseract item continues to request its blue palette and existing effects.

The Mote of Primal Mana becomes a custom-rendered item that requests:

- a black inner structure;
- a white outer lattice and highlights;
- restrained monochrome glow;
- slower dimensional folding than the ordinary Tesseract;
- no blue tint.

The renderer must remain safe in GUI, ground, fixed-frame, first-person, and
third-person item-display contexts.

## Abstract shrieking faces

Faces are temporary formations inside the projected lattice rather than separate
heads or flat orbiting sprites.

Each face:

1. emerges from a subset of nearby lattice intersections;
2. resolves into two eye voids and an elongated open mouth;
3. stretches a short distance away from the tesseract center;
4. fragments into small square monochrome traces;
5. collapses back into the lattice.

Multiple deterministic phase offsets allow a small number of faces to form at
different times. The effect must remain legible at inventory scale, avoid rapid
full-screen flicker, and use no color to communicate its shape. The animation is
purely client-side and carries no gameplay state.

## Testing and verification

Automated tests will verify:

- no Primal armor item is present in the item registry;
- the Mote of Primal Mana is registered with the dedicated custom-rendered item
  class;
- the existing Tesseract item remains registered with its original item class;
- no remaining code or resource reference names the removed armor registry
  entries.

Verification will include a clean build, the complete Forge GameTest suite, a
client initialization smoke test for model-layer and renderer failures, and visual
inspection of the Mote in at least GUI and world/item-hand contexts.

## Out of scope

- Renaming or changing the crafting role of the Mote of Primal Mana.
- Recoloring the existing Tesseract item or block.
- Adding sounds, gameplay effects, particles visible outside item rendering, or
  server synchronization for the faces.
- Migrating removed Primal armor stacks into Ineffable armor.
