# Dedicated Fractal Item Entity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give all six fractal item registrations one dedicated dropped-item entity with configurable presentation traits and static default motion.

**Architecture:** `FractalItemEntity` inherits vanilla/Forge `ItemEntity` gameplay and is created through Forge's `hasCustomEntity`/`createEntity` item hooks. A dedicated `FractalItemEntityRenderer` applies item-provided immutable traits before forwarding the stack through Minecraft's `ItemRenderer`, preserving every existing BEWLR and shader.

**Tech Stack:** Java 17, Minecraft 1.20.1 official mappings, Forge 47.4.0, Forge registries, Forge custom item-entity hooks, JUnit 5, Forge GameTest, Mixin removal.

## Global Constraints

- Apply the entity to `primal_mote`, `kochian_star`, `apollonian_trap`, `tesseract`, `tesseract_block`, and `mengerian_topology`.
- Shared defaults are zero vertical bobbing and zero animated rotation.
- Preserve vanilla/Forge gravity, collision, pickup, merging, ownership, lifespan, damage, serialization, and multiplayer tracking.
- Existing fractal shaders, palettes, GUI/hand transforms, and placed block renderers must remain unchanged.
- Ordinary items must continue using vanilla `ItemEntity`.
- Do not commit implementation changes automatically from the existing dirty worktree; commit commands below are handoff suggestions only.

## File Structure

**Create**

- `src/main/java/com/vincenthuto/mnagnosis/common/entity/item/FractalItemEntityTraits.java`: validated immutable presentation profile.
- `src/main/java/com/vincenthuto/mnagnosis/common/item/FractalEntityItem.java`: trait-provider contract shared by item and block-item bases.
- `src/main/java/com/vincenthuto/mnagnosis/common/item/FractalItem.java`: Forge custom-entity hook for ordinary items.
- `src/main/java/com/vincenthuto/mnagnosis/common/item/FractalBlockItem.java`: Forge custom-entity hook for block items.
- `src/main/java/com/vincenthuto/mnagnosis/common/entity/item/FractalItemEntity.java`: registered `ItemEntity` subclass and state-copy factory.
- `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/FractalItemEntityRenderer.java`: no-bob/no-spin dropped renderer.
- `src/test/java/com/vincenthuto/mnagnosis/common/entity/item/FractalItemEntityTraitsTest.java`: trait validation and motion behavior.
- `src/test/java/com/vincenthuto/mnagnosis/common/entity/item/FractalItemOptInContractTest.java`: six-item opt-in and ordinary-item exclusion.
- `src/test/java/com/vincenthuto/mnagnosis/client/render/entity/FractalItemEntityRendererContractTest.java`: renderer/mixin boundary contract.

**Modify**

- `src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java`: register `FRACTAL_ITEM`.
- `src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java`: register the entity renderer.
- Six concrete fractal item classes: move to the correct new base without changing their client renderers.
- `src/main/resources/mnagnosis.mixins.json`: remove `client.ItemEntityRendererMixin`.

**Delete**

- `src/main/java/com/vincenthuto/mnagnosis/mixin/client/ItemEntityRendererMixin.java`
- `src/main/java/com/vincenthuto/mnagnosis/client/render/item/ApollonianTrapEntitySpin.java`
- `src/test/java/com/vincenthuto/mnagnosis/client/render/item/ApollonianTrapEntitySpinTest.java`

---

### Task 1: Validated Presentation Traits

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/item/FractalItemEntityTraits.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/item/FractalEntityItem.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/entity/item/FractalItemEntityTraitsTest.java`

**Interfaces:**
- Produces: `FractalItemEntityTraits.STATIC`, `bobOffset(float)`, `rotation(float)`, `validate`.
- Produces: `FractalEntityItem.fractalEntityTraits(ItemStack)`.

- [ ] **Step 1: Write the failing trait test**

```java
@Test
void staticDefaultsNeverBobOrRotate() {
    assertEquals(0.0F, FractalItemEntityTraits.STATIC.bobOffset(0.0F));
    assertEquals(0.0F, FractalItemEntityTraits.STATIC.bobOffset(500.0F));
    assertEquals(0.0F, FractalItemEntityTraits.STATIC.rotation(500.0F));
}

