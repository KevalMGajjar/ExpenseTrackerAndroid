package com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.entities.Group
import com.example.splitwiseclone.ui_viewmodels.FriendSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendSettingsUi(
    navController: NavHostController,
    // Use the new, dedicated ViewModel
    viewModel: FriendSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friend Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading || uiState.friend == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val friend = uiState.friend!!
            val sharedGroups = uiState.sharedGroups

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                item {
                    UserInfoHeader(friend)
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionTitle("Manage relationship")
                    HorizontalDivider()
                }
                item {
                    SettingsActionRow(
                        title = "Delete friend",
                        subtitle = "Remove this user from your friends list.",
                        onClick = { showDeleteDialog = true }
                    )
                    HorizontalDivider()
                }
                item {
                    SettingsActionRow(
                        title = "Report user",
                        subtitle = "Flag an abusive, suspicious or spam account.",
                        onClick = { /* TODO: Implement report logic */ }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    SectionTitle("Shared groups (${sharedGroups.size})")
                    HorizontalDivider()
                }

                // FIX: Display the list of actual shared groups
                items(sharedGroups) { group ->
                    SharedGroupRow(
                        group = group,
                        onClick = { navController.navigate("groupsOuterProfileUi/${group.id}") }
                    )
                    HorizontalDivider()
                }
            }
        }

        if (showDeleteDialog && uiState.friend != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Friend") },
                text = { Text("Are you sure you want to permanently delete ${uiState.friend!!.username}? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteFriend {
                                // On success, navigate all the way back to the main friends list
                                showDeleteDialog = false
                                navController.navigate("friendsUi") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun UserInfoHeader(friend: Friend) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = friend.profilePic,
            contentDescription = "Profile Picture",
            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Text(text = friend.username ?: "Friend", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = friend.email ?: "No email", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsActionRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = if (title == "Delete friend") MaterialTheme.colorScheme.error else Color.Unspecified)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
private fun SharedGroupRow(group: Group, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = group.profilePicture,
            contentDescription = "Group Image",
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(group.groupName ?: "Group", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${group.members?.size ?: 0} members", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}