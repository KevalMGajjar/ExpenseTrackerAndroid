package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.ViewModel
import com.example.splitwiseclone.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    val dataStoreManager: DataStoreManager
): ViewModel() {
    
    val isLoggedIn = dataStoreManager.isLoggedIn
}