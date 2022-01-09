package com.jonxiong.video.decode

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.jonxiong.video.R
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*


class VideoPlayActivity : AppCompatActivity(), SurfaceHolder.Callback2 {

    companion object {
        const val TAG: String = "VideoPlayActivity"
        const val videoName: String = "baby.mp4"
    }

    private lateinit var surfaceView: SurfaceView

    @Volatile
    private var surfaceReady = false

    @Volatile
    private var waitSurfaceReady = false

    @Volatile
    private var playFinish = false

    private lateinit var outputIndexQueue: Deque<Int>
    private var mediaCodec: MediaCodec? = null
    private var mediaExtractor: MediaExtractor? = null
    private var mediaFormat: MediaFormat? = null
    private var mediaFormatMime: String? = null
    private var frameInterval = 16L

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        surfaceView = findViewById(R.id.play_view)
        surfaceView.holder.addCallback(this)

        initMediaExtractor(this, videoName)

        if (mediaFormatMime == null) {
            mediaExtractor?.release()
            return
        }

        initMediaCodec()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaExtractor?.release()
        mediaCodec?.release()
    }

    private fun initDecodeThread() {
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            if (!outputIndexQueue.isEmpty()) {
                mediaCodec?.releaseOutputBuffer(outputIndexQueue.removeFirst(), true)
            }
            if (!playFinish) {
                handler.postDelayed(runnable, frameInterval)
            }
        }
    }

    private fun initMediaExtractor(context: Context, fileName: String) {
        mediaExtractor = MediaExtractor()
        val extractor = mediaExtractor ?: return
        try {
            val assetFileDescriptor = context.assets.openFd(fileName)
            extractor.setDataSource(
                assetFileDescriptor.fileDescriptor, assetFileDescriptor.startOffset,
                assetFileDescriptor.length
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
    }

    private fun initMediaCodec() {
        val mediaFormatMime = mediaFormatMime ?: return
        mediaCodec = MediaCodec.createDecoderByType(mediaFormatMime)
        outputIndexQueue = ArrayDeque()

        mediaCodec?.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                var inputBuffer: ByteBuffer? = null
                try {
                    inputBuffer = codec.getInputBuffer(index)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                if (inputBuffer != null) {
                    val readSampleData = mediaExtractor!!.readSampleData(inputBuffer, 0)
                    val presentationTimeUs = mediaExtractor!!.sampleTime
                    val sampleFlags = mediaExtractor!!.sampleFlags
                    if (readSampleData < 0) {
                        codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    } else {
                        codec.queueInputBuffer(
                            index,
                            0,
                            readSampleData,
                            presentationTimeUs,
                            sampleFlags
                        )
                        mediaExtractor!!.advance()
                    }
                }
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {

                if (info.flags.and(MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    playFinish = true
                    Log.d(TAG, "outputFinish")
                }
                outputIndexQueue.add(index)
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            }

        })

        if (surfaceReady) {
            startMediaCodec()
        } else {
            waitSurfaceReady = true
        }
    }

    private fun startMediaCodec() {
        mediaCodec?.configure(mediaFormat, surfaceView.holder.surface, null, 0)
        mediaCodec?.start()
        initDecodeThread()
        handler.postDelayed(runnable, frameInterval)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceReady = true
        if (waitSurfaceReady) {
            waitSurfaceReady = false
            startMediaCodec()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {

    }
}