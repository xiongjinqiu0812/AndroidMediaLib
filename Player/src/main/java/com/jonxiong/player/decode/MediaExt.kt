package com.jonxiong.player.decode

import android.media.*

fun MediaFormat?.toAudioTrack(): AudioTrack? {
    val mediaFormat = this ?: return null
    //拿到采样率
    val pcmEncode: Int = if (mediaFormat.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
        mediaFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)
    } else {
        //默认采样率为 16bit
        AudioFormat.ENCODING_PCM_16BIT
    }

    //音频采样率
    val sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
    //获取音频通道数
    val channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
    //拿到声道
    val channelConfig =
        if (channelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
    val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, pcmEncode)

    /**
     * 设置音频信息属性
     * 1.设置支持多媒体属性，比如audio，video
     * 2.设置音频格式，比如 music
     */
    val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    /**
     * 设置音频数据
     * 1. 设置采样率
     * 2. 设置采样位数
     * 3. 设置声道
     */
    val format = AudioFormat.Builder()
        .setSampleRate(sampleRate)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setChannelMask(channelConfig)
        .build()


    //配置 audioTrack
    return AudioTrack(
        attributes,
        format,
        minBufferSize,
        AudioTrack.MODE_STREAM,  //采用流模式
        AudioManager.AUDIO_SESSION_ID_GENERATE
    )
}