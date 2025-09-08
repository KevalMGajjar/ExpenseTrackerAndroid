package com.example.splitwiseclone.rest_api.api_viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.central.ApiClient
import com.example.splitwiseclone.central.SyncRepository
import com.example.splitwiseclone.rest_api.AddExpenseRequest
import com.example.splitwiseclone.rest_api.ExpenseResponse
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.rest_api.SplitDto
import com.example.splitwiseclone.rest_api.UpdateFriendBalance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map

@HiltViewModel
class ExpenseApiViewModel @Inject constructor(
    private val apiService: RestApiService,
    private val syncRepository: SyncRepository,
    private val apiClient: ApiClient
) : ViewModel() {

    fun addExpense(
        groupId: String? = null,
        createdByUserId: String,
        totalExpense: Double,
        description: String,
        splitType: String,
        splits: List<SplitDto>,
        currencyCode: String,
        paidByUserIds: List<String>,
        participants: List<String>,
        expenseDate: String,
        onSuccess: (newExpense: ExpenseResponse) -> Unit
    ) {

        val request = AddExpenseRequest(
            groupId = groupId,
            createdByUserId = createdByUserId,
            totalExpense = totalExpense,
            description = description,
            splitType = splitType,
            splits = splits,
            currencyCode = currencyCode,
            paidByUserIds = paidByUserIds,
            participants = participants,
            expenseDate = expenseDate
        )

        viewModelScope.launch {
            try {
                val newExpense = apiService.addExpense(request)

                val splits = newExpense.splits.map { split ->
                    SplitDto(
                        owedByUserId = split.owedByUserId,
                        owedAmount = split.owedAmount,
                        owedToUserId = split.owedToUserId
                    )
                }
                val balanceRequest = UpdateFriendBalance(splits)
                apiService.updateFriendBalance(balanceRequest)

                syncRepository.syncAllData()
                onSuccess(newExpense)
                Log.d("success", "Expense added successfully $request")
            } catch (e: Exception) {
                Log.e("error", "Error while adding expense", e)
            }
        }
    }

    fun getAllExpenses(currentUserId: String) {
        viewModelScope.launch {
            apiClient.getAllExpenses(currentUserId)
        }
    }

    suspend fun deleteExpense(expenseId: String): Boolean {
        return try {
            val response = apiService.deleteExpense(expenseId)
            if (response.isSuccessful) {
                Log.d("ExpenseApiViewModel", "Successfully deleted expense $expenseId on server.")
                true
            } else {
                Log.e("ExpenseApiViewModel", "Error deleting expense: ${response.code()} - ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("ExpenseApiViewModel", "Exception while deleting expense", e)
            false
        }
    }

}