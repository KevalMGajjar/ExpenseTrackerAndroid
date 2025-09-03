package com.example.splitwiseclone.central

import android.util.Log
import com.example.splitwiseclone.rest_api.GetAllExpenseRequest
import com.example.splitwiseclone.rest_api.GetAllFriendsRequest
import com.example.splitwiseclone.rest_api.GetAllGroupsRequest
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.roomdb.expense.Expense
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import com.example.splitwiseclone.roomdb.expense.Splits
import com.example.splitwiseclone.roomdb.friends.FriendRepository
import com.example.splitwiseclone.roomdb.groups.Group
import com.example.splitwiseclone.roomdb.groups.GroupRepository
import com.example.splitwiseclone.roomdb.groups.Member
import javax.inject.Inject
import javax.inject.Singleton

@Singleton  
class ApiClient @Inject constructor(
    private val apiService: RestApiService,
    private val expenseRepository: ExpenseRepository,
    private val friendRepository: FriendRepository,
    private val groupRepository: GroupRepository
) {


    suspend fun getAllExpenses(currentUserId: String) {

        val request = GetAllExpenseRequest(currentUserId)

            try {
                val expenses = apiService.getAllExpenses(request)
                Log.d("success", "Expenses fetched successfully $expenses")
                expenseRepository.deleteAllExpenses()
                expenses.map { expense ->
                    expenseRepository.insertExpense(
                        Expense(
                            id = expense.id,
                            description = expense.description,
                            totalExpense = expense.totalExpense,
                            splits = expense.splits.map { split ->
                                Splits(
                                    id = split.id,
                                    owedByUserId = split.owedByUserId,
                                    owedAmount = split.owedAmount,
                                    owedToUserId = split.owedToUserId
                                )
                            },
                            currencyCode = expense.currencyCode,
                            paidByUserIds = expense.paidByUserIds,
                            participants = expense.participants,
                            groupId = expense.groupId,
                            expenseDate = expense.expenseDate,
                            splitType = expense.splitType,
                            createdById = expense.createdByUserId,
                            isDeleted = expense.deleted
                        )
                    )
                }
            }catch (e: Exception) {
                Log.e("error", "Error while getting all expenses", e)
            }
    }

    suspend fun getAllFriends(id: String?) {
        if(id != null) {
            val request = GetAllFriendsRequest(id)
                try {
                    val allFriends = apiService.getAllFriends(request)
                    friendRepository.deleteAllFriends()
                    allFriends.forEach { friend ->
                        friendRepository.insertFriend(friend)
                    }

                } catch (e: Exception) {
                    Log.e("Error in getting friends", "error", e)

                }
        }
    }
    suspend fun getAllGroups(userId: String?){
        if(userId != null) {
            val request = GetAllGroupsRequest(
                userId
            )
                try {
                    val allGroups = apiService.getAllGroups(request)
                    groupRepository.deleteAllGroups()
                    allGroups.forEach { groupsResponse ->
                        groupRepository.insertGroup(Group(
                            groupName = groupsResponse.groupName,
                            profilePicture = groupsResponse.profilePic,
                            groupCreatedByUserId = groupsResponse.createdByUserId,
                            groupType = groupsResponse.type,
                            members = groupsResponse.members?.map { memberResponse ->
                                Member(
                                    memberResponse.userId,
                                    memberResponse.role,
                                    memberResponse.username,
                                    memberResponse.email,
                                    memberResponse.profilePicture
                                )
                            },
                            isArchived = groupsResponse.archived,
                            id = groupsResponse.groupId
                        ))
                    }
                } catch (e: Exception) {
                    Log.e("error", "Error while getting all groups", e)
                }

        }
    }

}