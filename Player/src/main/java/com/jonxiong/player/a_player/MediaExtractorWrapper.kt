package com.jonxiong.player.a_player

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.text.TextUtils
import java.io.IOException
import java.nio.ByteBuffer

/**
 * 解析 MediaExtractor
 */
class MediaExtractorWrapper(avFlag: Int = PlayParams.VIDEO_FLAG, context: Context, url: String) {
    var mediaExtractor: MediaExtractor? = null
    var mediaFormat: MediaFormat? = null
    var trackId = -1

    init {
        try {
            mediaExtractor = MediaExtractor()
            val afd = context.assets.openFd(url)
            mediaExtractor!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val count = mediaExtractor?.trackCount ?: 0
        for (i in 0 until count) {
            val format = mediaExtractor!!.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (TextUtils.isEmpty(mime)) {
                continue
            }

            if (avFlag == PlayParams.AUDIO_FLAG && mime?.startsWith("audio") == true) {
                trackId = i
                mediaFormat = format
                break
            }

            if (avFlag == PlayParams.VIDEO_FLAG && mime?.startsWith("video") == true) {
                trackId = i
                mediaFormat = format
                break
            }

        }
    }

    /**
     * 获取当前帧的时间戳
     */
    var sampleTime: Long = 0

    /**
     * 获取当前帧的标志位
     */
    var sampleFlags = 0

    fun selectTrack(trackId: Int) {
        mediaExtractor?.selectTrack(trackId)
    }

    /**
     * 读取一帧的数据
     * @param buffer
     * @return
     */
    fun readBuffer(buffer: ByteBuffer): Int {
        val mediaExtractor = this.mediaExtractor ?: return -1
        buffer.clear()
        val bufferCount = mediaExtractor.readSampleData(buffer, 0)
        if (bufferCount < 0) {
            return -1
        }
        sampleTime = mediaExtractor.sampleTime
        sampleFlags = mediaExtractor.sampleFlags
        mediaExtractor.advance()
        return bufferCount
    }

    fun seekTo(timeUs: Long, mode: Int) {
        mediaExtractor?.seekTo(timeUs, mode)
    }

    /**
     * 释放资源
     */
    fun release() {
        mediaExtractor?.release()
    }


}