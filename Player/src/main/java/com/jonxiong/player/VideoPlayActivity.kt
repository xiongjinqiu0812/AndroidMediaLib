package com.jonxiong.player

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.jonxiong.player.render.OnGLSurfaceReadyListener
import com.jonxiong.player.render.OnRenderListener
import com.jonxiong.player.render.VideoGLRender


class VideoPlayActivity : AppCompatActivity(), SurfaceHolder.Callback2 {

    companion object {
        const val TAG: String = "JON_VideoPlayActivity"
        const val videoName: String = "midway.mp4"
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var glSurfaceView: GLSurfaceView
    private var render: VideoGLRender? = null
    private lateinit var play: Button
    private lateinit var pause: Button
    private lateinit var stop: Button


    private var player: BasePlayer? = null

    private var useGL = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        surfaceView = findViewById(R.id.play_view)
        surfaceView.holder.addCallback(this)

        glSurfaceView = findViewById(R.id.gl_play_view)

        if (useGL) {
            surfaceView.visibility = View.GONE
            glSurfaceView.visibility = View.VISIBLE

            render = VideoGLRender(this).apply {
                listener = object : OnGLSurfaceReadyListener {
                    override fun onGLSurfaceReady(surface: Surface?) {
                        Log.d(TAG, "onGLSurfaceReady")
                        if (surface == null) return

                        player = BasePlayer(this@VideoPlayActivity, surface).apply {
                            params.loop = true
//                            params.avFlag = PlayParams.VIDEO_FLAG
                            videoRenderListener = object : OnRenderListener {
                                override fun onFrameRender(presentationTimeUs: Long) {
                                    glSurfaceView.requestRender()
                                }
                            }
                        }
                    }
                }
            }
            glSurfaceView.setEGLContextClientVersion(3)
            glSurfaceView.setRenderer(render)
            glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        } else {
            surfaceView.visibility = View.VISIBLE
            glSurfaceView.visibility = View.GONE
        }

        play = findViewById(R.id.play)
        play.setOnClickListener { player?.play(videoName) }

        pause = findViewById(R.id.pause)
        pause.setOnClickListener { player?.pause() }

        stop = findViewById(R.id.stop)
        stop.setOnClickListener { player?.stop() }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        player?.releasePlayer()
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        if (useGL) return
        player = BasePlayer(this, holder.surface).apply {
            params.loop = true
//            params.avFlag = PlayParams.VIDEO_FLAG
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {

    }
}