package com.cq.cqcallrecorder.util

import android.os.Environment
import java.io.File


fun getRecordPath(): String {
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        val sdDir: StringBuilder = StringBuilder()
        sdDir.append(Environment.getExternalStorageDirectory().absolutePath)
        sdDir.append(File.separator)
        sdDir.append("Sounds")
        sdDir.append(File.separator)
        sdDir.append("CallRecord")
        return sdDir.toString()
    } else {
        return ""
    }
}

fun getCQRecordPath(): String {
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        val cqRecFile: StringBuilder = StringBuilder()
        cqRecFile.append(Environment.getExternalStorageDirectory().absolutePath)
        cqRecFile.append(File.separator)
        cqRecFile.append("cq")
        cqRecFile.append(File.separator)
        cqRecFile.append("recorder")
        cqRecFile.append(File.separator)
        val file = File(cqRecFile.toString())
        if (!file.exists()) {
            file.mkdirs()
        }
        return cqRecFile.toString()
    } else {
        return ""
    }
}

