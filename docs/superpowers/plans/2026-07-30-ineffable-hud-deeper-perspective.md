# Ineffable HUD Deeper Perspective Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deepen the complete Ineffable HUD's right-edge recession while preserving its left anchor, upward tilt, and readability.

**Architecture:** Keep `IneffableHudPerspective` as the sole transform owner and change only its Y-axis rotation from 10 to 18 degrees. Establish the new visual geometry through the existing pure projection and pose-stack tests before changing production code.

**Tech Stack:** Java 17, JOML, Mojang `PoseStack`, JUnit 5, Gradle

## Global Constraints

- Keep the pivot at `(14, 18)`.
- Keep Z rotation at `-3` degrees, X rotation at `6` degrees, and scale at `(0.98, 0.90, 1.0)`.
- Apply perspective once to the complete HUD.
- Do not change textures, shaders, portal layering, particles, local draw coordinates, or animation.
- Preserve all existing unstaged user changes.
- Work directly on `master`.

---

### Task 1: Deepen the HUD's right-edge projection

**Files:**
- Modify: `src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPerspectiveTest.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPerspective.java`

**Interfaces:**
- Consumes: `IneffableHudPerspective.project(float x, float y, float z)` and `IneffableHudPerspective.apply(PoseStack pose)`.
- Produces: the same APIs with `ROTATION_Y_DEGREES == 18.0F`.

- [ ] **Step 1: Write the failing projection expectations**

Update the right-edge and pose-stack expectations:

```java
assertEquals(175.0211F, right.x(), EPSILON);
assertEquals(9.561239F, right.y(), EPSILON);
assertEquals(-52.39074F, right.z(), EPSILON);
```

Retain the fixed-pivot test. Keep the compact bounds at width `160..173` and
height `30..45`; the expected 18-degree projection remains approximately
`163.60 × 38.78`.

- [ ] **Step 2: Run the focused test to verify it fails**

Run:

```powershell
.\gradlew.bat test --tests com.vincenthuto.mnagnosis.client.authorship.IneffableHudPerspectiveTest
```

Expected: FAIL because the current 10-degree transform projects the right edge
to approximately `(180.73549, 9.26176, -29.44031)`.

- [ ] **Step 3: Make the minimal production change**

Change exactly one constant:

```java
public static final float ROTATION_Y_DEGREES = 18.0F;
```

- [ ] **Step 4: Run focused and full verification**

Run:

```powershell
.\gradlew.bat test --tests com.vincenthuto.mnagnosis.client.authorship.IneffableHudPerspectiveTest
.\gradlew.bat test processResources
git diff --check
```

Expected: all Gradle tasks succeed and `git diff --check` reports no errors.

- [ ] **Step 5: Commit only the perspective files**

```powershell
git add -- src/main/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPerspective.java src/test/java/com/vincenthuto/mnagnosis/client/authorship/IneffableHudPerspectiveTest.java
git commit -m "feat: deepen Ineffable HUD perspective"
```
