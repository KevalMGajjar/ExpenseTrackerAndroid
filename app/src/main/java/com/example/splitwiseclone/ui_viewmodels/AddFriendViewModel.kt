package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AddFriendViewModel @Inject constructor(

): ViewModel() {

    private val _selectedNumbers = MutableStateFlow<List<String>>(emptyList())
    val selectedNumbers: StateFlow<List<String>> = _selectedNumbers

    fun toggleSelectedFriend(selected: String) {
        _selectedNumbers.value = if(_selectedNumbers.value.contains(selected)){
            _selectedNumbers.value - selected
        } else {
            _selectedNumbers.value + selected
        }
    }

    fun deleteSelectedNumbers() {
        _selectedNumbers.value = emptyList()
    }
}
