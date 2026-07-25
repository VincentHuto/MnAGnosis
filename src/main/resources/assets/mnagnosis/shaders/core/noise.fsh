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

uniform float BotaniaGrainIntensity;
uniform float GameTime;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    // Aura strength is carried in vertex alpha so every Truth can grow independently.
    float auraStrength = clamp(vertexColor.a, 0.0, 1.0);
    float r = rand(texCoord0 + vec2(vertexDistance * 0.025, GameTime * 31.0));
    float grain = mix(BotaniaGrainIntensity, 0.42, auraStrength);
    float staticBit = step(1.0 - grain, r);
    float charcoal = mix(0.005, 0.18, staticBit) + r * grain * 0.08;
    color = vec4(vec3(charcoal), color.a * mix(0.35, 0.95, auraStrength));

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
