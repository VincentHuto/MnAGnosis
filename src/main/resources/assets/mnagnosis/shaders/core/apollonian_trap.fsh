// Created by evilryu.
// Modified by mla, 2018.
// Minecraft item-render adaptation by the MnAGnosis project.
// Source: https://www.shadertoy.com/view/MlSBzW
// Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported.

#version 150

in vec3 localPosition;
in float vertexAlpha;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 ModelPoseMat;
uniform vec3 CameraOrigin;
uniform vec3 RayDirection;
uniform int Perspective;
uniform float ApollonianTime;
uniform vec3 PaletteOrbitCyan;
uniform vec3 PaletteSurfaceWhite;
uniform vec3 PaletteTrapRed;
uniform vec3 PaletteKeyLight;
uniform vec3 PaletteBackLight;
uniform vec3 PaletteSpecular;

out vec4 fragColor;

const int MAX_STEPS = 128;
const int APOLLONIAN_ITERATIONS = 10;
const float MAX_DISTANCE = 5.2;
const float SURFACE_EPSILON = 0.0011;

mat2 rotate2d(float angle) {
    float cosine = cos(angle);
    float sine = sin(angle);
    return mat2(cosine, -sine, sine, cosine);
}

vec3 rotateReferenceField(vec3 point) {
    point.zx = rotate2d(0.1 * ApollonianTime) * point.zx;
    return point;
}

vec3 centeredCubeRepeat(vec3 point) {
    return point - 2.0 * round(0.5 * point);
}

float crossPlaneDistance(vec3 point) {
    return min(
            abs(point.z) + abs(point.x),
            min(
                    abs(point.x) + abs(point.y),
                    abs(point.y) + abs(point.z)
            )
    );
}

float referenceMorphRadius() {
    return 1.0 + 0.2 * cos(0.123 * ApollonianTime);
}

vec3 outerInversion(vec3 point, out float inversionScale) {
    inversionScale = 4.0 / max(dot(point, point), 0.000001);
    return point * inversionScale + vec3(1.0);
}

float referenceApollonian(
        vec3 point,
        float inversionRadiusSquared,
        out vec4 orbitTrap
) {
    float conformalScale = 1.0;
    orbitTrap = vec4(1000.0);

    for (int iteration = 0;
            iteration < APOLLONIAN_ITERATIONS;
            iteration++) {
        point = centeredCubeRepeat(point);
        float radiusSquared = max(dot(point, point), 0.000001);
        orbitTrap = min(
                orbitTrap,
                vec4(abs(point), radiusSquared)
        );
        float inversion =
                inversionRadiusSquared / radiusSquared;
        point *= inversion;
        conformalScale *= inversion;
    }

    return crossPlaneDistance(point) / conformalScale;
}

float referenceMap(
        vec3 worldPoint,
        float inversionRadiusSquared,
        out vec4 orbitTrap
) {
    vec3 point = rotateReferenceField(worldPoint);
    float inversionScale;
    point = outerInversion(point, inversionScale);
    point.y += 0.1 * ApollonianTime;
    return 0.25 * referenceApollonian(
            point,
            inversionRadiusSquared,
            orbitTrap
    ) / inversionScale;
}

float referenceMap(vec3 point, float inversionRadiusSquared) {
    vec4 discardedOrbit;
    return referenceMap(
            point,
            inversionRadiusSquared,
            discardedOrbit
    );
}

vec3 referenceNormal(
        vec3 point,
        float inversionRadiusSquared
) {
    vec2 offset = vec2(SURFACE_EPSILON * 1.8, 0.0);
    return normalize(vec3(
            referenceMap(
                    point + offset.xyy,
                    inversionRadiusSquared
            ) - referenceMap(
                    point - offset.xyy,
                    inversionRadiusSquared
            ),
            referenceMap(
                    point + offset.yxy,
                    inversionRadiusSquared
            ) - referenceMap(
                    point - offset.yxy,
                    inversionRadiusSquared
            ),
            referenceMap(
                    point + offset.yyx,
                    inversionRadiusSquared
            ) - referenceMap(
                    point - offset.yyx,
                    inversionRadiusSquared
            )
    ));
}

