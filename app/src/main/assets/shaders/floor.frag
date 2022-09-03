#version 310 es
// mtl.frag

precision mediump float;

uniform vec4 Kd;
uniform sampler2D map_Kd;

in vec2 texCoord;
in vec3 normal;
in vec4 position;

out vec4 gl_FragColor;

void main() {
    float intensity = 1.0 - (0.2*pow(floor(mod(position.x, 2.0)) - floor(mod(position.y, 2.0)), 2.0));
    gl_FragColor = vec4(intensity, intensity, intensity, 1.0);
}