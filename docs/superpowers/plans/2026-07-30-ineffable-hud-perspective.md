# Ineffable HUD Perspective Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Angle the complete Ineffable HUD upward and away toward the right while keeping every live element spatially unified and readable.

**Architecture:** A focused `IneffableHudPerspective` utility owns fixed transform constants, pure point projection, and the matching `PoseStack` operation. `IneffableHudRenderer` applies it once inside its existing local push/pop scope, allowing the badge, text, textured layers, contradiction marks, and animated cubes to inherit the same transform.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1 GUI rendering, Mojang `PoseStack`/`Axis`, JOML, JUnit 5, Gradle.

## Global Constraints

- Use pivot `(14.0, 18.0, 0.0)` in HUD-local coordinates.
- Use Z rotation `-3.0°`, Y rotation `+10.0°`, and X rotation `+6.0°`.
- Use scale `(0.98, 0.90, 1.0)`.
- Apply the transform to badge, level, frame, fills, contradiction marks, and cubes.
- Do not modify or regenerate textures.
- Do not replace the global projection matrix or create an off-screen framebuffer.
- Keep the transform inside `IneffableHudRenderer`'s existing push/pop scope.

---

### Task 1: Add the tested perspective transform

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPerspective.java`
- Create: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPerspectiveTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java`

**Interfaces:**
- Consumes: `PoseStack`, `Axis`, `Vector3f`.
- Produces: `IneffableHudPerspective.apply(PoseStack)`, `IneffableHudPerspective.project(float, float, float)`, `Point(float x, float y, float z)`, and one shared transformed HUD render scope.

- [ ] **Step 1: Write the failing geometry tests**

Create `IneffableHudPerspectiveTest` with tests equivalent to:

```java
@Test
void keepsTheLeftCenterPivotFixed() {
    Point point = IneffableHudPerspective.project(14.0F, 18.0F, 0.0F);
    assertEquals(14.0F, point.x(), 0.0001F);
    assertEquals(18.0F, point.y(), 0.0001F);
    assertEquals(0.0F, point.z(), 0.0001F);
}

@Test
void sendsTheRightEdgeUpAndAway() {
    Point left = IneffableHudPerspective.project(14.0F, 18.0F, 0.0F);
    Point right = IneffableHudPerspective.project(187.0F, 18.0F, 0.0F);
    assertTrue(right.y() < left.y() - 7.0F);
    assertTrue(right.z() < left.z() - 25.0F);
    assertTrue(right.x() < 187.0F);
}

@Test
void keepsTheProjectedHudCompact() {
    List<Point> corners = List.of(
            IneffableHudPerspective.project(14.0F, 4.0F, 0.0F),
            IneffableHudPerspective.project(187.0F, 4.0F, 0.0F),
            IneffableHudPerspective.project(14.0F, 38.0F, 0.0F),
            IneffableHudPerspective.project(187.0F, 38.0F, 0.0F)
    );
    float width = maxX(corners) - minX(corners);
    float height = maxY(corners) - minY(corners);
    assertTrue(width >= 160.0F && width <= 173.0F);
    assertTrue(height >= 30.0F && height <= 45.0F);
}
```

Also assert the public constants are exactly the values in Global Constraints
and that repeated `project` calls are equal. Include a `PoseStack` parity test:

```java
@Test
void poseStackMatchesPureProjection() {
    PoseStack pose = new PoseStack();
    IneffableHudPerspective.apply(pose);
    Vector4f actual = new Vector4f(187.0F, 18.0F, 0.0F, 1.0F);
    pose.last().pose().transform(actual);
    Point expected = IneffableHudPerspective.project(187.0F, 18.0F, 0.0F);
    assertEquals(expected.x(), actual.x(), 0.0001F);
    assertEquals(expected.y(), actual.y(), 0.0001F);
    assertEquals(expected.z(), actual.z(), 0.0001F);
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
.\gradlew.bat test --tests "*IneffableHudPerspectiveTest"
```

Expected: test compilation fails because `IneffableHudPerspective` does not exist.

- [ ] **Step 3: Implement the minimal transform helper**

Create `IneffableHudPerspective` with:

```java
public static final float PIVOT_X = 14.0F;
public static final float PIVOT_Y = 18.0F;
public static final float ROTATION_Z_DEGREES = -3.0F;
public static final float ROTATION_Y_DEGREES = 10.0F;
public static final float ROTATION_X_DEGREES = 6.0F;
public static final float SCALE_X = 0.98F;
public static final float SCALE_Y = 0.90F;

public static void apply(PoseStack pose) {
    pose.translate(PIVOT_X, PIVOT_Y, 0.0F);
    pose.mulPose(Axis.ZP.rotationDegrees(ROTATION_Z_DEGREES));
    pose.mulPose(Axis.YP.rotationDegrees(ROTATION_Y_DEGREES));
    pose.mulPose(Axis.XP.rotationDegrees(ROTATION_X_DEGREES));
    pose.scale(SCALE_X, SCALE_Y, 1.0F);
    pose.translate(-PIVOT_X, -PIVOT_Y, 0.0F);
}

public static Point project(float x, float y, float z) {
    Vector3f point = new Vector3f(
            x - PIVOT_X, y - PIVOT_Y, z
    );
    point.mul(SCALE_X, SCALE_Y, 1.0F);
    point.rotateX(radians(ROTATION_X_DEGREES));
    point.rotateY(radians(ROTATION_Y_DEGREES));
    point.rotateZ(radians(ROTATION_Z_DEGREES));
    return new Point(
            point.x() + PIVOT_X,
            point.y() + PIVOT_Y,
            point.z()
    );
}
```

Add:

```java
private static float radians(float degrees) {
    return degrees * (float) Math.PI / 180.0F;
}

public record Point(float x, float y, float z) {
}
```

- [ ] **Step 4: Apply the transform at the shared render boundary**

In `IneffableHudRenderer.render`, immediately after:

```java
graphics.pose().translate(hudX, hudY, 0.0F);
```

add:

```java
IneffableHudPerspective.apply(graphics.pose());
```

Do not add any nested perspective calls. The existing badge, level, frame,
contradictions, and cube calls must remain inside the same outer pose scope.

- [ ] **Step 5: Run the focused test and verify GREEN**

Run:

```powershell
.\gradlew.bat test --tests "*IneffableHudPerspectiveTest"
```

Expected: PASS.

- [ ] **Step 6: Run existing HUD and full verification**

```powershell
.\gradlew.bat test processResources
git diff --check
```

Expected: `BUILD SUCCESSFUL` and no patch-hygiene output.

- [ ] **Step 7: Commit the implementation**

```powershell
git add src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPerspective.java src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudRenderer.java src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPerspectiveTest.java
git commit -m "feat: angle the complete Ineffable HUD"
```
