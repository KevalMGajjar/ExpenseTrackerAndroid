package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.ViewModel
import com.example.splitwiseclone.roomdb.friends.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class FriendsUiViewModel @Inject constructor(

): ViewModel() {

    private val _selectedFriend = MutableStateFlow<Friend?>(null)
    val selectedFriend: StateFlow<Friend?> = _selectedFriend

    fun selectFriend(friend: Friend) {
        _selectedFriend.value = friend
    }

}