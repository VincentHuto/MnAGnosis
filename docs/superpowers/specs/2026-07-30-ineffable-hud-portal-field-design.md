# Ineffable HUD Portal Field Design

## Goal

Add a shader-driven portal and star field inside the Ineffable HUD's mana
channel so the live resource bar reads as a distinct foreground layer.

## Visual Contract

The complete mana channel contains a continuously animated field, including the
currently unfilled portion. The field remains dark and restrained:

- near-black void as the dominant color;
- sparse white stars for crisp highlights;
- cyan stars and faint cyan depth bands for Ineffable identity;
- slow inward drift and layered parallax rather than fast lateral scrolling;
- no purple, magenta, rainbow, or vanilla End Portal palette.

The animation must remain visible at the compact HUD scale without competing
with the frame, mana rails, paradox layer, cap, or text. White and cyan peaks
remain isolated, and most pixels stay near black.

## Geometry and Layer Order

The shader quad uses the existing concept source-space channel:

- X: `80`;
- Y: `52`;
- width: `790`;
- height: `54`.

It renders within the frame's existing 976×158 source-space pose, which already
scales to 153×25 display pixels and inherits the whole-HUD perspective.

The exact frame composition order is:

1. concept base texture;
2. portal-field shader quad;
3. disruption texture, when active;
4. live mana texture;
5. live mana cap;
6. paradox texture;
7. XP texture.

Contradiction marks and the miniature cubes remain later outer-HUD passes.
This order keeps the field behind every dynamic frame and resource indicator.

## Shader

A new core shader named `ineffable_hud_portal` uses
`DefaultVertexFormat.POSITION_TEX`. Its vertex shader passes normalized channel
UV coordinates and uses the standard `ModelViewMat` and `ProjMat` uniforms.

The fragment shader receives:

- `PortalTime`, measured in seconds from client game time plus partial tick;
- `PortalOpacity`, fixed at `0.88`;
- interpolated normalized channel UV.

It builds three deterministic procedural star layers from hashed cells. Each
layer has a distinct grid density, drift rate, brightness, and cyan/white
selection. A low-amplitude warped cyan band adds depth between the stars.
Aspect correction uses the 790:54 channel ratio so stars remain compact instead
of stretching horizontally.

The shader samples no textures and creates no framebuffer or post-processing
pass.

## Architecture

### `CoreShaders`

Register `mnagnosis:ineffable_hud_portal` through the existing
`RegisterShadersEvent` path and store its `ShaderInstance`. Provide a nullable
getter so HUD rendering can skip the effect safely during reload or after a
shader-load failure.

### `IneffableHudPortalRenderer`

A stateless renderer owns:

- the exact channel rectangle;
- conversion of game time and partial tick to seconds;
- shader-uniform configuration;
- one immediate `POSITION_TEX` quad draw;
- local blend, depth-mask, and cull state setup and restoration.

It flushes pending `GuiGraphics` work before drawing so the concept base appears
first. If the shader instance is unavailable, it returns without drawing,
leaving the existing black channel intact.

### `IneffableHudRenderer`

`drawConceptFrame` receives animation time. It draws the base, invokes the
portal renderer while the high-resolution frame pose is active, and then draws
all disruption and resource layers in their current order.

No mana calculation, clipping rule, texture, perspective transform, or cube
animation changes.

## Render-State Safety

The portal renderer:

- enables standard alpha blending;
- disables depth writes for its HUD quad;
- disables culling for the transformed quad;
- restores shader color, depth writes, culling, and blending afterward;
- emits and draws only its own four vertices.

It does not replace the global projection matrix. The active GUI pose supplies
both the compact frame scale and the whole-HUD angle.

## Testing

Automated tests cover:

- exact channel geometry;
- deterministic seconds calculation including partial tick;
- shader JSON, vertex, and fragment resources;
- matching shader attributes and required uniforms;
- `CoreShaders` registration and getter;
- portal draw order between base and disruption/resource layers;
- safe behavior when the shader is unavailable;
- unchanged mana and paradox pixel calculations.

Full verification runs the Gradle test suite, resource processing, shader
resource contract tests, and patch-hygiene checks.

## Acceptance Criteria

- A black, white, and cyan portal/star field animates across the full channel.
- The field remains strictly inside the mana-channel rectangle.
- Live mana, cap, paradox, frame disruption, and XP remain visibly above it.
- The field follows the angled HUD without detaching or flattening.
- The animation is subtle enough to preserve resource readability.
- Missing or reloading shader state falls back to the original black channel.
- No render state leaks into later HUD elements.
