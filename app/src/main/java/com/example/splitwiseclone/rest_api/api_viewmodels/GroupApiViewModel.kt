package com.example.splitwiseclone.rest_api.api_viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.central.ApiClient
import com.example.splitwiseclone.central.SyncRepository
import com.example.splitwiseclone.rest_api.AddMemberRequest
import com.example.splitwiseclone.rest_api.GroupUpdateRequest
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.rest_api.SaveGroupRequest
import com.example.splitwiseclone.rest_api.models.GroupApi
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.entities.Group
import com.example.splitwiseclone.roomdb.entities.Member
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupApiViewModel @Inject constructor(
    private val apiService: RestApiService,
    private val syncRepository: SyncRepository,
    private val apiClient: ApiClient
): ViewModel() {

    fun saveGroup(group: GroupApi, onSuccess: (newGroupId: String) -> Unit) {

        val request = SaveGroupRequest(
            groupName = group.groupName,
            profilePicture = group.profilePicture,
            groupCreatedByUserId = group.groupCreatedByUserId,
            type = group.groupType
        )

        viewModelScope.launch {
            try {
                val groupCreatedId = apiService.addGroup(request).groupId

                syncRepository.syncAllData()

                onSuccess(groupCreatedId)

            } catch (e: Exception) {
                Log.e("GroupApiViewModel", "Error while adding group", e)
            }
        }
    }

    fun addMembers(members: List<Friend>, groupId: String, onSuccess: () -> Unit) {

        val request = AddMemberRequest(
            groupId = groupId,
            members = members.map { member ->
                Member(
                    userId = member.friendId,
                    role = "member",
                    username = member.username!!,
                    email = member.email,
                    profilePicture = member.profilePic
                )
            }
        )
        viewModelScope.launch {
            try {
                apiService.addMembers(request)
                syncRepository.syncAllData()
                onSuccess()
            } catch (e: Exception) {
                Log.e("error", "Error while adding members", e)
            }
        }

    }

    fun deleteGroup(groupId: String, currentUserId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteGroup(groupId, currentUserId)
                if (response.isSuccessful) {
                    syncRepository.syncAllData()
                    onSuccess()
                } else {
                    Log.e("GroupApiViewModel", "API error deleting group: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GroupApiViewModel", "Error while deleting group", e)
            }
        }
    }

    fun deleteMembers(groupId: String, membersIds: List<String>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteMembers(groupId, membersIds)
                if (response.isSuccessful) {
                    syncRepository.syncAllData()
                    onSuccess()
                } else {
                    Log.e("GroupApiViewModel", "API error deleting members: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GroupApiViewModel", "Error while deleting members $membersIds $groupId", e)
            }
        }
    }

    fun updateGroup(
        groupId: String,
        newName: String,
        onSuccess: () -> Unit,
        onFailure: (errorMessage: String) -> Unit
    ) {
        val request = GroupUpdateRequest(groupId = groupId, groupName = newName)
        viewModelScope.launch {
            try {
                val response = apiService.updateGroup(request)
                if (response.isSuccessful && response.body() != null) {
                    onSuccess()
                } else {
                    onFailure("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                onFailure("Network request failed: ${e.message}")
            }
        }
    }

}