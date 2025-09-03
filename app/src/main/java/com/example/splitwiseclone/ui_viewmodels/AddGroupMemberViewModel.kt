package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.ViewModel
import com.example.splitwiseclone.roomdb.friends.Friend
import com.example.splitwiseclone.roomdb.groups.Group
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AddGroupMemberViewModel @Inject constructor(

): ViewModel() {

    private val _selectedFriends = MutableStateFlow<List<Friend>>(emptyList())
    val selectedFriends: StateFlow<List<Friend>> = _selectedFriends

    private val _isSelected = MutableStateFlow<Boolean>(false)
    val isSelected = _isSelected

    fun setIsSelected(value: Boolean){
        _isSelected.value = value
    }
    fun toggleSelectedFriend(friend: Friend){
        _selectedFriends.value = if(_selectedFriends.value.contains(friend)) {
            _selectedFriends.value - friend
            } else {
                _selectedFriends.value + friend
            }
    }

    fun deselectAllFriend() {
        _selectedFriends.value = emptyList()
    }
}