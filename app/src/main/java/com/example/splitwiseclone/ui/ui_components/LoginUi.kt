package com.example.splitwiseclone.ui.ui_components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.splitwiseclone.central.SyncViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.UserApiViewModel
import com.example.splitwiseclone.ui_viewmodels.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginUi(navHostController: NavHostController, loginViewModel: LoginViewModel, userApiViewModel: UserApiViewModel, syncViewModel: SyncViewModel) {
    val email by loginViewModel.email
    val password by loginViewModel.password
    val scope = rememberCoroutineScope()

    Column {
        Row {
            OutlinedTextField(
                value = email,
                onValueChange = {loginViewModel.storeEmail(it)},
                label = { Text(text="Email") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Blue,
                    unfocusedTextColor = Color.Blue
                )
            )
        }
        Row {
            OutlinedTextField(
                value = password,
                onValueChange = {loginViewModel.storePassword(it)},
                label = { Text(text="Password") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Blue,
                    unfocusedTextColor = Color.Blue
                )
            )
        }
        Row {
            Button(onClick = {
                userApiViewModel.loginUser(
                    email = loginViewModel.email.value,
                    password = loginViewModel.password.value,
                    onSuccess = { scope.launch {
                        syncViewModel.syncAllData()
                        navHostController.navigate("dashboard")
                    } }
                )
            }) {
                Text(text="Login")
            }
        }
    }
}