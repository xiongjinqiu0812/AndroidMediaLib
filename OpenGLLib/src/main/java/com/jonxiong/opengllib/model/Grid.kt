package com.jonxiong.opengllib.model

class Grid(dividerPart: Int = 2) {
    companion object {
        const val GRID_VERTEX_POSITION_SIZE = 2
    }

    val vertices: FloatArray

    init {
        val lineNum = 2 * (dividerPart - 1)
        val startCoord = 2f / dividerPart
        vertices = FloatArray(2 * lineNum * GRID_VERTEX_POSITION_SIZE)
        var index = 0
        //横向
        for (i in 0 until lineNum / 2) {
            vertices[index++] = -1f
            vertices[index++] = 1f - (i + 1) * startCoord
            vertices[index++] = 1f
            vertices[index++] = 1f - (i + 1) * startCoord
        }
        //竖向
        for (i in 0 until lineNum / 2) {
            vertices[index++] = -1f + (i + 1) * startCoord
            vertices[index++] = 1f
            vertices[index++] = -1f + (i + 1) * startCoord
            vertices[index++] = -1f
        }

//        for (i in 0 until lineNum) {
//            LogUtil.d(
//                "Grid",
//                "index = $i : [${vertices[i * GRID_VERTEX_POSITION_SIZE * 2 + 0]} , ${vertices[i * GRID_VERTEX_POSITION_SIZE * 2 + 1]}]" +
//                        "[${vertices[i * GRID_VERTEX_POSITION_SIZE * 2 + 2]} , ${vertices[i * GRID_VERTEX_POSITION_SIZE * 2 + 3]}]"
//            )
//        }
    }
}