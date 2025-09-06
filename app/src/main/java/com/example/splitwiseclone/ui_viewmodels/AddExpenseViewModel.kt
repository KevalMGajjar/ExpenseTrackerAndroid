package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.rest_api.SplitDto
import com.example.splitwiseclone.roomdb.entities.Group
import com.example.splitwiseclone.roomdb.groups.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String? = savedStateHandle.get<String>("groupId")

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _totalAmount = MutableStateFlow("")
    val totalAmount = _totalAmount.asStateFlow()

    private val _date = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    val date = _date.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId = _selectedGroupId.asStateFlow()

    private val _participants = MutableStateFlow<List<String>>(emptyList())
    val participants = _participants.asStateFlow()

    private val _singleFriendExpenseUserId = MutableStateFlow<String?>(null)
    val singleFriendExpenseUserId = _singleFriendExpenseUserId.asStateFlow()

    private val _splits = MutableStateFlow<List<SplitDto>>(emptyList())
    val splits = _splits.asStateFlow()

    private val _paidByUserIds = MutableStateFlow<List<String>>(emptyList())
    val paidByUserIds = _paidByUserIds.asStateFlow()

    private val _paidByText = MutableStateFlow("You paid")
    val paidByText = _paidByText.asStateFlow()

    private val _splitText = MutableStateFlow("Split equally")
    val splitText = _splitText.asStateFlow()

    init {
        if (groupId != null) {
            viewModelScope.launch {
                groupRepository.getGroupById(groupId).firstOrNull()?.let { group ->
                    val currentUser = _participants.value.firstOrNull()
                    if (currentUser != null) {
                        selectGroup(group, currentUser)
                    }
                }
            }
        }
    }

    private fun recalculateDefaultSplits() {
        val amount = _totalAmount.value.toDoubleOrNull() ?: 0.0
        val allParticipants = _participants.value
        val payers = if (_paidByUserIds.value.isEmpty()) allParticipants.take(1) else _paidByUserIds.value

        if (amount <= 0 || allParticipants.isEmpty() || payers.isEmpty()) {
            _splits.value = emptyList()
            return
        }

        val sharePerPerson = amount / allParticipants.size
        val newSplits = allParticipants
            .filter { it !in payers }
            .map { participantId ->
                SplitDto(
                    owedByUserId = participantId,
                    owedAmount = sharePerPerson,
                    owedToUserId = payers.first()
                )
            }

        _splits.value = newSplits
    }

    fun selectGroup(group: Group, currentUserId: String) {
        if (_selectedGroupId.value == group.id) {
            _selectedGroupId.value = null
            _participants.value = listOf(currentUserId)
        } else {
            _selectedGroupId.value = group.id
            _participants.value = group.members?.mapNotNull { it.userId } ?: listOf(currentUserId)
        }
        commitPayerSelection(listOf(currentUserId), "You paid")
    }

    fun toggleFriendSelection(friendId: String) {
        _selectedGroupId.value = null
        val currentParticipants = _participants.value.toMutableSet()
        if (friendId in currentParticipants) {
            currentParticipants.remove(friendId)
        } else {
            currentParticipants.add(friendId)
        }
        _participants.value = currentParticipants.toList()
        recalculateDefaultSplits()
    }

    fun addCurrentUserToParticipants(id: String) {
        if (id !in _participants.value) {
            _participants.value = listOf(id)
            if (_paidByUserIds.value.isEmpty()) {
                _paidByUserIds.value = listOf(id)
            }
        }
    }

    fun setSingleFriendForSplit(friendId: String) {
        _singleFriendExpenseUserId.value = friendId
    }

    fun storeTotalAmount(newAmount: String) {
        _totalAmount.value = newAmount
        recalculateDefaultSplits()
    }

    fun commitPayerSelection(payers: List<String>, text: String) {
        _paidByUserIds.value = payers
        _paidByText.value = text
        recalculateDefaultSplits()
    }

    fun commitSplitSelection(newSplits: List<SplitDto>, text: String) {
        _splits.value = newSplits.filter { it.owedByUserId != it.owedToUserId }
        _splitText.value = text
    }

    fun resetState() {
        _description.value = ""
        _totalAmount.value = ""
        _date.value = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        _participants.value = emptyList()
        _selectedGroupId.value = null
        _singleFriendExpenseUserId.value = null
        _splits.value = emptyList()
        _paidByUserIds.value = emptyList()
        _paidByText.value = "You paid"
        _splitText.value = "Split equally"
    }

    fun storeDescription(newDescription: String) { _description.value = newDescription }
    fun storeDate(newDate: String) { _date.value = newDate }

    // FIX: Re-added the missing functions for the LaunchedEffect to call.
    fun storeSplit(newSplits: List<SplitDto>) {
        _splits.value = newSplits
    }

    fun storePaidByUserIds(newPayers: List<String>) {
        _paidByUserIds.value = newPayers
    }
}