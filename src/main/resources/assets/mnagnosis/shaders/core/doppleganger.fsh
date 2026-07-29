#version 150

#moj_import <fog.glsl>

// [VanillaCopy] rendertype_entity_translucent.fsh, changes noted
//
// Circle-grid pattern adapted from a shader by Nicole Vella (2021):
// https://www.shadertoy.com/view/Wl3BD8
// Licensed under Creative Commons Attribution 4.0 International:
// https://creativecommons.org/licenses/by/4.0/
//
// Fractal-flash pattern adapted from:
// https://www.shadertoy.com/view/lXySzV

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;
in vec3 robeModelPosition;
in vec3 robeModelNormal;

out vec4 fragColor;

uniform float GameTime;
uniform float TriangleScale;
uniform float TriangleLineWidth;
uniform float TriangleBrightness;
uniform float TriangleSpeed;
uniform int ShaderMode;
uniform float CircleGridScale;
uniform float CircleMinRadius;
uniform float CircleMaxRadius;
uniform float CircleSpeed;
uniform float CircleEdgeSoftness;
uniform int FbmOctaves;
uniform float FbmScale;
uniform float FbmSpeed;
uniform float FbmWarpOffset;
uniform float FbmInitialAmplitude;
uniform float FbmGain;
uniform float FbmLacunarity;
uniform float FbmRotation;
uniform float FbmShift;
uniform float FbmIntensity;
uniform float FbmSmoothMin;
uniform float FbmSmoothMax;
uniform float FbmPower;
uniform float FbmSampleOffset;
uniform float FbmGradientDivisor;
uniform float FbmOutputExponent;
uniform float FbmOutputBias;
uniform float FractalFieldScale;
uniform float FractalFlowX;
uniform float FractalFlowY;
uniform float FractalPrimaryCellSize;
uniform float FractalSecondaryCellSize;
uniform int FractalIterations;
uniform float FractalContourWidth;
uniform float FractalBrightness;
uniform float FractalSecondaryBrightness;
uniform float FractalGrowthMin;
uniform float FractalGrowthMax;
uniform float FractalLifecycleSpeed;
uniform float FractalRotationRange;

float trianglePattern(vec2 uv) {
    const float INV_SQRT_THREE = 0.57735026919;
    vec2 latticeUv = uv;
    latticeUv *= mat2(
        1.0, -INV_SQRT_THREE,
        0.0, 2.0 * INV_SQRT_THREE
    ) * TriangleScale;

    vec3 grid = vec3(latticeUv, 1.0 - latticeUv.x - latticeUv.y);
    vec3 cellId = floor(grid);
    vec3 edgeDistance = abs(2.0 * fract(grid) - 1.0);
    float nearestEdge = max(edgeDistance.x, max(edgeDistance.y, edgeDistance.z));
    float edgeSoftness = max(fwidth(nearestEdge), 0.001);
    float seam = smoothstep(
        1.0 - TriangleLineWidth - edgeSoftness,
        1.0 - TriangleLineWidth + edgeSoftness,
        nearestEdge
    );

    float phase = abs(cellId.z)
        + length(cellId * 0.25)
        - GameTime * TriangleSpeed;
    float band = smoothstep(0.10, 0.16, sin(phase));

    return clamp(mix(band * TriangleBrightness, 0.0, seam), 0.0, 1.0);
}

float remapValue(
    float value,
    float min1,
    float max1,
    float min2,
    float max2
) {
    return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
}

float circleMask(vec2 st, vec2 position, float radius) {
    float signedDistance = radius - length(position - st);
    float softness = max(
        fwidth(signedDistance) * CircleEdgeSoftness,
        0.002
    );
    return smoothstep(-softness, softness, signedDistance);
}

