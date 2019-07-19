package com.cq.cqcallrecorder.call.receiver

import android.content.Context
import android.media.MediaRecorder
import com.aykuttasil.callrecord.helper.PrefsHelper
import com.cq.cqcallrecorder.call.CallRecord
import com.cq.cqcallrecorder.util.LogUtils
import com.cq.cqcallrecorder.util.getCQRecordPath
import com.cq.cqcallrecorder.util.getRecordPath
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

open class CallRecordReceiver : PhoneCallReceiver() {

    companion object {
        private val TAG = CallRecordReceiver::class.java.simpleName

        const val ACTION_IN = "android.intent.action.PHONE_STATE"
        const val ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL"
        const val EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER"

        const val FILE_NAME_DIAL_TYPE_IN = "来电"
        const val FILE_NAME_DIAL_TYPE_OUT = "去电"

        const val DATE_FORMAT_CALL_TIME = "yyyyMMddHHmmss"

    }


    private var recorder: MediaRecorder? = null

    private var audioFile: File? = null
    private var isRecordStarted = false

    override fun onIncomingCallReceived(context: Context, number: String?, start: Date) {
    }

    override fun onIncomingCallAnswered(context: Context, number: String?, start: Date) {
//        startRecord(context, FILE_NAME_DIAL_TYPE_IN, number)
//        startCallLog(number, start, FILE_NAME_DIAL_TYPE_IN)
    }

    override fun onIncomingCallEnded(context: Context, number: String?, start: Date, end: Date) {
//        stopRecord(context)
        startCallLog(number, start, end, FILE_NAME_DIAL_TYPE_IN)
    }

    override fun onOutgoingCallStarted(context: Context, number: String?, start: Date) {
//        startRecord(context, FILE_NAME_DIAL_TYPE_OUT, number)
//        startCallLog(number, start, FILE_NAME_DIAL_TYPE_OUT)
    }

    override fun onOutgoingCallEnded(context: Context, number: String?, start: Date, end: Date) {
//        stopRecord(context)
        startCallLog(number, start, end, FILE_NAME_DIAL_TYPE_OUT)
    }

    override fun onMissedCall(context: Context, number: String?, start: Date) {
    }

    // Derived classes could override these to respond to specific events of interest
    protected fun onRecordingStarted(context: Context, audioFile: File?) {}

    protected fun onRecordingFinished(context: Context, audioFile: File?) {}

    private fun startCallLog(number: String?, start: Date, end: Date, fileNameDialTypeOut: String): String? {
        val formatter = SimpleDateFormat(DATE_FORMAT_CALL_TIME, Locale.getDefault())
        val startTime = formatter.format(start)
        val endTime = formatter.format(end)
        val numberStringBuilder = StringBuilder(number ?: "")
        if (numberStringBuilder.length == 11) {
            numberStringBuilder.insert(3, " ")
            numberStringBuilder.insert(8, " ")
        }
        val fileName = "${numberStringBuilder}_$startTime.amr"
        LogUtils.d("test", "call log fileName = $fileName")

        var dialType = ""
        when (fileNameDialTypeOut) {
            FILE_NAME_DIAL_TYPE_IN -> {
                dialType = "来电"
            }
            FILE_NAME_DIAL_TYPE_OUT -> {
                dialType = "去电"
            }
        }
        val newFileName = "${number}_${startTime}_${endTime}_$dialType.amr"
        LogUtils.d("test", "call log newFileName = $newFileName")

        val recordParentPath = getRecordPath()
        if (recordParentPath.isNotEmpty()) {
            val file = File(recordParentPath, fileName)
            val cqLogFile = File(getCQRecordPath(), newFileName)
            file.copyTo(cqLogFile)
        }

        return fileName
    }