@Test
void rejectsInvalidProfiles() {
    assertThrows(IllegalArgumentException.class, () ->
        new FractalItemEntityTraits(0, 0, 0, 0, 0, 0, false));
    assertThrows(IllegalArgumentException.class, () ->
        new FractalItemEntityTraits(Float.NaN, 0, 0, 0, 1, 0, false));
}
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```powershell
.\gradlew.bat test --tests "*FractalItemEntityTraitsTest" --no-daemon
```

Expected: compilation failure because the traits type does not exist.

- [ ] **Step 3: Implement the record and provider**

```java
public record FractalItemEntityTraits(
        float bobAmplitude,
        float bobRadiansPerTick,
        float rotationRadiansPerTick,
        float fixedYaw,
        float renderScale,
        float verticalOffset,
        boolean fullBright
) {
    public static final FractalItemEntityTraits STATIC =
            new FractalItemEntityTraits(0, 0, 0, 0, 1, 0, false);

    public FractalItemEntityTraits {
        // Reject every non-finite component and renderScale <= 0.
    }

    public float bobOffset(float ageInTicks) {
        return bobAmplitude == 0 ? 0 :
                Mth.sin(ageInTicks * bobRadiansPerTick) * bobAmplitude;
    }

    public float rotation(float ageInTicks) {
        return fixedYaw + ageInTicks * rotationRadiansPerTick;
    }
}
```

`FractalEntityItem` returns `STATIC` by default and takes an `ItemStack`
so future NBT-sensitive profiles do not require a new API.

- [ ] **Step 4: Run the trait test and verify GREEN**

Run the command from Step 2. Expected: PASS.

- [ ] **Step 5: Suggested commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/entity/item/FractalItemEntityTraits.java src/main/java/com/vincenthuto/mnagnosis/common/item/FractalEntityItem.java src/test/java/com/vincenthuto/mnagnosis/common/entity/item/FractalItemEntityTraitsTest.java
git commit -m "feat: define fractal item entity traits"
```

### Task 2: Entity Type and Item Creation Hooks

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/entity/item/FractalItemEntity.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/item/FractalItem.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/item/FractalBlockItem.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/entity/item/FractalItemOptInContractTest.java`

**Interfaces:**
- Consumes: `FractalEntityItem.fractalEntityTraits(ItemStack)`.
- Produces: `EntityRegistry.FRACTAL_ITEM`.
- Produces: `FractalItemEntity.create(Level, Entity, ItemStack)`.

- [ ] **Step 1: Write the failing opt-in contract**

Construct one instance of each item class with test properties/blocks and
assert `hasCustomEntity(stack)` is true. Assert `new Item(...).hasCustomEntity`
is false. Assert each opted-in item implements `FractalEntityItem`.

- [ ] **Step 2: Run the opt-in test and verify RED**

```powershell
.\gradlew.bat test --tests "*FractalItemOptInContractTest" --no-daemon
```

Expected: compilation failure because the item bases and entity do not
exist.

- [ ] **Step 3: Register and implement `FractalItemEntity`**

Register:

```java
public static final RegistryObject<EntityType<FractalItemEntity>>
        FRACTAL_ITEM = ENTITIES.register(
            "fractal_item",
            () -> EntityType.Builder
                .<FractalItemEntity>of(
                    FractalItemEntity::new,
                    MobCategory.MISC
                )
                .sized(0.25F, 0.25F)
                .clientTrackingRange(10)
                .updateInterval(20)
                .build(MnAGnosis.rloc("fractal_item").toString())
        );
```

