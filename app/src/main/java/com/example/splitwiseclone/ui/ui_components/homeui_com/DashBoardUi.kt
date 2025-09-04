package com.example.splitwiseclone.ui.ui_components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AccountCircle
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
import com.example.splitwiseclone.roomdb.groups.GroupRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.DashBoardViewModel
import com.example.splitwiseclone.ui_viewmodels.GroupBalanceSummary
import com.example.splitwiseclone.ui_viewmodels.GroupViewModel
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashBoardUi(
    navController: NavHostController,
    homeViewModel: DashBoardViewModel = hiltViewModel(),
    currentUserViewModel: CurrentUserViewModel = hiltViewModel(),
    groupViewModel: GroupViewModel = hiltViewModel()// Added for navigation
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
                title = { Text("Home", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* TODO: Navigate to current user's profile */ }) {
                        if (currentUser?.profileUrl != null && currentUser!!.profileUrl!!.isNotEmpty()) {
                            AsyncImage(
                                model = currentUser!!.profileUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(32.dp)
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
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    TotalBalanceHeader(uiState!!.totalBalance)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    MonthlyExpensesChart(uiState!!.monthlySummaries)
                    Spacer(modifier = Modifier.height(32.dp))
                }
                items(uiState!!.groupSummaries) { groupSummary ->
                    GroupListItem(
                        groupSummary = groupSummary,
                        onClick = {
                            // As requested, navigate to the group profile screen
                            groupViewModel.storeCurrentGroup(groupSummary.group)
                            navController.navigate("groupsOuterProfileUi")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TotalBalanceHeader(totalBalance: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val balanceColor = when {
            totalBalance > 0.01 -> PositiveBalanceColor
            totalBalance < -0.01 -> NegativeBalanceColor
            else -> MaterialTheme.colorScheme.onSurface
        }
        Text(
            text = formatCurrency(totalBalance, withSign = true),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = balanceColor
        )
        Text(
            text = "Your total balance",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun MonthlyExpensesChart(monthlySummaries: List<MonthlyExpenseSummary>) {
    val maxAbsBalance = monthlySummaries.maxOfOrNull { abs(it.balance) }?.takeIf { it > 0 } ?: 1.0
    val chartHeight = 200.dp

    Box(modifier = Modifier.height(chartHeight)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stepCount = 4
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            for (i in 0..stepCount) {
                val y = size.height * (1f - i.toFloat() / stepCount)
                drawLine(
                    color = Color.LightGray,
                    start = Offset(x = 60f, y = y),
                    end = Offset(x = size.width, y = y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = pathEffect
                )
            }
        }

        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom) {
            Column(
                modifier = Modifier
                    .height(chartHeight)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 4 downTo 0) {
                    val labelValue = (maxAbsBalance / 4 * i).roundToInt()
                    Text(text = "$$labelValue", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
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

@Composable
fun RowScope.BarItem(summary: MonthlyExpenseSummary, maxBalance: Double) {
    val barHeightFraction = (abs(summary.balance) / maxBalance).toFloat().coerceIn(0f, 1f)
    val barColor = if (summary.balance >= 0) BarColorPositive else BarColorNegative
    val icon = when {
        summary.balance > 0.01 -> Icons.Default.Add
        summary.balance < -0.01 -> Icons.Default.Clear // Changed icon
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
    val (icon, iconColor, balanceColor) = when {
        balance > 0.01 -> Triple(Icons.Default.Add, PositiveBalanceColor, PositiveBalanceColor)
        balance < -0.01 -> Triple(Icons.Default.Clear, NegativeBalanceColor, NegativeBalanceColor) // Changed icon
        else -> Triple(Icons.Default.Check, SettledUpColor, SettledUpColor)
    }

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
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = "Balance Status", tint = iconColor)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = groupSummary.group.groupName ?: "Group", fontWeight = FontWeight.Medium)
            Text(text = "${groupSummary.group.members?.size} people in this group", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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

