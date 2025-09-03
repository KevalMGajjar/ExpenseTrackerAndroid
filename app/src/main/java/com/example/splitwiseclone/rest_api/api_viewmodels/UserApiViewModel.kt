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
import com.example.splitwiseclone.roomdb.user.CurrentUser
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

    fun resetLoginStatus(){
        _loginSuccess.value = false
    }

    fun registerUser(user: User) {
        viewModelScope.launch {
            try {
                val request = UserRegisterRequest(
                    user.username,
                    user.email,
                    user.password,
                    user.currencyCode,
                    user.profileUrl,
                    user.phoneNumber
                    )
                val response = apiService.registerUser(request)

                if (response.isSuccessful) {
                    Log.d("UserViewModel", "Registration successful: ${response.body()}")
                }
                else{
                    val errorCode = response.code()
                    val errorMessage = response.errorBody()?.string()
                    Log.e("UserViewModel", "Registration failed with code $errorCode: $errorMessage")

                }
            } catch (e: Exception){

            }
        }
    }

    fun loginUser(email: String, password: String, onSuccess: () -> Unit) {
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

                onSuccess()

            } catch (e: Exception) {
                Log.e("UserViewModel", "Login failed with an exception", e)
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

    fun updateUser(newUsername: String? = null, newPassword: String? = null, oldPassword: String? = null, newProfilePicture: String? = null, newPhoneNumber: String? = null, currentUserId: String?, onSuccess: () -> Unit) {

        if(currentUserId != null) {
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
                    apiService.updateCurrentUser(request)
                    onSuccess()
                } catch (e: Exception) {
                    Log.e("error", "error while updating user", e)
                }
            }
        }
    }
}