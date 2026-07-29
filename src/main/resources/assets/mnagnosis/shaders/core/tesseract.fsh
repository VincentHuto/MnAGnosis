#version 150

in vec3 localPosition;
in float vertexAlpha;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 ModelPoseMat;
uniform vec3 CameraOrigin;
uniform vec3 RayDirection;
uniform int Perspective;
uniform float TesseractTime;
uniform float TesseractAngleXw;
uniform float TesseractAngleYz;
uniform float TesseractPulse;
uniform vec3 PaletteVoid;
uniform vec3 PaletteCyan;
uniform vec3 PaletteAzure;
uniform vec3 PaletteViolet;
uniform vec3 PalettePearl;
uniform vec3 PaletteGold;
uniform float PaletteBrightness;
uniform float PaletteGlowStrength;
uniform float TesseractTubeRadius;

out vec4 fragColor;

const int MAX_MARCH_STEPS = 64;
const float MAX_DISTANCE = 2.35;
const float SURFACE_EPSILON = 0.0012;
const float PROXY_BOUND = 0.78;
const float PROJECTION_DISTANCE = 3.4;
const float PROJECTED_SCALE = 0.30;

vec4 hypercubeVertex(int index) {
    return vec4(
            (index & 1) == 0 ? -1.0 : 1.0,
            (index & 2) == 0 ? -1.0 : 1.0,
            (index & 4) == 0 ? -1.0 : 1.0,
            (index & 8) == 0 ? -1.0 : 1.0
    );
}

vec3 projectTesseractVertex(int index) {
    vec4 vertex = hypercubeVertex(index);
    float cosineXw = cos(TesseractAngleXw);
    float sineXw = sin(TesseractAngleXw);
    float cosineYz = cos(TesseractAngleYz);
    float sineYz = sin(TesseractAngleYz);

    float x = vertex.x * cosineXw - vertex.w * sineXw;
    float w = vertex.x * sineXw + vertex.w * cosineXw;
    float y = vertex.y * cosineYz - vertex.z * sineYz;
    float z = vertex.y * sineYz + vertex.z * cosineYz;
    float projection = PROJECTION_DISTANCE
            / (PROJECTION_DISTANCE - w);
    return vec3(x, y, z) * projection * PROJECTED_SCALE;
}

float segmentDistance(vec3 point, vec3 start, vec3 end) {
    vec3 segment = end - start;
    float segmentLengthSquared = max(dot(segment, segment), 0.000001);
    float position = clamp(
            dot(point - start, segment) / segmentLengthSquared,
            0.0,
            1.0
    );
    return length(point - (start + segment * position));
}

float tesseractDistance(vec3 point) {
    vec3 vertices[16];
    for (int index = 0; index < 16; index++) {
        vertices[index] = projectTesseractVertex(index);
    }

    float nearest = 10.0;
    for (int vertex = 0; vertex < 16; vertex++) {
        for (int dimension = 0; dimension < 4; dimension++) {
            int bit = 1 << dimension;
            if ((vertex & bit) == 0) {
                nearest = min(
                        nearest,
                        segmentDistance(
                                point,
                                vertices[vertex],
                                vertices[vertex | bit]
                        )
                );
            }
        }
    }

    float pulseRadius = TesseractTubeRadius
            * (0.84 + TesseractPulse * 0.32);
    return nearest - pulseRadius;
}

vec3 estimateNormal(vec3 point) {
    vec2 offset = vec2(SURFACE_EPSILON * 1.7, 0.0);
    return normalize(vec3(
            tesseractDistance(point + offset.xyy)
                    - tesseractDistance(point - offset.xyy),
            tesseractDistance(point + offset.yxy)
                    - tesseractDistance(point - offset.yxy),
            tesseractDistance(point + offset.yyx)
                    - tesseractDistance(point - offset.yyx)
    ));
}

vec3 tesseractPalette(float value) {
    float scaled = fract(value) * 5.0;
    if (scaled < 1.0) {
        return mix(PaletteCyan, PaletteAzure,
                smoothstep(0.0, 1.0, scaled));
    }
    if (scaled < 2.0) {
        return mix(PaletteAzure, PaletteViolet,
                smoothstep(0.0, 1.0, scaled - 1.0));
    }
    if (scaled < 3.0) {
        return mix(PaletteViolet, PalettePearl,
                smoothstep(0.0, 1.0, scaled - 2.0));
    }
    if (scaled < 4.0) {
        return mix(PalettePearl, PaletteGold,
                smoothstep(0.0, 1.0, scaled - 3.0));
    }
    return mix(PaletteGold, PaletteCyan,
            smoothstep(0.0, 1.0, scaled - 4.0));
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
        float distanceToSurface = tesseractDistance(samplePoint);
        if (distanceToSurface < SURFACE_EPSILON) {
            hit = true;
            break;
        }
        float stepDistance = max(
                distanceToSurface * 0.82,
                SURFACE_EPSILON * 0.60
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
    vec3 lightDirection = normalize(vec3(-0.46, 0.76, 0.46));
    vec3 halfDirection = normalize(lightDirection - rayDirection);
    float diffuse = max(dot(normal, lightDirection), 0.0);
    float facing = max(dot(normal, -rayDirection), 0.0);
    float rim = pow(1.0 - facing, 2.2);
    float specular = pow(max(dot(normal, halfDirection), 0.0), 48.0);

    float energyFlow = dot(
            samplePoint,
            vec3(2.3, 3.1, 4.7)
    ) - TesseractTime * 0.18;
    vec3 energyColor = tesseractPalette(energyFlow);
    float pulseGlow = 0.82 + TesseractPulse * 0.36;
    vec3 shaded = mix(
            PaletteVoid,
            energyColor,
            0.56 + diffuse * 0.44
    );
    shaded += energyColor * rim * PaletteGlowStrength * 0.52;
    shaded += PalettePearl * specular * 0.72;
    shaded += PaletteCyan
            * (1.0 - diffuse)
            * PaletteGlowStrength
            * 0.11;
    shaded *= PaletteBrightness * pulseGlow;

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
