#version 300 es
layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec2 texCoord;
out vec2 TexCoord;
uniform mat4 u_MVPMatrix;
uniform float uOffset;


void main() {
    vec4 pos = vPosition;
//    pos.z = 0.25 * sin(radians(uOffset) + pos.y * radians(2.0 * 90.0));
    gl_Position = u_MVPMatrix * pos;
    TexCoord = vec2(texCoord.x, 1.0 - texCoord.y);
}