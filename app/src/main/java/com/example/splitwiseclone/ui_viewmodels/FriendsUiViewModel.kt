package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.ViewModel
import com.example.splitwiseclone.roomdb.entities.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class FriendsUiViewModel @Inject constructor(

): ViewModel() {

    private val _selectedFriend = MutableStateFlow<Friend?>(Friend(id= "", username = "", email = "", phoneNumber = "", profilePic = "", balanceWithUser = 0.0, currentUserId = "", friendId = ""))
    val selectedFriend = _selectedFriend


    fun selectFriend(friend: Friend) {
        _selectedFriend.value = friend
    }

}