float circleGridPattern(vec2 uv) {
    vec2 gridUv = uv * CircleGridScale;
    vec2 indices = floor(gridUv);
    vec2 cellUv = fract(gridUv);

    float animationPhase = GameTime * CircleSpeed;
    float circleRadius = remapValue(
        cos(animationPhase * 2.0),
        -1.0,
        1.0,
        CircleMaxRadius,
        CircleMinRadius
    );
    float animation0to1 = remapValue(
        cos(animationPhase),
        -1.0,
        1.0,
        0.01,
        0.99
    );
    float animation1to0 = remapValue(
        cos(animationPhase),
        -1.0,
        1.0,
        0.99,
        0.01
    );

    float circleTop = circleMask(
        cellUv,
        vec2(animation0to1, 1.0),
        circleRadius
    );
    float circleBottom = circleMask(
        cellUv,
        vec2(animation1to0, 0.0),
        circleRadius
    );
    float circleLeft = circleMask(
        cellUv,
        vec2(0.0, animation1to0),
        circleRadius
    );
    float circleRight = circleMask(
        cellUv,
        vec2(1.0, animation0to1),
        circleRadius
    );

    bool invertedCell = (
        mod(indices.x, 2.0) == 0.0
        && mod(indices.y, 2.0) == 1.0
    ) || (
        mod(indices.x, 2.0) == 1.0
        && mod(indices.y, 2.0) == 0.0
    );

    if (invertedCell) {
        return 1.0 - max(circleTop, circleBottom);
    }
    return max(circleRight, circleLeft);
}

float randomValue(vec2 st) {
    return fract(sin(dot(st, vec2(78.233, 12.988))) * 5462.543);
}

float valueNoise(vec2 st) {
    vec2 cell = floor(st);
    vec2 local = fract(st);
    vec2 blend = local * local * (3.0 - 2.0 * local);

    float bottomLeft = randomValue(cell);
    float bottomRight = randomValue(cell + vec2(1.0, 0.0));
    float topLeft = randomValue(cell + vec2(0.0, 1.0));
    float topRight = randomValue(cell + vec2(1.0, 1.0));

    return mix(
        mix(bottomLeft, bottomRight, blend.x),
        mix(topLeft, topRight, blend.x),
        blend.y
    );
}

float fbmValue(vec2 st) {
    float value = 0.0;
    float amplitude = FbmInitialAmplitude;
    mat2 rotation = mat2(
        cos(FbmRotation),
        sin(FbmRotation),
        -sin(FbmRotation),
        cos(FbmRotation)
    );

    for (int octave = 0; octave < 16; octave++) {
        if (octave >= FbmOctaves) {
            break;
        }
        value += amplitude * valueNoise(st);
        amplitude *= FbmGain;
        st = rotation * st * FbmLacunarity + vec2(FbmShift);
    }
    return value;
}

float fbmImage(vec2 uv) {
    vec2 st = uv;
    st.x -= 0.5;
    st *= FbmScale;

    const float PI = 3.1415926;
    float first = fbmValue(st);
    float second = fbmValue(st * PI);
    float warped = fbmValue(
        vec2(first, second) * PI
        + vec2(GameTime * FbmSpeed + FbmWarpOffset)
    );

    warped *= FbmIntensity;
    warped = smoothstep(FbmSmoothMin, FbmSmoothMax, warped);
    return pow(max(warped, 0.0), FbmPower);
}

float fbmPattern(vec2 uv) {
    float value = fbmImage(uv);
    float neighbor = fbmImage(uv + vec2(FbmSampleOffset));
    float gradient = max(clamp(value - neighbor, 0.0, 1.0), 0.0001);
    float divided = value / gradient;
    float shaped = pow(
        max(divided / max(FbmGradientDivisor, 0.0001), 0.0),
        FbmOutputExponent
    );
    return clamp(shaped - FbmOutputBias, 0.0, 1.0);
}

float hashCell(vec2 cell) {
    return fract(sin(dot(cell, vec2(127.1, 311.7))) * 43758.5453123);
}

