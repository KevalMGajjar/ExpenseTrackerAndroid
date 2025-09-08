package com.example.splitwiseclone.ui.ui_components.homeui_com.add_new

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.rest_api.api_viewmodels.GroupApiViewModel
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.roomdb.groups.GroupRoomViewModel
import com.example.splitwiseclone.roomdb.entities.Member
import com.example.splitwiseclone.ui_viewmodels.AddGroupMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewGroupMemberUi(
    navHostController: NavHostController,
    friendsRoomViewModel: FriendsRoomViewModel = hiltViewModel(),
    addGroupMemberViewModel: AddGroupMemberViewModel = hiltViewModel(),
    groupApiViewModel: GroupApiViewModel = hiltViewModel(),
    groupRoomViewModel: GroupRoomViewModel = hiltViewModel()
) {
    val friendsList by friendsRoomViewModel.allUser.collectAsState()
    val selectedFriends by addGroupMemberViewModel.selectedFriends.collectAsState()
    val currentGroup by addGroupMemberViewModel.currentGroup.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val existingMemberIds = currentGroup?.members?.mapNotNull { it.userId } ?: emptyList()
    val availableFriends = friendsList.filter { it.friendId !in existingMemberIds }

    val filteredFriends = availableFriends.filter {
        it.username?.contains(searchQuery, ignoreCase = true) == true ||
                it.phoneNumber?.contains(searchQuery, ignoreCase = true) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Members", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedFriends.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        currentGroup?.let { group ->
                            val newMembers = selectedFriends.map { friend ->
                                Member(
                                    userId = friend.friendId,
                                    role = "member",
                                    username = friend.username ?: "",
                                    email = friend.email,
                                    profilePicture = friend.profilePic
                                )
                            }

                            groupApiViewModel.addMembers(
                                selectedFriends,
                                group.id,
                                onSuccess = {
                                    val updatedGroup = group.copy(
                                        members = (group.members ?: emptyList()) + newMembers
                                    )
                                    groupRoomViewModel.updateGroup(updatedGroup) {
                                        navHostController.popBackStack()
                                    }
                                }
                            )
                        }
                    },
                    icon = { Icon(Icons.Default.Add, "Add Members") },
                    text = { Text("Add ${selectedFriends.size} Member" + if (selectedFriends.size > 1) "s" else "") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name or number") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredFriends) { friend ->
                    FriendSelectItem(
                        friend = friend,
                        isSelected = friend in selectedFriends,
                        onFriendClicked = { addGroupMemberViewModel.toggleSelectedFriend(friend) }
                    )
                }
            }
        }
    }
}

@Composable
fun FriendSelectItem(friend: Friend, isSelected: Boolean, onFriendClicked: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onFriendClicked)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = friend.profilePic,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = friend.username ?: "Friend", fontWeight = FontWeight.SemiBold)
                Text(text = friend.phoneNumber ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}