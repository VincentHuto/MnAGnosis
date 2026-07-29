#version 150

in vec3 localPosition;
in float vertexAlpha;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 ModelPoseMat;
uniform vec3 CameraOrigin;
uniform vec3 RayDirection;
uniform int Perspective;
uniform float MandelbulbTime;
uniform float MorphAmount;
uniform vec3 PaletteBlush;
uniform vec3 PalettePeach;
uniform vec3 PaletteButter;
uniform vec3 PaletteMint;
uniform vec3 PaletteSky;
uniform vec3 PaletteLavender;
uniform float PaletteStopPeach;
uniform float PaletteStopButter;
uniform float PaletteStopMint;
uniform float PaletteStopSky;
uniform float PaletteStopLavender;

out vec4 fragColor;

const int MAX_STEPS = 112;
const int FRACTAL_ITERATIONS = 12;
const float MAX_DISTANCE = 3.25;
const float SURFACE_EPSILON = 0.0012;
const float PROXY_BOUND = 1.24;

mat2 rotate2d(float angle) {
    float cosine = cos(angle);
    float sine = sin(angle);
    return mat2(cosine, -sine, sine, cosine);
}

float mandelbulbDistance(vec3 point, float power) {
    vec3 z = point;
    float derivative = 1.0;
    float radius = 0.0;
    for (int iteration = 0; iteration < FRACTAL_ITERATIONS; iteration++) {
        radius = length(z);
        if (radius > 2.25) {
            break;
        }
        radius = max(radius, 0.000001);
        float theta = acos(clamp(z.z / radius, -1.0, 1.0));
        float phi = atan(z.y, z.x);
        derivative = pow(radius, power - 1.0)
                * power * derivative + 1.0;
        float raisedRadius = pow(radius, power);
        theta *= power;
        phi *= power;
        z = raisedRadius * vec3(
                sin(theta) * cos(phi),
                sin(theta) * sin(phi),
                cos(theta)
        ) + point;
    }
    return 0.5 * log(radius) * radius / derivative;
}

vec3 animatedFractalPoint(vec3 point) {
    float time = MandelbulbTime;
    point.xz = rotate2d(time * 0.31) * point.xz;
    point.xy = rotate2d(time * 0.19 + 0.55) * point.xy;
    return point;
}

float animatedFractalPower() {
    return 8.0 + sin(MandelbulbTime * 0.37) * MorphAmount;
}

float sceneDistance(vec3 point) {
    return mandelbulbDistance(
            animatedFractalPoint(point),
            animatedFractalPower()
    );
}

vec3 estimateNormal(vec3 point) {
    vec2 offset = vec2(SURFACE_EPSILON * 1.5, 0.0);
    return normalize(vec3(
            sceneDistance(point + offset.xyy)
                    - sceneDistance(point - offset.xyy),
            sceneDistance(point + offset.yxy)
                    - sceneDistance(point - offset.yxy),
            sceneDistance(point + offset.yyx)
                    - sceneDistance(point - offset.yyx)
    ));
}

float mandelbulbOrbitValue(vec3 point) {
    vec3 constantPoint = animatedFractalPoint(point);
    float power = animatedFractalPower();
    vec3 z = constantPoint;
    vec4 orbitTrap = vec4(10.0);
    float orbitAccumulation = 0.0;

    for (int iteration = 0; iteration < FRACTAL_ITERATIONS; iteration++) {
        float radius = max(length(z), 0.000001);
        orbitTrap = min(orbitTrap, vec4(abs(z), radius));
        float iterationWeight =
                (float(iteration) + 1.0) / float(FRACTAL_ITERATIONS);
        orbitAccumulation += exp(-abs(radius - 0.72) * 4.0)
                * iterationWeight;
        if (radius > 2.25) {
            break;
        }

        float theta = acos(clamp(z.z / radius, -1.0, 1.0));
        float phi = atan(z.y, z.x);
        float raisedRadius = pow(radius, power);
        theta *= power;
        phi *= power;
        z = raisedRadius * vec3(
                sin(theta) * cos(phi),
                sin(theta) * sin(phi),
                cos(theta)
        ) + constantPoint;
    }

    float trappedDetail = -log(max(
            dot(orbitTrap.xyz, vec3(0.43, 0.31, 0.26))
                    + orbitTrap.w * 0.11,
            0.00001
    ));
    return trappedDetail * 0.58 + orbitAccumulation * 0.12;
}

