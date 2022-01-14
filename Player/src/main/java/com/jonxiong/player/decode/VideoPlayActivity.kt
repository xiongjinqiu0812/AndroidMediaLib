package com.jonxiong.player.decode

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.jonxiong.player.BasePlayer
import com.jonxiong.player.HWPlayer
import com.jonxiong.player.R
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*


class VideoPlayActivity : AppCompatActivity(), SurfaceHolder.Callback2 {

    companion object {
        const val TAG: String = "VideoPlayActivity"
        const val videoName: String = "baby.mp4"
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var stop: Button


    private var handler: Handler? = null

    private var player: BasePlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        surfaceView = findViewById(R.id.play_view)
        surfaceView.holder.addCallback(this)

        stop = findViewById(R.id.stop)
        stop.setOnClickListener { player?.stop() }

        player = HWPlayer(this)
        player?.playParams?.apply {
            this.loop = true
        }

        handler = Handler(Looper.getMainLooper())

        handler?.postDelayed({
             player?.play(videoName)
        }, 1000)


    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        if (player is HWPlayer) {
            (player as HWPlayer).setSurface(holder.surface)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {

    }
}