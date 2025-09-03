package com.example.splitwiseclone.ui.ui_components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel

@Composable
fun ProfileUi(navHostController: NavHostController, currentUserViewModel: CurrentUserViewModel) {
    Column {
        Row {
            //profile name and email and profile pic and a edit button at the end
        }
        Row {
            Button(onClick = {currentUserViewModel.logoutCurrentUser { navHostController.navigate("welcome") }}) {
                Text(text = "Logout")
            }
        }
    }
}