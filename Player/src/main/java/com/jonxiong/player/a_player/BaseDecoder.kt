package com.jonxiong.player.a_player

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import com.jonxiong.player.PlayParams
import com.jonxiong.player.PlayState
import com.jonxiong.player.decode.DecodeState
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.max

abstract class BaseDecoder(var avFlag: Int, var context: Context, var playParams: PlayParams) :
    IDecoder {

    companion object {
        private const val TAG = "BaseDecoder"
    }

    protected val lock = ReentrantLock()
    protected val condition = lock.newCondition()

    protected var decodeState = DecodeState.UN_KNOW

    protected var mediaFormat: MediaFormat? = null
    protected var mediaCodec: MediaCodec? = null
    protected var extractor: MediaExtractorWrapper? = null

    private val mInfo = MediaCodec.BufferInfo()

//    protected var mOnRenderListener: OnRenderListener? = null


    override fun changeState(state: DecodeState) {
        lock.lock()
        decodeState = state
        condition.signalAll()
    }

    override fun run() {
        lock.lock()
        //是否暂停
        while (playParams.playState == PlayState.PAUSED) {
            condition.await()
        }
        //初始化
        if (!initMediaCodec()) {
            lock.unlock()
            return
        }

        configMediaCodec()

        while (playParams.playState == PlayState.PLAYING) {
            //处理seek
            if (playParams.seekPts >= 0) {
                try {
                    extractor?.seekTo(playParams.seekPts, playParams.seekMode)
                } catch (e: Exception) {
                    Log.e(TAG, "seek fail", e)
                } finally {
                    playParams.seekPts = -1L
                }
            }

            val mediaCodec = this.mediaCodec ?: break
            val extractor = this.extractor ?: break

            //解码
            val inputBufferId = mediaCodec.dequeueInputBuffer(0)
            if (inputBufferId >= 0) {
                val inputBuffer = mediaCodec.getInputBuffer(inputBufferId)
                if (inputBuffer != null) {
                    val size: Int = extractor.readBuffer(inputBuffer)
                    val sampleSize = max(size, 0)
                    val sampleTime = if (size >= 0) extractor.sampleTime else 0L
                    var sampleFlags = if (size >= 0) extractor.sampleFlags else 1
                    if (size < 0) {
                        if (!playParams.loop) {
                            sampleFlags = sampleFlags or MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        } else {
                            extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                        }
                    }
                    mediaCodec.queueInputBuffer(
                        inputBufferId,
                        0,
                        sampleSize,
                        sampleTime,
                        sampleFlags
                    )
                }

                val isFinish: Boolean = handleOutputData(mInfo)
                if (isFinish) {
                    decodeState = DecodeState.STOP
                }
            }

            condition.await(getSyncTime(), TimeUnit.MICROSECONDS)
        }
        lock.unlock()
    }

    private fun initMediaCodec(): Boolean {
        try {
            extractor = MediaExtractorWrapper(avFlag, context, playParams.url)
            val extractor = this.extractor ?: return false
            mediaFormat = extractor.mediaFormat
            extractor.selectTrack(extractor.trackId)
            val mime: String = mediaFormat?.getString(MediaFormat.KEY_MIME) ?: return false
            mediaCodec = MediaCodec.createDecoderByType(mime)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }


    abstract fun getSyncTime(): Long

    abstract fun configMediaCodec()

    abstract fun handleOutputData(info: MediaCodec.BufferInfo): Boolean


}