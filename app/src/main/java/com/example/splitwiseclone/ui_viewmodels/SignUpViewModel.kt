package com.example.splitwiseclone.ui_viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import android.util.Patterns

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _currencyCode = mutableStateOf("")
    val currencyCode: State<String> = _currencyCode

    private val _phoneNumber = mutableStateOf("")
    val phoneNumber: State<String> = _phoneNumber

    // --- State for Validation Errors ---
    private val _usernameError = mutableStateOf<String?>(null)
    val usernameError: State<String?> = _usernameError

    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError

    private val _passwordError = mutableStateOf<String?>(null)
    val passwordError: State<String?> = _passwordError

    private val _phoneNumberError = mutableStateOf<String?>(null)
    val phoneNumberError: State<String?> = _phoneNumberError


    // --- State Update Functions ---
    fun storeUsername(value: String) {
        _username.value = value
        _usernameError.value = null // Clear error on change
    }

    fun storeEmail(value: String) {
        _email.value = value
        _emailError.value = null
    }

    fun storePassword(value: String) {
        _password.value = value
        _passwordError.value = null
    }

    fun storeCurrencyCode(value: String) {
        _currencyCode.value = value
    }

    fun storePhoneNumber(value: String) {
        _phoneNumber.value = value
        _phoneNumberError.value = null
    }

    /**
     * Validates all input fields and returns true if they are valid.
     * Updates error states for the UI to observe.
     */
    fun validateInputs(): Boolean {
        if (_username.value.isBlank()) {
            _usernameError.value = "Username cannot be empty"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _emailError.value = "Please enter a valid email address"
            return false
        }
        if (_password.value.length < 8) {
            _passwordError.value = "Password must be at least 8 characters long"
            return false
        }
        if (_phoneNumber.value.length < 10) { // Simple length check
            _phoneNumberError.value = "Please enter a valid phone number"
            return false
        }
        return true
    }
}