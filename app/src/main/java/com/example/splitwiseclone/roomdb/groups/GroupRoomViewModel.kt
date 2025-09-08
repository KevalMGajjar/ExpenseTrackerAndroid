package com.example.splitwiseclone.roomdb.groups

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.roomdb.entities.Group
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class GroupRoomViewModel @Inject constructor(
    private val repository: GroupRepository
): ViewModel() {

    val allGroups: StateFlow<List<Group>> = repository.allGroups
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertGroup(group: Group, onSuccess: () -> Unit){
        viewModelScope.launch {
            try {
                repository.insertGroup(group)
                onSuccess()
            }catch (e: Exception){
                Log.e("error", "Error while inserting Group", e)
            }
        }
    }


    fun updateGroup(group: Group, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.updateGroup(group)
                }
                onSuccess()
            } catch (e: Exception) {
                Log.e("error", "Error while updating Group", e)
            }
        }
    }

}