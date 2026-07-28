# Ineffable Robes Curio Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the four-piece Ineffable armor set with one effect-free `ineffable_robes` Curio body item whose full custom outfit expands around vanilla armor by visual section.

**Architecture:** A focused `IneffableRobesItem` owns Curios body-slot eligibility without armor behavior. A client lookup reads only the Curios `body` handler, while an immutable clearance value maps vanilla armor occupancy into model-section scale state. The existing custom render layer, shader, cloth animation, and model geometry remain the rendering path.

**Tech Stack:** Java 17, Minecraft 1.20.1, Forge 47.4.0, Curios build 5066845, JUnit Jupiter 5.10.2, Gradle.

## Global Constraints

- Keep the registry ID `mnagnosis:ineffable_robes`.
- The robe is stack-size one, epic rarity, and grants no attributes, protection, durability, or special effects.
- Only the Curios `body` slot activates the visual.
- Preserve all existing unrelated uncommitted work, especially the current Ineffable model, shader, texture, and gravity-shift changes.
- Remove `ineffable_hood`, `ineffable_leggings`, and `ineffable_boots` without legacy-item migration.
- Preserve the existing shader selection and cloth animation.

---

### Task 1: Effect-Free Curios Body Item

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/item/IneffableRobesItem.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/common/item/IneffableRobesItemTest.java`
- Create: `src/main/resources/data/curios/tags/items/body.json`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java`
- Modify: `src/main/resources/META-INF/mods.toml`
- Modify: `src/main/resources/assets/mnagnosis/lang/en_us.json`
- Delete: `src/main/java/com/vincenthuto/mnagnosis/common/item/armor/IneffableArmorItem.java`
- Delete: `src/main/java/com/vincenthuto/mnagnosis/common/item/armor/EnumArmorTiers.java`
- Delete: `src/main/resources/assets/mnagnosis/models/item/ineffable_hood.json`
- Delete: `src/main/resources/assets/mnagnosis/models/item/ineffable_leggings.json`
- Delete: `src/main/resources/assets/mnagnosis/models/item/ineffable_boots.json`

**Interfaces:**
- Produces: `IneffableRobesItem extends Item implements ICurioItem`
- Produces: `IneffableRobesItem.isBodySlot(String identifier): boolean`
- Produces: `ItemRegistry.INEFFABLE_ROBES`

- [ ] **Step 1: Write the failing item tests**

```java
class IneffableRobesItemTest {
    private final IneffableRobesItem item = new IneffableRobesItem();

    @Test
    void acceptsOnlyTheCuriosBodySlot() {
        assertTrue(item.canEquip(new SlotContext("body", null, 0, false, true),
                new ItemStack(item)));
        assertFalse(item.canEquip(new SlotContext("back", null, 0, false, true),
                new ItemStack(item)));
    }

    @Test
    void hasNoArmorBehaviorOrAttributes() {
        assertFalse(ArmorItem.class.isAssignableFrom(item.getClass()));
        assertTrue(item.getDefaultAttributeModifiers(EquipmentSlot.CHEST).isEmpty());
        assertEquals(1, item.getMaxStackSize());
        assertFalse(new ItemStack(item).isDamageableItem());
    }
}
```

- [ ] **Step 2: Run the focused test and confirm RED**

Run:

```powershell
.\gradlew.bat test --tests '*IneffableRobesItemTest'
```

Expected: compilation fails because `IneffableRobesItem` does not exist.

- [ ] **Step 3: Implement the minimal Curio item**

```java
public final class IneffableRobesItem extends Item implements ICurioItem {
    public IneffableRobesItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    static boolean isBodySlot(String identifier) {
        return SlotTypePreset.BODY.getIdentifier().equals(identifier);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return isBodySlot(slotContext.identifier());
    }
}
```

Register only `INEFFABLE_ROBES` in `ItemRegistry`. During `commonSetup`, call:

```java
CuriosApi.registerCurio(ItemRegistry.INEFFABLE_ROBES.get(),
        (IneffableRobesItem) ItemRegistry.INEFFABLE_ROBES.get());
```

Add `curios` as a mandatory `BOTH` dependency after Forge, add the robe to
`data/curios/tags/items/body.json`, and remove the three obsolete item
registrations, translations, models, and armor implementation classes.

- [ ] **Step 4: Run the focused test and confirm GREEN**

