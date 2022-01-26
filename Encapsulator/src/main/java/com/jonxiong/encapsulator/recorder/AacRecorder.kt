package com.jonxiong.encapsulator.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.min

@SuppressLint("MissingPermission")
class AacRecorder(
    private val context: Context,
    private val params: AudioRecordParams,
    private val aacEncoder: AacEncoder
) : Runnable {

    data class AudioRecordParams(
        var audioSource: Int,
        var sampleRateInHz: Int,
        var channelConfig: Int,
        var audioFormat: Int,
        var bufferSizeInBytes: Int
    )

    private val audioRecord: AudioRecord by lazy {
        AudioRecord(
            params.audioSource,
            params.sampleRateInHz,
            params.channelConfig,
            params.audioFormat,
            params.bufferSizeInBytes
        )
    }
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    private var isRecording = false

    private val minBufferSize = AudioRecord.getMinBufferSize(
        params.sampleRateInHz,
        params.channelConfig,
        AudioFormat.ENCODING_PCM_16BIT
    )

    override fun run() {
        if (isRecording) {
            return
        }

        lock.lock()
        isRecording = true
        lock.unlock()

        audioRecord.startRecording()

        while (isRecording) {
            val tempBuffer = ByteArray(min(2048, minBufferSize))
            val len: Int = audioRecord.read(tempBuffer, 0, tempBuffer.size)
            if (len > 0) {
                aacEncoder.putData(tempBuffer)
            }
        }

        audioRecord.stop()
        audioRecord.release()
        lock.lock()
        isRecording = false
        lock.unlock()
    }

    fun stopRecorder() {
        lock.lock()
        isRecording = false
        condition.signalAll()
        lock.unlock()
    }
}

