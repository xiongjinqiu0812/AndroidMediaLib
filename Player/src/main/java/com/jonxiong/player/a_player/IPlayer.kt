package com.jonxiong.player.a_player

import com.jonxiong.player.OnPlayListener

interface IPlayer: OnPlayListener {
    fun play(url: String)
    fun pause()
    fun stop()
    fun releasePlayer()
}