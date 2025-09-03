package com.example.splitwiseclone.ui.ui_components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.splitwiseclone.rest_api.api_viewmodels.UserApiViewModel
import com.example.splitwiseclone.rest_api.models.User
import com.example.splitwiseclone.ui_viewmodels.SignUpViewModel

@Composable
fun SignUpUi(navHostController: NavHostController, signUpViewModel: SignUpViewModel, userApiViewModel: UserApiViewModel) {
    val username by signUpViewModel.username
    val email by signUpViewModel.email
    val password by signUpViewModel.password
    val currencyCode by signUpViewModel.currencyCode
    val phoneNumber by signUpViewModel.phoneNumber
    Column(verticalArrangement = Arrangement.Center) {
        Row {
            OutlinedTextField(
                value = username,
                onValueChange = {signUpViewModel.storeUsername(it)},
                label = {Text(text="Username")},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Blue,
                    unfocusedTextColor = Color.Blue
                )
            )
        }
        Row {
            OutlinedTextField(
                value = email,
                onValueChange = {signUpViewModel.storeEmail(it)},
                label = {Text(text="Email")},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Blue,
                    unfocusedTextColor = Color.Blue
                )
            )
        }
        Row {
            OutlinedTextField(
                value = password,
                onValueChange = {signUpViewModel.storePassword(it)},
                label = {Text(text="Password")},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Blue,
                    unfocusedTextColor = Color.Blue
                )
            )
        }
        Row {
            OutlinedTextField(
                value = currencyCode,
                onValueChange = {signUpViewModel.storeCurrencyCode(it)},
                label = {Text(text="Currency Code")},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Blue,
                    unfocusedTextColor = Color.Blue
                )
            )
        }
        Row {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {signUpViewModel.storePhoneNumber(it)},
                label = {Text(text="Phone Number")},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Blue,
                    unfocusedTextColor = Color.Blue
                )
            )
        }
        Row {
            Button(onClick = {
                Log.d("click", "success")
                navHostController.navigate("dashboard")
                userApiViewModel.registerUser(
                User(
                    username = signUpViewModel.username.value,
                    email = signUpViewModel.email.value,
                    password = signUpViewModel.password.value,
                    currencyCode = signUpViewModel.currencyCode.value,
                    phoneNumber = signUpViewModel.phoneNumber.value
                )

            )}) {
                Text(text="Sign Up")
            }
        }
    }
}