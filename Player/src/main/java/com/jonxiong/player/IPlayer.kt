package com.jonxiong.player

interface IPlayer: OnPlayListener {
    fun play(url: String)
    fun pause()
    fun seekTo(pts: Long)
    fun stop()
    fun releasePlayer()
}