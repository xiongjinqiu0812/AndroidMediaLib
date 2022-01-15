package com.jonxiong.opengllib.model

import kotlin.math.cos
import kotlin.math.sin

//圆柱的侧面
class BezierCylinder(val textureNum: Int = 4) {
    companion object {
        const val VERTEX_POSITION_SIZE = 3
        const val TEXTURE_POSITION_SIZE = 2
    }

    private var dividedPart = 120
    private var scale = 1f
    private val radius = 1 * scale
    private val verticesCount = 2 * (dividedPart + textureNum)//索引法顶点数量，+textureNum是为了闭环

    val vertices = FloatArray(verticesCount * VERTEX_POSITION_SIZE)
    val textures = FloatArray(verticesCount * TEXTURE_POSITION_SIZE)
    val indices = ByteArray(verticesCount)

    init {
        val angleSpan = 360f / dividedPart
        val textureCycle = dividedPart / textureNum.toFloat()

        var count = 0
        var stCount = 0
        var indicesIndex = 0

        for (i in 0 until textureNum) {
            val offsetAngle = 360f / textureNum
            var loopCount = 0
            var startAngle = 0f
            while (startAngle <= offsetAngle) {
                val radian = Math.toRadians((startAngle + i * offsetAngle).toDouble())
                val sinValue = sin(radian).toFloat()
                val cosValue = cos(radian).toFloat()
                //按照逆时针顺序从右边开始
                //第一个点
                vertices[count++] = radius * 1 //x
                vertices[count++] = radius * sinValue//y
                vertices[count++] = radius * 1 //z
                textures[stCount++] = 1f
                if (startAngle + angleSpan >= offsetAngle) {
                    textures[stCount++] = 1f //闭环
                } else {
                    textures[stCount++] = (loopCount % textureCycle.toInt()) / textureCycle
                }
                indices[indicesIndex] = indicesIndex.toByte()
                indicesIndex++
                //第二个点
                vertices[count++] = radius * -1 //x
                vertices[count++] = radius * sinValue//y
                vertices[count++] = radius * 1 //z
                textures[stCount++] = 0f
                if (startAngle + angleSpan >= offsetAngle) {
                    textures[stCount++] = 1f //闭环
                } else {
                    textures[stCount++] = (loopCount % textureCycle.toInt()) / textureCycle
                }
                indices[indicesIndex] = indicesIndex.toByte()
                indicesIndex++
                startAngle += angleSpan
                loopCount++
            }
        }

//        for (i in 0 until verticesCount) {
//            LogUtil.d(
//                "Cylinder",
//                "index = $i : [${vertices[i * VERTEX_POSITION_SIZE + 0]} , ${vertices[i * VERTEX_POSITION_SIZE + 1]} , ${vertices[i * VERTEX_POSITION_SIZE + 2]}]" +
//                        "[${textures[i * TEXTURE_POSITION_SIZE + 0]}, ${textures[i * TEXTURE_POSITION_SIZE + 1]}]"
//            )
//        }
    }
}