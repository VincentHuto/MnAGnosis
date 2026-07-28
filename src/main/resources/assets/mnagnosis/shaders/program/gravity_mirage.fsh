#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MaskSampler;
uniform vec2 InSize;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

float maskAt(vec2 uv) {
    return texture(MaskSampler, clamp(uv, vec2(0.0), vec2(1.0))).r;
}

void main() {
    float mask = smoothstep(0.05, 0.8, maskAt(texCoord));
    vec2 pixels = texCoord * InSize;
    float waveX = sin(pixels.y * 0.115 + Time * 7.3)
            + sin((pixels.x + pixels.y) * 0.047 - Time * 4.1);
    float waveY = cos(pixels.x * 0.091 - Time * 6.2)
            + sin((pixels.x - pixels.y) * 0.039 + Time * 3.7);
    vec2 offset = vec2(waveX, waveY) * 1.15 / InSize;
    vec4 scene = texture(
            DiffuseSampler,
            clamp(texCoord + offset * mask, vec2(0.0), vec2(1.0))
    );

    vec2 texel = 1.0 / InSize;
    float neighbor = max(max(maskAt(texCoord + vec2(texel.x, 0.0)),
                             maskAt(texCoord - vec2(texel.x, 0.0))),
                         max(maskAt(texCoord + vec2(0.0, texel.y)),
                             maskAt(texCoord - vec2(0.0, texel.y))));
    float rim = clamp(neighbor - maskAt(texCoord), 0.0, 1.0);
    float pulse = 0.55 + 0.15 * sin(Time * 5.0);
    scene.rgb = min(vec3(1.0), scene.rgb + vec3(0.55, 0.72, 0.82) * rim * pulse);
    fragColor = scene;
}
