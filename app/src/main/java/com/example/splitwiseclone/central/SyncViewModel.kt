package com.example.splitwiseclone.central

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()
    suspend fun syncAllData() {
            if (_isSyncing.value) return

            _isSyncing.value = true
        try {
            syncRepository.syncAllData()
        } catch (e: Exception) {

        } finally {
            _isSyncing.value = false
        }
    }
}