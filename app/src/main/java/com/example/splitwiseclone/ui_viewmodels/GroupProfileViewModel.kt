package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.roomdb.entities.Expense
import com.example.splitwiseclone.roomdb.entities.Group
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import com.example.splitwiseclone.roomdb.groups.GroupRepository
import com.example.splitwiseclone.roomdb.user.CurrentUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class MemberBalance(val memberId: String, val memberName: String, val profilePic: String, val balanceWithCurrentUser: Double)
data class GroupProfileUiState(val group: Group? = null, val userBalanceInGroup: Double = 0.0, val memberBalances: List<MemberBalance> = emptyList(), val isLoading: Boolean = true)

@HiltViewModel
class GroupProfileViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val userRepository: CurrentUserRepository,
    private val balanceCalculator: BalanceCalculator, // Assuming you have this class
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: StateFlow<String?> = savedStateHandle.getStateFlow("groupId", null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GroupProfileUiState> =
        combine(groupId.filterNotNull(), userRepository.currentUser.filterNotNull()) { id, user ->
            Pair(id, user)
        }.flatMapLatest { (id, currentUser) ->
            combine(
                groupRepository.getGroupById(id),
                expenseRepository.getExpensesByGroupId(id)
            ) { group: Group?, expenses: List<Expense> ->

                if (group?.members == null) {
                    return@combine GroupProfileUiState(isLoading = false, group = group)
                }

                val localMembers = group.members
                val balanceResult = balanceCalculator.calculateBalances(expenses, currentUser.currentUserId)

                val memberBalances = localMembers
                    ?.filter { it.userId != null && it.userId != currentUser.currentUserId }
                    ?.mapNotNull { member ->
                        member.userId?.let { memberId ->
                            MemberBalance(
                                memberId = memberId,
                                memberName = member.username,
                                profilePic = member.profilePicture,
                                balanceWithCurrentUser = balanceResult.balancePerUser[memberId] ?: 0.0
                            )
                        }
                    }

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