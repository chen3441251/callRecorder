package com.cq.cqcallrecorder.call.receiver

import android.content.Context
import android.media.MediaRecorder
import com.aykuttasil.callrecord.helper.PrefsHelper
import com.cq.cqcallrecorder.call.CallRecord
import com.cq.cqcallrecorder.util.LogUtils
import java.io.File
import java.io.IOException

class RecordFactory(private val listener: OnRecordListener? = null) {

    interface OnRecordListener {
        fun onRecordingStarted(context: Context, audioFile: File?)
        fun onRecordingFinished(context: Context, audioFile: File?)
    }

    private val TAG = this.javaClass.simpleName

    private var recorder: MediaRecorder? = null

    private var audioFile: File? = null
    private var isRecordStarted = false

    private fun startRecord(context: Context, number: String, phoneNumber: String?) {
        try {
            val isSaveFile = PrefsHelper.readPrefBool(context, CallRecord.PREF_SAVE_FILE)

            if (!isSaveFile) {
                return
            }

            if (isRecordStarted) {
                try {
                    recorder?.stop()  // stop the recording
                } catch (e: RuntimeException) {
                    LogUtils.d(TAG, "RuntimeException: stop() is called immediately after start()")
                    audioFile?.delete()
                }

                releaseMediaRecorder()
                isRecordStarted = false
            } else {
                if (prepareAudioRecorder(context, number, phoneNumber)) {
                    recorder!!.start()
                    isRecordStarted = true
                    listener?.onRecordingStarted(context, audioFile)
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
                listener?.onRecordingFinished(context, audioFile)
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
                setOnErrorListener { _, what, extra ->
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