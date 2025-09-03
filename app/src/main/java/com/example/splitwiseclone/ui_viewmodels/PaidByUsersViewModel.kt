package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class Participant(
    val id: String,
    val name: String
)

@HiltViewModel
class PaidByViewModel @Inject constructor() : ViewModel() {

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants = _participants.asStateFlow()

    private val _selectedPayerId = MutableStateFlow<String?>(null)
    val selectedPayerId = _selectedPayerId.asStateFlow()

    private val _payerAmounts = MutableStateFlow<Map<String, String>>(emptyMap())
    val payerAmounts = _payerAmounts.asStateFlow()

    fun setParticipants(
        currentUser: Participant,
        friends: List<Participant>
    ) {
        if (_participants.value.isEmpty()) {
            _participants.value = listOf(currentUser) + friends
            _selectedPayerId.value = currentUser.id
        }
    }

    fun selectSinglePayer(userId: String) {
        _selectedPayerId.value = userId
    }

    fun updatePayerAmount(userId: String, amount: String) {
        val newAmounts = _payerAmounts.value.toMutableMap()
        newAmounts[userId] = amount
        _payerAmounts.value = newAmounts
    }
}