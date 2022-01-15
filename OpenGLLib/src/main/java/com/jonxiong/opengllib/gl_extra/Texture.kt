package com.jonxiong.opengllib.gl_extra

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.ETC1Util
import android.opengl.GLUtils
import android.text.TextUtils
import com.jonxiong.opengllib.utils.GLLog
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG = "TextureExtra"
private const val DEFAULT_ID = -1

data class TextureBean(val width: Int, val height: Int, val textureId: Int)

fun loadTexture(context: Context, resourceId: Int): TextureBean {
    val options = BitmapFactory.Options()
    options.inScaled = false
    val sourceBitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
    val bitmap =
        Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height)
    val textureBean = loadTexture(bitmap)
    sourceBitmap.recycle()
    return textureBean
}

fun loadTexture(context: Context, resUri: Uri): TextureBean {
    var bitmap: Bitmap? = null
    val scheme = resUri.scheme
    var path = "${resUri.authority}"
    if (!TextUtils.isEmpty(resUri.path)) {
        path += resUri.path
    }
    try {
        if ("file".equals(scheme, ignoreCase = true)) {
            bitmap = BitmapFactory.decodeFile(path)
        } else if ("assets".equals(scheme, ignoreCase = true)) {
            bitmap = BitmapFactory.decodeStream(context.assets.open(path))
        }
    } catch (ex: java.lang.Exception) {
        GLLog.e(TAG, "texture exception : ", ex)
    }
    return loadTexture(bitmap)
}

fun loadTexture(bitmap: Bitmap?): TextureBean {
    if (bitmap == null) {
        GLLog.e(TAG, "bitmap is null, could not be decoded.")
        return TextureBean(0, 0, DEFAULT_ID)
    }

    val textureIdArray = IntArray(1)
    mGL.glGenTextures(1, textureIdArray, 0)
    if (textureIdArray[0] == 0) {
        GLLog.e(TAG, "Could not generate a new OpenGL texture object.")
        return TextureBean(0, 0, DEFAULT_ID)
    }

    // 2. 将纹理绑定到OpenGL对象上
    mGL.glBindTexture(mGL.GL_TEXTURE_2D, textureIdArray[0])

    // 3. 设置纹理过滤参数:解决纹理缩放过程中的锯齿问题。若不设置，则会导致纹理为黑色
    mGL.glTexParameteri(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_MIN_FILTER, mGL.GL_LINEAR_MIPMAP_LINEAR)
    mGL.glTexParameteri(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_MAG_FILTER, mGL.GL_LINEAR)
    mGL.glTexParameteri(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_WRAP_S, mGL.GL_CLAMP_TO_EDGE)
    mGL.glTexParameteri(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_WRAP_T, mGL.GL_CLAMP_TO_EDGE)

    // 4. 通过OpenGL对象读取Bitmap数据，并且绑定到纹理对象上，之后就可以回收Bitmap对象
    GLUtils.texImage2D(mGL.GL_TEXTURE_2D, 0, bitmap, 0)

    // 5. 生成Mip位图
    mGL.glGenerateMipmap(mGL.GL_TEXTURE_2D)

    // 6. 回收Bitmap对象
    val width = bitmap.width
    val height = bitmap.height
    bitmap.recycle()

    // 7. 将纹理从OpenGL对象上解绑
    mGL.glBindTexture(mGL.GL_TEXTURE_2D, 0)
    return TextureBean(width, height, textureIdArray[0])
}

fun surfaceTextureOES(): Int {
    val textureId: Int
    val texture = IntArray(1)
    mGL.glGenTextures(texture.size, texture, 0)
    mGL.glBindTexture(mGLExt.GL_TEXTURE_EXTERNAL_OES, texture[0])
    mGL.glTexParameterf(mGLExt.GL_TEXTURE_EXTERNAL_OES, mGL.GL_TEXTURE_MIN_FILTER, mGL.GL_LINEAR.toFloat())
    mGL.glTexParameterf(mGLExt.GL_TEXTURE_EXTERNAL_OES, mGL.GL_TEXTURE_MAG_FILTER, mGL.GL_LINEAR.toFloat())
    mGL.glTexParameteri(mGLExt.GL_TEXTURE_EXTERNAL_OES, mGL.GL_TEXTURE_WRAP_S, mGL.GL_CLAMP_TO_EDGE)
    mGL.glTexParameteri(mGLExt.GL_TEXTURE_EXTERNAL_OES, mGL.GL_TEXTURE_WRAP_T, mGL.GL_CLAMP_TO_EDGE)
    textureId = texture[0]
    return textureId
}

fun texture(width: Int, height: Int): Int {
    val textureId: Int
    val texture = IntArray(1)
    //生成纹理
    mGL.glGenTextures(texture.size, texture, 0)
    mGL.glBindTexture(mGL.GL_TEXTURE_2D, texture[0])
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_MIN_FILTER, mGL.GL_LINEAR.toFloat())
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_MAG_FILTER, mGL.GL_LINEAR.toFloat())
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_WRAP_S, mGL.GL_CLAMP_TO_EDGE.toFloat())
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_WRAP_T, mGL.GL_CLAMP_TO_EDGE.toFloat())
    mGL.glTexImage2D(
        mGL.GL_TEXTURE_2D, 0, mGL.GL_RGBA, width, height, 0, mGL.GL_RGBA,
        mGL.GL_UNSIGNED_BYTE, null
    )
    textureId = texture[0]
    return textureId
}

