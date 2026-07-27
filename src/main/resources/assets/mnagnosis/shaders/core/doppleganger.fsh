#version 150

#moj_import <fog.glsl>

// [VanillaCopy] rendertype_entity_translucent.fsh, changes noted

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

void main() {
    vec4 mask = texture(Sampler0, texCoord0);
    if (mask.a < 0.1) {
        discard;
    }

    vec4 litColor = mask * vertexColor * ColorModulator;
    litColor.rgb = mix(overlayColor.rgb, litColor.rgb, overlayColor.a);
    litColor *= lightMapColor;

    const float INV_SQRT_THREE = 0.57735026919;
    vec2 latticeUv = texCoord0;
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

    float litLuma = dot(litColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 charcoal = litColor.rgb * 0.24 + vec3(litLuma * 0.035);
    float highlightLighting = mix(0.35, 1.0, clamp(litLuma, 0.0, 1.0));
    vec3 offWhite = vec3(0.92) * highlightLighting;
    vec3 highlighted = mix(charcoal, offWhite, band * TriangleBrightness);
    vec3 patterned = mix(highlighted, vec3(0.004), seam);
    vec4 color = vec4(patterned, litColor.a);

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
