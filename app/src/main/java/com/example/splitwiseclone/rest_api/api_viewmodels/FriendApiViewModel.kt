package com.example.splitwiseclone.rest_api.api_viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.central.ApiClient
import com.example.splitwiseclone.central.SyncRepository
import com.example.splitwiseclone.rest_api.AddFriendResponse
import com.example.splitwiseclone.rest_api.AddFriendsRequest
import com.example.splitwiseclone.rest_api.DeleteFriendRequest
import com.example.splitwiseclone.rest_api.GetAllFriendsRequest
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.rest_api.UpdateFriendBalance
import com.example.splitwiseclone.roomdb.friends.Friend
import com.example.splitwiseclone.roomdb.friends.FriendRepository
import com.example.splitwiseclone.roomdb.user.CurrentUser
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
                // The API call is now expected to succeed even if a user isn't found
                val response = apiService.addFriends(request)

                // Check the response body to see if any numbers were reported as not found
                if (response.notFoundPhoneNumbers?.isNotEmpty() == true) {
                    // If so, trigger the "User Not Found" dialog in the UI
                    onUserNotFound(response.notFoundPhoneNumbers.first())
                } else if (response.friends.isNotEmpty()) {
                    // If friends were found and added, save them to the local DB and call onSuccess
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
                    // This case handles when the user is already a friend. We can treat it as a success.
                    onSuccess()
                }

            } catch (e: Exception) {
                // This will now only catch major errors like network failures or 500 server errors
                Log.e("FriendApiViewModel", "An error occurred while adding friends", e)
            }
        }
    }


    fun getAllFriends(id: String?) {
        viewModelScope.launch {
            apiClient.getAllFriends(id)
        }
    }

    fun deleteFriend(currentUserId: String, friendId: String, onSuccess: () -> Unit){
        val request = DeleteFriendRequest(
            currentUserId,
            friendId
        )
        viewModelScope.launch {
            try {
                apiService.deleteFriend(request)
                syncRepository.syncAllData()
                onSuccess()
            } catch (e: Exception) {
                Log.e("Error in deleting Friend", "error", e)
            }
        }
    }
}