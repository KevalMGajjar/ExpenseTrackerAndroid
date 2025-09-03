package com.example.splitwiseclone.rest_api.api_viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.central.ApiClient
import com.example.splitwiseclone.central.SyncRepository
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

    fun addNewFriend(phoneNumbers: List<String>, currentUser: CurrentUser, onSuccess:() -> Unit) {

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
                val foundUsers = apiService.addFriends(request)
                foundUsers.friends.forEach { friend ->
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
            } catch (e: Exception) {
                Log.e("find_user", "error", e)
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

//    fun updateFriendBalance(friendId: String, updatedBalance: Double, currentUserId: String) {
//        val request = UpdateFriendBalance(
//            friendId = friendId,
//            updatedBalance = updatedBalance,
//            id = currentUserId
//        )
//        viewModelScope.launch {
//            try {
//                val updatedFriend = apiService.updateFriendBalance(request)
//                friendRepository.updateFriend(Friend(
//                    id = updatedFriend.id,
//                    email = updatedFriend.email ?: "",
//                    phoneNumber = updatedFriend.phoneNumber,
//                    profilePic = updatedFriend.profilePic,
//                    username = updatedFriend.username,
//                    currentUserId = updatedFriend.currentUserId,
//                    friendId = updatedFriend.friendId,
//                ))
//            } catch (e: Exception) {
//                Log.e("Error in updating Friend", "error", e)
//            }
//        }
//    }
}