vec2 hashCell2(vec2 cell) {
    return vec2(
        hashCell(cell + vec2(19.19, 7.73)),
        hashCell(cell + vec2(83.17, 41.53))
    );
}

mat2 rotation2d(float angle) {
    float sine = sin(angle);
    float cosine = cos(angle);
    return mat2(cosine, -sine, sine, cosine);
}

vec2 complexSquare(vec2 value) {
    return vec2(
        value.x * value.x - value.y * value.y,
        2.0 * value.x * value.y
    );
}

vec2 complexMultiply(vec2 left, vec2 right) {
    return vec2(
        left.x * right.x - left.y * right.y,
        left.x * right.y + left.y * right.x
    );
}

vec2 planarRobePosition(vec3 position, vec3 modelNormal) {
    vec3 unitNormal = normalize(modelNormal);
    vec3 referenceAxis = abs(unitNormal.y) < 0.9
        ? vec3(0.0, 1.0, 0.0)
        : vec3(0.0, 0.0, 1.0);
    vec3 tangent = normalize(cross(referenceAxis, unitNormal));
    vec3 bitangent = cross(unitNormal, tangent);
    return vec2(
        dot(position, tangent),
        dot(position, bitangent)
    );
}

float mandelbrotDistance(
    vec2 constantValue,
    int iterationLimit,
    out float escapeIteration
) {
    vec2 z = vec2(0.0);
    vec2 derivative = vec2(0.0);
    float escaped = 0.0;
    float squaredRadius = 0.0;
    escapeIteration = float(iterationLimit);

    for (int iteration = 0; iteration < 48; iteration++) {
        if (iteration >= iterationLimit) {
            break;
        }
        derivative = 2.0 * complexMultiply(z, derivative)
            + vec2(1.0, 0.0);
        z = complexSquare(z) + constantValue;
        squaredRadius = dot(z, z);
        if (squaredRadius > 256.0) {
            escaped = 1.0;
            escapeIteration = float(iteration + 1);
            break;
        }
    }

    if (escaped < 0.5) {
        return -1.0;
    }
    float radius = sqrt(max(squaredRadius, 1.0001));
    return 0.5 * log(max(squaredRadius, 1.0001)) * radius
        / max(length(derivative), 0.0001);
}

float nurseryLayer(vec2 fieldPosition, float cellSize, float seedOffset) {
    float safeCellSize = max(cellSize, 0.001);
    vec2 gridPosition = fieldPosition / safeCellSize;
    vec2 cell = floor(gridPosition);
    vec2 local = fract(gridPosition) - 0.5;
    float baseSeed = hashCell(cell + vec2(seedOffset));
    float cycle = GameTime * max(FractalLifecycleSpeed, 0.001)
        + baseSeed
        + seedOffset * 0.137;
    float generation = floor(cycle);
    float lifecycle = fract(cycle);
    vec2 generationKey = cell
        + vec2(seedOffset)
        + vec2(generation * 17.17, generation * 43.31);
    float seed = hashCell(generationKey);
    vec2 centerJitter = (
        hashCell2(generationKey) - 0.5
    ) * 0.64;
    float birth = smoothstep(0.02, 0.20, lifecycle);
    float death = 1.0 - smoothstep(0.70, 0.98, lifecycle);
    float lifeOpacity = birth * death;
    float growthPhase = smoothstep(0.03, 0.76, lifecycle);
    float sizeVariation = mix(
        0.68,
        1.28,
        hashCell(generationKey + vec2(29.73, 11.91))
    );
    float growth = mix(
        max(FractalGrowthMin, 0.05),
        max(FractalGrowthMax, FractalGrowthMin + 0.01),
        growthPhase
    ) * sizeVariation;
    float angle = (seed - 0.5) * FractalRotationRange;
    vec2 budPosition = rotation2d(angle)
        * (local - centerJitter)
        / growth;

    float evolution = smoothstep(0.16, 0.82, lifecycle);
    float boundaryAngle = 6.2831855 * hashCell(
        generationKey + vec2(71.37, 53.19)
    );
    vec2 boundaryTarget = vec2(
        0.5 * cos(boundaryAngle) - 0.25 * cos(2.0 * boundaryAngle),
        0.5 * sin(boundaryAngle) - 0.25 * sin(2.0 * boundaryAngle)
    );
    vec2 viewCenter = mix(vec2(-0.5, 0.0), boundaryTarget, evolution);
    float detailScale = mix(1.0, 0.20, evolution);
    vec2 constantValue = viewCenter
        + vec2(budPosition.x * 3.0, budPosition.y * 2.4)
            * detailScale;
    float activeIterations = mix(
        14.0,
        float(clamp(FractalIterations, 8, 48)),
        evolution
    );
    int activeIterationLimit = int(floor(activeIterations));
    float escapeIteration;
    float distance = mandelbrotDistance(
        constantValue,
        activeIterationLimit,
        escapeIteration
    );
    if (distance < 0.0) {
        return 0.0;
    }

    float width = max(FractalContourWidth, 0.0001) / growth;
    float antialias = max(
        min(fwidth(distance), width * 0.75),
        width * 0.15
    );
    float contour = 1.0 - smoothstep(
        width,
        width + antialias,
        distance
    );
    float newestDetail = smoothstep(
        max(activeIterations - 4.0, 1.0),
        activeIterations,
        escapeIteration
    );
    float cellEdge = max(abs(local.x), abs(local.y));
    float cellFade = 1.0 - smoothstep(0.44, 0.50, cellEdge);
    return contour
        * lifeOpacity
        * mix(0.62, 1.0, newestDetail)
        * cellFade;
}

