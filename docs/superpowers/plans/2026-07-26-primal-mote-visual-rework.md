# Primal Mote Visual Rework Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove every temporary Primal armor item and give the Mote of Primal Mana a monochrome tesseract renderer with abstract shrieking faces.

**Architecture:** A dedicated `PrimalMoteItem` supplies a client-only renderer. Shared tesseract projection and edge drawing move into `TesseractRenderCore`, while palette/effect policy remains in the two item renderers so the existing Tesseract stays blue and the mote becomes monochrome.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1, Mana and Artifice 3.1.11, Forge GameTests, Mojang item rendering APIs.

## Global Constraints

- Keep `mnagnosis:primal_mote`, its display name, stack behavior, and recipe uses.
- Keep the existing Tesseract item and block blue and otherwise unchanged.
- Remove all four Primal armor registry entries without migration.
- Faces are abstract lattice formations, client-only, monochrome, and non-gameplay.
- Do not touch `marrow_crown_layer_1.png` or `.codex-remote-attachments/`.

---

### Task 1: Registry contract and Primal armor removal

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/item/armor/EnumArmorTiers.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java`
- Delete: `src/main/java/com/vincenthuto/mnagnosis/common/item/armor/PrimalArmorItem.java`

**Interfaces:**
- Preserves: `RegistryObject<Item> ItemRegistry.primal_mote` under its existing identifier.
- Preserves: `RegistryObject<Item> ItemRegistry.tesseract` backed by `TesseractItem`.

- [ ] **Step 1: Write the failing registry GameTest**

Add a GameTest that asserts `primal_crown`, `primal_robes`, `primal_legwraps`, and
`primal_boots` are absent from `ForgeRegistries.ITEMS`; asserts `primal_mote` remains
registered; and asserts `tesseract` is still a `TesseractItem`.

- [ ] **Step 2: Run the GameTest suite and verify RED**

Run: `.\gradlew.bat runGameTestServer`

Expected: the new test fails because Primal armor entries still exist.

- [ ] **Step 3: Remove armor registrations and material references**

Delete the four armor `RegistryObject` fields and the `PrimalArmorItem` import from
`ItemRegistry`. Remove `PRIMAL_CROWN` from `EnumArmorTiers`. Keep `primal_mote`
registered under the same identifier and current class until Task 2.

- [ ] **Step 4: Run the GameTest suite and verify GREEN**

Run: `.\gradlew.bat runGameTestServer`

Expected: the registry contract test and all existing required tests pass.

- [ ] **Step 5: Commit**

Commit message: `refactor: remove temporary Primal armor items`

---

### Task 2: Shared tesseract core and monochrome mote renderer

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/item/PrimalMoteItem.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/render/item/TesseractRenderCore.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/render/item/PrimalMoteItemRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/render/item/TesseractItemRenderer.java`
- Modify: `src/main/resources/assets/mnagnosis/models/item/primal_mote.json`

**Interfaces:**
- Produces: `RegistryObject<Item> ItemRegistry.primal_mote` backed by `PrimalMoteItem`.
- `TesseractRenderCore.project(float angleXw, float angleYz)` returns the 16 projected vertices.
- `TesseractRenderCore.renderEdges(...)` draws the stable 32-edge lattice with caller-provided inner and outer colors.
- `PrimalMoteItemRenderer` draws the monochrome lattice and three phase-offset abstract face formations.

- [ ] **Step 1: Extend the registry test and verify RED**

Add an assertion that `ItemRegistry.primal_mote.get()` is a `PrimalMoteItem`.

Run: `.\gradlew.bat compileJava`

Expected: compilation fails because `PrimalMoteItem` does not exist.

- [ ] **Step 2: Add `PrimalMoteItem`**

Create an `Item` subclass whose `initializeClient` returns one
`PrimalMoteItemRenderer` through `IClientItemExtensions`. Change only the
`primal_mote` constructor in `ItemRegistry` to instantiate this class.

- [ ] **Step 3: Extract the shared projection core**