Run:

```powershell
.\gradlew.bat test --tests '*IneffableRobesItemTest'
```

Expected: both tests pass.

- [ ] **Step 5: Check resource and source references**

Run:

```powershell
rg -n "INEFFABLE_(HOOD|LEGGINGS|BOOTS)|ineffable_(hood|leggings|boots)|IneffableArmorItem|EnumArmorTiers" src/main
```

Expected: only render code scheduled for Task 3 may still reference deleted
pieces; no registry, item, language, or model-resource references remain.

- [ ] **Step 6: Commit only Task 1 files**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/common/item/IneffableRobesItem.java src/test/java/com/vincenthuto/mnagnosis/common/item/IneffableRobesItemTest.java src/main/resources/data/curios/tags/items/body.json src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java src/main/resources/META-INF/mods.toml src/main/resources/assets/mnagnosis/lang/en_us.json src/main/java/com/vincenthuto/mnagnosis/common/item/armor/IneffableArmorItem.java src/main/java/com/vincenthuto/mnagnosis/common/item/armor/EnumArmorTiers.java src/main/resources/assets/mnagnosis/models/item/ineffable_hood.json src/main/resources/assets/mnagnosis/models/item/ineffable_leggings.json src/main/resources/assets/mnagnosis/models/item/ineffable_boots.json
git commit --only src/main/java/com/vincenthuto/mnagnosis/common/item/IneffableRobesItem.java src/test/java/com/vincenthuto/mnagnosis/common/item/IneffableRobesItemTest.java src/main/resources/data/curios/tags/items/body.json src/main/java/com/vincenthuto/mnagnosis/common/registry/ItemRegistry.java src/main/java/com/vincenthuto/mnagnosis/MnAGnosis.java src/main/resources/META-INF/mods.toml src/main/resources/assets/mnagnosis/lang/en_us.json src/main/java/com/vincenthuto/mnagnosis/common/item/armor/IneffableArmorItem.java src/main/java/com/vincenthuto/mnagnosis/common/item/armor/EnumArmorTiers.java src/main/resources/assets/mnagnosis/models/item/ineffable_hood.json src/main/resources/assets/mnagnosis/models/item/ineffable_leggings.json src/main/resources/assets/mnagnosis/models/item/ineffable_boots.json -m "feat: convert ineffable robes to body curio"
```

### Task 2: Curio Lookup and Armor-Clearance State

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorClearance.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableRobesCurioLookup.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorClearanceTest.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableRobesCurioLookupTest.java`

**Interfaces:**
- Produces: `IneffableArmorClearance.from(boolean helmet, boolean chest, boolean legs, boolean feet)`
- Produces: boolean accessors `helmet()`, `chest()`, `legs()`, and `feet()`
- Produces: `IneffableRobesCurioLookup.isEquipped(LivingEntity entity, Item robe): boolean`
- Produces: package-visible `containsVisibleRobe(IItemHandler stacks, List<Boolean> renders, Item robe): boolean`

- [ ] **Step 1: Write failing clearance tests**

```java
@Test
void preservesIndependentArmorSections() {
    assertEquals(new IneffableArmorClearance(true, false, true, false),
            IneffableArmorClearance.from(true, false, true, false));
}

@Test
void emptyArmorProducesBaselineState() {
    assertEquals(IneffableArmorClearance.NONE,
            IneffableArmorClearance.from(false, false, false, false));
}
```

- [ ] **Step 2: Write failing Curio stack-selection tests**

Use Forge's `ItemStackHandler` with two slots and verify:

```java
assertFalse(IneffableRobesCurioLookup.containsVisibleRobe(
        handler, List.of(true, true), robe));
handler.setStackInSlot(1, new ItemStack(robe));
assertTrue(IneffableRobesCurioLookup.containsVisibleRobe(
        handler, List.of(true, true), robe));
assertFalse(IneffableRobesCurioLookup.containsVisibleRobe(
        handler, List.of(true, false), robe));
```

Also verify an empty render-flag list fails closed rather than indexing past
its bounds.

- [ ] **Step 3: Run both focused tests and confirm RED**

Run:

```powershell
.\gradlew.bat test --tests '*IneffableArmorClearanceTest' --tests '*IneffableRobesCurioLookupTest'
```

Expected: compilation fails because both production types are absent.

