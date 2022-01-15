package com.jonxiong.opengllib.model

class SurfaceRect(val lines: Int = 100) {

    companion object {
        const val VERTEX_POSITION_SIZE = 3
        const val TEXTURE_POSITION_SIZE = 2
    }

    val vertices = FloatArray(2 * lines * VERTEX_POSITION_SIZE)
    val textures = FloatArray(2 * lines * TEXTURE_POSITION_SIZE)
    val indices = ByteArray(2 * lines)

    init {
        var index = 0
        var lineCount = 0
        var indicesIndex = 0
        var stCount = 0
        while (lineCount < lines) {
            vertices[index++] = 1f
            vertices[index++] = 1f - lineCount * 2f / (lines - 1)
            vertices[index++] = 0f
            textures[stCount++] = 1f
            textures[stCount++] = 1f - lineCount / (lines - 1).toFloat()
            indices[indicesIndex] = indicesIndex.toByte()
            indicesIndex++


            vertices[index++] = -1f
            vertices[index++] = 1f - lineCount * 2f / (lines - 1)
            vertices[index++] = 0f
            textures[stCount++] = 0f
            textures[stCount++] = 1f - lineCount / (lines - 1).toFloat()
            indices[indicesIndex] = indicesIndex.toByte()
            indicesIndex++

            lineCount++
        }

//        for (i in 0 until 2 * lines) {
//            LogUtil.d(
//                "SurfaceRect",
//                "index = $i : [${vertices[i * VERTEX_POSITION_SIZE + 0]} , ${vertices[i * VERTEX_POSITION_SIZE + 1]} , ${vertices[i * VERTEX_POSITION_SIZE + 2]}]" +
//                        "[${textures[i * TEXTURE_POSITION_SIZE + 0]}, ${textures[i * TEXTURE_POSITION_SIZE + 1]}]"
//            )
//        }
    }
}