package com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.splitwiseclone.roomdb.entities.Expense
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.expense.ExpenseRoomViewModel
import com.example.splitwiseclone.roomdb.entities.CurrentUser
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.FriendsUiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// --- Define Colors ---
val HeaderBlue = Color(0xFF2D3E50)
val ButtonBackground = Color.Gray.copy(alpha = 0.1f)
val TextPositive = Color(0xFF2E8B57)
val TextNegative = Color(0xFFD32F2F)

@Composable
fun FriendOuterProfileUi(
    navHostController: NavHostController,
    friendsUiViewModel: FriendsUiViewModel,
    expenseRoomViewModel: ExpenseRoomViewModel,
    currentUserViewModel: CurrentUserViewModel
) {
    val friend by friendsUiViewModel.selectedFriend.collectAsState()
    val currentUser by currentUserViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser, friend) {
        if (currentUser != null && friend != null) {
            expenseRoomViewModel.getAllRelatedExpenses(
                id = currentUser!!.currentUserId,
                friendId = friend!!.friendId
            )
        }
    }
    val relatedExpenses by expenseRoomViewModel.relatedExpenses.collectAsState()

    if (currentUser != null && friend != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            FriendProfileHeader(navHostController, friend!!)
            ActionButtons(navHostController, friend!!)
            TransactionList(
                expenses = relatedExpenses,
                currentUser = currentUser!!,
                friend = friend!!,
                navHostController = navHostController
            )
        }
    } else {
        // Optional: Show a loading indicator or an empty state
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...")
        }
    }
}

@Composable
fun FriendProfileHeader(navController: NavHostController, friend: Friend) {
    Surface(
        color = HeaderBlue,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    text = friend.username ?: "Friend",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { navController.navigate("friendSettingsUi/${friend.friendId}") }) {
                    Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Total:", color = Color.White.copy(alpha = 0.8f))
            Text(
                text = formatBalance(friend.balanceWithUser, isTotal = true),
                color = if (friend.balanceWithUser >= 0) TextPositive else TextNegative,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ActionButtons(navController: NavHostController, friend: Friend) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(icon = Icons.Default.CheckCircle, text = "Settle up") {
            val friendId = friend.friendId
            navController.navigate("settleUp/$friendId") }
        ActionButton(icon = Icons.Default.Send, text = "Send reminder") { /* TODO: Send reminder logic */ }
        ActionButton(icon = Icons.Default.Share, text = "Share payment") { /* TODO: Share payment logic */ }
    }
}

@Composable
fun ActionButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(ButtonBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TransactionList(
    expenses: List<Expense>,
    currentUser: CurrentUser,
    friend: Friend,
    navHostController: NavHostController
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("All", "You are owed", "You owe")

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
    }

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        val filteredExpenses = expenses.filter { expense ->
            val userPortion = calculateUserPortion(expense, currentUser, friend)
            when (selectedIndex) {
                1 -> userPortion > 0 // You are owed
                2 -> userPortion < 0 // You owe
                else -> true // All
            }
        }
        items(filteredExpenses) { expense ->
            TransactionItem(
                expense = expense,
                currentUser = currentUser,
                friend = friend,
                onClick = { navHostController.navigate("expenseDetail/${expense.id}") }
            )
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
        }
    }
}

@Composable
fun TransactionItem(expense: Expense, currentUser: CurrentUser, friend: Friend, onClick: () -> Unit) {
    val userPortion = calculateUserPortion(expense, currentUser, friend)

    if (userPortion != 0.0) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(expense.description ?: ""),
                    contentDescription = "Category",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.description ?: "Expense", fontWeight = FontWeight.Medium)
                Text(text = "You paid ${formatBalance(expense.totalExpense, isTotal = true)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()), // Placeholder date
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = formatBalance(userPortion),
                    fontWeight = FontWeight.Bold,
                    color = if (userPortion > 0) TextPositive else TextNegative
                )
            }
        }
    }
}

// --- Utility Functions ---

private fun calculateUserPortion(expense: Expense, currentUser: CurrentUser, friend: Friend): Double {
    val splitOwedByUser = expense.splits.find { it.owedByUserId == currentUser.currentUserId && it.owedToUserId == friend.friendId }
    val splitOwedToUser = expense.splits.find { it.owedByUserId == friend.friendId && it.owedToUserId == currentUser.currentUserId }

    return when {
        splitOwedToUser != null -> splitOwedToUser.owedAmount
        splitOwedByUser != null -> -splitOwedByUser.owedAmount
        else -> 0.0
    }
}


private fun formatBalance(amount: Double, isTotal: Boolean = false): String {
    val sign = when {
        amount > 0 && !isTotal -> "+"
        amount < 0 -> "-"
        else -> ""
    }
    val formattedAmount = String.format(Locale.US, "%.2f", abs(amount))
    return "$sign$$formattedAmount"
}


private fun getCategoryIcon(description: String): ImageVector {
    return when {
        "uber" in description.lowercase() -> Icons.Default.Person
        "grocer" in description.lowercase() -> Icons.Default.ShoppingCart
        "cinema" in description.lowercase() || "movie" in description.lowercase() -> Icons.Default.ShoppingCart
        "present" in description.lowercase() || "gift" in description.lowercase() -> Icons.Default.ShoppingCart
        else -> Icons.Default.ShoppingCart
    }
}