- [ ] **Step 4: Implement immutable clearance mapping**

```java
public record IneffableArmorClearance(
        boolean helmet, boolean chest, boolean legs, boolean feet) {
    public static final IneffableArmorClearance NONE =
            new IneffableArmorClearance(false, false, false, false);

    public static IneffableArmorClearance from(
            boolean helmet, boolean chest, boolean legs, boolean feet) {
        return helmet || chest || legs || feet
                ? new IneffableArmorClearance(helmet, chest, legs, feet)
                : NONE;
    }
}
```

- [ ] **Step 5: Implement fail-closed body-slot lookup**

`isEquipped` must resolve `CuriosApi.getCuriosInventory(entity)`, request only
`SlotTypePreset.BODY.getIdentifier()`, then inspect the handler's equipped
stacks and corresponding render flags. `containsVisibleRobe` loops no farther
than both collections allow and returns true only for a visible matching stack.
It must not search other Curios slot types or ordinary inventory.

- [ ] **Step 6: Run both focused tests and confirm GREEN**

Run:

```powershell
.\gradlew.bat test --tests '*IneffableArmorClearanceTest' --tests '*IneffableRobesCurioLookupTest'
```

Expected: all lookup and mapping tests pass.

- [ ] **Step 7: Commit only Task 2 files**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorClearance.java src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableRobesCurioLookup.java src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorClearanceTest.java src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableRobesCurioLookupTest.java
git commit --only src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorClearance.java src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableRobesCurioLookup.java src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorClearanceTest.java src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableRobesCurioLookupTest.java -m "feat: detect equipped ineffable body curio"
```

### Task 3: Single-Item Section-Aware Outfit Rendering

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorModel.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/IneffableArmorLayer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java`

**Interfaces:**
- Consumes: `IneffableRobesCurioLookup.isEquipped(entity, ItemRegistry.INEFFABLE_ROBES.get())`
- Consumes: `IneffableArmorClearance.from(boolean, boolean, boolean, boolean)`
- Produces: `IneffableArmorModel(ModelPart root)`
- Produces: `IneffableArmorModel.applyClearance(IneffableArmorClearance clearance)`
- Produces: one `IneffableArmorModel.INEFFABLE_ROBES_LAYER`

- [ ] **Step 1: Extend clearance tests with exact scale selection**

Add pure methods to the clearance record:

```java
assertEquals(1.08F, IneffableArmorClearance.from(true, false, false, false)
        .helmetScale());
assertEquals(1.0F, IneffableArmorClearance.from(true, false, false, false)
        .chestScale());
assertEquals(1.06F, IneffableArmorClearance.from(false, false, true, false)
        .legsScale());
assertEquals(1.04F, IneffableArmorClearance.from(false, false, false, true)
        .feetScale());
```

Use named constants `HELMET_SCALE`, `CHEST_SCALE`, `LEGS_SCALE`, and
`FEET_SCALE`; each accessor returns its named constant when occupied and
`1.0F` otherwise.

- [ ] **Step 2: Run the clearance test and confirm RED**

Run:

```powershell
.\gradlew.bat test --tests '*IneffableArmorClearanceTest'
```

Expected: compilation fails because the four scale accessors are absent.

- [ ] **Step 3: Implement the scale accessors and confirm GREEN**

Run:

```powershell
.\gradlew.bat test --tests '*IneffableArmorClearanceTest'
```

Expected: all clearance tests pass.

- [ ] **Step 4: Refactor the model into independently scalable sections**

Keep the existing Blockbench-derived cubes and animation hierarchy, but:

- replace four model-layer constants with `INEFFABLE_ROBES_LAYER`;
- remove the `EquipmentSlot renderSlot` constructor argument and render switch;
- put torso cubes in a `body_shell` child of the normal empty `body` anchor;
- retain the hood under `head`;
- retain arms as independent top-level parts;
- retain `ClothBack`, `SideclothL`, and `SideclothR` as lower-cloth roots;
- use their terminal child segments plus `cloak3` as footwear-clearance parts.

`applyClearance` resets every controlled part's `xScale` and `zScale` on every
call, then assigns:

