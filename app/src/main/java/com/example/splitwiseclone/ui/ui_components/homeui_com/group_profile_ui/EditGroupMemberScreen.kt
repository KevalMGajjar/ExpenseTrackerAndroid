package com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.rest_api.api_viewmodels.GroupApiViewModel
import com.example.splitwiseclone.roomdb.entities.Member
import com.example.splitwiseclone.roomdb.groups.GroupRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.GroupProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupMembersScreen(
    navController: NavHostController,
    groupProfileViewModel: GroupProfileViewModel = hiltViewModel(),
    groupApiViewModel: GroupApiViewModel = hiltViewModel(),
    groupRoomViewModel: GroupRoomViewModel = hiltViewModel(),
    currentUserViewModel: CurrentUserViewModel = hiltViewModel()
) {
    val uiState by groupProfileViewModel.uiState.collectAsState()
    val currentUser by currentUserViewModel.currentUser.collectAsState()

    var memberToRemove by remember { mutableStateOf<Member?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Members") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val group = uiState.group
        val localCurrentUser = currentUser

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (group != null && localCurrentUser != null) {
            val isUserAdmin = group.groupCreatedByUserId == localCurrentUser.currentUserId
            if (isUserAdmin) {
                val members = group.members?.filter { it.userId != localCurrentUser.currentUserId }
                LaunchedEffect(members) {
                    if (members?.isEmpty() == true) {
                        navController.navigate("groupsOuterProfileUi/${group.id}") {
                            popUpTo("editGroupMembers/${group.id}") {
                                inclusive = true
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (members != null) {
                        items(members, key = { it.userId!! }) { member ->
                            MemberRow(
                                member = member,
                                onRemoveClick = { memberToRemove = member }
                            )
                            HorizontalDivider()
                        }
                    }
                }

                memberToRemove?.let { member ->
                    AlertDialog(
                        onDismissRequest = { memberToRemove = null },
                        title = { Text("Remove Member?") },
                        text = { Text("Are you sure you want to remove ${member.username} from the group?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val memberIdToRemove = member.userId
                                    if (memberIdToRemove != null) {
                                        groupApiViewModel.deleteMembers(
                                            groupId = group.id,
                                            membersIds = listOf(memberIdToRemove),
                                            onSuccess = {
                                                val updatedMembers = group.members?.filterNot { it.userId == memberIdToRemove }
                                                val updatedGroup = group.copy(members = updatedMembers)
                                                groupRoomViewModel.updateGroup(updatedGroup)
                                                memberToRemove = null
                                            }
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Remove") }
                        },
                        dismissButton = {
                            TextButton(onClick = { memberToRemove = null }) { Text("Cancel") }
                        }
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("You do not have permission to manage members for this group.")
                }
            }
        }
    }
}

@Composable
private fun MemberRow(member: Member, onRemoveClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = member.profilePicture,
                contentDescription = "Member profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(member.username, fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(onClick = onRemoveClick) {
            Text("Remove")
        }
    }
}