The entity constructor calls `super(type, level)`. When `location` is an
`ItemEntity`, the factory transfers its complete vanilla/Forge state
through `saveWithoutId` into a `CompoundTag` followed by `load` on the new
entity, then replaces the stack with `stack.copy()`. This preserves age,
pickup delay, owner, thrower, lifespan, position, motion, and rotation
without reflective field access. For a non-item source, copy position,
rotation, and delta movement directly and initialize the stack/lifespan.
Do not override `tick`, pickup, merge, serialization, or damage.

- [ ] **Step 4: Implement the two item bases**

Both bases implement `FractalEntityItem`, return true from
`hasCustomEntity`, and return `FractalItemEntity.create(level, location,
stack)` from `createEntity`. `FractalBlockItem` accepts `(Block,
Properties)`; `FractalItem` accepts `(Properties)`.

- [ ] **Step 5: Run the opt-in test and verify GREEN**

Run the command from Step 2. Expected: PASS.

- [ ] **Step 6: Suggested commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/entity/item src/main/java/com/vincenthuto/mnagnosis/common/item/FractalItem.java src/main/java/com/vincenthuto/mnagnosis/common/item/FractalBlockItem.java src/main/java/com/vincenthuto/mnagnosis/common/registry/EntityRegistry.java src/test/java/com/vincenthuto/mnagnosis/common/entity/item
git commit -m "feat: add dedicated fractal item entity"
```

### Task 3: Dedicated Client Renderer

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/render/entity/FractalItemEntityRenderer.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/client/render/entity/FractalItemEntityRendererContractTest.java`

**Interfaces:**
- Consumes: `FractalItemEntity`, `FractalEntityItem`, and
  `FractalItemEntityTraits`.
- Produces: renderer registration for `EntityRegistry.FRACTAL_ITEM`.

- [ ] **Step 1: Write the failing renderer contract**

The contract instantiates/query-checks the traits independently and reads
the renderer registration boundary to require `FRACTAL_ITEM` while
rejecting `ItemEntityRendererMixin`. It must not assert private formatting.

- [ ] **Step 2: Run the renderer contract and verify RED**

```powershell
.\gradlew.bat test --tests "*FractalItemEntityRendererContractTest" --no-daemon
```

Expected: failure because the renderer registration is absent.

- [ ] **Step 3: Implement renderer and registration**

Extend `EntityRenderer<FractalItemEntity>`, retain the
`EntityRendererProvider.Context#getItemRenderer`, and render one stack:

```java
float age = entity.getAge() + partialTick;
FractalItemEntityTraits traits = entity.traits();
float groundScaleY = itemRenderer
        .getModel(entity.getItem(), entity.level(), null, entity.getId())
        .getTransforms()
        .getTransform(ItemDisplayContext.GROUND)
        .scale.y();
poseStack.pushPose();
poseStack.translate(0.0F, 0.25F * groundScaleY + traits.verticalOffset()
        + traits.bobOffset(age), 0.0F);
poseStack.mulPose(Axis.YP.rotation(traits.rotation(age)));
poseStack.scale(traits.renderScale(), traits.renderScale(),
        traits.renderScale());
itemRenderer.renderStatic(
        entity.getItem(),
        ItemDisplayContext.GROUND,
        traits.fullBright() ? LightTexture.FULL_BRIGHT : packedLight,
        OverlayTexture.NO_OVERLAY,
        poseStack,
        buffer,
        entity.level(),
        entity.getId()
);
poseStack.popPose();
```

Return `InventoryMenu.BLOCK_ATLAS` from `getTextureLocation`. Register
`FractalItemEntityRenderer::new` in `ClientEvents`.

- [ ] **Step 4: Run renderer and existing fractal visual tests**

```powershell
.\gradlew.bat test --tests "*FractalItemEntityRendererContractTest" --tests "*ApollonianTrapVisualContractTest" --tests "*KochianStarVisualContractTest" --tests "*TesseractBlockItemRendererTest" --no-daemon
```

