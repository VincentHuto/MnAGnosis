#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 IViewRotMat;
uniform int FogShape;
uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;
uniform float GameTime;

out float vertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;
out vec4 normal;
out float glitchProgress;

float hash(float value) {
    return fract(sin(value * 91.3458) * 47453.5453);
}

void main() {
    // Color alpha carries this entity render's finale progress; no global mutable uniform is used.
    glitchProgress = clamp(Color.a, 0.0, 1.0);
    float scanline = floor(Position.y * 12.0 + GameTime * 160.0);
    float direction = hash(scanline) * 2.0 - 1.0;
    float fragment = step(0.42, hash(scanline * 1.91 + Position.x * 3.7));
    float displacement = direction * fragment * glitchProgress * glitchProgress * 0.13;
    vec3 displacedPosition = Position + vec3(displacement, 0.0, 0.0);

    gl_Position = ProjMat * ModelViewMat * vec4(displacedPosition, 1.0);
    vertexDistance = fog_distance(ModelViewMat, IViewRotMat * displacedPosition, FogShape);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    texCoord0 = UV0;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}
