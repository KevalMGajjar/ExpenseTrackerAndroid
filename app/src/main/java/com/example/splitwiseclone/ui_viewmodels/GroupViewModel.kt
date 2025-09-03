package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.ViewModel
import com.example.splitwiseclone.roomdb.groups.Group
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(

): ViewModel() {

    private val _currentGroup = MutableStateFlow<Group>(Group("", "", "", "", false, emptyList(), ""))
    val currentGroup = _currentGroup

    fun storeCurrentGroup(group: Group){
        _currentGroup.value = group
    }
}