#version 150

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 InversePose;
uniform vec4 ColorModulator;

out vec3 localPosition;
out float vertexAlpha;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    localPosition = (InversePose * vec4(Position, 1.0)).xyz;
    vertexAlpha = Color.a * ColorModulator.a;
}
