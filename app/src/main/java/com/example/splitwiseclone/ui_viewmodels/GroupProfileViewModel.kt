package com.example.splitwiseclone.ui_viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.roomdb.entities.Expense
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import com.example.splitwiseclone.roomdb.entities.Group
import com.example.splitwiseclone.roomdb.groups.GroupRepository
import com.example.splitwiseclone.roomdb.user.CurrentUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class MemberBalance(

    val memberId: String,

    val memberName: String,

    val profilePic: String,

    val balanceWithCurrentUser: Double

)



data class GroupProfileUiState(

    val group: Group? = null,

    val userBalanceInGroup: Double = 0.0,

    val memberBalances: List<MemberBalance> = emptyList(),

    val isLoading: Boolean = true

)

@HiltViewModel
class GroupProfileViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val userRepository: CurrentUserRepository,
    private val balanceCalculator: BalanceCalculator, // Ensure this is injected
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: StateFlow<String?> = savedStateHandle.getStateFlow("groupId", null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GroupProfileUiState> =
        combine(groupId.filterNotNull(), userRepository.currentUser.filterNotNull()) { id, user ->
            Pair(id, user)
        }.flatMapLatest { (id, currentUser) ->
            Log.d("GROUP_VM", "Attempting to load group with ID: $id")
            combine(
                groupRepository.getGroupById(id),
                expenseRepository.getExpensesByGroupId(id)
            ) { group: Group?, expenses: List<Expense> ->
                Log.d("GROUP_VM", "Group found from repo: ${group?.groupName}")
                Log.d("GROUP_VM", "Group found from repo: ${expenses}")
                if (group?.members == null) {
                    return@combine GroupProfileUiState(isLoading = false, group = group)
                }

                val localMembers = group.members

                // All complex balance calculation is now correctly handled by the service.
                val balanceResult = balanceCalculator.calculateBalances(expenses, currentUser.currentUserId)

                val memberBalances = localMembers
                    ?.filter { it.userId != null && it.userId != currentUser.currentUserId }
                    ?.map { member ->
                        MemberBalance(
                            memberId = member.userId!!,
                            memberName = member.username,
                            profilePic = member.profilePicture,
                            // Get the specific balance for this member from the calculator's result.
                            balanceWithCurrentUser = balanceResult.balancePerUser[member.userId] ?: 0.0
                        )
                    }

                // Emit the final, correctly calculated state.
                GroupProfileUiState(
                    group = group,
                    userBalanceInGroup = balanceResult.totalBalance,
                    memberBalances = memberBalances!!,
                    isLoading = false
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GroupProfileUiState()
        )
}