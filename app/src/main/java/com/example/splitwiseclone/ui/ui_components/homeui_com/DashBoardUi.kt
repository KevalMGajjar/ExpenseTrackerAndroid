package com.example.splitwiseclone.ui.ui_components.homeui_com

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun DashBoardUi(navHostController: NavHostController) {
    Column {
        Row {
            //custom top app bar with profile icon at start, then a App name at the center
            Button(onClick = {navHostController.navigate("profileUi")}) {
                Text(text="profile")
            }
        }
        Row {
            // greeting text and then below it a user name
        }
        Row {
            //the two latest groups that owe u money or u owe money to
        }
        Row {
            //This will have a lazy row with all the latest friends u have connected with
        }
    }

}