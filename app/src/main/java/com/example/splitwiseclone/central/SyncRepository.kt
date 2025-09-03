package com.example.splitwiseclone.central

import android.util.Log
import com.example.splitwiseclone.rest_api.api_viewmodels.ExpenseApiViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.FriendApiViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.GroupApiViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserRepository
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val currentUserRepository: CurrentUserRepository
){

    suspend fun syncAllData() {

        try {
            val userId = currentUserRepository.currentUser.first()?.currentUserId
            if (userId != null) {
                apiClient.getAllFriends(userId)
                apiClient.getAllExpenses(userId)
                apiClient.getAllGroups(userId)
            }
        }catch (e: Exception) {
            Log.e("error", "Error while syncing data", e)
        }
    }

}