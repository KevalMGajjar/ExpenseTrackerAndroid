package com.example.splitwiseclone.ui_viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.friends.FriendRepository
import com.example.splitwiseclone.roomdb.entities.Group
import com.example.splitwiseclone.roomdb.groups.GroupRepository
import com.example.splitwiseclone.roomdb.user.CurrentUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State for the friend settings screen
data class FriendSettingsUiState(
    val friend: Friend? = null,
    val sharedGroups: List<Group> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FriendSettingsViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val groupRepository: GroupRepository,
    private val currentUserRepository: CurrentUserRepository,
    private val apiService: RestApiService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val friendId: StateFlow<String?> = savedStateHandle.getStateFlow("friendId", null)

    val uiState: StateFlow<FriendSettingsUiState> = friendId.filterNotNull().flatMapLatest { id ->
        combine(
            friendRepository.findFriendById(id),
            groupRepository.allGroups,
            currentUserRepository.currentUser.filterNotNull()
        ) { friend, allGroups, currentUser ->
            if (friend == null) return@combine FriendSettingsUiState(isLoading = true)

            val shared = allGroups.filter { group ->
                val memberIds = group.members?.mapNotNull { it.userId } ?: emptyList()
                currentUser.currentUserId in memberIds && friend.friendId in memberIds
            }

            FriendSettingsUiState(
                friend = friend,
                sharedGroups = shared,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FriendSettingsUiState()
    )

    // FIX: The logic is now structured correctly for an API call that returns Unit.
    fun deleteFriend(onSuccess: () -> Unit) {
        val friendToDelete = uiState.value.friend ?: return
        val currentUserId = friendToDelete.currentUserId

        viewModelScope.launch {
            try {
                // 1. Call the API to delete the friend on the server.
                // If this function throws an exception, the `catch` block will be executed.
                apiService.deleteFriend(
                    currentUserId = currentUserId,
                    friendId = friendToDelete.friendId
                )

                // 2. If the API call completes without error, it means it was successful.
                // We can now safely update our local database.
                friendRepository.deleteFriend(friendToDelete)
                Log.d("FriendSettingsVM", "Successfully deleted friend.")

                // 3. Signal success to the UI to trigger navigation.
                onSuccess()

            } catch (e: Exception) {
                // This block will catch any network errors or HTTP error codes (like 404, 500, etc.).
                Log.e("FriendSettingsVM", "Exception while deleting friend", e)
            }
        }
    }
}