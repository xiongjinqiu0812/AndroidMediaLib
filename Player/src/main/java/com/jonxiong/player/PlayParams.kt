package com.jonxiong.player

import android.media.MediaExtractor
import com.jonxiong.player.a_player.CodecSyncInfo

class PlayParams {

    companion object {
        val VIDEO_FLAG = 0x01 shl 1
        val AUDIO_FLAG = 0x01 shl 2
    }

    var avFlag = VIDEO_FLAG or AUDIO_FLAG
    val syncInfo = CodecSyncInfo.create()

//    var playState = PlayState.UN_KNOW
    var url: String = ""
    var loop: Boolean = false
    var seekPts: Long = -1L
    var seekMode: Int = MediaExtractor.SEEK_TO_CLOSEST_SYNC
    var frameInterval = 16L

    fun reset() {
//        playState = PlayState.UN_KNOW
        url = ""
        loop = false
        seekPts = -1L
        seekMode = MediaExtractor.SEEK_TO_CLOSEST_SYNC
        frameInterval = 16L
        syncInfo.reset()
    }
}