# Ineffable HUD Texture Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the code-drawn Ineffable mana HUD with a crisp layered texture atlas that progresses from Contained to Desynchronized and includes an integrated badge cradle.

**Architecture:** Introduce a pure `IneffableHudAtlas` layout class that owns atlas regions and paradox-state selection. Store all stable frame, disruption, badge, mana, paradox, and XP artwork in the existing 256×256 PNG; keep the renderer responsible only for selecting/cropping texture layers and drawing genuinely dynamic content.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1 `GuiGraphics`, PNG resources, JUnit 5, Gradle.

## Global Constraints

- Keep `assets/mnagnosis/textures/mna/ineffable_resource_bars.png` at exactly 256×256 pixels.
- Keep the provider-compatible stable frame at atlas origin `(0, 0)` with size 153×16.
- Keep the live mana/paradox channel at 121×6 pixels.
- Keep HUD placement unchanged: content X offset 14, badge X 14, and frame X 34.
- Keep the existing badge item and centered magic-level text.
- Keep resource thresholds and gameplay semantics unchanged: 0.20 lattice, 0.45 local inversion, and 0.80 contradiction.
- Use only hard-edged transparent, near-black `#050505`, white `#F7F7F7`, gray `#898989`, and sparse cyan `#00B8D4` pixels in the atlas.
- Do not introduce antialiasing, gradients, or texture filtering.
- Do not refactor unrelated faction, casting-resource, or authorship behavior.

## File Map

- Create `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudAtlas.java`: atlas dimensions, sprite regions, and paradox-state selection.
- Modify `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java`: layered blits and dynamic-only drawing.
- Modify `src/main/resources/assets/mnagnosis/textures/mna/ineffable_resource_bars.png`: final runtime artwork.
- Create `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudAtlasTest.java`: atlas-coordinate and state-selection tests.
- Create `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudTextureContractTest.java`: PNG palette, occupancy, and renderer source contract.
- Modify `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`: update the in-game contract from code geometry to textured layers.

---

