package com.jonxiong.player.decode

import android.content.Context
import android.media.AudioTrack
import android.media.MediaCodec
import android.util.Log
import com.jonxiong.player.PlayParams
import java.lang.Exception
import java.nio.ByteBuffer

class AudioDecoder(avFlag: Int, context: Context, playParams: PlayParams) :
    BaseDecoder(avFlag, context, playParams) {
    companion object {
        private const val TAG = "AudioDecoder"
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

    override fun handleOutputData(info: MediaCodec.BufferInfo): Boolean {
        val mediaCodec: MediaCodec = this.mediaCodec ?: return false
        var outputIndex: Int = mediaCodec.dequeueOutputBuffer(info, 0)
        var outputBuffer: ByteBuffer?
        while (outputIndex >= 0) {
            outputBuffer = mediaCodec.getOutputBuffer(outputIndex)
            if (outputBuffer != null) {
                //写数据到 AudioTrack 中，实现音频播放
                audioTrack?.write(outputBuffer, info.size, AudioTrack.WRITE_BLOCKING)
                mediaCodec.releaseOutputBuffer(outputIndex, false)
            }
            outputIndex = mediaCodec.dequeueOutputBuffer(info, 0)
        }
        playParams.syncInfo.targetCurrPts.set(info.presentationTimeUs)
        // 在所有解码后的帧都被渲染后，就可以停止播放了
        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
            Log.d(TAG, "Audio BUFFER_FLAG_END_OF_STREAM, finished")
            return true
        }
        return false
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
        Log.d(TAG, "audioTrack release")
    }


}