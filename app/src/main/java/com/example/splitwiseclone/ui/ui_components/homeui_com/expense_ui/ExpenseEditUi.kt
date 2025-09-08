package com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.splitwiseclone.R
import com.example.splitwiseclone.rest_api.SplitDto
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.roomdb.entities.CurrentUser
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui.ui_components.common.ProfileImage
import com.example.splitwiseclone.ui_viewmodels.*
import kotlin.math.max
import androidx.compose.runtime.livedata.observeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditScreen(
    navController: NavHostController,
    friendsRoomViewModel: FriendsRoomViewModel = hiltViewModel(),
    currentUserViewModel: CurrentUserViewModel = hiltViewModel()
) {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry("expense_flow")
    }

    val viewModel: ExpenseEditViewModel = hiltViewModel(parentEntry)
    val paidByViewModel: PaidByViewModel = hiltViewModel(parentEntry)
    val splitOptionsViewModel: SplitOptionsViewModel = hiltViewModel(parentEntry)
    val twoPersonExpenseViewModel: TwoPersonExpenseViewModel = hiltViewModel(parentEntry)

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    val payerResult = savedStateHandle?.getLiveData<Map<String, List<String>>>("payer_result")?.observeAsState()
    LaunchedEffect(payerResult) {
        payerResult?.value?.let { result ->
            val payers = result["payerIds"] ?: emptyList()
            val text = when (payers.size) {
                0 -> "Select Payer"
                1 -> "Paid by 1 person"
                else -> "Paid by ${payers.size} people"
            }
            viewModel.commitPayerSelection(payers, text)
            savedStateHandle.remove<Map<String, List<String>>>("payer_result")
        }
    }

    val splitResult = savedStateHandle?.getLiveData<List<SplitDto>>("split_result")?.observeAsState()
    LaunchedEffect(splitResult) {
        splitResult?.value?.let { newSplits ->
            viewModel.commitSplitSelection(newSplits, "Split Customly")
            savedStateHandle.remove<List<SplitDto>>("split_result")
        }
    }

    val description by viewModel.description.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val allFriends by friendsRoomViewModel.allUser.collectAsState()
    val currentUser by currentUserViewModel.currentUser.collectAsState()

    val selectedTwoPersonSplit by twoPersonExpenseViewModel.selectedSplit.collectAsState()
    LaunchedEffect(selectedTwoPersonSplit) {
        if (participants.size == 2) {
            viewModel.recalculateForTwoPeople(selectedTwoPersonSplit)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Expense", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.saveChanges { navController.popBackStack() }
                    }) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(painter = painterResource(R.drawable.receipt_long_24dp_e3e3e3_fill0_wght400_grad0_opsz24), null) }
                    )
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = viewModel::onAmountChange,
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Text("₹", fontSize = 18.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.group_add_24dp_e3e3e3_fill0_wght400_grad0_opsz24), contentDescription = "Participants")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("With you and ${max(0, participants.size - 1)} others")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allFriends.filter { it.friendId in participants }) { friend ->
                            ProfileImage(
                                model = friend.profilePic,
                                contentDescription = friend.username,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            ActionButtons(
                navController = navController,
                parentEntry = parentEntry,
                participants = participants,
                totalAmount = totalAmount,
                allFriends = allFriends,
                currentUser = currentUser,
                paidByViewModel = paidByViewModel,
                splitOptionsViewModel = splitOptionsViewModel,
                twoPersonExpenseViewModel = twoPersonExpenseViewModel,
                editViewModel = viewModel
            )
        }
    }
}

@Composable
fun ActionButtons(
    navController: NavHostController,
    parentEntry: NavBackStackEntry?,
    participants: List<String>,
    totalAmount: String,
    allFriends: List<Friend>,
    currentUser: CurrentUser?,
    paidByViewModel: PaidByViewModel,
    splitOptionsViewModel: SplitOptionsViewModel,
    twoPersonExpenseViewModel: TwoPersonExpenseViewModel,
    editViewModel: ExpenseEditViewModel
) {
    val twoPersonSplitText by twoPersonExpenseViewModel.selectedSplitText.collectAsState()
    val paidByText by editViewModel.paidByText.collectAsState()
    val splitText by editViewModel.splitText.collectAsState()

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (participants.size == 2) {
            Button(
                onClick = {
                    val friendId = participants.find { it != currentUser?.currentUserId }
                    if (friendId != null) {
                        navController.navigate("twoPersonExpenseUi/$friendId")
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text(twoPersonSplitText) }
        } else if (participants.size > 2) {
            Button(
                onClick = {
                    currentUser?.let { user ->
                        val currentUserParticipant = Participant(user.currentUserId, "You")
                        val friendParticipants = participants
                            .filter { it != user.currentUserId }
                            .mapNotNull { friendId ->
                                allFriends.find { it.friendId == friendId }
                                    ?.let { friend -> Participant(friend.friendId, friend.username ?: "") }
                            }
                        paidByViewModel.setParticipants(
                            currentUser = currentUserParticipant,
                            friends = friendParticipants
                        )
                        navController.navigate("customPBUUi")
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text(paidByText) }
            Button(
                onClick = {
                    currentUser?.let { user ->
                        val participantObjects = participants.mapNotNull { id ->
                            if (id == user.currentUserId) Participant(id, "You")
                            else allFriends.find { it.friendId == id }?.let { Participant(it.friendId, it.username ?: "") }
                        }
                        splitOptionsViewModel.setInitialData(participantObjects, totalAmount)
                        navController.navigate("customSplitUi")
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text(splitText) }
        }
    }
}