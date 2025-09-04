package com.example.splitwiseclone.ui_viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.rest_api.RestApiService // FIX: Import the API service
import com.example.splitwiseclone.roomdb.expense.Expense
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import com.example.splitwiseclone.roomdb.friends.Friend
import com.example.splitwiseclone.roomdb.friends.FriendRepository
import com.example.splitwiseclone.roomdb.user.CurrentUser
import com.example.splitwiseclone.roomdb.user.CurrentUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data classes remain the same
data class ParticipantDetails(
    val name: String,
    val profilePic: String
)

data class ExpenseDetailUiState(
    val expense: Expense? = null,
    val userDetailsMap: Map<String, ParticipantDetails> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ExpenseDetailViewModel @Inject constructor(
    // FIX: Inject repositories and the API service directly, NOT other ViewModels.
    private val expenseRepository: ExpenseRepository,
    private val friendRepository: FriendRepository,
    private val userRepository: CurrentUserRepository,
    private val apiService: RestApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expenseId: StateFlow<String?> = savedStateHandle.getStateFlow("expenseId", null)

    val uiState: StateFlow<ExpenseDetailUiState> = expenseId.filterNotNull().flatMapLatest { id ->
        combine(
            expenseRepository.getExpenseById(id),
            friendRepository.allFriends,
            userRepository.currentUser
        ) { expense: Expense?, friends: List<Friend>, currentUser: CurrentUser? ->
            if (expense == null || currentUser == null) {
                return@combine ExpenseDetailUiState(isLoading = true)
            }

            val userMap = mutableMapOf<String, ParticipantDetails>()
            userMap[currentUser.currentUserId] = ParticipantDetails(currentUser.username, currentUser.profileUrl ?: "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Default_pfp.svg/1024px-Default_pfp.svg.png")
            friends.forEach { friend ->
                userMap[friend.friendId] = ParticipantDetails(friend.username ?: "Friend", friend.profilePic)
            }

            ExpenseDetailUiState(
                expense = expense,
                userDetailsMap = userMap,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseDetailUiState()
    )

    fun deleteExpense(onSuccess: () -> Unit) {
        val expenseToDelete = uiState.value.expense ?: return

        viewModelScope.launch {
            // FIX: The API call logic is now self-contained within this ViewModel.
            try {
                val response = apiService.deleteExpense(expenseToDelete.id)
                if (response.isSuccessful) {
                    Log.d("ExpenseDetailViewModel", "Successfully deleted expense on server.")
                    expenseRepository.deleteExpense(expenseToDelete)
                    onSuccess()
                } else {
                    Log.e("ExpenseDetailViewModel", "API error deleting expense: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ExpenseDetailViewModel", "Exception while deleting expense", e)
            }
        }
    }
}
