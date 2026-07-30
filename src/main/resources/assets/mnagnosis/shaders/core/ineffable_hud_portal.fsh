#version 150

in vec2 texCoord;

uniform float PortalTime;
uniform float PortalOpacity;

out vec4 fragColor;

float hash21(vec2 point) {
    vec3 hash = fract(vec3(point.xyx) * vec3(0.1031, 0.1030, 0.0973));
    hash += dot(hash, hash.yzx + 33.33);
    return fract((hash.x + hash.y) * hash.z);
}

vec3 starLayer(
        vec2 fieldUv,
        float time,
        float density,
        float speed,
        float brightness
) {
    vec2 gridScale = vec2(density * 0.12, density * 0.22);
    vec2 drift = vec2(time * speed, -time * speed * 0.17);
    vec2 gridUv = fieldUv * gridScale + drift;
    vec2 cell = floor(gridUv);
    vec2 local = fract(gridUv) - 0.5;
    float seed = hash21(cell);
    vec2 offset = vec2(
            hash21(cell + vec2(17.1, 3.7)),
            hash21(cell + vec2(43.7, 9.2))
    ) - 0.5;
    float distanceToStar = length(local - offset * 0.62);
    float exists = step(0.955, seed);
    float star = smoothstep(0.115, 0.018, distanceToStar) * exists;
    float pulse = 0.72 + 0.28 * sin(
            time * (1.15 + seed * 0.8) + seed * 31.4159
    );
    vec3 white = vec3(0.92, 0.96, 1.0);
    vec3 cyan = vec3(0.0, 0.72, 0.83);
    float cyanStar = step(0.76, hash21(cell + vec2(91.3, 27.4)));
    return mix(white, cyan, cyanStar) * star * pulse * brightness;
}

float warpedBand(vec2 fieldUv, float time) {
    float secondary = sin(fieldUv.x * 0.31 + time * 0.11) * 0.055;
    float center = 0.5
            + sin(fieldUv.x * 0.53 - time * 0.19) * 0.13
            + secondary;
    float distanceToBand = abs(fieldUv.y - center);
    float band = exp(-distanceToBand * 14.0);
    float fracture = 0.42 + 0.58 * sin(
            fieldUv.x * 2.7 + time * 0.27
    );
    return band * max(0.0, fracture);
}

void main() {
    vec2 fieldUv = vec2(texCoord.x * (790.0 / 54.0), texCoord.y);
    vec3 color = vec3(0.003, 0.005, 0.008);
    color += starLayer(fieldUv, PortalTime, 23.0, 0.10, 1.00);
    color += starLayer(fieldUv, PortalTime, 37.0, -0.06, 0.66);
    color += starLayer(fieldUv, PortalTime, 61.0, 0.035, 0.42);
    float band = warpedBand(fieldUv, PortalTime);
    color += vec3(0.0, 0.72, 0.83) * band * 0.12;
    fragColor = vec4(clamp(color, 0.0, 1.0), PortalOpacity);
}
