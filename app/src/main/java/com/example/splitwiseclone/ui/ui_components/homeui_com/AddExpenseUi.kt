package com.example.splitwiseclone.ui.ui_components.homeui_com

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePickerDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.rest_api.SplitDto
import com.example.splitwiseclone.rest_api.api_viewmodels.ExpenseApiViewModel
import com.example.splitwiseclone.roomdb.expense.Expense
import com.example.splitwiseclone.roomdb.expense.ExpenseRoomViewModel
import com.example.splitwiseclone.roomdb.expense.Splits
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.AddExpenseViewModel
import com.example.splitwiseclone.ui_viewmodels.PaidByViewModel
import com.example.splitwiseclone.ui_viewmodels.Participant
import com.example.splitwiseclone.ui_viewmodels.SplitOptionsViewModel
import com.example.splitwiseclone.ui_viewmodels.TwoPersonExpenseViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.util.Locale
import java.util.TimeZone

@Composable
fun AddExpenseUi(
    navHostController: NavHostController,
    addExpenseViewModel: AddExpenseViewModel,
    expenseApiViewModel: ExpenseApiViewModel,
    expenseRoomViewModel: ExpenseRoomViewModel,
    currentUserViewModel: CurrentUserViewModel,
    friendsRoomViewModel: FriendsRoomViewModel,
    twoPersonExpenseViewModel: TwoPersonExpenseViewModel,
    paidByViewModel: PaidByViewModel,
    splitOptionsViewModel: SplitOptionsViewModel
) {
    val description by addExpenseViewModel.description.collectAsState()
    val totalAmount by addExpenseViewModel.totalAmount.collectAsState()
    val currentUser by currentUserViewModel.currentUser.collectAsState()
    val date by addExpenseViewModel.date.collectAsState()
    val allFriends by friendsRoomViewModel.allUser.collectAsState()
    val selectedFriendIds by addExpenseViewModel.selectedFriends.collectAsState()
    val twoPersonSplitType by twoPersonExpenseViewModel.selectedSplit.collectAsState()
    val twoPersonSplitText by twoPersonExpenseViewModel.selectedSplitText.collectAsState()
    val splits by addExpenseViewModel.splits.collectAsState()
    val paidByUserIds by addExpenseViewModel.paidByUserIds.collectAsState()

    LaunchedEffect(Unit) {

        val user = currentUserViewModel.currentUser.filterNotNull().first()
        addExpenseViewModel.addCurrentUserToParticipants(user.currentUserId)

    }
    LaunchedEffect(twoPersonSplitType, totalAmount, selectedFriendIds) {
        if (selectedFriendIds.isNotEmpty() && totalAmount.isNotBlank() && currentUser != null) {

            val amount = totalAmount.toDoubleOrNull() ?: 0.0
            val friendId = selectedFriendIds[1]
            val currentUserId = currentUser!!.currentUserId

            when (twoPersonSplitType) {
                "1" -> {
                    addExpenseViewModel.storeSplit(
                        splits = listOf(
                            SplitDto(
                                owedByUserId = friendId,
                                owedAmount = (amount.toInt() / 2).toDouble(),
                                owedToUserId = currentUserId
                            )
                        )
                    )
                    addExpenseViewModel.storePaidByUserIds(paidByUserIds = listOf(currentUserId))
                }

                "2" -> {
                    addExpenseViewModel.storeSplit(
                        splits = listOf(
                            SplitDto(
                                owedByUserId = friendId,
                                owedAmount = amount.toDouble(),
                                owedToUserId = currentUserId
                            )
                        )
                    )
                    addExpenseViewModel.storePaidByUserIds(paidByUserIds = listOf(currentUserId))
                }

                "3" -> {
                    addExpenseViewModel.storeSplit(
                        splits = listOf(
                            SplitDto(
                                owedByUserId = currentUser!!.currentUserId,
                                owedAmount = (amount.toInt() / 2).toDouble(),
                                owedToUserId = friendId
                            )
                        )
                    )
                    addExpenseViewModel.storePaidByUserIds(paidByUserIds = listOf(friendId))
                }

                "4" -> {
                    addExpenseViewModel.storeSplit(
                        splits = listOf(
                            SplitDto(
                                owedByUserId = currentUser!!.currentUserId,
                                owedAmount = amount.toDouble(),
                                owedToUserId = friendId
                            )
                        )
                    )
                    addExpenseViewModel.storePaidByUserIds(paidByUserIds = listOf(friendId))
                }
            }
        }
    }
    Column {
        Row() {
            CustomTopAppBar(navHostController)
        }
        Row {
            LazyColumn {
                items(allFriends) {friend ->
                    Card(
                        onClick = {
                            addExpenseViewModel.toggleFriends(friend.friendId)
                        }
                    ) {
                        Row {
                            AsyncImage(
                                model = friend.profilePic,
                                contentDescription = "friend profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                            )
                            Column {
                                friend.username?.let { Text(text = it) }
                                Spacer(modifier = Modifier.height(2.dp))
                                friend.phoneNumber?.let { Text(text = it) }
                            }
                        }
                    }
                }
            }
        }
        Row {
            CustomDatePicker(date = date, onDateSelected = { newDate ->
                    addExpenseViewModel.storeDate(newDate)
                })
        }
        Row {
            OutlinedTextField(
                value = description,
                onValueChange = { addExpenseViewModel.storeDescription(it) },
                label = { Text("description") },
                modifier = Modifier.height(100.dp)
            )
        }
        Row {
            OutlinedTextField(
                value = totalAmount,
                onValueChange = { addExpenseViewModel.storeTotalAmount(it) },
                label = { Text("Total Amount") }
            )
        }
        Row {
            when (selectedFriendIds.size) {
                1 -> {
                    Text("Select one or more friends to continue.")
                }
                2 -> {
                    Button(onClick = {
                        addExpenseViewModel.storeSingleFriend(selectedFriendIds[1])
                        Log.d("friends", "$selectedFriendIds")
                        navHostController.navigate("twoPersonExpenseUi")
                    }) {
                        Text(text = twoPersonSplitText)
                    }
                }
                else -> {
                    val paidByText by addExpenseViewModel.paidByText.collectAsState()
                    val splitText by addExpenseViewModel.splitText.collectAsState()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Paid By: ")
                        Button(
                            onClick = {
                                currentUser?.let { user ->
                                    val currentUserParticipant = Participant(user.currentUserId, "You")

                                    val friendParticipants = selectedFriendIds
                                        .filter { it != user.currentUserId }
                                        .mapNotNull { friendId ->
                                            allFriends.find { it.friendId == friendId }?.let { friend ->
                                                Participant(friend.friendId, friend.username ?: "Unknown")
                                            }
                                        }
                                    paidByViewModel.setParticipants(
                                        currentUser = currentUserParticipant,
                                        friends = friendParticipants
                                    )
                                    navHostController.navigate("CustomPBUUi")
                                }
                            },
                            enabled = selectedFriendIds.isNotEmpty() && currentUser != null
                        ) {
                            Text(paidByText)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(text = "and split: ")
                        Button(
                            onClick = {
                                currentUser?.let { user ->
                                    val currentUserParticipant = Participant(user.currentUserId, "You")
                                    val friendParticipants = selectedFriendIds
                                        .filter { it != user.currentUserId }
                                        .mapNotNull { friendId ->
                                            allFriends.find { it.friendId == friendId }?.let { friend ->
                                                Participant(friend.friendId, friend.username ?: "Unknown")
                                            }
                                        }
                                    val allParticipants = listOf(currentUserParticipant) + friendParticipants

                                    splitOptionsViewModel.setInitialData(allParticipants, totalAmount)
                                    navHostController.navigate("customSplitUi")
                                }
                            },
                            enabled = selectedFriendIds.isNotEmpty() && (totalAmount.toDoubleOrNull() ?: 0.0) > 0.0
                        ) {
                            Text(splitText)
                        }
                    }
                }
            }
        }
        Row {
            Button(onClick = {
                expenseApiViewModel.addExpense(
                    createdByUserId = currentUser!!.currentUserId,
                    totalExpense = totalAmount.toDouble(),
                    description = description,
                    expenseDate = date,
                    currencyCode = "Inr",
                    splitType = "Equal",
                    splits = splits,
                    paidByUserIds = paidByUserIds,
                    participants = selectedFriendIds,
                    onSuccess = { newExpense ->
                        expenseRoomViewModel.insertExpense(
                            Expense(
                                id = newExpense.id,
                                groupId = newExpense.groupId,
                                createdById = currentUser!!.currentUserId,
                                totalExpense = newExpense.totalExpense,
                                description = newExpense.description,
                                splitType = newExpense.splitType,
                                splits = newExpense.splits.map { split ->
                                    Splits(
                                        id = split.id,
                                        owedByUserId = split.owedByUserId,
                                        owedAmount = split.owedAmount,
                                        owedToUserId = split.owedToUserId
                                    )
                                },
                                isDeleted = newExpense.deleted,
                                currencyCode = newExpense.currencyCode,
                                paidByUserIds = newExpense.paidByUserIds,
                                participants = newExpense.participants,
                                expenseDate = newExpense.expenseDate
                            ),
                            onSuccess = {  addExpenseViewModel.storeSplit(emptyList())
                                addExpenseViewModel.storePaidByUserIds(emptyList())
                                addExpenseViewModel.storeDescription("")
                                addExpenseViewModel.storeTotalAmount("")
                                addExpenseViewModel.storeDate("")
                                addExpenseViewModel.storeSingleFriend("")
                                addExpenseViewModel.deleteSelectedFreinds()
                                navHostController.navigate("dashboard") })
                    })
            }) {
                Text("Save")
            }
        }
    }

}

@Composable
fun CustomTopAppBar(navHostController: NavHostController) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)) {
        IconButton(
            onClick = { navHostController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to last ui"
            )
        }
        Text(
            text = "Add an expense", fontWeight = FontWeight.ExtraBold, modifier = Modifier.align(
                Alignment.Center
            )
        )
    }
}

private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePicker(
    modifier: Modifier = Modifier,
    label: String = "Date",
    date: String,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(convertMillisToDate(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = date,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date"
                )
            },
            readOnly = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .clickable { showDatePicker = true }
        )
    }
}
