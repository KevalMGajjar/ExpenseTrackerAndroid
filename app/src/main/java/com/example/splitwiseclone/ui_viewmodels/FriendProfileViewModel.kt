package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.roomdb.entities.Expense
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import com.example.splitwiseclone.roomdb.friends.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class FriendProfileUiState(
    val friend: Friend? = null,
    val relatedExpenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FriendProfileViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val expenseRepository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val friendId: StateFlow<String?> = savedStateHandle.getStateFlow("friendId", null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FriendProfileUiState> = friendId.filterNotNull().flatMapLatest { id ->
        combine(
            friendRepository.findFriendById(id),
            friendRepository.findFriendById(id).filterNotNull().flatMapLatest { friend ->
                expenseRepository.getAllRelatedExpenses(friend.currentUserId, id)
            }
        ) { friend, expenses ->
            FriendProfileUiState(
                friend = friend,
                relatedExpenses = expenses,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FriendProfileUiState()
    )
}