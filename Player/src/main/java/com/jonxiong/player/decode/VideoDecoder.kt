package com.jonxiong.player.decode

import android.content.Context
import android.media.MediaCodec
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import com.jonxiong.player.a_player.PlayParams
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class VideoDecoder(avFlag: Int, context: Context, playParams: PlayParams, var surface: Surface) :
    BaseDecoder(avFlag, context, playParams) {

    companion object {
        private const val TAG = "JON_VideoDecoder"
    }

    override fun configMediaCodec() {
        mediaCodec?.configure(mediaFormat, surface, null, 0)
        mediaCodec?.start()
    }

    override fun handleOutputData(info: MediaCodec.BufferInfo): Boolean {
        val mediaCodec = this.mediaCodec ?: return false
        var outputId: Int = mediaCodec.dequeueOutputBuffer(info, 1000)
        while (outputId >= 0) {
            updateVideoStartUs(info.presentationTimeUs)
            videoTimeSync(info.presentationTimeUs)
            mediaCodec.releaseOutputBuffer(outputId, true)
//            if (mOnRenderListener != null) {
//                mOnRenderListener.onFrameRender(info.presentationTimeUs)
//            }
            playParams.syncInfo.lastVideoPts.set(info.presentationTimeUs)
            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                Log.d(TAG, "video BUFFER_FLAG_END_OF_STREAM, finished")
                return true
            }
            outputId = mediaCodec.dequeueOutputBuffer(info, 0)
        }
        return false
    }

    private fun updateVideoStartUs(videoCurrPts: Long) {
        if (videoCurrPts == 0L) {
            playParams.syncInfo.startVideoUs.set(SystemClock.elapsedRealtime() * 1000)
            Log.d(TAG, "setStartTime = " + playParams.syncInfo.startVideoUs.get())
        }

        //暂停一段时间后开始播放，需要更新startVideoUs，否则同步不准
        if (playParams.syncInfo.startVideoUsExpired.compareAndSet(true, false)) {
            playParams.syncInfo.startVideoUs.set(SystemClock.elapsedRealtime() * 1000 - playParams.syncInfo.lastVideoPts.get())
            Log.d(TAG, "setStartTime = " + playParams.syncInfo.startVideoUs.get())
        }
    }

    private fun videoTimeSync(videoCurrPts: Long) {
        var delayTime: Long = if (playParams.syncInfo.targetCurrPts.get() == 0L) {
            videoSingleInterval(videoCurrPts)
        } else {
            videoSyncToAudio(videoCurrPts)
        }
        delayTime = min(max(0, delayTime), 100000)
//        Log.d(TAG, "delayTime = $delayTime")
        if (delayTime > 0) {
            condition.await(delayTime, TimeUnit.MICROSECONDS)
        }
    }

    private fun videoSingleInterval(videoCurrPts: Long): Long {
        val playTime: Long =
            SystemClock.elapsedRealtime() * 1000 - playParams.syncInfo.startVideoUs.get()
        return videoCurrPts - playTime
    }

    private fun videoSyncToAudio(videoCurrPts: Long): Long {
        return videoCurrPts - playParams.syncInfo.targetCurrPts.get()
    }


}