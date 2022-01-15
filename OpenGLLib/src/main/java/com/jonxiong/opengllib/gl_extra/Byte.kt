package com.jonxiong.opengllib.gl_extra

import java.nio.*

fun FloatArray.toFloatBuffer(): FloatBuffer {
    val array = this
    return ByteBuffer.allocateDirect(array.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(array).position(0)
        }
}

fun IntArray.toIntBuffer(): IntBuffer {
    val array = this
    return ByteBuffer.allocateDirect(array.size * Int.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asIntBuffer()
        .apply {
            put(array).position(0)
        }
}

fun ShortArray.toShortBuffer(): ShortBuffer {
    val array = this
    return ByteBuffer.allocateDirect(array.size * Short.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .apply {
            put(array).position(0)
        }
}

fun ByteArray.toByteBuffer(): ByteBuffer {
    val array = this
    return ByteBuffer.allocateDirect(array.size * Byte.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .apply {
            put(array).position(0)
        }
}

fun DoubleArray.toDoubleBuffer(): DoubleBuffer {
    val array = this
    return ByteBuffer.allocateDirect(array.size * Double.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asDoubleBuffer()
        .apply {
            put(array).position(0)
        }
}

fun CharArray.toCharBuffer(): CharBuffer {
    val array = this
    return ByteBuffer.allocateDirect(array.size * Char.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asCharBuffer()
        .apply {
            put(array).position(0)
        }
}

fun LongArray.toLongBuffer(): LongBuffer {
    val array = this
    return ByteBuffer.allocateDirect(array.size * Long.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asLongBuffer()
        .apply {
            put(array).position(0)
        }
}

fun getBufferByteSize(buffer: Buffer): Int {
    return when (buffer) {
        is ByteBuffer -> Byte.SIZE_BYTES
        is IntBuffer -> Int.SIZE_BYTES
        is ShortBuffer -> Short.SIZE_BYTES
        is FloatBuffer -> Float.SIZE_BYTES
        is DoubleBuffer -> Double.SIZE_BYTES
        is CharBuffer -> Char.SIZE_BYTES
        is LongBuffer -> Long.SIZE_BYTES
        else -> Float.SIZE_BITS
    }
}