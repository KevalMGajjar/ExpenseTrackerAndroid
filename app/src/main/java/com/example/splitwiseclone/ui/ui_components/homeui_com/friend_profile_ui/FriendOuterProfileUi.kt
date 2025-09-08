package com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.splitwiseclone.roomdb.entities.Expense
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.entities.CurrentUser
import com.example.splitwiseclone.ui_viewmodels.FriendProfileViewModel
import com.example.splitwiseclone.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import com.example.splitwiseclone.R

val HeaderBlue = Color(0xFF2D3E50)
val ButtonBackground = Color.Gray.copy(alpha = 0.1f)
val TextPositive = Color(0xFF2E8B57)
val TextNegative = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendOuterProfileUi(
    navController: NavHostController,
    friendProfileViewModel: FriendProfileViewModel = hiltViewModel()
) {
    val uiState by friendProfileViewModel.uiState.collectAsState()
    Scaffold { padding ->
        if (uiState.isLoading || uiState.friend == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val friend = uiState.friend!!
            val relatedExpenses = uiState.relatedExpenses

            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                FriendProfileHeader(navController, friend)
                ActionButtons(navController, friend)
                TransactionList(
                    expenses = relatedExpenses,
                    currentUser = CurrentUser(currentUserId = friend.currentUserId, username = "You", email = "", profileUrl = "", phoneNumber = null, currencyCode = "", hashedPassword = ""),
                    friend = friend,
                    navHostController = navController
                )
            }
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
            Text(text = "Total balance:", color = Color.White.copy(alpha = 0.8f))
            Text(
                text = CurrencyUtils.formatCurrency(friend.balanceWithUser, withSign = true),
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(
            text = "Settle up",
            icon = { Icon(painter = painterResource(R.drawable.attach_money_24dp_e3e3e3_fill0_wght400_grad0_opsz24), contentDescription = "Settle up") },
            onClick = { navController.navigate("settleUp/${friend.friendId}") }
        )
        ActionButton(
            text = "Send reminder",
            icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send reminder") },
            onClick = { /* TODO */ }
        )
        ActionButton(
            text = "Share payment",
            icon = { Icon(Icons.Default.Share, contentDescription = "Share payment") },
            onClick = { /* TODO */ }
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier.size(60.dp).clip(CircleShape).background(ButtonBackground),
            contentAlignment = Alignment.Center
        ) {
            ProvideTextStyle(value = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.primary)) {
                icon()
            }
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
                1 -> userPortion > 0
                2 -> userPortion < 0
                else -> true
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

    if (abs(userPortion) > 0.01) {
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
                ProvideTextStyle(value = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)) {
                    getCategoryIcon(expense.description ?: "")()
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.description ?: "Expense", fontWeight = FontWeight.Medium)
                Text(text = "You paid ${CurrencyUtils.formatCurrency(expense.totalExpense)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = expense.expenseDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = CurrencyUtils.formatCurrency(userPortion, withSign = true),
                    fontWeight = FontWeight.Bold,
                    color = if (userPortion > 0) TextPositive else TextNegative
                )
            }
        }
    }
}

// --- Utility Functions ---
private fun calculateUserPortion(expense: Expense, currentUser: CurrentUser, friend: Friend): Double {
    val splitOwedToUser = expense.splits.find { it.owedByUserId == friend.friendId && it.owedToUserId == currentUser.currentUserId }
    val splitOwedByUser = expense.splits.find { it.owedByUserId == currentUser.currentUserId && it.owedToUserId == friend.friendId }

    return when {
        splitOwedToUser != null -> splitOwedToUser.owedAmount
        splitOwedByUser != null -> -splitOwedByUser.owedAmount
        else -> 0.0
    }
}

@Composable
private fun getCategoryIcon(description: String): @Composable () -> Unit {
    return when {
        "uber" in description.lowercase() -> { { Icon(painter = painterResource(R.drawable.local_taxi_24dp_e3e3e3_fill0_wght400_grad0_opsz24), "Taxi") } }
        "grocer" in description.lowercase() -> { { Icon(painter = painterResource(R.drawable.shopping_cart_24dp_e3e3e3_fill0_wght400_grad0_opsz24), "Groceries") } }
        "cinema" in description.lowercase() || "movie" in description.lowercase() -> { { Icon(painter = painterResource(R.drawable.movie_24dp_e3e3e3_fill0_wght400_grad0_opsz24), "Movie") } }
        "present" in description.lowercase() || "gift" in description.lowercase() -> { { Icon(painter = painterResource(R.drawable.featured_seasonal_and_gifts_24dp_e3e3e3_fill0_wght400_grad0_opsz24), "Gift") } }
        else -> { { Icon(painterResource(R.drawable.attach_money_24dp_e3e3e3_fill0_wght400_grad0_opsz24), "Default Expense") } } // Use a more appropriate default
    }
}