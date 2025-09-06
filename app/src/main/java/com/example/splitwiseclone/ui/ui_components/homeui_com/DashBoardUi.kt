package com.example.splitwiseclone.ui.ui_components.homeui_com

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui.ui_components.common.ProfileImage
import com.example.splitwiseclone.ui_viewmodels.DashboardBalances
import com.example.splitwiseclone.ui_viewmodels.DashBoardViewModel
import com.example.splitwiseclone.ui_viewmodels.GroupBalanceSummary
import com.example.splitwiseclone.ui_viewmodels.HomeUiState
import com.example.splitwiseclone.ui_viewmodels.MonthlyExpenseSummary
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

// --- Define Colors ---
val PositiveBalanceColor = Color(0xFF4CAF50)
val NegativeBalanceColor = Color(0xFFF44336)
val SettledUpColor = Color(0xFF9E9E9E)
val BarColorPositive = Color(0xFF81D4FA)
val BarColorNegative = Color(0xFFFFCDD2)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashBoardUi(
    navController: NavHostController,
    homeViewModel: DashBoardViewModel = hiltViewModel(),
    currentUserViewModel: CurrentUserViewModel = hiltViewModel()
) {
    val currentUser by currentUserViewModel.currentUser.collectAsState()
    val uiState by produceState<HomeUiState?>(initialValue = null, currentUser) {
        currentUser?.let { user ->
            homeViewModel.getUiState(user).collect { state ->
                value = state
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("profileUi") }) {
                        // FIX: Changed currentUser.profileUrl to currentUser.profilePicture
                        if (currentUser?.profileUrl?.isNotEmpty() == true) {
                            ProfileImage(
                                model = currentUser?.profileUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState == null || uiState?.isLoading == true) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    // FIX: Replaced the old header with the new, swipeable balance pager.
                    BalancePager(balances = uiState!!.balances)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        MonthlyExpensesChart(uiState!!.monthlySummaries)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Your Groups", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    }
                }
                items(uiState!!.groupSummaries) { groupSummary ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        GroupListItem(
                            groupSummary = groupSummary,
                            onClick = {
                                navController.navigate("groupsOuterProfileUi/${groupSummary.group.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BalancePager(balances: DashboardBalances) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 48.dp),
            modifier = Modifier.height(100.dp)
        ) { page ->
            when (page) {
                0 -> BalanceCard(label = "Total balance", amount = balances.totalBalance)
                1 -> BalanceCard(label = "You owe", amount = balances.totalYouOwe)
                2 -> BalanceCard(label = "You are owed", amount = balances.totalYouAreOwed)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun BalanceCard(label: String, amount: Double) {
    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            val balanceColor = when {
                amount > 0.01 -> PositiveBalanceColor
                amount < -0.01 -> NegativeBalanceColor
                else -> MaterialTheme.colorScheme.onSurface
            }
            Text(
                text = formatCurrency(amount, withSign = true),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )
        }
    }
}

@Composable
fun MonthlyExpensesChart(monthlySummaries: List<MonthlyExpenseSummary>) {
    val maxAbsBalance = monthlySummaries.maxOfOrNull { abs(it.balance) }?.takeIf { it > 0 } ?: 1.0
    val chartHeight = 200.dp

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly Balances", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.height(chartHeight)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stepCount = 4
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    for (i in 0..stepCount) {
                        val y = size.height * (1f - i.toFloat() / stepCount)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(x = 60f, y = y),
                            end = Offset(x = size.width, y = y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = pathEffect
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom) {
                    Column(
                        modifier = Modifier.height(chartHeight).padding(end = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 4 downTo 0) {
                            val labelValue = (maxAbsBalance / 4 * i).roundToInt()
                            Text(text = "$$labelValue", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        monthlySummaries.takeLast(7).forEach { summary ->
                            BarItem(summary = summary, maxBalance = maxAbsBalance)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.BarItem(summary: MonthlyExpenseSummary, maxBalance: Double) {
    val barHeightFraction = (abs(summary.balance) / maxBalance).toFloat().coerceIn(0f, 1f)
    val barColor = if (summary.balance >= 0) BarColorPositive else BarColorNegative
    val icon = when {
        summary.balance > 0.01 -> Icons.Default.Add
        summary.balance < -0.01 -> Icons.Default.Clear
        else -> Icons.Default.Check
    }
    val iconColor = when {
        summary.balance > 0.01 -> PositiveBalanceColor
        summary.balance < -0.01 -> NegativeBalanceColor
        else -> SettledUpColor
    }

    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight(fraction = barHeightFraction)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(barColor)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = summary.month.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun GroupListItem(groupSummary: GroupBalanceSummary, onClick: () -> Unit) {
    val balance = groupSummary.userBalanceInGroup
    val balanceColor = when {
        balance > 0.01 -> PositiveBalanceColor
        balance < -0.01 -> NegativeBalanceColor
        else -> SettledUpColor
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FIX: Show the group's actual profile picture for better UI consistency.
        AsyncImage(
            model = groupSummary.group.profilePicture,
            contentDescription = groupSummary.group.groupName,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = groupSummary.group.groupName ?: "Group", fontWeight = FontWeight.Medium)
            Text(text = "${groupSummary.group.members?.size} people", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text(
            text = if (abs(balance) > 0.01) formatCurrency(balance) else "Settled Up",
            fontWeight = FontWeight.Bold,
            color = balanceColor
        )
    }
}

private fun formatCurrency(amount: Double, withSign: Boolean = false): String {
    val amountAbs = abs(amount)
    val sign = when {
        amount > 0.01 && withSign -> "+ "
        amount < -0.01 -> "- "
        else -> ""
    }
    return String.format(Locale.US, "%s$%.2f", sign, amountAbs).replace("$-", "-$")
}