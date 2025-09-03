package com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui.ui_components.homeui_com.CustomDatePicker
import com.example.splitwiseclone.ui_viewmodels.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditUi(
    navController: NavHostController,
    viewModel: ExpenseEditViewModel = hiltViewModel(),
    friendsRoomViewModel: FriendsRoomViewModel,
    paidByViewModel: PaidByViewModel,
    splitOptionsViewModel: SplitOptionsViewModel,
    twoPersonExpenseViewModel: TwoPersonExpenseViewModel,
    // ✅ FIX 1: Add the CurrentUserViewModel as a parameter
    currentUserViewModel: CurrentUserViewModel
) {
    // Collect all states from the ViewModel
    val description by viewModel.description.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val date by viewModel.date.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val paidByText by viewModel.paidByText.collectAsState()
    val splitText by viewModel.splitText.collectAsState()

    // Data needed for the UI
    val allFriends by friendsRoomViewModel.allUser.collectAsState()
    val twoPersonSplitText by twoPersonExpenseViewModel.selectedSplitText.collectAsState()
    // ✅ FIX 2: Get the current user to identify them in the participants list
    val currentUser by currentUserViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Expense") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(modifier = Modifier.height(150.dp)) {
                items(allFriends) { friend ->
                    Card(onClick = { viewModel.toggleParticipant(friend.friendId) }) {
                        Row { // Simple friend item UI
                            Text(friend.username ?: "Unknown")
                        }
                    }
                }
            }

            CustomDatePicker(date = date, onDateSelected = { viewModel.onDateSelected(it) })

            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = totalAmount,
                onValueChange = { viewModel.onAmountChange(it) },
                label = { Text("Total Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            when (participants.size) {
                2 -> { // Two-person flow
                    Button(onClick = {
                        navController.navigate("twoPersonExpenseUi")
                    }) {
                        Text(text = twoPersonSplitText)
                    }
                }
                in 3..Int.MAX_VALUE -> { // Multi-person flow
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Paid By: ")
                        Button(onClick = {
                            currentUser?.let { user ->
                                // 3a. Create the single Participant object for the current user
                                val currentUserParticipant = Participant(user.currentUserId, "You")

                                // 3b. Create the List<Participant> for ONLY the friends
                                val friendParticipants = participants
                                    .filter { it != user.currentUserId }
                                    .mapNotNull { friendId ->
                                        allFriends.find { it.friendId == friendId }?.let { friend ->
                                            Participant(friend.friendId, friend.username ?: "")
                                        }
                                    }

                                // ✅ FIX 3: Call the function with two separate arguments
                                paidByViewModel.setParticipants(
                                    currentUser = currentUserParticipant,
                                    friends = friendParticipants
                                )
                                navController.navigate("customPBUUi")
                            }
                        }) {
                            Text(paidByText)
                        }
                        Text("and split: ")
                        Button(onClick = {
                            currentUser?.let { user ->
                                val allParticipantObjects = participants.mapNotNull { pId ->
                                    // Find user in friends list OR check if it's the current user
                                    if (pId == user.currentUserId) {
                                        Participant(user.currentUserId, "You")
                                    } else {
                                        allFriends.find { it.friendId == pId }?.let { friend ->
                                            Participant(friend.friendId, friend.username ?: "")
                                        }
                                    }
                                }
                                splitOptionsViewModel.setInitialData(allParticipantObjects, totalAmount)
                                navController.navigate("customSplitUi")
                            }
                        }) {
                            Text(splitText)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.saveChanges {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}