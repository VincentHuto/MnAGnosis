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
uniform float FractalPhase;
uniform float FractalFlash;
uniform int FractalIterations;
uniform float FractalModulus;
uniform float FractalColorSpeed;
uniform float FractalScaleBase;
uniform float FractalScaleAmplitude;
uniform float FractalScaleSpeed;
uniform float FractalRotationSpeed;
uniform float FractalRotationOffset;
uniform float FractalOrbitX;
uniform float FractalOrbitY;
uniform float FractalOrbitXSpeed;
uniform float FractalOrbitYSpeed;
uniform float FractalDriftSpeed;
uniform float FractalRadiusSmoothMax;
uniform float FractalLengthOffset;
uniform float FractalEdgeWidth;
uniform float FractalDensityFade;
uniform float FractalPixelSize;
uniform int FractalBlurSamples;
uniform float FractalMotionBlurScale;
uniform float FractalAaBlurScale;
uniform float FractalBrightness;

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

vec3 hsvColor(float hue, float saturation, float value) {
    vec3 rgb = clamp(
        abs(fract(hue + vec3(3.0, 2.0, 1.0) / 3.0) * 6.0 - 3.0) - 1.0,
        0.0,
        1.0
    );
    return mix(vec3(1.0), rgb, saturation) * value;
}

mat2 rotation2d(float angle) {
    float sine = sin(angle);
    float cosine = cos(angle);
    return mat2(cosine, -sine, sine, cosine);
}

float fractalFormula(vec2 position, vec2 constantValue) {
    float modulusValue = max(FractalModulus, 0.0001);
    float colorValue = length(position);
    float visibility = 1.0;
    float squaredLength = max(dot(position, position), 0.0001);
    float pointLength = sqrt(squaredLength);
    float radius = smoothstep(
        0.0,
        max(FractalRadiusSmoothMax, 0.0001),
        pointLength
    );

    for (int iteration = 0; iteration < 16; iteration++) {
        if (iteration >= FractalIterations) {
            break;
        }

        position = abs(
            mod(position / squaredLength + constantValue, modulusValue)
            - modulusValue / 2.0
        );

        squaredLength = max(dot(position, position), 0.0001);
        pointLength = sqrt(max(squaredLength - FractalLengthOffset, 0.0));

        float edgeWidth = max(FractalEdgeWidth, 0.0001);
        visibility *= smoothstep(
            0.0,
            edgeWidth,
            abs(modulusValue / 2.0 - position.x) * pointLength
        );
        visibility *= smoothstep(
            0.0,
            edgeWidth,
            abs(modulusValue / 2.0 - position.y) * pointLength
        );
        visibility *= smoothstep(
            0.0,
            edgeWidth,
            abs(position.x) * 2.0
        );
        visibility *= smoothstep(
            0.0,
            edgeWidth,
            abs(position.y) * 2.0
        );

        radius *= smoothstep(
            0.0,
            max(FractalDensityFade, 0.0001),
            pointLength
        );
        colorValue += hsvColor(
            1.0 - max(position.x, position.y)
                + visibility * 2.0
                + GameTime * FractalColorSpeed,
            2.0 + FractalFlash - pointLength + visibility,
            radius
        ).z;
    }

    float bands = 1.0 - pow(
        abs(cos(colorValue / 2.0 - FractalPhase)),
        0.5
    );
    return clamp(
        bands * visibility * FractalBrightness,
        0.0,
        1.0
    );
}

float fractalFlashPattern(vec2 uv) {
    vec2 position = -1.0 + 2.0 * uv;
    position += 0.5;
    position *= FractalScaleBase
        + FractalScaleAmplitude * sin(GameTime * FractalScaleSpeed);
    position = rotation2d(
        GameTime * FractalRotationSpeed + FractalRotationOffset
    ) * position;

    vec2 orbit = vec2(
        FractalOrbitX + sin(GameTime * FractalOrbitXSpeed),
        FractalOrbitY + cos(GameTime * FractalOrbitYSpeed)
    );
    vec2 constantValue = GameTime * FractalDriftSpeed * orbit - orbit;

    int sampleCount = clamp(FractalBlurSamples, 1, 32);
    float samplesPerSide = sqrt(float(sampleCount));
    float orbitLength = max(length(orbit), 0.0001);
    float motionBlurAmount = FractalPixelSize
        / orbitLength
        / float(sampleCount)
        * FractalMotionBlurScale;
    float aaBlurAmount = FractalPixelSize
        / samplesPerSide
        * FractalAaBlurScale;

    float colorValue = 0.0;
    for (int sampleIndex = 0; sampleIndex < 32; sampleIndex++) {
        if (sampleIndex >= sampleCount) {
            break;
        }

        float sampleValue = float(sampleIndex);
        vec2 aaOffset = vec2(
            mod(sampleValue, samplesPerSide) * aaBlurAmount,
            sampleValue / samplesPerSide * aaBlurAmount
        );
        colorValue += fractalFormula(
            position + aaOffset,
            constantValue + orbit * motionBlurAmount * sampleValue
        );
    }

    return clamp(colorValue / float(sampleCount), 0.0, 1.0);
}

void main() {
    // Coverage comes from the rendered armor geometry; no mask texture is sampled.
    vec4 litColor = vertexColor * ColorModulator;
    litColor.rgb = mix(overlayColor.rgb, litColor.rgb, overlayColor.a);
    litColor *= lightMapColor;

    float pattern;
    if (ShaderMode == 3) {
        pattern = fractalFlashPattern(texCoord0);
    } else if (ShaderMode == 2) {
        pattern = fbmPattern(texCoord0);
    } else if (ShaderMode == 1) {
        pattern = circleGridPattern(texCoord0);
    } else {
        pattern = trianglePattern(texCoord0);
    }

    float litLuma = dot(litColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 charcoal = litColor.rgb * 0.24 + vec3(litLuma * 0.035);
    float highlightLighting = mix(0.35, 1.0, clamp(litLuma, 0.0, 1.0));
    vec3 offWhite = vec3(0.92) * highlightLighting;
    vec3 patterned = mix(charcoal, offWhite, pattern);
    vec4 color = vec4(patterned, litColor.a);

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
