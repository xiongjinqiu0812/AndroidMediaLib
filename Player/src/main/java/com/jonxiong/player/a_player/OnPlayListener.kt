package com.jonxiong.player.a_player

interface OnPlayListener {
    fun onStart()
    fun onPaused()
    fun onResume()
    fun onStop()
    fun onPlaying(pts: Long)
}