# Ineffable Armor Circle-Grid Shader Design

## Goal

Add Nicole Vella's animated checkerboard/circle pattern as a second Ineffable
armor shader style. A client config boolean selects the circle-grid style when
`true` and the existing triangular lattice when `false`; the default is
`true`.

## Scope

The option applies to every armor piece rendered by `IneffableArmorLayer`:
hood, robes, leggings, and boots. Existing armor models, equipment checks,
texture masking, lighting, fog, cloth animation, and vertex disfiguration
remain unchanged.

The client config class will be generalized from `ClientAuthorshipConfig` to
`ClientConfig`. Its existing Counterlaw HUD option and config file name remain
stable, while the armor option is added under an `ineffable_armor` section as
`useCircleGridArmorShader = true`.

## Rendering Design

The existing `doppleganger` core shader remains the sole render type. Its
fragment shader will contain two focused pattern functions:

- the current triangular lattice, preserving the working-tree tuning;
- a circle-grid function adapted from the supplied Shadertoy source.

The armor layer reads the client boolean each render and writes an integer
shader uniform. A true value selects the circle-grid function and a false value
selects the triangle function. This keeps switching inexpensive and allows
config changes to take effect without rebuilding render types or reloading
resources.

The circle-grid implementation uses armor UV coordinates to create a square
grid. Alternating cells invert between charcoal and off-white. Four
anti-aliased circles move along cell edges, matching the source animation's
top/bottom and left/right motion. The mask texture continues to define the
armor silhouette and alpha.

Both patterns return a monochrome pattern factor that is combined by one
shared lighting and fog path. This removes duplicated color processing and
keeps both modes visually consistent with Minecraft lighting.

## Attribution

The fragment shader will retain source attribution to Nicole Vella, the
Shadertoy link, and the Creative Commons Attribution 4.0 license notice. The
project credits will also record the adaptation.

## Testing and Verification

A small pure selector type will express the boolean-to-mode contract and receive
JUnit tests proving:

- `true` selects the circle-grid mode;
- `false` selects the triangle mode.

Verification will also run Gradle resource processing, Java compilation, the
unit tests, and the full build. Shader JSON will be checked for valid syntax,
and the final diff will be reviewed to ensure unrelated working-tree changes
were not overwritten or staged.

