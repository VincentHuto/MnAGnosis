# Dedicated Fractal Item Entity Design

## Goal

Replace dropped-fractal presentation hooks into Minecraft's shared
`ItemEntityRenderer` with one MnAGnosis-owned item entity and renderer.
The new boundary must let each fractal item select presentation traits
without changing ordinary dropped items or duplicating vanilla item
gameplay.

## Scope

The dedicated entity applies to these six item registrations:

- `mnagnosis:primal_mote`
- `mnagnosis:kochian_star`
- `mnagnosis:apollonian_trap`
- `mnagnosis:tesseract`
- `mnagnosis:tesseract_block`
- `mnagnosis:mengerian_topology`

Future fractal items can opt in through the same item base classes.

The initial shared presentation has no vertical bobbing and no animated
rotation. Existing item shaders, palettes, GUI rendering, held rendering,
placed-block rendering, recipes, and gameplay effects remain unchanged.

## Architecture

### Fractal item entity

Register one `mnagnosis:fractal_item` entity type whose implementation
extends `ItemEntity`. It stores and synchronizes its `ItemStack` through
the inherited item-entity data and retains inherited gravity, collision,
pickup, merge, owner, lifespan, damage, portal, and despawn behavior.

Creation uses Forge's custom item-entity hook rather than a world-join
replacement event. The replacement entity copies the source drop's
position, velocity, pickup delay, owner/thrower state where accessible,
age/lifespan state required by Forge, and the complete stack. Creating a
custom entity must not create a second drop.

The entity must support normal save/load and multiplayer spawning under
its registered type. It must remain an `ItemEntity` so existing systems
that intentionally find item entities, including MnAGnosis gravity
effects, continue to recognize it.

### Item opt-in

Add two reusable item bases:

- `FractalItem`, extending `Item`
- `FractalBlockItem`, extending `BlockItem`

They own the Forge custom-entity hook and expose a presentation-traits
method. Existing fractal item classes move to the appropriate base:

- `PrimalMoteItem`, `KochianStarItem`, `ApollonianTrapItem`, and the
  handheld `TesseractItem` extend `FractalItem`.
- `TesseractBlockItem` and `MengerianTopologyItem` extend
  `FractalBlockItem`.

The existing client-extension methods and their BEWLR instances stay on
the concrete item classes.

### Presentation traits

Use an immutable `FractalItemEntityTraits` value supplied by the item for
the current stack. Its initial fields are:

- bob amplitude
- bob angular speed
- rotation angular speed
- fixed yaw
- render scale
- vertical offset
- lighting mode

The default profile has zero bob amplitude, zero bob speed, zero rotation
speed, fixed yaw zero, neutral scale, neutral vertical offset, and normal
world lighting. Traits are code-defined and can be overridden per item;
they are not persisted separately because the item stack is the source of
truth.

Invalid non-finite values are rejected when a profile is constructed.
Scale must be positive. An unrecognized or non-provider stack falls back
to the static default profile.

### Fractal item entity renderer

Register a renderer specifically for `mnagnosis:fractal_item`. It renders
one copy of the entity's stack with `ItemDisplayContext.GROUND`, allowing
Minecraft's `ItemRenderer` to reach each existing custom BEWLR and shader.

The renderer applies, in order:

1. the stable ground-height transform,
2. the trait vertical offset,
3. optional trait bobbing,
4. the fixed yaw plus optional trait rotation,
5. trait scale,
6. normal item rendering.

With the default traits, the entity has a fixed position and orientation.
It does not use vanilla `ItemEntity.getSpin` or vanilla sine-wave bobbing.
The renderer does not alter shader time, so the fractals themselves keep
animating.

The renderer returns the normal item texture atlas location and preserves
the existing item model/BEWLR render path. An empty stack is skipped
safely.

## Lifecycle and Data Flow

1. Minecraft drops an opted-in fractal stack.
2. Forge calls the item's custom-entity hook.
3. The shared item base creates a `FractalItemEntity` and transfers the
   original drop state once.
4. The server tracks, saves, merges, damages, and allows pickup through
   inherited `ItemEntity` behavior.
5. The client receives the registered entity and synchronized stack.
6. `FractalItemEntityRenderer` queries the stack's item for its traits.
7. The renderer applies those transforms and sends the stack through the
   existing item renderer and fractal shader.

Ordinary items never enter this path.

## Removal of the Temporary Mixin

Delete `ItemEntityRendererMixin`, remove it from
`mnagnosis.mixins.json`, and remove the temporary
`ApollonianTrapEntitySpin` helper and its tests. No mixin may target
`ItemEntityRenderer` for fractal motion after this change.

## Failure Handling

- Empty-stack entities render nothing and retain safe vanilla lifecycle
  behavior.
- A stack whose item does not expose fractal traits uses the default
  static profile.
- Invalid trait construction fails immediately with
  `IllegalArgumentException`.
- If an item entity cannot be created, Forge falls back only where its
  custom-entity contract permits; tests must ensure all six registered
  fractal items create the intended type.
- Client-only renderer classes remain out of common entity and item
  construction paths.

## Verification

Automated coverage must include:

- default traits produce zero bob and zero rotation;
- invalid scale and non-finite values are rejected;
- all six scoped item registrations opt into the custom entity;
- an ordinary item does not opt in;
- the entity factory transfers stack, position, velocity, pickup delay,
  and ownership/lifecycle state supported by the API;
- save/load preserves the registered entity and complete item stack;
- pickup and merging retain normal `ItemEntity` behavior;
- the client renderer is registered for the new type;
- the temporary `ItemEntityRenderer` mixin is absent;
- focused renderer contracts for the existing fractal shaders continue
  to pass.

Runtime verification must spawn at least one regular fractal item and one
fractal block item, confirm both use `mnagnosis:fractal_item`, confirm
neither bobs nor rotates, and confirm both can be picked up normally.

## Non-Goals

- Rewriting any fractal shader or palette.
- Changing placed Tesseract or Mengerian block entities.
- Adding new gameplay physics to fractal drops in this change.
- Making traits data-pack or NBT configurable.
- Replacing ordinary Minecraft item entities.
