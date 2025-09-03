package com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.example.splitwiseclone.rest_api.api_viewmodels.FriendApiViewModel
import com.example.splitwiseclone.roomdb.expense.ExpenseRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.ExpenseDetailViewModel
import com.example.splitwiseclone.ui_viewmodels.FriendsUiViewModel

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

    if(currentUser != null && friend != null) {
        Column {
            Row {
                friend?.username?.let { Text(text = it) }
            }
            Row {
                friend?.balanceWithUser?.let { Text(text = it.toString()) }
            }
            Row {
                IconButton(onClick = { navHostController.navigate("friendSettingsUi") }) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                }
            }
            LazyColumn {
                items(relatedExpenses) { expense ->
                    Card(onClick = {
                        navHostController.navigate("expenseDetail/${expense.id}")
                    }) {
                        Row {
                            Column {
                                Text(text = expense.description ?: "Expense")
                                Text(text = expense.expenseDate)
                            }
                            val owedAmount = expense.splits.find { split ->
                                (split.owedToUserId == currentUser!!.currentUserId && split.owedByUserId == friend!!.friendId) || (split.owedToUserId == friend!!.friendId && split.owedByUserId == currentUser!!.currentUserId)
                            }?.owedAmount?.toString()
                            if(owedAmount != null) {
                                Text(text = owedAmount)
                            }else {
                                Text(text = "Settled Up")
                            }
                        }
                    }
                }
            }
        }
    }
}