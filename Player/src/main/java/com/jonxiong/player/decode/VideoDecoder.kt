package com.jonxiong.player.decode

import android.content.Context
import android.media.MediaCodec
import android.view.Surface
import com.jonxiong.player.PlayParams

class VideoDecoder(avFlag: Int, context: Context, playParams: PlayParams, var surface: Surface) :
    BaseDecoder(avFlag, context, playParams) {

    companion object {
        private const val TAG = "JON_VideoDecoder"
    }

    override fun configMediaCodec() {
        mediaCodec?.configure(mediaFormat, surface, null, 0)
        mediaCodec?.start()
    }

    override fun doRender(mediaCodec: MediaCodec, outputId: Int, info: MediaCodec.BufferInfo) {
        mediaCodec.releaseOutputBuffer(outputId, true)
        onRenderListener?.onFrameRender(info.presentationTimeUs)
    }


}