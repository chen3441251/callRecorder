package com.cq.cqcallrecorder.util

import android.util.Log
import com.cq.cqcallrecorder.BuildConfig

object LogUtils {

    @JvmStatic
    fun i(tag: String, message: String?) {
        if (BuildConfig.DEBUG)
            Log.i(tag, message)
    }


    @JvmStatic
    fun d(tag: String, message: String = "") {
        if (BuildConfig.DEBUG)
            Log.d(tag, message)
    }

    @JvmStatic
    fun e(message: String = "") {
        if (BuildConfig.DEBUG)
            Log.e("CQCall", message)
    }

}