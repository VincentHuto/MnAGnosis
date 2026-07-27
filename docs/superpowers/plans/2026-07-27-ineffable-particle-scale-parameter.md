# Ineffable Particle Scale Parameter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Carry a runtime scale multiplier in every Ineffable cube particle payload.

**Architecture:** Replace both simple particle registrations with custom particle types sharing one serialized options class. Thread the payload scale through the common spawn helpers and client provider into the existing size math, retaining `1.0` defaults for all current effects.

**Tech Stack:** Java 17, Minecraft Forge 1.20.1 particle codecs/network serialization, Forge GameTest.

## Global Constraints

- Preserve both existing particle registry IDs and textures.
- A scale of `1.0` must preserve the current `0.038–0.060` block full-width range.
- Existing emitters must continue working without mandatory call-site changes.
- Preserve concurrent gravity-shift, armor, particle motion, and renderer changes.

---

### Task 1: Define and test runtime particle options

**Files:**
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/particle/IneffableCubeParticleOptions.java`
- Create: `src/main/java/com/vincenthuto/mnagnosis/common/particle/IneffableCubeParticleType.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/particle/IneffableParticleScale.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/gametest/Tier6ProgressionGameTests.java`

**Interfaces:**
- Produces: `IneffableCubeParticleOptions.scale()`.
- Produces: `IneffableCubeParticleType.options(float scale)`.
- Produces: `IneffableParticleScale.baseHalfSize(float randomUnit, float scale)`.

- [ ] Add GameTest assertions for a `2.5F` runtime scale, invalid-value normalization, and multiplied minimum/maximum half-size.
- [ ] Run `.\gradlew.bat compileJava` and verify it fails because the custom option types and overload do not exist.
- [ ] Implement command, network, and codec serialization for the float scale.
- [ ] Add the two-argument scale math overload while retaining the existing one-argument method as a `1.0F` default.
- [ ] Run `.\gradlew.bat compileJava` and verify it passes.

### Task 2: Route registrations, spawning, and rendering

**Files:**
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/registry/ParticleRegistry.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/common/particle/IneffableParticleEffects.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/particle/OutlinedCubeParticle.java`
- Modify: `src/main/java/com/vincenthuto/mnagnosis/client/event/ClientEvents.java`

**Interfaces:**
- Consumes: `IneffableCubeParticleOptions.scale()`.
- Produces: `IneffableParticleEffects.add(..., float scale)`.
- Produces: `IneffableParticleEffects.cloud(..., float scale)`.
- Retains: existing overloads defaulting to `1.0F`.

- [ ] Register black and white `IneffableCubeParticleType` instances under the existing IDs.
- [ ] Make the provider generic over `IneffableCubeParticleOptions` and apply its scale during construction.
- [ ] Add scaled spawn-helper overloads and delegate existing overloads to `1.0F`.
- [ ] Run `.\gradlew.bat runGameTestServer` and verify every required test passes.
- [ ] Run `.\gradlew.bat build` and verify the packaged build succeeds.
- [ ] Commit only the custom particle-option implementation and its tests.
