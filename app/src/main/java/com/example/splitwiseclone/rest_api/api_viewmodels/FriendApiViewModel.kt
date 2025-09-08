package com.example.splitwiseclone.rest_api.api_viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.central.ApiClient
import com.example.splitwiseclone.central.SyncRepository
import com.example.splitwiseclone.rest_api.AddFriendsRequest
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.friends.FriendRepository
import com.example.splitwiseclone.roomdb.entities.CurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendApiViewModel @Inject constructor(
    private val apiService: RestApiService,
    private val friendRepository: FriendRepository,
    private val syncRepository: SyncRepository,
    private val apiClient: ApiClient
): ViewModel() {

    fun addNewFriend(
        phoneNumbers: List<String>,
        currentUser: CurrentUser,
        onSuccess: () -> Unit,
        onUserNotFound: (String) -> Unit
    ) {
        val request = AddFriendsRequest(
            phoneNumbers,
            currentUser.currentUserId,
            currentUser.email,
            currentUser.profileUrl!!,
            currentUser.phoneNumber!!,
            currentUser.username
        )
        viewModelScope.launch {
            try {
                val response = apiService.addFriends(request)

                if (response.notFoundPhoneNumbers?.isNotEmpty() == true) {
                    onUserNotFound(response.notFoundPhoneNumbers.first())
                } else if (response.friends.isNotEmpty()) {
                    response.friends.forEach { friend ->
                        friendRepository.insertFriend(
                            Friend(
                                id = friend.id,
                                email = friend.email,
                                phoneNumber = friend.phoneNumber,
                                profilePic = friend.profilePic,
                                username = friend.username,
                                currentUserId = currentUser.currentUserId,
                                friendId = friend.friendId
                            )
                        )
                    }
                    syncRepository.syncAllData()
                    onSuccess()
                } else {
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e("FriendApiViewModel", "An error occurred while adding friends", e)
            }
        }
    }


}