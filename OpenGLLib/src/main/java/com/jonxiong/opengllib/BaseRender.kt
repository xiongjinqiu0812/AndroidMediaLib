package com.jonxiong.opengllib

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.jonxiong.opengllib.gl_extra.mGL
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

open class BaseRender(var context: Context) : GLSurfaceView.Renderer {
    protected var mProgram = 0
    protected var mMvpMatrixHandle = 0
    protected var mMatrixState = MatrixState()
    var mVertexShader = 0
    var mFragShader = 0


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {}

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mGL.glViewport(0, 0, width, height)
        val ratio = 1f
        Matrix.frustumM(mMatrixState.mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 12f)
        Matrix.setLookAtM(mMatrixState.mCameraMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {}

    open fun onCreate() {}

    open fun onResume() {}

    open fun onPause() {}

    open fun onDestroy() {}

    open fun runAnimate(animateAttr: AnimateAttr) {

    }

    fun getTag() = javaClass.simpleName ?: "BaseRender"
}

