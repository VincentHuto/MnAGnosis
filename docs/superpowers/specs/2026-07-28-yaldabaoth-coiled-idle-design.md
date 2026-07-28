# Yaldabaoth Coiled Idle Design

## Goal

Give Yaldabaoth his recognizable lion-headed serpent silhouette while stationary: a compact coil at ground level with the front of the body reared high above it. When he moves, he deliberately unfurls into a low, extended slithering stance.

This work changes the main Yaldabaoth entity only. The Counterfeit Sun and Counterfeit Moon retain their existing animations.

## Visual Direction

The supplied occult illustration is the silhouette reference rather than a literal modeling reference.

- Segments 4–10, the tail, and the terminal sweep form compact, overlapping ground loops.
- Segments 1–3 rise out of the coil and support the neck, mane, and lion head.
- The head sits high and slightly forward so Yaldabaoth reads as alert and imposing rather than as a normal snake resting flat.
- The coil remains broad enough to support the raised body without making the silhouette look top-heavy.
- Idle secondary motion is restrained: slow breathing, slight head drift, subtle mane movement, and a small tail adjustment.

## Animation Architecture

The Yaldabaoth animation asset will expose two looping base animations:

- `animation.yaldabaoth.idle`: the compact, reared coil.
- `animation.yaldabaoth.move`: the low slithering stance.

The existing sibling-bone geometry will be retained. The idle animation will coordinate both position and rotation keyframes across the body segments to arrange them into the coil. This avoids a risky skeleton and UV rebuild.

The movement loop will extend the segment positions into a low body line and send a phase-shifted lateral wave from the forward segments through the tail. The head and mane will receive smaller counter-motion so the face stays readable while the body slithers.

The existing `animation.yaldabaoth.combat.roar_sweep` remains a triggerable combat animation and is not replaced by locomotion.

## Runtime Selection and Transition

The base GeckoLib controller will select the main entity's idle loop while stationary and movement loop while locomoting. The main Yaldabaoth class will provide the movement animation without changing the Sun or Moon animation identifiers.

The controller transition length will be 10 ticks, producing a 0.5-second deliberate unfurl at the normal 20 ticks per second. The same blend will reform the coil after movement stops, preventing a hard snap between the very different silhouettes.

Movement selection must follow GeckoLib's locomotion state rather than combat state. The combat controller remains independent so a roar/sweep can play without permanently changing the selected base loop.

## Visibility and Compatibility

The geometry's visible bounds will be raised and enlarged vertically to contain the reared idle silhouette and prevent frustum culling of the elevated head.

Existing bone names, geometry identifier, texture, entity registration, combat timer, and combat animation identifier remain stable. No AI or movement-speed behavior is added by this change; the animation responds whenever current or future encounter logic causes GeckoLib to report the entity as moving.

## Verification

Automated asset-contract coverage will verify:

- the new movement animation identifier exists;
- the idle animation contains coordinated segment translations/rotations that produce a materially taller and more compact pose;
- the movement animation returns the body to a materially lower, more extended stance and contains a traveling lateral wave;
- the geometry's visible bounds accommodate the raised pose;
- Java exposes and selects the movement animation while preserving the other encounter entities' existing animation contracts.

The focused unit tests and the normal Gradle test/build tasks will be run after implementation. Because JSON keyframes cannot fully prove the in-game silhouette, final acceptance also requires visual inspection in a development client or Blockbench: stationary Yaldabaoth must read as a compact reared coil, motion must read as a flat slither, and both directions of the transition must blend without snapping or obvious segment separation.

## Out of Scope

- Rebuilding the body as a parented segment chain.
- Changing Yaldabaoth's texture, scale, hitbox, AI, navigation, or movement speed.
- Redesigning the combat roar/sweep.
- Changing the Counterfeit Sun or Counterfeit Moon animations.
