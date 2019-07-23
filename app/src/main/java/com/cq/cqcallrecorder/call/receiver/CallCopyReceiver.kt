package com.cq.cqcallrecorder.call.receiver

import android.content.Context
import com.cq.cqcallrecorder.util.LogUtils
import com.cq.cqcallrecorder.util.getCQRecordPath
import com.cq.cqcallrecorder.util.getRecordPath
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

open class CallCopyReceiver : PhoneCallReceiver() {

    companion object {
        private val TAG = CallCopyReceiver::class.java.simpleName

        const val ACTION_IN = "android.intent.action.PHONE_STATE"
        const val ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL"
        const val EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER"

        const val FILE_NAME_DIAL_TYPE_IN = "来电"
        const val FILE_NAME_DIAL_TYPE_OUT = "去电"

        const val DATE_FORMAT_CALL_TIME = "yyyyMMddHHmmss"

    }


    override fun onIncomingCallReceived(context: Context, number: String?, start: Date) {
    }

    override fun onIncomingCallAnswered(context: Context, number: String?, start: Date) {
    }

    override fun onIncomingCallEnded(context: Context, number: String?, start: Date, end: Date) {
        startCallLog(number, start, end, FILE_NAME_DIAL_TYPE_IN)
    }

    override fun onOutgoingCallStarted(context: Context, number: String?, start: Date) {
    }

    override fun onOutgoingCallEnded(context: Context, number: String?, start: Date, end: Date) {
        startCallLog(number, start, end, FILE_NAME_DIAL_TYPE_OUT)
    }

    override fun onMissedCall(context: Context, number: String?, start: Date) {
    }

    // Derived classes could override these to respond to specific events of interest
    protected fun onRecordingStarted(context: Context, audioFile: File?) {}

    protected fun onRecordingFinished(context: Context, audioFile: File?) {}

    private fun startCallLog(number: String?, start: Date, end: Date, fileNameDialTypeOut: String): String? {
        val formatter = SimpleDateFormat(DATE_FORMAT_CALL_TIME, Locale.getDefault())
        var startTime = formatter.format(start)
        val endTime = formatter.format(end)
        val dialType = when (fileNameDialTypeOut) {
            FILE_NAME_DIAL_TYPE_IN -> "来电"
            FILE_NAME_DIAL_TYPE_OUT -> "去电"
            else -> ""
        }
        val newFileName = "${number}_${startTime}_${endTime}_$dialType.amr"

        val recordParentPath = getRecordPath()
        if (recordParentPath.isNotEmpty()) {
            val hwParentDir = File(recordParentPath)
            val file = hwParentDir.listFiles().run {
                sortBy {
                    it.lastModified()
                }
                lastOrNull()
            }
            LogUtils.d(TAG, "fileName = $file ${file?.name}")
            file?.let {
                val cqLogFile = File(getCQRecordPath(), newFileName)
                LogUtils.d(TAG, "newFileName = $newFileName")
                try {
                    if (it.exists() && !cqLogFile.exists())
                        it.copyTo(cqLogFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return file?.name ?: ""
        }
        return ""
    }


}
