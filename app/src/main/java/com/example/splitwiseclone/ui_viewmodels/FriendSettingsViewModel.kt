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

    fun deleteFriend(onSuccess: () -> Unit) {
        val friendToDelete = uiState.value.friend ?: return
        val currentUserId = friendToDelete.currentUserId

        viewModelScope.launch {
            try {
                apiService.deleteFriend(
                    currentUserId = currentUserId,
                    friendId = friendToDelete.friendId
                )
                friendRepository.deleteFriend(friendToDelete)
                Log.d("FriendSettingsVM", "Successfully deleted friend.")

                onSuccess()

            } catch (e: Exception) {
                Log.e("FriendSettingsVM", "Exception while deleting friend", e)
            }
        }
    }
}