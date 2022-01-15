package com.jonxiong.opengllib.utils;

import android.util.Log;

import java.util.Locale;

/**
 * Created by w00501804 on 2017/8/24.
 */

public class GLLog {
    public static String LOG_PREFIX = "HWE_";

    public static void d(String TAG, String msg) {
        Log.d(LOG_PREFIX + TAG, msg);
    }

    public static void d(String TAG, String msg, Object... args) {
        Log.d(LOG_PREFIX + TAG, String.format(Locale.CHINA, msg, args));
    }

    public static void i(String TAG, String msg, Object... args) {
        Log.i(LOG_PREFIX + TAG, String.format(Locale.CHINA, msg, args));
    }

    public static void w(String TAG, String msg, Object... args) {
        Log.w(LOG_PREFIX + TAG, String.format(Locale.CHINA, msg, args));
    }

    public static void e(String TAG, String msg) {
        Log.e(LOG_PREFIX + TAG, msg);
    }

    public static void e(String TAG, String msg, Throwable throwable) {
        Log.e(LOG_PREFIX + TAG, msg, throwable);
    }
}
