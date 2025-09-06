package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.roomdb.entities.Expense
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.friends.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class FriendProfileUiState(
    val friend: Friend? = null,
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FriendProfileViewModel @Inject constructor(
    friendRepository: FriendRepository,
    expenseRepository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val friendId: StateFlow<String> = savedStateHandle.getStateFlow("friendId", "")

    val uiState: StateFlow<FriendProfileUiState> = friendId.flatMapLatest { id ->
        if (id.isEmpty()) {
            flowOf(FriendProfileUiState(isLoading = false))
        } else {
            combine(
                friendRepository.findFriendById(id),
                expenseRepository.getAllRelatedExpenses(id, id) // This needs a proper currentUserId later
            ) { friend, expenses ->
                FriendProfileUiState(friend, expenses, isLoading = false)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FriendProfileUiState()
    )
}
