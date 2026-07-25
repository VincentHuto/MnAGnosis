#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GameTime;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;
in float glitchProgress;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(41.241, 289.732))) * 54231.127);
}

void main() {
    vec4 sampled = texture(Sampler0, texCoord0);
    if (sampled.a < 0.1) {
        discard;
    }

    float band = floor(texCoord0.y * 96.0 + GameTime * 140.0);
    float staticValue = hash(vec2(band, floor(texCoord0.x * 31.0)));
    float dissolve = smoothstep(0.76, 0.98, glitchProgress);
    if (staticValue < dissolve * 0.62) {
        discard;
    }

    float whiteBand = step(0.48, hash(vec2(band * 1.7, GameTime * 37.0)));
    vec3 digital = mix(vec3(0.0), vec3(1.0), whiteBand);
    float fade = 1.0 - smoothstep(0.82, 1.0, glitchProgress);
    vec4 color = vec4(digital, sampled.a * fade);
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