vec3 toneMap(vec3 color) {
    const float a = 2.51;
    const float b = 0.03;
    const float c = 2.43;
    const float d = 0.59;
    const float e = 0.14;
    return (color * (a * color + b))
            / (color * (c * color + d) + e);
}

vec3 referenceMaterial(
        vec3 point,
        vec3 rayDirection,
        float inversionRadiusSquared
) {
    vec4 orbitTrap;
    referenceMap(point, inversionRadiusSquared, orbitTrap);
    vec3 normal = referenceNormal(
            point,
            inversionRadiusSquared
    );

    vec3 lightDirection = normalize(vec3(-12.0, 2.0, -7.0));
    lightDirection.zx =
            rotate2d(0.1 * ApollonianTime) * lightDirection.zx;

    float ambientOcclusion = pow(
            clamp(orbitTrap.w * 2.0, 0.0, 1.0),
            1.2
    );
    float cyanAmount = pow(
            clamp(orbitTrap.w, 0.0, 1.0),
            2.0
    );
    vec3 orbitCyan = cyanAmount * PaletteOrbitCyan;
    vec3 surfaceColor = mix(
            PaletteSurfaceWhite,
            PaletteTrapRed,
            clamp(3.5 * orbitTrap.y, 0.0, 1.0)
    );

    float ambient = 0.5 + 0.5 * normal.y;
    float diffuse = max(dot(normal, lightDirection), 0.0);
    float back = max(dot(normal, -lightDirection), 0.0);
    float lowerBack = max(
            dot(normal, -vec3(0.0, 1.0, 0.0)),
            0.0
    );
    float specular = pow(
            clamp(
                    dot(
                            lightDirection,
                            reflect(rayDirection, normal)
                    ),
                    0.0,
                    1.0
            ),
            64.0
    );

    vec3 lighting = vec3(0.0);
    lighting += PaletteSurfaceWhite
            * 0.5 * ambient * ambientOcclusion;
    lighting += PaletteKeyLight
            * 5.0 * diffuse * ambientOcclusion;
    lighting += PaletteBackLight
            * back * ambientOcclusion;
    lighting += PaletteBackLight
            * lowerBack * ambientOcclusion;
    lighting += PaletteSpecular * specular * 6.2;

    vec3 color = (lighting * surfaceColor - orbitCyan) * 0.2;
    color = toneMap(max(color, vec3(0.0)));
    return pow(
            clamp(color, vec3(0.0), vec3(1.0)),
            vec3(0.45)
    );
}

void main() {
    vec3 rayDirection = Perspective == 1
            ? normalize(localPosition - CameraOrigin)
            : normalize(RayDirection);
    vec3 samplePoint =
            localPosition + rayDirection * SURFACE_EPSILON * 2.0;
    float inversionRadiusSquared = referenceMorphRadius();
    float traveled = 0.0;
    bool hit = false;

    for (int step = 0; step < MAX_STEPS; step++) {
        float distanceToSurface = referenceMap(
                samplePoint,
                inversionRadiusSquared
        );
        float hitThreshold =
                SURFACE_EPSILON * (1.0 + traveled * 0.12);
        if (distanceToSurface < hitThreshold) {
            hit = true;
            break;
        }

        float stepDistance = max(
                distanceToSurface * 0.82,
                SURFACE_EPSILON * 0.55
        );
        traveled += stepDistance;
        samplePoint += rayDirection * stepDistance;
        if (traveled > MAX_DISTANCE) {
            break;
        }
    }

    if (!hit) {
        discard;
    }

    vec3 color = referenceMaterial(
            samplePoint,
            rayDirection,
            inversionRadiusSquared
    );

    vec4 clipPosition = ProjMat * ModelViewMat * ModelPoseMat
            * vec4(samplePoint, 1.0);
    float normalizedDepth = clipPosition.z / clipPosition.w;
    gl_FragDepth = clamp(
            normalizedDepth * 0.5 + 0.5,
            0.0,
            1.0
    );
    fragColor = vec4(color, vertexAlpha);
}