```java
setHorizontalScale(this.head, clearance.helmetScale());
setHorizontalScale(this.bodyShell, clearance.chestScale());
setHorizontalScale(this.leftArm, clearance.chestScale());
setHorizontalScale(this.rightArm, clearance.chestScale());
setHorizontalScale(this.cloak, clearance.chestScale());
setHorizontalScale(this.clothBack, clearance.legsScale());
setHorizontalScale(this.sideclothL, clearance.legsScale());
setHorizontalScale(this.sideclothR, clearance.legsScale());
setHorizontalScale(this.clothBackR3, clearance.feetScale());
setHorizontalScale(this.clothBackL3, clearance.feetScale());
setHorizontalScale(this.sideclothR3, clearance.feetScale());
setHorizontalScale(this.sideclothR6, clearance.feetScale());
setHorizontalScale(this.cloak3, clearance.feetScale());
```

Leave `yScale` at `1.0F` so cloth lengths and pivots do not drift.
`renderToBuffer` renders `head`, `body`, `leftArm`, and `rightArm` once.

- [ ] **Step 5: Make the layer Curios-aware**

Bake one robes model. At the start of `render`, return unless
`IneffableRobesCurioLookup.isEquipped` finds a visible robe in the body slot.
Build clearance from `entity.getItemBySlot(HEAD/CHEST/LEGS/FEET).isEmpty()`,
copy parent properties, animate cloth, apply clearance, configure the shader,
and render the full model once through `RenderHelper.getDopplegangerLayer()`.

- [ ] **Step 6: Register only the single model layer**

In `ClientEvents`, register only:

```java
event.registerLayerDefinition(IneffableArmorModel.INEFFABLE_ROBES_LAYER,
        IneffableArmorModel::createBodyLayer);
```

Keep existing renderer-layer attachment behavior.

- [ ] **Step 7: Compile and resolve integration errors**

Run:

```powershell
.\gradlew.bat compileJava compileTestJava
```

Expected: `BUILD SUCCESSFUL` with no missing old armor-item or model-layer
references.

- [ ] **Step 8: Run the complete automated test suite**

Run:

```powershell
.\gradlew.bat test
```

Expected: all JUnit tests pass with zero failures.

- [ ] **Step 9: Build the distributable**

Run:

```powershell
.\gradlew.bat build
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 10: Inspect the final scoped diff**

Run:

```powershell
git diff --check
rg -n "INEFFABLE_(HOOD|LEGGINGS|BOOTS)|ineffable_(hood|leggings|boots)|IneffableArmorItem|EnumArmorTiers" src/main
git status --short
```

Expected: no whitespace errors; no obsolete armor-set references; all unrelated
pre-existing work remains present.

- [ ] **Step 11: Commit only Task 3 files**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorModel.java src/main/java/com/vincenthuto/mnagnosis/client/event/IneffableArmorLayer.java src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorClearance.java src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorClearanceTest.java
git commit --only src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorModel.java src/main/java/com/vincenthuto/mnagnosis/client/event/IneffableArmorLayer.java src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java src/main/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorClearance.java src/test/java/com/vincenthuto/mnagnosis/client/render/armor/IneffableArmorClearanceTest.java -m "feat: render ineffable robes over equipped armor"
```

### Task 4: Manual Client Verification

**Files:**
- No source changes expected.

**Interfaces:**
- Consumes: completed Curio item registration, lookup, clearance, and rendering.
- Produces: recorded manual verification results in the implementation handoff.

- [ ] **Step 1: Launch the development client**

Run:

```powershell
.\gradlew.bat runClient
```

- [ ] **Step 2: Verify equipment behavior**

In a test world, obtain one `mnagnosis:ineffable_robes`. Confirm it cannot be
equipped in vanilla armor slots, can be equipped in the Curios body slot, does
not render from ordinary inventory, and disappears when removed from the body
slot.

- [ ] **Step 3: Verify model and shader behavior**

Confirm the single robe renders the hood, torso, arms, cloak, and lower cloth;
the configured shader appears; and walking/idle cloth animation remains active.

- [ ] **Step 4: Verify armor clearance combinations**

Test no armor, then helmet only, chestplate only, leggings only, boots only, and
all four vanilla armor pieces. Confirm only the corresponding visual sections
expand and no normal-pose clipping remains.

- [ ] **Step 5: Verify render-state isolation**

Place or observe two humanoid entities with different armor combinations and
confirm the second entity does not inherit the first entity's robe scaling.