Move the immutable 4D vertices, edge indices, two-plane rotation, and perspective
projection out of `TesseractItemRenderer`. Preserve the existing projection distance
of `2.5F` and the 32-edge topology.

- [ ] **Step 4: Preserve the original blue renderer**

Rewrite `TesseractItemRenderer` as a thin caller of the shared core using its existing
cyan-to-blue depth gradient, scale `0.25F`, and rotation periods of 10 and 7 seconds.

- [ ] **Step 5: Implement the mote renderer**

Render a slower monochrome tesseract using black inner lines, white outer lines, and
high-contrast connecting edges. Use three deterministic face phases. During each
active phase, derive eye and mouth line segments from the projected lattice, stretch
them radially, and end the phase with several small square/line fragments collapsing
back toward the center.

- [ ] **Step 6: Enable the custom item model**

Set `primal_mote.json` to use the `builtin/entity` parent so Forge invokes the custom
renderer in GUI, hand, ground, and fixed display contexts.

- [ ] **Step 7: Compile and run the GameTest suite**

Run: `.\gradlew.bat compileJava`

Run: `.\gradlew.bat runGameTestServer`

Expected: compilation succeeds and every required GameTest passes.

- [ ] **Step 8: Commit**

Commit message: `feat: render the Primal Mote as a haunted tesseract`

---

### Task 3: Delete obsolete client assets and verify the final package

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java`
- Delete: `src/main/java/com/vincenthuto/mnagnosis/client/event/PrimalArmorLayer.java`
- Delete: `src/main/java/com/vincenthuto/mnagnosis/client/render/item/PrimalBootModel.java`
- Delete: `src/main/java/com/vincenthuto/mnagnosis/client/render/item/PrimalCrownModel.java`
- Delete: `src/main/java/com/vincenthuto/mnagnosis/client/render/item/PrimalHeadModel.java`
- Delete: `src/main/java/com/vincenthuto/mnagnosis/client/render/item/PrimalLegModel.java`
- Delete: `src/main/java/com/vincenthuto/mnagnosis/client/render/item/PrimalRobeModel.java`
- Delete: `src/main/resources/assets/mnagnosis/models/item/primal_boots.json`
- Delete: `src/main/resources/assets/mnagnosis/models/item/primal_chestplate.json`
- Delete: `src/main/resources/assets/mnagnosis/models/item/primal_crown.json`
- Delete: `src/main/resources/assets/mnagnosis/models/item/primal_leggings.json`
- Delete: corresponding Primal armor item and armor textures
- Modify: `src/main/resources/assets/mnagnosis/lang/en_us.json`

**Interfaces:**
- Client layer registration retains only currently registered armor sets.
- Final JAR contains no removed Primal armor registry names or assets.

- [ ] **Step 1: Remove client registrations and obsolete files**

Remove all Primal layer definitions and `PrimalArmorLayer` attachment calls from
`ClientEvents`. Delete the renderer/model classes and all armor-only item models and
textures. Remove the four armor localization keys while preserving
`item.mnagnosis.primal_mote`.

- [ ] **Step 2: Scan for stale references**

Run:
`rg -n "primal_(crown|robes|legwraps|boots|chestplate|leggings)|Primal(Armor|Crown|Head|Robe|Leg|Boot)" src/main`

Expected: no matches.

- [ ] **Step 3: Run full verification**

Run: `.\gradlew.bat clean build`

Run: `.\gradlew.bat runGameTestServer`

Launch `.\gradlew.bat runClient`, wait for sound-engine and texture-atlas
initialization, confirm no mixin/model/renderer failures, then stop only the launched
process tree.

- [ ] **Step 4: Inspect the JAR**

Verify the JAR contains `PrimalMoteItem`, `PrimalMoteItemRenderer`,
`TesseractRenderCore`, and `primal_mote.json`, and contains none of the removed armor
classes or assets.

- [ ] **Step 5: Commit**

Commit message: `chore: remove obsolete Primal armor assets`