### Task 1: Define the Atlas Layout and State Contract

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudAtlas.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudAtlasTest.java`

**Interfaces:**
- Consumes: A paradox ratio as `float`.
- Produces: `IneffableHudAtlas.Sprite(int u, int v, int width, int height)`, named sprite constants, and `IneffableHudAtlas.frameState(float)`.

- [ ] **Step 1: Write the failing atlas-layout tests**

Create `IneffableHudAtlasTest` with exact region and threshold assertions:

```java
package com.vincenthuto.mnagnosis.client.authorship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IneffableHudAtlasTest {

    @Test
    void spritesStayInsideTheProviderCompatibleAtlas() {
        assertEquals(256, IneffableHudAtlas.ATLAS_WIDTH);
        assertEquals(256, IneffableHudAtlas.ATLAS_HEIGHT);
        assertEquals(new IneffableHudAtlas.Sprite(0, 0, 153, 16),
                IneffableHudAtlas.FRAME_BASE);
        assertEquals(new IneffableHudAtlas.Sprite(0, 16, 153, 16),
                IneffableHudAtlas.FRAME_LATTICE);
        assertEquals(new IneffableHudAtlas.Sprite(0, 32, 153, 16),
                IneffableHudAtlas.FRAME_LOCAL_INVERSION);
        assertEquals(new IneffableHudAtlas.Sprite(0, 48, 153, 16),
                IneffableHudAtlas.FRAME_CONTRADICTION);
        assertEquals(new IneffableHudAtlas.Sprite(160, 0, 20, 20),
                IneffableHudAtlas.BADGE_CRADLE);
        assertEquals(new IneffableHudAtlas.Sprite(0, 64, 121, 6),
                IneffableHudAtlas.MANA_RAILS);
        assertEquals(new IneffableHudAtlas.Sprite(122, 64, 1, 6),
                IneffableHudAtlas.MANA_CAP);
        assertEquals(new IneffableHudAtlas.Sprite(0, 70, 121, 6),
                IneffableHudAtlas.PARADOX_LATTICE);
        assertEquals(new IneffableHudAtlas.Sprite(0, 76, 121, 1),
                IneffableHudAtlas.XP_STRIP);
        for (IneffableHudAtlas.Sprite sprite : IneffableHudAtlas.ALL_SPRITES) {
            assertEquals(true, sprite.u() >= 0 && sprite.v() >= 0);
            assertEquals(true, sprite.right() <= IneffableHudAtlas.ATLAS_WIDTH);
            assertEquals(true, sprite.bottom() <= IneffableHudAtlas.ATLAS_HEIGHT);
        }
    }

    @Test
    void paradoxSelectsProgressivelyDisruptedFrames() {
        assertEquals(IneffableHudAtlas.FrameState.CONTAINED,
                IneffableHudAtlas.frameState(Float.NaN));
        assertEquals(IneffableHudAtlas.FrameState.CONTAINED,
                IneffableHudAtlas.frameState(0.19F));
        assertEquals(IneffableHudAtlas.FrameState.LATTICE,
                IneffableHudAtlas.frameState(0.20F));
        assertEquals(IneffableHudAtlas.FrameState.LOCAL_INVERSION,
                IneffableHudAtlas.frameState(0.45F));
        assertEquals(IneffableHudAtlas.FrameState.CONTRADICTION,
                IneffableHudAtlas.frameState(0.80F));
    }
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run:

```powershell
.\gradlew.bat test --tests com.vincenthuto.mnagnosis.client.authorship.IneffableHudAtlasTest
```

Expected: compilation fails because `IneffableHudAtlas` does not exist.

- [ ] **Step 3: Implement the pure atlas contract**

Create `IneffableHudAtlas` with:

```java
public final class IneffableHudAtlas {
    public static final int ATLAS_WIDTH = 256;
    public static final int ATLAS_HEIGHT = 256;

    public static final Sprite FRAME_BASE = new Sprite(0, 0, 153, 16);
    public static final Sprite FRAME_LATTICE = new Sprite(0, 16, 153, 16);
    public static final Sprite FRAME_LOCAL_INVERSION =
            new Sprite(0, 32, 153, 16);
    public static final Sprite FRAME_CONTRADICTION =
            new Sprite(0, 48, 153, 16);
    public static final Sprite BADGE_CRADLE = new Sprite(160, 0, 20, 20);
    public static final Sprite MANA_RAILS = new Sprite(0, 64, 121, 6);
    public static final Sprite MANA_CAP = new Sprite(122, 64, 1, 6);
    public static final Sprite PARADOX_LATTICE =
            new Sprite(0, 70, 121, 6);
    public static final Sprite XP_STRIP = new Sprite(0, 76, 121, 1);

    public static final List<Sprite> ALL_SPRITES = List.of(
            FRAME_BASE, FRAME_LATTICE, FRAME_LOCAL_INVERSION,
            FRAME_CONTRADICTION, BADGE_CRADLE, MANA_RAILS, MANA_CAP,
            PARADOX_LATTICE, XP_STRIP
    );

    private IneffableHudAtlas() {
    }

    public static FrameState frameState(float paradoxRatio) {
        if (!Float.isFinite(paradoxRatio) || paradoxRatio < 0.20F) {
            return FrameState.CONTAINED;
        }
        if (paradoxRatio < 0.45F) {
            return FrameState.LATTICE;
        }
        if (paradoxRatio < 0.80F) {
            return FrameState.LOCAL_INVERSION;
        }
        return FrameState.CONTRADICTION;
    }

    public record Sprite(int u, int v, int width, int height) {
        public int right() {
            return u + width;
        }

        public int bottom() {
            return v + height;
        }
    }

    public enum FrameState {
        CONTAINED,
        LATTICE,
        LOCAL_INVERSION,
        CONTRADICTION
    }
}
```

Include `java.util.List` and the project package declaration.

- [ ] **Step 4: Run the focused test and verify it passes**

Run:

```powershell
.\gradlew.bat test --tests com.vincenthuto.mnagnosis.client.authorship.IneffableHudAtlasTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit the atlas contract**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudAtlas.java src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudAtlasTest.java
git commit -m "test: define Ineffable HUD atlas contract"
```

---

### Task 2: Paint and Validate the HUD Texture Atlas

**Files:**
- Modify: `src/main/resources/assets/mnagnosis/textures/mna/ineffable_resource_bars.png`
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudTextureContractTest.java`

**Interfaces:**
- Consumes: The exact `IneffableHudAtlas.Sprite` regions from Task 1.
- Produces: A 256×256 nearest-neighbor PNG whose named regions contain the runtime artwork.

- [ ] **Step 1: Write the failing PNG contract tests**

Create a JUnit test that loads the asset with `ImageIO.read` and checks:

```java
private static final Path ATLAS = Path.of(
        "src/main/resources/assets/mnagnosis/textures/mna/"
                + "ineffable_resource_bars.png"
);
private static final Set<Integer> PALETTE = Set.of(
        0x00000000,
        0xFF050505,
        0xFFF7F7F7,
        0xFF898989,
        0xFF00B8D4
);

@Test
void atlasContainsEveryRequiredTexturedLayer() throws IOException {
    BufferedImage atlas = ImageIO.read(ATLAS.toFile());
    assertEquals(256, atlas.getWidth());
    assertEquals(256, atlas.getHeight());
    for (IneffableHudAtlas.Sprite sprite : IneffableHudAtlas.ALL_SPRITES) {
        assertTrue(hasOpaquePixel(atlas, sprite), () -> "empty " + sprite);
    }
}

@Test
void atlasUsesOnlyTheApprovedHardEdgedPalette() throws IOException {
    BufferedImage atlas = ImageIO.read(ATLAS.toFile());
    for (int y = 0; y < atlas.getHeight(); y++) {
        for (int x = 0; x < atlas.getWidth(); x++) {
            assertTrue(PALETTE.contains(atlas.getRGB(x, y)),
                    () -> "unexpected color at " + x + "," + y);
        }
    }
}
```

Implement `hasOpaquePixel` by scanning only the supplied sprite rectangle and
testing `(atlas.getRGB(x, y) >>> 24) != 0`.

- [ ] **Step 2: Run the PNG tests and verify they fail**

Run:

```powershell
.\gradlew.bat test --tests com.vincenthuto.mnagnosis.client.authorship.IneffableHudTextureContractTest
```

Expected: required sprite regions below row 15 are empty.

- [ ] **Step 3: Paint the atlas at native pixel resolution**

Update the PNG without resizing it. Paint these exact regions:

- `(0,0)-(152,15)`: complete Contained base frame, with black backing, white
  angular caps, one-pixel white rails, circuit branches, and a transparent
  exterior.
- `(0,16)-(152,31)`: transparent Lattice overlay containing one-pixel displaced
  upper-left and lower-right rail fragments plus sparse cyan signal pixels.
- `(0,32)-(152,47)`: transparent Local Inversion overlay containing the Lattice
  changes plus the stronger swapped rail segments from concept C.
- `(0,48)-(152,63)`: transparent Contradiction overlay containing the most
  displaced but still readable frame fragments.
- `(160,0)-(179,19)`: 20×20 square badge cradle. Use a black backing, white
  stepped outline, gray inner separator, transparent 16×16 item opening at
  local `(2,2)`, and a right-edge connector that meets the frame at X 34.
- `(0,64)-(120,69)`: mana rails with white top and bottom rows and transparent
  center.
- `(122,64)-(122,69)`: white one-pixel mana leading cap.
- `(0,70)-(120,75)`: right-aligned 3×3 white lattice cells on a five-pixel
  pitch, with transparent gaps.
- `(0,76)-(120,76)`: solid gray XP strip.

Use only the colors in `PALETTE`. Save as RGBA PNG with no indexed-color
conversion, smoothing, or resampling.

- [ ] **Step 4: Run the PNG contract test and inspect the atlas**

Run:

```powershell
.\gradlew.bat test --tests com.vincenthuto.mnagnosis.client.authorship.IneffableHudTextureContractTest
```

Expected: `BUILD SUCCESSFUL`.

Render or enlarge the atlas with nearest-neighbor scaling and verify:

- the base frame is symmetrical and clean;
- each disruption layer stays legible over the base;
- the badge opening is truly transparent;
- no sprite touches an unintended neighboring region.

- [ ] **Step 5: Commit the texture asset**

```powershell
git add src/main/resources/assets/mnagnosis/textures/mna/ineffable_resource_bars.png src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudTextureContractTest.java
git commit -m "feat: texture the Ineffable HUD layers"
```

---

### Task 3: Replace Static Geometry with Layered Texture Blits

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java`
- Modify: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudTextureContractTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Consumes: `IneffableHudAtlas` sprites and `frameState(float)`.
- Produces: The existing public `IneffableHudRenderer.render(...)`, resource-width helpers, and unchanged HUD placement.

- [ ] **Step 1: Extend the contract test to reject code-drawn structure**

Add:

```java
private static final Path RENDERER = Path.of(
        "src/main/java/com/vincenthuto/mnagnosis/client/authorship/"
                + "IneffableHudRenderer.java"
);

@Test
void rendererUsesAtlasLayersForStaticArtwork() throws IOException {
    String source = Files.readString(RENDERER);
    assertTrue(source.contains("graphics.blit("));
    assertTrue(source.contains("IneffableHudAtlas.FRAME_BASE"));
    assertTrue(source.contains("IneffableHudAtlas.BADGE_CRADLE"));
    assertTrue(source.contains("IneffableHudAtlas.MANA_RAILS"));
    assertTrue(source.contains("IneffableHudAtlas.PARADOX_LATTICE"));
    assertTrue(source.contains("IneffableHudAtlas.XP_STRIP"));
    assertFalse(source.contains("private static void drawFrame("));
    assertFalse(source.contains("private static void drawCircuitDetails("));
    assertFalse(source.contains("private static void drawDetailNode("));
}
```

- [ ] **Step 2: Run the focused contract tests and verify failure**

Run:

```powershell
.\gradlew.bat test --tests com.vincenthuto.mnagnosis.client.authorship.IneffableHudTextureContractTest
```

Expected: `rendererUsesAtlasLayersForStaticArtwork` fails because the renderer
still uses `fill(...)` geometry.

- [ ] **Step 3: Add a single sprite-blit helper**

In `IneffableHudRenderer`, add:

```java
private static void blit(
        GuiGraphics graphics,
        IneffableHudAtlas.Sprite sprite,
        int x,
        int y
) {
    graphics.blit(
            IneffableFactionRegistry.HUD_TEXTURE,
            x,
            y,
            sprite.u(),
            sprite.v(),
            sprite.width(),
            sprite.height(),
            IneffableHudAtlas.ATLAS_WIDTH,
            IneffableHudAtlas.ATLAS_HEIGHT
    );
}
```

Import `IneffableFactionRegistry`.

- [ ] **Step 4: Replace frame and badge fills with texture blits**

Keep `CONTENT_OFFSET_X = 14`, `BADGE_X = 14`, `FRAME_X = 34`,
`FRAME_Y = 6`, and `CHANNEL_X = 16`. Set the custom frame height to the
provider-compatible 16 pixels.

Replace the old geometry calls with:

```java
blit(graphics, IneffableHudAtlas.BADGE_CRADLE, BADGE_X, FRAME_Y - 2);
blit(graphics, IneffableHudAtlas.FRAME_BASE, FRAME_X, FRAME_Y);
IneffableHudAtlas.Sprite disruption = switch (state) {
    case CONTAINED -> null;
    case LATTICE -> IneffableHudAtlas.FRAME_LATTICE;
    case LOCAL_INVERSION -> IneffableHudAtlas.FRAME_LOCAL_INVERSION;
    case CONTRADICTION -> IneffableHudAtlas.FRAME_CONTRADICTION;
};
if (disruption != null) {
    blit(graphics, disruption, FRAME_X, FRAME_Y + disruptionPhase(state));
}
```

`disruptionPhase` returns zero when animation is disabled or no level exists.
When enabled it alternates between zero and one every eight game ticks, but
only for non-Contained states.

Continue rendering the badge item at `BADGE_X + 2, FRAME_Y` and center the
level text within the 20-pixel cradle.

- [ ] **Step 5: Replace resource and XP fills with cropped blits**

Mana:

```java
if (manaWidth > 0) {
    blitCropped(graphics, IneffableHudAtlas.MANA_RAILS,
            channelLeft, channelTop, 0, manaWidth);
    blit(graphics, IneffableHudAtlas.MANA_CAP,
            channelLeft + manaWidth - 1, channelTop);
}
```

Paradox:

```java
if (paradoxWidth > 0) {
    int sourceOffset = CHANNEL_WIDTH - paradoxWidth;
    blitCropped(graphics, IneffableHudAtlas.PARADOX_LATTICE,
            channelLeft + sourceOffset, channelTop,
            sourceOffset, paradoxWidth);
}
```

XP uses `XP_STRIP` cropped from source offset zero to the calculated width.
Implement `blitCropped` with the same atlas texture and dimensions as `blit`,
adding `sourceOffsetX` to the sprite U coordinate.

- [ ] **Step 6: Remove superseded code geometry**

Delete:

- `drawFrame`
- `drawMana`
- `drawParadox`
- `drawExperience`
- `drawCircuitDetails`
- `drawDetailNode`
- `drawDesynchronization`
- `DetailNode`
- the old two-state `FrameState`
- constants used only by those deleted methods

Keep `manaPixels`, `manaGeometry`, `paradoxPixels`, `overlapPixels`,
`channelRightInset`, and `resourcePixels` because they remain behavioral
contracts. Make `frameState(float)` delegate to
`IneffableHudAtlas.frameState(float)`.

Keep `fill(...)` only inside genuinely dynamic contradiction/debt drawing in
`CounterlawHudRenderer`.

- [ ] **Step 7: Update the in-game contract**

In `Tier6ProgressionGameTests`:

- change the atlas occupied-bottom assertion from `15` to at least `76`;
- retain the 256×256 and provider-origin assertions;
- change `FRAME_HEIGHT == 14` to `FRAME_HEIGHT == 16`;
- replace the old `DESYNCHRONIZED` assertion and `detailNodes` assertions with
  exact `CONTAINED`, `LATTICE`, `LOCAL_INVERSION`, and `CONTRADICTION`
  threshold assertions through `IneffableHudRenderer.frameState`;
- retain mana, paradox, overlap, inset, and placement assertions.

- [ ] **Step 8: Run the focused tests**

Run:

```powershell
.\gradlew.bat test --tests com.vincenthuto.mnagnosis.client.authorship.IneffableHudAtlasTest --tests com.vincenthuto.mnagnosis.client.authorship.IneffableHudTextureContractTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Compile production and GameTest sources**

Run:

```powershell
.\gradlew.bat compileJava compileTestJava
```

Expected: `BUILD SUCCESSFUL` with no missing renderer methods or enum values.

- [ ] **Step 10: Commit the renderer migration**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudTextureContractTest.java
git commit -m "refactor: render Ineffable HUD from texture layers"
```

---

### Task 4: Full Verification and Visual QA

**Files:**
- Modify only if verification exposes a defect in a file already listed above.

**Interfaces:**
- Consumes: The completed atlas contract, texture, and renderer.
- Produces: Verified build and visual acceptance evidence.

- [ ] **Step 1: Run the complete unit-test suite**

Run:

```powershell
.\gradlew.bat test
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run resource processing**

Run:

```powershell
.\gradlew.bat processResources
```

Expected: `BUILD SUCCESSFUL` and the atlas is copied without transformation.

- [ ] **Step 3: Inspect representative composited HUD states**

Create temporary nearest-neighbor previews by compositing:

- base frame + zero-width paradox;
- base + lattice overlay + 25% paradox;
- base + local-inversion overlay + 55% paradox;
- base + contradiction overlay + 90% paradox.

Include the badge cradle, a representative badge icon, and the mana/XP layers.
Inspect at native size and an integer 4× scale. Verify transparent seams,
channel alignment, right-to-left paradox cropping, badge/frame connection, and
readability. Do not commit temporary previews.

- [ ] **Step 4: Inspect the final diff**

Run:

```powershell
git diff --check
git status --short
git diff --stat HEAD~3..HEAD
```

Expected: no whitespace errors and only the planned HUD files plus their tests.

- [ ] **Step 5: Commit any verification-only correction**

If Step 3 exposes a defect, fix only that defect, rerun Tasks 4.1–4.3, then:

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudAtlas.java src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java src/main/resources/assets/mnagnosis/textures/mna/ineffable_resource_bars.png src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudAtlasTest.java src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudTextureContractTest.java
git commit -m "fix: polish Ineffable HUD texture alignment"
```

If no correction is needed, do not create an empty commit.