Expected: PASS except for any already-documented unrelated baseline
failure, which must be reported rather than hidden.

- [ ] **Step 5: Suggested commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/client/render/entity/FractalItemEntityRenderer.java src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java src/test/java/com/vincenthuto/mnagnosis/client/render/entity/FractalItemEntityRendererContractTest.java
git commit -m "feat: render dedicated fractal item entities"
```

### Task 4: Migrate All Six Items and Remove the Mixin

**Files:**
- Modify: six concrete item classes named in Global Constraints.
- Modify: `src/main/resources/mnagnosis.mixins.json`
- Delete: `src/main/java/com/vincenthuto/mnagnosis/mixin/client/ItemEntityRendererMixin.java`
- Delete: `src/main/java/com/vincenthuto/mnagnosis/client/render/item/ApollonianTrapEntitySpin.java`
- Delete: `src/test/java/com/vincenthuto/mnagnosis/client/render/item/ApollonianTrapEntitySpinTest.java`
- Test: `src/test/java/com/vincenthuto/mnagnosis/common/entity/item/FractalItemOptInContractTest.java`

**Interfaces:**
- Consumes: `FractalItem` and `FractalBlockItem`.
- Produces: all six registered stacks use `FractalItemEntity`.

- [ ] **Step 1: Extend the migration test**

Use `ItemRegistry` entries to assert the concrete registry items implement
`FractalEntityItem`. Require both Tesseract registrations explicitly.

- [ ] **Step 2: Run the migration test and verify RED**

Run the Task 2 test command. Expected: FAIL listing the unmigrated items.

- [ ] **Step 3: Change inheritance without changing BEWLR code**

Move four ordinary item classes to `FractalItem` and two block item
classes to `FractalBlockItem`. Keep their constructors and
`initializeClient` implementations intact.

- [ ] **Step 4: Remove the temporary mixin path**

Delete the mixin/helper/test and remove only
`client.ItemEntityRendererMixin` from the JSON list. Validate the JSON
with PowerShell `ConvertFrom-Json`.

- [ ] **Step 5: Run focused tests and assemble**

```powershell
.\gradlew.bat test --tests "*FractalItem*" --tests "*ApollonianTrapVisualContractTest" --no-daemon
.\gradlew.bat assemble --no-daemon
```

Expected: both commands exit zero.

- [ ] **Step 6: Suggested commit**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/common/item src/main/resources/mnagnosis.mixins.json src/test/java/com/vincenthuto/mnagnosis
git commit -m "refactor: migrate fractals to custom item entity"
```

### Task 5: Runtime Lifecycle Verification

**Files:**
- Modify only if a runtime failure exposes a tested implementation defect.

**Interfaces:**
- Validates the complete server-to-client entity path.

- [ ] **Step 1: Launch the Forge client**

```powershell
.\gradlew.bat runClient --no-daemon
```

- [ ] **Step 2: Verify mixin and registry logs**

Require registration of `mnagnosis:fractal_item`, no mention of applying
`ItemEntityRendererMixin`, and no entity-renderer/mixin errors.

- [ ] **Step 3: Exercise one ordinary and one block fractal**

Drop `mnagnosis:apollonian_trap` and
`mnagnosis:mengerian_topology`. Confirm both:

- report entity type `mnagnosis:fractal_item`;
- remain at a fixed rendered height;
- remain at a fixed orientation;
- keep their internal shader animation;
- fall, collide, despawn, and pick up like normal items.

- [ ] **Step 4: Run final focused verification**

```powershell
.\gradlew.bat test --tests "*FractalItem*" --tests "*ApollonianTrapVisualContractTest" --no-daemon
.\gradlew.bat assemble --no-daemon
```

Expected: both commands exit zero. Report any unrelated full-suite
baseline failures separately.
