#version 310 es
// mtl.vert

uniform mat4 mModel;
uniform mat4 mView;
uniform mat4 mProjection;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;

out vec2 texCoord;
out vec3 normal;
out vec4 position;

void main() {
  gl_Position = mProjection * mView * mModel * vPosition;

  texCoord = vTexCoord;

  normal = vNormal;

  position = mModel * vPosition;
}