package com.jonxiong.player.decode

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.SystemClock
import androidx.annotation.CallSuper
import com.huawei.commom.LogUtil
import com.jonxiong.player.PlayParams
import com.jonxiong.player.PlayState
import com.jonxiong.player.render.OnRenderListener
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.max
import kotlin.math.min

abstract class BaseDecoder(var avFlag: Int, var context: Context, var playParams: PlayParams) :
    IDecoder {

    //这个类从创建到修改都在解码线程中，所以对它的操作不用加锁
    data class SyncData(var startTimeMs: Long, var startPtsUs: Long) {

        fun reset(currentTimeMs: Long, currentPtsUs: Long) {
            startTimeMs = currentTimeMs
            startPtsUs = currentPtsUs
            LogUtil.d("SyncData", "startTimeMs = $startTimeMs   startPtsUs = $startPtsUs")
        }

        fun getDelayTime(currentTimeMs: Long, currentPtsUs: Long): Long {
            val dT = (currentTimeMs - startTimeMs) * 1000
            val dPts = currentPtsUs - startPtsUs
            return min(max(0, (dPts - dT)), 100000)
        }
    }

    private val flag = if (avFlag == PlayParams.VIDEO_FLAG) "Video" else "Audio"
    private val TAG = "JON_${flag}Decoder"

    protected val lock = ReentrantLock()
    protected val condition = lock.newCondition()
    protected var decodeState = PlayState.UN_KNOW
    protected lateinit var syncData: SyncData


    protected var mediaFormat: MediaFormat? = null
    protected var mediaCodec: MediaCodec? = null
    protected var extractor: MediaExtractorWrapper? = null
    private val tempInfo = MediaCodec.BufferInfo()
    private var seekPts: Long = -1L
    private var lastPts: Long = 0L

    var onRenderListener: OnRenderListener? = null


    override fun changeState(state: PlayState) {
        lock.lock()
        val needNotify = decodeState == PlayState.PAUSED
        decodeState = state
        if (needNotify) {
            condition.signal()
        }
        lock.unlock()
    }

    override fun seekTo(pts: Long) {
        if (pts >= 0) {
            lock.lock()
            seekPts = pts
            lock.unlock()
        }
    }

    private fun waitLastVideoStop() {
        lock.lock()
        while (decodeState != PlayState.UN_KNOW) {
            condition.await(500, TimeUnit.MILLISECONDS)
        }
        lock.unlock()
    }

    override fun run() {

        //等待上一个视频播放结束
        waitLastVideoStop()

        //初始化
        if (!initMediaCodec()) {
            return
        }

        decodeState = PlayState.PLAYING

        configMediaCodec()

        //初始化同步信息
        syncData = SyncData(SystemClock.elapsedRealtime(), 0)

        try {
            loopForDecode()
        } catch (e: Exception) {
            LogUtil.e(TAG, e.message, e)
        }

        releaseDecoder()
    }

    private fun loopForDecode() {
        var isFinish = false
        while (!isFinish) {
            if (decodeState == PlayState.STOP) {
                LogUtil.d(TAG, "PlayState.STOP, break")
                break
            }
            //是否暂停
            var isPause = false
            while (decodeState == PlayState.PAUSED) {
                isPause = true
                LogUtil.d(TAG, "PlayState.PAUSED, wait")
                lock.lock()
                condition.await()
                lock.unlock()
            }

            //从暂停状态重新苏醒
            if (isPause) {
                syncData.reset(SystemClock.elapsedRealtime(), lastPts)
            }

            while (decodeState == PlayState.PLAYING) {
                //处理seek
                lock.lock()
                if (seekPts >= 0) {
                    try {
                        extractor?.seekTo(seekPts, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                        syncData.reset(SystemClock.elapsedRealtime(), seekPts)
                        LogUtil.d(TAG, "seek to $seekPts")
                    } catch (e: Exception) {
                        LogUtil.e(TAG, "seek fail", e)
                    } finally {
                        seekPts = -1L
                    }
                }
                lock.unlock()

                val mediaCodec = this.mediaCodec ?: break
                val extractor = this.extractor ?: break

                readSampleData(mediaCodec, extractor)
                if (writeSampleData(mediaCodec, tempInfo)) {
                    if (playParams.loop) {
                        mediaCodec.flush()
                        syncData.reset(SystemClock.elapsedRealtime(), seekPts)
                        LogUtil.d(TAG, "replay")
                    } else {
                        lock.lock()
                        decodeState = PlayState.STOP
                        lock.unlock()
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

            if (seekPts >= 0) {
                extractor.seekTo(seekPts, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    private fun readSampleData(mediaCodec: MediaCodec, extractor: MediaExtractorWrapper): Int {
        val inputBufferId = mediaCodec.dequeueInputBuffer(1000)
        if (inputBufferId >= 0) {
            val inputBuffer = mediaCodec.getInputBuffer(inputBufferId)
            if (inputBuffer != null) {
                val size: Int = extractor.readBuffer(inputBuffer)
                val sampleSize = max(size, 0)
                val sampleTime = if (size >= 0) extractor.sampleTime else 0L
                var sampleFlags = if (size >= 0) extractor.sampleFlags else 1
                if (size < 0) {
                    sampleFlags = MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    if (playParams.loop) {
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
                if (size >= 0) {
                    extractor.advance()
                }
            }
        }
        return inputBufferId
    }

    private fun writeSampleData(
        mediaCodec: MediaCodec,
        info: MediaCodec.BufferInfo
    ): Boolean {
        var outputId: Int = mediaCodec.dequeueOutputBuffer(info, 1000)
        while (outputId >= 0) {

            timeSync(info.presentationTimeUs)

            doRender(mediaCodec, outputId, info)

            lock.lock()
            lastPts = info.presentationTimeUs
            lock.unlock()

            if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                LogUtil.d(TAG, "BUFFER_FLAG_END_OF_STREAM, finished")
                return true
            }
            outputId = mediaCodec.dequeueOutputBuffer(info, 0)
        }
        return false
    }

    private fun timeSync(currentPtsUs: Long) {
        val delayTime = syncData.getDelayTime(SystemClock.elapsedRealtime(), currentPtsUs)
        lock.lock()
        condition.await(delayTime, TimeUnit.MICROSECONDS)
        lock.unlock()
    }

    abstract fun doRender(mediaCodec: MediaCodec, outputId: Int, info: MediaCodec.BufferInfo)

    @CallSuper
    override fun releaseDecoder() {
        mediaCodec?.release()
        LogUtil.d(TAG, "mediaCodec release ${mediaCodec?.hashCode()}")
        extractor?.release()
        LogUtil.d(TAG, "extractor release")
        lock.lock()
        decodeState = PlayState.UN_KNOW
        condition.signalAll()
        lock.unlock()
    }

    abstract fun configMediaCodec()


}