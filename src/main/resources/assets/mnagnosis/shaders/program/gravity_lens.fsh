#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform float Time;
uniform vec4 Lens0;
uniform vec4 Lens1;
uniform vec4 Lens2;

in vec2 texCoord;

out vec4 fragColor;

vec2 bendLens(vec2 uv, vec4 lens, float phase, inout float ringLight) {
    if (lens.z <= 0.0) {
        return uv;
    }

    vec2 deltaPixels = (uv - lens.xy) * InSize;
    float distancePixels = length(deltaPixels);
    if (distancePixels < 0.001) {
        return uv;
    }

    float normalizedDistance = distancePixels / lens.z;
    if (normalizedDistance >= 4.0) {
        return uv;
    }

    float clampedDistance = max(1.0, normalizedDistance);
    float progress = clamp((4.0 - clampedDistance) / 3.0, 0.0, 1.0);
    float falloff = progress * progress * (3.0 - 2.0 * progress);
    float polarity = lens.w < 0.0 ? -1.0 : 1.0;
    float pulse = 0.88 + 0.12 * sin(Time * 125.6637 + phase + polarity);
    float horizonGuard = smoothstep(0.72, 1.03, normalizedDistance);
    vec2 direction = deltaPixels / distancePixels;
    vec2 offsetPixels = direction
            * falloff * 0.18 * lens.z * polarity * pulse * horizonGuard;

    float primaryRing = exp(
            -pow((normalizedDistance - 1.16) / 0.075, 2.0)
    );
    float outerRing = exp(
            -pow((normalizedDistance - 1.62) / 0.16, 2.0)
    ) * 0.18;
    ringLight += (primaryRing + outerRing)
            * (0.72 + 0.18 * sin(Time * 188.4956 + phase));
    return uv - offsetPixels / InSize;
}

void main() {
    vec2 warpedUv = texCoord;
    float ringLight = 0.0;
    warpedUv = bendLens(warpedUv, Lens0, 0.0, ringLight);
    warpedUv = bendLens(warpedUv, Lens1, 2.0944, ringLight);
    warpedUv = bendLens(warpedUv, Lens2, 4.1888, ringLight);

    vec4 scene = texture(DiffuseSampler, clamp(warpedUv, vec2(0.0), vec2(1.0)));
    scene.rgb = min(vec3(1.0), scene.rgb + vec3(min(ringLight, 0.82)));
    fragColor = scene;
}
