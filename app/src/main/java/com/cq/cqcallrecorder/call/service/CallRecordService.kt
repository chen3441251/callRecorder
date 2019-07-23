package com.cq.cqcallrecorder.call.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.Nullable
import com.aykuttasil.callrecord.helper.PrefsHelper
import com.cq.cqcallrecorder.CallActivity
import com.cq.cqcallrecorder.call.CallRecord
import com.cq.cqcallrecorder.util.LogUtils

open class CallRecordService : Service() {

    private val TAG = CallRecordService::class.java.simpleName
    private lateinit var mCallRecord: CallRecord
    private val notificationId = "cqService"
    private val notificationName = "cqServiceName"

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, notificationId)
        } else {
            Notification.Builder(this)
        }
        notification.setContentTitle("船奇网")
        notification.setContentText("录音服务正在运行中...")
        val notificationIntent = Intent(this, CallActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        notification.setContentIntent(pendingIntent)
        startForeground(1, notification.build())
        LogUtils.i(TAG, "onCreate()")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        LogUtils.i(TAG, "onStartCommand()")

        val fileName = PrefsHelper.readPrefString(this, CallRecord.PREF_FILE_NAME)
        val dirPath = PrefsHelper.readPrefString(this, CallRecord.PREF_DIR_PATH)
        val dirName = PrefsHelper.readPrefString(this, CallRecord.PREF_DIR_NAME)
        val showSeed = PrefsHelper.readPrefBool(this, CallRecord.PREF_SHOW_SEED)
        val showPhoneNumber = PrefsHelper.readPrefBool(this, CallRecord.PREF_SHOW_PHONE_NUMBER)
        val outputFormat = PrefsHelper.readPrefInt(this, CallRecord.PREF_OUTPUT_FORMAT)
        val audioSource = PrefsHelper.readPrefInt(this, CallRecord.PREF_AUDIO_SOURCE)
        val audioEncoder = PrefsHelper.readPrefInt(this, CallRecord.PREF_AUDIO_ENCODER)

        mCallRecord = CallRecord.Builder(this).setRecordFileName(fileName).setRecordDirName(dirName)
            .setRecordDirPath(dirPath).setAudioEncoder(audioEncoder).setAudioSource(audioSource)
            .setOutputFormat(outputFormat).setShowSeed(showSeed).setShowPhoneNumber(showPhoneNumber)
            .build()
        mCallRecord.startCallReceiver()

        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
//        mCallRecord.stopCallReceiver()
        LogUtils.i(TAG, "onDestroy()")
    }

}
