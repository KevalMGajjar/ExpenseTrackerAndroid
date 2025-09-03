package com.example.splitwiseclone.rest_api.api_viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitwiseclone.central.ApiClient
import com.example.splitwiseclone.central.SyncRepository
import com.example.splitwiseclone.rest_api.AddMemberRequest
import com.example.splitwiseclone.rest_api.DeleteGroupMembers
import com.example.splitwiseclone.rest_api.DeleteGroupRequest
import com.example.splitwiseclone.rest_api.GetAllGroupsRequest
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.rest_api.SaveGroupRequest
import com.example.splitwiseclone.rest_api.models.GroupApi
import com.example.splitwiseclone.roomdb.friends.Friend
import com.example.splitwiseclone.roomdb.groups.Group
import com.example.splitwiseclone.roomdb.groups.GroupRepository
import com.example.splitwiseclone.roomdb.groups.Member
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupApiViewModel @Inject constructor(
    private val apiService: RestApiService,
    private val groupRepository: GroupRepository,
    private val syncRepository: SyncRepository,
    private val apiClient: ApiClient
): ViewModel() {

    fun saveGroup(group: GroupApi, onSuccess: () -> Unit): String {

        var groupCreatedId: String = ""

        val request = SaveGroupRequest(
            groupName = group.groupName,
            profilePicture = group.profilePicture,
            groupCreatedByUserId = group.groupCreatedByUserId,
            type = group.groupType
        )
        viewModelScope.launch {
            try {
                groupCreatedId = apiService.addGroup(request).groupId
                syncRepository.syncAllData()
            } catch (e: Exception){
                Log.e("error", "error while adding group", e)
            }
        }
        onSuccess()
        return groupCreatedId

    }

    fun getAllGroups(userId: String?){
        viewModelScope.launch {
            apiClient.getAllGroups(userId)
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

        val request = DeleteGroupRequest(
            groupId = groupId,
            currentUserId = currentUserId
        )

        viewModelScope.launch {
            try {
                apiService.deleteGroup(request)
                syncRepository.syncAllData()
                onSuccess()
            } catch (e: Exception) {
                Log.e("error", "Error while deleting group", e)
            }
        }

    }

    fun deleteMembers(groupId: String, membersIds: List<String>, onSuccess: () -> Unit){

        val request = DeleteGroupMembers(
            groupId = groupId,
            membersIds = membersIds
        )

        viewModelScope.launch {
            try {
                apiService.deleteMembers(request)
                syncRepository.syncAllData()
                onSuccess()
            } catch (e: Exception) {
                Log.e("error", "Error while deleting members $membersIds $groupId", e)
            }
        }

    }

}