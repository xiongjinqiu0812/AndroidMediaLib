package com.jonxiong.encapsulator.recorder

import android.media.*
import java.io.IOException
import java.util.*
import java.util.concurrent.locks.ReentrantLock

class H264Encoder(val mediaMuxer: MediaMuxerWrapper?, val width: Int, val height: Int, fps: Int): Runnable {
    companion object {
        private const val VIDEO_MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC //就是 H264
        private const val H264_ENCODER = 1
    }

    private var mediaCodec: MediaCodec? = null
    private val byteQueue = LinkedList<ByteArray>()
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    private var isEncoding = false
    private var nanoTime = 0L

    private var videoTrack = -1

    init {
        nanoTime = System.nanoTime()

        val bitRate = width * height * 3 / 2 * 8 * fps
        val mediaFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, height, width).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            )
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
        }

        try {
            mediaCodec = MediaCodec.createDecoderByType(VIDEO_MIME_TYPE).let {
                it.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                it.start()
                it
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun putData(array: ByteArray) {
        lock.lock()
        byteQueue.offer(array)
        condition.signal()
        lock.unlock()
    }

    override fun run() {
        val mediaCodec = this.mediaCodec ?: return
        while (isEncoding) {
            lock.lock()
            while (byteQueue.isEmpty()) {
                condition.await()
            }
            lock.unlock()

            val byteArray = byteQueue.poll() ?: continue
            val yuv420sp = ByteArray(width * height * 3 / 2)
            nv21ToNv12(byteArray, yuv420sp, width, height)

            val inputIndex: Int = mediaCodec.dequeueInputBuffer(10000)
            if (inputIndex >= 0) {
                mediaCodec.getInputBuffer(inputIndex)?.let {
                    it.clear()
                    it.put(byteArray)
                }
                val pts = (System.nanoTime() - nanoTime) / 1000
                mediaCodec.queueInputBuffer(inputIndex, 0, yuv420sp.size, pts, 0)
            }

            val bufferInfo = MediaCodec.BufferInfo()

            var outputIndex: Int = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)

            if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                mediaMuxer?.getMediaMuxer()?.addTrack(mediaCodec.outputFormat)
            }

            while (outputIndex > 0) {
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0
                }

                mediaCodec.getOutputBuffer(outputIndex)?.let {
                    if (videoTrack >= 0) {
                        mediaMuxer?.getMediaMuxer()?.writeSampleData(videoTrack, it, bufferInfo)
                    }

                    mediaCodec.releaseOutputBuffer(outputIndex, false)

                    outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
                }
            }
        }

        mediaCodec.stop()
        mediaCodec.release()
        byteQueue.clear()
    }


    fun stopEncoder() {
        lock.lock()
        isEncoding = false
        condition.signalAll()
        lock.unlock()
    }

    private fun nv21ToNv12(nv21: ByteArray?, nv12: ByteArray?, width: Int, height: Int) {
        if (nv21 == null || nv12 == null) return
        val frameSize = width * height
        var i = 0
        var j = 0
        System.arraycopy(nv21, 0, nv12, 0, frameSize)
        i = 0
        while (i < frameSize) {
            nv12[i] = nv21[i]
            i++
        }
        j = 0
        while (j < frameSize / 2) {
            nv12[frameSize + j - 1] = nv21[j + frameSize]
            j += 2
        }
        j = 0
        while (j < frameSize / 2) {
            nv12[frameSize + j] = nv21[j + frameSize - 1]
            j += 2
        }
    }

}