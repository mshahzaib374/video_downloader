package com.example.a4kvideodownloaderplayer.fragments.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private var _pageSelector: MutableLiveData<Int> = MutableLiveData()
    var pageSelector: LiveData<Int> = _pageSelector


    fun updatePageSelector(position: Int) {
        _pageSelector.value = position
    }


    private var _exitDialogStatus: MutableLiveData<Boolean> = MutableLiveData()
    var exitDialogStatus: LiveData<Boolean> = _exitDialogStatus


    fun showExitDialog(status: Boolean) {
        _exitDialogStatus.value = status
    }

    fun hideExitDialog(status: Boolean) {
        _exitDialogStatus.value = status
    }


}