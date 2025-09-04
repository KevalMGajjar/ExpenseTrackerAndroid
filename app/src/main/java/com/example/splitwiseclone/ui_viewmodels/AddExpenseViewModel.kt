package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.rest_api.SplitDto
import com.example.splitwiseclone.roomdb.groups.Group
import com.example.splitwiseclone.roomdb.groups.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
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

    private val _date = MutableStateFlow("")
    val date = _date.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId = _selectedGroupId.asStateFlow()

    private val _participants = MutableStateFlow<List<String>>(emptyList())
    val participants = _participants.asStateFlow()

    // FIX: Re-added state to hold the friend's ID for a two-person split.
    private val _singleFriendExpenseUserId = MutableStateFlow<String?>(null)
    val singleFriendExpenseUserId = _singleFriendExpenseUserId.asStateFlow()

    private val _splits = MutableStateFlow<List<SplitDto>>(emptyList())
    val splits = _splits.asStateFlow()

    private val _paidByUserIds = MutableStateFlow<List<String>>(emptyList())
    val paidByUserIds = _paidByUserIds.asStateFlow()

    private val _paidByText = MutableStateFlow("You")
    val paidByText = _paidByText.asStateFlow()

    private val _splitText = MutableStateFlow("Equally")
    val splitText = _splitText.asStateFlow()

    init {
        if (groupId != null) {
            viewModelScope.launch {
                groupRepository.getGroupById(groupId).firstOrNull()?.let { group ->
                    selectGroup(group)
                }
            }
        }
    }

    fun selectGroup(group: Group) {
        if (_selectedGroupId.value == group.id) {
            _selectedGroupId.value = null
            // FIX: Reset participants to only contain the current user.
            val currentUser = _participants.value.firstOrNull()
            _participants.value = if(currentUser != null) listOf(currentUser) else emptyList()
        } else {
            _selectedGroupId.value = group.id
            _participants.value = group.members?.mapNotNull { it.userId } ?: emptyList()
        }
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
    }

    fun addCurrentUserToParticipants(id: String) {
        if (id !in _participants.value) {
            _participants.value = listOf(id) + _participants.value
        }
    }

    // FIX: Added function to set the friend for the two-person split screen.
    fun setSingleFriendForSplit(friendId: String) {
        _singleFriendExpenseUserId.value = friendId
    }

    fun resetState() {
        _description.value = ""
        _totalAmount.value = ""
        _date.value = ""
        _participants.value = emptyList()
        _selectedGroupId.value = null
        _singleFriendExpenseUserId.value = null
        _splits.value = emptyList()
        _paidByUserIds.value = emptyList()
        _paidByText.value = "You"
        _splitText.value = "Equally"
    }

    fun storeDescription(newDescription: String) { _description.value = newDescription }
    fun storeTotalAmount(newAmount: String) { _totalAmount.value = newAmount }
    fun storeDate(newDate: String) { _date.value = newDate }
    fun storeSplit(newSplits: List<SplitDto>) { _splits.value = newSplits }
    fun storePaidByUserIds(newPayers: List<String>) { _paidByUserIds.value = newPayers }
    fun commitPayerSelection(payers: List<String>, text: String) {
        _paidByUserIds.value = payers
        _paidByText.value = text
    }
    fun commitSplitSelection(newSplits: List<SplitDto>, text: String) {
        _splits.value = newSplits
        _splitText.value = text
    }
}