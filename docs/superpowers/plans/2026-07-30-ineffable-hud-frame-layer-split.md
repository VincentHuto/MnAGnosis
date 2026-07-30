# Ineffable HUD Frame Layer Split Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Place all structural frame pixels above the portal field and close both ends of every nonempty mana rail.

**Architecture:** Mechanically split the original concept base into complementary backing and foreground PNGs whose union reconstructs the source exactly. Runtime composition draws the portal between those layers, then reuses the existing mana-cap texture at a fixed left endpoint and the current calculated right endpoint.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1 GUI rendering, Java ImageIO/JUnit 5, PowerShell System.Drawing asset generation, Gradle.

## Global Constraints

- Source and derived textures remain 976×158 RGBA PNGs.
- Backing receives only opaque `#050505` pixels inside `(80, 52, 790, 54)`.
- Foreground receives every other original base pixel.
- Backing plus foreground must reconstruct the original base pixel-for-pixel.
- Runtime order is backing, portal, foreground, disruption, mana, left cap, right cap, paradox, XP.
- Both caps render only when `manaWidth > 0`.
- Left-cap X is `CHANNEL_X`; right-cap X remains `manaCapX(manaWidth)`.
- Preserve the current uncommitted `CHANNEL_WIDTH - 30` portal trim.
- Preserve the current uncommitted resource-layer `Z + 1` translation.
- Do not stage or overwrite unrelated pre-existing formatting changes.

---

### Task 1: Generate and verify complementary frame layers

**Files:**
- Create: `scripts/generate_ineffable_hud_frame_layers.ps1`
- Create: `src/main/resources/assets/mnagnosis/textures/mna/ineffable_hud_concept_backing.png`
- Create: `src/main/resources/assets/mnagnosis/textures/mna/ineffable_hud_concept_frame.png`
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudFrameLayersTest.java`

**Interfaces:**
- Consumes: `ineffable_hud_concept_base.png`.
- Produces: complementary backing and foreground frame textures.

- [ ] **Step 1: Write the failing pixel contract**

Load all three PNGs and assert:

```java
assertEquals(976, backing.getWidth());
assertEquals(158, backing.getHeight());
assertEquals(976, frame.getWidth());
assertEquals(158, frame.getHeight());
```

For every pixel, assert exactly one of these contracts:

```java
if (insideChannel && baseArgb == 0xFF050505) {
    assertEquals(baseArgb, backingArgb);
    assertEquals(0, frameArgb >>> 24);
} else {
    assertEquals(0, backingArgb >>> 24);
    assertEquals(baseArgb, frameArgb);
}
```

Also count `41_713` opaque backing pixels and `947` opaque foreground pixels
inside the channel.

- [ ] **Step 2: Run the focused test and verify RED**

```powershell
.\gradlew.bat test --tests "*IneffableHudFrameLayersTest"
```

Expected: FAIL because both derived PNGs are absent.

- [ ] **Step 3: Add and run the deterministic generator**

Create a PowerShell script that loads the base with `System.Drawing.Bitmap`,
iterates every pixel, applies the exact branch above, and saves both outputs as
PNG without resizing or filtering:

```powershell
$inside = $x -ge 80 -and $x -lt 870 -and $y -ge 52 -and $y -lt 106
$isDark = $color.A -eq 255 -and $color.R -eq 5 `
        -and $color.G -eq 5 -and $color.B -eq 5
if ($inside -and $isDark) {
    $backing.SetPixel($x, $y, $color)
    $frame.SetPixel($x, $y, [Drawing.Color]::Transparent)
} else {
    $backing.SetPixel($x, $y, [Drawing.Color]::Transparent)
    $frame.SetPixel($x, $y, $color)
}
```

Run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File `
    .\scripts\generate_ineffable_hud_frame_layers.ps1
```

- [ ] **Step 4: Run the focused test and verify GREEN**

```powershell
.\gradlew.bat test --tests "*IneffableHudFrameLayersTest"
```

