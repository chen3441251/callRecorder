package com.cq.cqcallrecorder

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import com.cq.cqcallrecorder.call.CallRecord
import com.cq.cqcallrecorder.mvvm.CQBaseActivity
import com.cq.cqcallrecorder.util.LogUtils
import com.cq.cqcallrecorder.util.ToastUtil
import kotlinx.android.synthetic.main.activity_call_ui.*
import pub.devrel.easypermissions.EasyPermissions

class CallActivity : CQBaseActivity(), EasyPermissions.PermissionCallbacks {

    private val permissionCode = 101

//    private lateinit var callRecord: CallRecord

    private var mCallNumberStr = StringBuilder()

    private val mPermissionsNeed = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.PROCESS_OUTGOING_CALLS,
        Manifest.permission.CALL_PHONE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_ui)
        initListener()
        initRecorder()
        checkPermissionsBeforeCall(mPermissionsNeed)
    }

    private fun initListener() {
        btnNumber0.setOnClickListener(onClickListener)
        btnNumber1.setOnClickListener(onClickListener)
        btnNumber2.setOnClickListener(onClickListener)
        btnNumber3.setOnClickListener(onClickListener)
        btnNumber4.setOnClickListener(onClickListener)
        btnNumber5.setOnClickListener(onClickListener)
        btnNumber6.setOnClickListener(onClickListener)
        btnNumber7.setOnClickListener(onClickListener)
        btnNumber8.setOnClickListener(onClickListener)
        btnNumber9.setOnClickListener(onClickListener)
        btnNumberA.setOnClickListener(onClickListener)
        btnNumberB.setOnClickListener(onClickListener)
        ivBackSpace.setOnClickListener(onClickListener)
        llCallDial.setOnClickListener(onClickListener)
        ivBackSpace.setOnLongClickListener {
            mCallNumberStr.clear()
            tvCallNumber.text = ""
            return@setOnLongClickListener true
        }
    }

    private fun appendNumber(number: String) {
        mCallNumberStr.append(number)
        tvCallNumber.text = mCallNumberStr
    }

    private fun delNumber() {
        if (mCallNumberStr.isNotEmpty()) {
            mCallNumberStr.deleteCharAt(mCallNumberStr.lastIndex)
            tvCallNumber.text = mCallNumberStr
        }
    }

    private fun call() {
        if (!checkPermissionsBeforeCall(mPermissionsNeed)) {
            return
        }
        val number = tvCallNumber.text.toString()
        if (number.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
            startActivity(intent)
        }
    }

    private val onClickListener = View.OnClickListener {
        when (it.id) {
            R.id.btnNumber0 -> {
                appendNumber("0")
            }
            R.id.btnNumber1 -> {
                appendNumber("1")
            }
            R.id.btnNumber2 -> {
                appendNumber("2")
            }
            R.id.btnNumber3 -> {
                appendNumber("3")
            }
            R.id.btnNumber4 -> {
                appendNumber("4")
            }
            R.id.btnNumber5 -> {
                appendNumber("5")
            }
            R.id.btnNumber6 -> {
                appendNumber("6")
            }
            R.id.btnNumber7 -> {
                appendNumber("7")
            }
            R.id.btnNumber8 -> {
                appendNumber("8")
            }
            R.id.btnNumber9 -> {
                appendNumber("9")
            }
            R.id.btnNumberA -> {
                appendNumber("*")
            }
            R.id.btnNumberB -> {
                appendNumber("#")
            }
            R.id.ivBackSpace -> {
                delNumber()
            }
            R.id.llCallDial -> {
                call()
            }
        }
    }

    private fun checkPermissionsBeforeCall(permissions: Array<String>): Boolean {
        return if (EasyPermissions.hasPermissions(this, *permissions)) true
        else {
            EasyPermissions.requestPermissions(
                this,
                resources.getString(R.string.text_permission_rationale_request_again),
                permissionCode,
                *permissions
            )
            false
        }
    }

    private fun initRecorder() {
        CallRecord.initService(this)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        if (list.size == mPermissionsNeed.size) {
//            call()
        } else {
            LogUtils.d("test", "list.size != mPermissionsNeed.size")
        }
    }

    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        ToastUtil.showToast(this, resources.getString(R.string.text_permission_rationale))
    }
}
