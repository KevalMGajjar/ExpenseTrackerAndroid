package com.example.splitwiseclone.roomdb.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsRoomViewModel @Inject constructor(
    private val repository: FriendRepository
): ViewModel() {

    val allUser: StateFlow<List<Friend>> = repository.allFriends
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
    )

    private val _friendId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedFriend: StateFlow<Friend?> = _friendId.flatMapLatest { id ->
        if (id == null) {
            flowOf(null)
        } else {
            repository.findFriendById(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addFriend(friend: Friend) {
        viewModelScope.launch {
            repository.insertFriend(friend)
        }
    }

    fun deleteFriend(friend: Friend, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteFriend(friend)
            onSuccess()
        }
    }

    fun updateFriend(friend: Friend) {
        viewModelScope.launch {
            repository.updateFriend(friend)
        }
    }

    fun loadFriend(id: String) {
        _friendId.value = id
    }

}