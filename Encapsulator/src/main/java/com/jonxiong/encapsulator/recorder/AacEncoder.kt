package com.jonxiong.encapsulator.recorder

import android.media.*
import java.io.IOException
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.locks.ReentrantLock

class AacEncoder(val mediaMuxer: MediaMuxerWrapper?, sampleRateInHz: Int, channelConfig: Int): Runnable {

    companion object {
        private const val AUDIO_MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC //就是 aac
        private const val AAC_ENCODER = 2
    }

    private var mediaCodec: MediaCodec? = null
    private val byteQueue = LinkedList<ByteArray>()
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    private var isEncoding = false

    private var audioTrack = -1

    private var prevOutputPTSUs = 0L

    init {
        val channelCount =
            if (channelConfig == AudioFormat.CHANNEL_OUT_MONO) 1 else 2
        val bitRate =
            if (sampleRateInHz * 16 * channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2

        val mediaFormat = MediaFormat.createAudioFormat(
            AUDIO_MIME_TYPE, sampleRateInHz, channelCount
        ).apply {
            setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )
            //CHANNEL_IN_STEREO 立体声
//            setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_STEREO)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount)
            setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRateInHz)
        }

        try {
            mediaCodec = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE)
            mediaCodec?.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaCodec?.start()

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
            //从缓存中获取数据
            lock.lock()
            while (byteQueue.isEmpty()) {
                condition.await()
            }
            lock.unlock()

            val byteArray = byteQueue.poll() ?: continue

            val inputIndex: Int = mediaCodec.dequeueInputBuffer(10000)
            if (inputIndex > 0) {
                mediaCodec.getInputBuffer(inputIndex)?.let {
                    it.clear()
                    it.put(byteArray)
                }
                mediaCodec.queueInputBuffer(inputIndex, 0, byteArray.size, getPtsUs(), 0)
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
                bufferInfo.presentationTimeUs = getPtsUs()

                mediaCodec.getOutputBuffer(outputIndex)?.let {
                    if (audioTrack >= 0) {
                        mediaMuxer?.getMediaMuxer()?.writeSampleData(audioTrack, it, bufferInfo)
                    }
                    prevOutputPTSUs = bufferInfo.presentationTimeUs

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

    private fun getPtsUs(): Long {
        var result = System.nanoTime()
        if (result < prevOutputPTSUs) {
            result = prevOutputPTSUs
        }
        return result
    }

}