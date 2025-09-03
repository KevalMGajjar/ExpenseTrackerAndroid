package com.example.splitwiseclone.ui.ui_components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.splitwiseclone.rest_api.api_viewmodels.UserApiViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.AddPhoneNumberViewModel

@Composable
fun AddPhoneNumberUi(navHostController: NavHostController, addPhoneNumberViewModel: AddPhoneNumberViewModel, userApiViewModel: UserApiViewModel, currentUserViewModel: CurrentUserViewModel) {

    val phoneNumber by addPhoneNumberViewModel.phoneNumber
    val currentUser by currentUserViewModel.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
        Row {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {addPhoneNumberViewModel.storePhoneNumber(it)},
                label = {Text(text="Phone Number")},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Blue,
                    unfocusedTextColor = Color.Blue
                )
            )
        }
        Row {
            Button(onClick = { userApiViewModel.updateUser(currentUserId = currentUser?.currentUserId, newPhoneNumber = phoneNumber, onSuccess = {
                currentUser?.phoneNumber = phoneNumber
                navHostController.navigate("dashboard")
            })}) {
                Text(text = "Continue")
            }
        }
    }
}