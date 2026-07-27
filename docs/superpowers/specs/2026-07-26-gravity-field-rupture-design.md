# Gravity Field Rupture Design

## Intent

Gravity Convergence fields become unstable when their collapsed cores touch.
The result is not a conventional fireball: both laws of gravity fail together,
the fields disappear, and a monochrome spacetime rupture throws out several
expanding spherical shockwaves.

## Collision Rules

- Collision logic runs on the server after moving fields update their anchor and
  before any field applies its normal force.
- Anchor mode is irrelevant. Fixed, caster-bound, and target-bound fields all
  compare their current world positions.
- Two cores collide when their center distance is no greater than
  `max(1.5, (firstRadius + secondRadius) * 0.18)` blocks. This follows the
  rendered black-hole scale and deliberately does not use the much larger
  gameplay influence radii.
- Collision grouping is transitive. If A touches B and B touches C, all three
  fields form one rupture even when A does not directly touch C.
- The lowest entity ID resolves the cluster, ensuring one deterministic rupture
  rather than one explosion per pair.
- Every field in the cluster is consumed. The rupture forms at their
  radius-weighted center.

## Rupture Behavior

A new short-lived `GravityRuptureEntity` owns the effect:

- Three wavefronts launch six ticks apart and expand for twenty-four ticks.
- Base maximum radius is 10 blocks for two fields. Each additional field adds
  2 blocks, capped at 18.
- Each wave hits an entity at most once as its spherical shell crosses it.
- The first wave deals the largest explosion damage and knockback; later waves
  diminish. Damage falls off with distance.
- Players, field owners, living mobs, items, and projectiles can all be thrown.
  Spectators and other rupture controllers are ignored.
- Blocks are not destroyed and no fire is created.
- Ruptures do not trigger other gravity fields remotely. Only actual field-core
  collision creates a rupture.

## Visual and Audio Feedback

- The server plays a generic explosion and warden sonic-boom sound at collapse.
- The client renders each wave as three orthogonal black-and-white particle
  rings so the result reads as a spherical ripple from any camera angle.
- Reverse-portal particles stream inward at the center while the rings expand.
- Each newly emitted wave receives a sonic-boom flash and an explosion-emitter
  burst.
- A no-op entity renderer participates in normal entity registration; all
  rupture visuals are particle-driven.

## Persistence and Networking

- Maximum radius, field count, and rupture age are synchronized entity data.
- The ephemeral controller can save/load those values without duplicating an
  already-resolved field collision.
- Standard Forge entity spawning keeps the effect synchronized for nearby
  clients.

## Verification

- Pure math tests cover the collision threshold, rupture radius scaling, wave
  radius progression, damage falloff, and diminishing wave strength.
- GameTests cover all three requested pairings: moving/moving,
  moving/stationary, and stationary/stationary.
- A cluster test proves three connected fields produce exactly one rupture and
  consume every field.
- A wave test proves damage/knockback occur without block destruction.
- Resource/class tests require the rupture entity renderer registration.

