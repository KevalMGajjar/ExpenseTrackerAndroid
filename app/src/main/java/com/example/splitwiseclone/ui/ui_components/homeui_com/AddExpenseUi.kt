package com.example.splitwiseclone.ui.ui_components.homeui_com

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.rest_api.SplitDto
import com.example.splitwiseclone.rest_api.api_viewmodels.ExpenseApiViewModel
import com.example.splitwiseclone.roomdb.entities.*
import com.example.splitwiseclone.roomdb.expense.ExpenseRoomViewModel
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.roomdb.groups.GroupRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.AddExpenseViewModel
import com.example.splitwiseclone.ui_viewmodels.PaidByViewModel
import com.example.splitwiseclone.ui_viewmodels.Participant
import com.example.splitwiseclone.ui_viewmodels.SplitOptionsViewModel
import com.example.splitwiseclone.ui_viewmodels.TwoPersonExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import androidx.compose.runtime.livedata.observeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseUi(
    navController: NavHostController,
    expenseApiViewModel: ExpenseApiViewModel = hiltViewModel(),
    expenseRoomViewModel: ExpenseRoomViewModel = hiltViewModel(),
    currentUserViewModel: CurrentUserViewModel = hiltViewModel(),
    friendsRoomViewModel: FriendsRoomViewModel = hiltViewModel(),
    groupRoomViewModel: GroupRoomViewModel = hiltViewModel()
) {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry("expense_flow")
    }

    val addExpenseViewModel: AddExpenseViewModel = hiltViewModel(parentEntry)
    val twoPersonExpenseViewModel: TwoPersonExpenseViewModel = hiltViewModel(parentEntry)
    val paidByViewModel: PaidByViewModel = hiltViewModel(parentEntry)
    val splitOptionsViewModel: SplitOptionsViewModel = hiltViewModel(parentEntry)

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
            addExpenseViewModel.commitPayerSelection(payers, text)
            savedStateHandle.remove<Map<String, List<String>>>("payer_result")
        }
    }

    val splitResult = savedStateHandle?.getLiveData<List<SplitDto>>("split_result")?.observeAsState()
    LaunchedEffect(splitResult) {
        splitResult?.value?.let { newSplits ->
            addExpenseViewModel.commitSplitSelection(newSplits, "Split Customly")
            savedStateHandle.remove<List<SplitDto>>("split_result")
        }
    }

    val description by addExpenseViewModel.description.collectAsState()
    val totalAmount by addExpenseViewModel.totalAmount.collectAsState()
    val currentUser by currentUserViewModel.currentUser.collectAsState()
    val date by addExpenseViewModel.date.collectAsState()
    val allFriends by friendsRoomViewModel.allUser.collectAsState()
    val allGroups by groupRoomViewModel.allGroups.collectAsState()
    val participants by addExpenseViewModel.participants.collectAsState()
    val selectedGroupId by addExpenseViewModel.selectedGroupId.collectAsState()
    val splits by addExpenseViewModel.splits.collectAsState()
    val paidByUserIds by addExpenseViewModel.paidByUserIds.collectAsState()
    val twoPersonSplitType by twoPersonExpenseViewModel.selectedSplit.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            addExpenseViewModel.addCurrentUserToParticipants(user.currentUserId)
        }
    }

    LaunchedEffect(twoPersonSplitType, totalAmount, participants, currentUser) {
        if (participants.size == 2 && totalAmount.isNotBlank() && currentUser != null) {
            val amount = totalAmount.toDoubleOrNull() ?: 0.0
            val friendId = participants.find { it != currentUser!!.currentUserId } ?: return@LaunchedEffect
            val currentUserId = currentUser!!.currentUserId

            val newSplits = when (twoPersonSplitType) {
                "1" -> listOf(SplitDto(owedByUserId = friendId, owedAmount = amount / 2, owedToUserId = currentUserId))
                "2" -> listOf(SplitDto(owedByUserId = friendId, owedAmount = amount, owedToUserId = currentUserId))
                "3" -> listOf(SplitDto(owedByUserId = currentUserId, owedAmount = amount / 2, owedToUserId = friendId))
                "4" -> listOf(SplitDto(owedByUserId = currentUserId, owedAmount = amount, owedToUserId = friendId))
                else -> emptyList()
            }
            val newPaidBy = if (twoPersonSplitType in listOf("3", "4")) listOf(friendId) else listOf(currentUserId)
            addExpenseViewModel.storeSplit(newSplits)
            addExpenseViewModel.storePaidByUserIds(newPaidBy)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    TextButton(onClick = {
                        if (currentUser != null && totalAmount.isNotBlank() && description.isNotBlank()) {
                            val finalSplitType = when {
                                participants.size > 2 -> "CUSTOM"
                                twoPersonSplitType != "1" -> "CUSTOM"
                                else -> "EQUAL"
                            }
                            expenseApiViewModel.addExpense(
                                groupId = selectedGroupId,
                                createdByUserId = currentUser!!.currentUserId, totalExpense = totalAmount.toDouble(),
                                description = description, expenseDate = date, currencyCode = "INR",
                                splitType = finalSplitType,
                                splits = splits, paidByUserIds = paidByUserIds, participants = participants,
                                onSuccess = { newExpense ->
                                    expenseRoomViewModel.insertExpense(
                                        Expense(
                                            id = newExpense.id, groupId = newExpense.groupId, createdById = currentUser!!.currentUserId, totalExpense = newExpense.totalExpense,
                                            description = newExpense.description, splitType = newExpense.splitType,
                                            splits = newExpense.splits.map { Splits(id = it.id, owedByUserId = it.owedByUserId, owedAmount = it.owedAmount, owedToUserId = it.owedToUserId) },
                                            isDeleted = newExpense.deleted, currencyCode = newExpense.currencyCode, paidByUserIds = newExpense.paidByUserIds,
                                            participants = newExpense.participants, expenseDate = newExpense.expenseDate
                                        ),
                                        onSuccess = {
                                            addExpenseViewModel.resetState()
                                            twoPersonExpenseViewModel.selectSplit("1")
                                            navController.navigate("dashboard")
                                        }
                                    )
                                }
                            )
                        }
                    }) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = description, onValueChange = { addExpenseViewModel.storeDescription(it) }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Menu, null) })
                    OutlinedTextField(value = totalAmount, onValueChange = { addExpenseViewModel.storeTotalAmount(it) }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), leadingIcon = { Text("₹", fontSize = 18.sp) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ParticipantSelector(
                        allFriends = allFriends, allGroups = allGroups,
                        participants = participants, selectedGroupId = selectedGroupId,
                        currentUser = currentUser,
                        onFriendSelected = { friendId -> addExpenseViewModel.toggleFriendSelection(friendId) },
                        onGroupSelected = { group, userId -> addExpenseViewModel.selectGroup(group, userId) }
                    )
                    DatePickerField(date = date, onDateSelected = { addExpenseViewModel.storeDate(it) })
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            ActionButtons(
                navController = navController,
                addExpenseViewModel = addExpenseViewModel,
                twoPersonExpenseViewModel = twoPersonExpenseViewModel,
                paidByViewModel = paidByViewModel,
                splitOptionsViewModel = splitOptionsViewModel,
                allFriends = allFriends,
                currentUser = currentUser
            )
        }
    }
}

