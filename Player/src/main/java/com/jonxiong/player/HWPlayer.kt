package com.jonxiong.player

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.jonxiong.player.decode.VideoPlayActivity
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.locks.ReentrantLock

class HWPlayer(private val context: Context) : BasePlayer() {

    companion object {
        const val TAG = "HWPlayer"
    }

    private var outputIndexQueue: Deque<Int> = ArrayDeque()
    private var surface: Surface? = null
    private var mediaCodec: MediaCodec? = null
    private var mediaExtractor: MediaExtractor? = null
    private var mediaFormat: MediaFormat? = null
    private var frameInterval = 0L

    private val playThread: HandlerThread = HandlerThread("HWPlayer")
    private var handler: Handler
    private var runnable: Runnable? = null

    private val lock = ReentrantLock()

    init {
        playParams.playStats = PlayState.UN_KNOW
        playThread.start()
        handler = Handler(playThread.looper)
    }

    private fun checkRunnable() {
        if (runnable == null) {
            runnable = Runnable {
                lock.lock()
                handler.removeCallbacksAndMessages(null)
                if (!outputIndexQueue.isEmpty()) {
                    mediaCodec?.releaseOutputBuffer(outputIndexQueue.removeFirst(), true)
                } else {
                    Log.d(TAG, "outputIndexQueue is empty")
                }

                if (playParams.playStats == PlayState.PLAYING) {
                    runnable?.let { handler.postDelayed(it, frameInterval) }
                } else {
                    Log.d(TAG, "playParams.playStats is ${playParams.playStats}")
                }
                lock.unlock()
            }
        }
    }

    fun setSurface(surface: Surface?) {
        if (surface == null) {
            Log.e(TAG, "surface is null")
            return
        }
        this.surface = surface
    }

    override fun play(url: String?) {
        super.play(url)
        if (url == null) {
            Log.e(TAG, "url is null")
            return
        }

        checkRunnable()

        when (playParams.playStats) {
            PlayState.UN_KNOW, PlayState.STOP -> {
                initMediaCodec(url)
                runnable?.let {
                    handler.post(it)
                }
            }
            PlayState.PLAYING -> {
                return
            }
            PlayState.PAUSED -> {
                lock.lock()
                playParams.playStats = PlayState.PLAYING
                lock.unlock()
                handler.post { runnable }
            }
        }
    }

    private fun initMediaCodec(url: String) {
        if (surface == null) {
            Log.e(TAG, "surface is null")
            return
        }

        mediaExtractor = MediaExtractor()
        val extractor = mediaExtractor ?: return

        try {
            val assetFileDescriptor = context.assets.openFd(url)
            extractor.setDataSource(
                assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset,
                assetFileDescriptor.length
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var mediaFormatMime: String? = null
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i) ?: continue
            mediaFormat = format
            mediaFormatMime = format.getString(MediaFormat.KEY_MIME)
            if (mediaFormatMime?.startsWith("video/") == true) {
                extractor.selectTrack(i)
                break
            }
        }

        val frameRate = mediaFormat?.getInteger(MediaFormat.KEY_FRAME_RATE) ?: 0
        if (frameRate > 0) {
            frameInterval = (1000f / frameRate).toLong()
            Log.d(TAG, "frameInterval = $frameInterval")
        }

        if (mediaFormatMime == null) return
        mediaCodec = MediaCodec.createDecoderByType(mediaFormatMime)
        mediaCodec?.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                lock.lock()
                val mediaExtractor = this@HWPlayer.mediaExtractor ?: return
                var inputBuffer: ByteBuffer? = null
                try {
                    inputBuffer = codec.getInputBuffer(index)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                if (inputBuffer != null) {
                    val readSampleData = mediaExtractor.readSampleData(inputBuffer, 0)
                    val presentationTimeUs = mediaExtractor.sampleTime
                    val sampleFlags = mediaExtractor.sampleFlags

                    if (readSampleData >= 0) {
                        codec.queueInputBuffer(
                            index,
                            0,
                            readSampleData,
                            presentationTimeUs,
                            sampleFlags
                        )
                        mediaExtractor.advance()
                        return
                    }

                    if (playParams.loop) {
                        mediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                        Log.d(TAG, "seek to start")
                        return
                    }

                    codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)

                }
                lock.unlock()
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                lock.lock()
                if (info.flags.and(MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0 && !playParams.loop) {
                    Log.d(TAG, "outputFinish")
                    lock.lock()
                    playParams.playStats = PlayState.STOP
                    lock.unlock()
                    return
                }
                outputIndexQueue.add(index)
                lock.unlock()
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Log.e(TAG, "onError")
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            }

        }, handler)

        lock.lock()
        playParams.playStats = PlayState.PLAYING
        lock.unlock()

        mediaCodec?.configure(mediaFormat, surface, null, 0)
        mediaCodec?.start()
    }

    override fun pause() {
        lock.lock()
        playParams.playStats = PlayState.PAUSED
        handler.removeCallbacksAndMessages(null)
        lock.unlock()
    }

    override fun stop() {
        super.stop()
        lock.lock()
        playParams.playStats = PlayState.STOP
        mediaExtractor?.release()
        mediaCodec?.release()
        handler.removeCallbacksAndMessages(null)
        lock.unlock()
    }

    override fun seekTo(ptsUs: Long, mode: Int) {
        if (ptsUs < 0L) return
        lock.lock()
        playParams.seekPts = ptsUs
        playParams.seekMode = mode
        lock.unlock()
    }

    override fun release() {
        super.release()
        lock.lock()
        playParams.playStats = PlayState.STOP
        mediaExtractor?.release()
        mediaCodec?.stop()
        mediaCodec?.release()
        handler.removeCallbacksAndMessages(null)
        playThread.quitSafely()
        runnable = null
        mediaExtractor = null
        mediaCodec = null
        lock.unlock()
    }

}

