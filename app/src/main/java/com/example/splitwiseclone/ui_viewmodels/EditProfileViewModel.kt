package com.example.splitwiseclone.ui_viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.rest_api.UpdateUserRequest
import com.example.splitwiseclone.roomdb.entities.CurrentUser
import com.example.splitwiseclone.roomdb.user.CurrentUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val username: String = "",
    val imageUri: Uri? = null,
    val profilePictureUrl: String? = null,
    val oldPassword: String = "",
    val newPassword: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val currentUserRepository: CurrentUserRepository,
    private val apiService: RestApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()
    private var originalUser: CurrentUser? = null

    init {
        viewModelScope.launch {
            originalUser = currentUserRepository.currentUser.first()
            originalUser?.let { user ->
                _uiState.value = _uiState.value.copy(
                    username = user.username,
                    profilePictureUrl = user.profileUrl
                )
            }
        }
    }

    fun onUsernameChange(newName: String) { _uiState.value = _uiState.value.copy(username = newName) }
    fun onImageUriChange(newUri: Uri?) { _uiState.value = _uiState.value.copy(imageUri = newUri) }
    fun onOldPasswordChange(password: String) { _uiState.value = _uiState.value.copy(oldPassword = password) }
    fun onNewPasswordChange(password: String) { _uiState.value = _uiState.value.copy(newPassword = password) }

    fun saveChanges(onSuccess: () -> Unit) {
        val originalUser = this.originalUser ?: return
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isLoading = true)

        val newProfilePicUrl = if (currentState.imageUri != null) "https://new.image.url/from/server.jpg" else null

        val request = UpdateUserRequest(
            id = originalUser.currentUserId,
            newUsername = if (currentState.username != originalUser.username) currentState.username else null,
            newProfilePicUrl = newProfilePicUrl,
            oldPassword = currentState.oldPassword.ifBlank { null },
            newPassword = currentState.newPassword.ifBlank { null },
            newPhoneNumber = null
        )

        viewModelScope.launch {
            try {
                val updatedUserDto = apiService.updateCurrentUser(request)

                val updatedUserForDb = CurrentUser(
                    currentUserId = updatedUserDto.userId,
                    username = updatedUserDto.username,
                    email = updatedUserDto.email,
                    phoneNumber = updatedUserDto.phoneNumber,
                    profileUrl = updatedUserDto.profilePicture,
                    currencyCode = updatedUserDto.defaultCurrencyCode!!,
                    hashedPassword = updatedUserDto.hashedPassword!!
                )

                currentUserRepository.insertUser(updatedUserForDb)

                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()

            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Error updating user", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}