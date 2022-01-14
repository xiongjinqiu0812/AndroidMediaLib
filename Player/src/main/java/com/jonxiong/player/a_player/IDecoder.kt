package com.jonxiong.player.a_player

import com.jonxiong.player.decode.DecodeState

interface IDecoder : Runnable {

    fun releaseDecoder()

    fun changeState(state: DecodeState)
}