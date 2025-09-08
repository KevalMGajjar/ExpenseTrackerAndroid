package com.example.splitwiseclone.ui_viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import android.util.Patterns

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError

    private val _passwordError = mutableStateOf<String?>(null)
    val passwordError: State<String?> = _passwordError

    fun storeEmail(value: String) {
        _email.value = value
        _emailError.value = null
    }

    fun storePassword(value: String) {
        _password.value = value
        _passwordError.value = null
    }

    fun validateInputs(): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _emailError.value = "Please enter a valid email address"
            return false
        }
        if (_password.value.isBlank()) {
            _passwordError.value = "Password cannot be empty"
            return false
        }
        return true
    }
}