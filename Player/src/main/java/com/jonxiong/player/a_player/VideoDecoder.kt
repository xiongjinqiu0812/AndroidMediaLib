package com.jonxiong.player.a_player

import android.content.Context
import android.media.MediaCodec
import android.util.Log
import android.view.Surface
import com.jonxiong.player.PlayParams

class VideoDecoder(avFlag: Int, context: Context, playParams: PlayParams, var surface: Surface) :
    BaseDecoder(avFlag, context, playParams) {

    companion object {
        private const val TAG = "JON_VideoDecoder"
    }

    override fun getSyncTime(): Long {
        return playParams.frameInterval
    }

    override fun configMediaCodec() {
        mediaCodec?.configure(mediaFormat, surface, null, 0)
        mediaCodec?.start()
    }

    override fun handleOutputData(info: MediaCodec.BufferInfo): Boolean {
        val mediaCodec = this.mediaCodec ?: return false
        var outputId: Int = mediaCodec.dequeueOutputBuffer(info, 1000)
        while (outputId >= 0) {
//            updateVideoStartUs(info.presentationTimeUs)
//            videoTimeSync(info.presentationTimeUs)
            mediaCodec.releaseOutputBuffer(outputId, true)
//            if (mOnRenderListener != null) {
//                mOnRenderListener.onFrameRender(info.presentationTimeUs)
//            }
            playParams.syncInfo.lastVideoPts.set(info.presentationTimeUs)
            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                Log.i(TAG, "BUFFER_FLAG_END_OF_STREAM, finished")
                return true
            }
            outputId = mediaCodec.dequeueOutputBuffer(info, 0)
        }
        return false
    }


}