float fractalNurseryPattern(vec3 modelPosition, vec3 modelNormal) {
    vec2 projected = planarRobePosition(modelPosition, modelNormal);
    vec2 flow = vec2(FractalFlowX, FractalFlowY) * GameTime;
    flow.x += sin(GameTime * 240.0) * 0.035;
    vec2 organicWarp = vec2(
        valueNoise(projected * 1.37 + vec2(13.71, 4.93)),
        valueNoise(projected * 1.37 + vec2(-7.19, 21.37))
    ) - 0.5;
    vec2 field = projected * max(FractalFieldScale, 0.001)
        + flow
        + organicWarp * 0.30;

    float primary = nurseryLayer(
        field,
        FractalPrimaryCellSize,
        0.0
    );
    float secondary = nurseryLayer(
        field + vec2(0.37, 0.61),
        FractalSecondaryCellSize,
        17.0
    );
    float filaments = max(
        primary,
        secondary * FractalSecondaryBrightness
    );
    return clamp(filaments * FractalBrightness, 0.0, 1.0);
}

void main() {
    // Coverage comes from the rendered armor geometry; no mask texture is sampled.
    vec4 litColor = vertexColor * ColorModulator;
    litColor.rgb = mix(overlayColor.rgb, litColor.rgb, overlayColor.a);
    litColor *= lightMapColor;

    float pattern;
    if (ShaderMode == 3) {
        pattern = fractalNurseryPattern(
            robeModelPosition,
            robeModelNormal
        );
    } else if (ShaderMode == 2) {
        pattern = fbmPattern(texCoord0);
    } else if (ShaderMode == 1) {
        pattern = circleGridPattern(texCoord0);
    } else {
        pattern = trianglePattern(texCoord0);
    }

    float litLuma = dot(litColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 charcoal = litColor.rgb * 0.24 + vec3(litLuma * 0.035);
    if (ShaderMode == 3) {
        charcoal *= 0.38;
    }
    float highlightLighting = mix(0.35, 1.0, clamp(litLuma, 0.0, 1.0));
    vec3 offWhite = vec3(0.92) * highlightLighting;
    vec3 patterned = mix(charcoal, offWhite, pattern);
    vec4 color = vec4(patterned, litColor.a);

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
