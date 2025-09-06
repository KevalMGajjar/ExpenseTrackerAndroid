package com.example.splitwiseclone.ui.ui_components

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.splitwiseclone.R // Make sure to create this drawable resource
import com.example.splitwiseclone.rest_api.api_viewmodels.UserApiViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.utils.Constants
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException

@Composable
fun WelcomeUi(navHostController: NavHostController, userApiViewModel: UserApiViewModel, currentUserViewModel: CurrentUserViewModel) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    val loginSuccess by userApiViewModel.loginSuccess.collectAsStateWithLifecycle()
    val currentUser by currentUserViewModel.currentUser.collectAsState()

    // Animation for fade-in effect
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    LaunchedEffect(loginSuccess, currentUser) {
        // We only proceed if the login was successful AND the currentUser object has been updated.
        if (loginSuccess && currentUser != null) {
            // Check if the user who just logged in has a phone number.
            if (currentUser?.phoneNumber.isNullOrBlank()) {
                // If not, navigate to the screen to add one.
                navHostController.navigate("addPhoneNumberUi") {
                    popUpTo("welcome") { inclusive = true }
                }
            } else {
                // If they do, navigate to the dashboard.
                navHostController.navigate("dashboard") {
                    popUpTo("welcome") { inclusive = true }
                }
            }
            // Reset the login flag so this doesn't run again on recomposition.
            userApiViewModel.resetLoginStatus()
        }
    }

    // --- Google One-Tap Sign-In Logic ---
    val signInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(Constants.GOOGLE_WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        isLoading = false
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val oneTapClient = Identity.getSignInClient(context)
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    userApiViewModel.loginWithGoogle(idToken)
                } else {
                    Log.e("WelcomeUi", "Google ID Token was null.")
                }
            } catch (e: ApiException) {
                Log.e("WelcomeUi", "Google Sign-In failed with ApiException", e)
            }
        } else {
            Log.w("WelcomeUi", "Google Sign-In was cancelled or failed.")
        }
    }

    // --- UI ---
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .alpha(alphaAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(1.dp)) // Spacer to push content down

            // --- Header Section ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // You would add your app's logo or a relevant icon here
                // For example, using a drawable resource:
                Image(
                    painter = painterResource(id = R.drawable.ic_splitwise_logo), // Placeholder - create this drawable
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Welcome to Splitwise",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Share bills and expenses with ease, so everyone gets paid back.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }

            // --- Action Buttons Section ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Google Sign-In Button
                Button(
                    onClick = {
                        isLoading = true
                        Identity.getSignInClient(context).beginSignIn(signInRequest)
                            .addOnSuccessListener { result ->
                                try {
                                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                                    activityResultLauncher.launch(intentSenderRequest)
                                } catch (e: Exception) {
                                    isLoading = false
                                    Log.e("WelcomeUi", "Couldn't start One Tap UI: ${e.localizedMessage}")
                                }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                Log.e("WelcomeUi", "Google Sign-In begin failed", e)
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo), // Placeholder - create this drawable
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sign in with Google")
                        }
                    }
                }

                // Sign Up and Log In Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { navHostController.navigate("signup") }) {
                        Text("Sign Up")
                    }
                    TextButton(onClick = { navHostController.navigate("login") }) {
                        Text("Log In")
                    }
                }
            }
        }
    }
}