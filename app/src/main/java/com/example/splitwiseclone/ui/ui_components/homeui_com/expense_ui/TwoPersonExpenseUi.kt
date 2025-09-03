package com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui

import android.R.attr.text
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.ui_viewmodels.AddExpenseViewModel
import com.example.splitwiseclone.ui_viewmodels.TwoPersonExpenseViewModel

@Composable
fun TwoPersonExpenseUi(navHostController: NavHostController, twoPersonExpenseViewModel: TwoPersonExpenseViewModel, addExpenseViewModel: AddExpenseViewModel, friendsRoomViewModel: FriendsRoomViewModel) {
    val friendId by addExpenseViewModel.singleFriendExpenseUserId.collectAsState()

    LaunchedEffect(friendId) {
        friendsRoomViewModel.loadFriend(friendId)
    }

    val friend by friendsRoomViewModel.selectedFriend.collectAsState()
    val context = LocalContext.current
    friend?.let { friendData ->
        Column {
            Row {
                Card(onClick = {
                    twoPersonExpenseViewModel.selectSplit("1")
                    twoPersonExpenseViewModel.selectSplitText("You Paid, Split Equally")
                    navHostController.popBackStack()
                }) {
                    Text(text = "You Paid, Split Equally")
                }
            }
            Row {
                Card(onClick = {
                    twoPersonExpenseViewModel.selectSplit("2")
                    twoPersonExpenseViewModel.selectSplitText("You are owed the full amount")
                    navHostController.popBackStack()
                }) {
                    Text(text = "You are owed the full amount")
                }
            }
            Row {
                Card(onClick = {
                    twoPersonExpenseViewModel.selectSplit("3")
                    twoPersonExpenseViewModel.selectSplitText("${friendData.username} Paid, Split Equally")
                    navHostController.popBackStack()
                }) {
                    Text(text = "${friendData.username} Paid, Split Equally")
                }
            }
            Row {
                Card(onClick = {
                    twoPersonExpenseViewModel.selectSplit("4")
                    twoPersonExpenseViewModel.selectSplitText("${friendData.username} is owed the full amount")
                    navHostController.popBackStack()
                }) {
                    Text(text = "${friendData.username} is owed the full amount")
                }
            }
            Row {
                Button(onClick = {}) {
                    Text(text = "More Options")
                }
            }
        }
    }
}