package com.jonxiong.player.decode

import android.content.Context
import android.media.AudioTrack
import android.media.MediaCodec
import com.huawei.commom.LogUtil
import com.jonxiong.player.PlayParams

class AudioDecoder(avFlag: Int, context: Context, playParams: PlayParams) :
    BaseDecoder(avFlag, context, playParams) {
    companion object {
        private const val TAG = "JON_AudioDecoder"
    }

    private var audioTrack: AudioTrack? = null

    override fun configMediaCodec() {
        mediaCodec?.configure(mediaFormat, null, null, 0)
        mediaCodec?.start()
        audioTrack = mediaFormat?.toAudioTrack()
        try {
            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun doRender(mediaCodec: MediaCodec, outputId: Int, info: MediaCodec.BufferInfo) {
        mediaCodec.getOutputBuffer(outputId)?.let {
            audioTrack?.write(it, info.size, AudioTrack.WRITE_BLOCKING)
            mediaCodec.releaseOutputBuffer(outputId, false)
        }
    }

    override fun releaseDecoder() {
        super.releaseDecoder()
        if (audioTrack?.state != AudioTrack.STATE_UNINITIALIZED) {
            try {
                audioTrack?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        audioTrack?.release()
        LogUtil.d(TAG, "audioTrack release")
    }


}