Expected: PASS.

- [ ] **Step 5: Commit the generated layer contract**

```powershell
git add scripts/generate_ineffable_hud_frame_layers.ps1 src/main/resources/assets/mnagnosis/textures/mna/ineffable_hud_concept_backing.png src/main/resources/assets/mnagnosis/textures/mna/ineffable_hud_concept_frame.png src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudFrameLayersTest.java
git commit -m "feat: split Ineffable HUD frame layers"
```

### Task 2: Composite the split frame and dual mana caps

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudConcept.java`
- Modify carefully: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java`
- Modify: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudConceptTest.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudFrameCompositionTest.java`

**Interfaces:**
- Produces: `backingTexture()`, `frameTexture()`, and `leftManaCapX(int)`.
- Consumes the existing `manaCapTexture()` and `manaCapX(int)`.

- [ ] **Step 1: Write the failing cap and composition tests**

Add:

```java
assertEquals(-1, IneffableHudConcept.leftManaCapX(0));
assertEquals(80, IneffableHudConcept.leftManaCapX(1));
assertEquals(80, IneffableHudConcept.leftManaCapX(790));
```

Read `IneffableHudRenderer.java` and verify the first occurrences satisfy:

```java
assertTrue(backingIndex >= 0);
assertTrue(portalIndex > backingIndex);
assertTrue(frameIndex > portalIndex);
assertTrue(disruptionIndex > frameIndex);
assertTrue(manaIndex > disruptionIndex);
assertTrue(leftCapIndex > manaIndex);
assertTrue(rightCapIndex > leftCapIndex);
```

Extend the concept texture list to include both derived assets.

- [ ] **Step 2: Run focused tests and verify RED**

```powershell
.\gradlew.bat test --tests "*IneffableHudConceptTest" --tests "*IneffableHudFrameCompositionTest"
```

Expected: FAIL because the new accessors, left-cap calculation, and runtime
composition do not exist.

- [ ] **Step 3: Add concept accessors and left-cap geometry**

Add:

```java
public static ResourceLocation backingTexture() {
    return texture("ineffable_hud_concept_backing.png");
}

public static ResourceLocation frameTexture() {
    return texture("ineffable_hud_concept_frame.png");
}

public static int leftManaCapX(int manaWidth) {
    return manaWidth > 0 ? CHANNEL_X : -1;
}
```

- [ ] **Step 4: Update frame composition without replacing user edits**

Replace only the runtime base/portal sequence with:

```java
blitFull(graphics, IneffableHudConcept.backingTexture());
IneffableHudPortalRenderer.render(graphics, animationTicks);
blitFull(graphics, IneffableHudConcept.frameTexture());
```

Replace the right-only cap call with `blitManaCaps`. That helper returns for
zero mana, then blits the shared cap texture first at `leftManaCapX(manaWidth)`
and second at `manaCapX(manaWidth)`.

Retain `IneffableHudPortalRenderer.WIDTH = CHANNEL_WIDTH - 30` and
`graphics.pose().translate(0, 0, 1)`.

- [ ] **Step 5: Run focused tests and verify GREEN**

```powershell
.\gradlew.bat test --tests "*IneffableHudFrame*Test" --tests "*IneffableHudConceptTest" --tests "*IneffableHudPortalRendererTest"
```

Expected: PASS.

- [ ] **Step 6: Run full verification**

```powershell
.\gradlew.bat test processResources
git diff --check
```

Expected: `BUILD SUCCESSFUL`. Patch hygiene may still report only pre-existing
working-tree formatting; distinguish that from new files and new hunks.

- [ ] **Step 7: Commit only newly authored files and non-overlapping hunks**

Stage the generator, textures, tests, concept accessors, and renderer feature
hunks. Leave unrelated pre-existing formatting changes unstaged. If the
renderer hunks cannot be isolated safely, leave that file unstaged and report
the preserved working-tree state rather than staging unrelated user work.
