package com.example.splitwiseclone.rest_api.api_viewmodels

import androidx.lifecycle.ViewModel
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.rest_api.models.User
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.rest_api.UserLoginRequest
import com.example.splitwiseclone.rest_api.UserRegisterRequest
import com.example.splitwiseclone.rest_api.security.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import android.util.Log
import com.example.splitwiseclone.datastore.DataStoreManager
import com.example.splitwiseclone.rest_api.GoogleLoginRequest
import com.example.splitwiseclone.rest_api.UpdateUserRequest
import com.example.splitwiseclone.roomdb.entities.CurrentUser
import com.example.splitwiseclone.roomdb.user.CurrentUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


@HiltViewModel
class UserApiViewModel @Inject constructor(
    val apiService: RestApiService,
    val tokenManager: TokenManager,
    val currentUserRepository: CurrentUserRepository,
    val dataStoreManager: DataStoreManager
): ViewModel() {

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess = _registrationSuccess.asStateFlow()

    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError = _registrationError.asStateFlow()
    fun resetLoginStatus(){
        _loginSuccess.value = false
        _loginError.value = null
    }

    fun resetRegistrationStatus() {
        _registrationSuccess.value = false
        _registrationError.value = null
    }
    fun registerUser(user: User) {
        viewModelScope.launch {
            try {
                val request = UserRegisterRequest(
                    user.username, user.email, user.password,
                    user.currencyCode, user.profileUrl, user.phoneNumber
                )
                val response = apiService.registerUser(request)

                if (response.isSuccessful) {
                    Log.d("UserViewModel", "Registration successful: ${response.body()}")
                    _registrationSuccess.value = true // Signal success to the UI
                } else {
                    val errorCode = response.code()
                    val errorMessage = response.errorBody()?.string()
                    Log.e("UserViewModel", "Registration failed with code $errorCode: $errorMessage")
                    _registrationError.value = "Registration failed: $errorMessage" // Signal error to the UI
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Registration failed with an exception", e)
                _registrationError.value = "An unexpected error occurred."
            }
        }
    }
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val request = UserLoginRequest(
                    email,
                    password
                )
                val response = apiService.loginUser(request)
                tokenManager.saveTokens(accessToken = response.accessToken, refreshToken = response.refreshToken)

                currentUserRepository.insertUser(CurrentUser(
                    username = response.username,
                    currentUserId = response.userId,
                    email = response.email,
                    phoneNumber = response.phoneNumber,
                    profileUrl = response.profilePicture,
                    currencyCode = response.defaultCurrencyCode,
                    hashedPassword = response.hashedPassword
                ))

                dataStoreManager.saveLoginStatus(true)

                // On success, update the state variable for the UI to observe.
                _loginSuccess.value = true

            } catch (e: Exception) {
                Log.e("UserViewModel", "Login failed with an exception", e)
                // On failure, update the error state for the UI to observe.
                _loginError.value = "Login failed. Please check your credentials."
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                val response = apiService.loginWithGoogle(GoogleLoginRequest(idToken))

                tokenManager.saveTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )

                currentUserRepository.insertUser(
                    CurrentUser(
                        username = response.username,
                        currentUserId = response.userId,
                        email = response.email,
                        phoneNumber = response.phoneNumber,
                        profileUrl = response.profilePicture,
                        currencyCode = response.defaultCurrencyCode,
                        hashedPassword = response.hashedPassword
                    )
                )
                _loginSuccess.value = true
                dataStoreManager.saveLoginStatus(true)
                Log.d("UserVm", "Success")

            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception during Google login", e)
                _loginSuccess.value = false
            }
        }
    }

    fun updateUser(
        currentUserId: String?,
        newUsername: String? = null,
        newPassword: String? = null,
        oldPassword: String? = null,
        newProfilePicture: String? = null,
        newPhoneNumber: String? = null,
        // FIX: The onSuccess callback now provides the updated user object
        onSuccess: (updatedUser: CurrentUser) -> Unit
    ) {
        if (currentUserId != null) {
            val request = UpdateUserRequest(
                id = currentUserId,
                newProfilePicUrl = newProfilePicture,
                newPassword = newPassword,
                oldPassword = oldPassword,
                newUsername = newUsername,
                newPhoneNumber = newPhoneNumber
            )

            viewModelScope.launch {
                try {
                    // FIX: The API service now returns the updated user object from the server
                    val responseUser = apiService.updateCurrentUser(request)

                    // Map the server response to your local CurrentUser entity
                    val updatedCurrentUser = CurrentUser(
                        currentUserId = responseUser.userId,
                        username = responseUser.username,
                        email = responseUser.email,
                        phoneNumber = responseUser.phoneNumber,
                        profileUrl = responseUser.profilePicture,
                        currencyCode = responseUser.defaultCurrencyCode!!,
                        hashedPassword = responseUser.hashedPassword!!
                    )

                    // Pass the new, correct user object back to the calling screen
                    onSuccess(updatedCurrentUser)

                } catch (e: Exception) {
                    Log.e("UserApiViewModel", "Error while updating user", e)
                }
            }
        }
    }

    // In your UserApiViewModel.kt file

    fun deleteUserAccount(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteUserAccount(userId)
                if (response.isSuccessful) {
                    Log.d("UserApiViewModel", "Successfully deleted user account on server.")
                    onSuccess() // Signal to the UI that the API call was successful
                } else {
                    Log.e("UserApiViewModel", "API error deleting user account: ${response.code()}")
                    // Optionally, you could add an onError callback here
                }
            } catch (e: Exception) {
                Log.e("UserApiViewModel", "Exception while deleting user account", e)
            }
        }
    }
}