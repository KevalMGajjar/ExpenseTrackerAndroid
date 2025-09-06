package com.example.splitwiseclone.ui.ui_components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.splitwiseclone.R
import com.example.splitwiseclone.central.SyncViewModel
import com.example.splitwiseclone.ui_viewmodels.SplashScreenViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    splashScreenViewModel: SplashScreenViewModel = hiltViewModel(),
    syncViewModel: SyncViewModel = hiltViewModel()
) {
    // FIX: Collect with an initial value of `null` to represent the "loading" state.
    // This requires your SplashScreenViewModel's StateFlow to be nullable (e.g., StateFlow<Boolean?>).
    val isLoggedIn by splashScreenViewModel.isLoggedIn.collectAsState(initial = null)
    var startAnimation by remember { mutableStateOf(false) }

    val scaleAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 800)
    )
    val alphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    // This effect starts the animation as soon as the screen appears.
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // FIX: This new, reactive effect handles the navigation logic.
    // It will run ONLY when `isLoggedIn` changes from its initial `null` state to a non-null value (true or false).
    LaunchedEffect(isLoggedIn) {
        // We wait until the ViewModel has finished checking the login status before proceeding.
        if (isLoggedIn != null) {
            // Wait for a minimum duration to ensure the animation is seen by the user.
            delay(1500)

            val destination = if (isLoggedIn == true) {
                Log.d("s", "st")
                syncViewModel.syncAllData()
                "dashboard"
            } else {
                // If the user is not logged in.
                "welcome"
            }

            // Navigate to the correct destination, clearing the splash screen from the back stack.
            navController.navigate(destination) {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_splitwise_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnimation)
                    .alpha(alphaAnimation)
            )
        }
    }
}