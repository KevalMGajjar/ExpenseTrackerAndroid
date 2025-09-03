package com.example.splitwiseclone.ui_viewmodels

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.splitwiseclone.rest_api.SplitDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(

): ViewModel() {

    private var _description = MutableStateFlow("")
    var description: StateFlow<String> = _description

    private var _totalAmount = MutableStateFlow("")
    var totalAmount: StateFlow<String> = _totalAmount

    private var _date = MutableStateFlow("")
    var date: StateFlow<String> = _date

    private var _selectedFriends = MutableStateFlow<List<String>>(emptyList())
    var selectedFriends : StateFlow<List<String>> = _selectedFriends

    private var _singleFriendExpenseUserId = MutableStateFlow("")
    var singleFriendExpenseUserId : StateFlow<String> = _singleFriendExpenseUserId

    private var _splits = MutableStateFlow<List<SplitDto>>(emptyList())
    var splits: StateFlow<List<SplitDto>> = _splits

    private var _paidByUserIds = MutableStateFlow<List<String>>(emptyList())
    var paidByUserIds: StateFlow<List<String>> = _paidByUserIds

    private val _paidByText = MutableStateFlow("You")
    val paidByText = _paidByText.asStateFlow()

    private val _splitText = MutableStateFlow("Equal")
    val splitText = _splitText.asStateFlow()

    fun commitPayerSelection(payers: List<String>, text: String) {
        _paidByUserIds.value = payers
        _paidByText.value = text
    }

    fun commitSplitSelection(splits: List<SplitDto>, text: String) {
        _splits.value = splits
        _splitText.value = text
    }

    fun storePaidByUserIds(paidByUserIds: List<String>){
        _paidByUserIds.value = paidByUserIds
    }

    fun storeSplit(splits: List<SplitDto>){
        _splits.value = splits
    }

    fun storeSingleFriend(friendId: String) {
        _singleFriendExpenseUserId.value = friendId
    }

    fun toggleFriends(friendId: String) {
        _selectedFriends.value = if(_selectedFriends.value.contains(friendId)){
            _selectedFriends.value - friendId
        }else {
            _selectedFriends.value + friendId
        }
    }

    fun addCurrentUserToParticipants(id: String) {
        if (!_selectedFriends.value.contains(id)) {
            _selectedFriends.value = _selectedFriends.value + id
        }
    }

    fun storeDate(date: String) {
        _date.value = date
    }

    fun storeTotalAmount(totalAmount: String) {
        _totalAmount.value = totalAmount
    }

    fun storeDescription(description: String) {
        _description.value = description.trim()
    }

    fun deleteSelectedFreinds() {
        _selectedFriends.value = emptyList()
    }
}