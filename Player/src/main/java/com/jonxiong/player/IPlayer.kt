package com.jonxiong.player

interface IPlayer: OnPlayListener {
    fun play(url: String)
    fun pause()
    fun stop()
    fun releasePlayer()
}