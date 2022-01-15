package com.jonxiong.player.render

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.Matrix
import android.view.Surface
import com.jonxiong.opengllib.BaseRender
import com.jonxiong.opengllib.gl_extra.*
import com.jonxiong.opengllib.model.SurfaceRect
import com.jonxiong.player.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VideoGLRender(context: Context?) : BaseRender(context!!) {
    private val mRectangle = SurfaceRect()
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mSurface: Surface? = null
    private var mTextureId = 0
    private var mOffsetLoc = 0
    private var mTextureUnitLoc = 0
    private val mVao = IntArray(1)
    private val mVbo = IntArray(3)

    var listener: OnGLSurfaceReadyListener? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        mTextureId = surfaceTextureOES()
        if (mSurfaceTexture == null) {
            mSurfaceTexture = SurfaceTexture(mTextureId)
            mSurface = Surface(mSurfaceTexture)
            listener?.onGLSurfaceReady(mSurface)
        }
        mProgram = createProgramByRes(context, R.raw.video_vertex_shader, R.raw.video_frag_shader)
        mMvpMatrixHandle = GLES30.glGetUniformLocation(mProgram, "u_MVPMatrix")
        mOffsetLoc = GLES30.glGetUniformLocation(mProgram, "uOffset")
        mTextureUnitLoc = GLES30.glGetUniformLocation(mProgram, "ourTexture")

        GLES30.glGenVertexArrays(1, mVao, 0)
        GLES30.glGenBuffers(3, mVbo, 0)
        GLES30.glBindVertexArray(mVao[0])
        bindVboBufferArray(
            mVbo[0],
            mRectangle.vertices.toFloatBuffer(),
            0,
            SurfaceRect.VERTEX_POSITION_SIZE
        )
        bindVboBufferArray(
            mVbo[1],
            mRectangle.textures.toFloatBuffer(),
            1,
            SurfaceRect.TEXTURE_POSITION_SIZE
        )
        bindVboBufferElement(mVbo[2], mRectangle.indices.toByteBuffer())
        bindTextureId(0, mTextureId)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
        GLES30.glBindVertexArray(0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio = 1f
        Matrix.frustumM(mMatrixState.mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 12f)
        Matrix.setLookAtM(mMatrixState.mCameraMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)

        mSurfaceTexture?.updateTexImage()

        GLES30.glUseProgram(mProgram)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glBindVertexArray(mVao[0])

        mMatrixState.push()
        GLES30.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, mMatrixState.getFinalMatrix(), 0)
        GLES30.glUniform1f(mOffsetLoc, 15f)
        GLES30.glUniform1i(mTextureUnitLoc, 0)
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLE_STRIP,
            mRectangle.indices.size,
            GLES30.GL_UNSIGNED_BYTE,
            0
        )
        mMatrixState.pop()

        GLES30.glBindVertexArray(0)
    }
}