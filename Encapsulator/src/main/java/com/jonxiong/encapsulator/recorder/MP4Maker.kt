package com.jonxiong.encapsulator.recorder

import android.content.Context
import java.util.concurrent.Executors

class MP4Maker(private val context: Context) {
    private val mediaMuxerWrapper by lazy { MediaMuxerWrapper() }
    private val aacEncoder by lazy { AacEncoder(mediaMuxerWrapper, 0, 1) }
    private val h264Encoder by lazy { H264Encoder(mediaMuxerWrapper, 1, 1, 30) }
    private val aacRecorder by lazy {
        AacRecorder(
            context,
            AacRecorder.AudioRecordParams(
                1,1,1,1,1
            ),
            aacEncoder
        )
    }

    private val executorService = Executors.newFixedThreadPool(5)

    fun start() {
        executorService.execute { mediaMuxerWrapper }
        executorService.execute { aacEncoder }
        executorService.execute { aacRecorder }
        executorService.execute { h264Encoder }
    }

    fun stop() {
        mediaMuxerWrapper.stopMuxer()
        aacRecorder.stopRecorder()
        aacEncoder.stopEncoder()
        h264Encoder.stopEncoder()
        executorService.shutdown()
    }

}