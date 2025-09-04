package com.example.splitwiseclone.ui.ui_components.homeui_com

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.rest_api.api_viewmodels.ExpenseApiViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.FriendApiViewModel
import com.example.splitwiseclone.roomdb.friends.Friend
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.FriendsUiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// Define colors for positive and negative balances
val TextPositive = Color(0xFF008000) // Green
val TextNegative = Color(0xFFD32F2F) // Red

@Composable
fun FriendsUi(
    navHostController: NavHostController,
    friendsViewModel: FriendsRoomViewModel,
    currentUserViewModel: CurrentUserViewModel,
    friendApiViewModel: FriendApiViewModel,
    friendsUiViewModel: FriendsUiViewModel,
    expenseApiViewModel: ExpenseApiViewModel
) {
    val friends by friendsViewModel.allUser.collectAsState(initial = emptyList())
    val totalBalance = friends.sumOf { it.balanceWithUser }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 16.dp)
    ) {
        CustomTopBarFriends(navHostController)
        Spacer(modifier = Modifier.height(16.dp))
        SummaryHeader(totalBalance)
        Spacer(modifier = Modifier.height(24.dp))
        FriendsSelectionUi(friends, navHostController, friendsUiViewModel)
    }
}

@Composable
fun CustomTopBarFriends(navHostController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        IconButton(
            onClick = { navHostController.navigate("addNewFriendUi") },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add New Friend"
            )
        }
        Text(
            text = "Friends",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun SummaryHeader(totalBalance: Double) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Summary",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        Text(
            text = formatTotalCurrency(totalBalance),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FriendsSelectionUi(
    friends: List<Friend>,
    navHostController: NavHostController,
    friendsUiViewModel: FriendsUiViewModel
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("Overall", "I owe", "Owns me")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { selectedIndex = index },
                    selected = index == selectedIndex,
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        val filteredFriends = when (selectedIndex) {
            1 -> friends.filter { it.balanceWithUser < 0 }
            2 -> friends.filter { it.balanceWithUser > 0 }
            else -> friends
        }

        // Check if the filtered list is empty and show the appropriate UI
        if (filteredFriends.isEmpty()) {
            EmptyFriendsView(navHostController = navHostController)
        } else {
            CustomLazyFriendsList(
                items = filteredFriends,
                navHostController = navHostController,
                friendsUiViewModel = friendsUiViewModel
            )
        }
    }
}

@Composable
fun EmptyFriendsView(navHostController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "No Friends Found",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No friends found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navHostController.navigate("addNewFriendUi") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Add friend to splitwise")
        }
    }
}


@Composable
fun FriendItem(
    friend: Friend,
    navHostController: NavHostController,
    friendsUiViewModel: FriendsUiViewModel
) {
    val balance = friend.balanceWithUser
    val (labelText, amountText, amountColor) = when {
        balance > 0 -> Triple("owns you:", formatCurrency(balance), TextPositive)
        balance < 0 -> Triple("you owe:", formatCurrency(balance), TextNegative)
        else -> Triple("settled up", "", Color.Gray)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                friendsUiViewModel.selectFriend(friend)
                navHostController.navigate("friendsOuterProfileUi")
            }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = friend.profilePic,
            contentDescription = "Friend Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.username ?: "Friend",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        if (balance != 0.0) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }
        } else {
            Text(
                text = labelText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}


@Composable
fun CustomLazyFriendsList(
    items: List<Friend>,
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    friendsUiViewModel: FriendsUiViewModel
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items, key = { it.id }) { item ->
            FriendItem(item, navHostController, friendsUiViewModel = friendsUiViewModel)
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        }
    }
}

// --- Utility Functions ---

private fun formatCurrency(amount: Double): String {
    val sign = if (amount > 0) "+" else "-"
    val formattedAmount = String.format(Locale.US, "%.2f", abs(amount))
    return "$sign$$formattedAmount"
}

private fun formatTotalCurrency(amount: Double): String {
    val formattedAmount = String.format(Locale.US, "%.2f", amount)
    return "$$formattedAmount"
}