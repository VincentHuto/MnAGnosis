#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 ModelPoseMat;
uniform vec3 CameraOrigin;
uniform vec3 RayDirection;
uniform int Perspective;
uniform float KochianTime;
uniform float KochianAngle;
uniform float KochianRecursion;
uniform vec3 PaletteVoid;
uniform vec3 PaletteAmethyst;
uniform vec3 PaletteFuchsia;
uniform vec3 PalettePearl;
uniform vec3 PaletteIce;
uniform vec3 PaletteGold;

in vec3 localPosition;
in float vertexAlpha;

out vec4 fragColor;

const int MAX_MARCH_STEPS = 120;
const int MAX_KOCH_ITERATIONS = 8;
const float MAX_DISTANCE = 5.2;
const float SURFACE_EPSILON = 0.0018;
const float PROXY_BOUND = 1.32;
const float PI = 3.141592653589793;
const float THREE_ARM_SECTOR = 2.0943951023931953;
const float ARM_CENTER_OVERLAP = 0.06;

vec3 kochFold(vec3 z, float tetraHeight) {
    vec3 foldPlane = normalize(vec3(1.0, 0.0, -2.0 * tetraHeight));
    z.x -= 0.5;
    z.z -= tetraHeight;
    float reflected = 2.0 * min(0.0, dot(z, foldPlane));
    z -= reflected * foldPlane;
    z.x += 0.5;
    z.z += tetraHeight;
    z.x -= 1.0;
    return z;
}

float recursionArrival(int iteration) {
    float arrival = clamp(
            KochianRecursion - float(iteration),
            0.0,
            1.0
    );
    return arrival * arrival * (3.0 - 2.0 * arrival);
}

vec4 iterateKoch(vec3 point, bool collectOrbit) {
    vec3 z = point * 1.46;
    float derivative = 1.46;
    float orbit = 10.0;
    float beta = KochianAngle * PI / 360.0;
    float tangent = tan(beta);
    float tetraHeight = sqrt(
            max(3.0 * tangent * tangent - 1.0, 0.0001)
    ) * 0.25;
    float scale = 4.0 * cos(beta) * cos(beta);
    vec2 triangularPlane = vec2(0.8660254038, -0.5);

    for (int iteration = 0;
            iteration < MAX_KOCH_ITERATIONS;
            iteration++) {
        float arrival = recursionArrival(iteration);
        if (arrival <= 0.0001) {
            break;
        }

        vec3 folded = z;
        float triangularReflection = 2.0 * min(
                0.0,
                dot(folded.xy, triangularPlane)
        );
        folded.xy -= triangularReflection * triangularPlane;
        folded.y = abs(folded.y);
        folded = kochFold(folded, tetraHeight);
        folded *= scale;
        folded.x += 1.0;

        z = mix(z, folded, arrival);
        derivative *= mix(1.0, scale, arrival);
        if (collectOrbit) {
            float foldTrace = min(
                    abs(z.x),
                    min(abs(z.y), abs(z.z))
            ) / derivative;
            orbit = min(orbit, foldTrace);
        }
    }
    return vec4(z, collectOrbit ? orbit : derivative);
}

float kochV3Distance(vec3 point) {
    vec4 state = iterateKoch(point, false);
    return (length(state.xyz) - 3.0) / state.w;
}

vec3 foldThreeArms(vec3 point) {
    float angle = atan(point.z, point.x);
    float foldedAngle = mod(
            angle + THREE_ARM_SECTOR * 0.5,
            THREE_ARM_SECTOR
    ) - THREE_ARM_SECTOR * 0.5;
    float radius = length(point.xz);
    return vec3(
            cos(foldedAngle) * radius,
            point.y,
            sin(foldedAngle) * radius
    );
}

float kochArmDistance(vec3 armPoint) {
    float kochDistance = kochV3Distance(armPoint);
    float positiveArmClip = -armPoint.x - ARM_CENTER_OVERLAP;
    return max(kochDistance, positiveArmClip);
}

float sceneDistance(vec3 point) {
    return kochArmDistance(foldThreeArms(point));
}

vec3 estimateNormal(vec3 point) {
    vec2 epsilon = vec2(SURFACE_EPSILON * 1.25, 0.0);
    return normalize(vec3(
            sceneDistance(point + epsilon.xyy)
                    - sceneDistance(point - epsilon.xyy),
            sceneDistance(point + epsilon.yxy)
                    - sceneDistance(point - epsilon.yxy),
            sceneDistance(point + epsilon.yyx)
                    - sceneDistance(point - epsilon.yyx)
    ));
}

vec3 kochianPalette(float value) {
    float scaled = clamp(value, 0.0, 0.9999) * 5.0;
    if (scaled < 1.0) {
        return mix(PaletteVoid, PaletteAmethyst,
                smoothstep(0.0, 1.0, scaled));
    }
    if (scaled < 2.0) {
        return mix(PaletteAmethyst, PaletteFuchsia,
                smoothstep(0.0, 1.0, scaled - 1.0));
    }
    if (scaled < 3.0) {
        return mix(PaletteFuchsia, PalettePearl,
                smoothstep(0.0, 1.0, scaled - 2.0));
    }
    if (scaled < 4.0) {
        return mix(PalettePearl, PaletteIce,
                smoothstep(0.0, 1.0, scaled - 3.0));
    }
    return mix(PaletteIce, PaletteGold,
            smoothstep(0.0, 1.0, scaled - 4.0));
}

void main() {
    vec3 rayDirection = Perspective == 1
            ? normalize(localPosition - CameraOrigin)
            : normalize(RayDirection);
    vec3 samplePoint = localPosition
            + rayDirection * SURFACE_EPSILON * 2.0;
    float traveled = 0.0;
    bool hit = false;

    for (int step = 0; step < MAX_MARCH_STEPS; step++) {
        float distanceToSurface = sceneDistance(samplePoint);
        if (distanceToSurface < SURFACE_EPSILON) {
            hit = true;
            break;
        }
        float stepDistance = max(
                distanceToSurface * 0.64,
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
    vec3 lightDirection = normalize(vec3(-0.48, 0.76, 0.44));
    float diffuse = max(dot(normal, lightDirection), 0.0);
    float facing = max(dot(normal, -rayDirection), 0.0);
    float rim = pow(1.0 - facing, 2.6);
    float orbit = iterateKoch(foldThreeArms(samplePoint), true).w;
    float recursiveBands = fract(
            length(samplePoint) * 0.42
                    + KochianRecursion * 0.075
                    - KochianTime * 0.018
    );
    vec3 surfaceColor = kochianPalette(recursiveBands);
    float etchedKochLines = smoothstep(0.0025, 0.020, orbit);
    surfaceColor = mix(
            PaletteVoid * 0.30,
            surfaceColor,
            0.24 + etchedKochLines * 0.76
    );
    vec3 shaded = surfaceColor * (0.34 + diffuse * 0.82)
            + PalettePearl * rim * 0.20
            + PaletteAmethyst * (1.0 - diffuse) * 0.16;

    vec4 clipPosition = ProjMat * ModelViewMat * ModelPoseMat
            * vec4(samplePoint, 1.0);
    float normalizedDepth = clipPosition.z / clipPosition.w;
    gl_FragDepth = clamp(normalizedDepth * 0.5 + 0.5, 0.0, 1.0);
    fragColor = vec4(shaded, vertexAlpha);
}
