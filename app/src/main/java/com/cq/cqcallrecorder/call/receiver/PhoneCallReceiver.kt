package com.cq.cqcallrecorder.call.receiver

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.cq.cqcallrecorder.util.LogUtils
import java.util.*

abstract class PhoneCallReceiver : BroadcastReceiver() {

    private val TAG = PhoneCallReceiver::class.java.simpleName

    private var telephonyManager: TelephonyManager? = null

    override fun onReceive(context: Context, intent: Intent) {
        LogUtils.d(TAG, "onReceive")
        if (intent.action == CallCopyReceiver.ACTION_OUT) {
            LogUtils.d(TAG, "intent.action = outgoing")
            savedNumber = intent.extras!!.getString(CallCopyReceiver.EXTRA_PHONE_NUMBER)
        } else {
            LogUtils.d(TAG, "intent.action = InComing")
            val number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            savedNumber = number
//            val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
//            var state = when (stateStr) {
//                TelephonyManager.EXTRA_STATE_IDLE -> TelephonyManager.CALL_STATE_IDLE
//                TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
//                TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
//                else -> TelephonyManager.CALL_STATE_IDLE
//            }
            //以上代码有效，但是已经有mPhoneStateListener，故注释不用
        }
        if (telephonyManager == null)//注册1遍。避免重复
            addPhoneStateListener(context)
    }

    //Derived classes should override these to respond to specific events of interest
    protected abstract fun onIncomingCallReceived(context: Context, number: String?, start: Date)

    protected abstract fun onIncomingCallAnswered(context: Context, number: String?, start: Date)

    protected abstract fun onIncomingCallEnded(context: Context, number: String?, start: Date, end: Date)

    protected abstract fun onOutgoingCallStarted(context: Context, number: String?, start: Date)

    protected abstract fun onOutgoingCallEnded(
        context: Context, number: String?, start: Date, end: Date
    )

    protected abstract fun onMissedCall(context: Context, number: String?, start: Date)

    private fun onCallStateChanged(context: Context, state: Int, number: String?) {
        LogUtils.d(TAG, "onCallStateChanged state = $state")
        if (lastState == state) {
            LogUtils.d(TAG, "onCallStateChanged lastState == state")
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_IDLE ->
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    onMissedCall(context, savedNumber, callStartTime)
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, Date())
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, Date())
                }
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number

                onIncomingCallReceived(context, number, callStartTime)
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()

                    onOutgoingCallStarted(context, savedNumber, callStartTime)
                } else {
                    isIncoming = true
                    callStartTime = Date()

                    onIncomingCallAnswered(context, savedNumber, callStartTime)
                }
        }
        lastState = state
    }

    private fun addPhoneStateListener(context: Context) {
        if (telephonyManager == null)
            telephonyManager = context.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        mPhoneStateListener.context = context
        telephonyManager?.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun removePhoneStateListener(context: Context?) {
        if (telephonyManager == null)
            telephonyManager = context?.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        mPhoneStateListener.context = context
        telephonyManager?.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE)
    }

    private val mPhoneStateListener = object : PhoneStateListener() {
        var context: Context? = null

        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            context?.let {
                onCallStateChanged(it, state, savedNumber)
            }
        }
    }

    companion object {

        //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date = Date()
        private var isIncoming: Boolean = false
        private var savedNumber: String? = null  //because the passed incoming is only valid in ringing
    }
}