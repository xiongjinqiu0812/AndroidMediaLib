package com.jonxiong.player.a_player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import com.jonxiong.player.PlayParams
import com.jonxiong.player.PlayState
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BasePlayer(var context: Context, var surface: Surface) : IPlayer {

    companion object {
        private const val TAG = "JON_BasePlayer"
    }

    var params: PlayParams = PlayParams()

    var playState = PlayState.UN_KNOW
    var audioDecoder: IDecoder? = null
    var videoDecoder: IDecoder? = null

    private var executorService: ExecutorService = Executors.newFixedThreadPool(2)
    private var uiHandler: Handler? = null

    private fun initPlayer() {
        when (params.avFlag) {
            PlayParams.VIDEO_FLAG -> {
                videoDecoder = VideoDecoder(PlayParams.VIDEO_FLAG, context, params, surface)
                playState = PlayState.PREPARE
            }
            PlayParams.AUDIO_FLAG -> {
                audioDecoder = AudioDecoder(PlayParams.AUDIO_FLAG, context, params)
                playState = PlayState.PREPARE
            }
            PlayParams.VIDEO_FLAG or PlayParams.AUDIO_FLAG -> {
                audioDecoder = AudioDecoder(PlayParams.AUDIO_FLAG, context, params)
                videoDecoder = VideoDecoder(PlayParams.VIDEO_FLAG, context, params, surface)
                playState = PlayState.PREPARE
            }
            else -> playState = PlayState.UN_KNOW
        }

        Log.d(TAG, "init player")
    }

    override fun play(url: String) {

        if (Looper.getMainLooper() != Looper.myLooper()) {
            if (uiHandler == null) {
                uiHandler = Handler(Looper.getMainLooper())
            }
            uiHandler?.post { play(url) }
            return
        }

        //正在播放当前视频
        if (params.url == url && playState == PlayState.PLAYING) {
            return
        }
        //当前视频暂停
        if (params.url == url && playState == PlayState.PAUSED) {
            audioDecoder?.changeState(PlayState.PLAYING)
            videoDecoder?.changeState(PlayState.PLAYING)
            return
        }

        //重置播放器
        stop()
        params.url = url
        initPlayer()

        audioDecoder?.let {
            executorService.execute(it)
        }
        videoDecoder?.let {
            executorService.execute(it)
        }

        Log.d(TAG, "start player")
    }

    override fun pause() {
        playState = PlayState.PAUSED
        audioDecoder?.changeState(PlayState.PAUSED)
        videoDecoder?.changeState(PlayState.PAUSED)
        Log.d(TAG, "pause player")
    }

    override fun stop() {
        playState = PlayState.STOP
        audioDecoder?.changeState(PlayState.STOP)
        videoDecoder?.changeState(PlayState.STOP)
        Log.d(TAG, "stop player")
    }

    override fun releasePlayer() {
        stop()
        executorService.shutdown()
        Log.d(TAG, "release player")
    }

    override fun onStart() {

    }

    override fun onPaused() {

    }

    override fun onResume() {

    }

    override fun onStop() {

    }

    override fun onPlaying(pts: Long) {

    }

}