fun compressedTexture(context: Context, resUri: Uri, type: String?, internalFormat: Int): Int {
    val scheme = resUri.scheme
    var path = "${resUri.authority}"
    if (!TextUtils.isEmpty(resUri.path)) {
        path += resUri.path
    }
    var inputStream: InputStream? = null
    try {
        if ("file".equals(scheme, ignoreCase = true)) {
            inputStream = FileInputStream(File(path))
        } else if ("assets".equals(scheme, ignoreCase = true)) {
            inputStream = context.assets.open(path)
        }
    } catch (ex: Exception) {
        GLLog.e(TAG, "input stream exception : ", ex)
    }
    return compressedTexture(inputStream, type, internalFormat)
}

fun compressedTexture(inputStream: InputStream?, type: String?, internalFormat: Int): Int {
    if (inputStream == null) return 0
    val textures = IntArray(1)
    mGL.glGenTextures(1, textures, 0)
    val textureId = textures[0]
    mGL.glBindTexture(mGL.GL_TEXTURE_2D, textureId)
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_MIN_FILTER, mGL.GL_LINEAR.toFloat())
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_MAG_FILTER, mGL.GL_LINEAR.toFloat())
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_WRAP_S, mGL.GL_CLAMP_TO_EDGE.toFloat())
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_WRAP_T, mGL.GL_CLAMP_TO_EDGE.toFloat())
    if ("etc1".equals(type, ignoreCase = true)) {
        try {
            ETC1Util.loadTexture(mGL.GL_TEXTURE_2D, 0, 0, mGL.GL_RGB, internalFormat, inputStream)
        } catch (e: java.lang.Exception) {
            GLLog.e(TAG, "load etc1 exception : ", e)
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                // ignore exception thrown from close.
            }
        }
    } else if ("etc2".equals(type, ignoreCase = true) || "astc".equals(type, ignoreCase = true)) {
        var data: ByteArray? = null
        try {
            data = ByteArray(inputStream.available())
            inputStream.read(data)
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (data == null) return -1
        val buffer = ByteBuffer.allocateDirect(data.size).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(data).position(16)
        val header = ByteBuffer.allocateDirect(16).order(ByteOrder.BIG_ENDIAN)
        header.put(data, 0, 16).position(0)
        if ("etc2".equals(type, ignoreCase = true)) {
            val width = header.getShort(12).toInt()
            val height = header.getShort(14).toInt()
            mGL.glCompressedTexImage2D(
                mGL.GL_TEXTURE_2D, 0, internalFormat, width, height,
                0, data.size - 16, buffer
            )
        } else if ("astc".equals(type, ignoreCase = true)) {
            compressASTC(header, internalFormat, buffer)
        }
    }
    return textureId
}

fun compressedASTCTexture(buffer: ByteBuffer, header: ByteBuffer, type: String?, internalFormat: Int): Int {
    val textures = IntArray(1)
    mGL.glGenTextures(1, textures, 0)
    val textureId = textures[0]
    mGL.glBindTexture(mGL.GL_TEXTURE_2D, textureId)
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_MIN_FILTER, mGL.GL_LINEAR.toFloat())
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_MAG_FILTER, mGL.GL_LINEAR.toFloat())
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_WRAP_S, mGL.GL_CLAMP_TO_EDGE.toFloat())
    mGL.glTexParameterf(mGL.GL_TEXTURE_2D, mGL.GL_TEXTURE_WRAP_T, mGL.GL_CLAMP_TO_EDGE.toFloat())
    if ("etc2".equals(type, ignoreCase = true) || "astc".equals(type, ignoreCase = true)) {
        compressASTC(header, internalFormat, buffer)
    }
    return textureId
}

private fun compressASTC(header: ByteBuffer, internalFormat: Int, buffer: ByteBuffer) {
    val xSize: Int =
        (header[7].toInt() and 0xFF) + (header[8].toInt() and 0xFF shl 8) + (header[9].toInt() and 0xFF shl 16)
    val ySize: Int =
        (header[10].toInt() and 0xFF) + (header[11].toInt() and 0xFF shl 8) + (header[12].toInt() and 0xFF shl 16)
    val zSize: Int =
        (header[13].toInt() and 0xFF) + (header[14].toInt() and 0xFF shl 8) + (header[15].toInt() and 0xFF shl 16)
    val blockDimX: Int = header[4].toInt() and 0xFF
    val blockDimY: Int = header[5].toInt() and 0xFF
    val blockDimZ: Int = header[6].toInt() and 0xFF
    val xBlocks = (xSize + blockDimX - 1) / blockDimX
    val yBlocks = (ySize + blockDimY - 1) / blockDimY
    val zBlocks = (zSize + blockDimZ - 1) / blockDimZ
    val imageSize = xBlocks * yBlocks * zBlocks shl 4
    mGL.glCompressedTexImage2D(
        mGL.GL_TEXTURE_2D, 0, internalFormat, xSize, ySize,
        0, imageSize, buffer
    )
}