package com.jonxiong.encapsulator

class NativeLib {

    /**
     * A native method that is implemented by the 'encapsulator' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'encapsulator' library on application startup.
        init {
            System.loadLibrary("encapsulator")
        }
    }
}