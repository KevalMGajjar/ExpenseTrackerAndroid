package com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.rest_api.api_viewmodels.GroupApiViewModel
import com.example.splitwiseclone.roomdb.entities.Group
import com.example.splitwiseclone.roomdb.groups.GroupRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.GroupProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSettingsUi( // Renamed for clarity and consistency
    navController: NavHostController,
    groupProfileViewModel: GroupProfileViewModel = hiltViewModel(),
    groupApiViewModel: GroupApiViewModel = hiltViewModel(),
    groupRoomViewModel: GroupRoomViewModel = hiltViewModel(),
    currentUserViewModel: CurrentUserViewModel = hiltViewModel()
) {
    val currentUser by currentUserViewModel.currentUser.collectAsState()
    // FIX: Directly collect the public uiState from the ViewModel.
    // The produceState block is no longer needed.
    val uiState by groupProfileViewModel.uiState.collectAsState()

    val group = uiState.group
    var showConfirmationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (group != null && currentUser != null) {
            val isUserAdmin = group.groupCreatedByUserId == currentUser!!.currentUserId

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                GroupInfoHeader(group)
                Spacer(modifier = Modifier.height(24.dp))

                if (isUserAdmin) {
                    SettingActionRow(
                        title = "Delete Group",
                        subtitle = "This will permanently delete the group and all its expenses.",
                        icon = Icons.Default.Delete,
                        isDestructive = true,
                        onClick = { showConfirmationDialog = true }
                    )
                } else {
                    SettingActionRow(
                        title = "Leave Group",
                        subtitle = "You will be removed from the group and its expenses.",
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        isDestructive = true,
                        onClick = { showConfirmationDialog = true }
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            if (showConfirmationDialog) {
                ConfirmationDialog(
                    isUserAdmin = isUserAdmin,
                    group = group,
                    onDismiss = { showConfirmationDialog = false },
                    onConfirm = {
                        showConfirmationDialog = false
                        if (isUserAdmin) {
                            groupApiViewModel.deleteGroup(
                                group.id, currentUser!!.currentUserId,
                                onSuccess = {
                                    groupRoomViewModel.deleteGroup(group) {
                                        navController.navigate("groupsUi") { popUpTo("groupsUi") { inclusive = true } }
                                    }
                                }
                            )
                        } else {
                            groupApiViewModel.deleteMembers(
                                group.id, listOf(currentUser!!.currentUserId),
                                onSuccess = {
                                    groupRoomViewModel.deleteGroup(group) {
                                        navController.navigate("groupsUi") { popUpTo("groupsUi") { inclusive = true } }
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GroupInfoHeader(group: Group) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = group.profilePicture,
            contentDescription = "Group Profile Picture",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = group.groupName ?: "Group",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else LocalContentColor.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = contentColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = contentColor, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ConfirmationDialog(
    isUserAdmin: Boolean,
    group: Group,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val title = if (isUserAdmin) "Delete Group?" else "Leave Group?"
    val text = if (isUserAdmin) "Are you sure you want to permanently delete '${group.groupName}'? This action cannot be undone."
    else "Are you sure you want to leave '${group.groupName}'?"
    val confirmButtonText = if (isUserAdmin) "Delete" else "Leave"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text(confirmButtonText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}