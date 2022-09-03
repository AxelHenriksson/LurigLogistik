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
  gl_FragColor = texture(map_Kd, texCoord) * Kd;
}