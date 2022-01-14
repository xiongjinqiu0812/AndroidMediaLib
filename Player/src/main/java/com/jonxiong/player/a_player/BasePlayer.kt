package com.jonxiong.player.a_player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Surface
import com.jonxiong.player.PlayParams
import com.jonxiong.player.PlayState
import com.jonxiong.player.decode.DecodeState
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BasePlayer(var context: Context, var surface: Surface) : IPlayer {

    companion object {
        const val TAG = "BasePlayer"
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
                params.playState = PlayState.PREPARE
            }
            PlayParams.AUDIO_FLAG -> {
                audioDecoder = AudioDecoder(PlayParams.AUDIO_FLAG, context, params)
                params.playState = PlayState.PREPARE
            }
            PlayParams.VIDEO_FLAG or PlayParams.AUDIO_FLAG -> {
                audioDecoder = AudioDecoder(PlayParams.AUDIO_FLAG, context, params)
                videoDecoder = VideoDecoder(PlayParams.VIDEO_FLAG, context, params, surface)
                params.playState = PlayState.PREPARE
            }
            else -> params.playState = PlayState.UN_KNOW
        }

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
            audioDecoder?.changeState(DecodeState.DECODING)
            videoDecoder?.changeState(DecodeState.DECODING)
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
    }

    override fun pause() {
        audioDecoder?.changeState(DecodeState.PAUSED)
        videoDecoder?.changeState(DecodeState.PAUSED)
    }

    override fun stop() {
        audioDecoder?.releaseDecoder()
        videoDecoder?.releaseDecoder()
    }

    override fun releasePlayer() {
        executorService.shutdown()
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