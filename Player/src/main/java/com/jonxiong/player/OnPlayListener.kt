package com.jonxiong.player

interface OnPlayListener {
    fun onStart()
    fun onPaused()
    fun onResume()
    fun onStop()
    fun onPlaying(pts: Long)
}