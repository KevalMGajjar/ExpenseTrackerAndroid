package com.example.splitwiseclone.ui.ui_components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.splitwiseclone.central.SyncViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.ExpenseApiViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.FriendApiViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.GroupApiViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.SplashScreenViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(navController: NavController, splashScreenViewModel: SplashScreenViewModel, currentUserViewModel: CurrentUserViewModel, syncViewModel: SyncViewModel) {

    val isLoggedIn = splashScreenViewModel.isLoggedIn.collectAsState(initial = false)

    LaunchedEffect(Unit) {
        delay(1000)
        if (!isLoggedIn.value) {
            navController.navigate("welcome") {
                popUpTo("splash") { inclusive = true }
            }
        } else {

            val currentUser = currentUserViewModel.currentUser.filterNotNull().first()
            syncViewModel.syncAllData()
            navController.navigate("dashboard") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text("SplitWise Clone", fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

