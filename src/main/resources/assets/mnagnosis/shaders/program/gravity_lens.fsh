#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform float Time;
uniform vec4 Lens0;
uniform vec4 Lens1;
uniform vec4 Lens2;

in vec2 texCoord;

out vec4 fragColor;

const float LENS_HALO_RADIUS = 6.5;
const float STABLE_LENS_STRENGTH = 1.0;

vec2 bendLens(
        vec2 uv,
        vec4 lens,
        float phase,
        inout float ringLight,
        inout vec2 mirroredUv,
        inout float mirrorMix
) {
    if (lens.z <= 0.0) {
        return uv;
    }

    vec2 deltaPixels = (uv - lens.xy) * InSize;
    float distancePixels = length(deltaPixels);
    if (distancePixels < 0.001) {
        return uv;
    }

    float normalizedDistance = distancePixels / lens.z;
    if (normalizedDistance >= LENS_HALO_RADIUS) {
        return uv;
    }

    float clampedDistance = max(1.0, normalizedDistance);
    float progress = clamp(
            (LENS_HALO_RADIUS - clampedDistance)
            / (LENS_HALO_RADIUS - 1.0),
            0.0,
            1.0
    );
    float falloff = progress * progress * (3.0 - 2.0 * progress);
    float broadFalloff = pow(progress, 1.35);
    float inverseFalloff = max(0.0,
            (1.0 / clampedDistance - 1.0 / LENS_HALO_RADIUS)
            / (1.0 - 1.0 / LENS_HALO_RADIUS));
    float distortion = 0.46
            * (0.55 * inverseFalloff
            + 0.25 * falloff
            + 0.20 * broadFalloff);
    float polarity = lens.w < 0.0 ? -1.0 : 1.0;
    float horizonGuard = smoothstep(1.0, 1.10, normalizedDistance);
    vec2 direction = deltaPixels / distancePixels;
    vec2 offsetPixels = direction
            * distortion * lens.z * polarity
            * STABLE_LENS_STRENGTH * horizonGuard;

    float primaryRing = exp(
            -pow((normalizedDistance - 1.16) / 0.075, 2.0)
    );
    float outerRing = exp(
            -pow((normalizedDistance - 1.62) / 0.16, 2.0)
    ) * 0.18;
    ringLight += (primaryRing + outerRing)
            * (0.78 + 0.04 * sin(Time * 62.8319 + phase));

    float einsteinBand = exp(
            -pow((normalizedDistance - 1.20) / 0.19, 2.0)
    ) * horizonGuard;
    if (einsteinBand > mirrorMix) {
        float mirroredRadius = max(0.88, 2.25 - normalizedDistance)
                * lens.z;
        mirroredUv = lens.xy
                - direction * mirroredRadius / InSize;
        mirrorMix = einsteinBand;
    }
    return uv - offsetPixels / InSize;
}

vec4 sampleBentSpace(
        vec2 primaryUv,
        vec2 mirroredUv,
        float mirrorMix
) {
    vec4 primary = texture(
            DiffuseSampler,
            clamp(primaryUv, vec2(0.0), vec2(1.0))
    );
    if (mirrorMix <= 0.001) {
        return primary;
    }
    vec4 mirrored = texture(
            DiffuseSampler,
            clamp(mirroredUv, vec2(0.0), vec2(1.0))
    );
    return mix(primary, mirrored, min(0.68, mirrorMix * 0.68));
}

void main() {
    vec2 warpedUv = texCoord;
    vec2 mirroredUv = texCoord;
    float mirrorMix = 0.0;
    float ringLight = 0.0;
    warpedUv = bendLens(
            warpedUv, Lens0, 0.0, ringLight, mirroredUv, mirrorMix
    );
    warpedUv = bendLens(
            warpedUv, Lens1, 2.0944, ringLight, mirroredUv, mirrorMix
    );
    warpedUv = bendLens(
            warpedUv, Lens2, 4.1888, ringLight, mirroredUv, mirrorMix
    );

    vec4 scene = sampleBentSpace(warpedUv, mirroredUv, mirrorMix);
    scene.rgb = min(vec3(1.0), scene.rgb + vec3(min(ringLight, 0.82)));
    fragColor = scene;
}
