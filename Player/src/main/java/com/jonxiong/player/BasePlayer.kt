package com.jonxiong.player

import android.media.MediaExtractor
import androidx.annotation.CallSuper
import androidx.annotation.MainThread

abstract class BasePlayer {
    open var listener: OnPlayListener? = null
    open val playParams by lazy { PlayParams() }


    @Override
    @CallSuper
    open fun play(url: String? = null) {
        if (url == null) {
            return
        }
        playParams.url = url
    }

    abstract fun pause()

    @CallSuper
    open fun stop() {
        playParams.reset()
    }

    @CallSuper
    open fun release() {
        listener = null
    }

    open fun setOnPlayListener(listener: OnPlayListener) {
        this.listener = listener
    }

    abstract fun seekTo(ptsUs: Long, mode: Int = MediaExtractor.SEEK_TO_CLOSEST_SYNC)
}