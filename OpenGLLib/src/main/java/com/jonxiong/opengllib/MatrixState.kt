package com.jonxiong.opengllib

import android.opengl.Matrix

class MatrixState {
    var mStack = Array(10) { FloatArray(16) }
    var mStackTop = -1

    //模型矩阵
    var mMvpMatrix = FloatArray(16)
    var mProjectionMatrix = FloatArray(16)
    var mCameraMatrix = FloatArray(16)
    var mCurrMatrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    fun push() {
        mStackTop++
        for (i in 0..15) {
            mStack[mStackTop][i] = mCurrMatrix[i]
        }
    }

    fun pop() {
        for (i in 0..15) {
            mCurrMatrix[i] = mStack[mStackTop][i]
        }
        mStackTop--
    }

    fun translate(x: Float, y: Float, z: Float) {
        Matrix.translateM(mCurrMatrix, 0, x, y, z)
    }

    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        Matrix.rotateM(mCurrMatrix, 0, angle, x, y, z)
    }

    fun scale(x: Float, y: Float, z: Float) {
        Matrix.scaleM(mCurrMatrix, 0, x, y, z)
    }

    fun getFinalMatrix(): FloatArray {
        Matrix.multiplyMM(mMvpMatrix, 0, mCameraMatrix, 0, mCurrMatrix, 0)
        Matrix.multiplyMM(mMvpMatrix, 0, mProjectionMatrix, 0, mMvpMatrix, 0)
        return mMvpMatrix
    }
}