package com.example.splitwiseclone.ui_viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(

): ViewModel() {

    private var _username = mutableStateOf("")
    var username: State<String> = _username

    private var _email = mutableStateOf("")
    var email: State<String> = _email

    private var _password = mutableStateOf("")
    var password: State<String> = _password

    private var _currencyCode = mutableStateOf("")
    var currencyCode = _currencyCode

    private var _phoneNumber = mutableStateOf("")
    var phoneNumber = _phoneNumber

    fun storePhoneNumber(phoneNumber:String) {
        _phoneNumber.value = phoneNumber
    }

    fun storeUsername(username: String) {
        _username.value = username
    }

    fun storeEmail(email: String) {
        _email.value = email
    }

    fun storePassword(password: String) {
        _password.value = password
    }

    fun storeCurrencyCode(currencyCode: String) {
        _currencyCode.value = currencyCode
    }
}