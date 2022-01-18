package com.huawei.commom

import android.util.Log
import java.util.*

object LogUtil {
    var LOG_PREFIX = "HWE_"

    fun d(TAG: String, msg: String?) {
        Log.d(LOG_PREFIX + TAG, msg ?: "")
    }

    fun d(TAG: String, msg: String?, vararg args: Any?) {
        Log.d(LOG_PREFIX + TAG, String.format(Locale.CHINA, msg ?: "", *args))
    }

    fun i(TAG: String, msg: String?, vararg args: Any?) {
        Log.i(LOG_PREFIX + TAG, String.format(Locale.CHINA, msg ?: "", *args))
    }

    fun w(TAG: String, msg: String?, vararg args: Any?) {
        Log.w(LOG_PREFIX + TAG, String.format(Locale.CHINA, msg ?: "", *args))
    }

    fun e(TAG: String, msg: String?) {
        Log.e(LOG_PREFIX + TAG, msg ?: "")
    }

    fun e(TAG: String, msg: String?, throwable: Throwable?) {
        Log.e(LOG_PREFIX + TAG, msg, throwable)
    }
}