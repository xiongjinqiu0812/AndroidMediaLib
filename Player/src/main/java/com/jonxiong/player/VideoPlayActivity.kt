package com.jonxiong.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.jonxiong.player.a_player.BasePlayer
import com.jonxiong.player.a_player.PlayParams


class VideoPlayActivity : AppCompatActivity(), SurfaceHolder.Callback2 {

    companion object {
        const val TAG: String = "JON_VideoPlayActivity"
        const val videoName: String = "baby.mp4"
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var play: Button
    private lateinit var pause: Button
    private lateinit var stop: Button


    private var handler: Handler? = null
    private var player: BasePlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        surfaceView = findViewById(R.id.play_view)
        surfaceView.holder.addCallback(this)

        play = findViewById(R.id.play)
        play.setOnClickListener { player?.play(videoName) }

        pause = findViewById(R.id.pause)
        pause.setOnClickListener { player?.pause() }

        stop = findViewById(R.id.stop)
        stop.setOnClickListener { player?.stop() }


        handler = Handler(Looper.getMainLooper())

        handler?.postDelayed({
             player?.play(videoName)
        }, 1000)


    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        player?.releasePlayer()
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
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