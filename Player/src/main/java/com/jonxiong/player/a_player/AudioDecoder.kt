package com.jonxiong.player.a_player

import android.content.Context
import android.media.MediaCodec
import com.jonxiong.player.PlayParams

class AudioDecoder(avFlag: Int, context: Context, playParams: PlayParams) : BaseDecoder(avFlag, context, playParams) {
    override fun getSyncTime(): Long {
        return 0L
    }

    override fun configMediaCodec() {

    }

    override fun handleOutputData(info: MediaCodec.BufferInfo): Boolean {
        return false
    }

    override fun releaseDecoder() {
        
    }


}