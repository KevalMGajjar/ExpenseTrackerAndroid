package com.example.splitwiseclone.ui_viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddPhoneNumberViewModel @Inject constructor(

): ViewModel() {

    private var _phoneNumber = mutableStateOf("")
    var phoneNumber = _phoneNumber

    fun storePhoneNumber(phoneNumber:String) {
        _phoneNumber.value = phoneNumber
    }
}