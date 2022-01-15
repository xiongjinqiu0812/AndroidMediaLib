package com.jonxiong.player.render

import android.view.Surface

interface OnGLSurfaceReadyListener {
    fun onGLSurfaceReady(surface: Surface?)
}