package com.jonxiong.opengllib

class AnimateAttr {
    enum class AnimateType {
        TRANSITION, SCALE, ROTATE, REVERT
    }

    var type = AnimateType.TRANSITION
    var transitionVec = floatArrayOf(0f, 0f, 0f)
    var scaleVec = floatArrayOf(1f, 1f, 1f)
    var rotateVec = floatArrayOf(0f, 0f, 0f, 0f)
}