@Composable
fun ParticipantSelector(
    allFriends: List<Friend>, allGroups: List<Group>,
    participants: List<String>, selectedGroupId: String?,
    currentUser: CurrentUser?,
    onFriendSelected: (String) -> Unit, onGroupSelected: (Group, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredFriends = allFriends.filter { it.username?.contains(searchQuery, ignoreCase = true) == true }
    val filteredGroups = allGroups.filter { it.groupName?.contains(searchQuery, ignoreCase = true) == true }

    Column {
        OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = "Participants")
                Spacer(modifier = Modifier.width(8.dp))
                Text("With you and ${max(0, participants.size - 1)} others")
                Spacer(modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown, null)
            }
        }

        AnimatedVisibility(visible = expanded) {
            Surface(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(8.dp), tonalElevation = 4.dp) {
                Column {
                    OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, label = { Text("Search friends or groups") }, modifier = Modifier.fillMaxWidth().padding(8.dp), leadingIcon = { Icon(Icons.Default.Search, null) })
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp).padding(horizontal = 8.dp)) {
                        items(filteredGroups) { group ->
                            SelectableRow(
                                name = group.groupName ?: "Group", imageUrl = group.profilePicture,
                                isSelected = group.id == selectedGroupId,
                                onSelect = {
                                    currentUser?.let { user ->
                                        onGroupSelected(group, user.currentUserId)
                                    }
                                }
                            )
                        }
                        items(filteredFriends) { friend ->
                            SelectableRow(
                                name = friend.username ?: "Friend", imageUrl = friend.profilePic,
                                isSelected = friend.friendId in participants && selectedGroupId == null,
                                onSelect = { onFriendSelected(friend.friendId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableRow(name: String, imageUrl: String, isSelected: Boolean, onSelect: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = imageUrl, contentDescription = name, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray))
        Spacer(Modifier.width(16.dp))
        Text(name, modifier = Modifier.weight(1f))
        Checkbox(checked = isSelected, onCheckedChange = null)
    }
}

@Composable
fun ActionButtons(
    navController: NavHostController, addExpenseViewModel: AddExpenseViewModel,
    twoPersonExpenseViewModel: TwoPersonExpenseViewModel,
    paidByViewModel: PaidByViewModel,
    splitOptionsViewModel: SplitOptionsViewModel, allFriends: List<Friend>, currentUser: CurrentUser?
) {
    val participants by addExpenseViewModel.participants.collectAsState()
    val totalAmount by addExpenseViewModel.totalAmount.collectAsState()

    val twoPersonSplitText by twoPersonExpenseViewModel.selectedSplitText.collectAsState()
    val paidByText by addExpenseViewModel.paidByText.collectAsState()
    val splitText by addExpenseViewModel.splitText.collectAsState()

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(date: String, onDateSelected: (String) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { onDateSelected(convertMillisToDate(it)) }; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    OutlinedTextField(
        value = date,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
        label = { Text("Date") },
        trailingIcon = { Icon(Icons.Default.DateRange, "Select date") },
        readOnly = true
    )
}

private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}