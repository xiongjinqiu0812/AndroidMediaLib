package com.jonxiong.player

import java.util.concurrent.atomic.AtomicLong

class CodecSyncInfo private constructor() {
    //目标pts
    var targetCurrPts = AtomicLong(0)

    //没有目标时，视频轨道同步信息
    var startVideoUs = AtomicLong(0)
    var lastVideoPts = AtomicLong(0)


    companion object {
        fun create(): CodecSyncInfo {
            return CodecSyncInfo()
        }
    }

    fun reset() {
        targetCurrPts = AtomicLong(0)
        startVideoUs = AtomicLong(0)
        lastVideoPts = AtomicLong(0)
    }
}