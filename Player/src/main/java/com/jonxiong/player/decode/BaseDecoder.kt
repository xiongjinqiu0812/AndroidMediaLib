package com.jonxiong.player.decode

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import androidx.annotation.CallSuper
import com.jonxiong.player.a_player.MediaExtractorWrapper
import com.jonxiong.player.a_player.PlayParams
import com.jonxiong.player.a_player.PlayState
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.max

abstract class BaseDecoder(var avFlag: Int, var context: Context, var playParams: PlayParams) :
    IDecoder {


    private val flag = if (avFlag == PlayParams.VIDEO_FLAG) "Video" else "Audio"
    private val TAG = "JON_${flag}Decoder"

    protected val lock = ReentrantLock()
    protected val condition = lock.newCondition()
    protected var decodeState = PlayState.UN_KNOW

    protected var mediaFormat: MediaFormat? = null
    protected var mediaCodec: MediaCodec? = null
    protected var extractor: MediaExtractorWrapper? = null

    private val mInfo = MediaCodec.BufferInfo()

//    protected var mOnRenderListener: OnRenderListener? = null


    override fun changeState(state: PlayState) {
        lock.lock()
        val needNotify = decodeState == PlayState.PAUSED
        decodeState = state
        if (needNotify) {
            condition.signal()
        }
        lock.unlock()
    }

    override fun run() {
        lock.lock()

        //初始化
        if (!initMediaCodec()) {
            return
        }

        decodeState = PlayState.PLAYING

        configMediaCodec()

        try {
            doFrame()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }

        releaseDecoder()

        lock.unlock()
    }

    private fun doFrame() {
        var isFinish = false
        while (!isFinish) {
            if (decodeState == PlayState.STOP) {
                Log.d(TAG, "PlayState.STOP, break")
                break
            }
            //是否暂停
            while (decodeState == PlayState.PAUSED) {
                Log.d(TAG, "PlayState.PAUSED, wait")
                playParams.syncInfo.startVideoUsExpired.compareAndSet(false, true)
                Log.d(TAG, "startVideoUs expired")
                condition.await()
            }

            while (decodeState == PlayState.PLAYING) {
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
                val inputBufferId = mediaCodec.dequeueInputBuffer(1000)
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
                    isFinish = handleOutputData(mInfo)
                    if (isFinish) {
                        decodeState = PlayState.STOP
                    }
                }
            }
        }
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

    @CallSuper
    override fun releaseDecoder() {
        mediaCodec?.release()
        Log.d(TAG, "mediaCodec release ${mediaCodec?.hashCode()}")
        extractor?.release()
        Log.d(TAG, "extractor release")
    }

    abstract fun configMediaCodec()

    abstract fun handleOutputData(info: MediaCodec.BufferInfo): Boolean


}