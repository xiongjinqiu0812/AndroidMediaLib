package com.jonxiong.opengllib.model

//立方体
class LightCube {
    companion object {
        const val VERTEX_POSITION_SIZE = 3
        const val TEXTURE_POSITION_SIZE = 2
        const val LIGHT_POSITION_SIZE = 3
    }
    var vertices = floatArrayOf(
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f,  0.0f, -1.0f,
        0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,  0.0f, -1.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f, 0.0f,  0.0f, -1.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f, 0.0f,  0.0f, -1.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,  0.0f, -1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, 0.0f,  0.0f, -1.0f,

        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f,  0.0f, 1.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f,  0.0f, 1.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f,  0.0f, 1.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f,  0.0f, 1.0f,
        -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,  0.0f, 1.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f,  0.0f, 1.0f,

        -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, -1.0f,  0.0f,  0.0f,
        -0.5f, 0.5f, -0.5f, 1.0f, 1.0f, -1.0f,  0.0f,  0.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f, -1.0f,  0.0f,  0.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f, -1.0f,  0.0f,  0.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, -1.0f,  0.0f,  0.0f,
        -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, -1.0f,  0.0f,  0.0f,

        0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 1.0f,  0.0f,  0.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f, 1.0f,  0.0f,  0.0f,
        0.5f, -0.5f, -0.5f, 0.0f, 1.0f, 1.0f,  0.0f,  0.0f,
        0.5f, -0.5f, -0.5f, 0.0f, 1.0f, 1.0f,  0.0f,  0.0f,
        0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,  0.0f,  0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 1.0f,  0.0f,  0.0f,

        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f, 0.0f, -1.0f,  0.0f,
        0.5f, -0.5f, -0.5f, 1.0f, 1.0f, 0.0f, -1.0f,  0.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, -1.0f,  0.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, -1.0f,  0.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, -1.0f,  0.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f, 0.0f, -1.0f,  0.0f,

        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,  1.0f,  0.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f, 0.0f,  1.0f,  0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,  1.0f,  0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,  1.0f,  0.0f,
        -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 0.0f,  1.0f,  0.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,  1.0f,  0.0f
    )
}