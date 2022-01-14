package com.jonxiong.player.a_player

import com.jonxiong.player.PlayState

interface IDecoder : Runnable {

    fun releaseDecoder()

    fun changeState(state: PlayState)
}