    private fun startRecord(context: Context, seed: String, phoneNumber: String?) {
        try {
            val isSaveFile = PrefsHelper.readPrefBool(context, CallRecord.PREF_SAVE_FILE)

            // is save file?
            if (!isSaveFile) {
                return
            }

            if (isRecordStarted) {
                try {
                    recorder?.stop()  // stop the recording
                } catch (e: RuntimeException) {
                    // RuntimeException is thrown when stop() is called immediately after start().
                    // In this case the output file is not properly constructed ans should be deleted.
                    LogUtils.d(TAG, "RuntimeException: stop() is called immediately after start()")
                    audioFile?.delete()
                }

                releaseMediaRecorder()
                isRecordStarted = false
            } else {
                if (prepareAudioRecorder(context, seed, phoneNumber)) {
                    recorder!!.start()
                    isRecordStarted = true
                    onRecordingStarted(context, audioFile)
                    LogUtils.i(TAG, "record start")
                } else {
                    releaseMediaRecorder()
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            releaseMediaRecorder()
        } catch (e: RuntimeException) {
            e.printStackTrace()
            releaseMediaRecorder()
        } catch (e: Exception) {
            e.printStackTrace()
            releaseMediaRecorder()
        }
    }

    fun stopRecord(context: Context) {
        try {
            if (recorder != null && isRecordStarted) {
                releaseMediaRecorder()
                isRecordStarted = false
                onRecordingFinished(context, audioFile)
                LogUtils.i(TAG, "record stop")
            }
        } catch (e: Exception) {
            releaseMediaRecorder()
            e.printStackTrace()
        }
    }

    private fun prepareAudioRecorder(
        context: Context, seed: String, phoneNumber: String?
    ): Boolean {
        try {

            var fileName = PrefsHelper.readPrefString(context, CallRecord.PREF_FILE_NAME)
            val dirPath = PrefsHelper.readPrefString(context, CallRecord.PREF_DIR_PATH)
            val dirName = PrefsHelper.readPrefString(context, CallRecord.PREF_DIR_NAME)
            val showSeed = PrefsHelper.readPrefBool(context, CallRecord.PREF_SHOW_SEED)
            val showPhoneNumber =
                PrefsHelper.readPrefBool(context, CallRecord.PREF_SHOW_PHONE_NUMBER)
            val outputFormat = PrefsHelper.readPrefInt(context, CallRecord.PREF_OUTPUT_FORMAT)
            val audioSource = PrefsHelper.readPrefInt(context, CallRecord.PREF_AUDIO_SOURCE)
            val audioEncoder = PrefsHelper.readPrefInt(context, CallRecord.PREF_AUDIO_ENCODER)

            /*
            var file_name = PrefsHelper.readPrefString(context, CallRecord.PREF_FILE_NAME)
            val dir_path = PrefsHelper.readPrefString(context, CallRecord.PREF_DIR_PATH)
            val dir_name = PrefsHelper.readPrefString(context, CallRecord.PREF_DIR_NAME)
            val show_seed = PrefsHelper.readPrefBool(context, CallRecord.PREF_SHOW_SEED)
            val show_phone_number =
                PrefsHelper.readPrefBool(context, CallRecord.PREF_SHOW_PHONE_NUMBER)
            val output_format = PrefsHelper.readPrefInt(context, CallRecord.PREF_OUTPUT_FORMAT)
            val audio_source = PrefsHelper.readPrefInt(context, CallRecord.PREF_AUDIO_SOURCE)
            val audio_encoder = PrefsHelper.readPrefInt(context, CallRecord.PREF_AUDIO_ENCODER)
            */

            val sampleDir = File("$dirPath/$dirName")

            if (!sampleDir.exists()) {
                sampleDir.mkdirs()
            }

            val fileNameBuilder = StringBuilder()
            fileNameBuilder.append(fileName)
            fileNameBuilder.append("_")

            if (showSeed) {
                fileNameBuilder.append(seed)
                fileNameBuilder.append("_")
            }

            if (showPhoneNumber && phoneNumber != null) {
                fileNameBuilder.append(phoneNumber)
                fileNameBuilder.append("_")
            }

            fileName = fileNameBuilder.toString()

            val suffix: String
            when (outputFormat) {
                MediaRecorder.OutputFormat.AMR_NB -> {
                    suffix = ".amr"
                }
                MediaRecorder.OutputFormat.AMR_WB -> {
                    suffix = ".amr"
                }
                MediaRecorder.OutputFormat.MPEG_4 -> {
                    suffix = ".mp4"
                }
                MediaRecorder.OutputFormat.THREE_GPP -> {
                    suffix = ".3gp"
                }
                else -> {
                    suffix = ".amr"
                }
            }

            audioFile = File.createTempFile(fileName, suffix, sampleDir)

            recorder = MediaRecorder().apply {
                setAudioSource(audioSource)
                setOutputFormat(outputFormat)
                setAudioEncoder(audioEncoder)
                setOutputFile(audioFile!!.absolutePath)
                setOnErrorListener { mr, what, extra ->
                    LogUtils.d(TAG, "record error : what = $what extra = $extra")
                }
            }

            try {
                recorder?.prepare()
            } catch (e: IllegalStateException) {
                LogUtils.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.message)
                releaseMediaRecorder()
                return false
            } catch (e: IOException) {
                LogUtils.d(TAG, "IOException preparing MediaRecorder: " + e.message)
                releaseMediaRecorder()
                return false
            }

            return true
        } catch (e: Exception) {
            LogUtils.e()
            e.printStackTrace()
            return false
        }
    }

    private fun releaseMediaRecorder() {
        recorder?.apply {
            reset()
            release()
        }
        recorder = null
    }

}
