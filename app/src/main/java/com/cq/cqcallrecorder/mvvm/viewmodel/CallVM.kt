package com.cq.cqcallrecorder.mvvm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cq.cqcallrecorder.mvvm.CQBaseViewModel

class CallVM : CQBaseViewModel() {
    fun appendNumber(number: String) {
        mCallNumberStr.value?.append(number)
    }

    fun delNumber() {
        mCallNumberStr.value?.apply {
            deleteCharAt(lastIndex)
        }
    }


    private val mCallNumberStr: MutableLiveData<StringBuilder> = MutableLiveData()
    var mCallNumberData: LiveData<StringBuilder> = mCallNumberStr


}