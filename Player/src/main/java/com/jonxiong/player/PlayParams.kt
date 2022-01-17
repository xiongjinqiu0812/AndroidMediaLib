package com.jonxiong.player

class PlayParams {

    companion object {
        val VIDEO_FLAG = 0x01 shl 1
        val AUDIO_FLAG = 0x01 shl 2
    }

    var avFlag = VIDEO_FLAG or AUDIO_FLAG
    val syncInfo = CodecSyncInfo.create()

    var url: String = ""
    var loop: Boolean = false

    fun reset() {
        url = ""
        loop = false
        syncInfo.reset()
    }
}