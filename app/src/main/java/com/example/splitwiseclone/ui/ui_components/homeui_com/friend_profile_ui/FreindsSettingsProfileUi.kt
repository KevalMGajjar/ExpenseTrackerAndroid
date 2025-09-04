package com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.splitwiseclone.rest_api.api_viewmodels.FriendApiViewModel
import com.example.splitwiseclone.roomdb.friends.Friend
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.FriendProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendSettingsUi(
    navController: NavHostController,
    friendProfileViewModel: FriendProfileViewModel = hiltViewModel(),
    friendApiViewModel: FriendApiViewModel = hiltViewModel(),
    currentUserViewModel: CurrentUserViewModel = hiltViewModel(),
    friendsRoomViewModel: FriendsRoomViewModel = hiltViewModel()
) {
    val uiState by friendProfileViewModel.uiState.collectAsState()
    val friend = uiState.friend
    val currentUser by currentUserViewModel.currentUser.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* No title needed for this design */ },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (friend != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                UserInfoHeader(friend)
                Spacer(modifier = Modifier.height(24.dp))

                // Manage Relationship Section
                SectionTitle("Manage relationship")
                HorizontalDivider()
                SettingsActionRow(
                    title = "Delete friend",
                    subtitle = "Remove this user from your friends list, hide any shared groups, and suppress future notifications.",
                    onClick = { showDeleteDialog = true }
                )
                HorizontalDivider()
                SettingsActionRow(
                    title = "Report user",
                    subtitle = "Flag an abusive, suspicious or spam account.",
                    onClick = { /* TODO: Implement report logic */ }
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Shared Groups Section
                SectionTitle("Shared groups")
                HorizontalDivider()
                SharedGroupRow(
                    imageUrl = "https://example.com/group_image.png", // Placeholder image
                    groupName = "Adventurers",
                    subtitle = "since May 2017",
                    onClick = { /* TODO: Navigate to group details */ }
                )
                HorizontalDivider()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (showDeleteDialog && friend != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Friend") },
                text = { Text("Are you sure you want to permanently delete ${friend.username}? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            friendApiViewModel.deleteFriend(
                                currentUserId = currentUser?.currentUserId ?: "",
                                friendId = friend.friendId,
                                onSuccess = {
                                    friendsRoomViewModel.deleteFriend(friend) {}
                                    showDeleteDialog = false
                                    // Navigate all the way back to the main friends list
                                    navController.popBackStack("friendsUi", false)
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun UserInfoHeader(friend: Friend) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = friend.profilePic,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = friend.username ?: "Friend",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${friend.username?.lowercase()}@email.com", // Placeholder email
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsActionRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
private fun SharedGroupRow(
    imageUrl: String,
    groupName: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Group Image",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(groupName, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}