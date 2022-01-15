package com.jonxiong.player.decode

import com.jonxiong.player.a_player.PlayState

interface IDecoder : Runnable {

    fun releaseDecoder()

    fun changeState(state: PlayState)
}