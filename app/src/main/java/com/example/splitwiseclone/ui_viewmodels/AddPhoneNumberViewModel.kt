package com.example.splitwiseclone.ui_viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddPhoneNumberViewModel @Inject constructor() : ViewModel() {

    private val _phoneNumber = mutableStateOf("")
    val phoneNumber: State<String> = _phoneNumber

    fun storePhoneNumber(number: String) {
        _phoneNumber.value = number
    }
}