package com.jonxiong.encapsulator.recorder

import android.media.MediaFormat
import android.media.MediaMuxer
import java.util.concurrent.locks.ReentrantLock

class MediaMuxerWrapper : Runnable{

    companion object {
        private const val TRACK_VIDEO = "TRACK_VIDEO"
        private const val TRACK_AUDIO = "TRACK_AUDIO"
    }

    private var mediaMuxer: MediaMuxer? = null
    private var isReadyStart = false
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    private var videoTrack = -1
    private var audioTrack = -1

    private var isRunning = false

    fun getMediaMuxer() = mediaMuxer

    init {
        mediaMuxer = MediaMuxer("", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    fun addTrackIndex(track: String, mediaFormat: MediaFormat) {
        lock.lock()
        if (isReadyStart) {
            lock.unlock()
            return
        }

        if (videoTrack < 0 && track == TRACK_VIDEO) {
            videoTrack = mediaMuxer?.addTrack(mediaFormat) ?: -1
        }

        if (audioTrack < 0 && track == TRACK_AUDIO) {
            audioTrack = mediaMuxer?.addTrack(mediaFormat) ?: -1
        }

        if (videoTrack >= 0 && audioTrack >= 0) {
            isReadyStart = true
            mediaMuxer?.start()
        }

        lock.unlock()
    }

    override fun run() {
        lock.lock()
        isRunning = true
        lock.unlock()

        while (isRunning) {
            //todo 写入视频
            //todo 写入音频
        }

        mediaMuxer?.stop()
        mediaMuxer?.release()
    }


    fun stopMuxer() {
        lock.lock()
        isRunning = false
        condition.signalAll()
        lock.unlock()
    }




}