float featheredRange(float value, float start, float end) {
    return smoothstep(start, end, value);
}

vec3 pastelStepPalette(float value) {
    value = fract(value);
    if (value < PaletteStopPeach) {
        return mix(
                PaletteBlush,
                PalettePeach,
                featheredRange(value, 0.0, PaletteStopPeach)
        );
    }
    if (value < PaletteStopButter) {
        return mix(
                PalettePeach,
                PaletteButter,
                featheredRange(
                        value,
                        PaletteStopPeach,
                        PaletteStopButter
                )
        );
    }
    if (value < PaletteStopMint) {
        return mix(
                PaletteButter,
                PaletteMint,
                featheredRange(
                        value,
                        PaletteStopButter,
                        PaletteStopMint
                )
        );
    }
    if (value < PaletteStopSky) {
        return mix(
                PaletteMint,
                PaletteSky,
                featheredRange(value, PaletteStopMint, PaletteStopSky)
        );
    }
    if (value < PaletteStopLavender) {
        return mix(
                PaletteSky,
                PaletteLavender,
                featheredRange(
                        value,
                        PaletteStopSky,
                        PaletteStopLavender
                )
        );
    }
    return mix(
            PaletteLavender,
            PaletteBlush,
            featheredRange(value, PaletteStopLavender, 1.0)
    );
}

void main() {
    vec3 rayDirection = Perspective == 1
            ? normalize(localPosition - CameraOrigin)
            : normalize(RayDirection);
    vec3 samplePoint = localPosition + rayDirection * SURFACE_EPSILON * 2.0;
    float traveled = 0.0;
    bool hit = false;
    for (int step = 0; step < MAX_STEPS; step++) {
        float distanceToSurface = sceneDistance(samplePoint);
        if (distanceToSurface < SURFACE_EPSILON) {
            hit = true;
            break;
        }
        float stepDistance = max(
                distanceToSurface * 0.72,
                SURFACE_EPSILON * 0.6
        );
        traveled += stepDistance;
        samplePoint += rayDirection * stepDistance;
        if (traveled > MAX_DISTANCE
                || any(greaterThan(abs(samplePoint),
                        vec3(PROXY_BOUND + 0.02)))) {
            break;
        }
    }

    if (!hit) {
        discard;
    }

    vec3 normal = estimateNormal(samplePoint);
    vec3 lightDirection = normalize(vec3(-0.45, 0.72, 0.53));
    float diffuse = max(dot(normal, lightDirection), 0.0);
    float facing = max(dot(normal, -rayDirection), 0.0);
    float rim = pow(1.0 - facing, 2.4);
    float orbitValue = mandelbulbOrbitValue(samplePoint);
    float nestedBands = orbitValue * 2.15
            + sin(orbitValue * 13.0) * 0.11;
    float bandPosition = fract(
            nestedBands + MandelbulbTime * 0.012
    );
    vec3 pastel = pastelStepPalette(bandPosition);
    vec3 shaded = pastel * (0.52 + diffuse * 0.62)
            + pastel * rim * 0.28
            + vec3(0.10, 0.08, 0.14) * (1.0 - diffuse);

    vec4 clipPosition = ProjMat * ModelViewMat * ModelPoseMat
            * vec4(samplePoint, 1.0);
    float normalizedDepth = clipPosition.z / clipPosition.w;
    gl_FragDepth = clamp(normalizedDepth * 0.5 + 0.5, 0.0, 1.0);
    fragColor = vec4(shaded, vertexAlpha);
}
