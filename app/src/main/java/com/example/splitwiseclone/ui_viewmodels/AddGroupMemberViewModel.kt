package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.entities.Group
import com.example.splitwiseclone.roomdb.groups.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddGroupMemberViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val groupId: String? = savedStateHandle.get<String>("groupId")

    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup = _currentGroup.asStateFlow()

    private val _selectedFriends = MutableStateFlow<List<Friend>>(emptyList())
    val selectedFriends: StateFlow<List<Friend>> = _selectedFriends

    init {
        if (groupId != null) {
            viewModelScope.launch {
                _currentGroup.value = groupRepository.getGroupById(groupId).firstOrNull()
            }
        }
    }

    fun toggleSelectedFriend(friend: Friend){
        val currentSelection = _selectedFriends.value.toMutableList()
        if (currentSelection.contains(friend)) {
            currentSelection.remove(friend)
        } else {
            currentSelection.add(friend)
        }
        _selectedFriends.value = currentSelection
    }

    fun deselectAllFriend() {
        _selectedFriends.value = emptyList()
    }
}