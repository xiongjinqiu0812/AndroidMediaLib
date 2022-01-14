package com.jonxiong.player

import android.media.MediaExtractor

class PlayParams {
    var playStats = PlayState.UN_KNOW
    var url: String = ""
    var loop: Boolean = false
    var seekPts: Long = -1L
    var seekMode: Int = MediaExtractor.SEEK_TO_CLOSEST_SYNC

    fun reset() {
        playStats = PlayState.UN_KNOW
        url = ""
        loop = false
        seekPts = -1L
        seekMode = MediaExtractor.SEEK_TO_CLOSEST_SYNC
    }
}