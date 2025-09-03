package com.example.splitwiseclone.ui.ui_components.homeui_com.add_new

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.splitwiseclone.rest_api.api_viewmodels.GroupApiViewModel
import com.example.splitwiseclone.rest_api.models.GroupApi
import com.example.splitwiseclone.roomdb.groups.Group
import com.example.splitwiseclone.roomdb.groups.GroupRoomViewModel
import com.example.splitwiseclone.roomdb.groups.Member
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel

@Composable
fun AddNewGroupUi(navHostController: NavHostController, groupApiViewModel: GroupApiViewModel, groupRoomViewModel: GroupRoomViewModel, currentUserViewModel: CurrentUserViewModel) {

    var groupName by remember { mutableStateOf("") }
    var groupType by remember { mutableStateOf("") }
    var profilePicture by remember { mutableStateOf("https://cdn6.aptoide.com/imgs/1/2/2/1221bc0bdd2354b42b293317ff2adbcf_icon.png") }

    val currentUser by currentUserViewModel.currentUser.collectAsState()
    var createdGroupId = ""

    val initialMember =
        Member(
            userId = currentUser?.currentUserId ?: "",
            role = "admin",
            username = currentUser?.username ?: "",
            email = currentUser?.email ?: "",
            profilePicture = currentUser?.profileUrl ?: ""
        )

    val members: MutableList<Member> = ArrayList()
    members.add(initialMember)

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
        Row {
            CustomTopBarAddGroup()
        }
        Row {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text( text = "Group Name")}
            )
        }
        Row {
            OutlinedTextField(
                value = groupType,
                onValueChange = { groupType = it },
                label = { Text( text = "Group Type ")}
            )
        }
        Row {
            Button(onClick = {
                createdGroupId = groupApiViewModel.saveGroup(GroupApi(
                    groupName = groupName,
                    groupCreatedByUserId = currentUser?.currentUserId ?: "",
                    groupType = groupType,
                    profilePicture = profilePicture,
                    isArchived = false
                ), onSuccess = {groupRoomViewModel.insertGroup(Group(
                    id = createdGroupId,
                    groupName = groupName,
                    groupCreatedByUserId = currentUser?.currentUserId ?: "",
                    groupType = groupType,
                    profilePicture = profilePicture,
                    isArchived = false,
                    members = members
                ), onSuccess = { navHostController.navigate("groupsUi")}
                )}
                )
                }) {
                Text( text = "Add Group" )
            }
        }

    }

}

@Composable
fun CustomTopBarAddGroup() {
    Text(text = "Add new Group")
}