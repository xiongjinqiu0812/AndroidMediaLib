package com.jonxiong.player.decode

import com.jonxiong.player.PlayState

interface IDecoder : Runnable {

    fun releaseDecoder()

    fun changeState(state: PlayState)

    fun seekTo(pts: Long)
}