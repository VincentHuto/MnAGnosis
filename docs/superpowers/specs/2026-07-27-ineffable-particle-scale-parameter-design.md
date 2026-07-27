# Ineffable Particle Scale Parameter Design

## Intent

Every Ineffable cube spawn must be able to choose its size at runtime. Scale belongs to the particle payload itself rather than a global client configuration.

## Particle contract

Keep both existing registry IDs:

- `mnagnosis:ineffable_black_cube`
- `mnagnosis:ineffable_white_cube`

Replace their `SimpleParticleType` registrations with a shared custom particle type and options payload. `IneffableCubeParticleOptions` carries a positive floating-point `scale` multiplier, serializes it over the network, exposes it through the particle codec, and parses it from particle commands.

Command form:

```text
/particle mnagnosis:ineffable_black_cube 2.0
```

The value multiplies the approved base half-size range of `0.019–0.030` blocks. A scale of `1.0` preserves the current size, `0.5` halves it, and `2.0` doubles it. Non-positive and non-finite values normalize to `1.0`.

## Integration

`IneffableParticleEffects` gains overloads that accept a scale multiplier. Existing helpers continue to pass `1.0`, so no current spell changes size. New or tuned effects can provide scale independently for every particle.

The client provider receives `IneffableCubeParticleOptions` and applies its multiplier when constructing `OutlinedCubeParticle`. Variant selection remains determined by the registered black or white particle type.

## Verification

GameTests cover:

- runtime options preserve positive scale values;
- invalid values normalize to `1.0`;
- scale multiplies both ends of the base size range;
- alternating black/white selection still works.

