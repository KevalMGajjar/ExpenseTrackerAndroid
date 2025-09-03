package com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.example.splitwiseclone.rest_api.api_viewmodels.GroupApiViewModel
import com.example.splitwiseclone.roomdb.groups.GroupRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.GroupViewModel

@Composable
fun GroupSettingsUi(
    navHostController: NavHostController,
    groupViewModel: GroupViewModel,
    groupApiViewModel: GroupApiViewModel,
    groupRoomViewModel: GroupRoomViewModel,
    currentUserViewModel: CurrentUserViewModel
) {

    val currentGroup by groupViewModel.currentGroup.collectAsState()
    val currentUser by currentUserViewModel.currentUser.collectAsState()

    Column {
        if (currentGroup.groupCreatedByUserId == currentUser?.currentUserId) {
            Row {
                Button(onClick = {
                    groupApiViewModel.deleteGroup(
                        currentGroup.id, currentUser!!.currentUserId,
                        onSuccess = {
                            groupRoomViewModel.deleteGroup(
                                currentGroup,
                                onSuccess = { navHostController.navigate("groupsUi") })
                        })
                }) {
                    Text(text = "Delete Group")
                }
            }
        } else {
            Row {
                Button(onClick = {
                    groupApiViewModel.deleteMembers(
                        currentGroup.id,
                        listOf(currentUser!!.currentUserId),
                        onSuccess = {
                            groupRoomViewModel.deleteGroup(
                                currentGroup,
                                onSuccess = { navHostController.navigate("groupsUi") })
                        })
                }) {
                    Text(text = "Leave Group")
                }
            }
        }

    }
}