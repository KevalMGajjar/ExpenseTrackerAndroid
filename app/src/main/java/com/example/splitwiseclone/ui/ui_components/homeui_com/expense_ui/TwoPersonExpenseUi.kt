package com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.ui_viewmodels.TwoPersonExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoPersonExpenseUi(
    navController: NavHostController,
    friendId: String,
    // FIX: Receive the parent screen's navigation entry
    parentEntry: NavBackStackEntry
) {
    // FIX: Scope the ViewModel to the parent entry. This is the key to sharing the state.
    // It guarantees that this screen and AddExpenseScreen use the SAME ViewModel instance.
    val twoPersonExpenseViewModel: TwoPersonExpenseViewModel = hiltViewModel(parentEntry)
    val friendsRoomViewModel: FriendsRoomViewModel = hiltViewModel()

    val selectedSplitType by twoPersonExpenseViewModel.selectedSplit.collectAsState()
    var friend by remember { mutableStateOf<Friend?>(null) }
    val allFriends by friendsRoomViewModel.allUser.collectAsState()

    LaunchedEffect(friendId, allFriends) {
        if (friendId.isNotBlank()) {
            friend = allFriends.find { it.friendId == friendId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose split option", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            friend?.let { friendData ->
                UserInfoHeader(friendData)
                Spacer(modifier = Modifier.height(24.dp))

                val options = listOf(
                    SplitOption("1", "You paid, split equally"),
                    SplitOption("2", "You are owed the full amount"),
                    SplitOption("3", "${friendData.username} paid, split equally"),
                    SplitOption("4", "${friendData.username} is owed the full amount")
                )

                options.forEach { option ->
                    SplitOptionRow(
                        text = option.text,
                        isSelected = selectedSplitType == option.id,
                        onClick = {
                            twoPersonExpenseViewModel.selectSplit(option.id)
                            twoPersonExpenseViewModel.selectSplitText(option.text)
                            navController.popBackStack()
                        }
                    )
                    HorizontalDivider()
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (friendId.isNotBlank()) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun UserInfoHeader(friend: Friend) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = friend.profilePic,
            contentDescription = "Friend Profile Picture",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "Splitting with ${friend.username}",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun SplitOptionRow(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow, // Changed for better visual
            contentDescription = "Split Option",
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private data class SplitOption(val id: String, val text: String)