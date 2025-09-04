package com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.GroupProfileUiState
import com.example.splitwiseclone.ui_viewmodels.GroupProfileViewModel
import com.example.splitwiseclone.ui_viewmodels.MemberBalance
import java.util.*
import kotlin.math.abs

// --- Define Colors ---
val HeaderBlue = Color(0xFF2D3E50)
val ButtonBackground = Color.Gray.copy(alpha = 0.1f)
val TextPositive = Color(0xFF2E8B57)
val TextNegative = Color(0xFFD32F2F)

@Composable
fun GroupOuterProfileUi(
    navController: NavHostController,
    groupProfileViewModel: GroupProfileViewModel = hiltViewModel(),
    currentUserViewModel: CurrentUserViewModel = hiltViewModel()
) {
    val currentUser by currentUserViewModel.currentUser.collectAsState()
    val uiState by produceState<GroupProfileUiState?>(initialValue = null, currentUser) {
        currentUser?.let { user ->
            groupProfileViewModel.getUiState(user).collect { state ->
                value = state
            }
        }
    }

    var showSettleUpDialog by remember { mutableStateOf(false) }

    if (uiState == null || uiState?.isLoading == true) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        uiState?.group?.let { group ->
            Column(modifier = Modifier.fillMaxSize()) {
                GroupProfileHeader(navController, group, uiState!!.userBalanceInGroup)
                GroupActionButtons(
                    navController = navController,
                    groupId = group.id,
                    onSettleUpClick = { showSettleUpDialog = true }
                )
                MemberList(memberBalances = uiState!!.memberBalances)
            }

            if (showSettleUpDialog) {
                SettleUpMemberDialog(
                    memberBalances = uiState!!.memberBalances.filter { abs(it.balanceWithCurrentUser) > 0.01 },
                    onDismiss = { showSettleUpDialog = false },
                    onMemberSelected = { friendId ->
                        showSettleUpDialog = false
                        navController.navigate("settleUp/$friendId")
                    }
                )
            }
        }
    }
}

@Composable
fun GroupProfileHeader(navController: NavHostController, group: com.example.splitwiseclone.roomdb.groups.Group, userBalance: Double) {
    Surface(color = HeaderBlue, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp).padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
                Text(
                    text = group.groupName ?: "Group", color = Color.White, style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center, modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { navController.navigate("groupSettingsUi/${group.id}") }) { Icon(Icons.Default.Settings, "Settings", tint = Color.White) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Your balance:", color = Color.White.copy(alpha = 0.8f))
            Text(
                text = formatBalance(userBalance),
                color = if (userBalance >= 0) TextPositive else TextNegative,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GroupActionButtons(navController: NavHostController, groupId: String, onSettleUpClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(icon = Icons.Default.Add, text = "Add expense") {
            navController.navigate("addExpense?groupId=$groupId")
        }
        ActionButton(icon = Icons.Default.Add, text = "Settle up", onClick = onSettleUpClick)
        ActionButton(icon = Icons.Default.Person, text = "Add members") { navController.navigate("addNewGroupMemberUi/$groupId") }
    }
}

@Composable
fun ActionButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier.size(60.dp).clip(CircleShape).background(ButtonBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun MemberList(memberBalances: List<MemberBalance>) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(memberBalances) { member ->
            MemberListItem(member)
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
        }
    }
}

@Composable
fun MemberListItem(member: MemberBalance) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = member.profilePic, contentDescription = "Member profile",
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(member.memberName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)

        val balance = member.balanceWithCurrentUser
        val (text, color) = when {
            balance > 0.01 -> "owes you" to TextPositive
            balance < -0.01 -> "you owe" to TextNegative
            else -> "settled up" to Color.Gray
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text, style = MaterialTheme.typography.bodySmall, color = color)
            if (abs(balance) > 0.01) {
                Text(
                    text = formatBalance(balance, withSign = false),
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun SettleUpMemberDialog(
    memberBalances: List<MemberBalance>,
    onDismiss: () -> Unit,
    onMemberSelected: (friendId: String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Settle with...", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                // FIX: Added a heightIn modifier to ensure the LazyColumn has space to render its items.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp), // Give the list a maximum height
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(memberBalances) { member ->
                        MemberListItem(
                            member = member,
                            modifier = Modifier.clickable { onMemberSelected(member.memberId) }
                        )
                        HorizontalDivider()
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

// Overloaded MemberListItem for use inside the dialog
@Composable
fun MemberListItem(member: MemberBalance, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(model = member.profilePic, contentDescription = "Member profile", modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray), contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.width(16.dp))
        Text(member.memberName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(formatBalance(member.balanceWithCurrentUser), fontWeight = FontWeight.Bold, color = if (member.balanceWithCurrentUser >= 0) TextPositive else TextNegative)
    }
}


// --- Utility Function ---
private fun formatBalance(amount: Double, withSign: Boolean = true): String {
    val amountAbs = abs(amount)
    val sign = when {
        amount > 0.01 && withSign -> "+ "
        amount < -0.01 && withSign -> "- "
        else -> ""
    }
    return String.format(Locale.US, "%s$%.2f", sign, amountAbs)
}

