#version 150

in vec3 localPosition;
in float vertexAlpha;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 ModelPoseMat;
uniform vec3 CameraOrigin;
uniform vec3 RayDirection;
uniform int Perspective;
uniform float MengerianTime;
uniform float MengerianDepth;
uniform float MengerianSeparation;
uniform vec3 PaletteCrimson;
uniform vec3 PaletteGold;
uniform vec3 PaletteVerdant;
uniform vec3 PaletteViolet;
uniform vec3 PaletteAzure;
uniform vec3 PalettePearl;
uniform float PaletteBrightness;
uniform float PaletteShadeStrength;
uniform float PaletteDepthColorMix;

out vec4 fragColor;

const int MAX_MARCH_STEPS = 88;
const int MAX_MENGER_DEPTH = 3;
const float MAX_DISTANCE = 1.85;
const float SURFACE_EPSILON = 0.0011;
const float PROXY_BOUND = 0.62;

float boxDistance(vec3 point, vec3 halfSize) {
    vec3 offset = abs(point) - halfSize;
    return length(max(offset, vec3(0.0)))
            + min(max(offset.x, max(offset.y, offset.z)), 0.0);
}

float recursionArrival(int iteration) {
    float arrival = clamp(
            MengerianDepth - float(iteration),
            0.0,
            1.0
    );
    return arrival * arrival * (3.0 - 2.0 * arrival);
}

float mengerDistance(vec3 point) {
    vec3 unitPoint = point * 2.0;
    float distanceToSurface =
            boxDistance(unitPoint, vec3(1.0)) * 0.5;
    float scale = 1.0;

    for (int iteration = 0;
            iteration < MAX_MENGER_DEPTH;
            iteration++) {
        vec3 repeated = mod(unitPoint * scale, 2.0) - 1.0;
        scale *= 3.0;
        vec3 crossBars = abs(1.0 - 3.0 * abs(repeated));
        float xy = max(crossBars.x, crossBars.y);
        float yz = max(crossBars.y, crossBars.z);
        float zx = max(crossBars.z, crossBars.x);
        float crossDistance =
                (min(xy, min(yz, zx)) - 1.0) / scale * 0.5;
        float arrival = recursionArrival(iteration);
        float dormantOffset = (1.0 - arrival) * 0.75;
        float separation =
                MengerianSeparation * arrival * 0.08 / scale;
        distanceToSurface = max(
                distanceToSurface,
                crossDistance - dormantOffset + separation
        );
    }
    return distanceToSurface;
}

vec3 estimateNormal(vec3 point) {
    vec2 offset = vec2(SURFACE_EPSILON * 1.6, 0.0);
    return normalize(vec3(
            mengerDistance(point + offset.xyy)
                    - mengerDistance(point - offset.xyy),
            mengerDistance(point + offset.yxy)
                    - mengerDistance(point - offset.yxy),
            mengerDistance(point + offset.yyx)
                    - mengerDistance(point - offset.yyx)
    ));
}

vec3 mengerianPalette(float value) {
    float scaled = fract(value) * 6.0;
    if (scaled < 1.0) {
        return mix(PaletteCrimson, PaletteGold,
                smoothstep(0.0, 1.0, scaled));
    }
    if (scaled < 2.0) {
        return mix(PaletteGold, PaletteVerdant,
                smoothstep(0.0, 1.0, scaled - 1.0));
    }
    if (scaled < 3.0) {
        return mix(PaletteVerdant, PaletteViolet,
                smoothstep(0.0, 1.0, scaled - 2.0));
    }
    if (scaled < 4.0) {
        return mix(PaletteViolet, PaletteAzure,
                smoothstep(0.0, 1.0, scaled - 3.0));
    }
    if (scaled < 5.0) {
        return mix(PaletteAzure, PalettePearl,
                smoothstep(0.0, 1.0, scaled - 4.0));
    }
    return mix(PalettePearl, PaletteCrimson,
            smoothstep(0.0, 1.0, scaled - 5.0));
}

void main() {
    vec3 rayDirection = Perspective == 1
            ? normalize(localPosition - CameraOrigin)
            : normalize(RayDirection);
    vec3 samplePoint =
            localPosition + rayDirection * SURFACE_EPSILON * 2.0;
    float traveled = 0.0;
    bool hit = false;

    for (int step = 0; step < MAX_MARCH_STEPS; step++) {
        float distanceToSurface = mengerDistance(samplePoint);
        float hitThreshold =
                SURFACE_EPSILON * (1.0 + traveled * 0.10);
        if (distanceToSurface < hitThreshold) {
            hit = true;
            break;
        }
        float stepDistance = max(
                distanceToSurface * 0.78,
                SURFACE_EPSILON * 0.55
        );
        traveled += stepDistance;
        samplePoint += rayDirection * stepDistance;
        if (traveled > MAX_DISTANCE
                || any(greaterThan(
                        abs(samplePoint),
                        vec3(PROXY_BOUND + 0.02)
                ))) {
            break;
        }
    }

    if (!hit) {
        discard;
    }

    vec3 normal = estimateNormal(samplePoint);
    vec3 lightDirection = normalize(vec3(-0.48, 0.78, 0.40));
    vec3 halfDirection = normalize(lightDirection - rayDirection);
    float diffuse = max(dot(normal, lightDirection), 0.0);
    float facing = max(dot(normal, -rayDirection), 0.0);
    float rim = pow(1.0 - facing, 2.7);
    float specular = pow(max(dot(normal, halfDirection), 0.0), 42.0);

    float coordinateBands = dot(
            abs(samplePoint),
            vec3(2.7, 3.9, 5.1)
    );
    float movingBand = fract(
            coordinateBands
                    + MengerianDepth * 0.13
                    - MengerianTime * 0.022
    );
    vec3 surfaceColor = mengerianPalette(movingBand);
    vec3 depthColor = mengerianPalette(
            fract(MengerianDepth * 0.19 + normal.y * 0.18)
    );
    surfaceColor = mix(
            surfaceColor,
            depthColor,
            PaletteDepthColorMix
    );

    float shade = mix(
            1.0,
            0.30 + diffuse * 0.84,
            PaletteShadeStrength
    );
    vec3 shaded = surfaceColor * shade
            + PalettePearl * rim * 0.28
            + PalettePearl * specular * 0.48
            + PaletteAzure * (1.0 - diffuse) * 0.075;
    shaded *= PaletteBrightness;

    vec4 clipPosition = ProjMat * ModelViewMat * ModelPoseMat
            * vec4(samplePoint, 1.0);
    float normalizedDepth = clipPosition.z / clipPosition.w;
    gl_FragDepth = clamp(
            normalizedDepth * 0.5 + 0.5,
            0.0,
            1.0
    );
    fragColor = vec4(max(shaded, vec3(0.0)), vertexAlpha);
}
