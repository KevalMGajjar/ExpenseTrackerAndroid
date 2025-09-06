package com.example.splitwiseclone.roomdb.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.datastore.DataStoreManager
import com.example.splitwiseclone.rest_api.api_viewmodels.UserApiViewModel
import com.example.splitwiseclone.rest_api.security.TokenManager
import com.example.splitwiseclone.roomdb.entities.CurrentUser
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import com.example.splitwiseclone.roomdb.friends.FriendRepository
import com.example.splitwiseclone.roomdb.groups.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CurrentUserViewModel @Inject constructor(
    private val repository: CurrentUserRepository,
    val friendRepository: FriendRepository,
    val groupRepository: GroupRepository,
    val dataStoreManager: DataStoreManager,
    val expenseRepository: ExpenseRepository,
    val tokenManager: TokenManager
): ViewModel() {

    val currentUser: StateFlow<CurrentUser?> = repository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun updateUser(user: CurrentUser, onSuccess: () -> Unit) {
        viewModelScope.launch {

            repository.updateUser(user)
            onSuccess()
        }
    }



    fun logoutCurrentUser(onSuccess: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clearTokens()
            repository.deleteCurrentUser()
            friendRepository.deleteAllFriends()
            groupRepository.deleteAllGroups()
            expenseRepository.deleteAllExpenses()
            dataStoreManager.saveLoginStatus(false)
            onSuccess()
        }
    }

    fun deleteAllLocalUserData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCurrentUser()

            onSuccess()
        }
    }
}