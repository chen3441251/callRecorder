package com.cq.cqcallrecorder.util

import android.content.Context
import android.view.Gravity
import android.widget.Toast

object ToastUtil {

    fun showToast(context: Context, message: String?) {
        Toast.makeText(context, message ?: "", Toast.LENGTH_SHORT).show()
    }

    fun showToastCenter(context: Context, message: String?) {
        val toast = Toast.makeText(context, message ?: "", Toast.LENGTH_SHORT)
        toast?.setGravity(Gravity.CENTER, 0, 0)
        toast?.show()
    }
}