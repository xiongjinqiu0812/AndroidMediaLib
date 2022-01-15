package com.jonxiong.opengllib.gl_extra

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES30
import com.jonxiong.opengllib.utils.FileUtil
import com.jonxiong.opengllib.utils.GLLog
import java.io.IOException
import java.nio.Buffer

typealias mGL = GLES30
typealias mGLExt = GLES11Ext

private const val TAG = "OpenGLExtra"

/**
 * 从res/raw目录下读取着色器代码
 */
fun createProgramByRes(context: Context?, vertexRawId: Int, fragmentRawId: Int): Int {
    val vertexShader = loadShader(context, mGL.GL_VERTEX_SHADER, vertexRawId)
    if (vertexShader == 0) return 0
    val fragmentShader = loadShader(context, mGL.GL_FRAGMENT_SHADER, fragmentRawId)
    return if (fragmentShader == 0) 0 else buildProgram(vertexShader, fragmentShader)
}

/**
 * 从assets目录下读取着色器代码
 */
fun createProgramByFile(context: Context?, vertexFileName: String?, fragmentFileName: String?): Int {
    val vertexShader = loadShader(context, mGL.GL_VERTEX_SHADER, vertexFileName)
    if (vertexShader == 0) return 0
    val fragmentShader = loadShader(context, mGL.GL_FRAGMENT_SHADER, fragmentFileName)
    return if (fragmentShader == 0) 0 else buildProgram(vertexShader, fragmentShader)
}

fun createProgramByString(vertexCode: String?, fragmentCode: String?): Int {
    val vertexShader = loadShader(mGL.GL_VERTEX_SHADER, vertexCode)
    if (vertexShader == 0) return 0
    val fragmentShader = loadShader(mGL.GL_FRAGMENT_SHADER, fragmentCode)
    return if (fragmentShader == 0) 0 else buildProgram(vertexShader, fragmentShader)
}

private fun loadShader(type: Int, shaderCode: String?): Int {
    val shader = mGL.glCreateShader(type)
    mGL.glShaderSource(shader, shaderCode)
    mGL.glCompileShader(shader)
    val compiled = IntArray(1)
    mGL.glGetShaderiv(shader, mGL.GL_COMPILE_STATUS, compiled, 0)
    if (compiled[0] == 0) {
        GLLog.e(TAG, mGL.glGetShaderInfoLog(shader))
        mGL.glDeleteShader(shader)
        return 0
    }
    return shader
}

private fun loadShader(context: Context?, type: Int, rawId: Int): Int {
    if (context == null) {
        return 0
    }
    val shaderCode =
        FileUtil.readStringFromInputStream(context.resources.openRawResource(rawId))
    return loadShader(type, shaderCode)
}

private fun loadShader(context: Context?, type: Int, assetName: String?): Int {
    if (context == null || assetName == null) {
        return 0
    }
    try {
        val shaderCode = FileUtil.readStringFromInputStream(context.assets.open(assetName))
        return loadShader(type, shaderCode)
    } catch (e: IOException) {
        e.printStackTrace()
        GLLog.e(TAG, "load assets shader fail : ", e)
    }
    return 0
}

private fun buildProgram(vertexShader: Int, fragmentShader: Int): Int {
    var program = mGL.glCreateProgram()
    if (program != 0) {
        mGL.glAttachShader(program, vertexShader)
        checkGlError("attach vertex shader")
        mGL.glAttachShader(program, fragmentShader)
        checkGlError("attach fragment shader")
        mGL.glLinkProgram(program)
        val linkStatus = IntArray(1)
        mGL.glGetProgramiv(program, mGL.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != mGL.GL_TRUE) {
            GLLog.e(TAG, "createProgram glError : " + mGL.glGetProgramInfoLog(program))
            mGL.glDeleteProgram(program)
            program = 0
        }
    }
    return program
}


private fun checkGlError(op: String) {
    var error: Int
    if (mGL.glGetError().also { error = it } != mGL.GL_NO_ERROR) {
        GLLog.e(TAG, "$op checkGlError : $error")
        throw RuntimeException("$op : glError $error")
    }
}

fun bindVboBufferArray(vbo: Int, buffer: Buffer, handle: Int, positionSize: Int) {
    val byteSize = getBufferByteSize(buffer)
    mGL.glBindBuffer(mGL.GL_ARRAY_BUFFER, vbo)
    mGL.glBufferData(mGL.GL_ARRAY_BUFFER, byteSize * buffer.capacity(), buffer, mGL.GL_STATIC_DRAW)
    mGL.glEnableVertexAttribArray(handle)
    mGL.glVertexAttribPointer(handle, positionSize, mGL.GL_FLOAT, false, 0, 0)
}

fun bindVboBufferElement(vbo: Int, buffer: Buffer) {
    val byteSize = getBufferByteSize(buffer)
    mGL.glBindBuffer(mGL.GL_ELEMENT_ARRAY_BUFFER, vbo)
    mGL.glBufferData(
        mGL.GL_ELEMENT_ARRAY_BUFFER,
        byteSize * buffer.capacity(),
        buffer,
        mGL.GL_STATIC_DRAW
    )
}

fun bindTextureId(index: Int, textureId: Int) {
    mGL.glActiveTexture(mGL.GL_TEXTURE0 + index)
    mGL.glBindTexture(mGL.GL_TEXTURE_2D, textureId)
}

fun bindTextureIds(startIndex: Int, textureIds: ArrayList<TextureBean>) {
    textureIds.forEachIndexed { index, textureBean ->
        mGL.glActiveTexture(mGL.GL_TEXTURE0 + index + startIndex)
        mGL.glBindTexture(mGL.GL_TEXTURE_2D, textureBean.textureId)
    }
}




