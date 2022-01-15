#version 300 es
#extension GL_OES_EGL_image_external : enable
#extension GL_OES_EGL_image_external_essl3 : enable
precision mediump float;
in vec2 TexCoord;
out vec4 color;
uniform samplerExternalOES ourTexture;


void main() {
    color = texture(ourTexture, TexCoord);
//    color = vec4(1.0,1.0,1.0,1.0);
}