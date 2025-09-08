package com.example.splitwiseclone.ui.ui_components.homeui_com

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.ui.ui_components.common.ProfileImage
import com.example.splitwiseclone.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import com.example.splitwiseclone.R
val TextPositive = Color(0xFF008000) // Green
val TextNegative = Color(0xFFD32F2F) // Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsUi(
    navHostController: NavHostController,
    friendsViewModel: FriendsRoomViewModel = hiltViewModel()
) {
    val friends by friendsViewModel.allUser.collectAsState(initial = emptyList())
    val totalBalance = friends.sumOf { it.balanceWithUser }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navHostController.navigate("addNewFriendUi") }) {
                        Icon(painter = painterResource(R.drawable.person_add_24dp_e3e3e3_fill0_wght400_grad0_opsz24), "Add New Friend")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            SummaryHeader(totalBalance)
            Spacer(modifier = Modifier.height(24.dp))
            FriendsSelectionUi(friends, navHostController)
        }
    }
}

@Composable
fun SummaryHeader(totalBalance: Double) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Summary", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Text(
            text = CurrencyUtils.formatCurrency(totalBalance, withSign = true),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FriendsSelectionUi(
    friends: List<Friend>,
    navHostController: NavHostController
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("Overall", "You owe", "You are owed")

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
        if (filteredFriends.isEmpty()) {
            EmptyFriendsView(navHostController = navHostController)
        } else {
            CustomLazyFriendsList(
                items = filteredFriends,
                navHostController = navHostController
            )
        }
    }
}

@Composable
fun FriendItem(
    friend: Friend,
    navHostController: NavHostController
) {
    val balance = friend.balanceWithUser
    val (labelText, amountColor) = when {
        balance > 0 -> "owes you:" to TextPositive
        balance < 0 -> "you owe:" to TextNegative
        else -> "settled up" to Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navHostController.navigate("friendsOuterProfileUi/${friend.friendId}")
            }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileImage(
            model = friend.profilePic,
            contentDescription = "Friend Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = friend.username ?: "Friend", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            // Using a placeholder date for consistency
            Text(text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = labelText, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(
                text = CurrencyUtils.formatCurrency(balance),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@Composable
fun CustomLazyFriendsList(
    items: List<Friend>,
    modifier: Modifier = Modifier,
    navHostController: NavHostController
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items, key = { it.id }) { item ->
            FriendItem(item, navHostController)
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun EmptyFriendsView(navHostController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.person_add_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
            contentDescription = "No Friends Found",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "No friends found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navHostController.navigate("addNewFriendUi") },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Add friends to Expense Tracker")
        }
    }
}