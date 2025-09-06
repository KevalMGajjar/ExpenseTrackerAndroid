package com.example.splitwiseclone.ui.ui_components.homeui_com.add_new

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.splitwiseclone.rest_api.api_viewmodels.GroupApiViewModel
import com.example.splitwiseclone.rest_api.models.GroupApi
import com.example.splitwiseclone.roomdb.entities.Group
import com.example.splitwiseclone.roomdb.groups.GroupRoomViewModel
import com.example.splitwiseclone.roomdb.entities.Member
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewGroupUi(
    navHostController: NavHostController,
    groupApiViewModel: GroupApiViewModel,
    groupRoomViewModel: GroupRoomViewModel,
    currentUserViewModel: CurrentUserViewModel
) {
    var groupName by remember { mutableStateOf("") }
    var groupType by remember { mutableStateOf("") }
    val currentUser by currentUserViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create group", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Group Icon",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.secondary
            )

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = groupType,
                onValueChange = { groupType = it },
                label = { Text("Group type (e.g., Trip, Home)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = groupName.isNotBlank() && currentUser != null,
                onClick = {
                    val user = currentUser!!
                    val profilePictureUrl = "https://cdn6.aptoide.com/imgs/1/2/2/1221bc0bdd2354b42b293317ff2adbcf_icon.png" // Placeholder

                    val groupApiData = GroupApi(
                        groupName = groupName,
                        groupCreatedByUserId = user.currentUserId,
                        groupType = groupType,
                        profilePicture = profilePictureUrl,
                        isArchived = false
                    )

                    // This is the correct asynchronous pattern.
                    // Your `saveGroup` function in the ViewModel MUST be updated to accept a callback
                    // that provides the new group's ID after the network call succeeds.
                    // The required signature is:
                    // fun saveGroup(group: GroupApi, onSuccess: (newGroupId: String) -> Unit)
                    groupApiViewModel.saveGroup(groupApiData) { newGroupId ->
                        // This block executes ONLY after the server provides the new ID.
                        val initialMember = Member(
                            userId = user.currentUserId,
                            role = "admin",
                            username = user.username,
                            email = user.email,
                            profilePicture = user.profileUrl ?: ""
                        )
                        val newGroup = Group(
                            id = newGroupId, // Use the ID from the server
                            groupName = groupName,
                            groupCreatedByUserId = user.currentUserId,
                            groupType = groupType,
                            profilePicture = profilePictureUrl,
                            isArchived = false,
                            members = listOf(initialMember)
                        )

                        // Save the complete group to the local database and navigate.
                        groupRoomViewModel.insertGroup(newGroup) {
                            navHostController.navigate("groupsUi") {
                                popUpTo("groupsUi") { inclusive = true }
                            }
                        }
                    }
                }
            ) {
                Text("Save Group")
            }
        }
    }
}

